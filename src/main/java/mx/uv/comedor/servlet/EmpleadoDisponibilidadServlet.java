package mx.uv.comedor.servlet;

import mx.uv.comedor.dao.*;
import mx.uv.comedor.model.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/*
  Panel rapido para que el empleado active/desactive platillos a la carta.

  GET  /empleado/disponibilidad         -> lista platillos a la carta
  POST /empleado/disponibilidad/toggle  -> activa o desactiva
 */
@WebServlet(urlPatterns = {
        "/empleado/disponibilidad",
        "/empleado/disponibilidad/toggle"
})
public class EmpleadoDisponibilidadServlet extends HttpServlet {

    private final PlatilloDAO platDAO = new PlatilloDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!validar(req, resp)) return;
        try {
            List<Platillo> todos = platDAO.listarTodos();
            List<Platillo> aLaCarta = new ArrayList<>();
            for (Platillo p : todos) {
                if (p.getTipo() == TipoPlatEnum.CARTA) {
                    aLaCarta.add(p);
                }
            }
            req.setAttribute("platillos", aLaCarta);
            req.getRequestDispatcher("/WEB-INF/vistas/empleado/disponibilidad.jsp")
                    .forward(req, resp);
        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "Error: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/vistas/error.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!validar(req, resp)) return;
        try {
            Long id = Long.parseLong(req.getParameter("idPlatillo"));
            platDAO.toggleDisponibilidad(id);
            resp.sendRedirect(req.getContextPath() + "/empleado/disponibilidad");
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/empleado/disponibilidad?error="
                    + e.getMessage());
        }
    }

    private boolean validar(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("usuario") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return false;
        }
        Usuario u = (Usuario) s.getAttribute("usuario");
        if (u.getRol() != RolEnum.EMPLEADO && u.getRol() != RolEnum.ADMIN) {
            resp.sendRedirect(req.getContextPath() + "/menu");
            return false;
        }
        return true;
    }
}
