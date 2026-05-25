package mx.uv.comedor.servlet;

import mx.uv.comedor.model.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/*
  Servlet que maneja la página del menú de platillos.
  El JSP hace todas las consultas con los DAOs directamente.

  GET /menu       vistas/menu.jsp (menú del día)
  GET /menu?tab=carta → mismo JSP, tab activa = a la carta
 */
@WebServlet("/menu")
public class MenuServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        req.getRequestDispatcher("/WEB-INF/vistas/menu.jsp")
                .forward(req, resp);
    }
}
