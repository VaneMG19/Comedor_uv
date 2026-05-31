/*
  carrito.js — Gestión del carrito en memoria
 */
let carrito = { items: [], tipoPedido: 'INMEDIATO', metodoPago: 'EFECTIVO', fechaRecogida: null, horaRecogida: null };
let platilloActual = null;

// Tarjetas guardadas del usuario
let tarjetasUsuario = [];
let tarjetaSeleccionadaId = null;
let tarjetasYaCargadas = false;

function toggleCarrito() {
    const drawer  = document.getElementById('carrito-drawer');
    const overlay = document.getElementById('carrito-overlay');
    if (!drawer) return;
    const abierto = drawer.classList.toggle('abierto');
    if (overlay) overlay.style.display = abierto ? 'block' : 'none';
    document.body.style.overflow = abierto ? 'hidden' : '';
    if (abierto) {
        renderizarCarrito();
        cargarTarjetasUsuario();
    }
}

function actualizarTipoPedido(tipo) {
    carrito.tipoPedido = tipo;
    const camposAnt = document.getElementById('campos-anticipado');
    if (camposAnt) camposAnt.style.display = tipo === 'ANTICIPADO' ? 'block' : 'none';
}

function abrirModalPlatillo(datos) {
    platilloActual = { ...datos, cantidad: 1 };
    document.getElementById('modal-platillo-nombre').textContent = datos.nombre;
    document.getElementById('modal-platillo-desc').textContent   = datos.descripcion || '';
    document.getElementById('modal-cantidad').textContent = '1';
    document.getElementById('modal-personalizacion').value = '';
    const precioEl = document.getElementById('modal-platillo-precio');
    if (datos.cubiertoPorBeca) {
        precioEl.textContent = '$0.00'; precioEl.style.color = 'var(--uv-verde)';
    } else {
        precioEl.textContent = '$' + parseFloat(datos.precioFinal).toFixed(2);
        precioEl.style.color = 'var(--uv-azul)';
    }
    document.getElementById('modal-platillo-tiempo').textContent = (datos.tiempoPrep || 15) + ' min';
    const imgContainer = document.getElementById('modal-platillo-img');
    imgContainer.innerHTML = datos.imagen
        ? `<img src="${datos.imagen}" alt="${datos.nombre}" style="width:100%;height:100%;object-fit:cover;">`
        : '';
    renderizarNutricion(datos.nutri);
    const firstTab = document.querySelector('#modal-platillo .tab-btn');
    if (firstTab) switchModalTab('detalle', firstTab);
    document.getElementById('modal-platillo').classList.add('activo');
    document.body.style.overflow = 'hidden';
}

function cerrarModalPlatillo() {
    document.getElementById('modal-platillo').classList.remove('activo');
    document.body.style.overflow = '';
    platilloActual = null;
}

function renderizarNutricion(nutri) {
    const contenedor = document.getElementById('modal-nutri-contenido');
    if (!nutri || !nutri.calorias) {
        contenedor.innerHTML = '<div style="text-align:center;color:var(--uv-gris-500);padding:20px;">Sin información nutricional registrada</div>';
        return;
    }
    const nivelLabel = { BAJO: 'Bajo', MEDIO: 'Medio', ALTO: 'Alto' };
    let html = `<div class="nutri-grid">
        <div class="nutri-item"><div class="nutri-valor">${nutri.calorias||0}</div><div class="nutri-label">kcal</div></div>
        <div class="nutri-item"><div class="nutri-valor">${nutri.proteinas||0}g</div><div class="nutri-label">Proteínas</div></div>
        <div class="nutri-item"><div class="nutri-valor">${nutri.carbohidratos||0}g</div><div class="nutri-label">Carbohidratos</div></div>
        <div class="nutri-item"><div class="nutri-valor">${nutri.grasas||0}g</div><div class="nutri-label">Grasas</div></div>
        <div class="nutri-item"><div class="nutri-valor">${nutri.fibra||0}g</div><div class="nutri-label">Fibra</div></div>
        <div class="nutri-item"><div class="nutri-valor">${nutri.sodio||0}mg</div><div class="nutri-label">Sodio</div></div>
    </div>`;
    if (nutri.huellaCarbonoKg && nutri.nivelHuella)
        html += `<div style="margin-top:12px;"><span class="huella-badge ${nutri.nivelHuella}">${nivelLabel[nutri.nivelHuella]||nutri.nivelHuella} · ${nutri.huellaCarbonoKg} kg CO₂eq</span></div>`;
    let chips = '';
    if (nutri.esVegetariano) chips += '<span class="chip verde">Vegetariano</span>';
    if (nutri.esVegano)      chips += '<span class="chip verde">Vegano</span>';
    if (nutri.esGlutenFree)  chips += '<span class="chip amarillo">Sin gluten</span>';
    if (chips) html += `<div class="chips" style="margin-top:12px;">${chips}</div>`;
    if (nutri.alergenos) html += `<div style="margin-top:12px;font-size:.85rem;"><strong>Alérgenos:</strong> ${nutri.alergenos}</div>`;
    contenedor.innerHTML = html;
}

