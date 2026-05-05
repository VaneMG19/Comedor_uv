package mx.uv.comedor.dao;

import mx.uv.comedor.model.CanalNotifEnum;
import mx.uv.comedor.model.Notificacion;
import mx.uv.comedor.model.TipoNotifEnum;
import mx.uv.comedor.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla 'notificacion'.
 * Las notificaciones se crean automáticamente desde triggers de BD.
 * Java solo las consulta y las marca como leídas.
 */
public class NotificacionDAO {

    /**
     * Retorna las notificaciones NO leídas de un usuario.
     * Se llama desde el servlet de polling cada 10-15 segundos.
     */
    public List<Notificacion> listarNoLeidas(Long idUsuario) throws SQLException {
        String sql = """
            SELECT id_notificacion, id_usuario, titulo, mensaje,
                   tipo, leida, fecha_envio, fecha_lectura,
                   canal, id_referencia, modulo_referencia
            FROM notificacion
            WHERE id_usuario = ? AND leida = FALSE
            ORDER BY fecha_envio DESC
            """;

        List<Notificacion> lista = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    /**
     * Retorna el número de notificaciones no leídas.
     * Para mostrar el badge en el ícono de la campana.
     */
    public int contarNoLeidas(Long idUsuario) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM notificacion
            WHERE id_usuario = ? AND leida = FALSE
            """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public List<Notificacion> listarTodas(Long idUsuario) throws SQLException {
        String sql = """
            SELECT id_notificacion, id_usuario, titulo, mensaje,
                   tipo, leida, fecha_envio, fecha_lectura,
                   canal, id_referencia, modulo_referencia
            FROM notificacion
            WHERE id_usuario = ?
            ORDER BY fecha_envio DESC
            LIMIT 50
            """;

        List<Notificacion> lista = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    /**
     * Marca una notificación específica como leída.
     */
    public boolean marcarLeida(Long idNotificacion) throws SQLException {
        String sql = """
            UPDATE notificacion
            SET leida = TRUE, fecha_lectura = NOW()
            WHERE id_notificacion = ?
            """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idNotificacion);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Marca TODAS las notificaciones del usuario como leídas.
     */
    public int marcarTodasLeidas(Long idUsuario) throws SQLException {
        String sql = """
            UPDATE notificacion
            SET leida = TRUE, fecha_lectura = NOW()
            WHERE id_usuario = ? AND leida = FALSE
            """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            return ps.executeUpdate();
        }
    }

    // ── HELPER ────────────────────────────────────────────────────

    private Notificacion mapear(ResultSet rs) throws SQLException {
        Notificacion n = new Notificacion();
        n.setIdNotificacion(rs.getLong("id_notificacion"));
        n.setIdUsuario(rs.getLong("id_usuario"));
        n.setTitulo(rs.getString("titulo"));
        n.setMensaje(rs.getString("mensaje"));
        n.setTipo(TipoNotifEnum.valueOf(rs.getString("tipo")));
        n.setLeida(rs.getBoolean("leida"));
        n.setFechaEnvio(rs.getTimestamp("fecha_envio").toLocalDateTime());
        Timestamp fl = rs.getTimestamp("fecha_lectura");
        if (fl != null) n.setFechaLectura(fl.toLocalDateTime());
        n.setCanal(CanalNotifEnum.valueOf(rs.getString("canal")));
        n.setIdReferencia(rs.getObject("id_referencia") != null
            ? rs.getLong("id_referencia") : null);
        n.setModuloReferencia(rs.getString("modulo_referencia"));
        return n;
    }
}
