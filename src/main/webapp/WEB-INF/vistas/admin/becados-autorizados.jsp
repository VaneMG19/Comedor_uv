<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="mx.uv.comedor.model.*" %>
<%@ page import="java.util.List" %>
<%
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    if (usuario == null || usuario.getRol() != RolEnum.ADMIN) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    List<BecadoAutorizado> becados = (List<BecadoAutorizado>) request.getAttribute("becados");
    if (becados == null) becados = new java.util.ArrayList<>();
    String exito = request.getParameter("exito");
    String error = request.getParameter("error");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1.0">
    <title>Becados Autorizados — Comedor UV</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body data-context-path="${pageContext.request.contextPath}">

<%@ include file="../_header.jsp" %>

<main class="page-wrapper">

    <div class="page-header d-flex justify-between align-center flex-wrap gap-2">
        <div>
            <div class="page-title">Becados Autorizados </div>
            <div class="page-subtitle">
                <%= becados.size() %> becados en lista —
                Pre-aprobados por la universidad
            </div>
        </div>
        <button class="btn btn-primario" onclick="toggleForm()">
            + Agregar becado
        </button>
    </div>

    <% if (exito != null) { %>
    <div class="alert alert-exito" data-auto-close> <%= exito %></div>
    <% } %>
    <% if (error != null) { %>
    <div class="alert alert-error" data-auto-close> <%= error %></div>
    <% } %>

    <!-- Form colapsable -->
    <div id="form-nuevo" class="card" style="display:none;margin-bottom:24px;">
        <div class="card-header">
            <div class="card-title">+ Agregar becado autorizado</div>
        </div>
        <div class="card-body">
            <form method="post" action="${pageContext.request.contextPath}/admin/becados/agregar">
                <div style="display:grid;grid-template-columns:1fr 1fr;gap:0 16px;">
                    <div class="form-group">
                        <label class="form-label" for="email">Correo institucional</label>
                        <input type="email" id="email" name="email" class="form-control"
                               placeholder="zS21000XXX@estudiantes.uv.mx" required>
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="matricula">Matrícula</label>
                        <input type="text" id="matricula" name="matricula" class="form-control"
                               placeholder="zS21000XXX" required>
                    </div>
                </div>
                <div class="form-group">
                    <label class="form-label" for="nombreCompleto">Nombre completo</label>
                    <input type="text" id="nombreCompleto" name="nombreCompleto" class="form-control"
                           placeholder="Nombre Apellido Paterno Apellido Materno" required>
                </div>
                <div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:0 16px;">
                    <div class="form-group">
                        <label class="form-label" for="tipoBeca">Tipo de beca</label>
                        <select id="tipoBeca" name="tipoBeca" class="form-control" required>
                            <option value="COMPLETA">Completa</option>
                            <option value="SOLO_COMIDA">Solo comida</option>
                            <option value="SOLO_DESAYUNO">Solo desayuno</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="comidasSemana">Comidas/semana</label>
                        <input type="number" id="comidasSemana" name="comidasSemana"
                               class="form-control" min="1" max="14" value="10" required>
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="vigenciaHasta">Vigencia hasta</label>
                        <input type="date" id="vigenciaHasta" name="vigenciaHasta"
                               class="form-control" required>
                    </div>
                </div>
                <div class="form-group">
                    <label class="form-label" for="notas">Notas (opcional)</label>
                    <input type="text" id="notas" name="notas" class="form-control"
                           placeholder="Información adicional sobre la beca">
                </div>
                <button type="submit" class="btn btn-primario"> Agregar a la lista</button>
                <button type="button" class="btn btn-ghost" onclick="toggleForm()">Cancelar</button>
            </form>
        </div>
    </div>

    <!-- Lista de becados -->
    <div class="card">
        <div style="overflow-x:auto;">
            <table class="tabla">
                <thead>
                    <tr>
                        <th>Matrícula</th>
                        <th>Nombre</th>
                        <th>Email</th>
                        <th>Tipo</th>
                        <th>Comidas/sem</th>
                        <th>Vigencia</th>
                        <th>Estado</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (becados.isEmpty()) { %>
                    <tr><td colspan="7" style="text-align:center;color:var(--uv-gris-500);padding:32px;">
                        Sin becados autorizados aún. Agrega uno con el botón de arriba.
                    </td></tr>
                    <% } else for (BecadoAutorizado b : becados) {
                        String claseEstado = "REGISTRADO".equals(b.getEstado()) ? "estado-LISTO"
                            : "PENDIENTE_REGISTRO".equals(b.getEstado()) ? "estado-PENDIENTE"
                            : "estado-CANCELADO";
                    %>
                    <tr>
                        <td style="font-family:var(--fuente-display);font-weight:700;">
                            <%= b.getMatricula() %>
                        </td>
                        <td><%= b.getNombreCompleto() %></td>
                        <td style="font-size:.85rem;"><%= b.getEmail() %></td>
                        <td>
                            <span style="background:var(--uv-verde-light);color:var(--uv-verde-dark);
                                         font-size:.7rem;font-weight:700;padding:3px 8px;border-radius:10px;">
                                <%= b.getTipoBeca() %>
                            </span>
                        </td>
                        <td style="text-align:center;font-weight:600;">
                            <%= b.getComidasSemana() %>
                        </td>
                        <td style="font-size:.8rem;">
                            <%= b.getVigenciaHasta() %>
                        </td>
                        <td>
                            <span class="estado-badge <%= claseEstado %>">
                                <%= b.getEstado() %>
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

<script>
function toggleForm() {
    const f = document.getElementById('form-nuevo');
    f.style.display = f.style.display === 'none' ? 'block' : 'none';
    if (f.style.display === 'block') f.scrollIntoView({behavior:'smooth'});
}
</script>
</body>
</html>
