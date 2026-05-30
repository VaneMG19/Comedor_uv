<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="mx.uv.comedor.model.*" %>
<%@ page import="java.util.List" %>
<%
 Usuario usuario = (Usuario) session.getAttribute("usuario");
 if (usuario == null || (usuario.getRol() != RolEnum.EMPLEADO && usuario.getRol() != RolEnum.ADMIN)) {
  response.sendRedirect(request.getContextPath() + "/login");
  return;
 }
 List<Platillo> menuDia  = (List<Platillo>) request.getAttribute("menuDia");
 List<Platillo> aLaCarta = (List<Platillo>) request.getAttribute("aLaCarta");
 if (menuDia  == null) menuDia  = new java.util.ArrayList<>();
 if (aLaCarta == null) aLaCarta = new java.util.ArrayList<>();
 String error = request.getParameter("error");
%>
<!DOCTYPE html>
<html lang="es">
<head>
 <meta charset="UTF-8">
 <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
 <meta name="viewport" content="width=device-width,initial-scale=1.0">
 <title>Punto de Venta — Comedor UV</title>
 <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
 <style>
  .pos-layout {
   display: grid;
   grid-template-columns: 1.5fr 1fr;
   gap: 20px;
   min-height: 70vh;
  }
  @media (max-width: 1024px) { .pos-layout { grid-template-columns: 1fr; } }

  .pos-catalogo { display: flex; flex-direction: column; gap: 16px; }
  .pos-tabs {
   display: flex;
   background: white;
   border-radius: 12px;
   padding: 4px;
   gap: 4px;
   box-shadow: var(--sombra-sm);
  }
  .pos-tab {
   flex: 1;
   padding: 12px;
   border: none;
   background: transparent;
   border-radius: 10px;
   font-family: var(--fuente-display);
   font-weight: 600;
   cursor: pointer;
   color: var(--uv-gris-700);
  }
  .pos-tab.activa { background: var(--uv-azul); color: white; }

  /* Buscador del POS */
  .pos-buscador-bar {
   background: white;
   border-radius: 12px;
   padding: 14px;
   box-shadow: var(--sombra-sm);
   display: flex;
   gap: 10px;
   align-items: center;
   flex-wrap: wrap;
  }
  .pos-buscador-bar input[type="text"] {
   flex: 1;
   min-width: 180px;
  }

  .pos-cat-chips {
   display: flex;
   gap: 6px;
   flex-wrap: wrap;
  }
  .pos-cat-chip {
   padding: 6px 12px;
   border-radius: 16px;
   border: 2px solid var(--color-borde);
   background: white;
   font-size: .75rem;
   font-weight: 600;
   cursor: pointer;
   color: var(--uv-gris-700);
   transition: all .15s;
  }
  .pos-cat-chip:hover { border-color: var(--uv-azul); }
  .pos-cat-chip.activa {
   background: var(--uv-azul);
   color: white;
   border-color: var(--uv-azul);
  }

  .pos-grid {
   display: grid;
   grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
   gap: 12px;
  }
  .pos-platillo {
   background: white;
   border-radius: 12px;
   padding: 14px;
   box-shadow: var(--sombra-sm);
   cursor: pointer;
   transition: all .15s;
   border: 2px solid transparent;
   text-align: center;
  }
  .pos-platillo:hover { border-color: var(--uv-azul); transform: translateY(-2px); }
  .pos-platillo .nombre {
   font-weight: 700;
   font-size: .9rem;
   margin-bottom: 4px;
   line-height: 1.3;
   min-height: 2.2em;
  }
  .pos-platillo .cat {
   font-size: .68rem;
   color: var(--uv-gris-500);
   margin-bottom: 6px;
  }
  .pos-platillo .precio {
   color: var(--uv-azul);
   font-weight: 800;
   font-family: var(--fuente-display);
   font-size: 1.1rem;
  }
  .pos-sin-resultados {
   grid-column: 1/-1;
   text-align: center;
   padding: 40px 20px;
   color: var(--uv-gris-500);
  }

  .pos-carrito {
   background: white;
   border-radius: 16px;
   padding: 20px;
   box-shadow: var(--sombra-md);
   display: flex;
   flex-direction: column;
   min-height: 600px;
  }
  .pos-cliente-box {
   background: var(--uv-gris-100);
   border-radius: 10px;
   padding: 12px;
   margin-bottom: 14px;
  }
  .pos-cliente-info {
   display: flex;
   align-items: center;
   gap: 10px;
   margin-top: 8px;
   padding: 10px;
   background: var(--uv-verde-light);
   border-radius: 8px;
   font-size: .85rem;
   display: none;
  }
  .pos-cliente-info.visible { display: flex; }
  .pos-cliente-info .av {
   width: 32px; height: 32px;
   border-radius: 50%;
   background: var(--uv-verde);
   color: white;
   display: flex;
   align-items: center;
   justify-content: center;
   font-weight: 700;
   font-size: .75rem;
   flex-shrink: 0;
  }
  .pos-cart-items {
   flex: 1;
   overflow-y: auto;
   margin: 16px 0;
   min-height: 200px;
  }
  .pos-cart-item {
   display: flex;
   justify-content: space-between;
   align-items: center;
   padding: 10px 0;
   border-bottom: 1px solid var(--color-borde);
   gap: 10px;
  }
  .pos-cart-item .info { flex: 1; min-width: 0; }
  .pos-cart-item .nombre {
   font-weight: 600;
   font-size: .85rem;
   overflow: hidden;
   text-overflow: ellipsis;
  }
  .pos-cart-item .precio-unit {
   font-size: .72rem;
   color: var(--uv-gris-500);
  }
  .pos-cart-item .cantidad {
   display: flex;
   align-items: center;
   gap: 6px;
   background: var(--uv-gris-100);
   border-radius: 6px;
   padding: 2px;
  }
  .pos-cart-item .cantidad button {
   border: none;
   background: white;
   width: 22px;
   height: 22px;
   border-radius: 4px;
   cursor: pointer;
   font-weight: 700;
  }
  .pos-cart-item .subtotal {
   font-weight: 700;
   color: var(--uv-azul);
   font-size: .9rem;
   min-width: 60px;
   text-align: right;
  }
  .pos-cart-empty {
   text-align: center;
   padding: 60px 20px;
   color: var(--uv-gris-500);
  }
  .pos-totales {
   padding: 14px;
   background: var(--uv-gris-100);
   border-radius: 10px;
   margin-bottom: 14px;
  }
  .pos-total {
   display: flex;
   justify-content: space-between;
   font-family: var(--fuente-display);
   font-weight: 800;
   font-size: 1.3rem;
   color: var(--uv-azul);
  }
  .metodo-grid {
   display: grid;
   grid-template-columns: 1fr 1fr;
   gap: 8px;
   margin-bottom: 14px;
  }
  .metodo-opcion input[type="radio"] {
   position: absolute;
   opacity: 0;
  }
  .metodo-opcion label {
   display: block;
   padding: 12px;
   border: 2px solid var(--color-borde);
   border-radius: 10px;
   text-align: center;
   cursor: pointer;
   font-weight: 600;
   font-size: .85rem;
  }
  .metodo-opcion input:checked + label {
   border-color: var(--uv-azul);
   background: var(--uv-azul-light);
   color: var(--uv-azul);
  }

  .modal-overlay-pos {
   position: fixed;
   inset: 0;
   background: rgba(0,0,0,.5);
   display: none;
   align-items: center;
   justify-content: center;
   z-index: 1000;
  }
  .modal-overlay-pos.visible { display: flex; }
  .modal-content-pos {
   background: white;
   border-radius: 16px;
   padding: 24px;
   max-width: 400px;
   width: 90%;
  }

  .pos-platillo.pos-agotado {
   opacity: .5;
   cursor: not-allowed;
   background: var(--uv-gris-100);
  }
  .pos-platillo.pos-agotado:hover {
   border-color: transparent;
   transform: none;
  }
 </style>
