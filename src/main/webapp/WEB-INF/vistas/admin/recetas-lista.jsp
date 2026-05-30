<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="mx.uv.comedor.model.*" %>
<%@ page import="java.util.List" %>
<%
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    if (usuario == null || usuario.getRol() != RolEnum.ADMIN) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    List<Platillo> platillos = (List<Platillo>) request.getAttribute("platillos");
    if (platillos == null) platillos = new java.util.ArrayList<>();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Recetas - Comedor UV</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body data-context-path="${pageContext.request.contextPath}">

<%@ include file="../_header.jsp" %>

<main class="page-wrapper">

    <div class="page-header">
        <div class="page-title">Recetas de Platillos</div>
        <div class="page-subtitle">
            Define que ingredientes (y en que cantidad) usa cada platillo.
            Esto permite que el inventario se descuente automaticamente con cada venta.
        </div>
    </div>

    <div class="card">
        <div style="overflow-x:auto;">
            <table class="tabla">
                <thead>
                    <tr>
                        <th>Platillo</th>
                        <th>Tipo</th>
                        <th>Precio</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (platillos.isEmpty()) { %>
                    <tr><td colspan="4" style="text-align:center;color:var(--uv-gris-500);padding:24px;">
                        Sin platillos registrados
                    </td></tr>
                    <% } %>
                    <% for (Platillo p : platillos) { %>
                    <tr>
                        <td>
                            <div style="font-weight:600;"><%= p.getNombre() %></div>
                            <% if (p.getCategoria() != null) { %>
                            <div style="font-size:.72rem;color:var(--uv-gris-500);">
                                <%= p.getCategoria().getEtiqueta() %>
                            </div>
                            <% } %>
                        </td>
                        <td>
                            <span style="font-size:.72rem;background:var(--uv-gris-200);padding:2px 8px;border-radius:10px;font-weight:700;">
                                <%= p.getTipo().name() %>
                            </span>
                        </td>
                        <td style="font-weight:700;color:var(--uv-azul);">
                            $<%= p.getPrecio().toPlainString() %>
                        </td>
                        <td>
                            <a href="${pageContext.request.contextPath}/admin/recetas?idPlatillo=<%= p.getIdPlatillo() %>"
                               class="btn btn-ghost btn-sm">
                                Configurar receta
                            </a>
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
