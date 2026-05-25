<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="mx.uv.comedor.model.*" %>
<%@ page import="java.util.List" %>
<%
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    if (usuario == null || usuario.getRol() != RolEnum.ADMIN) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    List<AlertaInventario> alertas = (List<AlertaInventario>) request.getAttribute("alertas");
    if (alertas == null) alertas = new java.util.ArrayList<>();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1.0">
    <title>Alertas de Inventario — Comedor UV</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body data-context-path="${pageContext.request.contextPath}">

<%@ include file="../_header.jsp" %>

<main class="page-wrapper">

    <div class="page-header">
        <div class="page-title">Alertas de Inventario ⚠️</div>
        <div class="page-subtitle"><%= alertas.size() %> alertas activas</div>
    </div>

    <div class="tabs">
        <a href="${pageContext.request.contextPath}/admin/inventario" class="tab-btn" style="text-decoration:none;"> Ingredientes</a>
        <a href="${pageContext.request.contextPath}/admin/inventario/alertas" class="tab-btn activo" style="text-decoration:none;"> Alertas</a>
        <a href="${pageContext.request.contextPath}/admin/inventario/compras" class="tab-btn" style="text-decoration:none;"> Compras</a>
    </div>

    <div class="card">
        <% if (alertas.isEmpty()) { %>
        <div style="text-align:center;padding:60px;color:var(--uv-gris-500);">
            <div style="font-size:3rem;margin-bottom:12px;">✅</div>
            <div style="font-family:var(--fuente-display);font-weight:700;font-size:1.1rem;">
                Sin alertas activas
            </div>
            <div style="font-size:.875rem;margin-top:6px;">El inventario está en buen estado.</div>
        </div>
        <% } else { %>
        <% for (AlertaInventario a : alertas) { %>
        <div style="display:flex;align-items:flex-start;gap:14px;padding:16px 22px;
                    border-bottom:1px solid var(--color-borde);">
            <span style="font-size:1.8rem;flex-shrink:0;">
                <%= a.getNivel() == NivelAlertaEnum.CRITICO ? "🔴"
                  : a.getNivel() == NivelAlertaEnum.WARNING ? "🟡" : "🔵" %>
            </span>
            <div style="flex:1;">
                <div style="font-weight:700;font-size:.95rem;margin-bottom:4px;">
                    <%= a.getMensaje() %>
                </div>
                <div style="font-size:.8rem;color:var(--uv-gris-500);">
                    Tipo: <%= a.getTipo().name() %> · Nivel: <%= a.getNivel().name() %>
                </div>
            </div>
            <form method="post" action="${pageContext.request.contextPath}/admin/inventario/alerta/atender">
                <input type="hidden" name="idAlerta" value="<%= a.getIdAlerta() %>">
                <button type="submit" class="btn btn-sm btn-ghost">Atender</button>
            </form>
        </div>
        <% } %>
        <% } %>
    </div>

</main>

<%@ include file="../_footer.jsp" %>
</body>
</html>
