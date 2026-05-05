package mx.uv.comedor.dao;

import mx.uv.comedor.model.Administrador;
import mx.uv.comedor.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdministradorDAO {

    public Long insertar(Administrador a) throws SQLException {
        String sql = "INSERT INTO administrador (id_usuario, nivel_acceso, departamento) " +
                "VALUES (?, ?, ?) RETURNING id_admin";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, a.getIdUsuario());
            ps.setInt(2, a.getNivelAcceso());
            ps.setString(3, a.getDepartamento());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong("id_admin");
            throw new SQLException("No se obtuvo ID al insertar administrador");
        }
    }

    public Administrador buscarPorIdUsuario(Long idUsuario) throws SQLException {
        String sql = "SELECT id_admin, id_usuario, nivel_acceso, departamento " +
                "FROM administrador WHERE id_usuario = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Administrador a = new Administrador();
                a.setIdAdmin(rs.getLong("id_admin"));
                a.setIdUsuario(rs.getLong("id_usuario"));
                a.setNivelAcceso(rs.getInt("nivel_acceso"));
                a.setDepartamento(rs.getString("departamento"));
                return a;
            }
            return null;
        }
    }
}