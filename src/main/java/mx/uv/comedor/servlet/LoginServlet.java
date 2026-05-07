package mx.uv.comedor.servlet;

import mx.uv.comedor.dao.AlumnoBecadoDAO;
import mx.uv.comedor.dao.EstudianteDAO;
import mx.uv.comedor.dao.UsuarioDAO;
import mx.uv.comedor.model.AlumnoBecado;
import mx.uv.comedor.model.Estudiante;
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

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UsuarioDAO      usuarioDAO = new UsuarioDAO();
    private final EstudianteDAO   estDAO     = new EstudianteDAO();
    private final AlumnoBecadoDAO becadoDAO  = new AlumnoBecadoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("usuario") != null) {
            redirigirPorRol((Usuario) session.getAttribute("usuario"), resp);
            return;
        }
        req.getRequestDispatcher("/WEB-INF/vistas/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String email    = req.getParameter("email");
        String password = req.getParameter("password");
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            req.setAttribute("error", "Email y contraseña son obligatorios");
            req.getRequestDispatcher("/WEB-INF/vistas/login.jsp").forward(req, resp);
            return;
        }
        try {
            Usuario usuario = usuarioDAO.login(email.trim(), password);
            if (usuario == null) {
                req.setAttribute("error", "Email o contraseña incorrectos");
                req.getRequestDispatcher("/WEB-INF/vistas/login.jsp").forward(req, resp);
                return;
            }
            HttpSession session = req.getSession(true);
            session.setAttribute("usuario", usuario);
            session.setMaxInactiveInterval(60 * 30);
            cargarDatosRol(usuario, session);
            redirigirPorRol(usuario, resp);
        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "Error del servidor. Intenta de nuevo.");
            req.getRequestDispatcher("/WEB-INF/vistas/login.jsp").forward(req, resp);
        }
    }

    private void cargarDatosRol(Usuario usuario, HttpSession session) throws SQLException {
        if (usuario.getRol() == RolEnum.ESTUDIANTE) {
            Estudiante est = estDAO.buscarPorIdUsuario(usuario.getIdUsuario());
            session.setAttribute("estudiante", est);
        } else if (usuario.getRol() == RolEnum.BECADO) {
            Estudiante est = estDAO.buscarPorIdUsuario(usuario.getIdUsuario());
            session.setAttribute("estudiante", est);
            if (est != null) {
                AlumnoBecado becado = becadoDAO.buscarPorIdEstudiante(est.getIdEstudiante());
                session.setAttribute("becado", becado);
            }
        }
    }

    private void redirigirPorRol(Usuario usuario, HttpServletResponse resp) throws IOException {
        switch (usuario.getRol()) {
            case ADMIN:    resp.sendRedirect("admin/dashboard"); break;
            case EMPLEADO: resp.sendRedirect("empleado/dashboard"); break;
            default:       resp.sendRedirect("menu"); break;
        }
    }
}