package mx.uv.comedor.servlet;

import mx.uv.comedor.dao.NotificacionDAO;
import mx.uv.comedor.model.Notificacion;
import mx.uv.comedor.model.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

@WebServlet(urlPatterns = {
        "/notificaciones/nuevas",
        "/notificaciones/leer",
        "/notificaciones"
})
public class NotificacionServlet extends HttpServlet {

    private final NotificacionDAO notifDAO = new NotificacionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.setStatus(401);
            return;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        String  path    = req.getServletPath();

        if ("/notificaciones".equals(path)) {
            resp.sendRedirect(req.getContextPath() + "/menu");
            return;
        }

        resp.setContentType("application/json;charset=UTF-8");
        try {
            List<Notificacion> notifs = notifDAO.listarNoLeidas(usuario.getIdUsuario());
            StringBuilder sb = new StringBuilder();
            sb.append("{\"total\":").append(notifs.size()).append(",");
            sb.append("\"notificaciones\":[");
            for (int i = 0; i < notifs.size(); i++) {
                Notificacion n = notifs.get(i);
                if (i > 0) sb.append(",");
                sb.append("{");
                sb.append("\"id\":").append(n.getIdNotificacion()).append(",");
                sb.append("\"titulo\":\"").append(esc(n.getTitulo())).append("\",");
                sb.append("\"mensaje\":\"").append(esc(n.getMensaje())).append("\",");
                sb.append("\"icono\":\"🔔\"");
                sb.append("}");
            }
            sb.append("]}");
            PrintWriter out = resp.getWriter();
            out.write(sb.toString());
            out.flush();
        } catch (SQLException e) {
            e.printStackTrace();
            resp.getWriter().write("{\"total\":0,\"notificaciones\":[]}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.setStatus(401); return;
        }
        try {
            Long idNotif = Long.parseLong(req.getParameter("id"));
            notifDAO.marcarLeida(idNotif);
            resp.setStatus(200);
        } catch (IllegalArgumentException | SQLException e) {
            resp.setStatus(400);
        }
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "");
    }
}