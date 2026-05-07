package mx.uv.comedor.dao;

import mx.uv.comedor.model.Calificacion;
import mx.uv.comedor.model.EstadisticaPlatillo;
import mx.uv.comedor.model.RespuestaCalificacion;
import mx.uv.comedor.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/*
  DAO para calificacion, respuesta_calificacion y estadistica_platillo.
 */
public class CalificacionDAO {

    // Calificacion

    /*
      Inserta una calificación.
      El trigger recalcula estadistica_platillo automáticamente.
      Lanza excepción si el pedido no está ENTREGADO (constraint en BD).
     */
    public Long insertar(Calificacion c) throws SQLException {
        String sql = """
            INSERT INTO calificacion
                (id_usuario, id_platillo, id_pedido, puntuacion, comentario)
            VALUES (?, ?, ?, ?, ?)
            RETURNING id_calificacion
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, c.getIdUsuario());
            ps.setLong(2, c.getIdPlatillo());
            ps.setLong(3, c.getIdPedido());
            ps.setInt(4, c.getPuntuacion());
            ps.setString(5, c.getComentario());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong("id_calificacion");
            throw new SQLException("No se obtuvo ID al insertar calificación");
        }
    }

    /*
      Verifica si un pedido ya fue calificado.
     */
    public boolean pedidoCalificado(Long idPedido) throws SQLException {
        String sql = "SELECT COUNT(*) FROM calificacion WHERE id_pedido = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idPedido);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public Calificacion buscarPorId(Long id) throws SQLException {
        String sql = """
            SELECT c.id_calificacion, c.id_usuario, c.id_platillo, c.id_pedido,
                   c.puntuacion, c.comentario, c.fecha, c.aprobada,
                   u.nombre || ' ' || u.apellidos AS nombre_usuario,
                   p.nombre AS nombre_platillo
            FROM calificacion c
            JOIN usuario  u ON c.id_usuario  = u.id_usuario
            JOIN platillo p ON c.id_platillo = p.id_platillo
            WHERE c.id_calificacion = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Calificacion c = mapear(rs);
                c.setRespuesta(buscarRespuesta(c.getIdCalificacion()));
                return c;
            }
            return null;
        }
    }

    public List<Calificacion> listarPorPlatillo(Long idPlatillo)
            throws SQLException {
        String sql = """
            SELECT c.id_calificacion, c.id_usuario, c.id_platillo, c.id_pedido,
                   c.puntuacion, c.comentario, c.fecha, c.aprobada,
                   u.nombre || ' ' || u.apellidos AS nombre_usuario,
                   p.nombre AS nombre_platillo
            FROM calificacion c
            JOIN usuario  u ON c.id_usuario  = u.id_usuario
            JOIN platillo p ON c.id_platillo = p.id_platillo
            WHERE c.id_platillo = ? AND c.aprobada = TRUE
            ORDER BY c.fecha DESC
            """;

        List<Calificacion> lista = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idPlatillo);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Calificacion> listarTodas() throws SQLException {
        String sql = """
            SELECT c.id_calificacion, c.id_usuario, c.id_platillo, c.id_pedido,
                   c.puntuacion, c.comentario, c.fecha, c.aprobada,
                   u.nombre || ' ' || u.apellidos AS nombre_usuario,
                   p.nombre AS nombre_platillo
            FROM calificacion c
            JOIN usuario  u ON c.id_usuario  = u.id_usuario
            JOIN platillo p ON c.id_platillo = p.id_platillo
            ORDER BY c.fecha DESC
            """;

        List<Calificacion> lista = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Calificacion c = mapear(rs);
                c.setRespuesta(buscarRespuesta(c.getIdCalificacion()));
                lista.add(c);
            }
        }
        return lista;
    }

    public boolean cambiarAprobacion(Long idCalificacion, boolean aprobada)
            throws SQLException {
        String sql = """
            UPDATE calificacion SET aprobada = ?
            WHERE id_calificacion = ?
            """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, aprobada);
            ps.setLong(2, idCalificacion);
            return ps.executeUpdate() > 0;
        }
    }

    // RespuestaCalificacion

    public Long insertarRespuesta(RespuestaCalificacion r) throws SQLException {
        String sql = """
            INSERT INTO respuesta_calificacion
                (id_calificacion, id_admin, respuesta)
            VALUES (?, ?, ?)
            RETURNING id_respuesta
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, r.getIdCalificacion());
            ps.setLong(2, r.getIdAdmin());
            ps.setString(3, r.getRespuesta());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong("id_respuesta");
            throw new SQLException("No se obtuvo ID al insertar respuesta");
        }
    }

    private RespuestaCalificacion buscarRespuesta(Long idCalificacion)
            throws SQLException {
        String sql = """
            SELECT r.id_respuesta, r.id_calificacion, r.id_admin,
                   r.respuesta, r.fecha,
                   ua.nombre || ' ' || ua.apellidos AS nombre_admin
            FROM respuesta_calificacion r
            JOIN administrador a ON r.id_admin = a.id_admin
            JOIN usuario ua ON a.id_usuario = ua.id_usuario
            WHERE r.id_calificacion = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idCalificacion);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                RespuestaCalificacion r = new RespuestaCalificacion();
                r.setIdRespuesta(rs.getLong("id_respuesta"));
                r.setIdCalificacion(rs.getLong("id_calificacion"));
                r.setIdAdmin(rs.getLong("id_admin"));
                r.setRespuesta(rs.getString("respuesta"));
                r.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                r.setNombreAdmin(rs.getString("nombre_admin"));
                return r;
            }
            return null;
        }
    }

    // EstadisticaPlatillo

    public EstadisticaPlatillo obtenerEstadistica(Long idPlatillo)
            throws SQLException {
        String sql = """
            SELECT id_est, id_platillo, promedio_calif, total_calif,
                   total_pedidos, ingreso_total, ultima_actualizacion
            FROM estadistica_platillo
            WHERE id_platillo = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idPlatillo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapearEstadistica(rs);
            return null;
        }
    }

    public List<EstadisticaPlatillo> listarTopPlatillos(int limite)
            throws SQLException {
        String sql = """
            SELECT ep.id_est, ep.id_platillo, ep.promedio_calif,
                   ep.total_calif, ep.total_pedidos,
                   ep.ingreso_total, ep.ultima_actualizacion
            FROM estadistica_platillo ep
            WHERE ep.total_calif > 0
            ORDER BY ep.promedio_calif DESC, ep.total_calif DESC
            LIMIT ?
            """;

        List<EstadisticaPlatillo> lista = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limite);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapearEstadistica(rs));
        }
        return lista;
    }

    //

    private Calificacion mapear(ResultSet rs) throws SQLException {
        Calificacion c = new Calificacion();
        c.setIdCalificacion(rs.getLong("id_calificacion"));
        c.setIdUsuario(rs.getLong("id_usuario"));
        c.setIdPlatillo(rs.getLong("id_platillo"));
        c.setIdPedido(rs.getLong("id_pedido"));
        c.setPuntuacion(rs.getInt("puntuacion"));
        c.setComentario(rs.getString("comentario"));
        c.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        c.setAprobada(rs.getBoolean("aprobada"));
        c.setNombreUsuario(rs.getString("nombre_usuario"));
        c.setNombrePlatillo(rs.getString("nombre_platillo"));
        return c;
    }

    private EstadisticaPlatillo mapearEstadistica(ResultSet rs)
            throws SQLException {
        EstadisticaPlatillo e = new EstadisticaPlatillo();
        e.setIdEst(rs.getLong("id_est"));
        e.setIdPlatillo(rs.getLong("id_platillo"));
        e.setPromedioCalif(rs.getBigDecimal("promedio_calif"));
        e.setTotalCalif(rs.getInt("total_calif"));
        e.setTotalPedidos(rs.getInt("total_pedidos"));
        e.setIngresoTotal(rs.getBigDecimal("ingreso_total"));
        e.setUltimaActualizacion(
            rs.getTimestamp("ultima_actualizacion").toLocalDateTime());
        return e;
    }
}
