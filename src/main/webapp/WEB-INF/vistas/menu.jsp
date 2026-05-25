<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="mx.uv.comedor.model.*" %>
<%@ page import="mx.uv.comedor.dao.*" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.LinkedHashMap" %>
<%
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    if (usuario == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }

    String tab = request.getParameter("tab");
    if (tab == null) tab = "dia";

    boolean esBecado = usuario.getRol() == RolEnum.BECADO;
    boolean esAdmin  = usuario.getRol() == RolEnum.ADMIN;
    AlumnoBecado becado = esBecado ? (AlumnoBecado) session.getAttribute("becado") : null;

    PlatilloDAO platilloDAO = new PlatilloDAO();
    List<Platillo> desayunos = platilloDAO.listarPorMenuActivoYCategoria(CatMenuEnum.DESAYUNO);
    List<Platillo> comidas   = platilloDAO.listarPorMenuActivoYCategoria(CatMenuEnum.COMIDA);
    List<Platillo> carta     = platilloDAO.listarCarta();

    // Agrupar carta por categoría (manteniendo orden del enum)
    Map<CategoriaPlatEnum, List<Platillo>> cartaPorCat = new LinkedHashMap<>();
    for (CategoriaPlatEnum cat : CategoriaPlatEnum.values()) {
        cartaPorCat.put(cat, new java.util.ArrayList<>());
    }
    for (Platillo p : carta) {
        CategoriaPlatEnum cat = p.getCategoria() != null ? p.getCategoria() : CategoriaPlatEnum.OTRO;
        cartaPorCat.get(cat).add(p);
    }

    String diaHoy = java.time.LocalDate.now()
            .getDayOfWeek().getDisplayName(
                    java.time.format.TextStyle.FULL,
                    new java.util.Locale("es","MX"));
    diaHoy = diaHoy.substring(0,1).toUpperCase() + diaHoy.substring(1);
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1.0">
    <title>Menú del Día — Comedor UV</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
    <style>
        .filtros-bar {
            background: white;
            border-radius: 14px;
            padding: 16px;
            box-shadow: var(--sombra-sm);
            margin-bottom: 20px;
        }
        .filtros-row {
            display: grid;
            grid-template-columns: 2fr 1fr 1fr;
            gap: 12px;
            margin-bottom: 12px;
        }
        @media (max-width: 768px) { .filtros-row { grid-template-columns: 1fr; } }

        .cat-chips {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
        }
        .cat-chip {
            padding: 8px 14px;
            border-radius: 20px;
            border: 2px solid var(--color-borde);
            background: white;
            font-size: .8rem;
            font-weight: 600;
            cursor: pointer;
            transition: all .15s;
            color: var(--uv-gris-700);
        }
        .cat-chip:hover { border-color: var(--uv-azul); }
        .cat-chip.activa {
            background: var(--uv-azul);
            color: white;
            border-color: var(--uv-azul);
        }
        .cat-seccion-titulo {
            font-family: var(--fuente-display);
            font-size: 1.05rem;
            font-weight: 800;
            color: var(--uv-azul);
            margin: 24px 0 12px 0;
            padding-bottom: 8px;
            border-bottom: 2px solid var(--color-borde);
        }
        .vacio-busqueda {
            text-align: center;
            padding: 60px 20px;
            color: var(--uv-gris-500);
        }
    </style>
</head>
<body data-context-path="${pageContext.request.contextPath}">

<%@ include file="_header.jsp" %>

