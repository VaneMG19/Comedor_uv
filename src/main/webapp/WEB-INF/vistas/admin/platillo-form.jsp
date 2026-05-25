<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="mx.uv.comedor.model.*" %>
<%
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    if (usuario == null || usuario.getRol() != RolEnum.ADMIN) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    Platillo platillo = (Platillo) request.getAttribute("platillo");
    String modo = (String) request.getAttribute("modo");
    boolean esNuevo = "nuevo".equals(modo);
    if (platillo == null) platillo = new Platillo();

    InformacionNutricional nutri = platillo.getInformacionNutricional();
    if (nutri == null) nutri = new InformacionNutricional();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1.0">
    <title><%= esNuevo ? "Nuevo Platillo" : "Editar Platillo" %> — Comedor UV</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
    <style>
        .form-secciones {
            display: grid;
            grid-template-columns: 1fr;
            gap: 16px;
        }
        @media (min-width: 900px) {
            .form-secciones { grid-template-columns: 1.3fr 1fr; }
        }
        .form-row {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 12px;
        }
        @media (max-width: 600px) { .form-row { grid-template-columns: 1fr; } }
        .check-row {
            display: flex;
            flex-wrap: wrap;
            gap: 14px;
            margin-top: 8px;
        }
        .check-row label {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: .9rem;
            cursor: pointer;
        }
    </style>
</head>
<body data-context-path="${pageContext.request.contextPath}">

<%@ include file="../_header.jsp" %>

