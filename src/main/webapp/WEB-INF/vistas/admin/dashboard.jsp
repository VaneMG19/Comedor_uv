<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="mx.uv.comedor.model.*" %>
<%@ page import="mx.uv.comedor.dao.*" %>
<%@ page import="java.util.List" %>
<%
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    if (usuario == null || usuario.getRol() != RolEnum.ADMIN) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }

    // Estadísticas rápidas
    PedidoDAO      pedidoDAO  = new PedidoDAO();
    InventarioDAO  invDAO     = new InventarioDAO();
    UsuarioDAO     usuarioDAO = new UsuarioDAO();

    List<Pedido>           pedidosActivos = pedidoDAO.listarActivos();
    List<AlertaInventario> alertas        = invDAO.listarAlertasActivas();
    List<Usuario>          usuarios       = usuarioDAO.listarTodos();

    long alertasCriticas = alertas.stream()
            .filter(a -> a.getNivel() == NivelAlertaEnum.CRITICO).count();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1.0">
    <title>Dashboard Admin — Comedor UV</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body data-context-path="${pageContext.request.contextPath}">

<%@ include file="../_header.jsp" %>

<main class="page-wrapper">

    <div class="page-header">
        <div class="page-title">Panel de Administración ⚙️</div>
        <div class="page-subtitle">
            Bienvenido, <%= usuario.getNombreCompleto() %> —
            <%= java.time.LocalDate.now()
                    .format(java.time.format.DateTimeFormatter
                            .ofPattern("EEEE d 'de' MMMM, yyyy",
                                    new java.util.Locale("es","MX"))) %>
        </div>
    </div>

    <!-- KPIs -->
    <div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(200px,1fr));
                gap:16px;margin-bottom:28px;">

        <div class="stat-card">
            <div class="stat-icon azul">📋</div>
            <div>
                <div class="stat-valor"><%= pedidosActivos.size() %></div>
                <div class="stat-label">Pedidos activos</div>
            </div>
        </div>

        <div class="stat-card">
            <div class="stat-icon <%= alertasCriticas > 0 ? "rojo" : "verde" %>">
                <%= alertasCriticas > 0 ? "⚠️" : "📦" %>
            </div>
            <div>
                <div class="stat-valor"><%= alertas.size() %></div>
                <div class="stat-label">
                    Alertas de inventario
                    <% if (alertasCriticas > 0) { %>
                    <span style="color:var(--uv-rojo);font-weight:700;">
                        (<%= alertasCriticas %> críticas)
                    </span>
                    <% } %>
                </div>
            </div>
        </div>

        <div class="stat-card">
            <div class="stat-icon azul">👥</div>
            <div>
                <div class="stat-valor"><%= usuarios.size() %></div>
                <div class="stat-label">Usuarios registrados</div>
            </div>
        </div>

        <div class="stat-card">
            <div class="stat-icon amarillo">🍽️</div>
            <div>
                <div class="stat-valor">
                    <%= pedidosActivos.stream()
                            .filter(p -> p.getEstado() == EstadoPedidoEnum.LISTO)
                            .count() %>
                </div>
                <div class="stat-label">Listos para recoger</div>
            </div>
        </div>
    </div>

    <div style="display:grid;grid-template-columns:1fr 1fr;gap:20px;">

        <!-- Pedidos activos -->
        <div class="card">
            <div class="card-header">
                <div class="card-title">Pedidos activos</div>
                <a href="${pageContext.request.contextPath}/empleado/dashboard"
                   class="btn btn-sm btn-ghost">Ver todos</a>
            </div>
            <div style="overflow-x:auto;">
                <table class="tabla">
                    <thead>
                    <tr>
                        <th>Folio</th>
                        <th>Tipo</th>
                        <th>Estado</th>
                        <th>Total</th>
                    </tr>
                    </thead>
                    <tbody>
                    <% if (pedidosActivos.isEmpty()) { %>
                    <tr>
                        <td colspan="4" style="text-align:center;
                                color:var(--uv-gris-500);padding:24px;">
                            Sin pedidos activos
                        </td>
                    </tr>
                    <% } %>
                    <% for (Pedido p : pedidosActivos.subList(0,
                            Math.min(5, pedidosActivos.size()))) { %>
                    <tr>
                        <td style="font-weight:600;font-family:var(--fuente-display);">
                            <%= p.getFolio() %>
                        </td>
                        <td>
                                <span style="font-size:.8rem;">
                                    <%= p.getTipo() == TipoPedidoEnum.ANTICIPADO
                                            ? " Anticipado" : " Inmediato" %>
                                </span>
                        </td>
                        <td>
                                <span class="estado-badge estado-<%= p.getEstado().name() %>">
                                    <%= p.getEstado().name() %>
                                </span>
                        </td>
                        <td style="font-weight:700;">
                            $<%= p.getTotal().toPlainString() %>
                        </td>
                    </tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Alertas de inventario -->
        <div class="card">
            <div class="card-header">
                <div class="card-title"> Alertas de inventario</div>
                <a href="${pageContext.request.contextPath}/admin/inventario/alertas"
                   class="btn btn-sm btn-ghost">Ver todas</a>
            </div>
            <div class="card-body" style="padding:0;">
                <% if (alertas.isEmpty()) { %>
                <div style="text-align:center;padding:32px;color:var(--uv-gris-500);">
                    <div style="font-size:2rem;margin-bottom:8px;">✅</div>
                    <div>Inventario en buen estado</div>
                </div>
                <% } %>
                <% for (AlertaInventario a : alertas.subList(0,
                        Math.min(5, alertas.size()))) { %>
                <div style="display:flex;align-items:center;gap:12px;
                            padding:12px 20px;border-bottom:1px solid var(--color-borde);">
                    <span style="font-size:1.3rem;">
                        <%= a.getNivel() == NivelAlertaEnum.CRITICO ? "🔴"
                                : a.getNivel() == NivelAlertaEnum.WARNING  ? "🟡" : "🔵" %>
                    </span>
                    <div style="flex:1;min-width:0;">
                        <div style="font-weight:600;font-size:.875rem;
                                    white-space:nowrap;overflow:hidden;
                                    text-overflow:ellipsis;">
                            <%= a.getMensaje() %>
                        </div>
                        <div style="font-size:.75rem;color:var(--uv-gris-500);">
                            <%= a.getTipo().name() %>
                        </div>
                    </div>
                    <form method="post"
                          action="${pageContext.request.contextPath}/admin/inventario/alerta/atender">
                        <input type="hidden" name="idAlerta" value="<%= a.getIdAlerta() %>">
                        <button type="submit" class="btn btn-sm btn-ghost">
                            Atender
                        </button>
                    </form>
                </div>
                <% } %>
            </div>
        </div>
    </div>

    <!-- Accesos rápidos -->
    <div style="margin-top:24px;">
        <div class="page-title" style="font-size:1rem;margin-bottom:14px;">
            Accesos rápidos
        </div>
        <div style="display:flex;flex-wrap:wrap;gap:12px;">
            <a href="${pageContext.request.contextPath}/admin/menu"
               class="btn btn-outline"> Gestionar menú</a>
            <a href="${pageContext.request.contextPath}/admin/usuarios"
               class="btn btn-outline"> Usuarios</a>
            <a href="${pageContext.request.contextPath}/admin/inventario"
               class="btn btn-outline"> Inventario</a>
            <a href="${pageContext.request.contextPath}/admin/inventario/compras"
               class="btn btn-outline"> Compras anticipadas</a>
            <a href="${pageContext.request.contextPath}/calificaciones"
               class="btn btn-outline"> Calificaciones</a>
        </div>
    </div>

</main>

<%@ include file="../_footer.jsp" %>
</body>
</html>
