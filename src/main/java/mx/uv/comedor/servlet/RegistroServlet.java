package mx.uv.comedor.servlet;

import mx.uv.comedor.dao.*;
import mx.uv.comedor.model.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/*
  Servlet de registro de nuevos usuarios.

  Lógica especial para becados:
  Si el usuario marca "Estudiante con beca", el sistema verifica
    que su email esté en la tabla becados_autorizados y vigente.
  Si no está autorizado, le pide registrarse como estudiante normal.
 */
@WebServlet("/registro")
public class RegistroServlet extends HttpServlet {

    private final UsuarioDAO          usuarioDAO    = new UsuarioDAO();
    private final EstudianteDAO       estudianteDAO = new EstudianteDAO();
    private final DocenteDAO          docenteDAO    = new DocenteDAO();
    private final AlumnoBecadoDAO     becadoDAO     = new AlumnoBecadoDAO();
    private final BecadoAutorizadoDAO becadoAutDAO  = new BecadoAutorizadoDAO();

    private static final String[] DOMINIOS_UV = {
            "@uv.mx", "@estudiantes.uv.mx", "@alumni.uv.mx"
    };

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/vistas/registro.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String nombre    = req.getParameter("nombre");
        String apellidos = req.getParameter("apellidos");
        String email     = req.getParameter("email");
        String telefono  = req.getParameter("telefono");
        String password  = req.getParameter("password");
        String rolStr    = req.getParameter("rol");

        String emailLower = email != null ? email.toLowerCase().trim() : "";
        boolean esUV = false;
        for (String d : DOMINIOS_UV) if (emailLower.endsWith(d)) { esUV = true; break; }

        if (!esUV) {
            mostrarError(req, resp,
                    "Solo se aceptan correos @uv.mx o @estudiantes.uv.mx");
            return;
        }

        try {
            // Verificar que el email no exista en usuarios ya registrados
            if (usuarioDAO.buscarPorEmail(emailLower) != null) {
                mostrarError(req, resp, "Ya existe una cuenta con ese correo.");
                return;
            }

            //Validación especial para BECADO
            BecadoAutorizado becadoAut = null;
            if (RolEnum.BECADO.name().equals(rolStr)) {
                becadoAut = becadoAutDAO.buscarPorEmail(emailLower);
                if (becadoAut == null) {
                    mostrarError(req, resp,
                            "Tu correo no está en la lista de becados autorizados. " +
                                    "Si tienes beca aprobada, contacta a la administración del comedor. " +
                                    "Por ahora puedes registrarte como Estudiante.");
                    return;
                }
                if (!becadoAut.estaVigente()) {
                    String motivo = "REGISTRADO".equals(becadoAut.getEstado())
                            ? "Esta beca ya fue registrada anteriormente."
                            : "Esta beca no está vigente.";
                    mostrarError(req, resp, motivo);
                    return;
                }
            }

            //  Crear el usuario base
            Usuario nuevo = new Usuario();
            nuevo.setNombre(nombre);
            nuevo.setApellidos(apellidos);
            nuevo.setEmail(emailLower);
            nuevo.setTelefono(telefono);
            nuevo.setRol(RolEnum.valueOf(rolStr));
            nuevo.setActivo(true);

            Long idUsuario = usuarioDAO.insertar(nuevo, password);

            //  Datos específicos según rol
            if (RolEnum.ESTUDIANTE.name().equals(rolStr)) {
                Estudiante est = new Estudiante();
                est.setIdUsuario(idUsuario);
                est.setMatricula(req.getParameter("matricula"));
                est.setCarrera(req.getParameter("carrera"));
                try {
                    est.setSemestre(Integer.parseInt(req.getParameter("semestre")));
                } catch (NumberFormatException nfe) { est.setSemestre(1); }
                estudianteDAO.insertar(est);

            } else if (RolEnum.DOCENTE.name().equals(rolStr)) {
                Docente doc = new Docente();
                doc.setIdUsuario(idUsuario);
                doc.setNumEmpleadoDocente(req.getParameter("numEmpleadoDocente"));
                doc.setFacultad(req.getParameter("facultad"));
                doc.setDepartamento(req.getParameter("departamento"));
                doc.setCategoria(req.getParameter("categoria"));
                docenteDAO.insertar(doc);

            } else if (RolEnum.BECADO.name().equals(rolStr) && becadoAut != null) {
                // Crear primero el estudiante (todo becado es estudiante)
                Estudiante est = new Estudiante();
                est.setIdUsuario(idUsuario);
                est.setMatricula(becadoAut.getMatricula());
                est.setCarrera(req.getParameter("carrera"));
                try {
                    est.setSemestre(Integer.parseInt(req.getParameter("semestre")));
                } catch (NumberFormatException nfe) { est.setSemestre(1); }
                Long idEstudiante = estudianteDAO.insertar(est);

                // Crear el registro de becado con datos pre-aprobados
                AlumnoBecado becado = new AlumnoBecado();
                becado.setIdEstudiante(idEstudiante);
                becado.setTipoBeca(becadoAut.getTipoBeca());
                becado.setComidasDisponiblesSemana(becadoAut.getComidasSemana());
                becado.setComidasUsadasSemana(0);
                becado.setVigenciaDesde(becadoAut.getVigenciaDesde());
                becado.setVigenciaHasta(becadoAut.getVigenciaHasta());
                becadoDAO.insertar(becado);

                // Marcar la beca como REGISTRADA en la lista de autorizados
                becadoAutDAO.marcarRegistrado(becadoAut.getIdBecadoAut());
            }

            resp.sendRedirect(req.getContextPath()
                    + "/login?msg=Cuenta creada exitosamente. Inicia sesion.");

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError(req, resp, "Error al crear la cuenta. Intenta de nuevo.");
        }
    }

    private void mostrarError(HttpServletRequest req, HttpServletResponse resp,
                              String mensaje) throws ServletException, IOException {
        req.setAttribute("error", mensaje);
        req.getRequestDispatcher("/WEB-INF/vistas/registro.jsp").forward(req, resp);
    }
}
