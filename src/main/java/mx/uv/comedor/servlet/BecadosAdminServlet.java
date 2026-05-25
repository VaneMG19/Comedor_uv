package mx.uv.comedor.servlet;

import mx.uv.comedor.dao.BecadoAutorizadoDAO;
import mx.uv.comedor.model.BecadoAutorizado;
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
import java.time.LocalDate;
import java.util.List;

/*
  Servlet para que el admin gestione la lista de becados autorizados.

  GET  /admin/becados         lista los becados autorizados
  POST /admin/becados/agregar  agrega un nuevo becado autorizado
 */
@WebServlet(urlPatterns = {
    "/admin/becados",
    "/admin/becados/agregar"
})
public class BecadosAdminServlet extends HttpServlet {

    private final BecadoAutorizadoDAO becadoAutDAO = new BecadoAutorizadoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario.getRol() != RolEnum.ADMIN) {
            resp.sendRedirect(req.getContextPath() + "/menu");
            return;
        }

        try {
            List<BecadoAutorizado> becados = becadoAutDAO.listarTodos();
            req.setAttribute("becados", becados);
            req.getRequestDispatcher("/WEB-INF/vistas/admin/becados-autorizados.jsp")
               .forward(req, resp);
        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "Error al cargar becados");
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
        if (usuario.getRol() != RolEnum.ADMIN) { resp.sendError(403); return; }

        try {
            BecadoAutorizado b = new BecadoAutorizado();
            b.setEmail(req.getParameter("email").toLowerCase().trim());
            b.setMatricula(req.getParameter("matricula"));
            b.setNombreCompleto(req.getParameter("nombreCompleto"));
            b.setTipoBeca(req.getParameter("tipoBeca"));
            try {
                b.setComidasSemana(Integer.parseInt(req.getParameter("comidasSemana")));
            } catch (NumberFormatException e) { b.setComidasSemana(10); }
            b.setVigenciaDesde(LocalDate.now());
            try {
                b.setVigenciaHasta(LocalDate.parse(req.getParameter("vigenciaHasta")));
            } catch (Exception e) { b.setVigenciaHasta(LocalDate.now().plusYears(1)); }
            b.setNotas(req.getParameter("notas"));

            becadoAutDAO.insertar(b);
            resp.sendRedirect(req.getContextPath()
                + "/admin/becados?exito=Becado agregado correctamente");
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath()
                + "/admin/becados?error=Error: el correo o matricula ya existe");
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath()
                + "/admin/becados?error=Datos invalidos");
        }
    }
}