function cambiarCantidadModal(delta) {
    if (!platilloActual) return;
    platilloActual.cantidad = Math.max(1, platilloActual.cantidad + delta);
    document.getElementById('modal-cantidad').textContent = platilloActual.cantidad;
}

function agregarAlCarrito() {
    if (!platilloActual) return;
    const personalizacion = document.getElementById('modal-personalizacion').value.trim();
    const existente = carrito.items.find(i => i.idPlatillo === platilloActual.idPlatillo);
    if (existente) {
        existente.cantidad += platilloActual.cantidad;
        if (personalizacion) {
            existente.personalizacion = existente.personalizacion
                ? existente.personalizacion + '; ' + personalizacion
                : personalizacion;
        }
    } else {
        carrito.items.push({
            idPlatillo: platilloActual.idPlatillo,
            nombre: platilloActual.nombre,
            precio: parseFloat(platilloActual.precioFinal)||0,
            cantidad: platilloActual.cantidad,
            cubiertoPorBeca: platilloActual.cubiertoPorBeca||false,
            personalizacion,
            imagen: platilloActual.imagen||null
        });
    }
    actualizarBadgeCarrito();
    cerrarModalPlatillo();
    mostrarMensaje(platilloActual.nombre + ' agregado al carrito', 'exito');
    setTimeout(() => {
        if (!document.getElementById('carrito-drawer').classList.contains('abierto')) toggleCarrito();
        else renderizarCarrito();
    }, 300);
}

function renderizarCarrito() {
    const container = document.getElementById('carrito-items');
    const footer    = document.getElementById('carrito-footer');
    const vacio     = document.getElementById('carrito-vacio');
    if (!container) return;
    if (carrito.items.length === 0) {
        if (vacio) vacio.style.display = 'block';
        if (footer) footer.style.display = 'none';
        container.innerHTML = '';
        if (vacio) container.appendChild(vacio);
        return;
    }
    if (vacio) vacio.style.display = 'none';
    if (footer) footer.style.display = 'block';
    container.innerHTML = carrito.items.map((item, idx) => `
        <div class="carrito-item">
            <div class="carrito-item-img">${item.imagen ? `<img src="${item.imagen}" style="width:100%;height:100%;object-fit:cover;border-radius:8px;">` : ''}</div>
            <div class="carrito-item-info">
                <div class="carrito-item-nombre">${item.nombre}</div>
                <div class="carrito-item-precio ${item.cubiertoPorBeca ? 'beca' : ''}">${item.cubiertoPorBeca ? 'Beca' : '$' + (item.precio * item.cantidad).toFixed(2)}</div>
                ${item.personalizacion ? `<div style="font-size:.75rem;color:var(--uv-gris-500);font-style:italic;">${item.personalizacion}</div>` : ''}
                <div class="cantidad-control">
                    <button class="cantidad-btn" onclick="cambiarCantidadCarrito(${idx},-1)">−</button>
                    <span class="cantidad-num">${item.cantidad}</span>
                    <button class="cantidad-btn" onclick="cambiarCantidadCarrito(${idx},1)">+</button>
                </div>
            </div>
            <button class="carrito-eliminar" onclick="eliminarDelCarrito(${idx})">×</button>
        </div>`).join('');
    calcularTotales();
}

function cambiarCantidadCarrito(idx, delta) {
    carrito.items[idx].cantidad = Math.max(1, carrito.items[idx].cantidad + delta);
    actualizarBadgeCarrito(); renderizarCarrito();
}

function eliminarDelCarrito(idx) {
    carrito.items.splice(idx, 1); actualizarBadgeCarrito(); renderizarCarrito();
}

