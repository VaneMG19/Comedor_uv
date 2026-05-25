<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="mx.uv.comedor.model.Usuario" %>
<%
    String errorMsg = (String) request.getAttribute("error");
    if (errorMsg == null) errorMsg = "Ha ocurrido un error inesperado.";
    Usuario usuario = (Usuario) session.getAttribute("usuario");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1.0">
    <title>Error — Comedor UV</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body data-context-path="${pageContext.request.contextPath}">

<% if (usuario != null) { %>
<%@ include file="_header.jsp" %>
<main class="page-wrapper">
<% } else { %>
<main style="min-height:100vh;display:flex;align-items:center;
             justify-content:center;background:var(--uv-gris-100);padding:20px;">
<% } %>

    <div style="text-align:center;max-width:500px;margin:0 auto;">
        <div style="font-size:5rem;margin-bottom:16px;"></div>
        <div style="font-family:var(--fuente-display);font-weight:800;
                    font-size:1.5rem;color:var(--uv-gris-900);margin-bottom:10px;">
            Algo salió mal
        </div>
        <div style="color:var(--uv-gris-500);font-size:.95rem;
                    margin-bottom:28px;line-height:1.6;">
            <%= errorMsg %>
        </div>
        <div style="display:flex;gap:12px;justify-content:center;flex-wrap:wrap;">
            <button class="btn btn-ghost" onclick="history.back()">
                ← Regresar
            </button>
            <a href="${pageContext.request.contextPath}/menu"
               class="btn btn-primario"> Ir al menú</a>
        </div>
    </div>

</main>
</body>
</html>
