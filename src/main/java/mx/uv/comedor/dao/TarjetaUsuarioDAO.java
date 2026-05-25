package mx.uv.comedor.dao;

import mx.uv.comedor.model.TarjetaUsuario;
import mx.uv.comedor.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/*
  DAO para tarjeta_usuario.
 */
public class TarjetaUsuarioDAO {

    public Long insertar(TarjetaUsuario t) throws SQLException {
        String sql = "INSERT INTO tarjeta_usuario " +
            "(id_usuario, alias, marca, ultimos_4, nombre_titular, " +
            " mes_vencimiento, anio_vencimiento, es_predeterminada) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_tarjeta";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, t.getIdUsuario());
            ps.setString(2, t.getAlias());
            ps.setString(3, t.getMarca());
            ps.setString(4, t.getUltimos4());
            ps.setString(5, t.getNombreTitular());
            ps.setInt(6, t.getMesVencimiento());
            ps.setInt(7, t.getAnioVencimiento());
            ps.setBoolean(8, t.isEsPredeterminada());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong("id_tarjeta");
            throw new SQLException("No se obtuvo ID al insertar tarjeta");
        }
    }

    public List<TarjetaUsuario> listarPorUsuario(Long idUsuario) throws SQLException {
        String sql = "SELECT * FROM tarjeta_usuario " +
                     "WHERE id_usuario = ? AND activa = TRUE " +
                     "ORDER BY es_predeterminada DESC, fecha_registro DESC";
        List<TarjetaUsuario> lista = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public TarjetaUsuario buscarPorId(Long idTarjeta) throws SQLException {
        String sql = "SELECT * FROM tarjeta_usuario WHERE id_tarjeta = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idTarjeta);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
            return null;
        }
    }

    public boolean eliminar(Long idTarjeta, Long idUsuario) throws SQLException {
        String sql = "UPDATE tarjeta_usuario SET activa = FALSE " +
                     "WHERE id_tarjeta = ? AND id_usuario = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idTarjeta);
            ps.setLong(2, idUsuario);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean marcarPredeterminada(Long idTarjeta, Long idUsuario) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // Primero quitar la marca de todas
                String sql1 = "UPDATE tarjeta_usuario SET es_predeterminada = FALSE " +
                              "WHERE id_usuario = ?";
                try (PreparedStatement ps = con.prepareStatement(sql1)) {
                    ps.setLong(1, idUsuario);
                    ps.executeUpdate();
                }
                // Después marcar la elegida
                String sql2 = "UPDATE tarjeta_usuario SET es_predeterminada = TRUE " +
                              "WHERE id_tarjeta = ? AND id_usuario = ?";
                try (PreparedStatement ps = con.prepareStatement(sql2)) {
                    ps.setLong(1, idTarjeta);
                    ps.setLong(2, idUsuario);
                    int filas = ps.executeUpdate();
                    con.commit();
                    return filas > 0;
                }
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    private TarjetaUsuario mapear(ResultSet rs) throws SQLException {
        TarjetaUsuario t = new TarjetaUsuario();
        t.setIdTarjeta(rs.getLong("id_tarjeta"));
        t.setIdUsuario(rs.getLong("id_usuario"));
        t.setAlias(rs.getString("alias"));
        t.setMarca(rs.getString("marca"));
        t.setUltimos4(rs.getString("ultimos_4"));
        t.setNombreTitular(rs.getString("nombre_titular"));
        t.setMesVencimiento(rs.getInt("mes_vencimiento"));
        t.setAnioVencimiento(rs.getInt("anio_vencimiento"));
        t.setEsPredeterminada(rs.getBoolean("es_predeterminada"));
        if (rs.getTimestamp("fecha_registro") != null)
            t.setFechaRegistro(rs.getTimestamp("fecha_registro").toLocalDateTime());
        t.setActiva(rs.getBoolean("activa"));
        return t;
    }
}