<main class="page-wrapper">

    <div class="page-header d-flex justify-between align-center flex-wrap gap-2">
        <div>
            <div class="page-title">Menú de hoy 🍽️</div>
            <div class="page-subtitle"><%= diaHoy %> —
                <% if (desayunos.isEmpty() && comidas.isEmpty()) { %>
                No hay menú registrado para hoy
                <% } else { %>
                <%= desayunos.size() + comidas.size() %> platillos disponibles
                <% } %>
            </div>
        </div>
        <% if (esBecado && becado != null) { %>
        <div class="card" style="padding:12px 18px;border-left:4px solid var(--uv-verde);">
            <div style="font-size:.75rem;color:var(--uv-gris-500);font-weight:600;
                        text-transform:uppercase;letter-spacing:.4px;">Tu beca esta semana</div>
            <div style="font-family:var(--fuente-display);font-weight:700;font-size:1.1rem;
                        color:var(--uv-verde);margin-top:2px;">
                🎫 <%= becado.getComidasRestantesSemana() %> comidas disponibles
            </div>
        </div>
        <% } %>
    </div>

    <!-- Tabs -->
    <div class="tabs">
        <button class="tab-btn <%= tab.equals("dia") ? "activo" : "" %>"
                onclick="cambiarTab('dia', this)">
             Menú del día
        </button>
        <button class="tab-btn <%= tab.equals("carta") ? "activo" : "" %>"
                onclick="cambiarTab('carta', this)">
             A la carta
        </button>
    </div>

    <%--  TAB: MENÚ DEL DÍA  --%>
    <div id="panel-dia" style="<%= tab.equals("dia") ? "" : "display:none;" %>">

        <% if (desayunos.isEmpty() && comidas.isEmpty()) { %>
        <div style="text-align:center;padding:60px 20px;color:var(--uv-gris-500);">
            <div style="font-size:3.5rem;margin-bottom:12px;"></div>
            <div style="font-family:var(--fuente-display);font-weight:700;font-size:1.1rem;">
                Sin menú disponible hoy
            </div>
        </div>
        <% } else { %>

        <% if (!desayunos.isEmpty()) { %>
        <div style="margin-bottom:28px;">
            <div style="display:flex;align-items:center;gap:10px;margin-bottom:16px;">
                <div style="width:4px;height:24px;background:var(--uv-amarillo);border-radius:2px;"></div>
                <h2 style="font-family:var(--fuente-display);font-size:1.1rem;font-weight:700;">
                     Desayuno
                </h2>
            </div>
            <div class="platillos-grid">
                <% for (Platillo p : desayunos) {
                    boolean cubiertoXBeca = esBecado && becado != null && becado.puedeUsarBeca();
                    String precioFinal = cubiertoXBeca ? "0.00" : p.getPrecio().toPlainString();
                %>
                <%@ include file="_platillo-card.jspf" %>
                <% } %>
            </div>
        </div>
        <% } %>

        <% if (!comidas.isEmpty()) { %>
        <div style="margin-bottom:28px;">
            <div style="display:flex;align-items:center;gap:10px;margin-bottom:16px;">
                <div style="width:4px;height:24px;background:var(--uv-verde);border-radius:2px;"></div>
                <h2 style="font-family:var(--fuente-display);font-size:1.1rem;font-weight:700;">
                     Comida
                </h2>
            </div>
            <div class="platillos-grid">
                <% for (Platillo p : comidas) {
                    boolean cubiertoXBeca = esBecado && becado != null && becado.puedeUsarBeca();
                    String precioFinal = cubiertoXBeca ? "0.00" : p.getPrecio().toPlainString();
                %>
                <%@ include file="_platillo-card.jspf" %>
                <% } %>
            </div>
        </div>
        <% } %>

        <% } %>
    </div>

    <%--TAB: A LA CARTA con filtros --%>
    <div id="panel-carta" style="<%= tab.equals("carta") ? "" : "display:none;" %>">

        <div class="alert alert-info" style="margin-bottom:16px;">
             Los platillos a la carta están siempre disponibles y
            <strong>no están cubiertos por la beca alimentaria</strong>.
        </div>

        <% if (carta.isEmpty()) { %>
        <div style="text-align:center;padding:60px 20px;color:var(--uv-gris-500);">
            <div style="font-size:3rem;margin-bottom:12px;"></div>
            <div style="font-weight:600;">Sin platillos a la carta disponibles</div>
        </div>
        <% } else { %>

        <!-- BARRA DE FILTROS -->
        <div class="filtros-bar">
            <div class="filtros-row">
                <div>
                    <label class="form-label" style="font-size:.78rem;">🔍 Buscar</label>
                    <input type="text" id="filtro-busqueda" class="form-control"
                           placeholder="Nombre del platillo..."
                           oninput="filtrarCarta()">
                </div>
                <div>
                    <label class="form-label" style="font-size:.78rem;">Precio mínimo</label>
                    <input type="number" id="filtro-min" class="form-control"
                           placeholder="$0" min="0" step="5"
                           oninput="filtrarCarta()">
                </div>
                <div>
                    <label class="form-label" style="font-size:.78rem;"> Precio máximo</label>
                    <input type="number" id="filtro-max" class="form-control"
                           placeholder="Sin límite" min="0" step="5"
                           oninput="filtrarCarta()">
                </div>
            </div>
            <div>
                <label class="form-label" style="font-size:.78rem;margin-bottom:8px;"> Categoría</label>
                <div class="cat-chips">
                    <button type="button" class="cat-chip activa" data-cat="TODAS"
                            onclick="seleccionarCategoria(this)">Todas</button>
                    <% for (CategoriaPlatEnum cat : CategoriaPlatEnum.values()) {
                        if (cartaPorCat.get(cat).isEmpty()) continue;
                    %>
                    <button type="button" class="cat-chip" data-cat="<%= cat.name() %>"
                            onclick="seleccionarCategoria(this)">
                        <%= cat.getEtiqueta() %>
                        <span style="opacity:.6;margin-left:4px;">(<%= cartaPorCat.get(cat).size() %>)</span>
                    </button>
                    <% } %>
                </div>
            </div>
            <div style="display:flex;justify-content:space-between;align-items:center;
                        margin-top:12px;padding-top:12px;border-top:1px solid var(--color-borde);">
                <div id="contador-resultados" style="font-size:.85rem;color:var(--uv-gris-700);
                                                      font-weight:600;">
                    <%= carta.size() %> platillos
                </div>
                <button type="button" class="btn btn-ghost btn-sm" onclick="limpiarFiltros()">
                    ✕ Limpiar filtros
                </button>
            </div>
        </div>

        <!-- LISTADO POR CATEGORÍA -->
        <div id="lista-carta">
            <% for (CategoriaPlatEnum cat : CategoriaPlatEnum.values()) {
                List<Platillo> pls = cartaPorCat.get(cat);
                if (pls.isEmpty()) continue;
            %>
            <div class="categoria-bloque" data-cat="<%= cat.name() %>">
                <div class="cat-seccion-titulo">
                    <%= cat.getEtiqueta() %>
                    <span style="font-size:.78rem;font-weight:500;color:var(--uv-gris-500);">
                        (<%= pls.size() %> platillos)
                    </span>
                </div>
                <div class="platillos-grid">
                    <% for (Platillo p : pls) {
                        boolean cubiertoXBeca = false;
                        String precioFinal = p.getPrecio().toPlainString();
                    %>
                    <%@ include file="_platillo-card.jspf" %>
                    <% } %>
                </div>
            </div>
            <% } %>
        </div>

        <div id="vacio-busqueda" class="vacio-busqueda" style="display:none;">
            <div style="font-size:2.5rem;margin-bottom:10px;">🔍</div>
            <div style="font-weight:600;">No hay platillos que coincidan con tu búsqueda</div>
            <div style="font-size:.85rem;margin-top:4px;">Intenta con otros filtros</div>
        </div>

        <% } %>
    </div>

