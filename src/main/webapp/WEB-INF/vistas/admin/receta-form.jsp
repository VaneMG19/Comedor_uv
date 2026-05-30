<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="mx.uv.comedor.model.*" %>
<%@ page import="java.util.List" %>
<%
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    if (usuario == null || usuario.getRol() != RolEnum.ADMIN) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    Platillo platillo = (Platillo) request.getAttribute("platillo");
    List<PlatilloIngrediente> receta = (List<PlatilloIngrediente>) request.getAttribute("receta");
    List<Ingrediente> ingredientes = (List<Ingrediente>) request.getAttribute("ingredientes");
    if (receta == null) receta = new java.util.ArrayList<>();
    if (ingredientes == null) ingredientes = new java.util.ArrayList<>();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Receta de <%= platillo.getNombre() %> - Comedor UV</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
    <style>
        .ing-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 12px 16px;
            background: var(--uv-gris-100);
            border-radius: 10px;
            margin-bottom: 8px;
        }
        .ing-item .info { flex: 1; }
        .ing-item .nombre {
            font-weight: 700;
            font-size: .95rem;
        }
        .ing-item .stock {
            font-size: .72rem;
            color: var(--uv-gris-500);
            margin-top: 2px;
        }
        .ing-item .cantidad {
            font-family: var(--fuente-display);
            font-weight: 800;
            font-size: 1.05rem;
            color: var(--uv-azul);
            min-width: 100px;
            text-align: right;
            margin-right: 14px;
        }
        .sin-receta {
            text-align: center;
            padding: 30px;
            color: var(--uv-gris-500);
            font-style: italic;
            background: var(--uv-gris-100);
            border-radius: 10px;
        }
    </style>
</head>
<body data-context-path="${pageContext.request.contextPath}">

<%@ include file="../_header.jsp" %>

<main class="page-wrapper" style="max-width:900px;">

    <div style="margin-bottom:12px;">
        <a href="${pageContext.request.contextPath}/admin/recetas"
           style="font-size:.85rem;color:var(--uv-gris-500);">
            &larr; Volver a recetas
        </a>
    </div>

    <div class="page-header">
        <div class="page-title">Receta: <%= platillo.getNombre() %></div>
        <div class="page-subtitle">
            Define los ingredientes que se usan para preparar UNA porcion.
            Cuando se venda este platillo, el inventario se descontara automaticamente.
        </div>
    </div>

    <div class="card">
        <div class="card-header">
            <div class="card-title">Ingredientes asignados (<%= receta.size() %>)</div>
        </div>
        <div class="card-body">

            <% if (receta.isEmpty()) { %>
            <div class="sin-receta">
                Aun no has agregado ingredientes a la receta de este platillo.<br>
                Usa el formulario de abajo para empezar.
            </div>
            <% } else {
                for (PlatilloIngrediente pi : receta) {
                    java.math.BigDecimal stock = pi.getStockActualIngrediente();
                    boolean stockBajo = stock != null && pi.getCantidad() != null
                                         && stock.compareTo(pi.getCantidad()) < 0;
            %>
            <div class="ing-item">
                <div class="info">
                    <div class="nombre"><%= pi.getNombreIngrediente() %></div>
                    <div class="stock <%= stockBajo ? "stock-bajo" : "" %>"
                         style="<%= stockBajo ? "color:var(--uv-rojo);font-weight:700;" : "" %>">
                        Stock actual:
                        <%= stock != null ? stock.toPlainString() : "0" %> <%= pi.getUnidadIngrediente() %>
                    </div>
                </div>
                <div class="cantidad">
                    <%= pi.getCantidad().toPlainString() %> <%= pi.getUnidadIngrediente() %>
                </div>
                <form method="post" action="${pageContext.request.contextPath}/admin/recetas/quitar"
                      style="display:inline;"
                      onsubmit="return confirm('Quitar este ingrediente de la receta?');">
                    <input type="hidden" name="idPlatillo" value="<%= platillo.getIdPlatillo() %>">
                    <input type="hidden" name="idReceta" value="<%= pi.getIdReceta() %>">
                    <button type="submit" class="btn btn-ghost btn-sm" style="color:var(--uv-rojo);">
                        Quitar
                    </button>
                </form>
            </div>
            <%   } } %>

        </div>
    </div>

    <div class="card mt-3">
        <div class="card-header">
            <div class="card-title">Agregar ingrediente a la receta</div>
        </div>
        <div class="card-body">
            <% if (ingredientes.isEmpty()) { %>
            <div class="alert alert-info">
                Aun no hay ingredientes registrados en el inventario.
                <a href="${pageContext.request.contextPath}/admin/inventario"
                   style="color:var(--uv-azul);font-weight:600;">
                    Agregar ingredientes
                </a>.
            </div>
            <% } else { %>
            <form method="post" action="${pageContext.request.contextPath}/admin/recetas/agregar"
                  style="display:grid;grid-template-columns:2fr 1fr auto;gap:10px;align-items:end;">
                <input type="hidden" name="idPlatillo" value="<%= platillo.getIdPlatillo() %>">

                <div>
                    <label class="form-label">Ingrediente</label>
                    <select name="idIngrediente" class="form-control" required>
                        <option value="">-- Selecciona un ingrediente --</option>
                        <%
                            for (Ingrediente ing : ingredientes) {
                                // Evitar mostrar los que ya estan en la receta
                                boolean yaEsta = false;
                                for (PlatilloIngrediente pi : receta) {
                                    if (pi.getIdIngrediente().equals(ing.getIdIngrediente())) {
                                        yaEsta = true; break;
                                    }
                                }
                                if (yaEsta) continue;
                        %>
                        <option value="<%= ing.getIdIngrediente() %>"
                                data-unidad="<%= ing.getUnidadMedida() %>">
                            <%= ing.getNombre() %> (<%= ing.getUnidadMedida() %>)
                        </option>
                        <% } %>
                    </select>
                </div>
                <div>
                    <label class="form-label">Cantidad por porcion</label>
                    <input type="number" name="cantidad" class="form-control"
                           step="0.001" min="0.001" required placeholder="0.150">
                </div>
                <div>
                    <button type="submit" class="btn btn-primario">+ Agregar</button>
                </div>
            </form>
            <div style="font-size:.8rem;color:var(--uv-gris-500);margin-top:10px;">
                <strong>Ejemplo:</strong> Si "Arroz con pollo" usa 150 g de arroz, escribe <code>0.150</code> con unidad kg.
                Cuando se venda 1 porcion, se descontaran 0.150 kg de arroz.
            </div>
            <% } %>
        </div>
    </div>

</main>

<%@ include file="../_footer.jsp" %>
</body>
</html>
