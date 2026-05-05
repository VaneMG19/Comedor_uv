package mx.uv.comedor.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.uv.comedor.dao.AlumnoBecadoDAO;
import mx.uv.comedor.dao.EstudianteDAO;
import mx.uv.comedor.dao.UsuarioDAO;
import mx.uv.comedor.model.AlumnoBecado;
import mx.uv.comedor.model.Estudiante;
import mx.uv.comedor.model.Usuario;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Servlet de autenticación.
 * GET  /login  muestra el formulario login.jsp
 * POST /login   valida credenciales y redirige según rol
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UsuarioDAO      usuarioDAO      = new UsuarioDAO();
    private final EstudianteDAO   estudianteDAO   = new EstudianteDAO();
    private final AlumnoBecadoDAO becadoDAO       = new AlumnoBecadoDAO();

    // ── GET: mostrar formulario ───────────────────────────────────

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Si ya hay sesión activa, redirigir al dashboard
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("usuario") != null) {
            redirigirPorRol((Usuario) session.getAttribute("usuario"), resp);
            return;
        }
        req.getRequestDispatcher("/WEB-INF/vistas/login.jsp").forward(req, resp);
    }

    // ── POST: validar credenciales ────────────────────────────────

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email    = req.getParameter("email");
        String password = req.getParameter("password");

        // Validación básica de campos vacíos
        if (email == null || email.isBlank() ||
            password == null || password.isBlank()) {
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

            // Crear sesión
            HttpSession session = req.getSession(true);
            session.setAttribute("usuario", usuario);
            session.setMaxInactiveInterval(60 * 30); // 30 minutos

            // Cargar datos específicos del rol en sesión
            cargarDatosRol(usuario, session);

            // Redirigir según rol
            redirigirPorRol(usuario, resp);

        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "Error del servidor. Intenta de nuevo.");
            req.getRequestDispatcher("/WEB-INF/vistas/login.jsp").forward(req, resp);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────

    /**
     * Carga los datos específicos del rol en la sesión.
     * Así los JSP pueden acceder a ellos sin consultar la BD de nuevo.
     */
    private void cargarDatosRol(Usuario usuario, HttpSession session)
            throws SQLException {

        switch (usuario.getRol()) {
            case ESTUDIANTE -> {
                Estudiante est = estudianteDAO.buscarPorIdUsuario(usuario.getIdUsuario());
                session.setAttribute("estudiante", est);
            }
            case BECADO -> {
                Estudiante est = estudianteDAO.buscarPorIdUsuario(usuario.getIdUsuario());
                session.setAttribute("estudiante", est);
                if (est != null) {
                    AlumnoBecado becado = becadoDAO.buscarPorIdEstudiante(est.getIdEstudiante());
                    session.setAttribute("becado", becado);
                }
            }
            // ADMIN, EMPLEADO, DOCENTE — sus DAOs se cargan en sus propios servlets
            default -> {}
        }
    }

    /**
     * Redirige al dashboard correspondiente según el rol del usuario.
     */
    private void redirigirPorRol(Usuario usuario, HttpServletResponse resp)
            throws IOException {
        switch (usuario.getRol()) {
            case ADMIN    -> resp.sendRedirect("admin/dashboard");
            case EMPLEADO -> resp.sendRedirect("empleado/dashboard");
            case DOCENTE  -> resp.sendRedirect("menu");
            case ESTUDIANTE, BECADO -> resp.sendRedirect("menu");
            default       -> resp.sendRedirect("menu");
        }
    }
}
