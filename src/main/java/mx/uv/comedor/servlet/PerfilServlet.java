package mx.uv.comedor.servlet;

import mx.uv.comedor.dao.UsuarioDAO;
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
  Servlet del perfil del usuario.
  GET  /perfil              vistas/perfil.jsp
  POST /perfil/actualizar   actualiza datos personales o contraseña
 */
@WebServlet(urlPatterns = { "/perfil", "/perfil/actualizar" })
public class PerfilServlet extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        req.getRequestDispatcher("/WEB-INF/vistas/perfil.jsp")
                .forward(req, resp);
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
        String  accion  = req.getParameter("accion");

        try {
            if ("datos".equals(accion)) {
                usuario.setNombre(req.getParameter("nombre"));
                usuario.setApellidos(req.getParameter("apellidos"));
                usuario.setTelefono(req.getParameter("telefono"));
                usuarioDAO.actualizarPerfil(usuario);
                session.setAttribute("usuario", usuario);
                resp.sendRedirect(req.getContextPath()
                        + "/perfil?exito=Datos actualizados correctamente");

            } else if ("password".equals(accion)) {
                String passActual = req.getParameter("passwordActual");
                String passNueva  = req.getParameter("passwordNueva");

                // Verificar la contraseña actual primero
                Usuario verif = usuarioDAO.login(usuario.getEmail(), passActual);
                if (verif == null) {
                    resp.sendRedirect(req.getContextPath()
                            + "/perfil?error=La contrasena actual es incorrecta");
                    return;
                }

                boolean ok = usuarioDAO.cambiarPassword(
                        usuario.getIdUsuario(), passNueva);

                if (ok) {
                    session.invalidate();
                    resp.sendRedirect(req.getContextPath()
                            + "/login?msg=Contrasena cambiada. Inicia sesion nuevamente.");
                } else {
                    resp.sendRedirect(req.getContextPath()
                            + "/perfil?error=No se pudo cambiar la contrasena");
                }
            } else {
                resp.sendRedirect(req.getContextPath() + "/perfil");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath()
                    + "/perfil?error=Error al actualizar. Intenta de nuevo.");
        }
    }
}