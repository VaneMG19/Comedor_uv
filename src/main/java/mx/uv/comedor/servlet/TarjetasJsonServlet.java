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
import java.util.List;

/*
 Devuelve las tarjetas del usuario en formato JSON para el carrito.

  GET /tarjeta/listar
 */
@WebServlet("/tarjeta/listar")
public class TarjetasJsonServlet extends HttpServlet {

    private final TarjetaUsuarioDAO tarjetaDAO = new TarjetaUsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.sendError(401);
            return;
        }
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        resp.setContentType("application/json;charset=UTF-8");
        try {
            List<TarjetaUsuario> tarjetas = tarjetaDAO.listarPorUsuario(usuario.getIdUsuario());
            StringBuilder sb = new StringBuilder();
            sb.append("{\"tarjetas\":[");
            for (int i = 0; i < tarjetas.size(); i++) {
                if (i > 0) sb.append(",");
                TarjetaUsuario t = tarjetas.get(i);
                sb.append("{");
                sb.append("\"idTarjeta\":").append(t.getIdTarjeta()).append(",");
                sb.append("\"alias\":\"").append(esc(t.getAlias())).append("\",");
                sb.append("\"marca\":\"").append(esc(t.getMarca())).append("\",");
                sb.append("\"ultimos4\":\"").append(esc(t.getUltimos4())).append("\",");
                sb.append("\"esPredeterminada\":").append(t.isEsPredeterminada());
                sb.append("}");
            }
            sb.append("]}");
            resp.getWriter().write(sb.toString());
            resp.getWriter().flush();
        } catch (SQLException e) {
            resp.getWriter().write("{\"tarjetas\":[]}");
        }
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\","\\\\").replace("\"","\\\"");
    }
}
