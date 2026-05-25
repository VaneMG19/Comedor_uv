package mx.uv.comedor.servlet;

import mx.uv.comedor.dao.PlatilloDAO;
import mx.uv.comedor.model.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/*
  CRUD de platillos para el administrador.

  GET  /admin/platillos/nuevo        formulario crear
  GET  /admin/platillos/editar?id=X  formulario editar
  POST /admin/platillos/guardar      guardar (crear o editar)
  POST /admin/platillos/eliminar     eliminar (soft)
  POST /admin/platillos/toggle        activar/desactivar
 */
@WebServlet(urlPatterns = {
    "/admin/platillos/nuevo",
    "/admin/platillos/editar",
    "/admin/platillos/guardar",
    "/admin/platillos/eliminar",
    "/admin/platillos/toggle"
})
public class AdminPlatilloServlet extends HttpServlet {

    private final PlatilloDAO platilloDAO = new PlatilloDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!esAdmin(req, resp)) return;

        String path = req.getServletPath();

        try {
            if ("/admin/platillos/nuevo".equals(path)) {
                req.setAttribute("modo", "nuevo");
                req.getRequestDispatcher("/WEB-INF/vistas/admin/platillo-form.jsp")
                   .forward(req, resp);
            } else if ("/admin/platillos/editar".equals(path)) {
                Long id = Long.parseLong(req.getParameter("id"));
                Platillo p = platilloDAO.buscarPorId(id);
                if (p == null) {
                    resp.sendRedirect(req.getContextPath() + "/admin/menu?error=No encontrado");
                    return;
                }
                req.setAttribute("platillo", p);
                req.setAttribute("modo", "editar");
                req.getRequestDispatcher("/WEB-INF/vistas/admin/platillo-form.jsp")
                   .forward(req, resp);
            }
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/admin/menu?error=Error");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!esAdmin(req, resp)) return;

        String path = req.getServletPath();

        try {
            if ("/admin/platillos/guardar".equals(path)) {
                guardar(req, resp);
            } else if ("/admin/platillos/eliminar".equals(path)) {
                Long id = Long.parseLong(req.getParameter("idPlatillo"));
                platilloDAO.eliminar(id);
                resp.sendRedirect(req.getContextPath() + "/admin/menu?exito=Platillo eliminado");
            } else if ("/admin/platillos/toggle".equals(path)) {
                Long id = Long.parseLong(req.getParameter("idPlatillo"));
                platilloDAO.toggleDisponibilidad(id);
                resp.sendRedirect(req.getContextPath() + "/admin/menu?exito=Disponibilidad cambiada");
            }
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/admin/menu?error=" + e.getMessage());
        }
    }

    private void guardar(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, IOException {
        String idStr = req.getParameter("idPlatillo");
        boolean esNuevo = idStr == null || idStr.isBlank();

        Platillo p;
        if (esNuevo) {
            p = new Platillo();
        } else {
            p = platilloDAO.buscarPorId(Long.parseLong(idStr));
            if (p == null) {
                resp.sendRedirect(req.getContextPath() + "/admin/menu?error=No encontrado");
                return;
            }
        }

        p.setNombre(req.getParameter("nombre"));
        p.setDescripcion(req.getParameter("descripcion"));
        p.setPrecio(new BigDecimal(req.getParameter("precio")));

        String pSubStr = req.getParameter("precioSubsidiado");
        p.setPrecioSubsidiado(pSubStr != null && !pSubStr.isBlank()
            ? new BigDecimal(pSubStr) : null);

        p.setImagen(req.getParameter("imagen"));
        p.setDisponible("on".equals(req.getParameter("disponible"))
                     || "true".equals(req.getParameter("disponible")));
        p.setTipo(TipoPlatEnum.valueOf(req.getParameter("tipo")));
        p.setCategoria(CategoriaPlatEnum.valueOf(req.getParameter("categoria")));

        try {
            p.setTiempoPrep(Integer.parseInt(req.getParameter("tiempoPrep")));
        } catch (Exception e) {
            p.setTiempoPrep(15);
        }

        if (esNuevo) {
            Long id = platilloDAO.insertar(p);
            p.setIdPlatillo(id);
        } else {
            platilloDAO.actualizar(p);
        }

        // Información nutricional (opcional)
        try {
            String calStr = req.getParameter("calorias");
            if (calStr != null && !calStr.isBlank()) {
                InformacionNutricional n = new InformacionNutricional();
                n.setIdPlatillo(p.getIdPlatillo());
                n.setCalorias(new BigDecimal(calStr));
                n.setProteinas(parseBig(req.getParameter("proteinas")));
                n.setCarbohidratos(parseBig(req.getParameter("carbohidratos")));
                n.setGrasas(parseBig(req.getParameter("grasas")));
                n.setFibra(parseBig(req.getParameter("fibra")));
                n.setSodio(parseBig(req.getParameter("sodio")));
                n.setAzucar(parseBig(req.getParameter("azucar")));
                n.setAlergenos(req.getParameter("alergenos"));
                n.setEsVegetariano("on".equals(req.getParameter("esVegetariano")));
                n.setEsVegano("on".equals(req.getParameter("esVegano")));
                n.setEsGlutenFree("on".equals(req.getParameter("esGlutenFree")));
                n.setHuellaCarbonoKg(parseBig(req.getParameter("huellaCarbonoKg")));
                platilloDAO.actualizarNutricional(n);
            }
        } catch (Exception e) {
            // Nutrición opcional, no fallar si no se puede guardar
            e.printStackTrace();
        }

        resp.sendRedirect(req.getContextPath()
            + "/admin/menu?exito=" + (esNuevo ? "Platillo creado" : "Platillo actualizado"));
    }

    private BigDecimal parseBig(String s) {
        try {
            if (s == null || s.isBlank()) return null;
            return new BigDecimal(s);
        } catch (Exception e) { return null; }
    }

    private boolean esAdmin(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return false;
        }
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario.getRol() != RolEnum.ADMIN) {
            resp.sendRedirect(req.getContextPath() + "/menu");
            return false;
        }
        return true;
    }
}
