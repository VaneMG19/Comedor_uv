package mx.uv.comedor.servlet;

import mx.uv.comedor.dao.EmpleadoCocinaDAO;
import mx.uv.comedor.dao.PedidoDAO;
import mx.uv.comedor.model.EmpleadoCocina;
import mx.uv.comedor.model.EstadoPedidoEnum;
import mx.uv.comedor.model.Pedido;
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
import java.util.List;

@WebServlet(urlPatterns = {"/empleado/dashboard", "/empleado/estado"})
public class EmpleadoServlet extends HttpServlet {

    private final PedidoDAO        pedidoDAO = new PedidoDAO();
    private final EmpleadoCocinaDAO empDAO   = new EmpleadoCocinaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!esEmpleado(req, resp)) return;
        try {
            List<Pedido> pedidos = pedidoDAO.listarActivos();
            req.setAttribute("pedidos", pedidos);
            req.getRequestDispatcher("/WEB-INF/vistas/empleado/dashboard.jsp").forward(req, resp);
        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "Error al cargar pedidos");
            req.getRequestDispatcher("/WEB-INF/vistas/error.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!esEmpleado(req, resp)) return;
        try {
            Long idPedido = Long.parseLong(req.getParameter("idPedido"));
            EstadoPedidoEnum nuevoEstado = EstadoPedidoEnum.valueOf(req.getParameter("nuevoEstado"));
            String comentario = req.getParameter("comentario");
            HttpSession session = req.getSession(false);
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            EmpleadoCocina emp = empDAO.buscarPorIdUsuario(usuario.getIdUsuario());
            pedidoDAO.cambiarEstado(idPedido, nuevoEstado, emp.getIdEmpleado(), comentario);
            resp.sendRedirect(req.getContextPath() + "/empleado/dashboard?exito=estadoActualizado");
        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "Error al cambiar estado: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/vistas/error.jsp").forward(req, resp);
        }
    }

    private boolean esEmpleado(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return false;
        }
        Usuario u = (Usuario) session.getAttribute("usuario");
        if (u.getRol() != RolEnum.EMPLEADO && u.getRol() != RolEnum.ADMIN) {
            resp.sendRedirect(req.getContextPath() + "/menu");
            return false;
        }
        return true;
    }
}