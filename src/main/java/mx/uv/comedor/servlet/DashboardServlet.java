package mx.uv.comedor.servlet;

import mx.uv.comedor.model.RolEnum;
import mx.uv.comedor.model.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/*
  Servlet que maneja la ruta del dashboard de admin.
 El dashboard de empleado lo maneja EmpleadoServlet.

  Ruta:
  GET /admin/dashboard vistas/admin/dashboard.jsp
 */
@WebServlet("/admin/dashboard")
public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        // Solo admins pueden ver el dashboard de admin
        if (usuario.getRol() != RolEnum.ADMIN) {
            resp.sendRedirect(req.getContextPath() + "/menu");
            return;
        }

        req.getRequestDispatcher("/WEB-INF/vistas/admin/dashboard.jsp")
                .forward(req, resp);
    }
}