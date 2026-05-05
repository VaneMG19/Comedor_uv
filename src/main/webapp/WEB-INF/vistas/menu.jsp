<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<body>
<h2>Bienvenido, ${usuario.nombreCompleto}</h2>
<p>Rol: ${usuario.rol}</p>
<a href="${pageContext.request.contextPath}/logout">Cerrar sesión</a>
</body>
</html>