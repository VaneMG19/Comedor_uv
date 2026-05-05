package mx.uv.comedor.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.uv.comedor.dao.NotificacionDAO;
import mx.uv.comedor.model.Notificacion;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Servlet de notificaciones en tiempo real via polling.
 *
 * El frontend llama a GET /notificaciones/nuevas cada 10 segundos
 * y recibe JSON con las notificaciones no leídas.
 *
 * GET  /notificaciones         → página HTML con todas las notificaciones
 * GET  /notificaciones/nuevas  → JSON con no leídas (para el badge)
 * POST /notificaciones/leer    → marca una notificación como leída
 * POST /notificaciones/leerTodas → marca todas como leídas
 */
@WebServlet(urlPatterns = {
    "/notificaciones",
    "/notificaciones/nuevas",
    "/notificaciones/leer",
    "/notificaciones/leerTodas"
})
public class NotificacionServlet extends HttpServlet {

    private final NotificacionDAO notifDAO = new NotificacionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!estaAutenticado(req, resp)) return;

        HttpSession session = req.getSession(false);
        Usuario usuario     = (Usuario) session.getAttribute("usuario");

        try {
            if ("/notificaciones/nuevas".equals(req.getServletPath())) {
                // Respuesta JSON para el polling del frontend
                responderJSON(req, resp, usuario);
            } else {
                // Página HTML con historial completo
                var todas = notifDAO.listarTodas(usuario.getIdUsuario());
                req.setAttribute("notificaciones", todas);
                req.getRequestDispatcher(
                    "/WEB-INF/vistas/notificaciones.jsp")
                   .forward(req, resp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(500, "Error al cargar notificaciones");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!estaAutenticado(req, resp)) return;

        HttpSession session = req.getSession(false);
        Usuario usuario     = (Usuario) session.getAttribute("usuario");

        try {
            if ("/notificaciones/leer".equals(req.getServletPath())) {
                Long id = Long.parseLong(req.getParameter("id"));
                notifDAO.marcarLeida(id);
            } else {
                notifDAO.marcarTodasLeidas(usuario.getIdUsuario());
            }
            // Respuesta vacía 200 OK para peticiones AJAX
            resp.setStatus(200);

        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(500);
        }
    }

    // ── Respuesta JSON para polling ───────────────────────────────

    /**
     * Retorna JSON con la forma:
     * { "total": 3, "notificaciones": [ { "id":1, "titulo":"...", ... } ] }
     *
     * El frontend usa este endpoint cada ~10 segundos para actualizar
     * el badge de la campana sin recargar la página.
     */
    private void responderJSON(HttpServletRequest req,
                                HttpServletResponse resp,
                                Usuario usuario)
            throws SQLException, IOException {

        var noLeidas = notifDAO.listarNoLeidas(usuario.getIdUsuario());

        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-cache");

        // Construir JSON manualmente para no depender de librerías extra
        StringBuilder json = new StringBuilder();
        json.append("{\"total\":").append(noLeidas.size());
        json.append(",\"notificaciones\":[");

        for (int i = 0; i < noLeidas.size(); i++) {
            Notificacion n = noLeidas.get(i);
            json.append("{");
            json.append("\"id\":").append(n.getIdNotificacion()).append(",");
            json.append("\"titulo\":\"").append(escapar(n.getTitulo())).append("\",");
            json.append("\"mensaje\":\"").append(escapar(n.getMensaje())).append("\",");
            json.append("\"tipo\":\"").append(n.getTipo()).append("\",");
            json.append("\"icono\":\"").append(n.getIcono()).append("\",");
            json.append("\"idReferencia\":").append(
                n.getIdReferencia() != null ? n.getIdReferencia() : "null").append(",");
            json.append("\"modulo\":\"").append(
                n.getModuloReferencia() != null ? n.getModuloReferencia() : "").append("\"");
            json.append("}");
            if (i < noLeidas.size() - 1) json.append(",");
        }

        json.append("]}");
        resp.getWriter().write(json.toString());
    }

    private String escapar(String texto) {
        if (texto == null) return "";
        return texto.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
    }

    private boolean estaAutenticado(HttpServletRequest req,
                                     HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.sendError(401, "No autenticado");
            return false;
        }
        return true;
    }
}
