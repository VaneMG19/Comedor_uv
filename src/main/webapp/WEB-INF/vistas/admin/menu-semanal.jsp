<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="mx.uv.comedor.model.*" %>
<%@ page import="java.util.*" %>
<%
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    if (usuario == null || usuario.getRol() != RolEnum.ADMIN) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    Long idMenu = (Long) request.getAttribute("idMenu");
    Map<DiaEnum, Map<CatMenuEnum, List<Platillo>>> programacion =
            (Map<DiaEnum, Map<CatMenuEnum, List<Platillo>>>) request.getAttribute("programacion");
    List<Platillo> platillosDisponibles = (List<Platillo>) request.getAttribute("platillosDisponibles");

    String diaSelStr = request.getParameter("dia");
    DiaEnum diaSel = DiaEnum.LUNES;
    if (diaSelStr != null) {
        try { diaSel = DiaEnum.valueOf(diaSelStr); }
        catch (Exception e) { diaSel = DiaEnum.LUNES; }
    }
    String error = request.getParameter("error");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1.0">
    <title>Programar Menu Semanal - Comedor UV</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
    <style>
        .dia-tabs {
            display: flex;
            background: white;
            border-radius: 12px;
            padding: 5px;
            gap: 4px;
            box-shadow: var(--sombra-sm);
            margin-bottom: 20px;
            overflow-x: auto;
        }
        .dia-tab {
            padding: 10px 16px;
            border: none;
            background: transparent;
            border-radius: 8px;
            font-family: var(--fuente-display);
            font-weight: 600;
            font-size: .85rem;
            color: var(--uv-gris-700);
            cursor: pointer;
            text-decoration: none;
            transition: all .15s;
            white-space: nowrap;
            display: inline-flex;
            align-items: center;
            gap: 6px;
        }
        .dia-tab:hover { background: var(--uv-gris-100); }
        .dia-tab.activa { background: var(--uv-azul); color: white; }
        .dia-tab .count {
            background: rgba(255,255,255,.25);
            padding: 2px 7px;
            border-radius: 10px;
            font-size: .72rem;
            font-weight: 700;
        }
        .dia-tab:not(.activa) .count {
            background: var(--uv-gris-200);
            color: var(--uv-gris-700);
        }

        .cat-section {
            background: white;
            border-radius: 14px;
            padding: 20px;
            box-shadow: var(--sombra-sm);
            margin-bottom: 18px;
        }
        .cat-section-titulo {
            font-family: var(--fuente-display);
            font-size: 1.05rem;
            font-weight: 800;
            color: var(--uv-azul);
            padding-bottom: 8px;
            border-bottom: 2px solid var(--color-borde);
            margin-bottom: 14px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .platillo-asignado {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 12px 14px;
            background: var(--uv-gris-100);
            border-radius: 10px;
            margin-bottom: 8px;
            font-size: .9rem;
            gap: 10px;
            flex-wrap: wrap;
        }
        .platillo-asignado .info { flex: 1; min-width: 180px; }
        .platillo-asignado .nombre {
            font-weight: 700;
        }
        .platillo-asignado .cupo-info {
            font-size: .72rem;
            color: var(--uv-gris-500);
            margin-top: 2px;
        }
        .cupo-info.agotado { color: var(--uv-rojo); font-weight: 700; }
        .platillo-asignado .precio {
            font-weight: 700;
            color: var(--uv-azul);
        }
        .sin-platillos {
            text-align: center;
            padding: 30px;
            color: var(--uv-gris-500);
            font-style: italic;
            background: var(--uv-gris-100);
            border-radius: 10px;
            font-size: .85rem;
        }
        .agregar-form {
            display: grid;
            grid-template-columns: 1fr 110px auto;
            gap: 8px;
            margin-top: 12px;
            align-items: end;
        }
        .agregar-form label {
            font-size: .72rem;
            color: var(--uv-gris-500);
            font-weight: 600;
            display: block;
            margin-bottom: 4px;
        }
        @media (max-width: 600px) {
            .agregar-form { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body data-context-path="${pageContext.request.contextPath}">

<%@ include file="../_header.jsp" %>

<main class="page-wrapper">

    <div class="page-header">
        <div class="page-title">Programar Menu Semanal</div>
        <div class="page-subtitle">
            Asigna platillos por dia y categoria con su <strong>cupo</strong>
            (cantidad de porciones disponibles).
        </div>
    </div>

    <% if (error != null) { %>
    <div class="alert alert-error" data-auto-close><%= error %></div>
    <% } %>

    <div class="dia-tabs">
        <% for (DiaEnum d : DiaEnum.values()) {
            int total = programacion.get(d).get(CatMenuEnum.DESAYUNO).size()
                    + programacion.get(d).get(CatMenuEnum.COMIDA).size();
            boolean activo = d == diaSel;
        %>
        <a href="${pageContext.request.contextPath}/admin/menu/semanal?dia=<%= d.name() %>"
           class="dia-tab <%= activo ? "activa" : "" %>">
            <%= d.getEtiqueta() %>
            <span class="count"><%= total %></span>
        </a>
        <% } %>
    </div>

    <% for (CatMenuEnum cat : new CatMenuEnum[]{CatMenuEnum.DESAYUNO, CatMenuEnum.COMIDA}) {
        List<Platillo> asignados = programacion.get(diaSel).get(cat);
        String labelCat = cat == CatMenuEnum.DESAYUNO ? "Desayuno" : "Comida";
    %>
    <div class="cat-section">
        <div class="cat-section-titulo">
            <span><%= labelCat %> - <%= diaSel.getEtiqueta() %></span>
            <span style="font-size:.78rem;font-weight:500;color:var(--uv-gris-500);">
                <%= asignados.size() %> platillo<%= asignados.size() == 1 ? "" : "s" %>
            </span>
        </div>

        <% if (asignados.isEmpty()) { %>
        <div class="sin-platillos">
            No hay platillos asignados a <%= labelCat.toLowerCase() %> de <%= diaSel.getEtiqueta() %>
        </div>
        <% } else {
            for (Platillo p : asignados) {
                int cupo = p.getCupo() != null ? p.getCupo() : 0;
                int vend = p.getVendidos() != null ? p.getVendidos() : 0;
                int rest = Math.max(0, cupo - vend);
                boolean agotado = rest == 0;
        %>
        <div class="platillo-asignado">
            <div class="info">
                <div class="nombre"><%= p.getNombre() %></div>
                <div class="cupo-info <%= agotado ? "agotado" : "" %>">
                    Cupo: <%= cupo %> - Vendidos: <%= vend %> - Restantes: <strong><%= rest %></strong>
                    <% if (agotado) { %>(AGOTADO)<% } %>
                </div>
            </div>
            <div class="precio">$<%= p.getPrecio().toPlainString() %></div>
            <form method="post"
                  action="${pageContext.request.contextPath}/admin/menu/semanal/quitar"
                  style="display:inline;"
                  onsubmit="return confirm('Quitar este platillo del menu?');">
                <input type="hidden" name="dia" value="<%= diaSel.name() %>">
                <input type="hidden" name="categoria" value="<%= cat.name() %>">
                <input type="hidden" name="idPlatillo" value="<%= p.getIdPlatillo() %>">
                <button type="submit" class="btn btn-ghost btn-sm" style="color:var(--uv-rojo);">
                    Quitar
                </button>
            </form>
        </div>
        <%   } } %>

        <% if (platillosDisponibles != null && !platillosDisponibles.isEmpty()) { %>
        <form method="post" action="${pageContext.request.contextPath}/admin/menu/semanal/agregar"
              class="agregar-form">
            <input type="hidden" name="dia" value="<%= diaSel.name() %>">
            <input type="hidden" name="categoria" value="<%= cat.name() %>">
            <div>
                <label>Platillo</label>
                <select name="idPlatillo" class="form-control" required>
                    <option value="">-- Selecciona un platillo --</option>
                    <% for (Platillo p : platillosDisponibles) {
                        boolean yaAsignado = false;
                        for (Platillo asig : asignados) {
                            if (asig.getIdPlatillo().equals(p.getIdPlatillo())) {
                                yaAsignado = true; break;
                            }
                        }
                        if (yaAsignado) continue;
                    %>
                    <option value="<%= p.getIdPlatillo() %>">
                        <%= p.getNombre() %> ($<%= p.getPrecio().toPlainString() %>)
                    </option>
                    <% } %>
                </select>
            </div>
            <div>
                <label>Cupo</label>
                <input type="number" name="cupo" class="form-control" value="50" min="1" max="999" required>
            </div>
            <div>
                <button type="submit" class="btn btn-primario">+ Agregar</button>
            </div>
        </form>
        <% } else { %>
        <div class="alert alert-info" style="margin-top:12px;font-size:.85rem;">
            No hay platillos de tipo "Menu del Dia" disponibles.
            <a href="${pageContext.request.contextPath}/admin/platillos/nuevo"
               style="color:var(--uv-azul);font-weight:600;">
                Crear uno
            </a>.
        </div>
        <% } %>
    </div>
    <% } %>

    <div class="alert alert-info" style="margin-top:20px;">
        <strong>Como funciona el cupo:</strong> cada vez que se vende este platillo
        (online o en POS), el sistema descuenta del cupo. Cuando se agota,
        los clientes lo veran como AGOTADO y no podran pedirlo.
    </div>

</main>

<%@ include file="../_footer.jsp" %>
</body>
</html>
