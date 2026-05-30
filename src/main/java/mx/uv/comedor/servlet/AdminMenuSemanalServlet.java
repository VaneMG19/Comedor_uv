package mx.uv.comedor.servlet;

import mx.uv.comedor.dao.*;
import mx.uv.comedor.model.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@WebServlet(urlPatterns = {
        "/admin/menu/semanal",
        "/admin/menu/semanal/agregar",
        "/admin/menu/semanal/quitar"
})
public class AdminMenuSemanalServlet extends HttpServlet {

    private final MenuSemanalDAO   menuDAO  = new MenuSemanalDAO();
    private final AdministradorDAO adminDAO = new AdministradorDAO();
    private final PlatilloDAO      platDAO  = new PlatilloDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Usuario u = validarAdmin(req, resp);
        if (u == null) return;
        try {
            Administrador admin = adminDAO.buscarPorIdUsuario(u.getIdUsuario());
            if (admin == null) {
                resp.sendRedirect(req.getContextPath() + "/admin/dashboard?error=Sin perfil admin");
                return;
            }
            Long idMenu = menuDAO.obtenerOCrearMenuSemanaActual(admin.getIdAdmin());

            Map<DiaEnum, Map<CatMenuEnum, List<Platillo>>> programacion = new LinkedHashMap<>();
            for (DiaEnum d : DiaEnum.values()) {
                Map<CatMenuEnum, List<Platillo>> porCat = new LinkedHashMap<>();
                porCat.put(CatMenuEnum.DESAYUNO, menuDAO.listarPlatillosDelDia(idMenu, d, CatMenuEnum.DESAYUNO));
                porCat.put(CatMenuEnum.COMIDA,   menuDAO.listarPlatillosDelDia(idMenu, d, CatMenuEnum.COMIDA));
                programacion.put(d, porCat);
            }

            List<Platillo> todos = platDAO.listarTodos();
            List<Platillo> disponibles = new ArrayList<>();
            for (Platillo p : todos) {
                if (p.getTipo() == TipoPlatEnum.MENU && p.isDisponible()) {
                    disponibles.add(p);
                }
            }

            req.setAttribute("idMenu", idMenu);
            req.setAttribute("programacion", programacion);
            req.setAttribute("platillosDisponibles", disponibles);

            req.getRequestDispatcher("/WEB-INF/vistas/admin/menu-semanal.jsp")
                    .forward(req, resp);
        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "Error al cargar menu semanal: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/vistas/error.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Usuario u = validarAdmin(req, resp);
        if (u == null) return;
        try {
            Administrador admin = adminDAO.buscarPorIdUsuario(u.getIdUsuario());
            Long idMenu = menuDAO.obtenerOCrearMenuSemanaActual(admin.getIdAdmin());

            DiaEnum dia = DiaEnum.valueOf(req.getParameter("dia"));
            CatMenuEnum cat = CatMenuEnum.valueOf(req.getParameter("categoria"));
            Long idPlatillo = Long.parseLong(req.getParameter("idPlatillo"));

            String path = req.getServletPath();
            if (path.endsWith("/agregar")) {
                int cupo = 50;
                try { cupo = Integer.parseInt(req.getParameter("cupo")); } catch (Exception e) {}
                if (cupo < 1) cupo = 1;
                menuDAO.agregarPlatilloADia(idMenu, dia, cat, idPlatillo, cupo);
            } else if (path.endsWith("/quitar")) {
                menuDAO.quitarPlatilloDeDia(idMenu, dia, cat, idPlatillo);
            }

            resp.sendRedirect(req.getContextPath() + "/admin/menu/semanal?dia=" + dia.name());
        } catch (SQLException | IllegalArgumentException e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/admin/menu/semanal?error=" + e.getMessage());
        }
    }

    private Usuario validarAdmin(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("usuario") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return null;
        }
        Usuario u = (Usuario) s.getAttribute("usuario");
        if (u.getRol() != RolEnum.ADMIN) {
            resp.sendRedirect(req.getContextPath() + "/menu");
            return null;
        }
        return u;
    }
}
