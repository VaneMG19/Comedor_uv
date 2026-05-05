package mx.uv.comedor.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.uv.comedor.dao.CalificacionDAO;
import mx.uv.comedor.model.Calificacion;
import mx.uv.comedor.model.EstadisticaPlatillo;
import mx.uv.comedor.model.RespuestaCalificacion;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * GET  /calificaciones?id=X   → lista calificaciones de un platillo
 * POST /calificaciones/crear  → el usuario califica su pedido entregado
 * POST /calificaciones/responder → el admin responde una calificación
 * POST /calificaciones/aprobar   → el admin aprueba/rechaza un comentario
 */
@WebServlet(urlPatterns = {
    "/calificaciones",
    "/calificaciones/crear",
    "/calificaciones/responder",
    "/calificaciones/aprobar"
})
public class CalificacionServlet extends HttpServlet {

    private final CalificacionDAO califDAO = new CalificacionDAO();
    private final AdministradorDAO admDAO  = new AdministradorDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!estaAutenticado(req, resp)) return;

        try {
            String idParam = req.getParameter("id");
            if (idParam == null) {
                // Panel del admin — todas las calificaciones
                List<Calificacion> todas = califDAO.listarTodas();
                req.setAttribute("calificaciones", todas);
                req.getRequestDispatcher(
                    "/WEB-INF/vistas/admin/calificaciones.jsp")
                   .forward(req, resp);
            } else {
                // Calificaciones de un platillo específico (vista pública)
                Long idPlatillo = Long.parseLong(idParam);
                List<Calificacion> lista =
                    califDAO.listarPorPlatillo(idPlatillo);
                EstadisticaPlatillo est =
                    califDAO.obtenerEstadistica(idPlatillo);
                req.setAttribute("calificaciones", lista);
                req.setAttribute("estadistica", est);
                req.setAttribute("idPlatillo", idPlatillo);
                req.getRequestDispatcher(
                    "/WEB-INF/vistas/calificaciones-platillo.jsp")
                   .forward(req, resp);
            }
        } catch (SQLException e) {
            manejarError(req, resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!estaAutenticado(req, resp)) return;

        try {
            switch (req.getServletPath()) {
                case "/calificaciones/crear"    -> crear(req, resp);
                case "/calificaciones/responder"-> responder(req, resp);
                case "/calificaciones/aprobar"  -> aprobar(req, resp);
            }
        } catch (SQLException e) {
            manejarError(req, resp, e);
        }
    }

    // ── Acciones ──────────────────────────────────────────────────

    private void crear(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, IOException, ServletException {

        HttpSession session = req.getSession(false);
        Usuario usuario     = (Usuario) session.getAttribute("usuario");

        Long   idPedido   = Long.parseLong(req.getParameter("idPedido"));
        Long   idPlatillo = Long.parseLong(req.getParameter("idPlatillo"));
        int    puntuacion = Integer.parseInt(req.getParameter("puntuacion"));
        String comentario = req.getParameter("comentario");

        // Validar que el pedido no haya sido ya calificado
        if (califDAO.pedidoCalificado(idPedido)) {
            req.setAttribute("error", "Este pedido ya fue calificado");
            req.getRequestDispatcher("/WEB-INF/vistas/error.jsp")
               .forward(req, resp);
            return;
        }

        Calificacion c = new Calificacion(
            usuario.getIdUsuario(), idPlatillo, idPedido,
            puntuacion, comentario
        );
        califDAO.insertar(c);

        resp.sendRedirect(req.getContextPath() +
            "/pedido/historial?exito=calificado");
    }

    private void responder(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, IOException {

        HttpSession session   = req.getSession(false);
        Usuario     usuario   = (Usuario) session.getAttribute("usuario");
        Administrador admin   = admDAO.buscarPorIdUsuario(usuario.getIdUsuario());

        Long   idCalif    = Long.parseLong(req.getParameter("idCalificacion"));
        String respuesta  = req.getParameter("respuesta");

        RespuestaCalificacion r = new RespuestaCalificacion(
            idCalif, admin.getIdAdmin(), respuesta);
        califDAO.insertarRespuesta(r);

        resp.sendRedirect(req.getContextPath() +
            "/calificaciones?exito=respondida");
    }

    private void aprobar(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, IOException {

        Long    idCalif  = Long.parseLong(req.getParameter("idCalificacion"));
        boolean aprobada = Boolean.parseBoolean(req.getParameter("aprobada"));
        califDAO.cambiarAprobacion(idCalif, aprobada);

        resp.sendRedirect(req.getContextPath() + "/calificaciones");
    }

    // ── Helpers ───────────────────────────────────────────────────

    private boolean estaAutenticado(HttpServletRequest req,
                                     HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return false;
        }
        return true;
    }

    private void manejarError(HttpServletRequest req, HttpServletResponse resp,
                               Exception e) throws ServletException, IOException {
        e.printStackTrace();
        req.setAttribute("error", "Error: " + e.getMessage());
        req.getRequestDispatcher("/WEB-INF/vistas/error.jsp").forward(req, resp);
    }
}
