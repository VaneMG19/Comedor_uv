<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="mx.uv.comedor.model.*" %>
<%@ page import="java.util.List" %>
<%
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    if (usuario == null || usuario.getRol() != RolEnum.ADMIN) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    List<CompraAnticipada> compras = (List<CompraAnticipada>) request.getAttribute("compras");
    if (compras == null) compras = new java.util.ArrayList<>();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1.0">
    <title>Compras Anticipadas — Comedor UV</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body data-context-path="${pageContext.request.contextPath}">

<%@ include file="../_header.jsp" %>

<main class="page-wrapper">

    <div class="page-header">
        <div class="page-title">Compras Anticipadas 🛒</div>
        <div class="page-subtitle"><%= compras.size() %> compras registradas</div>
    </div>

    <div class="tabs">
        <a href="${pageContext.request.contextPath}/admin/inventario" class="tab-btn" style="text-decoration:none;"> Ingredientes</a>
        <a href="${pageContext.request.contextPath}/admin/inventario/alertas" class="tab-btn" style="text-decoration:none;"> Alertas</a>
        <a href="${pageContext.request.contextPath}/admin/inventario/compras" class="tab-btn activo" style="text-decoration:none;"> Compras</a>
    </div>

    <div class="card">
        <% if (compras.isEmpty()) { %>
        <div style="text-align:center;padding:60px;color:var(--uv-gris-500);">
            <div style="font-size:3rem;margin-bottom:12px;">🛒</div>
            <div style="font-family:var(--fuente-display);font-weight:700;font-size:1.1rem;">
                Sin compras registradas
            </div>
        </div>
        <% } else { %>
        <div style="overflow-x:auto;">
            <table class="tabla">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Proveedor</th>
                        <th>Fecha emisión</th>
                        <th>Entrega esperada</th>
                        <th>Total</th>
                        <th>Estado</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (CompraAnticipada c : compras) { %>
                    <tr>
                        <td style="font-family:var(--fuente-display);font-weight:700;">
                            #<%= c.getIdCompra() %>
                        </td>
                        <td><%= c.getProveedor() %></td>
                        <td style="font-size:.85rem;">
                            <%= c.getFechaEmision() != null ? c.getFechaEmision().toLocalDate() : "—" %>
                        </td>
                        <td style="font-size:.85rem;">
                            <%= c.getFechaEntregaEsperada() != null ? c.getFechaEntregaEsperada() : "—" %>
                        </td>
                        <td style="font-weight:700;color:var(--uv-azul);">
                            $<%= c.getTotalEstimado() != null ? c.getTotalEstimado().toPlainString() : "0.00" %>
                        </td>
                        <td>
                            <span class="estado-badge estado-LISTO">
                                <%= c.getEstado().name() %>
                            </span>
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
