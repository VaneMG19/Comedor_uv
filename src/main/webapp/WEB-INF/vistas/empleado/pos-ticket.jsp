<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="mx.uv.comedor.model.*" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%
 Usuario empleado = (Usuario) session.getAttribute("usuario");
 if (empleado == null) { response.sendRedirect(request.getContextPath() + "/login"); return; }
 Pedido pedido = (Pedido) request.getAttribute("pedido");
 Usuario cliente = (Usuario) request.getAttribute("cliente");
 if (pedido == null) { response.sendRedirect(request.getContextPath() + "/pos"); return; }
 DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Ticket <%= pedido.getFolio() %></title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
  <style>
 body { background: var(--uv-gris-100); padding: 20px; }
.ticket-wrapper {
 max-width: 360px;
 margin: 30px auto;
 background: white;
 padding: 28px;
 box-shadow: var(--sombra-md);
 font-family: 'Courier New', monospace;
  }
.ticket-header {
 text-align: center;
 padding-bottom: 16px;
 border-bottom: 2px dashed var(--uv-gris-300);
 margin-bottom: 16px;
  }
.ticket-header h1 {
 font-family: var(--fuente-display);
 color: var(--uv-azul);
 font-size: 1.2rem;
 margin-bottom: 4px;
  }
.ticket-header.sub {
 font-size:.75rem;
 color: var(--uv-gris-700);
  }
.ticket-folio {
 background: var(--uv-azul);
 color: white;
 text-align: center;
 padding: 8px;
 margin: 12px 0;
 font-family: var(--fuente-display);
 font-weight: 800;
 font-size:.95rem;
 border-radius: 4px;
  }
.ticket-info {
 font-size:.8rem;
 line-height: 1.5;
 margin-bottom: 16px;
  }
.ticket-info.row {
 display: flex;
 justify-content: space-between;
  }
.ticket-items {
 border-top: 1px dashed var(--uv-gris-300);
 border-bottom: 1px dashed var(--uv-gris-300);
 padding: 10px 0;
 margin: 10px 0;
  }
.ticket-item {
 display: flex;
 justify-content: space-between;
 font-size:.82rem;
 margin-bottom: 6px;
  }
.ticket-item.qty {
 min-width: 30px;
  }
.ticket-total {
 display: flex;
 justify-content: space-between;
 font-family: var(--fuente-display);
 font-weight: 800;
 font-size: 1.1rem;
 color: var(--uv-azul);
 margin: 14px 0 8px;
 padding-top: 8px;
 border-top: 2px solid var(--uv-azul);
  }
.ticket-footer {
 text-align: center;
 font-size:.72rem;
 color: var(--uv-gris-700);
 padding-top: 14px;
 border-top: 2px dashed var(--uv-gris-300);
 margin-top: 14px;
  }
.acciones-no-print {
 max-width: 360px;
 margin: 16px auto;
 display: flex;
 gap: 10px;
  }
.acciones-no-print.btn { flex: 1; }
  @media print {
 body { background: white; padding: 0; }
.ticket-wrapper { box-shadow: none; margin: 0; padding: 14px; }
.acciones-no-print { display: none!important; }
  }
  </style>
</head>
<body>

<div class="acciones-no-print">
  <button class="btn btn-primario" onclick="window.print()">Imprimir</button>
  <a href="${pageContext.request.contextPath}/pos" class="btn btn-ghost">
  + Nueva venta
  </a>
</div>

<div class="ticket-wrapper">

  <div class="ticket-header">
  <h1>COMEDOR UNIVERSITARIO</h1>
  <div class="sub">Universidad Veracruzana</div>
  <div class="sub" style="margin-top:4px;">
  <%= pedido.getFechaCreacion()!= null
? pedido.getFechaCreacion().format(fmt) : "" %>
  </div>
  </div>

  <div class="ticket-folio">
 FOLIO <%= pedido.getFolio()!= null? pedido.getFolio() : "#"+pedido.getIdPedido() %>
  </div>

  <div class="ticket-info">
  <div class="row">
  <span>Cajero:</span>
  <strong><%= empleado.getNombre() %></strong>
  </div>
  <% if (cliente!= null &&!cliente.getIdUsuario().equals(empleado.getIdUsuario())) { %>
  <div class="row">
  <span>Cliente:</span>
  <strong><%= cliente.getNombreCompleto() %></strong>
  </div>
  <% } %>
  <div class="row">
  <span>Método pago:</span>
  <strong><%= pedido.getMetodoPagoDisplay()!= null
? pedido.getMetodoPagoDisplay() : "-" %></strong>
  </div>
  </div>

  <div class="ticket-items">
  <% if (pedido.getDetalles()!= null) {
 for (DetallePedido d : pedido.getDetalles()) {
 String nombre = (d.getPlatillo()!= null && d.getPlatillo().getNombre()!= null)
? d.getPlatillo().getNombre()
  : "Platillo #" + d.getIdPlatillo();
 java.math.BigDecimal sub = d.getPrecioUnitario()
.multiply(new java.math.BigDecimal(d.getCantidad()));
  %>
  <div class="ticket-item">
  <span class="qty"><%= d.getCantidad() %>x</span>
  <span style="flex:1;padding:0 8px;"><%= nombre %></span>
  <span>$<%= sub.toPlainString() %></span>
  </div>
  <% } } %>
  </div>

  <% if (pedido.getSubtotal()!= null) { %>
  <div class="ticket-item">
  <span>Subtotal:</span>
  <span>$<%= pedido.getSubtotal().toPlainString() %></span>
  </div>
  <% } %>
  <% if (pedido.getDescuentoBeca()!= null
  && pedido.getDescuentoBeca().compareTo(java.math.BigDecimal.ZERO) > 0) { %>
  <div class="ticket-item" style="color:var(--uv-verde-dark);">
  <span>Beca:</span>
  <span>-$<%= pedido.getDescuentoBeca().toPlainString() %></span>
  </div>
  <% } %>

  <div class="ticket-total">
  <span>TOTAL</span>
  <span>$<%= pedido.getTotal()!= null
? pedido.getTotal().toPlainString() : "0.00" %></span>
  </div>

  <div class="ticket-footer">
  ¡Gracias por tu preferencia!<br>
 Universidad Veracruzana
  </div>
</div>

<script>
// Auto-imprimir al cargar (opcional, descomenta si lo deseas)
// window.addEventListener('load', () =>setTimeout(() =>window.print(), 500));
</script>

</body>
</html>
