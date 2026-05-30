<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="mx.uv.comedor.model.*" %>
<%@ page import="java.util.List" %>
<%
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    if (usuario == null || usuario.getRol() != RolEnum.ADMIN) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    List<Ingrediente> ingredientes = (List<Ingrediente>) request.getAttribute("ingredientes");
    if (ingredientes == null) ingredientes = new java.util.ArrayList<>();
    String exito = request.getParameter("exito");
    String error = request.getParameter("error");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1.0">
    <title>Inventario - Comedor UV</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
    <style>
        .inv-tabs {
            display: flex;
            background: white;
            border-radius: 12px;
            padding: 5px;
            gap: 4px;
            box-shadow: var(--sombra-sm);
            margin-bottom: 18px;
        }
        .inv-tab {
            padding: 10px 18px;
            border-radius: 8px;
            font-family: var(--fuente-display);
            font-weight: 600;
            font-size: .9rem;
            color: var(--uv-gris-700);
            text-decoration: none;
            transition: all .15s;
        }
        .inv-tab:hover { background: var(--uv-gris-100); }
        .inv-tab.activa { background: var(--uv-azul); color: white; }
        .stock-bajo { color: var(--uv-rojo); font-weight: 700; }
        .stock-ok   { color: var(--uv-verde); font-weight: 700; }

        .modal-overlay-inv {
            position: fixed; inset: 0;
            background: rgba(0,0,0,.5);
            display: none;
            align-items: center;
            justify-content: center;
            z-index: 1000;
            padding: 20px;
        }
        .modal-overlay-inv.visible { display: flex; }
        .modal-content-inv {
            background: white;
            border-radius: 16px;
            max-width: 600px;
            width: 100%;
            max-height: 90vh;
            overflow-y: auto;
            padding: 24px;
        }
        .form-row-inv {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 12px;
        }
        @media (max-width: 600px) { .form-row-inv { grid-template-columns: 1fr; } }
    </style>
</head>
<body data-context-path="${pageContext.request.contextPath}">

<%@ include file="../_header.jsp" %>

