<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="mx.uv.comedor.model.*" %>
<%@ page import="java.util.List" %>
<%
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    if (usuario == null || usuario.getRol() != RolEnum.ADMIN) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    List<Usuario> usuarios = (List<Usuario>) request.getAttribute("usuarios");
    if (usuarios == null) usuarios = new java.util.ArrayList<>();

    String rolFiltro    = (String) request.getAttribute("rolFiltro");
    if (rolFiltro == null) rolFiltro = "TODOS";
    String busqueda     = (String) request.getAttribute("busqueda");
    if (busqueda == null) busqueda = "";

    Integer totalUsuarios = (Integer) request.getAttribute("totalUsuarios");
    if (totalUsuarios == null) totalUsuarios = usuarios.size();

    Long countEst = (Long) request.getAttribute("countEst"); if (countEst == null) countEst = 0L;
    Long countBec = (Long) request.getAttribute("countBec"); if (countBec == null) countBec = 0L;
    Long countDoc = (Long) request.getAttribute("countDoc"); if (countDoc == null) countDoc = 0L;
    Long countEmp = (Long) request.getAttribute("countEmp"); if (countEmp == null) countEmp = 0L;
    Long countAdm = (Long) request.getAttribute("countAdm"); if (countAdm == null) countAdm = 0L;

    String exito = request.getParameter("exito");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1.0">
    <title>Usuarios — Comedor UV</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
    <style>
        .user-tabs {
            display: flex;
            background: white;
            border-radius: 12px;
            padding: 6px;
            gap: 4px;
            box-shadow: var(--sombra-sm);
            margin-bottom: 16px;
            overflow-x: auto;
        }
        .user-tab {
            padding: 10px 14px;
            border: none;
            background: transparent;
            border-radius: 8px;
            font-family: var(--fuente-display);
            font-weight: 600;
            font-size: .82rem;
            color: var(--uv-gris-700);
            cursor: pointer;
            white-space: nowrap;
            text-decoration: none;
            transition: all .15s;
            display: inline-flex;
            align-items: center;
            gap: 6px;
        }
        .user-tab:hover { background: var(--uv-gris-100); }
        .user-tab.activa { background: var(--uv-azul); color: white; }
        .user-tab .count {
            background: rgba(255,255,255,.25);
            padding: 2px 7px;
            border-radius: 10px;
            font-size: .72rem;
            font-weight: 700;
        }
        .user-tab:not(.activa) .count {
            background: var(--uv-gris-200);
            color: var(--uv-gris-700);
        }
        .rol-pill {
            font-size: .68rem;
            font-weight: 700;
            padding: 3px 8px;
            border-radius: 10px;
            text-transform: uppercase;
            letter-spacing: .3px;
        }
        .rol-ESTUDIANTE { background: var(--uv-azul-light); color: var(--uv-azul); }
        .rol-BECADO     { background: var(--uv-verde-light); color: var(--uv-verde-dark); }
        .rol-DOCENTE    { background: #fef3c7; color: #92400e; }
        .rol-EMPLEADO   { background: #fce7f3; color: #9d174d; }
        .rol-ADMIN      { background: #ddd6fe; color: #5b21b6; }
    </style>
</head>
<body data-context-path="${pageContext.request.contextPath}">

<%@ include file="../_header.jsp" %>

<main class="page-wrapper">

    <div class="page-header d-flex justify-between align-center flex-wrap gap-2">
        <div>
            <div class="page-title">Gestión de Usuarios 👥</div>
            <div class="page-subtitle">
                <%= totalUsuarios %> usuarios en total —
                Mostrando <%= usuarios.size() %>
            </div>
        </div>
    </div>

    <% if (exito != null) { %>
    <div class="alert alert-exito" data-auto-close> <%= exito %></div>
    <% } %>

    <!-- Tabs por rol -->
    <div class="user-tabs">
        <a href="${pageContext.request.contextPath}/admin/usuarios"
           class="user-tab <%= "TODOS".equals(rolFiltro) ? "activa" : "" %>">
             Todos <span class="count"><%= totalUsuarios %></span>
        </a>
        <a href="${pageContext.request.contextPath}/admin/usuarios?rol=ESTUDIANTE"
           class="user-tab <%= "ESTUDIANTE".equals(rolFiltro) ? "activa" : "" %>">
             Estudiantes <span class="count"><%= countEst %></span>
        </a>
        <a href="${pageContext.request.contextPath}/admin/usuarios?rol=BECADO"
           class="user-tab <%= "BECADO".equals(rolFiltro) ? "activa" : "" %>">
             Becados <span class="count"><%= countBec %></span>
        </a>
        <a href="${pageContext.request.contextPath}/admin/usuarios?rol=DOCENTE"
           class="user-tab <%= "DOCENTE".equals(rolFiltro) ? "activa" : "" %>">
             Docentes <span class="count"><%= countDoc %></span>
        </a>
        <a href="${pageContext.request.contextPath}/admin/usuarios?rol=EMPLEADO"
           class="user-tab <%= "EMPLEADO".equals(rolFiltro) ? "activa" : "" %>">
             Empleados <span class="count"><%= countEmp %></span>
        </a>
        <a href="${pageContext.request.contextPath}/admin/usuarios?rol=ADMIN"
           class="user-tab <%= "ADMIN".equals(rolFiltro) ? "activa" : "" %>">
             Admins <span class="count"><%= countAdm %></span>
        </a>
    </div>

    <!-- Búsqueda -->
    <form method="get" action="${pageContext.request.contextPath}/admin/usuarios"
          style="margin-bottom:16px;display:flex;gap:8px;">
        <% if (!"TODOS".equals(rolFiltro)) { %>
        <input type="hidden" name="rol" value="<%= rolFiltro %>">
        <% } %>
        <input type="text" name="q" class="form-control"
               placeholder=" Buscar por nombre o email..."
               value="<%= busqueda %>"
               style="flex:1;">
        <button type="submit" class="btn btn-primario">Buscar</button>
        <% if (!busqueda.isEmpty()) { %>
        <a href="${pageContext.request.contextPath}/admin/usuarios<%= !"TODOS".equals(rolFiltro) ? "?rol=" + rolFiltro : "" %>"
           class="btn btn-ghost">✕ Limpiar</a>
        <% } %>
    </form>

    <!-- Tabla -->
    <div class="card">
        <div style="overflow-x:auto;">
            <table class="tabla">
                <thead>
                <tr>
                    <th>Nombre</th>
                    <th>Email</th>
                    <th>Rol</th>
                    <th>Estado</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <% if (usuarios.isEmpty()) { %>
                <tr>
                    <td colspan="5" style="text-align:center;padding:40px;color:var(--uv-gris-500);">
                        <div style="font-size:2rem;margin-bottom:8px;">🔍</div>
                        <div>
                            No se encontraron usuarios
                            <% if (!busqueda.isEmpty()) { %>
                            con la búsqueda "<%= busqueda %>"
                            <% } else if (!"TODOS".equals(rolFiltro)) { %>
                            con rol <strong><%= rolFiltro %></strong>
                            <% } %>
                        </div>
                    </td>
                </tr>
                <% } else for (Usuario u : usuarios) { %>
                <tr>
                    <td>
                        <div style="display:flex;align-items:center;gap:10px;">
                            <div style="width:36px;height:36px;border-radius:50%;
                                            background:linear-gradient(135deg, var(--uv-azul), var(--uv-verde));
                                            color:white;display:flex;align-items:center;
                                            justify-content:center;font-weight:700;font-size:.78rem;
                                            font-family:var(--fuente-display);">
                                <%= (u.getNombre().charAt(0) + "" + u.getApellidos().charAt(0)).toUpperCase() %>
                            </div>
                            <div>
                                <div style="font-weight:600;"><%= u.getNombreCompleto() %></div>
                                <% if (u.getTelefono() != null && !u.getTelefono().isBlank()) { %>
                                <div style="font-size:.72rem;color:var(--uv-gris-500);">
                                     <%= u.getTelefono() %>
                                </div>
                                <% } %>
                            </div>
                        </div>
                    </td>
                    <td style="font-size:.85rem;"><%= u.getEmail() %></td>
                    <td>
                            <span class="rol-pill rol-<%= u.getRol().name() %>">
                                <%= u.getRol().name() %>
                            </span>
                    </td>
                    <td>
                        <% if (u.isActivo()) { %>
                        <span style="color:var(--uv-verde);font-size:.85rem;font-weight:600;">
                                ● Activo
                            </span>
                        <% } else { %>
                        <span style="color:var(--uv-gris-500);font-size:.85rem;">
                                ○ Inactivo
                            </span>
                        <% } %>
                    </td>
                    <td style="text-align:right;">
                        <a href="${pageContext.request.contextPath}/admin/usuarios?id=<%= u.getIdUsuario() %>"
                           class="btn btn-ghost btn-sm">Ver detalle</a>
                    </td>
                </tr>
                <% } %>
                </tbody>
            </table>
        </div>
    </div>

</main>

<%@ include file="../_footer.jsp" %>
</body>
</html>
