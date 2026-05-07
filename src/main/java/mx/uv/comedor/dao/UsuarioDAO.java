package mx.uv.comedor.dao;

import mx.uv.comedor.model.RolEnum;
import mx.uv.comedor.model.Usuario;
import mx.uv.comedor.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/*
  DAO para la tabla 'usuario'.
 */
public class UsuarioDAO {


    /*
      Inserta un nuevo usuario en la BD y retorna el ID generado.
      El password se hashea con la función hashear_password() de PostgreSQL.
     */
    public Long insertar(Usuario u, String passwordPlano) throws SQLException {
        String sql = """
            INSERT INTO usuario (nombre, apellidos, email, password_hash,
                                 telefono, foto_perfil, activo, rol)
            VALUES (?, ?, ?, hashear_password(?), ?, ?, ?, CAST(? AS rol_enum))
            RETURNING id_usuario
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, u.getNombre());
            ps.setString(2, u.getApellidos());
            ps.setString(3, u.getEmail());
            ps.setString(4, passwordPlano);
            ps.setString(5, u.getTelefono());
            ps.setString(6, u.getFotoPerfil());
            ps.setBoolean(7, u.isActivo());
            ps.setString(8, u.getRol().name());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong("id_usuario");
            }
            throw new SQLException("No se obtuvo ID al insertar usuario");
        }
    }



    /*
      Busca un usuario por su ID.
     */
    public Usuario buscarPorId(Long idUsuario) throws SQLException {
        String sql = """
            SELECT id_usuario, nombre, apellidos, email, password_hash,
                   telefono, foto_perfil, activo, fecha_registro, rol
            FROM usuario
            WHERE id_usuario = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idUsuario);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapear(rs);
            }
            return null;
        }
    }

    /*
      Busca un usuario por email — usado en login.
     */
    public Usuario buscarPorEmail(String email) throws SQLException {
        String sql = """
            SELECT id_usuario, nombre, apellidos, email, password_hash,
                   telefono, foto_perfil, activo, fecha_registro, rol
            FROM usuario
            WHERE email = ? AND activo = TRUE
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapear(rs);
            }
            return null;
        }
    }

    /*
      Retorna todos los usuarios activos.
     */
    public List<Usuario> listarTodos() throws SQLException {
        String sql = """
            SELECT id_usuario, nombre, apellidos, email, password_hash,
                   telefono, foto_perfil, activo, fecha_registro, rol
            FROM usuario
            WHERE activo = TRUE
            ORDER BY apellidos, nombre
            """;

        List<Usuario> lista = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /*
      Lista usuarios por rol.
     */
    public List<Usuario> listarPorRol(RolEnum rol) throws SQLException {
        String sql = """
            SELECT id_usuario, nombre, apellidos, email, password_hash,
                   telefono, foto_perfil, activo, fecha_registro, rol
            FROM usuario
            WHERE rol = CAST(? AS rol_enum) AND activo = TRUE
            ORDER BY apellidos, nombre
            """;

        List<Usuario> lista = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, rol.name());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }



    /*
      Verifica email y contraseña usando la función de PostgreSQL.
      Retorna el usuario si las credenciales son correctas, null si no.
     */
    public Usuario login(String email, String passwordPlano) throws SQLException {
        String sql = """
            SELECT id_usuario, nombre, apellidos, email, password_hash,
                   telefono, foto_perfil, activo, fecha_registro, rol
            FROM usuario
            WHERE email = ?
              AND activo = TRUE
              AND verificar_password(?, password_hash) = TRUE
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, passwordPlano);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapear(rs);
            }
            return null; // Credenciales incorrectas
        }
    }



    /*
      Actualiza datos básicos del perfil (sin cambiar password ni rol).
     */
    public boolean actualizarPerfil(Usuario u) throws SQLException {
        String sql = """
            UPDATE usuario
            SET nombre = ?, apellidos = ?, telefono = ?, foto_perfil = ?
            WHERE id_usuario = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, u.getNombre());
            ps.setString(2, u.getApellidos());
            ps.setString(3, u.getTelefono());
            ps.setString(4, u.getFotoPerfil());
            ps.setLong(5, u.getIdUsuario());

            return ps.executeUpdate() > 0;
        }
    }

    /*
      Cambia la contraseña del usuario.
     */
    public boolean cambiarPassword(Long idUsuario, String nuevaPassword) throws SQLException {
        String sql = """
            UPDATE usuario
            SET password_hash = hashear_password(?)
            WHERE id_usuario = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevaPassword);
            ps.setLong(2, idUsuario);

            return ps.executeUpdate() > 0;
        }
    }


    /*
      Desactiva un usuario — nunca se elimina físicamente.
     */
    public boolean desactivar(Long idUsuario) throws SQLException {
        String sql = "UPDATE usuario SET activo = FALSE WHERE id_usuario = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idUsuario);
            return ps.executeUpdate() > 0;
        }
    }



    /*
      Mapea un ResultSet a un objeto Usuario.
      Se reutiliza en todos los métodos de consulta.
     */
    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getLong("id_usuario"));
        u.setNombre(rs.getString("nombre"));
        u.setApellidos(rs.getString("apellidos"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setTelefono(rs.getString("telefono"));
        u.setFotoPerfil(rs.getString("foto_perfil"));
        u.setActivo(rs.getBoolean("activo"));
        u.setFechaRegistro(rs.getTimestamp("fecha_registro").toLocalDateTime());
        u.setRol(RolEnum.valueOf(rs.getString("rol")));
        return u;
    }
}