<main class="page-wrapper">

    <div class="page-header d-flex justify-between align-center flex-wrap gap-2">
        <div>
            <div class="page-title">Inventario</div>
            <div class="page-subtitle">
                <%= ingredientes.size() %> ingredientes registrados
            </div>
        </div>
        <div style="display:flex;gap:8px;">
            <button class="btn btn-primario" onclick="abrirModalIngrediente()">
                + Nuevo ingrediente
            </button>
        </div>
    </div>

    <% if (exito != null) { %>
    <div class="alert alert-exito" data-auto-close>Operacion realizada correctamente</div>
    <% } %>
    <% if (error != null) { %>
    <div class="alert alert-error" data-auto-close><%= error %></div>
    <% } %>

    <!-- Tabs -->
    <div class="inv-tabs">
        <a href="${pageContext.request.contextPath}/admin/inventario" class="inv-tab activa">
            Ingredientes
        </a>
        <a href="${pageContext.request.contextPath}/admin/inventario/movimientos" class="inv-tab">
            Movimientos
        </a>
    </div>

    <!-- Tabla de ingredientes -->
    <div class="card">
        <div style="overflow-x:auto;">
            <table class="tabla">
                <thead>
                <tr>
                    <th>Ingrediente</th>
                    <th>Stock actual</th>
                    <th>Stock minimo</th>
                    <th>Unidad</th>
                    <th>Precio unit.</th>
                    <th>Categoria</th>
                    <th>Estado</th>
                    <th>Acciones</th>
                </tr>
                </thead>
                <tbody>
                <% if (ingredientes.isEmpty()) { %>
                <tr>
                    <td colspan="8" style="text-align:center;color:var(--uv-gris-500);padding:40px;">
                        <div style="font-weight:600;margin-bottom:4px;">Sin ingredientes registrados</div>
                        <div style="font-size:.85rem;">
                            Comienza agregando tu primer ingrediente con el boton de arriba.
                        </div>
                    </td>
                </tr>
                <% } %>
                <% for (Ingrediente ing : ingredientes) {
                    boolean stockBajo = ing.getStockActual() != null
                            && ing.getStockMinimo() != null
                            && ing.getStockActual().compareTo(ing.getStockMinimo()) <= 0;
                %>
                <tr>
                    <td>
                        <div style="font-weight:600;"><%= ing.getNombre() %></div>
                        <% if (ing.getDescripcion() != null && !ing.getDescripcion().isEmpty()) { %>
                        <div style="font-size:.75rem;color:var(--uv-gris-500);">
                            <%= ing.getDescripcion() %>
                        </div>
                        <% } %>
                    </td>
                    <td class="<%= stockBajo ? "stock-bajo" : "stock-ok" %>">
                        <%= ing.getStockActual() != null ? ing.getStockActual().toPlainString() : "0" %>
                    </td>
                    <td><%= ing.getStockMinimo() != null ? ing.getStockMinimo().toPlainString() : "-" %></td>
                    <td><%= ing.getUnidadMedida() %></td>
                    <td>$<%= ing.getPrecioUnitario() != null ? ing.getPrecioUnitario().toPlainString() : "0.00" %></td>
                    <td>
                            <span style="font-size:.72rem;background:var(--uv-gris-200);padding:2px 8px;border-radius:10px;">
                                <%= ing.getCategoria() != null ? ing.getCategoria() : "-" %>
                            </span>
                    </td>
                    <td>
                        <% if (stockBajo) { %>
                        <span class="estado-badge estado-CANCELADO">STOCK BAJO</span>
                        <% } else { %>
                        <span class="estado-badge estado-LISTO">OK</span>
                        <% } %>
                    </td>
                    <td>
                        <button class="btn btn-ghost btn-sm"
                                onclick="abrirModalMovimiento(<%= ing.getIdIngrediente() %>, '<%= ing.getNombre().replace("'","\\'") %>', '<%= ing.getUnidadMedida() %>')">
                            Movimiento
                        </button>
                    </td>
                </tr>
                <% } %>
                </tbody>
            </table>
        </div>
    </div>

</main>

<!-- Modal: Nuevo ingrediente -->
<div class="modal-overlay-inv" id="modal-ingrediente">
    <div class="modal-content-inv">
        <h2 style="font-family:var(--fuente-display);color:var(--uv-azul);margin-bottom:16px;">
            Nuevo ingrediente
        </h2>
        <form method="post" action="${pageContext.request.contextPath}/admin/inventario/ingrediente/crear">

            <div class="form-group">
                <label class="form-label">Nombre *</label>
                <input type="text" name="nombre" class="form-control" required maxlength="100"
                       placeholder="Ej: Tomate, Arroz, Aceite...">
            </div>

            <div class="form-group">
                <label class="form-label">Descripcion</label>
                <textarea name="descripcion" class="form-control" rows="2" maxlength="300"
                          placeholder="Descripcion opcional"></textarea>
            </div>

            <div class="form-row-inv">
                <div class="form-group">
                    <label class="form-label">Unidad de medida *</label>
                    <select name="unidadMedida" class="form-control" required>
                        <option value="">-- Selecciona --</option>
                        <option value="kg">Kilogramos (kg)</option>
                        <option value="g">Gramos (g)</option>
                        <option value="L">Litros (L)</option>
                        <option value="ml">Mililitros (ml)</option>
                        <option value="pz">Piezas (pz)</option>
                        <option value="caja">Caja</option>
                        <option value="bulto">Bulto</option>
                    </select>
                </div>
                <div class="form-group">
                    <label class="form-label">Categoria</label>
                    <input type="text" name="categoria" class="form-control"
                           placeholder="Ej: Carnes, Verduras...">
                </div>
            </div>

            <div class="form-row-inv">
                <div class="form-group">
                    <label class="form-label">Stock inicial</label>
                    <input type="number" name="stockInicial" class="form-control"
                           step="0.01" min="0" placeholder="0">
                </div>
                <div class="form-group">
                    <label class="form-label">Stock minimo *</label>
                    <input type="number" name="stockMinimo" class="form-control"
                           step="0.01" min="0" required placeholder="5">
                </div>
            </div>

            <div class="form-row-inv">
                <div class="form-group">
                    <label class="form-label">Precio unitario *</label>
                    <input type="number" name="precioUnitario" class="form-control"
                           step="0.01" min="0" required placeholder="25.50">
                </div>
                <div class="form-group">
                    <label class="form-label">Proveedor</label>
                    <input type="text" name="proveedor" class="form-control"
                           placeholder="Nombre del proveedor">
                </div>
            </div>

            <input type="hidden" name="stockMaximo" value="">

            <div style="display:flex;gap:10px;margin-top:20px;">
                <button type="button" class="btn btn-ghost" style="flex:1;" onclick="cerrarModalIngrediente()">
                    Cancelar
                </button>
                <button type="submit" class="btn btn-primario" style="flex:2;">
                    Crear ingrediente
                </button>
            </div>
        </form>
    </div>
