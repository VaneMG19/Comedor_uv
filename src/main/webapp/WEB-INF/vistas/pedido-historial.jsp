<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="mx.uv.comedor.model.*" %>
<%@ page import="mx.uv.comedor.dao.*" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    if (usuario == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    PedidoDAO pedidoDAO = new PedidoDAO();
    CalificacionDAO califDAO = new CalificacionDAO();
    List<Pedido>pedidos = pedidoDAO.listarPorUsuario(usuario.getIdUsuario());
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    String exito = request.getParameter("exito");
    String error = request.getParameter("error");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1.0">
    <title>Mis Pedidos - Comedor UV</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body data-context-path="${pageContext.request.contextPath}">

<%@ include file="_header.jsp" %>

<main class="page-wrapper">

    <div class="page-header d-flex justify-between align-center flex-wrap gap-2">
        <div>
            <div class="page-title">Mis Pedidos </div>
            <div class="page-subtitle"><%= pedidos.size() %>pedidos registrados</div>
        </div>
        <a href="${pageContext.request.contextPath}/menu"
           class="btn btn-primario">+ Nuevo pedido</a>
    </div>

    <% if ("calificado".equals(exito)) { %>
    <div class="alert alert-exito" data-auto-close>
        ⭐ ¡Gracias por tu calificación!
    </div>
    <% } %>
    <% if ("cancelado".equals(exito)) { %>
    <div class="alert alert-info" data-auto-close>
        Pedido cancelado correctamente.
    </div>
    <% } %>
    <% if (error!= null) { %>
    <div class="alert alert-error" data-auto-close> <%= error %></div>
    <% } %>

    <% if (pedidos.isEmpty()) { %>
    <div style="text-align:center;padding:80px 20px;">
        <div style="font-size:4rem;margin-bottom:16px;"></div>
        <div style="font-family:var(--fuente-display);font-weight:700;font-size:1.2rem;
 margin-bottom:8px;">Aún no tienes pedidos</div>
        <div style="color:var(--uv-gris-500);margin-bottom:24px;">
            Explora el menú y haz tu primer pedido
        </div>
        <a href="${pageContext.request.contextPath}/menu"
           class="btn btn-primario btn-lg">Ver el menú</a>
    </div>
    <% } else { %>

    <div style="display:flex;flex-direction:column;gap:14px;">
        <% for (Pedido p : pedidos) {
            boolean esAnticipado = p.getTipo() == TipoPedidoEnum.ANTICIPADO;
            boolean puedeCalif  = (p.getEstado() == EstadoPedidoEnum.ENTREGADO
                    || p.getEstado() == EstadoPedidoEnum.LISTO)
                    &&!califDAO.pedidoCalificado(p.getIdPedido());
            boolean yaCalificado  = (p.getEstado() == EstadoPedidoEnum.ENTREGADO
                    || p.getEstado() == EstadoPedidoEnum.LISTO)
                    && califDAO.pedidoCalificado(p.getIdPedido());
            boolean puedeCancelar = p.getEstado() == EstadoPedidoEnum.PENDIENTE;
        %>
        <div class="card" style="overflow:visible;">
            <div style="padding:16px 20px;display:flex;align-items:flex-start;
 justify-content:space-between;flex-wrap:wrap;gap:12px;
 border-bottom:1px solid var(--color-borde);">
                <div>
                    <!-- Folio y fecha -->
                    <div style="display:flex;align-items:center;gap:10px;flex-wrap:wrap;">
  <span style="font-family:var(--fuente-display);font-weight:700;
 font-size:1rem;color:var(--uv-azul);">
  <%= p.getFolio() %>
  </span>
                        <span class="estado-badge estado-<%= p.getEstado().name() %>">
  <%= p.getEstado().name() %>
  </span>
                        <% if (esAnticipado) { %>
                        <span style="font-size:.72rem;background:var(--uv-amarillo-light);
 color:#744210;padding:3px 8px;border-radius:10px;
 font-weight:600;">
 Anticipado
  </span>
                        <% } %>
                    </div>
                    <div style="font-size:.8rem;color:var(--uv-gris-500);margin-top:4px;">
                        <%= p.getFechaCreacion()!= null? p.getFechaCreacion().format(fmt) : "" %>
                        <% if (esAnticipado && p.getProgramacion()!= null) { %>
                        - Recoger: <strong>
                        <%= p.getProgramacion().getFechaRecogida() %>
                        <%= p.getProgramacion().getHoraRecogida() %>
                    </strong>
                        <% } %>
                    </div>
                </div>

                <!-- Total -->
                <div style="text-align:right;">
                    <div style="font-family:var(--fuente-display);font-weight:800;
 font-size:1.2rem;color:var(--uv-gris-900);">
                        $<%= p.getTotal().toPlainString() %>
                    </div>
                    <% if (p.getDescuentoBeca().compareTo(java.math.BigDecimal.ZERO) > 0) { %>
                    <div style="font-size:.75rem;color:var(--uv-verde);">
                        Beca: -$<%= p.getDescuentoBeca().toPlainString() %>
                    </div>
                    <% } %>
                </div>
            </div>

            <!-- Detalles del pedido -->
            <% if (p.getDetalles()!= null &&!p.getDetalles().isEmpty()) { %>
            <div style="padding:12px 20px;border-bottom:1px solid var(--uv-gris-200);">
                <% for (DetallePedido d : p.getDetalles()) {
                    String nombrePl = d.getPlatillo()!= null
                            ? d.getPlatillo().getNombre()
                            : "Platillo #" + d.getIdPlatillo();
                %>
                <div style="display:flex;justify-content:space-between;
 font-size:.875rem;padding:3px 0;">
  <span>
  <span style="font-weight:700;color:var(--uv-azul);">
  <%= d.getCantidad() %>x
  </span>
  <%= nombrePl %>
  <% if (d.isCubiertoPorBeca()) { %>
  <span style="font-size:.7rem;background:var(--uv-verde-light);
 color:var(--uv-verde-dark);padding:1px 6px;
 border-radius:8px;">Beca</span>
  <% } %>
  </span>
                    <span style="color:var(--uv-gris-500);">
  <%= d.isCubiertoPorBeca()? "Gratis"
          : "$" + d.getSubtotal().toPlainString() %>
  </span>
                </div>
                <% if (d.getPersonalizaciones()!= null &&!d.getPersonalizaciones().isBlank()) { %>
                <div style="font-size:.75rem;color:var(--uv-gris-500);
 font-style:italic;margin-left:20px;">
                    <%= d.getPersonalizaciones() %>
                </div>
                <% } %>
                <% } %>
            </div>
            <% } %>

            <!-- Acciones -->
            <div style="padding:12px 20px;display:flex;gap:8px;flex-wrap:wrap;">

                <!-- Ver detalle -->
                <a href="${pageContext.request.contextPath}/pedido/detalle?id=<%= p.getIdPedido() %>"
                   class="btn btn-ghost btn-sm">Ver detalle</a>

                <!-- Descargar PDF -->
                <a href="${pageContext.request.contextPath}/pedido/comprobante?id=<%= p.getIdPedido() %>"
                   class="btn btn-ghost btn-sm">Descargar PDF</a>

                <!-- Calificar (solo pedidos entregados/listos sin calificar) -->
                <% if (puedeCalif) { %>
                <a href="${pageContext.request.contextPath}/calificar?idPedido=<%= p.getIdPedido() %>"
                   class="btn btn-sm"
                   style="background:var(--uv-amarillo-light);color:#744210;">
                    ⭐ Calificar
                </a>
                <% } %>

                <!-- Ya calificado -->
                <% if (yaCalificado) { %>
                <span class="btn btn-sm"
                      style="background:var(--uv-verde-light);color:var(--uv-verde-dark);
 cursor:default;">
 Calificado
  </span>
                <% } %>

                <!-- Cancelar (solo pendientes) -->
                <% if (puedeCancelar) { %>
                <form method="post"
                      action="${pageContext.request.contextPath}/pedido/cancelar"
                      style="display:inline;">
                    <input type="hidden" name="idPedido" value="<%= p.getIdPedido() %>">
                    <button type="submit" class="btn btn-ghost btn-sm"
                            style="color:var(--uv-rojo);"
                            onclick="return confirm('¿Cancelar este pedido?')">
                        Cancelar
                    </button>
                </form>
                <% } %>
            </div>
        </div>
        <% } %>
    </div>
    <% } %>

</main>

<%@ include file="_footer.jsp" %>

</body>
</html>
