package mx.uv.comedor.servlet;

import mx.uv.comedor.dao.CalificacionDAO;
import mx.uv.comedor.dao.PedidoDAO;
import mx.uv.comedor.model.Calificacion;
import mx.uv.comedor.model.DetallePedido;
import mx.uv.comedor.model.Pedido;
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
  Servlet para calificaciones de usuarios.

  GET  /calificar?idPedido=X  muestra formulario
  POST /calificar/enviar       guarda calificaciones
 */
@WebServlet(urlPatterns = { "/calificar", "/calificar/enviar" })
public class CalificacionUsuarioServlet extends HttpServlet {

    private final CalificacionDAO califDAO  = new CalificacionDAO();
    private final PedidoDAO       pedidoDAO = new PedidoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        try {
            Long idPedido = Long.parseLong(req.getParameter("idPedido"));
            Pedido pedido = pedidoDAO.buscarPorId(idPedido);
            if (pedido == null || !pedido.getIdUsuario().equals(usuario.getIdUsuario())) {
                resp.sendRedirect(req.getContextPath() + "/pedido/historial");
                return;
            }

            if (pedido.getEstado() != mx.uv.comedor.model.EstadoPedidoEnum.ENTREGADO
                && pedido.getEstado() != mx.uv.comedor.model.EstadoPedidoEnum.LISTO) {
                resp.sendRedirect(req.getContextPath()
                    + "/pedido/historial?error=Solo puedes calificar pedidos entregados");
                return;
            }

            if (califDAO.pedidoCalificado(idPedido)) {
                resp.sendRedirect(req.getContextPath()
                    + "/pedido/detalle?id=" + idPedido + "&info=Ya calificaste este pedido");
                return;
            }

            req.setAttribute("pedido", pedido);
            req.getRequestDispatcher("/WEB-INF/vistas/calificar.jsp").forward(req, resp);

        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/pedido/historial");
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/pedido/historial?error=Error al cargar");
        }
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

        try {
            Long idPedido = Long.parseLong(req.getParameter("idPedido"));
            Pedido pedido = pedidoDAO.buscarPorId(idPedido);
            if (pedido == null || !pedido.getIdUsuario().equals(usuario.getIdUsuario())) {
                resp.sendError(403);
                return;
            }

            int total = 0;
            if (pedido.getDetalles() != null) {
                for (DetallePedido d : pedido.getDetalles()) {
                    // CRÍTICO: validar idPlatillo
                    if (d.getIdPlatillo() == null || d.getIdPlatillo() <= 0) continue;

                    String puntStr = req.getParameter("puntuacion_" + d.getIdPlatillo());
                    String coment  = req.getParameter("comentario_" + d.getIdPlatillo());
                    if (puntStr == null || puntStr.isBlank()) continue;
                    int punt;
                    try { punt = Integer.parseInt(puntStr); }
                    catch (NumberFormatException nfe) { continue; }
                    if (punt < 1 || punt > 5) continue;

                    Calificacion c = new Calificacion();
                    c.setIdUsuario(usuario.getIdUsuario());
                    c.setIdPlatillo(d.getIdPlatillo());
                    c.setIdPedido(idPedido);
                    c.setPuntuacion(punt);
                    c.setComentario(coment);
                    califDAO.insertar(c);
                    total++;
                }
            }

            if (total == 0) {
                resp.sendRedirect(req.getContextPath()
                    + "/pedido/detalle?id=" + idPedido + "&error=Selecciona al menos una calificacion");
                return;
            }

            resp.sendRedirect(req.getContextPath()
                + "/pedido/detalle?id=" + idPedido + "&exito=Gracias por tu reseña!");

        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/pedido/historial");
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath()
                + "/pedido/historial?error=Error al guardar reseña");
        }
    }
}
