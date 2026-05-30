<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="mx.uv.comedor.model.*" %>
<%
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    if (usuario == null) { response.sendRedirect(request.getContextPath() + "/login"); return; }
    Pedido pedido = (Pedido) request.getAttribute("pedido");
    if (pedido == null) { response.sendRedirect(request.getContextPath() + "/pedido/historial"); return; }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1.0">
    <title>Calificar Pedido - Comedor UV</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
    <style>
        .platillo-rating-card {
            background: white;
            border-radius: var(--radio-lg);
            padding: 24px;
            box-shadow: var(--sombra-sm);
            margin-bottom: 16px;
        }
        .estrellas-input {
            display: inline-flex;
            flex-direction: row-reverse;
            gap: 4px;
            margin: 8px 0;
        }
        .estrellas-input input[type="radio"] {
            position: absolute;
            opacity: 0;
            width: 0; height: 0;
        }
        .estrellas-input label {
            font-size: 2.2rem;
            cursor: pointer;
            color: var(--uv-gris-300);
            transition: color.15s;
            line-height: 1;
        }
        .estrellas-input label:hover,
        .estrellas-input label:hover ~ label,
        .estrellas-input input[type="radio"]:checked ~ label {
            color: var(--uv-amarillo);
        }
        .platillo-rating-card h3 {
            font-family: var(--fuente-display);
            color: var(--uv-azul);
            font-size: 1.1rem;
            margin-bottom: 6px;
        }
        .platillo-rating-card.desc {
            color: var(--uv-gris-500);
            font-size:.85rem;
            margin-bottom: 12px;
        }
    </style>
</head>
<body data-context-path="${pageContext.request.contextPath}">

<%@ include file="_header.jsp" %>

<main class="page-wrapper" style="max-width:780px;">

    <div style="margin-bottom:12px;">
        <a href="${pageContext.request.contextPath}/pedido/detalle?id=<%= pedido.getIdPedido() %>"
           style="font-size:.85rem;color:var(--uv-gris-500);">Volver al pedido</a>
    </div>

    <div class="page-header">
        <div class="page-title">Califica tu pedido ⭐</div>
        <div class="page-subtitle">
            Pedido <strong><%= pedido.getFolio() %></strong> -
            Cuéntanos qué te pareció cada platillo
        </div>
    </div>

    <form method="post" action="${pageContext.request.contextPath}/calificar/enviar" id="form-calif">
        <input type="hidden" name="idPedido" value="<%= pedido.getIdPedido() %>">

        <%
            int idx = 0;
            boolean hayAlgo = false;
            if (pedido.getDetalles()!= null) {
                for (DetallePedido d : pedido.getDetalles()) {
                    // CRÍTICO: validar que el platillo exista
                    if (d.getIdPlatillo() == null || d.getIdPlatillo() <= 0) continue;
                    hayAlgo = true;
                    idx++;
                    String nombre = (d.getPlatillo()!= null && d.getPlatillo().getNombre()!= null)
                            ? d.getPlatillo().getNombre()
                            : "Platillo #" + d.getIdPlatillo();
                    String descripcion = (d.getPlatillo()!= null)? d.getPlatillo().getDescripcion() : null;
                    Long idP = d.getIdPlatillo();
        %>
        <div class="platillo-rating-card">
            <h3> <%= nombre %></h3>
            <% if (descripcion!= null &&!descripcion.isBlank()) { %>
            <div class="desc"><%= descripcion %></div>
            <% } %>

            <!-- ID del platillo como hidden por si acaso -->
            <input type="hidden" name="platillos" value="<%= idP %>">

            <label class="form-label">¿Cómo te pareció?</label>
            <div class="estrellas-input">
                <input type="radio" name="puntuacion_<%= idP %>" id="p<%= idP %>_5" value="5" required>
                <label for="p<%= idP %>_5" title="Excelente">★</label>
                <input type="radio" name="puntuacion_<%= idP %>" id="p<%= idP %>_4" value="4">
                <label for="p<%= idP %>_4" title="Muy bueno">★</label>
                <input type="radio" name="puntuacion_<%= idP %>" id="p<%= idP %>_3" value="3">
                <label for="p<%= idP %>_3" title="Bueno">★</label>
                <input type="radio" name="puntuacion_<%= idP %>" id="p<%= idP %>_2" value="2">
                <label for="p<%= idP %>_2" title="Regular">★</label>
                <input type="radio" name="puntuacion_<%= idP %>" id="p<%= idP %>_1" value="1">
                <label for="p<%= idP %>_1" title="Malo">★</label>
            </div>

            <div class="form-group" style="margin-top:8px;">
                <label class="form-label" for="c<%= idP %>">Comentario (opcional)</label>
                <textarea id="c<%= idP %>" name="comentario_<%= idP %>"
                          class="form-control" rows="2"
                          placeholder="Cuéntanos más sobre tu experiencia..."
                          maxlength="500"></textarea>
            </div>
        </div>
        <% } } %>

        <% if (!hayAlgo) { %>
        <div class="alert alert-error">
            No se encontraron platillos válidos en este pedido para calificar.
        </div>
        <% } else { %>
        <div style="display:flex;gap:12px;margin-top:20px;">
            <button type="submit" class="btn btn-primario btn-lg" style="flex:1;">
                ⭐ Enviar reseñas
            </button>
            <a href="${pageContext.request.contextPath}/pedido/detalle?id=<%= pedido.getIdPedido() %>"
               class="btn btn-ghost btn-lg">Cancelar</a>
        </div>
        <% } %>
    </form>

</main>

<%@ include file="_footer.jsp" %>
</body>
</html>
