package mx.uv.comedor.servlet;

import mx.uv.comedor.dao.EmpleadoCocinaDAO;
import mx.uv.comedor.dao.PedidoDAO;
import mx.uv.comedor.model.EmpleadoCocina;
import mx.uv.comedor.model.EstadoPedidoEnum;
import mx.uv.comedor.model.RolEnum;
import mx.uv.comedor.model.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(urlPatterns = { "/empleado/dashboard", "/empleado/estado" })
public class EmpleadoServlet extends HttpServlet {

    private final PedidoDAO         pedidoDAO   = new PedidoDAO();
    private final EmpleadoCocinaDAO empleadoDAO = new EmpleadoCocinaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario.getRol() != RolEnum.EMPLEADO && usuario.getRol() != RolEnum.ADMIN) {
            resp.sendRedirect(req.getContextPath() + "/menu");
            return;
        }
        req.getRequestDispatcher("/WEB-INF/vistas/empleado/dashboard.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario.getRol() != RolEnum.EMPLEADO && usuario.getRol() != RolEnum.ADMIN) {
            resp.sendError(403);
            return;
        }
        try {
            Long  idPedido    = Long.parseLong(req.getParameter("idPedido"));
            String nuevoEst   = req.getParameter("nuevoEstado");
            String comentario = req.getParameter("comentario");
            EstadoPedidoEnum estado = EstadoPedidoEnum.valueOf(nuevoEst);

            // FIX: el log de estado requiere el id_empleado_cocina, NO el id_usuario.
            // Buscamos el empleado_cocina asociado al usuario logueado.
            Long idEmpleadoCocina = null;
            EmpleadoCocina empCocina = empleadoDAO.buscarPorIdUsuario(usuario.getIdUsuario());
            if (empCocina != null) {
                idEmpleadoCocina = empCocina.getIdEmpleado();
            }
            // Si es ADMIN que actúa como empleado, idEmpleadoCocina puede ser null —
            // el log entonces se queda sin firmar.

            pedidoDAO.cambiarEstado(idPedido, estado, idEmpleadoCocina, comentario);
            resp.sendRedirect(req.getContextPath() + "/empleado/dashboard");
        } catch (IllegalArgumentException e) {
            resp.sendRedirect(req.getContextPath() + "/empleado/dashboard");
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/empleado/dashboard?error=BD");
        }
    }
}