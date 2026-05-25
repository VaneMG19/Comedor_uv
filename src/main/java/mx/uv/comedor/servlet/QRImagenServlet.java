package mx.uv.comedor.servlet;

import mx.uv.comedor.dao.PedidoDAO;
import mx.uv.comedor.model.Pedido;
import mx.uv.comedor.model.Usuario;
import mx.uv.comedor.util.QRService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

/*
  Devuelve el QR de un pedido como imagen PNG para mostrarse inline en la página.

  GET /pedido/qr?id=X
 */
@WebServlet("/pedido/qr")
public class QRImagenServlet extends HttpServlet {

    private final PedidoDAO pedidoDAO = new PedidoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.sendError(401);
            return;
        }
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.sendError(400);
            return;
        }

        try {
            Long idPedido = Long.parseLong(idParam);
            Pedido pedido = pedidoDAO.buscarPorId(idPedido);
            if (pedido == null) {
                resp.sendError(404);
                return;
            }

            String contenido = String.format(
                "PEDIDO_UV|id=%d|folio=%s|usuario=%d|total=%s",
                pedido.getIdPedido(),
                pedido.getFolio() != null ? pedido.getFolio() : "SF",
                pedido.getIdUsuario(),
                pedido.getTotal() != null ? pedido.getTotal().toPlainString() : "0"
            );
            byte[] qrPng = QRService.generarQR(contenido, 300);

            resp.setContentType("image/png");
            resp.setContentLength(qrPng.length);
            resp.getOutputStream().write(qrPng);
            resp.getOutputStream().flush();

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(500);
        }
    }
}
