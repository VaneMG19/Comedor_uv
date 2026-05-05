package mx.uv.comedor.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.uv.comedor.dao.PedidoDAO;
import mx.uv.comedor.model.DetallePedido;
import mx.uv.comedor.model.MetodoPagoEnum;
import mx.uv.comedor.model.Pedido;
import mx.uv.comedor.model.ProgramacionPedido;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servlet para crear y consultar pedidos.
 *
 * GET  /pedido/nuevo         → formulario de pedido
 * POST /pedido/crear         → crea el pedido (inmediato o anticipado)
 * GET  /pedido/detalle?id=X  → detalle de un pedido
 * GET  /pedido/historial     → historial del usuario en sesión
 * POST /pedido/cancelar      → cancela un pedido
 */
@WebServlet(urlPatterns = {
    "/pedido/nuevo",
    "/pedido/crear",
    "/pedido/detalle",
    "/pedido/historial",
    "/pedido/cancelar"
})
public class PedidoServlet extends HttpServlet {

    private final PedidoDAO      pedidoDAO   = new PedidoDAO();
    private final PlatilloDAO    platilloDAO = new PlatilloDAO();
    private final AlumnoBecadoDAO becadoDAO  = new AlumnoBecadoDAO();
    private final EstudianteDAO   estDAO     = new EstudianteDAO();

