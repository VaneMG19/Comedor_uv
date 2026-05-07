package mx.uv.comedor.servlet;

import mx.uv.comedor.dao.AlumnoBecadoDAO;
import mx.uv.comedor.dao.EstudianteDAO;
import mx.uv.comedor.dao.PedidoDAO;
import mx.uv.comedor.dao.PlatilloDAO;
import mx.uv.comedor.model.AlumnoBecado;
import mx.uv.comedor.model.DetallePedido;
import mx.uv.comedor.model.Estudiante;
import mx.uv.comedor.model.MetodoPagoEnum;
import mx.uv.comedor.model.Pedido;
import mx.uv.comedor.model.Platillo;
import mx.uv.comedor.model.ProgramacionPedido;
import mx.uv.comedor.model.RolEnum;
import mx.uv.comedor.model.TipoPedidoEnum;
import mx.uv.comedor.model.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = {
        "/pedido/nuevo",
        "/pedido/crear",
        "/pedido/detalle",
        "/pedido/historial",
        "/pedido/cancelar"
})
public class PedidoServlet extends HttpServlet {

    private final PedidoDAO       pedidoDAO  = new PedidoDAO();
    private final PlatilloDAO     platilloDAO = new PlatilloDAO();
    private final AlumnoBecadoDAO becadoDAO  = new AlumnoBecadoDAO();
    private final EstudianteDAO   estDAO     = new EstudianteDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!estaAutenticado(req, resp)) return;
        try {
            switch (req.getServletPath()) {
                case "/pedido/nuevo":    mostrarFormulario(req, resp); break;
                case "/pedido/detalle":  mostrarDetalle(req, resp);    break;
                case "/pedido/historial":mostrarHistorial(req, resp);  break;
                default: resp.sendRedirect(req.getContextPath() + "/menu");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "Error al cargar pedido: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/vistas/error.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!estaAutenticado(req, resp)) return;
        try {
            switch (req.getServletPath()) {
                case "/pedido/crear":   crearPedido(req, resp);   break;
                case "/pedido/cancelar":cancelarPedido(req, resp);break;
                default: resp.sendRedirect(req.getContextPath() + "/menu");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "Error al procesar pedido: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/vistas/error.jsp").forward(req, resp);
        }
    }

    private void mostrarFormulario(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        HttpSession session = req.getSession(false);
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario.getRol() == RolEnum.BECADO) {
            AlumnoBecado becado = (AlumnoBecado) session.getAttribute("becado");
            req.setAttribute("puedeUsarBeca", becado != null && becado.puedeUsarBeca());
        }
        req.getRequestDispatcher("/WEB-INF/vistas/pedido-nuevo.jsp").forward(req, resp);
    }

    private void mostrarDetalle(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.sendRedirect(req.getContextPath() + "/pedido/historial");
            return;
        }
        Pedido pedido = pedidoDAO.buscarPorId(Long.parseLong(idParam));
        req.setAttribute("pedido", pedido);
        req.getRequestDispatcher("/WEB-INF/vistas/pedido-detalle.jsp").forward(req, resp);
    }

    private void mostrarHistorial(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        HttpSession session = req.getSession(false);
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        List<Pedido> historial = pedidoDAO.listarPorUsuario(usuario.getIdUsuario());
        req.setAttribute("historial", historial);
        req.getRequestDispatcher("/WEB-INF/vistas/pedido-historial.jsp").forward(req, resp);
    }

    private void crearPedido(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, IOException, ServletException {
        HttpSession session = req.getSession(false);
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        String tipoStr = req.getParameter("tipoPedido");
        TipoPedidoEnum tipo = TipoPedidoEnum.valueOf(tipoStr != null ? tipoStr : "INMEDIATO");
        MetodoPagoEnum metodo = MetodoPagoEnum.valueOf(req.getParameter("metodoPago"));

        String[] idsPlat    = req.getParameterValues("platilloId");
        String[] cantidades = req.getParameterValues("cantidad");

        if (idsPlat == null || idsPlat.length == 0) {
            req.setAttribute("error", "Debes seleccionar al menos un platillo");
            req.getRequestDispatcher("/WEB-INF/vistas/pedido-nuevo.jsp").forward(req, resp);
            return;
        }

        AlumnoBecado becado = null;
        if (usuario.getRol() == RolEnum.BECADO) {
            becado = (AlumnoBecado) session.getAttribute("becado");
            if (becado == null) {
                Estudiante est = estDAO.buscarPorIdUsuario(usuario.getIdUsuario());
                if (est != null) becado = becadoDAO.buscarPorIdEstudiante(est.getIdEstudiante());
            }
        }

        List<DetallePedido> detalles = new ArrayList<>();
        for (int i = 0; i < idsPlat.length; i++) {
            Long idPlatillo = Long.parseLong(idsPlat[i]);
            int  cantidad   = Integer.parseInt(cantidades[i]);
            Platillo platillo = platilloDAO.buscarPorId(idPlatillo);
            if (platillo == null || !platillo.isDisponible()) continue;
            BigDecimal precio = platillo.calcularPrecioFinal(usuario.getRol());
            DetallePedido d = new DetallePedido(idPlatillo, cantidad, precio, false);
            d.setPlatillo(platillo);
            detalles.add(d);
        }

        Pedido pedido = new Pedido(usuario.getIdUsuario(), tipo);
        pedido.setDetalles(detalles);
        pedido.setNotas(req.getParameter("notas"));

        if (tipo == TipoPedidoEnum.ANTICIPADO) {
            String fechaStr = req.getParameter("fechaRecogida");
            String horaStr  = req.getParameter("horaRecogida");
            String lugar    = req.getParameter("lugarRecogida");
            if (fechaStr == null || horaStr == null) {
                req.setAttribute("error", "Para pedido anticipado debes indicar fecha y hora");
                req.getRequestDispatcher("/WEB-INF/vistas/pedido-nuevo.jsp").forward(req, resp);
                return;
            }
            ProgramacionPedido prog = new ProgramacionPedido(
                    null,
                    LocalDate.parse(fechaStr),
                    LocalTime.parse(horaStr),
                    lugar != null ? lugar : "Ventanilla principal"
            );
            pedido.setProgramacion(prog);
        }

        Pedido pedidoCreado = pedidoDAO.crearPedidoCompleto(pedido, metodo, becado);

        if (becado != null && pedido.getDescuentoBeca().compareTo(BigDecimal.ZERO) > 0) {
            becado.registrarUso();
            session.setAttribute("becado", becado);
        }

        resp.sendRedirect(req.getContextPath() +
                "/pedido/detalle?id=" + pedidoCreado.getIdPedido() + "&exito=creado");
    }

    private void cancelarPedido(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, IOException {
        Long idPedido = Long.parseLong(req.getParameter("idPedido"));
        pedidoDAO.cancelar(idPedido);
        resp.sendRedirect(req.getContextPath() + "/pedido/historial?exito=cancelado");
    }

    private boolean estaAutenticado(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return false;
        }
        return true;
    }
}