<main class="page-wrapper" style="max-width:1100px;">

    <div style="margin-bottom:12px;">
        <a href="${pageContext.request.contextPath}/admin/menu"
           style="font-size:.85rem;color:var(--uv-gris-500);">
            ← Volver al menú
        </a>
    </div>

    <div class="page-header">
        <div class="page-title">
            <%= esNuevo ? " Nuevo platillo" : " Editar platillo" %>
        </div>
        <div class="page-subtitle">
            <%= esNuevo ? "Agrega un nuevo platillo al catálogo del comedor"
                        : "Editando \"" + platillo.getNombre() + "\"" %>
        </div>
    </div>

    <form method="post" action="${pageContext.request.contextPath}/admin/platillos/guardar">
        <% if (!esNuevo) { %>
        <input type="hidden" name="idPlatillo" value="<%= platillo.getIdPlatillo() %>">
        <% } %>

        <div class="form-secciones">

            <!-- IZQUIERDA: Datos básicos -->
            <div>
                <div class="card">
                    <div class="card-header">
                        <div class="card-title">Datos básicos</div>
                    </div>
                    <div class="card-body">
                        <div class="form-group">
                            <label class="form-label">Nombre del platillo *</label>
                            <input type="text" name="nombre" class="form-control"
                                   value="<%= platillo.getNombre() != null ? platillo.getNombre() : "" %>"
                                   required maxlength="200">
                        </div>

                        <div class="form-group">
                            <label class="form-label">Descripción</label>
                            <textarea name="descripcion" class="form-control" rows="3"
                                      maxlength="500"
                                      placeholder="Describe el platillo, ingredientes, etc."><%= platillo.getDescripcion() != null ? platillo.getDescripcion() : "" %></textarea>
                        </div>

                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Tipo *</label>
                                <select name="tipo" class="form-control" required>
                                    <option value="MENU" <%= platillo.getTipo() == TipoPlatEnum.MENU ? "selected" : "" %>>Menú del Día</option>
                                    <option value="CARTA" <%= platillo.getTipo() == TipoPlatEnum.CARTA ? "selected" : "" %>>A la Carta</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label class="form-label">Categoría *</label>
                                <select name="categoria" class="form-control" required>
                                    <% for (CategoriaPlatEnum cat : CategoriaPlatEnum.values()) { %>
                                    <option value="<%= cat.name() %>"
                                            <%= platillo.getCategoria() == cat ? "selected" : "" %>>
                                        <%= cat.getEtiqueta() %>
                                    </option>
                                    <% } %>
                                </select>
                            </div>
                        </div>

                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Precio normal ($) *</label>
                                <input type="number" name="precio" class="form-control"
                                       step="0.01" min="0.01" required
                                       value="<%= platillo.getPrecio() != null ? platillo.getPrecio().toPlainString() : "" %>">
                            </div>
                            <div class="form-group">
                                <label class="form-label">Precio subsidiado ($)</label>
                                <input type="number" name="precioSubsidiado" class="form-control"
                                       step="0.01" min="0"
                                       value="<%= platillo.getPrecioSubsidiado() != null ? platillo.getPrecioSubsidiado().toPlainString() : "" %>"
                                       placeholder="Solo si aplica descuento">
                            </div>
                        </div>

                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Tiempo de preparación (min)</label>
                                <input type="number" name="tiempoPrep" class="form-control"
                                       min="1" max="120"
                                       value="<%= platillo.getTiempoPrep() > 0 ? platillo.getTiempoPrep() : 15 %>">
                            </div>
                            <div class="form-group">
                                <label class="form-label">URL de imagen</label>
                                <input type="text" name="imagen" class="form-control"
                                       value="<%= platillo.getImagen() != null ? platillo.getImagen() : "" %>"
                                       placeholder="https://...">
                            </div>
                        </div>

                        <div class="form-group">
                            <label style="display:flex;align-items:center;gap:8px;cursor:pointer;">
                                <input type="checkbox" name="disponible"
                                       <%= esNuevo || platillo.isDisponible() ? "checked" : "" %>>
                                <span>Disponible para venta</span>
                            </label>
                        </div>
                    </div>
                </div>
            </div>

            <!-- DERECHA: Información nutricional -->
            <div>
                <div class="card">
                    <div class="card-header">
                        <div class="card-title"> Información nutricional (opcional)</div>
                    </div>
                    <div class="card-body">
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Calorías (kcal)</label>
                                <input type="number" name="calorias" class="form-control" step="0.1" min="0"
                                       value="<%= nutri.getCalorias() != null ? nutri.getCalorias().toPlainString() : "" %>">
                            </div>
                            <div class="form-group">
                                <label class="form-label">Proteínas (g)</label>
                                <input type="number" name="proteinas" class="form-control" step="0.1" min="0"
                                       value="<%= nutri.getProteinas() != null ? nutri.getProteinas().toPlainString() : "" %>">
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Carbohidratos (g)</label>
                                <input type="number" name="carbohidratos" class="form-control" step="0.1" min="0"
                                       value="<%= nutri.getCarbohidratos() != null ? nutri.getCarbohidratos().toPlainString() : "" %>">
                            </div>
                            <div class="form-group">
                                <label class="form-label">Grasas (g)</label>
                                <input type="number" name="grasas" class="form-control" step="0.1" min="0"
                                       value="<%= nutri.getGrasas() != null ? nutri.getGrasas().toPlainString() : "" %>">
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Fibra (g)</label>
                                <input type="number" name="fibra" class="form-control" step="0.1" min="0"
                                       value="<%= nutri.getFibra() != null ? nutri.getFibra().toPlainString() : "" %>">
                            </div>
                            <div class="form-group">
                                <label class="form-label">Sodio (mg)</label>
                                <input type="number" name="sodio" class="form-control" step="0.1" min="0"
                                       value="<%= nutri.getSodio() != null ? nutri.getSodio().toPlainString() : "" %>">
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Azúcar (g)</label>
                                <input type="number" name="azucar" class="form-control" step="0.1" min="0"
                                       value="<%= nutri.getAzucar() != null ? nutri.getAzucar().toPlainString() : "" %>">
                            </div>
                            <div class="form-group">
                                <label class="form-label">Huella carbono (kg CO₂)</label>
                                <input type="number" name="huellaCarbonoKg" class="form-control" step="0.001" min="0"
                                       value="<%= nutri.getHuellaCarbonoKg() != null ? nutri.getHuellaCarbonoKg().toPlainString() : "" %>">
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="form-label">Alérgenos</label>
                            <input type="text" name="alergenos" class="form-control"
                                   value="<%= nutri.getAlergenos() != null ? nutri.getAlergenos() : "" %>"
                                   placeholder="Ej: gluten, lácteos, frutos secos">
                        </div>
                        <div class="check-row">
                            <label>
                                <input type="checkbox" name="esVegetariano"
                                       <%= nutri.isEsVegetariano() ? "checked" : "" %>>
                                 Vegetariano
                            </label>
                            <label>
                                <input type="checkbox" name="esVegano"
                                       <%= nutri.isEsVegano() ? "checked" : "" %>>
                                Vegano
                            </label>
                            <label>
                                <input type="checkbox" name="esGlutenFree"
                                       <%= nutri.isEsGlutenFree() ? "checked" : "" %>>
                                Sin gluten
                            </label>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div style="display:flex;gap:12px;margin-top:20px;">
            <button type="submit" class="btn btn-primario btn-lg">
                <%= esNuevo ? "Crear platillo" : "Guardar cambios" %>
            </button>
            <a href="${pageContext.request.contextPath}/admin/menu" class="btn btn-ghost btn-lg">
                Cancelar
            </a>
        </div>
    </form>

</main>

<%@ include file="../_footer.jsp" %>
</body>
</html>
