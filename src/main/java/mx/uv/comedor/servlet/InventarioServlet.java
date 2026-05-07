package mx.uv.comedor.servlet;

import mx.uv.comedor.dao.AdministradorDAO;
import mx.uv.comedor.dao.InventarioDAO;
import mx.uv.comedor.model.Administrador;
import mx.uv.comedor.model.AlertaInventario;
import mx.uv.comedor.model.CompraAnticipada;
import mx.uv.comedor.model.DetalleCompra;
import mx.uv.comedor.model.EstCompraEnum;
import mx.uv.comedor.model.Ingrediente;
import mx.uv.comedor.model.MovimientoInventario;
import mx.uv.comedor.model.NivelAlertaEnum;
import mx.uv.comedor.model.RolEnum;
import mx.uv.comedor.model.TipoMovInvEnum;
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
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = {
        "/admin/inventario",
        "/admin/inventario/alertas",
        "/admin/inventario/compras",
        "/admin/inventario/movimientos",
        "/admin/inventario/ingrediente/crear",
        "/admin/inventario/movimiento/crear",
        "/admin/inventario/compra/crear",
        "/admin/inventario/compra/enviar",
        "/admin/inventario/compra/recepcionar",
        "/admin/inventario/alerta/atender"
})
public class InventarioServlet extends HttpServlet {