function calcularTotales() {
    let subtotal = 0, descBeca = 0;
    carrito.items.forEach(item => {
        const imp = item.precio * item.cantidad;
        subtotal += imp;
        if (item.cubiertoPorBeca) descBeca += imp;
    });
    const total = subtotal - descBeca;
    const elSub   = document.getElementById('carrito-subtotal');
    const elTot   = document.getElementById('carrito-total');
    const filaBeca = document.getElementById('fila-beca');
    const elDesc  = document.getElementById('carrito-descuento-beca');
    if (elSub) elSub.textContent = '$' + subtotal.toFixed(2);
    if (elTot) elTot.textContent = '$' + total.toFixed(2);
    if (filaBeca) filaBeca.style.display = descBeca > 0 ? 'flex' : 'none';
    if (elDesc) elDesc.textContent = '-$' + descBeca.toFixed(2);
}

function actualizarBadgeCarrito() {
    const badge = document.getElementById('carrito-badge');
    if (!badge) return;
    const total = carrito.items.reduce((s, i) => s + i.cantidad, 0);
    badge.textContent = total > 9 ? '9+' : total;
    badge.style.display = total > 0 ? 'inline-flex' : 'none';
}


//   SELECTOR DE TARJETAS GUARDADAS


async function cargarTarjetasUsuario() {
    try {
        const ctx = document.body.dataset.contextPath || '';
        const url = ctx + '/tarjeta/listar';
        console.log('[Carrito] Cargando tarjetas desde:', url);

        const r = await fetch(url, { credentials: 'same-origin' });
        console.log('[Carrito] Respuesta:', r.status);

        if (!r.ok) {
            console.error('[Carrito] Error HTTP:', r.status);
            tarjetasUsuario = [];
        } else {
            const data = await r.json();
            console.log('[Carrito] Datos:', data);
            tarjetasUsuario = data.tarjetas || [];
            const pred = tarjetasUsuario.find(t => t.esPredeterminada);
            tarjetaSeleccionadaId = pred ? pred.idTarjeta : (tarjetasUsuario[0]?.idTarjeta || null);
        }
    } catch (e) {
        console.error('[Carrito] Error cargando tarjetas:', e);
        tarjetasUsuario = [];
    }
    tarjetasYaCargadas = true;
    // Re-render si el método ya está en TARJETA
    const sel = document.getElementById('metodoPago');
    if (sel && sel.value === 'TARJETA') onMetodoPagoChange('TARJETA');
}

function onMetodoPagoChange(metodo) {
    const cont = document.getElementById('selector-tarjeta-cont');
    if (!cont) {
        console.warn('[Carrito] No se encontró #selector-tarjeta-cont en el DOM');
        return;
    }

    if (metodo !== 'TARJETA') {
        cont.innerHTML = '';
        cont.style.display = 'none';
        return;
    }
    cont.style.display = 'block';

    if (!tarjetasYaCargadas) {
        cont.innerHTML = '<div style="padding:12px;text-align:center;color:var(--uv-gris-500);font-size:.85rem;">Cargando tarjetas...</div>';
        return;
    }

    if (tarjetasUsuario.length === 0) {
        const ctx = document.body.dataset.contextPath || '';
        cont.innerHTML = `
            <div style="background:var(--uv-amarillo-light);
                        border-left:4px solid var(--uv-amarillo);
                        padding:12px;border-radius:10px;font-size:.85rem;line-height:1.5;">
                <strong>No tienes tarjetas guardadas</strong><br>
                <a href="${ctx}/perfil?tab=tarjetas"
                   style="color:var(--uv-azul);font-weight:600;text-decoration:underline;">
                   Agregar tarjeta en mi perfil
                </a>
            </div>`;
        return;
    }

    cont.innerHTML = `
        <label style="display:block;font-weight:600;font-size:.85rem;
                      color:var(--uv-gris-700);margin-bottom:6px;">
            Tarjeta a usar:
        </label>
        <div style="display:flex;flex-direction:column;gap:6px;">
            ${tarjetasUsuario.map(t => `
                <label style="display:flex;align-items:center;gap:10px;
                              padding:10px;
                              border:2px solid ${t.idTarjeta === tarjetaSeleccionadaId ? 'var(--uv-azul)' : 'var(--color-borde)'};
                              background:${t.idTarjeta === tarjetaSeleccionadaId ? 'var(--uv-azul-light)' : 'white'};
                              border-radius:10px;cursor:pointer;font-size:.85rem;">
                    <input type="radio" name="tarjetaSel" value="${t.idTarjeta}"
                           ${t.idTarjeta === tarjetaSeleccionadaId ? 'checked' : ''}
                           onchange="seleccionarTarjeta(${t.idTarjeta})"
                           style="margin:0;">
                    <div style="flex:1;">
                        <div style="font-weight:700;">
                            ${escapeHTML(t.alias)}
                            ${t.esPredeterminada ? '<span style="background:var(--uv-amarillo);color:#000;font-size:.6rem;padding:2px 6px;border-radius:8px;margin-left:5px;font-weight:800;">PREDET.</span>' : ''}
                        </div>
                        <div style="font-family:monospace;color:var(--uv-gris-500);font-size:.75rem;">
                            ${escapeHTML(t.marca || '')} •••• ${escapeHTML(t.ultimos4 || '')}
                        </div>
                    </div>
                </label>
            `).join('')}
        </div>`;
}

