<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="mx.uv.comedor.model.*" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.LinkedHashSet" %>
<%
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    if (usuario == null) { response.sendRedirect(request.getContextPath()+"/login"); return; }
    List<Platillo> platillos = (List<Platillo>) request.getAttribute("platillos");
    if (platillos == null) platillos = new java.util.ArrayList<>();

    Set<CategoriaPlatEnum> categorias = new LinkedHashSet<>();
    for (Platillo p : platillos) {
        if (p.getCategoria() != null) categorias.add(p.getCategoria());
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Disponibilidad - Cocina</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
    <style>
        .buscador-bar {
            background: white;
            border-radius: 12px;
            padding: 14px;
            box-shadow: var(--sombra-sm);
            margin-bottom: 18px;
            display: flex;
            gap: 10px;
            align-items: center;
            flex-wrap: wrap;
        }
        .buscador-bar input[type="text"] {
            flex: 1;
            min-width: 180px;
        }
        .cat-chips {
            display: flex;
            gap: 6px;
            flex-wrap: wrap;
        }
        .cat-chip {
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
        .cat-chip:hover { border-color: var(--uv-azul); }
        .cat-chip.activa {
            background: var(--uv-azul);
            color: white;
            border-color: var(--uv-azul);
        }

        .plat-row {
            display: grid;
            grid-template-columns: 1fr auto auto;
            gap: 14px;
            align-items: center;
            background: white;
            border-radius: 12px;
            padding: 14px 18px;
            box-shadow: var(--sombra-sm);
            margin-bottom: 10px;
        }
        .plat-row.no-disponible {
            opacity: 0.6;
        }
        .plat-row .nombre { font-weight: 700; }
        .plat-row .cat {
            font-size: .72rem;
            color: var(--uv-gris-500);
            margin-top: 2px;
        }
        .plat-row .precio {
            font-family: var(--fuente-display);
            font-weight: 700;
            color: var(--uv-azul);
        }
        .toggle-btn {
            border: none;
            padding: 10px 22px;
            border-radius: 10px;
            font-weight: 700;
            font-size: .85rem;
            cursor: pointer;
            font-family: var(--fuente-display);
            transition: all .15s;
        }
        .toggle-btn.disponible {
            background: var(--uv-verde-light);
            color: var(--uv-verde-dark);
        }
        .toggle-btn.disponible:hover { background: var(--uv-rojo-light); color: var(--uv-rojo); }
        .toggle-btn.no-disp {
            background: var(--uv-rojo-light);
            color: var(--uv-rojo);
        }
        .toggle-btn.no-disp:hover { background: var(--uv-verde-light); color: var(--uv-verde-dark); }

        .sin-resultados {
            text-align: center;
            padding: 40px;
            color: var(--uv-gris-500);
            background: white;
            border-radius: 12px;
            display: none;
        }
    </style>
</head>
<body data-context-path="${pageContext.request.contextPath}">

<%@ include file="../_header.jsp" %>

<main class="page-wrapper">

    <div class="page-header">
        <div class="page-title">Disponibilidad - A la Carta</div>
        <div class="page-subtitle">
            Cuando un platillo se agote en cocina, desactivalo aqui. Dejara
            de aparecer en el menu de los clientes y en el POS.
        </div>
    </div>

    <!-- Buscador y filtros -->
    <div class="buscador-bar">
        <input type="text" id="buscador" class="form-control"
               placeholder="Buscar platillo..."
               oninput="filtrar()">
        <div class="cat-chips" id="chips">
            <button type="button" class="cat-chip activa" data-cat="TODAS" onclick="seleccionarCat(this)">Todas</button>
            <% for (CategoriaPlatEnum cat : categorias) { %>
            <button type="button" class="cat-chip" data-cat="<%= cat.name() %>" onclick="seleccionarCat(this)">
                <%= cat.getEtiqueta() %>
            </button>
            <% } %>
        </div>
    </div>

    <div id="lista-platillos">
        <% for (Platillo p : platillos) { %>
        <div class="plat-row <%= !p.isDisponible() ? "no-disponible" : "" %>"
             data-nombre="<%= p.getNombre().toLowerCase() %>"
             data-categoria="<%= p.getCategoria() != null ? p.getCategoria().name() : "OTRO" %>">
            <div>
                <div class="nombre"><%= p.getNombre() %></div>
                <% if (p.getCategoria() != null) { %>
                <div class="cat"><%= p.getCategoria().getEtiqueta() %></div>
                <% } %>
            </div>
            <div class="precio">$<%= p.getPrecio().toPlainString() %></div>
            <form method="post"
                  action="${pageContext.request.contextPath}/empleado/disponibilidad/toggle"
                  style="margin:0;">
                <input type="hidden" name="idPlatillo" value="<%= p.getIdPlatillo() %>">
                <button type="submit"
                        class="toggle-btn <%= p.isDisponible() ? "disponible" : "no-disp" %>">
                    <%= p.isDisponible() ? "Disponible" : "Agotado" %>
                </button>
            </form>
        </div>
        <% } %>

        <div id="sin-resultados" class="sin-resultados">
            No se encontraron platillos con esos criterios.
        </div>
    </div>

    <% if (platillos.isEmpty()) { %>
    <div class="card">
        <div style="text-align:center;padding:40px;color:var(--uv-gris-500);">
            <div style="font-weight:600;margin-bottom:4px;">Sin platillos a la carta</div>
            <div style="font-size:.85rem;">
                El admin aun no ha registrado platillos de tipo "A la Carta".
            </div>
        </div>
    </div>
    <% } %>

</main>

<%@ include file="../_footer.jsp" %>

<script>
    let categoriaActiva = 'TODAS';

    function seleccionarCat(btn) {
        document.querySelectorAll('.cat-chip').forEach(c => c.classList.remove('activa'));
        btn.classList.add('activa');
        categoriaActiva = btn.dataset.cat;
        filtrar();
    }

    function filtrar() {
        const busqueda = (document.getElementById('buscador').value || '').toLowerCase().trim();
        let mostradas = 0;
        document.querySelectorAll('.plat-row').forEach(row => {
            const nombre = row.dataset.nombre || '';
            const cat    = row.dataset.categoria;
            const matchBusqueda = !busqueda || nombre.includes(busqueda);
            const matchCat = categoriaActiva === 'TODAS' || cat === categoriaActiva;
            const visible = matchBusqueda && matchCat;
            row.style.display = visible ? '' : 'none';
            if (visible) mostradas++;
        });
        document.getElementById('sin-resultados').style.display = mostradas === 0 ? 'block' : 'none';
    }
</script>
</body>
</html>