    // ── GET ───────────────────────────────────────────────────────

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!estaAutenticado(req, resp)) return;

        try {
            switch (req.getServletPath()) {
                case "/pedido/nuevo"    -> mostrarFormulario(req, resp);
                case "/pedido/detalle"  -> mostrarDetalle(req, resp);
                case "/pedido/historial"-> mostrarHistorial(req, resp);
                default -> resp.sendRedirect(req.getContextPath() + "/menu");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "Error al cargar pedido: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/vistas/error.jsp").forward(req, resp);
        }
    }

    // ── POST ──────────────────────────────────────────────────────

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!estaAutenticado(req, resp)) return;

        try {
            switch (req.getServletPath()) {
                case "/pedido/crear"   -> crearPedido(req, resp);
                case "/pedido/cancelar"-> cancelarPedido(req, resp);
                default -> resp.sendRedirect(req.getContextPath() + "/menu");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "Error al procesar pedido: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/vistas/error.jsp").forward(req, resp);
        }
    }

    // ── Acciones GET ──────────────────────────────────────────────

    private void mostrarFormulario(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {

        // Los platillos ya están en sesión desde MenuServlet
        // Solo agregamos la bandera de si puede usar beca
        HttpSession session = req.getSession(false);
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario.getRol() == RolEnum.BECADO) {
            AlumnoBecado becado = (AlumnoBecado) session.getAttribute("becado");
            req.setAttribute("puedeUsarBeca",
                becado != null && becado.puedeUsarBeca());
        }

        req.getRequestDispatcher("/WEB-INF/vistas/pedido-nuevo.jsp")
           .forward(req, resp);
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
        req.getRequestDispatcher("/WEB-INF/vistas/pedido-detalle.jsp")
           .forward(req, resp);
    }

    private void mostrarHistorial(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {

        HttpSession session = req.getSession(false);
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        List<Pedido> historial = pedidoDAO.listarPorUsuario(usuario.getIdUsuario());
        req.setAttribute("historial", historial);
        req.getRequestDispatcher("/WEB-INF/vistas/pedido-historial.jsp")
           .forward(req, resp);
    }

    // ── Acción POST: crear pedido ─────────────────────────────────

    private void crearPedido(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, IOException, ServletException {

        HttpSession session  = req.getSession(false);
        Usuario     usuario  = (Usuario) session.getAttribute("usuario");

        // 1. Tipo de pedido
        String tipoStr     = req.getParameter("tipoPedido");
        TipoPedidoEnum tipo = TipoPedidoEnum.valueOf(
            tipoStr != null ? tipoStr : "INMEDIATO");

        // 2. Método de pago
        String metodoStr        = req.getParameter("metodoPago");
        MetodoPagoEnum metodo   = MetodoPagoEnum.valueOf(metodoStr);

        // 3. Construir detalles desde el formulario
        // El form envía: platilloId[]=1&platilloId[]=5&cantidad[]=1&cantidad[]=2
        String[] idsPlat    = req.getParameterValues("platilloId");
        String[] cantidades = req.getParameterValues("cantidad");

        if (idsPlat == null || idsPlat.length == 0) {
            req.setAttribute("error", "Debes seleccionar al menos un platillo");
            req.getRequestDispatcher("/WEB-INF/vistas/pedido-nuevo.jsp")
               .forward(req, resp);
            return;
        }

        // 4. Obtener becado si aplica
        AlumnoBecado becado = null;
        if (usuario.getRol() == RolEnum.BECADO) {
            becado = (AlumnoBecado) session.getAttribute("becado");
            if (becado == null) {
                Estudiante est = estDAO.buscarPorIdUsuario(usuario.getIdUsuario());
                if (est != null) {
                    becado = becadoDAO.buscarPorIdEstudiante(est.getIdEstudiante());
                }
            }
        }

        // 5. Construir lista de detalles
        List<DetallePedido> detalles = new ArrayList<>();
        for (int i = 0; i < idsPlat.length; i++) {
            Long   idPlatillo = Long.parseLong(idsPlat[i]);
            int    cantidad   = Integer.parseInt(cantidades[i]);
            Platillo platillo = platilloDAO.buscarPorId(idPlatillo);

            if (platillo == null || !platillo.isDisponible()) continue;

            BigDecimal precio = platillo.calcularPrecioFinal(usuario.getRol());
            DetallePedido d   = new DetallePedido(idPlatillo, cantidad, precio, false);
            d.setPlatillo(platillo);
            detalles.add(d);
        }

        // 6. Construir pedido
        Pedido pedido = new Pedido(usuario.getIdUsuario(), tipo);
        pedido.setDetalles(detalles);
        pedido.setNotas(req.getParameter("notas"));

        // 7. Si es ANTICIPADO, agregar programación
        if (tipo == TipoPedidoEnum.ANTICIPADO) {
            String fechaStr = req.getParameter("fechaRecogida");
            String horaStr  = req.getParameter("horaRecogida");
            String lugar    = req.getParameter("lugarRecogida");

            if (fechaStr == null || horaStr == null) {
                req.setAttribute("error",
                    "Para pedido anticipado debes indicar fecha y hora de recogida");
                req.getRequestDispatcher("/WEB-INF/vistas/pedido-nuevo.jsp")
                   .forward(req, resp);
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

        // 8. Crear pedido completo en BD (transacción)
        Pedido pedidoCreado = pedidoDAO.crearPedidoCompleto(pedido, metodo, becado);

        // 9. Actualizar sesión del becado si usó beca
        if (becado != null && pedido.getDescuentoBeca()
                                    .compareTo(BigDecimal.ZERO) > 0) {
            becado.registrarUso();
            session.setAttribute("becado", becado);
        }

        // 10. Redirigir a detalle del pedido creado
        resp.sendRedirect(req.getContextPath() +
            "/pedido/detalle?id=" + pedidoCreado.getIdPedido() +
            "&exito=creado");
    }

    // ── Acción POST: cancelar ─────────────────────────────────────

    private void cancelarPedido(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, IOException {

        Long idPedido = Long.parseLong(req.getParameter("idPedido"));
        pedidoDAO.cancelar(idPedido);
        resp.sendRedirect(req.getContextPath() +
            "/pedido/historial?exito=cancelado");
    }

    // ── Seguridad ─────────────────────────────────────────────────

    private boolean estaAutenticado(HttpServletRequest req,
                                     HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return false;
        }
        return true;
    }
}