function seleccionarTarjeta(id) {
    tarjetaSeleccionadaId = parseInt(id);
    const sel = document.getElementById('metodoPago');
    if (sel) onMetodoPagoChange(sel.value);
}

function escapeHTML(s) {
    if (s == null) return '';
    return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;')
        .replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}



async function confirmarPedido() {
    if (carrito.items.length === 0) { mostrarMensaje('Agrega al menos un platillo', 'error'); return; }
    const tipo   = document.querySelector('input[name="tipoPedido"]:checked')?.value || 'INMEDIATO';
    const metodo = document.getElementById('metodoPago')?.value || 'EFECTIVO';

    if (metodo === 'TARJETA') {
        if (tarjetasUsuario.length === 0) {
            mostrarMensaje('Agrega una tarjeta en tu perfil primero', 'aviso');
            return;
        }
        if (!tarjetaSeleccionadaId) {
            mostrarMensaje('Selecciona una tarjeta', 'aviso');
            return;
        }
    }

    if (tipo === 'ANTICIPADO') {
        const fecha = document.getElementById('fechaRecogida')?.value;
        const hora  = document.getElementById('horaRecogida')?.value;
        if (!fecha || !hora) { mostrarMensaje('Indica fecha y hora de recogida', 'aviso'); return; }
        carrito.fechaRecogida = fecha; carrito.horaRecogida = hora;
    }

    const params = new URLSearchParams();
    params.append('tipoPedido', tipo);
    params.append('metodoPago', metodo);

    if (metodo === 'TARJETA' && tarjetaSeleccionadaId) {
        params.append('idTarjeta', tarjetaSeleccionadaId);
    }

    if (tipo === 'ANTICIPADO') {
        params.append('fechaRecogida', carrito.fechaRecogida);
        params.append('horaRecogida',  carrito.horaRecogida);
        params.append('lugarRecogida', 'Ventanilla principal');
    }

    const notasEl = document.getElementById('notasPedido');
    const notasTxt = notasEl ? (notasEl.value || '').trim() : '';
    params.append('notas', notasTxt);

    carrito.items.forEach(item => {
        params.append('platilloId',      item.idPlatillo);
        params.append('cantidad',        item.cantidad);
        params.append('personalizacion', item.personalizacion || '');
    });

    const btn = document.querySelector('.carrito-footer .btn-primario');
    if (btn) { btn.disabled = true; btn.textContent = 'Procesando...'; }

    try {
        const ctx  = document.body.dataset.contextPath || '';
        const resp = await fetch(ctx + '/pedido/crear', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params,
            credentials: 'same-origin'
        });
        carrito.items = []; actualizarBadgeCarrito();
        const _notasEl = document.getElementById('notasPedido');
        if (_notasEl) _notasEl.value = '';
        window.location.href = resp.redirected ? resp.url : ctx + '/pedido/historial';
    } catch (err) {
        mostrarMensaje('Error de conexión. Verifica tu red.', 'error');
        if (btn) { btn.disabled = false; btn.textContent = 'Confirmar Pedido'; }
    }
}

function mostrarMensaje(texto, tipo = 'info') {
    const colores = { exito: 'var(--uv-verde)', error: 'var(--uv-rojo)', aviso: 'var(--uv-amarillo)', info: 'var(--uv-azul)' };
    const toast = document.createElement('div');
    toast.style.cssText = `position:fixed;bottom:24px;left:50%;transform:translateX(-50%);
        background:white;border-left:4px solid ${colores[tipo]||colores.info};
        border-radius:10px;box-shadow:var(--sombra-lg);padding:12px 20px;
        font-size:.875rem;font-weight:600;z-index:9999;
        animation:slideInToast .3s ease;max-width:90vw;text-align:center;`;
    toast.textContent = texto;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
}