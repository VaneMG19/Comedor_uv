package mx.uv.comedor.servlet;

import mx.uv.comedor.dao.InventarioDAO;
import mx.uv.comedor.model.Ingrediente;
import mx.uv.comedor.model.MovimientoInventario;
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
import java.util.List;

/*
  Gestion de inventario (simplificada).

  GET  /admin/inventario                  -> lista de ingredientes
  GET  /admin/inventario/movimientos       -> historial de movimientos
  POST /admin/inventario/ingrediente/crear -> crear ingrediente
  POST /admin/inventario/movimiento/crear  -> registrar movimiento manual
 */
@WebServlet(urlPatterns = {
        "/admin/inventario",
        "/admin/inventario/movimientos",
        "/admin/inventario/ingrediente/crear",
        "/admin/inventario/movimiento/crear"
})
public class InventarioServlet extends HttpServlet {

    private final InventarioDAO invDAO = new InventarioDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!esAdmin(req, resp)) return;
        try {
            switch (req.getServletPath()) {
                case "/admin/inventario":             mostrarInventario(req, resp); break;
                case "/admin/inventario/movimientos": mostrarMovimientos(req, resp); break;
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
                case "/admin/inventario/ingrediente/crear": crearIngrediente(req, resp); break;
                case "/admin/inventario/movimiento/crear":  crearMovimiento(req, resp);  break;
            }
        } catch (SQLException e) { manejarError(req, resp, e); }
    }

    private void mostrarInventario(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        List<Ingrediente> ingredientes = invDAO.listarIngredientes();
        req.setAttribute("ingredientes", ingredientes);
        req.setAttribute("totalAlertas", 0);
        req.getRequestDispatcher("/WEB-INF/vistas/admin/inventario.jsp").forward(req, resp);
    }

    private void mostrarMovimientos(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        String idStr = req.getParameter("id");
        List<MovimientoInventario> movimientos;
        Ingrediente ingrediente = null;

        if (idStr != null && !idStr.isBlank()) {
            // Movimientos de UN ingrediente especifico
            Long idIngrediente = Long.parseLong(idStr);
            ingrediente = invDAO.buscarIngredientePorId(idIngrediente);
            movimientos = invDAO.listarMovimientosPorIngrediente(idIngrediente);
        } else {
            // TODOS los movimientos (vista general)
            movimientos = invDAO.listarTodosMovimientos();
        }

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
        // Si se indica un stock inicial, lo agregamos
        String stockInicialStr = req.getParameter("stockInicial");
        if (stockInicialStr != null && !stockInicialStr.isBlank()) {
            ing.setStockActual(new BigDecimal(stockInicialStr));
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
