package mx.uv.comedor.dao;

import mx.uv.comedor.model.*;
import mx.uv.comedor.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/*
 DAO para ingrediente, movimiento_inventario,
  compra_anticipada, detalle_compra y alerta_inventario.
 */
public class InventarioDAO {


    //  INGREDIENTE

    public Long insertarIngrediente(Ingrediente i) throws SQLException {
        String sql = """
            INSERT INTO ingrediente
                (nombre, descripcion, unidad_medida, stock_actual,
                 stock_minimo, stock_maximo, precio_unitario, proveedor, categoria)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id_ingrediente
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, i.getNombre());
            ps.setString(2, i.getDescripcion());
            ps.setString(3, i.getUnidadMedida());
            ps.setBigDecimal(4, i.getStockActual());
            ps.setBigDecimal(5, i.getStockMinimo());
            ps.setBigDecimal(6, i.getStockMaximo());
            ps.setBigDecimal(7, i.getPrecioUnitario());
            ps.setString(8, i.getProveedor());
            ps.setString(9, i.getCategoria());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong("id_ingrediente");
            throw new SQLException("No se obtuvo ID al insertar ingrediente");
        }
    }

    public Ingrediente buscarIngredientePorId(Long id) throws SQLException {
        String sql = """
            SELECT id_ingrediente, nombre, descripcion, unidad_medida,
                   stock_actual, stock_minimo, stock_maximo,
                   precio_unitario, proveedor, categoria, activo
            FROM ingrediente WHERE id_ingrediente = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapearIngrediente(rs);
            return null;
        }
    }

    public List<Ingrediente> listarIngredientes() throws SQLException {
        String sql = """
            SELECT id_ingrediente, nombre, descripcion, unidad_medida,
                   stock_actual, stock_minimo, stock_maximo,
                   precio_unitario, proveedor, categoria, activo
            FROM ingrediente
            WHERE activo = TRUE
            ORDER BY nombre
            """;

        List<Ingrediente> lista = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapearIngrediente(rs));
        }
        return lista;
    }

    /*
      Lista ingredientes con stock bajo usando la vista de inventario.
     */
    public List<Ingrediente> listarConStockBajo() throws SQLException {
        String sql = """
            SELECT id_ingrediente, nombre, descripcion, unidad_medida,
                   stock_actual, stock_minimo, stock_maximo,
                   precio_unitario, proveedor, categoria, TRUE AS activo
            FROM vista_inventario
            WHERE estado_stock IN ('BAJO','AGOTADO')
            ORDER BY estado_stock, nombre
            """;

        List<Ingrediente> lista = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapearIngrediente(rs));
        }
        return lista;
    }

    public boolean actualizarIngrediente(Ingrediente i) throws SQLException {
        String sql = """
            UPDATE ingrediente
            SET nombre = ?, descripcion = ?, unidad_medida = ?,
                stock_minimo = ?, stock_maximo = ?,
                precio_unitario = ?, proveedor = ?, categoria = ?
            WHERE id_ingrediente = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, i.getNombre());
            ps.setString(2, i.getDescripcion());
            ps.setString(3, i.getUnidadMedida());
            ps.setBigDecimal(4, i.getStockMinimo());
            ps.setBigDecimal(5, i.getStockMaximo());
            ps.setBigDecimal(6, i.getPrecioUnitario());
            ps.setString(7, i.getProveedor());
            ps.setString(8, i.getCategoria());
            ps.setLong(9, i.getIdIngrediente());
            return ps.executeUpdate() > 0;
        }
    }

    //  MOVIMIENTO INVENTARIO

    /*
      Registra un movimiento de stock.
      El trigger en BD actualiza automáticamente stock_actual del ingrediente
      y genera alerta si el stock queda bajo.
     */
    public Long registrarMovimiento(MovimientoInventario m) throws SQLException {
        String sql = """
            INSERT INTO movimiento_inventario
                (id_ingrediente, id_usuario, tipo, cantidad,
                 stock_resultante, motivo)
            VALUES (?, ?, CAST(? AS tipo_mov_inv_enum), ?, 0, ?)
            RETURNING id_mov
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, m.getIdIngrediente());
            ps.setLong(2, m.getIdUsuario());
            ps.setString(3, m.getTipo().name());
            ps.setBigDecimal(4, m.getCantidad());
            ps.setString(5, m.getMotivo());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong("id_mov");
            throw new SQLException("No se obtuvo ID al registrar movimiento");
        }
    }

    public List<MovimientoInventario> listarMovimientosPorIngrediente(
            Long idIngrediente) throws SQLException {
        String sql = """
            SELECT id_mov, id_ingrediente, id_usuario, tipo,
                   cantidad, stock_resultante, motivo, fecha
            FROM movimiento_inventario
            WHERE id_ingrediente = ?
            ORDER BY fecha DESC
            LIMIT 50
            """;

        List<MovimientoInventario> lista = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idIngrediente);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapearMovimiento(rs));
        }
        return lista;
    }

    //  COMPRA ANTICIPADA

    /*
      Crea una compra con sus detalles en una sola transacción.
     */
    public Long crearCompra(CompraAnticipada compra) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // 1. Insertar compra
                String sqlC = """
                    INSERT INTO compra_anticipada
                        (id_admin, proveedor, fecha_entrega_esperada, estado, notas)
                    VALUES (?, ?, ?, CAST(? AS est_compra_enum), ?)
                    RETURNING id_compra
                    """;

                PreparedStatement psC = con.prepareStatement(sqlC);
                psC.setLong(1, compra.getIdAdmin());
                psC.setString(2, compra.getProveedor());
                psC.setDate(3, Date.valueOf(compra.getFechaEntregaEsperada()));
                psC.setString(4, compra.getEstado().name());
                psC.setString(5, compra.getNotas());

                ResultSet rs = psC.executeQuery();
                if (!rs.next()) throw new SQLException("No se obtuvo ID compra");
                Long idCompra = rs.getLong("id_compra");

                // 2. Insertar detalles
                String sqlD = """
                    INSERT INTO detalle_compra
                        (id_compra, id_ingrediente, cantidad_solicitada, precio_unitario)
                    VALUES (?, ?, ?, ?)
                    """;

                PreparedStatement psD = con.prepareStatement(sqlD);
                for (DetalleCompra d : compra.getDetalles()) {
                    psD.setLong(1, idCompra);
                    psD.setLong(2, d.getIdIngrediente());
                    psD.setBigDecimal(3, d.getCantidadSolicitada());
                    psD.setBigDecimal(4, d.getPrecioUnitario());
                    psD.addBatch();
                }
                psD.executeBatch();

                // 3. Calcular total en BD
                PreparedStatement psTot = con.prepareStatement(
                    "SELECT calcular_total_compra(?)");
                psTot.setLong(1, idCompra);
                psTot.execute();

                con.commit();
                return idCompra;

            } catch (Exception e) {
                con.rollback();
                throw new SQLException("Error al crear compra: " + e.getMessage(), e);
            }
        }
    }

    public CompraAnticipada buscarCompraPorId(Long idCompra) throws SQLException {
        String sql = """
            SELECT id_compra, id_admin, proveedor, fecha_emision,
                   fecha_entrega_esperada, estado, total_estimado, notas
            FROM compra_anticipada WHERE id_compra = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idCompra);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                CompraAnticipada c = mapearCompra(rs);
                c.setDetalles(listarDetallesCompra(idCompra));
                return c;
            }
            return null;
        }
    }

    public List<CompraAnticipada> listarCompras() throws SQLException {
        String sql = """
            SELECT id_compra, id_admin, proveedor, fecha_emision,
                   fecha_entrega_esperada, estado, total_estimado, notas
            FROM compra_anticipada
            ORDER BY fecha_entrega_esperada DESC
            """;

        List<CompraAnticipada> lista = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapearCompra(rs));
        }
        return lista;
    }

    public boolean cambiarEstadoCompra(Long idCompra,
                                        EstCompraEnum nuevoEstado)
            throws SQLException {
        String sql = """
            UPDATE compra_anticipada
            SET estado = CAST(? AS est_compra_enum)
            WHERE id_compra = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado.name());
            ps.setLong(2, idCompra);
            return ps.executeUpdate() > 0;
        }
    }

    /*
      Recepciona la compra: genera movimientos de ENTRADA en inventario.
      Llama a la función recepcionar_compra() de PostgreSQL.
     */
    public void recepcionarCompra(Long idCompra, Long idUsuario)
            throws SQLException {
        String sql = "SELECT recepcionar_compra(?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idCompra);
            ps.setLong(2, idUsuario);
            ps.execute();
        }
    }

    public boolean registrarCantidadRecibida(Long idDetalleCompra,
                                              BigDecimal cantidadRecibida)
            throws SQLException {
        String sql = """
            UPDATE detalle_compra
            SET cantidad_recibida = ?
            WHERE id_detalle_compra = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, cantidadRecibida);
            ps.setLong(2, idDetalleCompra);
            return ps.executeUpdate() > 0;
        }
    }

    private List<DetalleCompra> listarDetallesCompra(Long idCompra)
            throws SQLException {
        String sql = """
            SELECT dc.id_detalle_compra, dc.id_compra, dc.id_ingrediente,
                   dc.cantidad_solicitada, dc.cantidad_recibida,
                   dc.precio_unitario, dc.subtotal,
                   i.nombre AS nombre_ingrediente
            FROM detalle_compra dc
            JOIN ingrediente i ON dc.id_ingrediente = i.id_ingrediente
            WHERE dc.id_compra = ?
            """;

        List<DetalleCompra> lista = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idCompra);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                DetalleCompra d = new DetalleCompra();
                d.setIdDetalleCompra(rs.getLong("id_detalle_compra"));
                d.setIdCompra(rs.getLong("id_compra"));
                d.setIdIngrediente(rs.getLong("id_ingrediente"));
                d.setCantidadSolicitada(rs.getBigDecimal("cantidad_solicitada"));
                d.setCantidadRecibida(rs.getBigDecimal("cantidad_recibida"));
                d.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                d.setSubtotal(rs.getBigDecimal("subtotal"));
                // Mini ingrediente con solo el nombre
                Ingrediente ing = new Ingrediente();
                ing.setNombre(rs.getString("nombre_ingrediente"));
                d.setIngrediente(ing);
                lista.add(d);
            }
        }
        return lista;
    }

    //  ALERTAS
    public List<AlertaInventario> listarAlertasActivas() throws SQLException {
        String sql = """
            SELECT a.id_alerta, a.id_ingrediente, a.id_admin,
                   a.tipo, a.mensaje, a.nivel, a.fecha_generada, a.atendida,
                   i.nombre AS nombre_ingrediente,
                   i.stock_actual, i.stock_minimo, i.unidad_medida
            FROM alerta_inventario a
            JOIN ingrediente i ON a.id_ingrediente = i.id_ingrediente
            WHERE a.atendida = FALSE
            ORDER BY
                CASE a.nivel WHEN 'CRITICO' THEN 1 WHEN 'WARNING' THEN 2 ELSE 3 END,
                a.fecha_generada DESC
            """;

        List<AlertaInventario> lista = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                AlertaInventario a = mapearAlerta(rs);
                Ingrediente ing = new Ingrediente();
                ing.setNombre(rs.getString("nombre_ingrediente"));
                ing.setStockActual(rs.getBigDecimal("stock_actual"));
                ing.setStockMinimo(rs.getBigDecimal("stock_minimo"));
                ing.setUnidadMedida(rs.getString("unidad_medida"));
                a.setIngrediente(ing);
                lista.add(a);
            }
        }
        return lista;
    }

    public boolean atenderAlerta(Long idAlerta) throws SQLException {
        String sql = """
            UPDATE alerta_inventario
            SET atendida = TRUE
            WHERE id_alerta = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idAlerta);
            return ps.executeUpdate() > 0;
        }
    }

    //  HELPERS

    private Ingrediente mapearIngrediente(ResultSet rs) throws SQLException {
        Ingrediente i = new Ingrediente();
        i.setIdIngrediente(rs.getLong("id_ingrediente"));
        i.setNombre(rs.getString("nombre"));
        i.setDescripcion(rs.getString("descripcion"));
        i.setUnidadMedida(rs.getString("unidad_medida"));
        i.setStockActual(rs.getBigDecimal("stock_actual"));
        i.setStockMinimo(rs.getBigDecimal("stock_minimo"));
        i.setStockMaximo(rs.getBigDecimal("stock_maximo"));
        i.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
        i.setProveedor(rs.getString("proveedor"));
        i.setCategoria(rs.getString("categoria"));
        i.setActivo(rs.getBoolean("activo"));
        return i;
    }

    private MovimientoInventario mapearMovimiento(ResultSet rs) throws SQLException {
        MovimientoInventario m = new MovimientoInventario();
        m.setIdMov(rs.getLong("id_mov"));
        m.setIdIngrediente(rs.getLong("id_ingrediente"));
        m.setIdUsuario(rs.getLong("id_usuario"));
        m.setTipo(TipoMovInvEnum.valueOf(rs.getString("tipo")));
        m.setCantidad(rs.getBigDecimal("cantidad"));
        m.setStockResultante(rs.getBigDecimal("stock_resultante"));
        m.setMotivo(rs.getString("motivo"));
        m.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        return m;
    }

    private CompraAnticipada mapearCompra(ResultSet rs) throws SQLException {
        CompraAnticipada c = new CompraAnticipada();
        c.setIdCompra(rs.getLong("id_compra"));
        c.setIdAdmin(rs.getLong("id_admin"));
        c.setProveedor(rs.getString("proveedor"));
        c.setFechaEmision(rs.getTimestamp("fecha_emision").toLocalDateTime());
        c.setFechaEntregaEsperada(rs.getDate("fecha_entrega_esperada").toLocalDate());
        c.setEstado(EstCompraEnum.valueOf(rs.getString("estado")));
        c.setTotalEstimado(rs.getBigDecimal("total_estimado"));
        c.setNotas(rs.getString("notas"));
        return c;
    }

    private AlertaInventario mapearAlerta(ResultSet rs) throws SQLException {
        AlertaInventario a = new AlertaInventario();
        a.setIdAlerta(rs.getLong("id_alerta"));
        a.setIdIngrediente(rs.getLong("id_ingrediente"));
        a.setIdAdmin(rs.getObject("id_admin") != null
            ? rs.getLong("id_admin") : null);
        a.setTipo(TipoAlertaEnum.valueOf(rs.getString("tipo")));
        a.setMensaje(rs.getString("mensaje"));
        a.setNivel(NivelAlertaEnum.valueOf(rs.getString("nivel")));
        a.setFechaGenerada(rs.getTimestamp("fecha_generada").toLocalDateTime());
        a.setAtendida(rs.getBoolean("atendida"));
        return a;
    }
}
