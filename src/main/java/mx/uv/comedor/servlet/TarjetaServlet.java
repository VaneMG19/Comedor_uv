package mx.uv.comedor.servlet;

import mx.uv.comedor.dao.TarjetaUsuarioDAO;
import mx.uv.comedor.model.TarjetaUsuario;
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
  Servlet para gestión de tarjetas del usuario.

  POST /tarjeta/agregar           agregar tarjeta
  POST /tarjeta/eliminar          eliminar tarjeta
  POST /tarjeta/predeterminada    marcar como predeterminada
 */
@WebServlet(urlPatterns = {
    "/tarjeta/agregar",
    "/tarjeta/eliminar",
    "/tarjeta/predeterminada"
})
public class TarjetaServlet extends HttpServlet {

    private final TarjetaUsuarioDAO tarjetaDAO = new TarjetaUsuarioDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        String path = req.getServletPath();

        try {
            if ("/tarjeta/agregar".equals(path)) {
                agregar(req, resp, usuario);
            } else if ("/tarjeta/eliminar".equals(path)) {
                eliminar(req, resp, usuario);
            } else if ("/tarjeta/predeterminada".equals(path)) {
                marcarPredeterminada(req, resp, usuario);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/perfil?tab=tarjetas&error=Error al procesar");
        }
    }

    private void agregar(HttpServletRequest req, HttpServletResponse resp, Usuario u)
            throws SQLException, IOException {
        String numero = req.getParameter("numeroTarjeta");
        if (numero == null) numero = "";
        // Quitar espacios y guiones
        numero = numero.replaceAll("[\\s-]", "");

        if (!numero.matches("^[0-9]{13,19}$")) {
            resp.sendRedirect(req.getContextPath()
                + "/perfil?tab=tarjetas&error=Numero de tarjeta invalido");
            return;
        }

        String ultimos4 = numero.substring(numero.length() - 4);

        // Detectar marca por primer dígito (algoritmo de Luhn simplificado)
        String marca = detectarMarca(numero);

        TarjetaUsuario t = new TarjetaUsuario();
        t.setIdUsuario(u.getIdUsuario());
        t.setAlias(req.getParameter("alias"));
        t.setMarca(marca);
        t.setUltimos4(ultimos4);
        t.setNombreTitular(req.getParameter("nombreTitular"));
        try {
            t.setMesVencimiento(Integer.parseInt(req.getParameter("mesVencimiento")));
            t.setAnioVencimiento(Integer.parseInt(req.getParameter("anioVencimiento")));
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath()
                + "/perfil?tab=tarjetas&error=Fecha invalida");
            return;
        }
        t.setEsPredeterminada("on".equals(req.getParameter("esPredeterminada")));

        Long idNueva = tarjetaDAO.insertar(t);

        // Si la marcó como predeterminada, actualizar las otras
        if (t.isEsPredeterminada()) {
            tarjetaDAO.marcarPredeterminada(idNueva, u.getIdUsuario());
        }

        resp.sendRedirect(req.getContextPath()
            + "/perfil?tab=tarjetas&exito=Tarjeta agregada correctamente");
    }

    private void eliminar(HttpServletRequest req, HttpServletResponse resp, Usuario u)
            throws SQLException, IOException {
        try {
            Long idTarjeta = Long.parseLong(req.getParameter("idTarjeta"));
            tarjetaDAO.eliminar(idTarjeta, u.getIdUsuario());
            resp.sendRedirect(req.getContextPath()
                + "/perfil?tab=tarjetas&exito=Tarjeta eliminada");
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath()
                + "/perfil?tab=tarjetas&error=Tarjeta invalida");
        }
    }

    private void marcarPredeterminada(HttpServletRequest req, HttpServletResponse resp, Usuario u)
            throws SQLException, IOException {
        try {
            Long idTarjeta = Long.parseLong(req.getParameter("idTarjeta"));
            tarjetaDAO.marcarPredeterminada(idTarjeta, u.getIdUsuario());
            resp.sendRedirect(req.getContextPath()
                + "/perfil?tab=tarjetas&exito=Tarjeta marcada como predeterminada");
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/perfil?tab=tarjetas");
        }
    }

    /** Detecta la marca por el primer dígito del número. */
    private String detectarMarca(String numero) {
        if (numero == null || numero.isEmpty()) return "OTRA";
        char c = numero.charAt(0);
        if (c == '4') return "VISA";
        if (c == '5') return "MASTERCARD";
        if (c == '3') return "AMEX";
        return "OTRA";
    }
}
