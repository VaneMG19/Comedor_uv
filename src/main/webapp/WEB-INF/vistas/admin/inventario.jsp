<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="mx.uv.comedor.model.*" %>
<%@ page import="java.util.List" %>
<%
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    if (usuario == null || usuario.getRol() != RolEnum.ADMIN) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    List<Ingrediente> ingredientes = (List<Ingrediente>) request.getAttribute("ingredientes");

    // FIX: el servlet manda esto como Long, no como Integer
    Object _totalObj  = request.getAttribute("totalAlertas");
    Object _critObj   = request.getAttribute("alertasCriticas");
    long totalAlertas    = _totalObj instanceof Number ? ((Number)_totalObj).longValue() : 0L;
    long alertasCriticas = _critObj  instanceof Number ? ((Number)_critObj).longValue()  : 0L;

    if (ingredientes == null) ingredientes = new java.util.ArrayList<>();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1.0">
    <title>Inventario — Comedor UV</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body data-context-path="${pageContext.request.contextPath}">

<%@ include file="../_header.jsp" %>

<main class="page-wrapper">

    <div class="page-header">
        <div class="page-title">Inventario 📦</div>
        <div class="page-subtitle">
            <%= ingredientes.size() %> ingredientes registrados —
            <% if (totalAlertas > 0) { %>
            <span style="color:var(--uv-rojo);font-weight:700;">
                ⚠️ <%= totalAlertas %> alertas
                <% if (alertasCriticas > 0) { %>(<%= alertasCriticas %> críticas)<% } %>
            </span>
            <% } else { %> Sin alertas<% } %>
        </div>
    </div>

    <div class="tabs">
        <a href="${pageContext.request.contextPath}/admin/inventario" class="tab-btn activo" style="text-decoration:none;"> Ingredientes</a>
        <a href="${pageContext.request.contextPath}/admin/inventario/alertas" class="tab-btn" style="text-decoration:none;">️ Alertas</a>
        <a href="${pageContext.request.contextPath}/admin/inventario/compras" class="tab-btn" style="text-decoration:none;"> Compras</a>
    </div>

    <div class="card">
        <div style="overflow-x:auto;">
            <table class="tabla">
                <thead>
                <tr>
                    <th>Ingrediente</th>
                    <th>Stock actual</th>
                    <th>Stock mínimo</th>
                    <th>Unidad</th>
                    <th>Estado</th>
                </tr>
                </thead>
                <tbody>
                <% if (ingredientes.isEmpty()) { %>
                <tr><td colspan="5" style="text-align:center;color:var(--uv-gris-500);padding:24px;">
                    Sin ingredientes registrados
                </td></tr>
                <% } %>
                <% for (Ingrediente ing : ingredientes) {
                    boolean bajo = ing.getStockActual().compareTo(ing.getStockMinimo()) <= 0;
                %>
                <tr>
                    <td>
                        <div style="font-weight:600;"><%= ing.getNombre() %></div>
                        <% if (ing.getCategoria() != null) { %>
                        <div style="font-size:.75rem;color:var(--uv-gris-500);">
                            <%= ing.getCategoria() %>
                        </div>
                        <% } %>
                    </td>
                    <td style="font-weight:700;color:<%= bajo ? "var(--uv-rojo)" : "var(--uv-verde)" %>;">
                        <%= ing.getStockActual().toPlainString() %>
                    </td>
                    <td><%= ing.getStockMinimo().toPlainString() %></td>
                    <td><%= ing.getUnidadMedida() %></td>
                    <td>
                            <span class="estado-badge <%= bajo ? "estado-CANCELADO" : "estado-LISTO" %>">
                                <%= bajo ? "BAJO STOCK" : "OK" %>
                            </span>
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
