package mx.uv.comedor.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.uv.comedor.model.RolEnum;
import mx.uv.comedor.model.Usuario;

import java.io.IOException;

/*
  Servlet para gestion del menu semanal por el administrador.
  GET /admin/menu -> vistas/admin/menu-gestion.jsp
 */
@WebServlet("/admin/menu")
public class AdminMenuServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario.getRol() != RolEnum.ADMIN) {
            resp.sendRedirect(req.getContextPath() + "/menu");
            return;
        }
        req.getRequestDispatcher("/WEB-INF/vistas/admin/menu-gestion.jsp")
           .forward(req, resp);
    }
}
