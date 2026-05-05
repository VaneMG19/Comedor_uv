package mx.uv.comedor.dao;

import mx.uv.comedor.model.Estudiante;
import mx.uv.comedor.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla 'estudiante'.
 */
public class EstudianteDAO {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    // ── CREATE

    public Long insertar(Estudiante e) throws SQLException {
        String sql = """
            INSERT INTO estudiante (id_usuario, matricula, carrera, semestre)
            VALUES (?, ?, ?, ?)
            RETURNING id_estudiante
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, e.getIdUsuario());
            ps.setString(2, e.getMatricula());
            ps.setString(3, e.getCarrera());
            ps.setInt(4, e.getSemestre());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong("id_estudiante");
            throw new SQLException("No se obtuvo ID al insertar estudiante");
        }
    }

    // ── READ

    public Estudiante buscarPorId(Long idEstudiante) throws SQLException {
        String sql = """
            SELECT e.id_estudiante, e.id_usuario, e.matricula, e.carrera, e.semestre
            FROM estudiante e
            WHERE e.id_estudiante = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idEstudiante);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Estudiante est = mapear(rs);
                est.setUsuario(usuarioDAO.buscarPorId(est.getIdUsuario()));
                return est;
            }
            return null;
        }
    }

    public Estudiante buscarPorMatricula(String matricula) throws SQLException {
        String sql = """
            SELECT e.id_estudiante, e.id_usuario, e.matricula, e.carrera, e.semestre
            FROM estudiante e
            WHERE e.matricula = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, matricula);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Estudiante est = mapear(rs);
                est.setUsuario(usuarioDAO.buscarPorId(est.getIdUsuario()));
                return est;
            }
            return null;
        }
    }

    public Estudiante buscarPorIdUsuario(Long idUsuario) throws SQLException {
        String sql = """
            SELECT id_estudiante, id_usuario, matricula, carrera, semestre
            FROM estudiante WHERE id_usuario = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
            return null;
        }
    }

    public List<Estudiante> listarTodos() throws SQLException {
        String sql = """
            SELECT e.id_estudiante, e.id_usuario, e.matricula, e.carrera, e.semestre
            FROM estudiante e
            JOIN usuario u ON e.id_usuario = u.id_usuario
            WHERE u.activo = TRUE
            ORDER BY e.matricula
            """;

        List<Estudiante> lista = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    // ── UPDATE

    public boolean actualizar(Estudiante e) throws SQLException {
        String sql = """
            UPDATE estudiante
            SET carrera = ?, semestre = ?
            WHERE id_estudiante = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, e.getCarrera());
            ps.setInt(2, e.getSemestre());
            ps.setLong(3, e.getIdEstudiante());
            return ps.executeUpdate() > 0;
        }
    }

    // ── HELPER ────────────────────────────────────────────────────

    private Estudiante mapear(ResultSet rs) throws SQLException {
        Estudiante e = new Estudiante();
        e.setIdEstudiante(rs.getLong("id_estudiante"));
        e.setIdUsuario(rs.getLong("id_usuario"));
        e.setMatricula(rs.getString("matricula"));
        e.setCarrera(rs.getString("carrera"));
        e.setSemestre(rs.getInt("semestre"));
        return e;
    }
}
