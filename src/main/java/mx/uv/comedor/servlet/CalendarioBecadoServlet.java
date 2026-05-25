package mx.uv.comedor.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.uv.comedor.dao.AlumnoBecadoDAO;
import mx.uv.comedor.dao.ApartadoBecadoDAO;
import mx.uv.comedor.dao.EstudianteDAO;
import mx.uv.comedor.model.*;

import java.io.IOException;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

/*
  Servlet del calendario de apartados del becado.

  GET  /becado/calendario          muestra el calendario semanal
  POST /becado/calendario/apartar   aparta una comida (fecha + tipo)
 POST /becado/calendario/cancelar  cancela un apartado
 */
@WebServlet(urlPatterns = {
    "/becado/calendario",
    "/becado/calendario/apartar",
    "/becado/calendario/cancelar"
})
public class CalendarioBecadoServlet extends HttpServlet {

    private final ApartadoBecadoDAO apartadoDAO = new ApartadoBecadoDAO();
    private final AlumnoBecadoDAO   becadoDAO   = new AlumnoBecadoDAO();
    private final EstudianteDAO     estDAO      = new EstudianteDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario.getRol() != RolEnum.BECADO) {
            resp.sendRedirect(req.getContextPath() + "/menu");
            return;
        }

        try {
            // Cargar datos del becado
            Estudiante est = estDAO.buscarPorIdUsuario(usuario.getIdUsuario());
            if (est == null) { resp.sendRedirect(req.getContextPath() + "/menu"); return; }
            AlumnoBecado becado = becadoDAO.buscarPorIdEstudiante(est.getIdEstudiante());
            if (becado == null) { resp.sendRedirect(req.getContextPath() + "/menu"); return; }

            // Calcular semana actual (lunes a viernes)
            LocalDate hoy = LocalDate.now();
            LocalDate lunes = hoy.with(DayOfWeek.MONDAY);
            LocalDate viernes = lunes.plusDays(4);

            // Si pasan ?semana=1 mostrar la siguiente semana
            String semanaParam = req.getParameter("semana");
            int offset = 0;
            if (semanaParam != null) {
                try { offset = Integer.parseInt(semanaParam); }
                catch (NumberFormatException e) { offset = 0; }
            }
            lunes   = lunes.plusWeeks(offset);
            viernes = viernes.plusWeeks(offset);

            // Cargar apartados existentes de esta semana
            List<ApartadoBecado> apartados =
                apartadoDAO.listarPorSemana(becado.getIdBecado(), lunes, viernes);

            int totalApartados = apartadoDAO.contarApartadosSemana(
                becado.getIdBecado(), lunes, viernes);

            req.setAttribute("becado",         becado);
            req.setAttribute("apartados",      apartados);
            req.setAttribute("lunes",          lunes);
            req.setAttribute("viernes",        viernes);
            req.setAttribute("offset",         offset);
            req.setAttribute("totalApartados", totalApartados);
            req.setAttribute("hoy",            hoy);

            req.getRequestDispatcher("/WEB-INF/vistas/becado/calendario.jsp")
               .forward(req, resp);

        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "Error al cargar el calendario: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/vistas/error.jsp").forward(req, resp);
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
        if (usuario.getRol() != RolEnum.BECADO) {
            resp.sendError(403);
            return;
        }

        try {
            Estudiante est = estDAO.buscarPorIdUsuario(usuario.getIdUsuario());
            AlumnoBecado becado = becadoDAO.buscarPorIdEstudiante(est.getIdEstudiante());
            String path = req.getServletPath();

            if ("/becado/calendario/apartar".equals(path)) {
                LocalDate fecha = LocalDate.parse(req.getParameter("fecha"));
                String tipo = req.getParameter("tipoComida");

                // Validar que la fecha no sea pasada
                if (fecha.isBefore(LocalDate.now())) {
                    resp.sendRedirect(req.getContextPath()
                        + "/becado/calendario?error=No puedes apartar fechas pasadas");
                    return;
                }

                // Validar cupo semanal disponible
                LocalDate lunesDeEsa = fecha.with(DayOfWeek.MONDAY);
                LocalDate viernesDeEsa = lunesDeEsa.plusDays(4);
                int yaApartados = apartadoDAO.contarApartadosSemana(
                    becado.getIdBecado(), lunesDeEsa, viernesDeEsa);

                if (yaApartados >= becado.getComidasDisponiblesSemana()) {
                    resp.sendRedirect(req.getContextPath()
                        + "/becado/calendario?error=Has alcanzado tu limite de comidas para esa semana");
                    return;
                }

                apartadoDAO.apartar(becado.getIdBecado(), fecha, tipo);
                resp.sendRedirect(req.getContextPath()
                    + "/becado/calendario?exito=Comida apartada correctamente");

            } else if ("/becado/calendario/cancelar".equals(path)) {
                Long idApartado = Long.parseLong(req.getParameter("idApartado"));
                apartadoDAO.cancelar(idApartado, becado.getIdBecado());
                resp.sendRedirect(req.getContextPath()
                    + "/becado/calendario?exito=Apartado cancelado");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath()
                + "/becado/calendario?error=Esa comida ya estaba apartada");
        } catch (IllegalArgumentException e) {
            resp.sendRedirect(req.getContextPath()
                + "/becado/calendario?error=Datos invalidos");
        }
    }
}