</main>

<%@ include file="_footer.jsp" %>

<script>
    function cambiarTab(tab, btn) {
        ['dia','carta'].forEach(t => {
            const el = document.getElementById('panel-' + t);
            if (el) el.style.display = 'none';
        });
        document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('activo'));
        const panel = document.getElementById('panel-' + tab);
        if (panel) panel.style.display = 'block';
        btn.classList.add('activo');
        history.replaceState(null, '', '?tab=' + tab);
    }

    function abrirPlatillo(id, nombre, desc, precio, tiempo, imagen, beca, nutri) {
        abrirModalPlatillo({
            idPlatillo: id, nombre: nombre, descripcion: desc,
            precioFinal: precio, tiempoPrep: tiempo,
            imagen: imagen || null, cubiertoPorBeca: beca, nutri: nutri
        });
    }

    function agregarRapido(id, nombre, precio, beca) {
        const existente = carrito.items.find(i => i.idPlatillo === id);
        if (existente) {
            existente.cantidad++;
        } else {
            carrito.items.push({
                idPlatillo: id, nombre, precio: parseFloat(precio),
                cantidad: 1, cubiertoPorBeca: beca, personalizacion: '', imagen: null
            });
        }
        actualizarBadgeCarrito();
        mostrarMensaje(' ' + nombre + ' agregado', 'exito');
    }

    //  FILTROS DE A LA CARTA
    let categoriaActiva = 'TODAS';

    function seleccionarCategoria(btn) {
        document.querySelectorAll('.cat-chip').forEach(c => c.classList.remove('activa'));
        btn.classList.add('activa');
        categoriaActiva = btn.dataset.cat;
        filtrarCarta();
    }

    function filtrarCarta() {
        const busqueda = (document.getElementById('filtro-busqueda').value || '').toLowerCase().trim();
        const precioMin = parseFloat(document.getElementById('filtro-min').value) || 0;
        const precioMax = parseFloat(document.getElementById('filtro-max').value) || Infinity;

        let mostradas = 0;
        document.querySelectorAll('.categoria-bloque').forEach(bloque => {
            const cat = bloque.dataset.cat;
            let visiblesEnBloque = 0;

            bloque.querySelectorAll('.platillo-card').forEach(card => {
                const nombre = (card.dataset.nombre || '').toLowerCase();
                const precio = parseFloat(card.dataset.precio) || 0;
                const cardCat = card.dataset.categoria;

                const matchBusqueda = !busqueda || nombre.includes(busqueda);
                const matchPrecio   = precio >= precioMin && precio <= precioMax;
                const matchCat      = categoriaActiva === 'TODAS' || cardCat === categoriaActiva;

                const visible = matchBusqueda && matchPrecio && matchCat;
                card.style.display = visible ? '' : 'none';
                if (visible) { visiblesEnBloque++; mostradas++; }
            });

            bloque.style.display = visiblesEnBloque > 0 ? '' : 'none';
        });

        document.getElementById('contador-resultados').textContent =
            mostradas + ' platillo' + (mostradas !== 1 ? 's' : '');
        document.getElementById('vacio-busqueda').style.display = mostradas === 0 ? 'block' : 'none';
    }

    function limpiarFiltros() {
        document.getElementById('filtro-busqueda').value = '';
        document.getElementById('filtro-min').value = '';
        document.getElementById('filtro-max').value = '';
        document.querySelectorAll('.cat-chip').forEach(c => c.classList.remove('activa'));
        document.querySelector('.cat-chip[data-cat="TODAS"]').classList.add('activa');
        categoriaActiva = 'TODAS';
        filtrarCarta();
    }
