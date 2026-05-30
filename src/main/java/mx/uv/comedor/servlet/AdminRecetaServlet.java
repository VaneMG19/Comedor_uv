package mx.uv.comedor.servlet;

import mx.uv.comedor.dao.*;
import mx.uv.comedor.model.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/*
  Gestion de recetas por platillo.

  GET  /admin/recetas?idPlatillo=X   -> ver/editar la receta del platillo X
  POST /admin/recetas/agregar        -> agrega ingrediente a la receta
  POST /admin/recetas/quitar         -> quita un ingrediente
 */
@WebServlet(urlPatterns = {
        "/admin/recetas",
        "/admin/recetas/agregar",
        "/admin/recetas/quitar"
})
public class AdminRecetaServlet extends HttpServlet {

    private final RecetaDAO     recetaDAO = new RecetaDAO();
    private final PlatilloDAO   platDAO   = new PlatilloDAO();
    private final InventarioDAO invDAO    = new InventarioDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!esAdmin(req, resp)) return;
        try {
            String idStr = req.getParameter("idPlatillo");
            if (idStr == null || idStr.isBlank()) {
                // Vista general: lista todos los platillos para que el admin elija
                List<Platillo> todos = platDAO.listarTodos();
                req.setAttribute("platillos", todos);
                req.getRequestDispatcher("/WEB-INF/vistas/admin/recetas-lista.jsp")
                        .forward(req, resp);
            } else {
                // Vista de una receta especifica
                Long idPlat = Long.parseLong(idStr);
                Platillo platillo = platDAO.buscarPorId(idPlat);
                if (platillo == null) {
                    resp.sendRedirect(req.getContextPath() + "/admin/recetas?error=No encontrado");
                    return;
                }
                List<PlatilloIngrediente> receta = recetaDAO.listarIngredientesDelPlatillo(idPlat);
                List<Ingrediente> todosIng = invDAO.listarIngredientes();
                req.setAttribute("platillo", platillo);
                req.setAttribute("receta", receta);
                req.setAttribute("ingredientes", todosIng);
                req.getRequestDispatcher("/WEB-INF/vistas/admin/receta-form.jsp")
                        .forward(req, resp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "Error: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/vistas/error.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!esAdmin(req, resp)) return;
        try {
            String path = req.getServletPath();
            String idPlatStr = req.getParameter("idPlatillo");
            Long idPlatillo = Long.parseLong(idPlatStr);

            if (path.endsWith("/agregar")) {
                Long idIng = Long.parseLong(req.getParameter("idIngrediente"));
                BigDecimal cantidad = new BigDecimal(req.getParameter("cantidad"));
                recetaDAO.agregarIngrediente(idPlatillo, idIng, cantidad);
            } else if (path.endsWith("/quitar")) {
                Long idReceta = Long.parseLong(req.getParameter("idReceta"));
                recetaDAO.quitarIngrediente(idReceta);
            }
            resp.sendRedirect(req.getContextPath() + "/admin/recetas?idPlatillo=" + idPlatillo);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/admin/recetas?error=" + e.getMessage());
        }
    }

    private boolean esAdmin(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("usuario") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return false;
        }
        Usuario u = (Usuario) s.getAttribute("usuario");
        if (u.getRol() != RolEnum.ADMIN) {
            resp.sendRedirect(req.getContextPath() + "/menu");
            return false;
        }
        return true;
    }
}
