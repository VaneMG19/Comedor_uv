<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="mx.uv.comedor.model.*" %>
<%@ page import="java.util.List" %>
<%
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    if (usuario == null || usuario.getRol() != RolEnum.ADMIN) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    Ingrediente ingrediente = (Ingrediente) request.getAttribute("ingrediente");
    List<MovimientoInventario> movimientos = (List<MovimientoInventario>) request.getAttribute("movimientos");
    if (movimientos == null) movimientos = new java.util.ArrayList<>();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1.0">
    <title>Movimientos — Comedor UV</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body data-context-path="${pageContext.request.contextPath}">

<%@ include file="../_header.jsp" %>

<main class="page-wrapper">

    <div class="page-header">
        <div class="page-title">
            Movimientos de Inventario 📊
            <% if (ingrediente != null) { %>
            — <%= ingrediente.getNombre() %>
            <% } %>
        </div>
        <div class="page-subtitle"><%= movimientos.size() %> movimientos</div>
    </div>

    <div style="margin-bottom:16px;">
        <a href="${pageContext.request.contextPath}/admin/inventario"
           style="font-size:.85rem;color:var(--uv-gris-500);">← Volver a inventario</a>
    </div>

    <div class="card">
        <% if (movimientos.isEmpty()) { %>
        <div style="text-align:center;padding:60px;color:var(--uv-gris-500);">
            <div style="font-size:3rem;margin-bottom:12px;">📊</div>
            <div style="font-family:var(--fuente-display);font-weight:700;font-size:1.1rem;">
                Sin movimientos registrados
            </div>
        </div>
        <% } else { %>
        <div style="overflow-x:auto;">
            <table class="tabla">
                <thead>
                    <tr>
                        <th>Fecha</th>
                        <th>Tipo</th>
                        <th>Cantidad</th>
                        <th>Stock resultante</th>
                        <th>Motivo</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (MovimientoInventario m : movimientos) { %>
                    <tr>
                        <td style="font-size:.85rem;">
                            <%= m.getFecha() != null ? m.getFecha().toLocalDate() : "—" %>
                        </td>
                        <td>
                            <span class="estado-badge"
                                  style="background:var(--uv-azul-light);color:var(--uv-azul);">
                                <%= m.getTipo().name() %>
                            </span>
                        </td>
                        <td style="font-weight:700;">
                            <%= m.getCantidad().toPlainString() %>
                        </td>
                        <td><%= m.getStockResultante() != null ? m.getStockResultante().toPlainString() : "—" %></td>
                        <td style="font-size:.85rem;color:var(--uv-gris-700);">
                            <%= m.getMotivo() != null ? m.getMotivo() : "—" %>
                        </td>
                    </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
        <% } %>
    </div>

</main>

<%@ include file="../_footer.jsp" %>
</body>
</html>
