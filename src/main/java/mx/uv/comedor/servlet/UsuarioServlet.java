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
import mx.uv.comedor.model.*;
import mx.uv.comedor.util.DBConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Servlet CRUD de usuarios — solo accesible para ADMIN.
 *
 * GET  /admin/usuarios           lista todos los usuarios
 * GET  /admin/usuarios?id=X      detalle de un usuario
 * POST /admin/usuarios/crear     crea usuario + perfil de rol
 * POST /admin/usuarios/editar    actualiza datos del usuario
 * POST /admin/usuarios/eliminar  desactiva el usuario (soft delete)
 */
@WebServlet(urlPatterns = {
    "/admin/usuarios",
    "/admin/usuarios/crear",
    "/admin/usuarios/editar",
    "/admin/usuarios/eliminar"
})
public class UsuarioServlet extends HttpServlet {

    private final UsuarioDAO      usuarioDAO    = new UsuarioDAO();
    private final EstudianteDAO   estudianteDAO = new EstudianteDAO();
    private final AlumnoBecadoDAO becadoDAO     = new AlumnoBecadoDAO();

    // ── GET ───────────────────────────────────────────────────────

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Solo ADMIN puede acceder
        if (!esAdmin(req, resp)) return;

        String idParam = req.getParameter("id");

        try {
            if (idParam != null) {
                // Detalle de un usuario
                Usuario u = usuarioDAO.buscarPorId(Long.parseLong(idParam));
                req.setAttribute("usuario", u);
                req.getRequestDispatcher("/WEB-INF/vistas/admin/usuario-detalle.jsp")
                   .forward(req, resp);
            } else {
                // Lista completa
                List<Usuario> lista = usuarioDAO.listarTodos();
                req.setAttribute("usuarios", lista);
                req.getRequestDispatcher("/WEB-INF/vistas/admin/usuarios-lista.jsp")
                   .forward(req, resp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "Error al cargar usuarios");
            req.getRequestDispatcher("/WEB-INF/vistas/error.jsp").forward(req, resp);
        }
    }

    // ── POST ──────────────────────────────────────────────────────

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!esAdmin(req, resp)) return;

        String accion = req.getServletPath();

        try {
            switch (accion) {
                case "/admin/usuarios/crear"   -> crear(req, resp);
                case "/admin/usuarios/editar"  -> editar(req, resp);
                case "/admin/usuarios/eliminar"-> eliminar(req, resp);
                default -> resp.sendRedirect("admin/usuarios");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "Error en la operación: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/vistas/error.jsp").forward(req, resp);
        }
    }

    // ── Acciones ──────────────────────────────────────────────────

    /**
     * Crea un usuario y su perfil de rol en una transacción.
     * Si algo falla, hace rollback completo.
     */
    private void crear(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, IOException, ServletException {

        String nombre     = req.getParameter("nombre");
        String apellidos  = req.getParameter("apellidos");
        String email      = req.getParameter("email");
        String password   = req.getParameter("password");
        String rolStr     = req.getParameter("rol");
        RolEnum rol       = RolEnum.valueOf(rolStr);

        // Validar campos obligatorios
        if (nombre == null || apellidos == null || email == null ||
            password == null || rolStr == null) {
            req.setAttribute("error", "Todos los campos son obligatorios");
            req.getRequestDispatcher("/WEB-INF/vistas/admin/usuario-form.jsp")
               .forward(req, resp);
            return;
        }

        // Transacción manual para garantizar consistencia
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // 1. Crear usuario base
                Usuario u = new Usuario(nombre, apellidos, email, password, rol);
                Long idUsuario = usuarioDAO.insertar(u, password);

                // 2. Crear perfil según rol
                switch (rol) {
                    case ESTUDIANTE, BECADO -> {
                        String matricula = req.getParameter("matricula");
                        String carrera   = req.getParameter("carrera");
                        int semestre     = Integer.parseInt(req.getParameter("semestre"));

                        Estudiante est = new Estudiante(idUsuario, matricula, carrera, semestre);
                        Long idEst = estudianteDAO.insertar(est);

                        if (rol == RolEnum.BECADO) {
                            String tipoBeca = req.getParameter("tipoBeca");
                            int comidasSemana = Integer.parseInt(req.getParameter("comidasSemana"));
                            LocalDate desde = LocalDate.parse(req.getParameter("vigenciaDesde"));
                            LocalDate hasta = LocalDate.parse(req.getParameter("vigenciaHasta"));

                            AlumnoBecado becado = new AlumnoBecado(idEst, tipoBeca,
                                                                    comidasSemana, desde, hasta);
                            becadoDAO.insertar(becado);
                        }
                    }
                    case ADMIN -> {
                        int nivel       = Integer.parseInt(req.getParameter("nivelAcceso"));
                        String depto    = req.getParameter("departamento");
                        new AdministradorDAO().insertar(new Administrador(idUsuario, nivel, depto));
                    }
                    case EMPLEADO -> {
                        String numEmp = req.getParameter("numEmpleado");
                        String turno  = req.getParameter("turno");
                        String puesto = req.getParameter("puesto");
                        EmpleadoCocina emp = new EmpleadoCocina(idUsuario, numEmp,
                                TurnoEnum.valueOf(turno), puesto);
                        new EmpleadoCocinaDAO().insertar(emp);
                    }
                    case DOCENTE -> {
                        String numDoc  = req.getParameter("numEmpleadoDocente");
                        String fac     = req.getParameter("facultad");
                        String depto   = req.getParameter("departamento");
                        String cat     = req.getParameter("categoria");
                        Docente doc = new Docente(idUsuario, numDoc, fac, depto, cat);
                        new DocenteDAO().insertar(doc);
                    }
                }

                con.commit();
                resp.sendRedirect(req.getContextPath() + "/admin/usuarios?exito=creado");

            } catch (Exception e) {
                con.rollback();
                throw new SQLException("Error al crear usuario: " + e.getMessage(), e);
            }
        }
    }

    private void editar(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, IOException {

        Long idUsuario = Long.parseLong(req.getParameter("idUsuario"));
        Usuario u = usuarioDAO.buscarPorId(idUsuario);

        if (u == null) {
            resp.sendRedirect(req.getContextPath() + "/admin/usuarios?error=noEncontrado");
            return;
        }

        u.setNombre(req.getParameter("nombre"));
        u.setApellidos(req.getParameter("apellidos"));
        u.setTelefono(req.getParameter("telefono"));

        usuarioDAO.actualizarPerfil(u);
        resp.sendRedirect(req.getContextPath() + "/admin/usuarios?exito=actualizado");
    }

    private void eliminar(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, IOException {

        Long idUsuario = Long.parseLong(req.getParameter("idUsuario"));
        usuarioDAO.desactivar(idUsuario);
        resp.sendRedirect(req.getContextPath() + "/admin/usuarios?exito=eliminado");
    }

    // ── Seguridad ─────────────────────────────────────────────────

    /**
     * Verifica que el usuario en sesión sea ADMIN.
     * Si no, redirige al login.
     */
    private boolean esAdmin(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return false;
        }

        Usuario u = (Usuario) session.getAttribute("usuario");
        if (u.getRol() != RolEnum.ADMIN) {
            resp.sendRedirect(req.getContextPath() + "/menu");
            return false;
        }
        return true;
    }
}
