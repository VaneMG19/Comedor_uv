<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%-- _footer.jsp --%>
<div class="carrito-drawer" id="carrito-drawer">
    <div class="carrito-header">
        <div class="carrito-titulo">🛒 Mi Carrito</div>
        <button class="carrito-cerrar" onclick="toggleCarrito()">✕</button>
    </div>
    <div class="carrito-items" id="carrito-items">
        <div id="carrito-vacio" style="text-align:center;padding:40px 20px;color:var(--uv-gris-500);">
            <div style="font-size:3rem;margin-bottom:12px;">🛒</div>
            <div style="font-weight:600;">Tu carrito está vacío</div>
            <div style="font-size:.85rem;margin-top:6px;">Agrega platillos del menú</div>
        </div>
    </div>
    <div class="carrito-footer" id="carrito-footer" style="display:none;">
        <div class="carrito-resumen">
            <div class="carrito-fila"><span>Subtotal</span><span id="carrito-subtotal">$0.00</span></div>
            <div class="carrito-fila beca-desc" id="fila-beca" style="display:none;">
                <span> Descuento beca</span><span id="carrito-descuento-beca">-$0.00</span>
            </div>
            <div class="carrito-fila total"><span>Total</span><span id="carrito-total">$0.00</span></div>
        </div>
        <div style="margin-bottom:14px;">
            <label class="form-label">Tipo de pedido</label>
            <div style="display:flex;gap:8px;">
                <label style="flex:1;cursor:pointer;font-size:.875rem;">
                    <input type="radio" name="tipoPedido" value="INMEDIATO" checked
                           onchange="actualizarTipoPedido(this.value)"> Inmediato
                </label>
                <label style="flex:1;cursor:pointer;font-size:.875rem;">
                    <input type="radio" name="tipoPedido" value="ANTICIPADO"
                           onchange="actualizarTipoPedido(this.value)"> Programar
                </label>
            </div>
        </div>
        <div id="campos-anticipado" style="display:none;margin-bottom:14px;">
            <div class="form-group">
                <label class="form-label">Fecha de recogida</label>
                <input type="date" id="fechaRecogida" class="form-control">
            </div>
            <div class="form-group" style="margin-bottom:0;">
                <label class="form-label">Hora de recogida</label>
                <input type="time" id="horaRecogida" class="form-control" min="07:00" max="18:00">
            </div>
        </div>
        <div style="margin-bottom:14px;">
            <label class="form-label">Método de pago</label>
            <select id="metodoPago" class="form-control" onchange="onMetodoPagoChange(this.value)">
                <option value="EFECTIVO"> Efectivo</option>
                <option value="TARJETA"> Tarjeta</option>
            </select>

            <!-- Selector de tarjetas (se llena con JS cuando se elige TARJETA) -->
            <div id="selector-tarjeta-cont" style="display:none;margin-top:12px;"></div>
        </div>
        <div style="margin-bottom:14px;">
            <label class="form-label" for="notasPedido">
                Instrucciones especiales <span style="color:var(--uv-gris-500);font-weight:400;font-size:.8rem;">(opcional)</span>
            </label>
            <textarea id="notasPedido" class="form-control" rows="2"
                      maxlength="200"
                      placeholder="Ej: sin cebolla, salsa aparte..."
                      style="resize:vertical;font-family:inherit;"></textarea>
        </div>
        <button class="btn btn-primario btn-block btn-lg" onclick="confirmarPedido()">
            Confirmar Pedido
        </button>
    </div>
</div>
<div id="carrito-overlay" onclick="toggleCarrito()"
     style="display:none;position:fixed;inset:0;background:rgba(0,0,0,.3);z-index:940;"></div>

<div class="modal-overlay" id="modal-platillo">
    <div class="modal" style="max-width:580px;">
        <div class="modal-header">
            <div class="modal-titulo" id="modal-platillo-nombre">Platillo</div>
            <button class="modal-cerrar" onclick="cerrarModalPlatillo()">✕</button>
        </div>
        <div class="modal-body">
            <div id="modal-platillo-img"
                 style="width:100%;height:200px;border-radius:12px;overflow:hidden;
                        background:var(--uv-gris-200);margin-bottom:16px;
                        display:flex;align-items:center;justify-content:center;font-size:4rem;"></div>
            <div class="tabs" style="margin-bottom:16px;">
                <button class="tab-btn activo" onclick="switchModalTab('detalle',this)">Detalle</button>
                <button class="tab-btn" onclick="switchModalTab('nutricion',this)"> Nutrición</button>
            </div>
            <div id="modal-tab-detalle">
                <p id="modal-platillo-desc" style="color:var(--uv-gris-700);font-size:.9rem;margin-bottom:16px;"></p>
                <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:16px;">
                    <div>
                        <span style="font-size:.8rem;color:var(--uv-gris-500);">Precio</span>
                        <div id="modal-platillo-precio" style="font-family:var(--fuente-display);font-weight:800;font-size:1.4rem;color:var(--uv-azul);">$0.00</div>
                    </div>
                    <div style="text-align:right;">
                        <span style="font-size:.8rem;color:var(--uv-gris-500);">Tiempo estimado</span>
                        <div id="modal-platillo-tiempo" style="font-weight:600;">️ 15 min</div>
                    </div>
                </div>
                <div class="form-group">
                    <label class="form-label">¿Alguna personalización?</label>
                    <textarea id="modal-personalizacion" class="form-control" rows="2"
                              placeholder="Ej: sin cebolla, extra salsa..."></textarea>
                </div>
                <div style="display:flex;align-items:center;gap:16px;margin-top:8px;">
                    <span style="font-weight:600;font-size:.9rem;">Cantidad:</span>
                    <div class="cantidad-control">
                        <button class="cantidad-btn" onclick="cambiarCantidadModal(-1)">−</button>
                        <span class="cantidad-num" id="modal-cantidad">1</span>
                        <button class="cantidad-btn" onclick="cambiarCantidadModal(1)">+</button>
                    </div>
                </div>
            </div>
            <div id="modal-tab-nutricion" style="display:none;">
                <div id="modal-nutri-contenido">
                    <div style="text-align:center;color:var(--uv-gris-500);padding:20px;">Sin información nutricional</div>
                </div>
            </div>
        </div>
        <div class="modal-footer">
            <button class="btn btn-ghost" onclick="cerrarModalPlatillo()">Cancelar</button>
            <button class="btn btn-primario btn-lg" onclick="agregarAlCarrito()">🛒 Agregar al carrito</button>
        </div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/js/styles.js"></script>
<script src="${pageContext.request.contextPath}/js/notificaciones.js"></script>
<script src="${pageContext.request.contextPath}/js/carrito.js"></script>
</body>
</html>
