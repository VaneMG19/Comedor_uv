<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="mx.uv.comedor.model.*" %>
<%
 Usuario admin = (Usuario) session.getAttribute("usuario");
 if (admin == null || admin.getRol()!= RolEnum.ADMIN) {
 response.sendRedirect(request.getContextPath() + "/login");
 return;
  }
 Usuario u = (Usuario) request.getAttribute("usuario");
 if (u == null) {
 response.sendRedirect(request.getContextPath() + "/admin/usuarios");
 return;
  }
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width,initial-scale=1.0">
  <title>Usuario - Comedor UV</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body data-context-path="${pageContext.request.contextPath}">

<%@ include file="../_header.jsp" %>

<main class="page-wrapper">

  <div style="margin-bottom:20px;">
  <a href="${pageContext.request.contextPath}/admin/usuarios"
 style="font-size:.85rem;color:var(--uv-gris-500);">
 Volver a usuarios
  </a>
  </div>

  <div style="display:flex;align-items:center;gap:20px;margin-bottom:28px;flex-wrap:wrap;">
  <div style="width:80px;height:80px;border-radius:50%;
 background:linear-gradient(135deg,var(--uv-azul),var(--uv-verde));
 display:flex;align-items:center;justify-content:center;
 font-weight:800;font-size:1.6rem;color:white;">
  <%= (u.getNombre().charAt(0) + "" + u.getApellidos().charAt(0)).toUpperCase() %>
  </div>
  <div>
  <div class="page-title"><%= u.getNombreCompleto() %></div>
  <div style="font-size:.85rem;color:var(--uv-gris-500);margin-top:4px;">
  <%= u.getEmail() %>
  </div>
  <div style="margin-top:6px;">
  <span style="background:var(--uv-azul-light);color:var(--uv-azul);
 font-size:.72rem;font-weight:700;padding:3px 10px;
 border-radius:12px;">
  <%= u.getRol().name() %>
  </span>
  </div>
  </div>
  </div>

  <div class="card" style="max-width:600px;">
  <div class="card-header"><div class="card-title">Información</div></div>
  <div class="card-body">
  <div style="display:grid;gap:12px;">
  <div style="display:flex;justify-content:space-between;padding:10px;
 background:var(--uv-gris-100);border-radius:8px;">
  <span style="color:var(--uv-gris-700);">Teléfono</span>
  <span style="font-weight:600;"><%= u.getTelefono()!= null? u.getTelefono() : "-" %></span>
  </div>
  <div style="display:flex;justify-content:space-between;padding:10px;
 background:var(--uv-gris-100);border-radius:8px;">
  <span style="color:var(--uv-gris-700);">Fecha de registro</span>
  <span style="font-weight:600;">
  <%= u.getFechaRegistro()!= null? u.getFechaRegistro().toLocalDate() : "-" %>
  </span>
  </div>
  <div style="display:flex;justify-content:space-between;padding:10px;
 background:var(--uv-gris-100);border-radius:8px;">
  <span style="color:var(--uv-gris-700);">Estado</span>
  <span style="font-weight:600;color:<%= u.isActivo()? "var(--uv-verde)" : "var(--uv-rojo)" %>;">
  <%= u.isActivo()? " Activa" : " Inactiva" %>
  </span>
  </div>
  </div>
  </div>
  </div>

</main>

<%@ include file="../_footer.jsp" %>
</body>
</html>
