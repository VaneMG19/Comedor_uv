<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<body>
<h2>Login</h2>
<c:if test="${not empty error}">
  <p style="color:red">${error}</p>
</c:if>
<form method="post" action="${pageContext.request.contextPath}/login">
  <input type="email" name="email" placeholder="Email" required/><br/>
  <input type="password" name="password" placeholder="Contraseña" required/><br/>
  <button type="submit">Entrar</button>
</form>
</body>
</html>