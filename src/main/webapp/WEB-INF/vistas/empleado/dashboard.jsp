<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="mx.uv.comedor.model.*" %>
<%@ page import="mx.uv.comedor.dao.*" %>
<%@ page import="java.util.List" %>
<%
 Usuario usuario = (Usuario) session.getAttribute("usuario");
 if (usuario == null ||
         (usuario.getRol() != RolEnum.EMPLEADO && usuario.getRol() != RolEnum.ADMIN)) {
  response.sendRedirect(request.getContextPath() + "/login");
  return;
 }
 PedidoDAO pedidoDAO = new PedidoDAO();
 List<Pedido> pedidos = pedidoDAO.listarActivos();

 long pendientes  = pedidos.stream().filter(p -> p.getEstado() == EstadoPedidoEnum.PENDIENTE).count();
 long preparando  = pedidos.stream().filter(p -> p.getEstado() == EstadoPedidoEnum.PREPARANDO).count();
 long listos      = pedidos.stream().filter(p -> p.getEstado() == EstadoPedidoEnum.LISTO).count();
 long anticipados = pedidos.stream().filter(p -> p.getTipo()   == TipoPedidoEnum.ANTICIPADO).count();
%>
<!DOCTYPE html>
<html lang="es">
<head>
 <meta charset="UTF-8">
 <meta name="viewport" content="width=device-width,initial-scale=1.0">
 <title>Panel Cocina — Comedor UV</title>
 <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
 <style>
  /* Auto-refresh cada 30 segundos */
  .refresh-bar {
   height: 3px;
   background: var(--uv-verde);
   animation: progress 30s linear infinite;
   border-radius: 2px;
   margin-bottom: 20px;
  }
  @keyframes progress {
   from { width: 100%; }
   to   { width: 0%;   }
  }

  .pedido-cocina {
   background: white;
   border-radius: var(--radio-lg);
   border: 1px solid var(--color-borde);
   box-shadow: var(--sombra-sm);
   overflow: hidden;
   transition: box-shadow var(--trans-normal);
  }
  .pedido-cocina:hover { box-shadow: var(--sombra-md); }

  .pedido-cocina-header {
   padding: 14px 18px;
   display: flex;
   align-items: center;
   justify-content: space-between;
   border-bottom: 1px solid var(--color-borde);
  }
  .pedido-cocina.anticipado {
   border-left: 4px solid var(--uv-amarillo);
  }
  .pedido-cocina.inmediato {
   border-left: 4px solid var(--uv-azul);
  }
  .pedido-cocina.listo {
   border-left: 4px solid var(--uv-verde);
  }

  .pedido-platillos {
   padding: 12px 18px;
   border-bottom: 1px solid var(--uv-gris-200);
  }
  .pedido-platillo-item {
   display: flex;
   justify-content: space-between;
   font-size: .875rem;
   padding: 4px 0;
  }

  .pedido-acciones {
   padding: 12px 18px;
   display: flex;
   gap: 8px;
   flex-wrap: wrap;
  }

  .filtro-tabs {
   display: flex;
   gap: 4px;
   background: var(--uv-gris-200);
   border-radius: var(--radio);
   padding: 4px;
   margin-bottom: 20px;
   flex-wrap: wrap;
  }
  .filtro-tab {
   padding: 7px 16px;
   border: none;
   border-radius: 8px;
   font-family: var(--fuente-display);
   font-weight: 600;
   font-size: .82rem;
   cursor: pointer;
   color: var(--uv-gris-700);
   background: transparent;
   transition: all var(--trans-rapida);
   display: flex;
   align-items: center;
   gap: 6px;
  }
  .filtro-tab.activo {
   background: white;
   color: var(--uv-azul);
   box-shadow: var(--sombra-sm);
  }
  .filtro-count {
   background: var(--uv-azul);
   color: white;
   font-size: .65rem;
   padding: 2px 6px;
   border-radius: 10px;
  }
  .filtro-tab.activo .filtro-count { background: var(--uv-azul); }
 </style>
</head>
<body data-context-path="${pageContext.request.contextPath}">

<%@ include file="../_header.jsp" %>

