package mx.uv.comedor.servlet;

import mx.uv.comedor.dao.PedidoDAO;
import mx.uv.comedor.dao.UsuarioDAO;
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

/*
  Muestra el ticket de venta del POS para imprimir.

  GET /pos/ticket?id=X
 */
@WebServlet("/pos/ticket")
public class POSTicketServlet extends HttpServlet {

    private final PedidoDAO  pedidoDAO  = new PedidoDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        Usuario empleado = (Usuario) session.getAttribute("usuario");
        if (empleado.getRol() != RolEnum.EMPLEADO && empleado.getRol() != RolEnum.ADMIN) {
            resp.sendRedirect(req.getContextPath() + "/menu");
            return;
        }

        try {
            Long idPedido = Long.parseLong(req.getParameter("id"));
            Pedido pedido = pedidoDAO.buscarPorId(idPedido);
            if (pedido == null) {
                resp.sendRedirect(req.getContextPath() + "/pos");
                return;
            }
            Usuario clienteUsuario = usuarioDAO.buscarPorId(pedido.getIdUsuario());

            req.setAttribute("pedido", pedido);
            req.setAttribute("cliente", clienteUsuario);
            req.getRequestDispatcher("/WEB-INF/vistas/empleado/pos-ticket.jsp")
               .forward(req, resp);

        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/pos");
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/pos?error=BD");
        }
    }
}
