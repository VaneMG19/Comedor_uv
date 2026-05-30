<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="mx.uv.comedor.model.Usuario, mx.uv.comedor.model.RolEnum" %>
<%
    Usuario _usuario = (Usuario) session.getAttribute("usuario");
    if (_usuario == null) { response.sendRedirect(request.getContextPath() + "/login"); return; }
    String _rol = _usuario.getRol().name();
    String _iniciales = ("" + _usuario.getNombre().charAt(0) + _usuario.getApellidos().charAt(0)).toUpperCase();
    String _path = request.getServletPath();
    boolean _mostrarCampana = !_rol.equals("ADMIN") && !_rol.equals("EMPLEADO");
    boolean _mostrarCarrito = !_rol.equals("ADMIN") && !_rol.equals("EMPLEADO");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Comedor UV</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body data-context-path="${pageContext.request.contextPath}">
<nav class="navbar">
    <div style="display:flex;align-items:center;gap:12px;">
        <button class="notif-btn" id="sidebar-toggle" style="display:none;"
                onclick="toggleSidebar()">Menu</button>
        <a href="${pageContext.request.contextPath}/menu" class="navbar-brand">
            <div class="logo-icon">UV</div>
            <div class="logo-text">Comedor Universitario<span>Universidad Veracruzana</span></div>
        </a>
    </div>
    <div class="navbar-actions">
        <% if (_mostrarCampana) { %>
        <div style="position:relative;">
            <button class="notif-btn" id="notif-campana" title="Notificaciones"
                    onclick="event.stopPropagation();
                             if (window.NotificacionesPolling) NotificacionesPolling.toggleDropdown();">
                🔔<span id="notif-badge"></span>
            </button>
            <div id="notif-dropdown" style="display:none;position:absolute;
                                            top:48px;right:0;width:340px;
                                            background:white;border-radius:14px;
                                            box-shadow:0 8px 30px rgba(0,0,0,.18);
                                            z-index:1000;overflow:hidden;">
            </div>
        </div>
        <% } %>

        <% if (_mostrarCarrito) { %>
        <button class="notif-btn" id="btn-carrito" onclick="toggleCarrito()" title="Carrito">
            🛒<span id="carrito-badge" style="display:none;"></span>
        </button>
        <% } %>

        <div class="user-menu">
            <div class="user-avatar" onclick="toggleDropdown()"><%= _iniciales %></div>
            <div class="user-dropdown" id="user-dropdown">
                <div class="user-dropdown-header">
                    <div class="nombre"><%= _usuario.getNombreCompleto() %></div>
                    <div class="rol"><%= _usuario.getEmail() %></div>
                </div>
                <a href="${pageContext.request.contextPath}/perfil">Mi perfil</a>
                <% if (_rol.equals("ADMIN"))    { %><a href="${pageContext.request.contextPath}/admin/dashboard">Panel Admin</a><% } %>
                <% if (_rol.equals("EMPLEADO")) { %><a href="${pageContext.request.contextPath}/empleado/dashboard">Panel Cocina</a><% } %>
                <hr>
                <a href="${pageContext.request.contextPath}/logout" class="peligro">Cerrar sesion</a>
            </div>
        </div>
    </div>
</nav>
<aside class="sidebar" id="sidebar">
    <% if (_rol.equals("ADMIN")) { %>
    <div class="sidebar-section">
        <div class="sidebar-section-title">Principal</div>
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="sidebar-link <%= _path.contains("dashboard") ? "activo" : "" %>">Dashboard</a>
        <a href="${pageContext.request.contextPath}/menu" class="sidebar-link">Ver Menu</a>
    </div>
    <div class="sidebar-section">
        <div class="sidebar-section-title">Gestion</div>
        <a href="${pageContext.request.contextPath}/admin/menu/semanal" class="sidebar-link <%= _path.contains("/admin/menu/semanal") ? "activo" : "" %>">Programar Menu Semanal</a>
        <a href="${pageContext.request.contextPath}/admin/menu" class="sidebar-link <%= (_path.equals("/admin/menu") || _path.contains("/admin/platillos")) ? "activo" : "" %>">Platillos y Menu</a>
        <a href="${pageContext.request.contextPath}/admin/recetas" class="sidebar-link <%= _path.contains("/admin/recetas") ? "activo" : "" %>">Recetas</a>
        <a href="${pageContext.request.contextPath}/admin/usuarios" class="sidebar-link <%= _path.contains("usuarios") ? "activo" : "" %>">Usuarios</a>
        <a href="${pageContext.request.contextPath}/admin/becados" class="sidebar-link <%= _path.contains("becados") ? "activo" : "" %>">Becados Autorizados</a>
        <a href="${pageContext.request.contextPath}/admin/inventario" class="sidebar-link <%= _path.contains("inventario") ? "activo" : "" %>">Inventario</a>
        <a href="${pageContext.request.contextPath}/calificaciones" class="sidebar-link <%= _path.contains("calificaciones") ? "activo" : "" %>">Calificaciones</a>
    </div>
    <% } else if (_rol.equals("EMPLEADO")) { %>
    <div class="sidebar-section">
        <div class="sidebar-section-title">Cocina</div>
        <a href="${pageContext.request.contextPath}/empleado/dashboard" class="sidebar-link <%= _path.contains("/empleado/dashboard") ? "activo" : "" %>">Pedidos Activos</a>
        <a href="${pageContext.request.contextPath}/empleado/disponibilidad" class="sidebar-link <%= _path.contains("/empleado/disponibilidad") ? "activo" : "" %>">Disponibilidad</a>
    </div>
    <div class="sidebar-section">
        <div class="sidebar-section-title">Caja</div>
        <a href="${pageContext.request.contextPath}/pos" class="sidebar-link <%= _path.startsWith("/pos") ? "activo" : "" %>">Punto de Venta</a>
    </div>
    <div class="sidebar-section">
        <a href="${pageContext.request.contextPath}/menu" class="sidebar-link">Ver Menu</a>
    </div>
    <% } else { %>
    <div class="sidebar-section">
        <div class="sidebar-section-title">Menu</div>
        <a href="${pageContext.request.contextPath}/menu" class="sidebar-link <%= _path.equals("/menu") ? "activo" : "" %>">Menu del Dia</a>
        <a href="${pageContext.request.contextPath}/menu?tab=carta" class="sidebar-link">A la Carta</a>
    </div>
    <div class="sidebar-section">
        <div class="sidebar-section-title">Mis Pedidos</div>
        <a href="${pageContext.request.contextPath}/pedido/historial" class="sidebar-link <%= _path.contains("historial") ? "activo" : "" %>">Historial</a>
    </div>
    <% if (_rol.equals("BECADO")) { %>
    <div class="sidebar-section">
        <div class="sidebar-section-title">Mi Beca</div>
        <a href="${pageContext.request.contextPath}/becado/calendario" class="sidebar-link <%= _path.contains("calendario") ? "activo" : "" %>">Mi Calendario</a>
    </div>
    <% } %>
    <div class="sidebar-section">
        <div class="sidebar-section-title">Cuenta</div>
        <a href="${pageContext.request.contextPath}/perfil" class="sidebar-link <%= _path.contains("perfil") ? "activo" : "" %>">Mi Perfil</a>
    </div>
    <% } %>
    <script src="${pageContext.request.contextPath}/js/accesibilidad.js" defer></script>

</aside>
<div id="sidebar-overlay" onclick="toggleSidebar()"
     style="display:none;position:fixed;inset:0;background:rgba(0,0,0,.4);z-index:850;"></div>