</head>
<body data-context-path="${pageContext.request.contextPath}">

<%@ include file="../_header.jsp" %>

<main class="page-wrapper">

 <div class="page-header">
  <div class="page-title">Punto de Venta</div>
  <div class="page-subtitle">
   Atendido por <strong><%= usuario.getNombreCompleto() %></strong>
  </div>
 </div>

 <% if (error != null) { %>
 <div class="alert alert-error" data-auto-close><%= error %></div>
 <% } %>

 <div class="pos-layout">

  <!-- IZQUIERDA: Catalogo -->
  <div class="pos-catalogo">
   <div class="pos-tabs">
    <button class="pos-tab activa" onclick="switchPosTab('dia', this)">
     Menu del Dia (<%= menuDia.size() %>)
    </button>
    <button class="pos-tab" onclick="switchPosTab('carta', this)">
     A la Carta (<%= aLaCarta.size() %>)
    </button>
   </div>

   <!-- BUSCADOR — solo visible en tab "carta" -->
   <div id="pos-buscador" class="pos-buscador-bar" style="display:none;">
    <input type="text" id="pos-busqueda" class="form-control"
           placeholder="Buscar platillo..."
           oninput="filtrarPos()">
    <div class="pos-cat-chips" id="pos-chips">
     <button type="button" class="pos-cat-chip activa" data-cat="TODAS"
             onclick="seleccionarCatPos(this)">Todas</button>
     <%
      // Categorias presentes en la carta
      java.util.Set<CategoriaPlatEnum> catsEnCarta = new java.util.LinkedHashSet<>();
      for (Platillo p : aLaCarta) {
       if (p.getCategoria() != null) catsEnCarta.add(p.getCategoria());
      }
      for (CategoriaPlatEnum cat : catsEnCarta) {
     %>
     <button type="button" class="pos-cat-chip" data-cat="<%= cat.name() %>"
             onclick="seleccionarCatPos(this)">
      <%= cat.getEtiqueta() %>
     </button>
     <% } %>
    </div>
   </div>

   <div id="cat-dia" class="pos-grid">
    <% for (Platillo p : menuDia) {
     boolean _agotado = p.isAgotado() || !p.isDisponible();
    %>
    <div class="pos-platillo <%= _agotado ? "pos-agotado" : "" %>"
            <% if (!_agotado) { %>
         onclick="posAgregarItem(<%= p.getIdPlatillo() %>, '<%= p.getNombre().replace("'","\\'") %>', <%= p.getPrecio() %>)"
            <% } %>>
     <div class="nombre"><%= p.getNombre() %></div>
     <% if (_agotado) { %>
     <div style="font-size:.7rem;color:var(--uv-rojo);font-weight:700;margin:4px 0;">AGOTADO</div>
     <% } else if (p.getCupo() != null) {
      int rest = p.getRestante();
      if (rest > 0 && rest <= 10) { %>
     <div style="font-size:.7rem;color:var(--uv-rojo);font-weight:600;">Quedan <%= rest %></div>
     <% } } %>
     <div class="precio">$<%= p.getPrecio().toPlainString() %></div>
    </div>
    <% } %>
    <% if (menuDia.isEmpty()) { %>
    <div class="pos-sin-resultados">
     Sin platillos en el menu del dia
    </div>
    <% } %>
   </div>

   <div id="cat-carta" class="pos-grid" style="display:none;">
    <% for (Platillo p : aLaCarta) {
     boolean _agotadoC = !p.isDisponible();
    %>
    <div class="pos-platillo carta-item <%= _agotadoC ? "pos-agotado" : "" %>"
         data-nombre="<%= p.getNombre() %>"
         data-categoria="<%= p.getCategoria() != null ? p.getCategoria().name() : "OTRO" %>"
            <% if (!_agotadoC) { %>
         onclick="posAgregarItem(<%= p.getIdPlatillo() %>, '<%= p.getNombre().replace("'","\\'") %>', <%= p.getPrecio() %>)"
            <% } %>>
     <div class="nombre"><%= p.getNombre() %></div>
     <% if (p.getCategoria() != null) { %>
     <div class="cat"><%= p.getCategoria().getEtiqueta() %></div>
     <% } %>
     <% if (_agotadoC) { %>
     <div style="font-size:.7rem;color:var(--uv-rojo);font-weight:700;margin:4px 0;">AGOTADO</div>
     <% } %>
     <div class="precio">$<%= p.getPrecio().toPlainString() %></div>
    </div>
    <% } %>
    <% if (aLaCarta.isEmpty()) { %>
    <div class="pos-sin-resultados">
     Sin platillos a la carta
    </div>
    <% } %>
    <div id="pos-sin-resultados" class="pos-sin-resultados" style="display:none;">
     No hay platillos que coincidan con tu busqueda
    </div>
   </div>
  </div>

  <!-- DERECHA: Carrito -->
  <div class="pos-carrito">
   <h2 style="font-family:var(--fuente-display);color:var(--uv-azul);margin-bottom:14px;">
    Venta actual
   </h2>

   <div class="pos-cliente-box">
    <label class="form-label" style="margin-bottom:6px;font-size:.78rem;">
     Cliente (opcional)
    </label>
    <div style="display:flex;gap:6px;">
     <input type="text" id="buscar-cliente" class="form-control"
            placeholder="Email o matricula" style="flex:1;font-size:.85rem;">
     <button type="button" class="btn btn-ghost btn-sm" onclick="posBuscarCliente()">
      Buscar
     </button>
    </div>
    <div id="cliente-info" class="pos-cliente-info">
     <div class="av" id="cliente-av">--</div>
     <div style="flex:1;">
      <div style="font-weight:600;" id="cliente-nombre">--</div>
      <div style="font-size:.7rem;color:var(--uv-gris-500);" id="cliente-detalle">--</div>
     </div>
     <button onclick="posLimpiarCliente()" style="border:none;background:none;cursor:pointer;font-size:1rem;">
      X
     </button>
    </div>
    <div style="font-size:.72rem;color:var(--uv-gris-500);margin-top:6px;">
     Si no asocias cliente, queda como venta anonima.
    </div>
   </div>

   <div class="pos-cart-items" id="pos-items">
    <div class="pos-cart-empty">
     <div>Selecciona platillos del menu</div>
    </div>
   </div>

   <div class="pos-totales">
    <div class="pos-total">
     <span>TOTAL</span>
     <span id="pos-total">$0.00</span>
    </div>
   </div>

   <div>
    <label class="form-label" style="margin-bottom:8px;font-size:.85rem;">
     Metodo de pago
    </label>
    <div class="metodo-grid">
     <div class="metodo-opcion">
      <input type="radio" name="mp" id="mp-efectivo" value="EFECTIVO" checked>
      <label for="mp-efectivo">Efectivo</label>
     </div>
     <div class="metodo-opcion">
      <input type="radio" name="mp" id="mp-tarjeta" value="TARJETA">
      <label for="mp-tarjeta">Tarjeta</label>
     </div>
    </div>
   </div>

   <button class="btn btn-primario btn-lg" style="width:100%;font-size:1.1rem;"
           onclick="posAbrirCobro()" id="btn-cobrar" disabled>
    Cobrar
   </button>
  </div>
 </div>

