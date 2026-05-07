package mx.uv.comedor.dao;

import mx.uv.comedor.model.AlumnoBecado;
import mx.uv.comedor.util.DBConnection;

import java.sql.*;

/*
  DAO para la tabla 'alumno_becado'.
 */
public class AlumnoBecadoDAO {

    private final EstudianteDAO estudianteDAO = new EstudianteDAO();



    public Long insertar(AlumnoBecado b) throws SQLException {
        String sql = """
            INSERT INTO alumno_becado
                (id_estudiante, tipo_beca, comidas_disponibles_semana,
                 vigencia_desde, vigencia_hasta)
            VALUES (?, ?, ?, ?, ?)
            RETURNING id_becado
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, b.getIdEstudiante());
            ps.setString(2, b.getTipoBeca());
            ps.setInt(3, b.getComidasDisponiblesSemana());
            ps.setDate(4, Date.valueOf(b.getVigenciaDesde()));
            ps.setDate(5, Date.valueOf(b.getVigenciaHasta()));

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong("id_becado");
            throw new SQLException("No se obtuvo ID al insertar alumno becado");
        }
    }

    // ── READ

    public AlumnoBecado buscarPorId(Long idBecado) throws SQLException {
        String sql = """
            SELECT id_becado, id_estudiante, tipo_beca,
                   comidas_disponibles_semana, comidas_usadas_semana,
                   vigencia_desde, vigencia_hasta, aplica_solo
            FROM alumno_becado WHERE id_becado = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idBecado);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                AlumnoBecado b = mapear(rs);
                b.setEstudiante(estudianteDAO.buscarPorId(b.getIdEstudiante()));
                return b;
            }
            return null;
        }
    }

    public AlumnoBecado buscarPorIdEstudiante(Long idEstudiante) throws SQLException {
        String sql = """
            SELECT id_becado, id_estudiante, tipo_beca,
                   comidas_disponibles_semana, comidas_usadas_semana,
                   vigencia_desde, vigencia_hasta, aplica_solo
            FROM alumno_becado
            WHERE id_estudiante = ?
              AND vigencia_hasta >= CURRENT_DATE
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idEstudiante);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
            return null;
        }
    }

    // ── UPDATE

    /*
     Incrementa las comidas usadas esta semana en 1.
      Se llama cada vez que el becado usa su beca.
     */
    public boolean registrarUsoComida(Long idBecado) throws SQLException {
        String sql = """
            UPDATE alumno_becado
            SET comidas_usadas_semana = comidas_usadas_semana + 1
            WHERE id_becado = ?
              AND comidas_usadas_semana < comidas_disponibles_semana
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idBecado);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new SQLException("El becado no tiene comidas disponibles esta semana");
            }
            return true;
        }
    }

    /*
      Verifica si el becado puede usar su beca hoy.
      La beca SOLO aplica para platillos tipo MENU.
     */
    public boolean puedeUsarBeca(Long idBecado) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM alumno_becado
            WHERE id_becado = ?
              AND vigencia_hasta >= CURRENT_DATE
              AND comidas_usadas_semana < comidas_disponibles_semana
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idBecado);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    /*
      Resetea el contador de comidas usadas (llamar cada lunes).
     */
    public void resetearComidassemana() throws SQLException {
        String sql = "SELECT resetear_comidas_semana()";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.execute();
        }
    }



    private AlumnoBecado mapear(ResultSet rs) throws SQLException {
        AlumnoBecado b = new AlumnoBecado();
        b.setIdBecado(rs.getLong("id_becado"));
        b.setIdEstudiante(rs.getLong("id_estudiante"));
        b.setTipoBeca(rs.getString("tipo_beca"));
        b.setComidasDisponiblesSemana(rs.getInt("comidas_disponibles_semana"));
        b.setComidasUsadasSemana(rs.getInt("comidas_usadas_semana"));
        b.setVigenciaDesde(rs.getDate("vigencia_desde").toLocalDate());
        b.setVigenciaHasta(rs.getDate("vigencia_hasta").toLocalDate());
        return b;
    }
}
