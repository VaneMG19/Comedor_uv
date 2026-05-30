package mx.uv.comedor.dao;

import mx.uv.comedor.model.*;
import mx.uv.comedor.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/*
  Gestiona las recetas (platillo_ingrediente) y el descuento
  automático de inventario cuando se vende un platillo.

 */
public class RecetaDAO {

    public List<PlatilloIngrediente> listarIngredientesDelPlatillo(Long idPlatillo)
            throws SQLException {
        String sql =
                "SELECT pi.id_rel, pi.id_platillo, pi.id_ingrediente, pi.cantidad, " +
                        "       i.nombre, i.unidad_medida, i.stock_actual " +
                        "FROM platillo_ingrediente pi " +
                        "JOIN ingrediente i ON pi.id_ingrediente = i.id_ingrediente " +
                        "WHERE pi.id_platillo = ? " +
                        "ORDER BY i.nombre";

        List<PlatilloIngrediente> lista = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idPlatillo);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public void agregarIngrediente(Long idPlatillo, Long idIngrediente, BigDecimal cantidad)
            throws SQLException {
        // Obtener la unidad del ingrediente para guardarla en la columna 'unidad'
        String unidad = obtenerUnidadIngrediente(idIngrediente);

        String sql = "INSERT INTO platillo_ingrediente " +
                "(id_platillo, id_ingrediente, cantidad, unidad) " +
                "VALUES (?, ?, ?, ?) " +
                "ON CONFLICT (id_platillo, id_ingrediente) DO UPDATE " +
                "SET cantidad = EXCLUDED.cantidad";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idPlatillo);
            ps.setLong(2, idIngrediente);
            ps.setBigDecimal(3, cantidad);
            ps.setString(4, unidad != null ? unidad : "");
            ps.executeUpdate();
        }
    }

    public void quitarIngrediente(Long idRel) throws SQLException {
        String sql = "DELETE FROM platillo_ingrediente WHERE id_rel = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idRel);
            ps.executeUpdate();
        }
    }

    private String obtenerUnidadIngrediente(Long idIngrediente) throws SQLException {
        String sql = "SELECT unidad_medida FROM ingrediente WHERE id_ingrediente = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idIngrediente);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("unidad_medida");
        }
        return null;
    }

    public List<String> verificarStockSuficiente(Map<Long, Integer> platillosACantidad)
            throws SQLException {
        Map<Long, BigDecimal> necesidades = new HashMap<>();
        Map<Long, String>    nombres      = new HashMap<>();
        Map<Long, String>    unidades     = new HashMap<>();
        Map<Long, BigDecimal> stockActual = new HashMap<>();

        for (Map.Entry<Long, Integer> e : platillosACantidad.entrySet()) {
            Long idPlat = e.getKey();
            int  cant   = e.getValue();

            List<PlatilloIngrediente> receta = listarIngredientesDelPlatillo(idPlat);
            for (PlatilloIngrediente pi : receta) {
                BigDecimal req = pi.getCantidad().multiply(BigDecimal.valueOf(cant));
                Long idIng = pi.getIdIngrediente();
                necesidades.merge(idIng, req, BigDecimal::add);
                nombres.put(idIng, pi.getNombreIngrediente());
                unidades.put(idIng, pi.getUnidadIngrediente());
                stockActual.put(idIng, pi.getStockActualIngrediente());
            }
        }

        List<String> faltantes = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> e : necesidades.entrySet()) {
            Long idIng = e.getKey();
            BigDecimal requerido  = e.getValue();
            BigDecimal disponible = stockActual.get(idIng);
            if (disponible == null) disponible = BigDecimal.ZERO;

            if (disponible.compareTo(requerido) < 0) {
                faltantes.add(String.format("%s: requiere %s %s, hay %s %s",
                        nombres.get(idIng),
                        requerido.toPlainString(), unidades.get(idIng),
                        disponible.toPlainString(), unidades.get(idIng)));
            }
        }
        return faltantes;
    }

    public void descontarStock(Map<Long, Integer> platillosACantidad,
                               Long idUsuario, String motivo) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                for (Map.Entry<Long, Integer> e : platillosACantidad.entrySet()) {
                    Long idPlat = e.getKey();
                    int  cant   = e.getValue();

                    List<PlatilloIngrediente> receta = listarIngredientesDelPlatillo(idPlat);
                    for (PlatilloIngrediente pi : receta) {
                        BigDecimal aDescontar = pi.getCantidad().multiply(BigDecimal.valueOf(cant));
                        descontarUno(con, pi.getIdIngrediente(), aDescontar, idUsuario, motivo);
                    }
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    private void descontarUno(Connection con, Long idIngrediente, BigDecimal cantidad,
                              Long idUsuario, String motivo) throws SQLException {

        String sqlUpdate =
                "UPDATE ingrediente SET stock_actual = stock_actual - ? " +
                        "WHERE id_ingrediente = ? RETURNING stock_actual";
        BigDecimal stockResultante;
        try (PreparedStatement ps = con.prepareStatement(sqlUpdate)) {
            ps.setBigDecimal(1, cantidad);
            ps.setLong(2, idIngrediente);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) throw new SQLException("Ingrediente no encontrado: " + idIngrediente);
            stockResultante = rs.getBigDecimal("stock_actual");
        }

        String sqlMov =
                "INSERT INTO movimiento_inventario " +
                        "(id_ingrediente, id_usuario, tipo, cantidad, stock_resultante, motivo) " +
                        "VALUES (?, ?, CAST('SALIDA' AS tipo_mov_inv_enum), ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sqlMov)) {
            ps.setLong(1, idIngrediente);
            if (idUsuario != null) ps.setLong(2, idUsuario);
            else ps.setNull(2, Types.BIGINT);
            ps.setBigDecimal(3, cantidad);
            ps.setBigDecimal(4, stockResultante);
            ps.setString(5, motivo);
            ps.executeUpdate();
        }
    }

    private PlatilloIngrediente mapear(ResultSet rs) throws SQLException {
        PlatilloIngrediente pi = new PlatilloIngrediente();
        pi.setIdReceta(rs.getLong("id_rel"));
        pi.setIdPlatillo(rs.getLong("id_platillo"));
        pi.setIdIngrediente(rs.getLong("id_ingrediente"));
        pi.setCantidad(rs.getBigDecimal("cantidad"));
        pi.setNombreIngrediente(rs.getString("nombre"));
        pi.setUnidadIngrediente(rs.getString("unidad_medida"));
        pi.setStockActualIngrediente(rs.getBigDecimal("stock_actual"));
        return pi;
    }
}