</script>

<%!
    private String escapeJS(String s) {
        if (s == null) return "";
        return s.replace("\\","\\\\").replace("'","\\'")
                .replace("\n","\\n").replace("\r","");
    }

    private String buildNutriJSON(InformacionNutricional n) {
        if (n == null) return "null";
        return "{" +
                "calorias:"      + (n.getCalorias()      != null ? n.getCalorias()      : 0) + "," +
                "proteinas:"     + (n.getProteinas()     != null ? n.getProteinas()     : 0) + "," +
                "carbohidratos:" + (n.getCarbohidratos() != null ? n.getCarbohidratos() : 0) + "," +
                "grasas:"        + (n.getGrasas()        != null ? n.getGrasas()        : 0) + "," +
                "fibra:"         + (n.getFibra()         != null ? n.getFibra()         : 0) + "," +
                "sodio:"         + (n.getSodio()         != null ? n.getSodio()         : 0) + "," +
                "azucar:"        + (n.getAzucar()        != null ? n.getAzucar()        : 0) + "," +
                "huellaCarbonoKg:" + (n.getHuellaCarbonoKg() != null ? n.getHuellaCarbonoKg() : 0) + "," +
                "nivelHuella:'"  + (n.getNivelHuella()   != null ? n.getNivelHuella().name() : "") + "'," +
                "esVegetariano:" + n.isEsVegetariano()  + "," +
                "esVegano:"      + n.isEsVegano()        + "," +
                "esGlutenFree:"  + n.isEsGlutenFree()    + "," +
                "alergenos:'"    + (n.getAlergenos()     != null ? n.getAlergenos() : "") + "'" +
                "}";
    }
%>

</body>
</html>
