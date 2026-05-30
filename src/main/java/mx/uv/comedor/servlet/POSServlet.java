package mx.uv.comedor.servlet;

import mx.uv.comedor.dao.*;
import mx.uv.comedor.model.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
  Punto de Venta para el empleado del comedor.
  Al cobrar, el pedido se crea como PENDIENTE (igual que un pedido online).
  El empleado de cocina lo prepara y luego se entrega. El inventario se
  descuenta en EmpleadoServlet cuando se marca como ENTREGADO.

  GET  /pos                 vista del POS
  POST /pos/buscar-usuario  busca un usuario por email/matricula (AJAX)
  POST /pos/cobrar          registra la venta
 */
@WebServlet(urlPatterns = { "/pos", "/pos/buscar-usuario", "/pos/cobrar" })
public class POSServlet extends HttpServlet {

    private final PlatilloDAO       platilloDAO   = new PlatilloDAO();
    private final UsuarioDAO        usuarioDAO    = new UsuarioDAO();
    private final EstudianteDAO     estDAO        = new EstudianteDAO();
    private final AlumnoBecadoDAO   becadoDAO     = new AlumnoBecadoDAO();
    private final PedidoDAO         pedidoDAO     = new PedidoDAO();
    private final RecetaDAO         recetaDAO     = new RecetaDAO();
    private final MenuSemanalDAO menuDAO = new MenuSemanalDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario.getRol() != RolEnum.EMPLEADO && usuario.getRol() != RolEnum.ADMIN) {
            resp.sendRedirect(req.getContextPath() + "/menu");
            return;
        }

        try {
            // MENU DEL DIA: solo los platillos del dia actual (desayuno + comida)
            List<Platillo> menuDia = new ArrayList<>();
            menuDia.addAll(platilloDAO.listarPorMenuActivoYCategoria(CatMenuEnum.DESAYUNO));
            menuDia.addAll(platilloDAO.listarPorMenuActivoYCategoria(CatMenuEnum.COMIDA));

            // A LA CARTA: todos los platillos tipo carta disponibles
            List<Platillo> aLaCarta = platilloDAO.listarCarta();

            req.setAttribute("menuDia", menuDia);
            req.setAttribute("aLaCarta", aLaCarta);
            req.getRequestDispatcher("/WEB-INF/vistas/empleado/pos.jsp")
                    .forward(req, resp);

        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "Error al cargar platillos");
            req.getRequestDispatcher("/WEB-INF/vistas/error.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.sendError(401); return;
        }
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario.getRol() != RolEnum.EMPLEADO && usuario.getRol() != RolEnum.ADMIN) {
            resp.sendError(403); return;
        }

        String path = req.getServletPath();
        if ("/pos/buscar-usuario".equals(path)) {
            buscarUsuario(req, resp);
        } else if ("/pos/cobrar".equals(path)) {
            cobrar(req, resp, usuario);
        }
    }

    /** Busca un usuario por email o matrícula. Responde JSON. */
    private void buscarUsuario(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String texto = req.getParameter("q");
        if (texto == null || texto.isBlank()) {
            responderJSON(resp, "{\"found\":false}");
            return;
        }
        // NO convertir a lowercase para matricula; el DAO ya hace LOWER() en ambos lados.
        // Para email, lowercase es seguro (los emails se guardan en lowercase).
        texto = texto.trim();

        try {
            Usuario u = null;
            if (texto.contains("@")) {
                u = usuarioDAO.buscarPorEmail(texto.toLowerCase());
            } else {
                Estudiante est = estDAO.buscarPorMatricula(texto);
                if (est != null) {
                    u = usuarioDAO.buscarPorId(est.getIdUsuario());
                }
            }

            if (u == null) {
                responderJSON(resp, "{\"found\":false}");
                return;
            }

            boolean esBecado = u.getRol() == RolEnum.BECADO;
            int comidasBeca = 0;
            String tipoBeca = "";
            if (esBecado) {
                Estudiante est = estDAO.buscarPorIdUsuario(u.getIdUsuario());
                if (est != null) {
                    AlumnoBecado b = becadoDAO.buscarPorIdEstudiante(est.getIdEstudiante());
                    if (b != null && b.esBecaVigente()) {
                        comidasBeca = b.getComidasRestantesSemana();
                        tipoBeca = b.getTipoBeca();
                    }
                }
            }

            String json = "{"
                    + "\"found\":true,"
                    + "\"idUsuario\":" + u.getIdUsuario() + ","
                    + "\"nombre\":\"" + esc(u.getNombreCompleto()) + "\","
                    + "\"email\":\"" + esc(u.getEmail()) + "\","
                    + "\"rol\":\"" + u.getRol().name() + "\","
                    + "\"esBecado\":" + esBecado + ","
                    + "\"comidasBeca\":" + comidasBeca + ","
                    + "\"tipoBeca\":\"" + esc(tipoBeca) + "\""
                    + "}";
            responderJSON(resp, json);

        } catch (SQLException e) {
            e.printStackTrace();
            responderJSON(resp, "{\"found\":false,\"error\":\"BD\"}");
        }
    }

    /**
     * Procesa la venta. Crea un Pedido tipo INMEDIATO con estado PENDIENTE
     * para que aparezca en Pedidos Activos y el empleado de cocina lo
     * prepare. El descuento de inventario ocurrira cuando se marque como
     * ENTREGADO en el panel del empleado.
     */
    private void cobrar(HttpServletRequest req, HttpServletResponse resp, Usuario empleado)
            throws IOException {

        try {
            String idUsuarioStr = req.getParameter("idUsuario");
            String metodoPago   = req.getParameter("metodoPago");
            String[] platillos  = req.getParameterValues("platilloId");
            String[] cantidades = req.getParameterValues("cantidad");

            if (platillos == null || platillos.length == 0) {
                resp.sendRedirect(req.getContextPath() + "/pos?error=Carrito vacio");
                return;
            }

            Long idUsuarioCompra = null;
            AlumnoBecado becado = null;
            if (idUsuarioStr != null && !idUsuarioStr.isBlank()) {
                idUsuarioCompra = Long.parseLong(idUsuarioStr);
                Usuario u = usuarioDAO.buscarPorId(idUsuarioCompra);
                if (u != null && u.getRol() == RolEnum.BECADO) {
                    Estudiante est = estDAO.buscarPorIdUsuario(u.getIdUsuario());
                    if (est != null) {
                        becado = becadoDAO.buscarPorIdEstudiante(est.getIdEstudiante());
                    }
                }
            } else {
                idUsuarioCompra = empleado.getIdUsuario();
            }

            // 1) Construir mapa idPlatillo -> cantidad
            Map<Long, Integer> platillosCantidad = new HashMap<>();
            for (int i = 0; i < platillos.length; i++) {
                Long idPlat = Long.parseLong(platillos[i]);
                int cant = 1;
                if (cantidades != null && i < cantidades.length) {
                    try { cant = Integer.parseInt(cantidades[i]); } catch (Exception e) {}
                }
                platillosCantidad.merge(idPlat, cant, Integer::sum);
            }

            // 2) Verificar stock ANTES de crear el pedido
            List<String> faltantes = recetaDAO.verificarStockSuficiente(platillosCantidad);
            if (!faltantes.isEmpty()) {
                resp.sendRedirect(req.getContextPath()
                        + "/pos?error=Sin stock: " + String.join("; ", faltantes));
                return;
            }

            // 3) Construir el pedido (queda como PENDIENTE por defecto)
            Pedido pedido = new Pedido(idUsuarioCompra, TipoPedidoEnum.INMEDIATO);

            for (int i = 0; i < platillos.length; i++) {
                Long idPlat = Long.parseLong(platillos[i]);
                int cant = 1;
                if (cantidades != null && i < cantidades.length) {
                    try { cant = Integer.parseInt(cantidades[i]); } catch (Exception e) {}
                }
                Platillo p = platilloDAO.buscarPorId(idPlat);
                if (p == null) continue;

                RolEnum rolCompra = becado != null ? RolEnum.BECADO
                        : (idUsuarioStr != null && !idUsuarioStr.isBlank()
                        ? usuarioDAO.buscarPorId(idUsuarioCompra).getRol()
                        : RolEnum.EMPLEADO);
                BigDecimal precio = p.calcularPrecioFinal(rolCompra);

                DetallePedido d = new DetallePedido(idPlat, cant, precio, false);
                pedido.agregarDetalle(d);
            }

            MetodoPagoEnum mp = MetodoPagoEnum.valueOf(metodoPago != null ? metodoPago : "EFECTIVO");

            // Verificar y descontar cupo del menu del dia
            DiaEnum diaHoy = DiaEnum.desdeDayOfWeek(java.time.LocalDate.now().getDayOfWeek());

            for (DetallePedido det : pedido.getDetalles()) {
                Platillo p = platilloDAO.buscarPorId(det.getIdPlatillo());
                if (p == null) continue;
                if (p.getTipo() == TipoPlatEnum.MENU) {
                    boolean ok = menuDAO.incrementarVendidos(
                            det.getIdPlatillo(), diaHoy, CatMenuEnum.DESAYUNO, det.getCantidad());
                    if (!ok) {
                        ok = menuDAO.incrementarVendidos(
                                det.getIdPlatillo(), diaHoy, CatMenuEnum.COMIDA, det.getCantidad());
                    }
                    if (!ok) {
                        resp.sendRedirect(req.getContextPath()
                                + "/pos?error=El platillo \"" + p.getNombre()
                                + "\" se agoto");
                        return;
                    }
                }
            }


            // 4) Crear el pedido. El estado queda PENDIENTE (default del modelo).
            // No descontamos inventario aqui - se descuenta cuando el empleado lo
            // marca como ENTREGADO desde su panel.
            pedidoDAO.crearPedidoCompleto(pedido, mp, becado);

            // 5) Redirigir al ticket
            resp.sendRedirect(req.getContextPath()
                    + "/pos/ticket?id=" + pedido.getIdPedido());

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/pos?error=Error: " + e.getMessage());
        }
    }

    private void responderJSON(HttpServletResponse resp, String json) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(json);
        resp.getWriter().flush();
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}