</main>

<!-- Modal de cobro -->
<div class="modal-overlay-pos" id="modal-cobro">
 <div class="modal-content-pos">
  <h2 style="font-family:var(--fuente-display);color:var(--uv-azul);margin-bottom:16px;">
   Confirmar cobro
  </h2>
  <div style="text-align:center;margin:20px 0;">
   <div style="color:var(--uv-gris-500);font-size:.85rem;">Total a cobrar</div>
   <div style="font-family:var(--fuente-display);font-weight:800;
                        font-size:2.5rem;color:var(--uv-azul);" id="modal-total">$0.00</div>
  </div>

  <div id="cambio-section">
   <label class="form-label">Con cuanto paga? (efectivo)</label>
   <input type="number" id="paga-con" class="form-control"
          placeholder="0.00" step="0.01" oninput="posCalcularCambio()">
   <div id="cambio-result" style="margin-top:10px;padding:10px;
                                            background:var(--uv-verde-light);
                                            border-radius:8px;text-align:center;
                                            font-family:var(--fuente-display);font-weight:700;
                                            color:var(--uv-verde-dark);display:none;">
    Cambio: <span id="cambio-monto">$0.00</span>
   </div>
  </div>

  <div style="display:flex;gap:10px;margin-top:20px;">
   <button class="btn btn-ghost" style="flex:1;" onclick="posCerrarModal()">
    Cancelar
   </button>
   <button class="btn btn-primario" style="flex:2;" onclick="posConfirmarVenta()">
    Confirmar venta
   </button>
  </div>
 </div>