    private final InventarioDAO   invDAO  = new InventarioDAO();
    private final AdministradorDAO admDAO = new AdministradorDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!esAdmin(req, resp)) return;
        try {
            switch (req.getServletPath()) {
                case "/admin/inventario":             mostrarInventario(req, resp); break;
                case "/admin/inventario/alertas":     mostrarAlertas(req, resp);    break;
                case "/admin/inventario/compras":     mostrarCompras(req, resp);    break;
                case "/admin/inventario/movimientos": mostrarMovimientos(req, resp);break;
                default: resp.sendRedirect(req.getContextPath() + "/admin/inventario");
            }
        } catch (SQLException e) { manejarError(req, resp, e); }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!esAdmin(req, resp)) return;
        try {
            switch (req.getServletPath()) {
                case "/admin/inventario/ingrediente/crear":  crearIngrediente(req, resp);  break;
                case "/admin/inventario/movimiento/crear":   crearMovimiento(req, resp);   break;
                case "/admin/inventario/compra/crear":       crearCompra(req, resp);       break;
                case "/admin/inventario/compra/enviar":      enviarCompra(req, resp);      break;
                case "/admin/inventario/compra/recepcionar": recepcionarCompra(req, resp); break;
                case "/admin/inventario/alerta/atender":     atenderAlerta(req, resp);     break;
            }
        } catch (SQLException e) { manejarError(req, resp, e); }
    }

    private void mostrarInventario(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        List<Ingrediente> ingredientes = invDAO.listarIngredientes();
        List<AlertaInventario> alertas = invDAO.listarAlertasActivas();
        req.setAttribute("ingredientes", ingredientes);
        req.setAttribute("totalAlertas", alertas.size());
        long criticas = 0;
        for (AlertaInventario a : alertas) {
            if (a.getNivel() == NivelAlertaEnum.CRITICO) criticas++;
        }
        req.setAttribute("alertasCriticas", criticas);
        req.getRequestDispatcher("/WEB-INF/vistas/admin/inventario.jsp").forward(req, resp);
    }

    private void mostrarAlertas(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        List<AlertaInventario> alertas = invDAO.listarAlertasActivas();
        req.setAttribute("alertas", alertas);
        req.getRequestDispatcher("/WEB-INF/vistas/admin/inventario-alertas.jsp").forward(req, resp);
    }

    private void mostrarCompras(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        List<CompraAnticipada> compras = invDAO.listarCompras();
        req.setAttribute("compras", compras);
        req.getRequestDispatcher("/WEB-INF/vistas/admin/inventario-compras.jsp").forward(req, resp);
    }

    private void mostrarMovimientos(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        Long idIngrediente = Long.parseLong(req.getParameter("id"));
        Ingrediente ingrediente = invDAO.buscarIngredientePorId(idIngrediente);
        List<MovimientoInventario> movimientos = invDAO.listarMovimientosPorIngrediente(idIngrediente);
        req.setAttribute("ingrediente", ingrediente);
        req.setAttribute("movimientos", movimientos);
        req.getRequestDispatcher("/WEB-INF/vistas/admin/inventario-movimientos.jsp").forward(req, resp);
    }

    private void crearIngrediente(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, IOException {
        Ingrediente ing = new Ingrediente(
                req.getParameter("nombre"),
                req.getParameter("unidadMedida"),
                new BigDecimal(req.getParameter("stockMinimo")),
                new BigDecimal(req.getParameter("precioUnitario"))
        );
        ing.setDescripcion(req.getParameter("descripcion"));
        ing.setProveedor(req.getParameter("proveedor"));
        ing.setCategoria(req.getParameter("categoria"));
        String maxStr = req.getParameter("stockMaximo");
        if (maxStr != null && !maxStr.isBlank()) {
            ing.setStockMaximo(new BigDecimal(maxStr));
        }
        invDAO.insertarIngrediente(ing);
        resp.sendRedirect(req.getContextPath() + "/admin/inventario?exito=ingredienteCreado");
    }

    private void crearMovimiento(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, IOException {
        HttpSession session = req.getSession(false);
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        MovimientoInventario mov = new MovimientoInventario(
                Long.parseLong(req.getParameter("idIngrediente")),
                usuario.getIdUsuario(),
                TipoMovInvEnum.valueOf(req.getParameter("tipo")),
                new BigDecimal(req.getParameter("cantidad")),
                req.getParameter("motivo")
        );
        invDAO.registrarMovimiento(mov);
        resp.sendRedirect(req.getContextPath() + "/admin/inventario?exito=movimientoRegistrado");
    }

    private void crearCompra(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, IOException {
        HttpSession session = req.getSession(false);
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        Administrador admin = admDAO.buscarPorIdUsuario(usuario.getIdUsuario());
        CompraAnticipada compra = new CompraAnticipada(
                admin.getIdAdmin(),
                req.getParameter("proveedor"),
                LocalDate.parse(req.getParameter("fechaEntregaEsperada"))
        );
        compra.setNotas(req.getParameter("notas"));
        String[] ids       = req.getParameterValues("ingredienteId");
        String[] cantidades = req.getParameterValues("cantidad");
        String[] precios   = req.getParameterValues("precio");
        List<DetalleCompra> detalles = new ArrayList<>();
        if (ids != null) {
            for (int i = 0; i < ids.length; i++) {
                detalles.add(new DetalleCompra(
                        Long.parseLong(ids[i]),
                        new BigDecimal(cantidades[i]),
                        new BigDecimal(precios[i])
                ));
            }
        }
        compra.setDetalles(detalles);
        Long idCompra = invDAO.crearCompra(compra);
        resp.sendRedirect(req.getContextPath() + "/admin/inventario/compras?exito=compraCreada&id=" + idCompra);
    }

    private void enviarCompra(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, IOException {
        Long idCompra = Long.parseLong(req.getParameter("idCompra"));
        invDAO.cambiarEstadoCompra(idCompra, EstCompraEnum.ENVIADA);
        resp.sendRedirect(req.getContextPath() + "/admin/inventario/compras?exito=compraEnviada");
    }

    private void recepcionarCompra(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, IOException {
        Long idCompra = Long.parseLong(req.getParameter("idCompra"));
        String[] idsDetalle    = req.getParameterValues("idDetalle");
        String[] cantRecibidas = req.getParameterValues("cantidadRecibida");
        if (idsDetalle != null) {
            for (int i = 0; i < idsDetalle.length; i++) {
                invDAO.registrarCantidadRecibida(
                        Long.parseLong(idsDetalle[i]),
                        new BigDecimal(cantRecibidas[i])
                );
            }
        }
        HttpSession session = req.getSession(false);
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        invDAO.recepcionarCompra(idCompra, usuario.getIdUsuario());
        resp.sendRedirect(req.getContextPath() + "/admin/inventario/compras?exito=compraRecepcionada");
    }

    private void atenderAlerta(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, IOException {
        Long idAlerta = Long.parseLong(req.getParameter("idAlerta"));
        invDAO.atenderAlerta(idAlerta);
        resp.sendRedirect(req.getContextPath() + "/admin/inventario/alertas?exito=alertaAtendida");
    }

    private boolean esAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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

    private void manejarError(HttpServletRequest req, HttpServletResponse resp, Exception e)
            throws ServletException, IOException {
        e.printStackTrace();
        req.setAttribute("error", "Error en inventario: " + e.getMessage());
        req.getRequestDispatcher("/WEB-INF/vistas/error.jsp").forward(req, resp);
    }
}