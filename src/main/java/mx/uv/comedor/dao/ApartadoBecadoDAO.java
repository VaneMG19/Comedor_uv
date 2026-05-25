package mx.uv.comedor.dao;

import mx.uv.comedor.model.ApartadoBecado;
import mx.uv.comedor.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/*
  DAO para apartado_becado.
  Maneja los apartados de comidas que hace el becado para días futuros.
 */
public class ApartadoBecadoDAO {

    /*
      Aparta una comida para un día específico.
      Falla si ya existe un apartado para ese día y tipo (constraint UNIQUE).
     */
    public Long apartar(Long idBecado, LocalDate fecha, String tipoComida) throws SQLException {
        String sql = "INSERT INTO apartado_becado " +
            "(id_becado, fecha_consumo, tipo_comida, estado) " +
            "VALUES (?, ?, ?, 'APARTADO') RETURNING id_apartado";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idBecado);
            ps.setObject(2, fecha);
            ps.setString(3, tipoComida);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong("id_apartado");
            throw new SQLException("No se pudo apartar");
        }
    }

    /*
      Cancela un apartado (el becado decide ya no usarlo).
     */
    public boolean cancelar(Long idApartado, Long idBecado) throws SQLException {
        String sql = "UPDATE apartado_becado SET estado = 'CANCELADO' " +
                     "WHERE id_apartado = ? AND id_becado = ? AND estado = 'APARTADO'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idApartado);
            ps.setLong(2, idBecado);
            return ps.executeUpdate() > 0;
        }
    }

    /*
    Lista todos los apartados del becado para una semana específica.
     */
    public List<ApartadoBecado> listarPorSemana(Long idBecado, LocalDate inicio, LocalDate fin)
            throws SQLException {
        String sql = "SELECT * FROM apartado_becado " +
                     "WHERE id_becado = ? AND fecha_consumo BETWEEN ? AND ? " +
                     "ORDER BY fecha_consumo, tipo_comida";
        List<ApartadoBecado> lista = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idBecado);
            ps.setObject(2, inicio);
            ps.setObject(3, fin);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    /*
      Cuenta cuántos apartados activos tiene el becado esta semana.
     */
    public int contarApartadosSemana(Long idBecado, LocalDate inicio, LocalDate fin)
            throws SQLException {
        String sql = "SELECT COUNT(*) FROM apartado_becado " +
                     "WHERE id_becado = ? AND fecha_consumo BETWEEN ? AND ? " +
                     "AND estado IN ('APARTADO','CONSUMIDO')";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idBecado);
            ps.setObject(2, inicio);
            ps.setObject(3, fin);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
            return 0;
        }
    }

    private ApartadoBecado mapear(ResultSet rs) throws SQLException {
        ApartadoBecado a = new ApartadoBecado();
        a.setIdApartado(rs.getLong("id_apartado"));
        a.setIdBecado(rs.getLong("id_becado"));
        a.setFechaConsumo(rs.getDate("fecha_consumo").toLocalDate());
        a.setTipoComida(rs.getString("tipo_comida"));
        a.setEstado(rs.getString("estado"));
        if (rs.getTimestamp("fecha_creacion") != null)
            a.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
        if (rs.getTimestamp("fecha_consumo_real") != null)
            a.setFechaConsumoReal(rs.getTimestamp("fecha_consumo_real").toLocalDateTime());
        a.setNotas(rs.getString("notas"));
        return a;
    }
}
