package mx.uv.comedor.dao;

import mx.uv.comedor.model.Docente;
import mx.uv.comedor.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DocenteDAO {

    public Long insertar(Docente d) throws SQLException {
        String sql = "INSERT INTO docente " +
                "(id_usuario, num_empleado_docente, facultad, departamento, categoria) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING id_docente";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, d.getIdUsuario());
            ps.setString(2, d.getNumEmpleadoDocente());
            ps.setString(3, d.getFacultad());
            ps.setString(4, d.getDepartamento());
            ps.setString(5, d.getCategoria());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong("id_docente");
            throw new SQLException("No se obtuvo ID al insertar docente");
        }
    }

    public Docente buscarPorIdUsuario(Long idUsuario) throws SQLException {
        String sql = "SELECT id_docente, id_usuario, num_empleado_docente, " +
                "facultad, departamento, categoria " +
                "FROM docente WHERE id_usuario = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Docente d = new Docente();
                d.setIdDocente(rs.getLong("id_docente"));
                d.setIdUsuario(rs.getLong("id_usuario"));
                d.setNumEmpleadoDocente(rs.getString("num_empleado_docente"));
                d.setFacultad(rs.getString("facultad"));
                d.setDepartamento(rs.getString("departamento"));
                d.setCategoria(rs.getString("categoria"));
                return d;
            }
            return null;
        }
    }
}