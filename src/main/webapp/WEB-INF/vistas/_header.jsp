<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="mx.uv.comedor.model.Usuario, mx.uv.comedor.model.RolEnum" %>
<%
    Usuario _usuario = (Usuario) session.getAttribute("usuario");
    if (_usuario == null) { response.sendRedirect(request.getContextPath() + "/login"); return; }
    String _rol = _usuario.getRol().name();
    String _iniciales = ("" + _usuario.getNombre().charAt(0) + _usuario.getApellidos().charAt(0)).toUpperCase();
    String _path = request.getServletPath();
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
                onclick="toggleSidebar()">☰</button>
        <a href="${pageContext.request.contextPath}/menu" class="navbar-brand">
            <div class="logo-icon">UV</div>
            <div class="logo-text">Comedor Universitario<span>Universidad Veracruzana</span></div>
        </a>
    </div>
    <div class="navbar-actions">
        <div style="position:relative;">
            <button class="notif-btn" id="notif-campana" title="Notificaciones"
                    onclick="event.stopPropagation();
                             if (window.NotificacionesPolling) NotificacionesPolling.toggleDropdown();
                             else console.warn('NotificacionesPolling no esta cargado todavia');">
                🔔<span id="notif-badge"></span>
            </button>
            <div id="notif-dropdown" style="display:none;position:absolute;
                                            top:48px;right:0;width:340px;
                                            background:white;border-radius:14px;
                                            box-shadow:0 8px 30px rgba(0,0,0,.18);
                                            z-index:1000;overflow:hidden;">
            </div>
        </div>

        <% if (!_rol.equals("EMPLEADO") && !_rol.equals("ADMIN")) { %>
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
                <a href="${pageContext.request.contextPath}/perfil"> Mi perfil</a>
                <% if (_rol.equals("ADMIN"))    { %><a href="${pageContext.request.contextPath}/admin/dashboard">Panel Admin</a><% } %>
                <% if (_rol.equals("EMPLEADO")) { %><a href="${pageContext.request.contextPath}/empleado/dashboard"> Panel Cocina</a><% } %>
                <hr>
                <a href="${pageContext.request.contextPath}/logout" class="peligro"> Cerrar sesión</a>
            </div>
        </div>
    </div>
</nav>
<aside class="sidebar" id="sidebar">
    <% if (_rol.equals("ADMIN")) { %>
    <div class="sidebar-section">
        <div class="sidebar-section-title">Principal</div>
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="sidebar-link <%= _path.contains("dashboard") ? "activo" : "" %>"><span class="icon"></span> Dashboard</a>
        <a href="${pageContext.request.contextPath}/menu" class="sidebar-link"><span class="icon"></span> Ver Menú</a>
    </div>
    <div class="sidebar-section">
        <div class="sidebar-section-title">Gestión</div>
        <a href="${pageContext.request.contextPath}/admin/menu" class="sidebar-link <%= _path.contains("/admin/menu") || _path.contains("/admin/platillos") ? "activo" : "" %>"><span class="icon"></span> Platillos y Menú</a>
        <a href="${pageContext.request.contextPath}/admin/usuarios" class="sidebar-link <%= _path.contains("usuarios") ? "activo" : "" %>"><span class="icon"></span> Usuarios</a>
        <a href="${pageContext.request.contextPath}/admin/becados" class="sidebar-link <%= _path.contains("becados") ? "activo" : "" %>"><span class="icon"></span> Becados Autorizados</a>
        <a href="${pageContext.request.contextPath}/admin/inventario" class="sidebar-link <%= _path.contains("inventario") ? "activo" : "" %>"><span class="icon"></span> Inventario</a>
        <a href="${pageContext.request.contextPath}/calificaciones" class="sidebar-link <%= _path.contains("calificaciones") ? "activo" : "" %>"><span class="icon"></span> Calificaciones</a>
    </div>
    <% } else if (_rol.equals("EMPLEADO")) { %>
    <div class="sidebar-section">
        <div class="sidebar-section-title">Cocina</div>
        <a href="${pageContext.request.contextPath}/empleado/dashboard" class="sidebar-link <%= _path.contains("/empleado/dashboard") ? "activo" : "" %>"><span class="icon"></span> Pedidos Activos</a>
    </div>
    <div class="sidebar-section">
        <div class="sidebar-section-title">Punto de Venta</div>
        <a href="${pageContext.request.contextPath}/pos" class="sidebar-link <%= _path.startsWith("/pos") ? "activo" : "" %>"><span class="icon"></span> POS — Vender</a>
    </div>
    <div class="sidebar-section">
        <a href="${pageContext.request.contextPath}/menu" class="sidebar-link"><span class="icon"></span> Ver Menú</a>
    </div>
    <% } else { %>
    <div class="sidebar-section">
        <div class="sidebar-section-title">Menú</div>
        <a href="${pageContext.request.contextPath}/menu" class="sidebar-link <%= _path.equals("/menu") ? "activo" : "" %>"><span class="icon"></span> Menú del Día</a>
        <a href="${pageContext.request.contextPath}/menu?tab=carta" class="sidebar-link"><span class="icon"></span> A la Carta</a>
    </div>
    <div class="sidebar-section">
        <div class="sidebar-section-title">Mis Pedidos</div>
        <a href="${pageContext.request.contextPath}/pedido/historial" class="sidebar-link <%= _path.contains("historial") ? "activo" : "" %>"><span class="icon"></span> Historial</a>
    </div>
    <% if (_rol.equals("BECADO")) { %>
    <div class="sidebar-section">
        <div class="sidebar-section-title">Mi Beca</div>
        <a href="${pageContext.request.contextPath}/becado/calendario" class="sidebar-link <%= _path.contains("calendario") ? "activo" : "" %>"><span class="icon">📅</span> Mi Calendario</a>
    </div>
    <% } %>
    <div class="sidebar-section">
        <div class="sidebar-section-title">Cuenta</div>
        <a href="${pageContext.request.contextPath}/perfil" class="sidebar-link <%= _path.contains("perfil") ? "activo" : "" %>"><span class="icon"></span> Mi Perfil</a>
    </div>
    <% } %>
</aside>
<div id="sidebar-overlay" onclick="toggleSidebar()"
     style="display:none;position:fixed;inset:0;background:rgba(0,0,0,.4);z-index:850;"></div>
