<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="mx.uv.comedor.model.*" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%
 Usuario usuario = (Usuario) session.getAttribute("usuario");
 if (usuario == null || usuario.getRol() != RolEnum.ADMIN) {
  response.sendRedirect(request.getContextPath() + "/login");
  return;
 }
 List<MovimientoInventario> movimientos = (List<MovimientoInventario>) request.getAttribute("movimientos");
 if (movimientos == null) movimientos = new java.util.ArrayList<>();
 Ingrediente ingrediente = (Ingrediente) request.getAttribute("ingrediente");

 DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
%>
<!DOCTYPE html>
<html lang="es">
<head>
 <meta charset="UTF-8">
 <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
 <meta name="viewport" content="width=device-width,initial-scale=1.0">
 <title>Movimientos de Inventario - Comedor UV</title>
 <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
 <style>
  .inv-tabs {
   display: flex;
   background: white;
   border-radius: 12px;
   padding: 5px;
   gap: 4px;
   box-shadow: var(--sombra-sm);
   margin-bottom: 18px;
  }
  .inv-tab {
   padding: 10px 18px;
   border-radius: 8px;
   font-family: var(--fuente-display);
   font-weight: 600;
   font-size: .9rem;
   color: var(--uv-gris-700);
   text-decoration: none;
   transition: all .15s;
  }
  .inv-tab:hover { background: var(--uv-gris-100); }
  .inv-tab.activa { background: var(--uv-azul); color: white; }

  .tipo-pill {
   display: inline-block;
   font-size: .7rem;
   font-weight: 700;
   padding: 4px 10px;
   border-radius: 12px;
   letter-spacing: .3px;
  }
  .tipo-ENTRADA { background: #d1fae5; color: #065f46; }
  .tipo-SALIDA  { background: #fee2e2; color: #991b1b; }
  .tipo-MERMA   { background: #fef3c7; color: #92400e; }
  .tipo-AJUSTE  { background: #ddd6fe; color: #5b21b6; }
 </style>
</head>
<body data-context-path="${pageContext.request.contextPath}">

<%@ include file="../_header.jsp" %>

<main class="page-wrapper">

 <div class="page-header d-flex justify-between align-center flex-wrap gap-2">
  <div>
   <div class="page-title">
    <% if (ingrediente != null) { %>
    Movimientos de <%= ingrediente.getNombre() %>
    <% } else { %>
    Historial de Movimientos
    <% } %>
   </div>
   <div class="page-subtitle">
    <% if (ingrediente != null) { %>
    Stock actual: <strong><%= ingrediente.getStockActual().toPlainString() %> <%= ingrediente.getUnidadMedida() %></strong>
    - <%= movimientos.size() %> movimiento<%= movimientos.size() == 1 ? "" : "s" %>
    <% } else { %>
    Ultimos <%= movimientos.size() %> movimientos registrados
    <% } %>
   </div>
  </div>
  <a href="${pageContext.request.contextPath}/admin/inventario" class="btn btn-ghost">
   Volver a inventario
  </a>
 </div>

 <!-- Tabs (solo 2) -->
 <div class="inv-tabs">
  <a href="${pageContext.request.contextPath}/admin/inventario" class="inv-tab">
   Ingredientes
  </a>
  <a href="${pageContext.request.contextPath}/admin/inventario/movimientos" class="inv-tab activa">
   Movimientos
  </a>
 </div>

 <!-- Tabla -->
 <div class="card">
  <div style="overflow-x:auto;">
   <table class="tabla">
    <thead>
    <tr>
     <th>Fecha</th>
     <th>Tipo</th>
     <% if (ingrediente == null) { %>
     <th>Ingrediente</th>
     <% } %>
     <th>Cantidad</th>
     <th>Stock resultante</th>
     <th>Motivo</th>
    </tr>
    </thead>
    <tbody>
    <% if (movimientos.isEmpty()) { %>
    <tr>
     <td colspan="<%= ingrediente == null ? 6 : 5 %>"
         style="text-align:center;color:var(--uv-gris-500);padding:40px;">
      <div style="font-weight:600;margin-bottom:4px;">Sin movimientos registrados</div>
      <div style="font-size:.85rem;">
       Los movimientos apareceran aqui cuando registres entradas/salidas
       o cuando se vendan platillos con receta.
      </div>
     </td>
    </tr>
    <% } %>
    <% for (MovimientoInventario mov : movimientos) {
     String tipo = mov.getTipo() != null ? mov.getTipo().name() : "AJUSTE";
    %>
    <tr>
     <td style="font-size:.85rem;color:var(--uv-gris-700);">
      <%= mov.getFecha() != null ? mov.getFecha().format(fmt) : "-" %>
     </td>
     <td>
      <span class="tipo-pill tipo-<%= tipo %>"><%= tipo %></span>
     </td>
     <% if (ingrediente == null) { %>
     <td style="font-weight:600;">
      Ingrediente #<%= mov.getIdIngrediente() %>
     </td>
     <% } %>
     <td style="font-weight:700;
             color:<%= tipo.equals("ENTRADA") ? "var(--uv-verde)" :
                                              tipo.equals("SALIDA") || tipo.equals("MERMA") ? "var(--uv-rojo)" :
                                              "var(--uv-gris-700)" %>;">
      <%= tipo.equals("ENTRADA") ? "+" : tipo.equals("SALIDA") || tipo.equals("MERMA") ? "-" : "" %><%= mov.getCantidad().toPlainString() %>
     </td>
     <td>
      <%= mov.getStockResultante() != null ? mov.getStockResultante().toPlainString() : "-" %>
     </td>
     <td style="font-size:.85rem;color:var(--uv-gris-700);">
      <%= mov.getMotivo() != null && !mov.getMotivo().isEmpty() ? mov.getMotivo() : "-" %>
     </td>
    </tr>
    <% } %>
    </tbody>
   </table>
  </div>
 </div>

</main>

<%@ include file="../_footer.jsp" %>
</body>
</html>
