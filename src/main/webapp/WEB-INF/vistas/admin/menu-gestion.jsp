<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="mx.uv.comedor.model.*" %>
<%@ page import="mx.uv.comedor.dao.*" %>
<%@ page import="java.util.List" %>
<%
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    if (usuario == null || usuario.getRol() != RolEnum.ADMIN) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    PlatilloDAO platilloDAO = new PlatilloDAO();
    List<Platillo> todos = platilloDAO.listarTodos();

    List<Platillo> menuDelDia = new java.util.ArrayList<>();
    List<Platillo> aLaCarta   = new java.util.ArrayList<>();
    for (Platillo p : todos) {
        if (p.getTipo() == TipoPlatEnum.MENU)        menuDelDia.add(p);
        else if (p.getTipo() == TipoPlatEnum.CARTA)  aLaCarta.add(p);
    }
    String exito = request.getParameter("exito");
    String error = request.getParameter("error");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1.0">
    <title>Gestion de Menu - Comedor UV</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
    <style>
        .cat-pill {
            display: inline-block;
            font-size: .68rem;
            font-weight: 700;
            padding: 2px 8px;
            border-radius: 10px;
            background: var(--uv-gris-200);
            color: var(--uv-gris-700);
        }
        .acciones-cell {
            display: flex;
            gap: 6px;
            flex-wrap: wrap;
        }
        .acciones-cell .btn {
            padding: 4px 10px;
            font-size: .75rem;
        }
    </style>
</head>
<body data-context-path="${pageContext.request.contextPath}">

<%@ include file="../_header.jsp" %>

<main class="page-wrapper">

    <div class="page-header d-flex justify-between align-center flex-wrap gap-2">
        <div>
            <div class="page-title">Gestion de Menu</div>
            <div class="page-subtitle">
                <%= todos.size() %> platillos -
                <%= menuDelDia.size() %> del dia - <%= aLaCarta.size() %> a la carta
            </div>
        </div>
        <a href="${pageContext.request.contextPath}/admin/platillos/nuevo"
           class="btn btn-primario">
            + Nuevo platillo
        </a>
    </div>

    <% if (exito != null) { %>
    <div class="alert alert-exito" data-auto-close><%= exito %></div>
    <% } %>
    <% if (error != null) { %>
    <div class="alert alert-error" data-auto-close><%= error %></div>
    <% } %>

    <div class="card">
        <div class="card-header">
            <div class="card-title">Menu del Dia (<%= menuDelDia.size() %>)</div>
        </div>
        <div style="overflow-x:auto;">
            <table class="tabla">
                <thead>
                <tr>
                    <th>Platillo</th>
                    <th>Categoria</th>
                    <th>Precio</th>
                    <th>P. Subsidiado</th>
                    <th>Tiempo</th>
                    <th>Estado</th>
                    <th>Acciones</th>
                </tr>
                </thead>
                <tbody>
                <% if (menuDelDia.isEmpty()) { %>
                <tr><td colspan="7" style="text-align:center;color:var(--uv-gris-500);padding:24px;">
                    Sin platillos en el menu del dia
                </td></tr>
                <% } %>
                <% for (Platillo p : menuDelDia) { %>
                <tr>
                    <td>
                        <div style="font-weight:600;"><%= p.getNombre() %></div>
                        <% if (p.getDescripcion() != null) { %>
                        <div style="font-size:.75rem;color:var(--uv-gris-500);">
                            <%= p.getDescripcion() %>
                        </div>
                        <% } %>
                    </td>
                    <td>
                            <span class="cat-pill">
                                <%= p.getCategoria() != null ? p.getCategoria().getEtiqueta() : "-" %>
                            </span>
                    </td>
                    <td style="font-weight:700;color:var(--uv-azul);">
                        $<%= p.getPrecio().toPlainString() %>
                    </td>
                    <td><%= p.getPrecioSubsidiado() != null
                            ? "$" + p.getPrecioSubsidiado().toPlainString() : "-" %></td>
                    <td><%= p.getTiempoPrep() %> min</td>
                    <td>
                            <span class="estado-badge <%= p.isDisponible() ? "estado-LISTO" : "estado-CANCELADO" %>">
                                <%= p.isDisponible() ? "DISPONIBLE" : "NO DISP." %>
                            </span>
                    </td>
                    <td class="acciones-cell">
                        <a href="${pageContext.request.contextPath}/admin/platillos/editar?id=<%= p.getIdPlatillo() %>"
                           class="btn btn-ghost">Editar</a>
                        <form method="post" action="${pageContext.request.contextPath}/admin/platillos/toggle" style="display:inline;">
                            <input type="hidden" name="idPlatillo" value="<%= p.getIdPlatillo() %>">
                            <button type="submit" class="btn btn-ghost"
                                    style="color:<%= p.isDisponible() ? "var(--uv-rojo)" : "var(--uv-verde)" %>;">
                                <%= p.isDisponible() ? "Desactivar" : "Activar" %>
                            </button>
                        </form>
                    </td>
                </tr>
                <% } %>
                </tbody>
            </table>
        </div>
    </div>

    <div class="card mt-4">
        <div class="card-header">
            <div class="card-title">A la Carta (<%= aLaCarta.size() %>)</div>
        </div>
        <div style="overflow-x:auto;">
            <table class="tabla">
                <thead>
                <tr>
                    <th>Platillo</th>
                    <th>Categoria</th>
                    <th>Precio</th>
                    <th>Tiempo</th>
                    <th>Estado</th>
                    <th>Acciones</th>
                </tr>
                </thead>
                <tbody>
                <% if (aLaCarta.isEmpty()) { %>
                <tr><td colspan="6" style="text-align:center;color:var(--uv-gris-500);padding:24px;">
                    Sin platillos a la carta
                </td></tr>
                <% } %>
                <% for (Platillo p : aLaCarta) { %>
                <tr>
                    <td>
                        <div style="font-weight:600;"><%= p.getNombre() %></div>
                        <% if (p.getDescripcion() != null) { %>
                        <div style="font-size:.75rem;color:var(--uv-gris-500);">
                            <%= p.getDescripcion() %>
                        </div>
                        <% } %>
                    </td>
                    <td>
                            <span class="cat-pill">
                                <%= p.getCategoria() != null ? p.getCategoria().getEtiqueta() : "-" %>
                            </span>
                    </td>
                    <td style="font-weight:700;color:var(--uv-azul);">
                        $<%= p.getPrecio().toPlainString() %>
                    </td>
                    <td><%= p.getTiempoPrep() %> min</td>
                    <td>
                            <span class="estado-badge <%= p.isDisponible() ? "estado-LISTO" : "estado-CANCELADO" %>">
                                <%= p.isDisponible() ? "DISPONIBLE" : "NO DISP." %>
                            </span>
                    </td>
                    <td class="acciones-cell">
                        <a href="${pageContext.request.contextPath}/admin/platillos/editar?id=<%= p.getIdPlatillo() %>"
                           class="btn btn-ghost">Editar</a>
                        <form method="post" action="${pageContext.request.contextPath}/admin/platillos/toggle" style="display:inline;">
                            <input type="hidden" name="idPlatillo" value="<%= p.getIdPlatillo() %>">
                            <button type="submit" class="btn btn-ghost"
                                    style="color:<%= p.isDisponible() ? "var(--uv-rojo)" : "var(--uv-verde)" %>;">
                                <%= p.isDisponible() ? "Desactivar" : "Activar" %>
                            </button>
                        </form>
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