</div>

<!-- Modal: Movimiento de stock -->
<div class="modal-overlay-inv" id="modal-movimiento">
    <div class="modal-content-inv" style="max-width:480px;">
        <h2 style="font-family:var(--fuente-display);color:var(--uv-azul);margin-bottom:6px;">
            Registrar movimiento
        </h2>
        <div style="color:var(--uv-gris-500);font-size:.85rem;margin-bottom:16px;" id="mov-ingrediente-nombre">
            -
        </div>
        <form method="post" action="${pageContext.request.contextPath}/admin/inventario/movimiento/crear">
            <input type="hidden" name="idIngrediente" id="mov-idIngrediente">

            <div class="form-group">
                <label class="form-label">Tipo de movimiento *</label>
                <select name="tipo" class="form-control" required>
                    <option value="ENTRADA">Entrada (sumar stock)</option>
                    <option value="SALIDA">Salida (restar stock)</option>
                    <option value="MERMA">Merma (perdida/desperdicio)</option>
                    <option value="AJUSTE">Ajuste</option>
                </select>
            </div>

            <div class="form-row-inv">
                <div class="form-group">
                    <label class="form-label">Cantidad *</label>
                    <input type="number" name="cantidad" class="form-control"
                           step="0.01" min="0.01" required>
                </div>
                <div class="form-group">
                    <label class="form-label">Unidad</label>
                    <input type="text" class="form-control" id="mov-unidad" readonly
                           style="background:var(--uv-gris-100);">
                </div>
            </div>

            <div class="form-group">
                <label class="form-label">Motivo / Notas</label>
                <textarea name="motivo" class="form-control" rows="2"
                          placeholder="Ej: Compra al proveedor, Caducidad, Uso en cocina..."></textarea>
            </div>

            <div style="display:flex;gap:10px;margin-top:16px;">
                <button type="button" class="btn btn-ghost" style="flex:1;" onclick="cerrarModalMovimiento()">
                    Cancelar
                </button>
                <button type="submit" class="btn btn-primario" style="flex:2;">
                    Registrar movimiento
                </button>
            </div>
        </form>
    </div>
</div>

<%@ include file="../_footer.jsp" %>

<script>
    function abrirModalIngrediente() {
        document.getElementById('modal-ingrediente').classList.add('visible');
        document.body.style.overflow = 'hidden';
    }
    function cerrarModalIngrediente() {
        document.getElementById('modal-ingrediente').classList.remove('visible');
        document.body.style.overflow = '';
    }
    function abrirModalMovimiento(id, nombre, unidad) {
        document.getElementById('mov-idIngrediente').value = id;
        document.getElementById('mov-ingrediente-nombre').textContent = nombre;
        document.getElementById('mov-unidad').value = unidad;
        document.getElementById('modal-movimiento').classList.add('visible');
        document.body.style.overflow = 'hidden';
    }
    function cerrarModalMovimiento() {
        document.getElementById('modal-movimiento').classList.remove('visible');
        document.body.style.overflow = '';
    }
</script>

</body>
</html>
