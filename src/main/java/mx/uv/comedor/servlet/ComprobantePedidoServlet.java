package mx.uv.comedor.servlet;

import mx.uv.comedor.dao.PedidoDAO;
import mx.uv.comedor.dao.UsuarioDAO;
import mx.uv.comedor.model.Pedido;
import mx.uv.comedor.model.Usuario;
import mx.uv.comedor.util.PDFService;
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
  Genera el PDF de comprobante de un pedido y lo descarga.

  GET /pedido/comprobante?id=X
 */
@WebServlet("/pedido/comprobante")
public class ComprobantePedidoServlet extends HttpServlet {

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
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");

        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.sendRedirect(req.getContextPath() + "/pedido/historial");
            return;
        }

        try {
            Long idPedido = Long.parseLong(idParam);
            Pedido pedido = pedidoDAO.buscarPorId(idPedido);

            if (pedido == null) {
                resp.sendError(404, "Pedido no encontrado");
                return;
            }

            // Verificar que el pedido sea del usuario (o que sea admin/empleado)
            if (!pedido.getIdUsuario().equals(usuarioSesion.getIdUsuario())
                && usuarioSesion.getRol() != mx.uv.comedor.model.RolEnum.ADMIN
                && usuarioSesion.getRol() != mx.uv.comedor.model.RolEnum.EMPLEADO) {
                resp.sendError(403, "No tienes acceso a este pedido");
                return;
            }

            // Cargar datos del usuario dueño del pedido
            Usuario usuarioPedido = usuarioDAO.buscarPorId(pedido.getIdUsuario());

            // Generar QR con datos del pedido (texto que se puede escanear)
            String contenidoQR = String.format(
                "PEDIDO_UV|id=%d|folio=%s|usuario=%d|total=%s",
                pedido.getIdPedido(),
                pedido.getFolio() != null ? pedido.getFolio() : "SF",
                pedido.getIdUsuario(),
                pedido.getTotal() != null ? pedido.getTotal().toPlainString() : "0"
            );
            byte[] qrPng = QRService.generarQR(contenidoQR, 250);

            // Generar el PDF
            byte[] pdf = PDFService.generarComprobante(pedido, usuarioPedido, qrPng);

            // Enviar al cliente como descarga
            String filename = "comprobante_" +
                (pedido.getFolio() != null ? pedido.getFolio() : pedido.getIdPedido()) + ".pdf";

            resp.setContentType("application/pdf");
            resp.setHeader("Content-Disposition",
                "inline; filename=\"" + filename + "\"");
            resp.setContentLength(pdf.length);
            resp.getOutputStream().write(pdf);
            resp.getOutputStream().flush();

        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/pedido/historial");
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(500, "Error al cargar pedido: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(500, "Error al generar PDF: " + e.getMessage());
        }
    }
}