<main class="page-wrapper">

 <div class="page-header d-flex justify-between align-center flex-wrap gap-2">
  <div>
   <div class="page-title">Panel de Cocina 👨‍🍳</div>
   <div class="page-subtitle">
    <%= pedidos.size() %> pedidos activos —
    Se actualiza automáticamente cada 30 segundos
   </div>
  </div>
  <button class="btn btn-ghost" onclick="location.reload()">🔄 Actualizar</button>
 </div>

 <!-- Barra de progreso de auto-refresh -->
 <div class="refresh-bar"></div>

 <!-- KPIs rápidos -->
 <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:12px;margin-bottom:24px;">
  <div class="stat-card" style="padding:14px;">
   <div class="stat-icon amarillo" style="width:38px;height:38px;font-size:1.1rem;">⏳</div>
   <div>
    <div class="stat-valor" style="font-size:1.4rem;"><%= pendientes %></div>
    <div class="stat-label">Pendientes</div>
   </div>
  </div>
  <div class="stat-card" style="padding:14px;">
   <div class="stat-icon azul" style="width:38px;height:38px;font-size:1.1rem;"></div>
   <div>
    <div class="stat-valor" style="font-size:1.4rem;"><%= preparando %></div>
    <div class="stat-label">Preparando</div>
   </div>
  </div>
  <div class="stat-card" style="padding:14px;">
   <div class="stat-icon verde" style="width:38px;height:38px;font-size:1.1rem;"></div>
   <div>
    <div class="stat-valor" style="font-size:1.4rem;"><%= listos %></div>
    <div class="stat-label">Listos</div>
   </div>
  </div>
  <div class="stat-card" style="padding:14px;">
   <div class="stat-icon amarillo" style="width:38px;height:38px;font-size:1.1rem;"></div>
   <div>
    <div class="stat-valor" style="font-size:1.4rem;"><%= anticipados %></div>
    <div class="stat-label">Anticipados</div>
   </div>
  </div>
 </div>

 <!-- Filtros -->
 <div class="filtro-tabs">
  <button class="filtro-tab activo" onclick="filtrar('todos', this)">
   Todos <span class="filtro-count"><%= pedidos.size() %></span>
  </button>
  <button class="filtro-tab" onclick="filtrar('PENDIENTE', this)">
   ⏳ Pendientes <span class="filtro-count"><%= pendientes %></span>
  </button>
  <button class="filtro-tab" onclick="filtrar('PREPARANDO', this)">
   Preparando <span class="filtro-count"><%= preparando %></span>
  </button>
  <button class="filtro-tab" onclick="filtrar('LISTO', this)">
   Listos <span class="filtro-count"><%= listos %></span>
  </button>
  <button class="filtro-tab" onclick="filtrar('ANTICIPADO', this)">
   Anticipados <span class="filtro-count"><%= anticipados %></span>
  </button>
 </div>

 <!-- Grid de pedidos -->
 <div id="pedidos-grid"
      style="display:grid;grid-template-columns:repeat(auto-fill,minmax(300px,1fr));gap:16px;">

  <% if (pedidos.isEmpty()) { %>
  <div style="grid-column:1/-1;text-align:center;padding:60px;
                    color:var(--uv-gris-500);">
   <div style="font-size:3rem;margin-bottom:12px;"></div>
   <div style="font-family:var(--fuente-display);font-weight:700;font-size:1.1rem;">
    ¡Sin pedidos pendientes!
   </div>
   <div style="font-size:.875rem;margin-top:6px;">
    Todos los pedidos han sido atendidos.
   </div>
  </div>
  <% } %>

  <% for (Pedido p : pedidos) {
   boolean esAnticipado = p.getTipo() == TipoPedidoEnum.ANTICIPADO;
   String claseEstado = p.getEstado() == EstadoPedidoEnum.LISTO ? "listo"
           : esAnticipado ? "anticipado" : "inmediato";
  %>
  <div class="pedido-cocina <%= claseEstado %>"
       data-estado="<%= p.getEstado().name() %>"
       data-tipo="<%= p.getTipo().name() %>">

   <!-- Header del pedido -->
   <div class="pedido-cocina-header">
    <div>
     <div style="font-family:var(--fuente-display);font-weight:700;
                                font-size:1rem;">
      <%= p.getFolio() %>
     </div>
     <div style="font-size:.75rem;color:var(--uv-gris-500);margin-top:2px;">
      <%= esAnticipado ? "Anticipado" : "Inmediato" %>
      <% if (p.getFechaCreacion() != null) { %>
      · <%= p.getFechaCreacion()
             .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) %>
      <% } %>
     </div>
    </div>
    <span class="estado-badge estado-<%= p.getEstado().name() %>">
                    <%= p.getEstado().name() %>
                </span>
   </div>

   <!-- Hora de recogida (si es anticipado) -->
   <% if (esAnticipado && p.getProgramacion() != null) { %>
   <div style="background:var(--uv-amarillo-light);padding:8px 18px;
                        font-size:.8rem;color:#744210;
                        border-bottom:1px solid var(--color-borde);">
    Recoger a las
    <strong><%= p.getProgramacion().getHoraRecogida() %></strong>
    · <%= p.getProgramacion().getLugarRecogida() %>
   </div>
   <% } %>

   <!-- Platillos -->
   <div class="pedido-platillos">
    <% if (p.getDetalles() != null && !p.getDetalles().isEmpty()) {
     for (DetallePedido d : p.getDetalles()) {
      String nombrePl = d.getPlatillo() != null
              ? d.getPlatillo().getNombre()
              : "Platillo #" + d.getIdPlatillo();
    %>
    <div class="pedido-platillo-item">
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
    </div>
    <% if (d.getPersonalizaciones() != null && !d.getPersonalizaciones().isBlank()) { %>
    <div style="font-size:.75rem;color:var(--uv-gris-500);
                            font-style:italic;margin-left:24px;">
     <%= d.getPersonalizaciones() %>
    </div>
    <% } %>
    <% } } %>
   </div>

   <!-- Notas/instrucciones especiales del pedido -->
   <% if (p.getNotas() != null && !p.getNotas().isBlank()) { %>
   <div style="background:#fef3c7;border-left:4px solid #d97706;
                        padding:10px 18px;font-size:.85rem;color:#78350f;
                        margin:0 18px 12px 18px;border-radius:0 8px 8px 0;">
    <div style="font-weight:700;font-size:.75rem;margin-bottom:4px;
                            text-transform:uppercase;letter-spacing:.5px;">
     Instrucciones especiales
    </div>
    <%= p.getNotas() %>
   </div>
   <% } %>

   <!-- Acciones según estado -->
   <div class="pedido-acciones">
    <% if (p.getEstado() == EstadoPedidoEnum.PENDIENTE) { %>
    <form method="post"
          action="${pageContext.request.contextPath}/empleado/estado">
     <input type="hidden" name="idPedido" value="<%= p.getIdPedido() %>">
     <input type="hidden" name="nuevoEstado" value="PREPARANDO">
     <input type="hidden" name="comentario" value="Inicio de preparación">
     <button type="submit" class="btn btn-primario btn-sm">
      Iniciar preparación
     </button>
    </form>

    <% } else if (p.getEstado() == EstadoPedidoEnum.PREPARANDO) { %>
    <form method="post"
          action="${pageContext.request.contextPath}/empleado/estado">
     <input type="hidden" name="idPedido" value="<%= p.getIdPedido() %>">
     <input type="hidden" name="nuevoEstado" value="LISTO">
     <input type="hidden" name="comentario" value="Listo para recoger">
     <button type="submit" class="btn btn-secundario btn-sm">
      Marcar como listo
     </button>
    </form>

    <% } else if (p.getEstado() == EstadoPedidoEnum.LISTO) { %>
    <form method="post"
          action="${pageContext.request.contextPath}/empleado/estado">
     <input type="hidden" name="idPedido" value="<%= p.getIdPedido() %>">
     <input type="hidden" name="nuevoEstado" value="ENTREGADO">
     <input type="hidden" name="comentario" value="Pedido entregado al cliente">
     <button type="submit" class="btn btn-acento btn-sm">
      Marcar entregado
     </button>
    </form>
    <% } %>

    <!-- Cancelar siempre disponible excepto en LISTO/ENTREGADO -->
    <% if (p.getEstado() == EstadoPedidoEnum.PENDIENTE
            || p.getEstado() == EstadoPedidoEnum.PREPARANDO) { %>
    <form method="post"
          action="${pageContext.request.contextPath}/empleado/estado">
     <input type="hidden" name="idPedido" value="<%= p.getIdPedido() %>">
     <input type="hidden" name="nuevoEstado" value="CANCELADO">
     <input type="hidden" name="comentario" value="Cancelado por cocina">
     <button type="submit" class="btn btn-ghost btn-sm"
             onclick="return confirm('¿Cancelar este pedido?')">
      ✕ Cancelar
     </button>
    </form>
    <% } %>
   </div>
  </div>
  <% } %>
 </div>

</main>

<%@ include file="../_footer.jsp" %>

<script>
 // Filtrar pedidos por estado/tipo
 function filtrar(filtro, btn) {
  document.querySelectorAll('.filtro-tab').forEach(b => b.classList.remove('activo'));
  btn.classList.add('activo');
  document.querySelectorAll('.pedido-cocina').forEach(card => {
   if (filtro === 'todos') {
    card.style.display = 'block';
   } else if (filtro === 'ANTICIPADO') {
    card.style.display =
            card.dataset.tipo === 'ANTICIPADO' ? 'block' : 'none';
   } else {
    card.style.display =
            card.dataset.estado === filtro ? 'block' : 'none';
   }
  });
 }

 // Auto-refresh cada 30 segundos
 setTimeout(() => location.reload(), 30000);
</script>
</body>
</html>
