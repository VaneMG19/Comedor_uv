<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="mx.uv.comedor.model.*" %>
<%@ page import="java.util.List" %>
<%
 Usuario usuario = (Usuario) session.getAttribute("usuario");
 if (usuario == null) {
 response.sendRedirect(request.getContextPath() + "/login");
 return;
  }
 List<Calificacion>califs = (List<Calificacion>) request.getAttribute("calificaciones");
 if (califs == null) califs = new java.util.ArrayList<>();
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width,initial-scale=1.0">
  <title>Calificaciones - Comedor UV</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body data-context-path="${pageContext.request.contextPath}">

<%@ include file="../_header.jsp" %>

<main class="page-wrapper">

  <div class="page-header">
  <div class="page-title">Calificaciones ⭐</div>
  <div class="page-subtitle">
  <%= califs.size() %>calificaciones recibidas
  <% if (!califs.isEmpty()) {
 double prom = califs.stream().mapToInt(Calificacion::getPuntuacion).average().orElse(0);
  %>
  - Promedio: <strong style="color:var(--uv-amarillo);">
  <%= String.format("%.1f", prom) %> ⭐
  </strong>
  <% } %>
  </div>
  </div>

  <div class="card">
  <% if (califs.isEmpty()) { %>
  <div style="text-align:center;padding:60px;color:var(--uv-gris-500);">
  <div style="font-size:3rem;margin-bottom:12px;">⭐</div>
  <div style="font-family:var(--fuente-display);font-weight:700;font-size:1.1rem;">
 Sin calificaciones aún
  </div>
  <div style="font-size:.875rem;margin-top:6px;">
 Cuando los usuarios califiquen sus pedidos aparecerán aquí.
  </div>
  </div>
  <% } else for (Calificacion c : califs) { %>
  <div style="padding:16px 22px;border-bottom:1px solid var(--color-borde);">
  <div style="display:flex;align-items:flex-start;justify-content:space-between;
 gap:12px;flex-wrap:wrap;">
  <div style="flex:1;min-width:200px;">
  <div style="font-weight:700;font-size:.95rem;">
  <%= c.getNombrePlatillo()!= null? c.getNombrePlatillo()
  : "Pedido #" + c.getIdPedido() %>
  </div>
  <% if (c.getNombreUsuario()!= null) { %>
  <div style="font-size:.8rem;color:var(--uv-gris-500);">
 por <%= c.getNombreUsuario() %>
  </div>
  <% } %>
  <div style="margin:6px 0;color:var(--uv-amarillo);font-size:1.1rem;">
  <% for (int i = 1; i <= 5; i++) { %>
  <%= i <= c.getPuntuacion()? "★" : "☆" %>
  <% } %>
  </div>
  <% if (c.getComentario()!= null &&!c.getComentario().isBlank()) { %>
  <div style="font-size:.875rem;color:var(--uv-gris-700);font-style:italic;
 background:var(--uv-gris-100);padding:10px;border-radius:8px;
 margin-top:8px;">
  "<%= c.getComentario() %>"
  </div>
  <% } %>
  </div>
  <div style="text-align:right;font-size:.8rem;color:var(--uv-gris-500);">
  <%= c.getFecha()!= null? c.getFecha().toLocalDate() : "-" %>
  </div>
  </div>
  </div>
  <% } %>
  </div>

</main>

<%@ include file="../_footer.jsp" %>
</body>
</html>
