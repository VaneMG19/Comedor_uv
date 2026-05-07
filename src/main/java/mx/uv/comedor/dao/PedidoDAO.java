package mx.uv.comedor.dao;

import mx.uv.comedor.model.*;
import mx.uv.comedor.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


/*
  DAO para pedido, detalle_pedido y programacion_pedido.
  El método crearPedidoCompleto() usa transacción para garantizar
  que pedido + detalles + programación + pago se insertan juntos.
 */
public class PedidoDAO {

    private final PlatilloDAO     platilloDAO = new PlatilloDAO();
    private final AlumnoBecadoDAO becadoDAO   = new AlumnoBecadoDAO();

    // CREATE — transacción completa

    /*
      Crea el pedido completo en una sola transacción:
      1. Inserta pedido con folio generado por BD
      2. Inserta cada detalle aplicando beca si corresponde
      3. Si es ANTICIPADO, inserta programacion_pedido
      4. Inserta el pago
      Hace rollback automático si algo falla.
     */
    public Pedido crearPedidoCompleto(Pedido pedido, MetodoPagoEnum metodoPago,
                                       AlumnoBecado becado)
            throws SQLException {

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // 1. Generar folio y calcular totales
                aplicarBecaADetalles(pedido, becado);
                pedido.calcularTotal();

                // 2. Insertar pedido
                Long idPedido = insertarPedido(con, pedido);
                pedido.setIdPedido(idPedido);

                // 3. Insertar detalles
                for (DetallePedido d : pedido.getDetalles()) {
                    d.setIdPedido(idPedido);
                    insertarDetalle(con, d);
                }

                // 4. Si es anticipado, insertar programación
                if (pedido.esProgramado() && pedido.getProgramacion() != null) {
                    pedido.getProgramacion().setIdPedido(idPedido);
                    insertarProgramacion(con, pedido.getProgramacion());
                }

                // 5. Insertar pago
                Pago pago = construirPago(pedido, metodoPago, becado);
                Long idPago = insertarPago(con, pago);
                pago.setIdPago(idPago);
                pedido.setPago(pago);

                con.commit();
                return pedido;

            } catch (Exception e) {
                con.rollback();
                throw new SQLException("Error al crear pedido — rollback: " + e.getMessage(), e);
            }
        }
    }

    // READ

    public Pedido buscarPorId(Long idPedido) throws SQLException {
        String sql = """
            SELECT id_pedido, id_usuario, folio, fecha_creacion,
                   tipo, estado, subtotal, descuento_beca, total, notas
            FROM pedido WHERE id_pedido = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idPedido);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Pedido p = mapearPedido(rs);
                p.setDetalles(obtenerDetalles(p.getIdPedido()));
                if (p.esProgramado()) {
                    p.setProgramacion(obtenerProgramacion(p.getIdPedido()));
                }
                return p;
            }
            return null;
        }
    }

    public Pedido buscarPorFolio(String folio) throws SQLException {
        String sql = """
            SELECT id_pedido, id_usuario, folio, fecha_creacion,
                   tipo, estado, subtotal, descuento_beca, total, notas
            FROM pedido WHERE folio = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, folio);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Pedido p = mapearPedido(rs);
                p.setDetalles(obtenerDetalles(p.getIdPedido()));
                return p;
            }
            return null;
        }
    }

    /*
      Lista los pedidos activos para el empleado de cocina
      usando la vista que ya ordena: anticipados primero.
     */
    public List<Pedido> listarActivos() throws SQLException {
        String sql = """
            SELECT id_pedido, id_usuario, folio, fecha_creacion,
                   tipo, estado, subtotal, descuento_beca, total, notas
            FROM pedido
            WHERE estado IN ('PENDIENTE','PREPARANDO','LISTO')
            ORDER BY
                CASE tipo WHEN 'ANTICIPADO' THEN 0 ELSE 1 END,
                fecha_creacion ASC
            """;

        List<Pedido> lista = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Pedido p = mapearPedido(rs);
                p.setDetalles(obtenerDetalles(p.getIdPedido()));
                if (p.esProgramado()) {
                    p.setProgramacion(obtenerProgramacion(p.getIdPedido()));
                }
                lista.add(p);
            }
        }
        return lista;
    }

    public List<Pedido> listarPorUsuario(Long idUsuario) throws SQLException {
        String sql = """
            SELECT id_pedido, id_usuario, folio, fecha_creacion,
                   tipo, estado, subtotal, descuento_beca, total, notas
            FROM pedido
            WHERE id_usuario = ?
            ORDER BY fecha_creacion DESC
            """;

        List<Pedido> lista = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapearPedido(rs));
        }
        return lista;
    }

    // UPDATE: cambio de estado

    /*
      Cambia el estado del pedido y registra el cambio en estado_pedido_log.
      Solo el empleado de cocina puede cambiar el estado.
     */
    public boolean cambiarEstado(Long idPedido, EstadoPedidoEnum nuevoEstado,
                                  Long idEmpleado, String comentario)
            throws SQLException {

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // 1. Obtener estado actual
                String sqlEstado = "SELECT estado FROM pedido WHERE id_pedido = ?";
                PreparedStatement psEst = con.prepareStatement(sqlEstado);
                psEst.setLong(1, idPedido);
                ResultSet rs = psEst.executeQuery();
                if (!rs.next()) return false;
                EstadoPedidoEnum estadoActual =
                    EstadoPedidoEnum.valueOf(rs.getString("estado"));

                // 2. Actualizar pedido
                String sqlUpd = """
                    UPDATE pedido SET estado = CAST(? AS estado_pedido_enum)
                    WHERE id_pedido = ?
                    """;
                PreparedStatement psUpd = con.prepareStatement(sqlUpd);
                psUpd.setString(1, nuevoEstado.name());
                psUpd.setLong(2, idPedido);
                psUpd.executeUpdate();

                // 3. Registrar en log
                String sqlLog = """
                    INSERT INTO estado_pedido_log
                        (id_pedido, id_empleado, estado_anterior, estado_nuevo, comentario)
                    VALUES (?, ?, CAST(? AS estado_pedido_enum),
                               CAST(? AS estado_pedido_enum), ?)
                    """;
                PreparedStatement psLog = con.prepareStatement(sqlLog);
                psLog.setLong(1, idPedido);
                psLog.setLong(2, idEmpleado);
                psLog.setString(3, estadoActual.name());
                psLog.setString(4, nuevoEstado.name());
                psLog.setString(5, comentario);
                psLog.executeUpdate();

                // 4. Si el nuevo estado es LISTO y es ANTICIPADO →
                //    actualizar estado_prog
                if (nuevoEstado == EstadoPedidoEnum.LISTO) {
                    String sqlProg = """
                        UPDATE programacion_pedido
                        SET estado_prog = 'LISTO_RECOGIDA'
                        WHERE id_pedido = ?
                        """;
                    PreparedStatement psProg = con.prepareStatement(sqlProg);
                    psProg.setLong(1, idPedido);
                    psProg.executeUpdate();
                }

                con.commit();
                return true;

            } catch (Exception e) {
                con.rollback();
                throw new SQLException("Error al cambiar estado: " + e.getMessage(), e);
            }
        }
    }

    public boolean cancelar(Long idPedido) throws SQLException {
        String sql = """
            UPDATE pedido SET estado = 'CANCELADO'
            WHERE id_pedido = ? AND estado NOT IN ('ENTREGADO','CANCELADO')
            """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idPedido);
            return ps.executeUpdate() > 0;
        }
    }

    // Métodos privados de inserción

    private Long insertarPedido(Connection con, Pedido p) throws SQLException {
        String sql = """
            INSERT INTO pedido
                (id_usuario, folio, tipo, estado,
                 subtotal, descuento_beca, total, notas)
            VALUES (?, generar_folio(),
                    CAST(? AS tipo_pedido_enum),
                    CAST(? AS estado_pedido_enum),
                    ?, ?, ?, ?)
            RETURNING id_pedido, folio
            """;

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setLong(1, p.getIdUsuario());
        ps.setString(2, p.getTipo().name());
        ps.setString(3, p.getEstado().name());
        ps.setBigDecimal(4, p.getSubtotal());
        ps.setBigDecimal(5, p.getDescuentoBeca());
        ps.setBigDecimal(6, p.getTotal());
        ps.setString(7, p.getNotas());

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            p.setFolio(rs.getString("folio"));
            return rs.getLong("id_pedido");
        }
        throw new SQLException("No se obtuvo ID al insertar pedido");
    }

    private void insertarDetalle(Connection con, DetallePedido d) throws SQLException {
        String sql = """
            INSERT INTO detalle_pedido
                (id_pedido, id_platillo, cantidad, precio_unitario,
                 subtotal, cubierto_por_beca, personalizaciones)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setLong(1, d.getIdPedido());
        ps.setLong(2, d.getIdPlatillo());
        ps.setInt(3, d.getCantidad());
        ps.setBigDecimal(4, d.getPrecioUnitario());
        ps.setBigDecimal(5, d.getSubtotal());
        ps.setBoolean(6, d.isCubiertoPorBeca());
        ps.setString(7, d.getPersonalizaciones());
        ps.executeUpdate();
    }

    private void insertarProgramacion(Connection con, ProgramacionPedido prog)
            throws SQLException {
        String sql = """
            INSERT INTO programacion_pedido
                (id_pedido, fecha_recogida, hora_recogida,
                 lugar_recogida, minutos_anticipacion, estado_prog)
            VALUES (?, ?, ?, ?, ?, CAST(? AS est_prog_enum))
            """;

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setLong(1, prog.getIdPedido());
        ps.setDate(2, Date.valueOf(prog.getFechaRecogida()));
        ps.setTime(3, Time.valueOf(prog.getHoraRecogida()));
        ps.setString(4, prog.getLugarRecogida());
        ps.setInt(5, prog.getMinutosAnticipacion());
        ps.setString(6, prog.getEstadoProg().name());
        ps.executeUpdate();
    }

    private Long insertarPago(Connection con, Pago pago) throws SQLException {
        String sql = """
            INSERT INTO pago
                (id_pedido, id_usuario, monto, monto_beca, monto_efectivo,
                 metodo_pago, estado, referencia)
            VALUES (?, ?, ?, ?, ?,
                    CAST(? AS metodo_pago_enum),
                    CAST(? AS est_pago_enum), ?)
            RETURNING id_pago
            """;

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setLong(1, pago.getIdPedido());
        ps.setLong(2, pago.getIdUsuario());
        ps.setBigDecimal(3, pago.getMonto());
        ps.setBigDecimal(4, pago.getMontoBeca());
        ps.setBigDecimal(5, pago.getMontoEfectivo());
        ps.setString(6, pago.getMetodoPago().name());
        ps.setString(7, pago.getEstado().name());
        ps.setString(8, pago.getReferencia());

        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getLong("id_pago");
        throw new SQLException("No se obtuvo ID al insertar pago");
    }

    // Helpers de negocio

    /*
      Aplica la beca a los detalles del pedido si el usuario es becado.
      Solo aplica a renglones con platillo tipo=MENU.
     */
    private void aplicarBecaADetalles(Pedido pedido, AlumnoBecado becado)
            throws SQLException {

        if (becado == null || !becado.puedeUsarBeca()) return;

        for (DetallePedido d : pedido.getDetalles()) {
            Platillo p = platilloDAO.buscarPorId(d.getIdPlatillo());
            d.setPlatillo(p);
            if (p != null && p.getTipo() == TipoPlatEnum.MENU) {
                d.aplicarBeca();
            }
        }
    }

    /*
     Construye el objeto Pago con los montos correctos según el método.
     */
    private Pago construirPago(Pedido pedido, MetodoPagoEnum metodo,
                                AlumnoBecado becado) {
        BigDecimal montoBeca = pedido.getDescuentoBeca();
        BigDecimal montoEfectivo = pedido.getTotal();

        // Determinar método real según los montos
        MetodoPagoEnum metodoFinal = metodo;
        if (montoBeca.compareTo(BigDecimal.ZERO) > 0) {
            metodoFinal = montoEfectivo.compareTo(BigDecimal.ZERO) > 0
                ? MetodoPagoEnum.MIXTO
                : MetodoPagoEnum.BECA;
        }

        return new Pago(pedido.getIdPedido(), pedido.getIdUsuario(),
                        pedido.getSubtotal(), montoBeca, metodoFinal);
    }

    private List<DetallePedido> obtenerDetalles(Long idPedido) throws SQLException {
        String sql = """
            SELECT d.id_detalle, d.id_pedido, d.id_platillo, d.cantidad,
                   d.precio_unitario, d.subtotal, d.cubierto_por_beca,
                   d.personalizaciones
            FROM detalle_pedido d
            WHERE d.id_pedido = ?
            """;

        List<DetallePedido> lista = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idPedido);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                DetallePedido d = new DetallePedido();
                d.setIdDetalle(rs.getLong("id_detalle"));
                d.setIdPedido(rs.getLong("id_pedido"));
                d.setIdPlatillo(rs.getLong("id_platillo"));
                d.setCantidad(rs.getInt("cantidad"));
                d.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                d.setSubtotal(rs.getBigDecimal("subtotal"));
                d.setCubiertoPorBeca(rs.getBoolean("cubierto_por_beca"));
                d.setPersonalizaciones(rs.getString("personalizaciones"));
                lista.add(d);
            }
        }
        return lista;
    }

    private ProgramacionPedido obtenerProgramacion(Long idPedido) throws SQLException {
        String sql = """
            SELECT id_programacion, id_pedido, fecha_recogida, hora_recogida,
                   lugar_recogida, recordatorio_enviado,
                   minutos_anticipacion, estado_prog
            FROM programacion_pedido WHERE id_pedido = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idPedido);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ProgramacionPedido prog = new ProgramacionPedido();
                prog.setIdProgramacion(rs.getLong("id_programacion"));
                prog.setIdPedido(rs.getLong("id_pedido"));
                prog.setFechaRecogida(rs.getDate("fecha_recogida").toLocalDate());
                prog.setHoraRecogida(rs.getTime("hora_recogida").toLocalTime());
                prog.setLugarRecogida(rs.getString("lugar_recogida"));
                prog.setRecordatorioEnviado(rs.getBoolean("recordatorio_enviado"));
                prog.setMinutosAnticipacion(rs.getInt("minutos_anticipacion"));
                prog.setEstadoProg(EstProgEnum.valueOf(rs.getString("estado_prog")));
                return prog;
            }
            return null;
        }
    }

    private Pedido mapearPedido(ResultSet rs) throws SQLException {
        Pedido p = new Pedido();
        p.setIdPedido(rs.getLong("id_pedido"));
        p.setIdUsuario(rs.getLong("id_usuario"));
        p.setFolio(rs.getString("folio"));
        p.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
        p.setTipo(TipoPedidoEnum.valueOf(rs.getString("tipo")));
        p.setEstado(EstadoPedidoEnum.valueOf(rs.getString("estado")));
        p.setSubtotal(rs.getBigDecimal("subtotal"));
        p.setDescuentoBeca(rs.getBigDecimal("descuento_beca"));
        p.setTotal(rs.getBigDecimal("total"));
        p.setNotas(rs.getString("notas"));
        return p;
    }
}