</div>

<script>
 const CTX_POS = '${pageContext.request.contextPath}';
 let carritoPOS = [];
 let clientePOS = null;
 let categoriaActivaPOS = 'TODAS';

 function switchPosTab(tab, btn) {
  document.querySelectorAll('.pos-tab').forEach(b => b.classList.remove('activa'));
  btn.classList.add('activa');
  document.getElementById('cat-dia').style.display   = tab === 'dia' ? 'grid' : 'none';
  document.getElementById('cat-carta').style.display = tab === 'carta' ? 'grid' : 'none';

  // Mostrar buscador solo en tab carta
  document.getElementById('pos-buscador').style.display = tab === 'carta' ? 'flex' : 'none';
 }

 function seleccionarCatPos(btn) {
  document.querySelectorAll('.pos-cat-chip').forEach(c => c.classList.remove('activa'));
  btn.classList.add('activa');
  categoriaActivaPOS = btn.dataset.cat;
  filtrarPos();
 }

 function filtrarPos() {
  const busqueda = (document.getElementById('pos-busqueda').value || '').toLowerCase().trim();
  let mostradas = 0;
  document.querySelectorAll('.carta-item').forEach(card => {
   const nombre = (card.dataset.nombre || '').toLowerCase();
   const cat    = card.dataset.categoria;
   const matchBusqueda = !busqueda || nombre.includes(busqueda);
   const matchCat = categoriaActivaPOS === 'TODAS' || cat === categoriaActivaPOS;
   const visible = matchBusqueda && matchCat;
   card.style.display = visible ? '' : 'none';
   if (visible) mostradas++;
  });
  document.getElementById('pos-sin-resultados').style.display = mostradas === 0 ? 'block' : 'none';
 }

 function posAgregarItem(id, nombre, precio) {
  const existente = carritoPOS.find(i => i.idPlatillo === id);
  if (existente) {
   existente.cantidad++;
  } else {
   carritoPOS.push({ idPlatillo: id, nombre: nombre, precio: parseFloat(precio), cantidad: 1 });
  }
  posRenderCarrito();
 }

 function posEliminarItem(id) {
  carritoPOS = carritoPOS.filter(i => i.idPlatillo !== id);
  posRenderCarrito();
 }

 function posCambiarCantidad(id, delta) {
  const it = carritoPOS.find(i => i.idPlatillo === id);
  if (it) {
   it.cantidad += delta;
   if (it.cantidad <= 0) posEliminarItem(id);
   else posRenderCarrito();
  }
 }

 function posRenderCarrito() {
  const container = document.getElementById('pos-items');
  const btn = document.getElementById('btn-cobrar');
  if (carritoPOS.length === 0) {
   container.innerHTML = '<div class="pos-cart-empty"><div>Selecciona platillos del menu</div></div>';
   document.getElementById('pos-total').textContent = '$0.00';
   btn.disabled = true;
   return;
  }
  btn.disabled = false;
  let total = 0;
  container.innerHTML = carritoPOS.map(it => {
   const sub = it.precio * it.cantidad;
   total += sub;
   return `
            <div class="pos-cart-item">
                <div class="info">
                    <div class="nombre">\${it.nombre}</div>
                    <div class="precio-unit">$\${it.precio.toFixed(2)} c/u</div>
                </div>
                <div class="cantidad">
                    <button onclick="posCambiarCantidad(\${it.idPlatillo}, -1)">-</button>
                    <span>\${it.cantidad}</span>
                    <button onclick="posCambiarCantidad(\${it.idPlatillo}, 1)">+</button>
                </div>
                <div class="subtotal">$\${sub.toFixed(2)}</div>
                <button onclick="posEliminarItem(\${it.idPlatillo})"
                        style="border:none;background:none;cursor:pointer;color:var(--uv-rojo);font-size:.95rem;">X</button>
            </div>`;
  }).join('');
  document.getElementById('pos-total').textContent = '$' + total.toFixed(2);
 }

 async function posBuscarCliente() {
  const q = document.getElementById('buscar-cliente').value.trim();
  if (!q) return;
  try {
   const r = await fetch(CTX_POS + '/pos/buscar-usuario', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: 'q=' + encodeURIComponent(q),
    credentials: 'same-origin'
   });
   const data = await r.json();
   if (!data.found) {
    alert('Cliente no encontrado');
    return;
   }
   clientePOS = data;
   const av = (data.nombre || 'CL').split(' ').map(w => w[0]).slice(0,2).join('').toUpperCase();
   document.getElementById('cliente-av').textContent = av;
   document.getElementById('cliente-nombre').textContent = data.nombre;
   let detalle = data.email + ' - ' + data.rol;
   if (data.esBecado) detalle += ' - Beca: ' + data.comidasBeca + ' restantes';
   document.getElementById('cliente-detalle').textContent = detalle;
   document.getElementById('cliente-info').classList.add('visible');
  } catch (e) {
   alert('Error al buscar: ' + e.message);
  }
 }

 function posLimpiarCliente() {
  clientePOS = null;
  document.getElementById('buscar-cliente').value = '';
  document.getElementById('cliente-info').classList.remove('visible');
 }

 function posAbrirCobro() {
  if (carritoPOS.length === 0) return;
  const total = carritoPOS.reduce((s,i) => s + i.precio * i.cantidad, 0);
  document.getElementById('modal-total').textContent = '$' + total.toFixed(2);
  document.getElementById('paga-con').value = '';
  document.getElementById('cambio-result').style.display = 'none';
  const metodo = document.querySelector('input[name="mp"]:checked').value;
  document.getElementById('cambio-section').style.display = metodo === 'EFECTIVO' ? 'block' : 'none';
  document.getElementById('modal-cobro').classList.add('visible');
 }

 function posCerrarModal() {
  document.getElementById('modal-cobro').classList.remove('visible');
 }

 function posCalcularCambio() {
  const total = carritoPOS.reduce((s,i) => s + i.precio * i.cantidad, 0);
  const paga = parseFloat(document.getElementById('paga-con').value) || 0;
  const cambio = paga - total;
  const res = document.getElementById('cambio-result');
  if (paga > 0) {
   if (cambio < 0) {
    res.style.background = 'var(--uv-rojo-light)';
    res.style.color = 'var(--uv-rojo)';
    res.innerHTML = 'Falta: $' + Math.abs(cambio).toFixed(2);
   } else {
    res.style.background = 'var(--uv-verde-light)';
    res.style.color = 'var(--uv-verde-dark)';
    res.innerHTML = 'Cambio: $' + cambio.toFixed(2);
   }
   res.style.display = 'block';
  } else {
   res.style.display = 'none';
  }
 }

 async function posConfirmarVenta() {
  const metodo = document.querySelector('input[name="mp"]:checked').value;
  const params = new URLSearchParams();
  params.append('metodoPago', metodo);
  if (clientePOS) params.append('idUsuario', clientePOS.idUsuario);
  carritoPOS.forEach(it => {
   params.append('platilloId', it.idPlatillo);
   params.append('cantidad', it.cantidad);
  });
  const btns = document.querySelectorAll('#modal-cobro button');
  btns.forEach(b => b.disabled = true);
  try {
   const r = await fetch(CTX_POS + '/pos/cobrar', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: params,
    credentials: 'same-origin'
   });
   window.location.href = r.redirected ? r.url : CTX_POS + '/pos';
  } catch (e) {
   alert('Error al procesar venta: ' + e.message);
   btns.forEach(b => b.disabled = false);
  }
 }
</script>

<%@ include file="../_footer.jsp" %>
</body>
</html>
