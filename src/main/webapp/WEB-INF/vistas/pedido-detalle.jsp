<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="mx.uv.comedor.model.*" %>
<%@ page import="mx.uv.comedor.dao.CalificacionDAO" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    if (usuario == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    Pedido pedido = (Pedido) request.getAttribute("pedido");
    if (pedido == null) {
        response.sendRedirect(request.getContextPath() + "/pedido/historial");
        return;
    }
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    String exito = request.getParameter("exito");
    String info  = request.getParameter("info");
    boolean recienCreado = "creado".equals(exito);

    // Saber si se puede calificar
    boolean puedeCalificar = false;
    boolean yaCalificado  = false;
    if (pedido.getEstado() == EstadoPedidoEnum.ENTREGADO
            || pedido.getEstado() == EstadoPedidoEnum.LISTO) {
        CalificacionDAO _cdao = new CalificacionDAO();
        yaCalificado = _cdao.pedidoCalificado(pedido.getIdPedido());
        puedeCalificar =!yaCalificado;
    }

    // Formatear método de pago
    String metodoPagoTexto = "-";
    String metodoPagoIcono = "";
    if (pedido.getMetodoPagoDisplay()!= null) {
        switch (pedido.getMetodoPagoDisplay()) {
            case "EFECTIVO": metodoPagoTexto = "Efectivo"; metodoPagoIcono = ""; break;
            case "TARJETA": metodoPagoTexto = "Tarjeta"; metodoPagoIcono = ""; break;
            case "BECA": metodoPagoTexto = "Beca alimentaria"; metodoPagoIcono = ""; break;
            case "MIXTO": metodoPagoTexto = "Pago mixto"; metodoPagoIcono = ""; break;
            case "TRANSFERENCIA": metodoPagoTexto = "Transferencia"; metodoPagoIcono = ""; break;
            default: metodoPagoTexto = pedido.getMetodoPagoDisplay();
        }
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1.0">
    <title>Pedido <%= pedido.getFolio()!= null? pedido.getFolio() : "" %> - Comedor UV</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
    <style>
        .exito-banner {
            background: linear-gradient(135deg, var(--uv-verde-light), #d4f2e0);
            border-left: 4px solid var(--uv-verde);
            padding: 20px 24px; border-radius: var(--radio-lg);
            margin-bottom: 24px; display: flex; align-items: center; gap: 16px;
        }
        .exito-icono { font-size: 2.5rem; line-height: 1; }
        .exito-titulo {
            font-family: var(--fuente-display);
            font-size: 1.2rem; font-weight: 800;
            color: var(--uv-verde-dark); margin-bottom: 2px;
        }
        .exito-texto { color: var(--uv-gris-700); font-size:.9rem; }
        .calificar-banner {
            background: linear-gradient(135deg, var(--uv-amarillo-light), #fff5cc);
            border-left: 4px solid var(--uv-amarillo);
            padding: 20px 24px; border-radius: var(--radio-lg);
            margin-bottom: 24px;
            display: flex; align-items: center; gap: 16px; flex-wrap: wrap;
        }
        .qr-section {
            background: white; border-radius: var(--radio-lg);
            padding: 24px; text-align: center; box-shadow: var(--sombra-sm);
            margin-bottom: 20px;
        }
        .qr-section img {
            max-width: 240px; width: 100%;
            border: 1px solid var(--color-borde);
            border-radius: 12px; padding: 12px;
        }
        .qr-instrucciones {
            margin-top: 12px; color: var(--uv-gris-500);
            font-size:.85rem; line-height: 1.5;
        }
        .resumen-grid {
            display: grid; grid-template-columns: 1.5fr 1fr; gap: 20px;
        }
        @media (max-width: 768px) {.resumen-grid { grid-template-columns: 1fr; } }
        .info-box {
            background: white; border-radius: var(--radio-lg);
            padding: 20px; box-shadow: var(--sombra-sm);
            margin-bottom: 16px;
        }
        .info-titulo {
            font-family: var(--fuente-display); font-weight: 700;
            color: var(--uv-azul); font-size: 1rem;
            margin-bottom: 12px;
            display: flex; align-items: center; gap: 8px;
        }
        .platillo-fila {
            display: flex; justify-content: space-between;
            align-items: flex-start; padding: 12px 0;
            border-bottom: 1px solid var(--color-borde);
        }
        .platillo-fila:last-child { border-bottom: none; }
        .platillo-info { flex: 1; }
        .platillo-nombre { font-weight: 600; font-size:.95rem; }
        .platillo-descripcion {
            font-size:.78rem; color: var(--uv-gris-500);
            margin-top: 2px; font-style: italic;
        }
        .platillo-cantidad {
            font-size:.8rem; color: var(--uv-gris-500); margin-top: 4px;
        }
        .platillo-precio {
            font-weight: 700; color: var(--uv-azul); font-size: 1rem;
        }
        .totales-fila { display: flex; justify-content: space-between; padding: 8px 0; font-size:.9rem; }
        .totales-fila.total {
            border-top: 2px solid var(--uv-azul);
            padding-top: 12px; margin-top: 8px;
            font-size: 1.1rem; font-weight: 800; color: var(--uv-azul);
        }
        .badge-beca {
            display: inline-block; background: var(--uv-verde-light);
            color: var(--uv-verde-dark); font-size:.68rem; font-weight: 700;
            padding: 2px 6px; border-radius: 4px; margin-left: 6px;
        }
        .pago-card {
            background: linear-gradient(135deg, var(--uv-azul-light), #e8f1fc);
            border-radius: 14px; padding: 16px;
            display: flex; align-items: center; gap: 14px; margin-bottom: 16px;
        }
        .pago-icono {
            font-size: 2rem; background: white;
            width: 48px; height: 48px; border-radius: 12px;
            display: flex; align-items: center; justify-content: center;
            box-shadow: var(--sombra-sm);
        }
        .pago-label {
            font-size:.7rem; color: var(--uv-gris-500);
            text-transform: uppercase; letter-spacing:.5px; font-weight: 700;
        }
        .pago-valor {
            font-family: var(--fuente-display); font-weight: 700;
            color: var(--uv-azul); font-size: 1rem; margin-top: 2px;
        }
        .anticipado-aviso {
            background: var(--uv-amarillo-light);
            border-left: 4px solid var(--uv-amarillo);
            padding: 14px 16px; border-radius: 10px; margin-bottom: 16px;
        }
    </style>
</head>
<body data-context-path="${pageContext.request.contextPath}">

<%@ include file="_header.jsp" %>

<main class="page-wrapper">

    <div style="margin-bottom:12px;">
        <a href="${pageContext.request.contextPath}/pedido/historial"
           style="font-size:.85rem;color:var(--uv-gris-500);">Mis pedidos</a>
    </div>

    <% if (recienCreado) { %>
    <div class="exito-banner">
        <div class="exito-icono"></div>
        <div>
            <div class="exito-titulo">¡Pedido confirmado!</div>
            <div class="exito-texto">
                Tu pedido ha sido registrado exitosamente.
                Descarga tu comprobante o muestra el código QR al recoger tu comida.
            </div>
        </div>
    </div>
    <% } %>

    <% if (exito!= null &&!recienCreado) { %>
    <div class="alert alert-exito" data-auto-close> <%= exito %></div>
    <% } %>
    <% if (info!= null) { %>
    <div class="alert alert-info" data-auto-close>ℹ <%= info %></div>
    <% } %>

    <!-- Banner para calificar si aplica -->
    <% if (puedeCalificar) { %>
    <div class="calificar-banner">
        <div style="font-size:2.2rem;">⭐</div>
        <div style="flex:1;">
            <div style="font-family:var(--fuente-display);font-weight:800;font-size:1.1rem;">
                ¿Qué te pareció este pedido?
            </div>
            <div style="font-size:.85rem;color:var(--uv-gris-700);">
                Tu opinión nos ayuda a mejorar la calidad del comedor.
            </div>
        </div>
        <a href="${pageContext.request.contextPath}/calificar?idPedido=<%= pedido.getIdPedido() %>"
           class="btn btn-primario">
            ⭐ Dejar reseña
        </a>
    </div>
    <% } %>

    <% if (yaCalificado) { %>
    <div class="alert alert-info" style="margin-bottom:20px;">
        ⭐ Ya calificaste este pedido. ¡Gracias!
    </div>
    <% } %>

    <div class="page-header d-flex justify-between align-center flex-wrap gap-2">
        <div>
            <div class="page-title">
                Pedido <%= pedido.getFolio()!= null? pedido.getFolio() : "#" + pedido.getIdPedido() %>
            </div>
            <div class="page-subtitle">
                <% if (pedido.getEstado()!= null) { %>
                <span class="estado-badge estado-<%= pedido.getEstado().name() %>">
  <%= pedido.getEstado().name() %>
  </span>
                <% } %>
                <% if (pedido.getFechaCreacion()!= null) { %>
                - Creado el <%= pedido.getFechaCreacion().format(fmt) %>
                <% } %>
            </div>
        </div>
        <a href="${pageContext.request.contextPath}/pedido/comprobante?id=<%= pedido.getIdPedido() %>"
           class="btn btn-primario" target="_blank">
            Descargar comprobante PDF
        </a>
    </div>

    <% if (pedido.getProgramacion()!= null) { %>
    <div class="anticipado-aviso">
        <div style="font-weight:700;font-size:.95rem;margin-bottom:4px;">Pedido anticipado</div>
        <div style="font-size:.85rem;color:var(--uv-gris-700);">
            Recoger el <strong><%= pedido.getProgramacion().getFechaRecogida() %></strong>
            a las <strong><%= pedido.getProgramacion().getHoraRecogida() %></strong>
            en <strong><%= pedido.getProgramacion().getLugarRecogida() %></strong>
        </div>
    </div>
    <% } %>

    <div class="resumen-grid">
        <div>
            <div class="pago-card">
                <div class="pago-icono"><%= metodoPagoIcono %></div>
                <div style="flex:1;">
                    <div class="pago-label">Método de pago</div>
                    <div class="pago-valor"><%= metodoPagoTexto %></div>
                </div>
            </div>

            <div class="info-box">
                <div class="info-titulo">Platillos</div>
                <%
                    if (pedido.getDetalles()!= null) {
                        for (DetallePedido d : pedido.getDetalles()) {
                            String nombre = (d.getPlatillo()!= null && d.getPlatillo().getNombre()!= null)
                                    ? d.getPlatillo().getNombre()
                                    : "Platillo #" + d.getIdPlatillo();
                            String descripcion = (d.getPlatillo()!= null)? d.getPlatillo().getDescripcion() : null;
                            java.math.BigDecimal subtotal = d.getPrecioUnitario()
                                    .multiply(new java.math.BigDecimal(d.getCantidad()));
                %>
                <div class="platillo-fila">
                    <div class="platillo-info">
                        <div class="platillo-nombre">
                            <%= nombre %>
                            <% if (d.isCubiertoPorBeca()) { %><span class="badge-beca">BECA</span><% } %>
                        </div>
                        <% if (descripcion!= null &&!descripcion.isBlank()) { %>
                        <div class="platillo-descripcion"><%= descripcion %></div>
                        <% } %>
                        <div class="platillo-cantidad">
                            <%= d.getCantidad() %>X $<%= d.getPrecioUnitario().toPlainString() %>
                        </div>
                    </div>
                    <div class="platillo-precio">$<%= subtotal.toPlainString() %></div>
                </div>
                <% } } %>
            </div>

            <div class="info-box">
                <div class="info-titulo">Totales</div>
                <% if (pedido.getSubtotal()!= null) { %>
                <div class="totales-fila">
                    <span>Subtotal</span>
                    <span>$<%= pedido.getSubtotal().toPlainString() %></span>
                </div>
                <% } %>
                <% if (pedido.getDescuentoBeca()!= null
                        && pedido.getDescuentoBeca().compareTo(java.math.BigDecimal.ZERO) > 0) { %>
                <div class="totales-fila" style="color:var(--uv-verde-dark);">
                    <span>Descuento beca</span>
                    <span>-$<%= pedido.getDescuentoBeca().toPlainString() %></span>
                </div>
                <% } %>
                <div class="totales-fila total">
                    <span>TOTAL</span>
                    <span>$<%= pedido.getTotal()!= null? pedido.getTotal().toPlainString() : "0.00" %></span>
                </div>
            </div>
        </div>

        <div>
            <div class="qr-section">
                <div class="info-titulo" style="justify-content:center;">Tu código QR</div>
                <img src="${pageContext.request.contextPath}/pedido/qr?id=<%= pedido.getIdPedido() %>"
                     alt="Código QR del pedido">
                <div class="qr-instrucciones">
                    Muestra este código al personal del comedor cuando recojas tu pedido
                </div>
            </div>

            <div class="info-box">
                <div class="info-titulo">ℹ Información</div>
                <div style="font-size:.85rem;line-height:1.7;">
                    <div><strong>Cliente:</strong> <%= usuario.getNombreCompleto() %></div>
                    <div><strong>Tipo:</strong> <%= pedido.getTipo()!= null? pedido.getTipo().name() : "-" %></div>
                    <% if (pedido.getEstado()!= null) { %>
                    <div><strong>Estado:</strong> <%= pedido.getEstado().name() %></div>
                    <% } %>
                </div>
            </div>
        </div>
    </div>

</main>

<%@ include file="_footer.jsp" %>
</body>
</html>
