<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="mx.uv.comedor.model.Usuario" %>
<%
  Usuario _u = (Usuario) session.getAttribute("usuario");
  if (_u != null) {
    response.sendRedirect(request.getContextPath() + "/menu");
    return;
  }
  String error = (String) request.getAttribute("error");
  String msg   = request.getParameter("msg");
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Iniciar Sesión — Comedor UV</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
  <style>
    body { background: var(--uv-azul); min-height: 100vh;
      display: flex; align-items: center; justify-content: center;
      padding: 20px; }
    .login-wrapper {
      display: flex; width: 100%; max-width: 900px; min-height: 560px;
      border-radius: var(--radio-xl); overflow: hidden;
      box-shadow: 0 25px 60px rgba(0,0,0,.35);
    }
    .login-branding {
      flex: 1;
      background: linear-gradient(145deg, var(--uv-azul-dark) 0%, #1a6abf 100%);
      padding: 48px 40px; display: flex; flex-direction: column;
      justify-content: space-between; position: relative; overflow: hidden;
    }
    .login-branding::before, .login-branding::after {
      content: ''; position: absolute; border-radius: 50%;
      background: rgba(255,255,255,.05);
    }
    .login-branding::before { width: 300px; height: 300px; top: -80px; right: -80px; }
    .login-branding::after  { width: 200px; height: 200px; bottom: -50px; left: -50px;
      background: rgba(255,255,255,.04); }
    .brand-logo { display: flex; align-items: center; gap: 14px; position: relative; z-index: 1; }
    .brand-logo-icon {
      width: 52px; height: 52px; background: white; border-radius: 12px;
      display: flex; align-items: center; justify-content: center;
      font-family: var(--fuente-display); font-weight: 800;
      font-size: 1.3rem; color: var(--uv-azul);
    }
    .brand-logo-text { color: white; font-family: var(--fuente-display); }
    .brand-logo-text h1 { font-size: 1.1rem; font-weight: 700; }
    .brand-logo-text p  { font-size: .75rem; opacity: .75; margin-top: 2px; }
    .brand-content { position: relative; z-index: 1; }
    .brand-content h2 {
      color: white; font-family: var(--fuente-display);
      font-size: 1.8rem; font-weight: 800; line-height: 1.25; margin-bottom: 14px;
    }
    .brand-content p { color: rgba(255,255,255,.75); font-size: .9rem; line-height: 1.6; }
    .brand-features { position: relative; z-index: 1; display: flex; flex-direction: column; gap: 10px; }
    .brand-feature {
      display: flex; align-items: center; gap: 10px;
      color: rgba(255,255,255,.9); font-size: .85rem;
    }
    .brand-feature .feat-icon {
      width: 32px; height: 32px; background: rgba(255,255,255,.15);
      border-radius: 8px; display: flex; align-items: center;
      justify-content: center; font-size: 1rem; flex-shrink: 0;
    }
    .login-form-panel {
      width: 380px; background: white; padding: 48px 40px;
      display: flex; flex-direction: column; justify-content: center;
    }
    .login-title { font-family: var(--fuente-display); font-size: 1.6rem;
      font-weight: 800; color: var(--uv-gris-900); margin-bottom: 6px; }
    .login-subtitle { color: var(--uv-gris-500); font-size: .875rem; margin-bottom: 32px; }
    .login-form .form-group { margin-bottom: 20px; }
    .input-icon-wrapper { position: relative; }
    .input-icon-wrapper .form-control { padding-left: 42px; }
    .input-icon {
      position: absolute; left: 14px; top: 50%;
      transform: translateY(-50%); font-size: 1rem; pointer-events: none;
    }
    .btn-login {
      width: 100%; padding: 13px; font-size: 1rem;
      border-radius: var(--radio-lg); margin-top: 8px;
    }
    .login-footer { margin-top: 28px; text-align: center;
      font-size: .8rem; color: var(--uv-gris-500); }
    @media (max-width: 700px) {
      .login-branding { display: none; }
      .login-form-panel { width: 100%; padding: 36px 28px; }
      body { background: white; padding: 0; }
      .login-wrapper { border-radius: 0; box-shadow: none; min-height: 100vh; }
    }
  </style>
</head>
<body>

<div class="login-wrapper">
  <div class="login-branding">
    <div class="brand-logo">
      <div class="brand-logo-icon">UV</div>
      <div class="brand-logo-text">
        <h1>Comedor Universitario</h1>
        <p>Universidad Veracruzana</p>
      </div>
    </div>
    <div class="brand-content">
      <h2>Come bien,<br>rinde mejor.</h2>
      <p>Ordena tu comida del día, programa tu recogida y disfruta
        de platillos frescos preparados para ti.</p>
    </div>
  </div>

  <div class="login-form-panel">
    <div class="login-title">Bienvenido </div>
    <div class="login-subtitle">Inicia sesión con tu cuenta universitaria</div>

    <% if (error != null && !error.isEmpty()) { %>
    <div class="alert alert-error" data-auto-close> <%= error %></div>
    <% } %>
    <% if (msg != null && !msg.isEmpty()) { %>
    <div class="alert alert-exito" data-auto-close> <%= msg %></div>
    <% } %>

    <form class="login-form" method="post"
          action="${pageContext.request.contextPath}/login"
          onsubmit="return validarLogin(this)">

      <div class="form-group">
        <label class="form-label" for="email">Correo institucional</label>
        <div class="input-icon-wrapper">
          <span class="input-icon"></span>
          <input type="email" id="email" name="email"
                 class="form-control"
                 placeholder="usuario@uv.mx"
                 required autocomplete="email" autofocus>
        </div>
      </div>

      <div class="form-group">
        <label class="form-label" for="password">Contraseña</label>
        <div class="input-icon-wrapper">
          <span class="input-icon"></span>
          <input type="password" id="password" name="password"
                 class="form-control"
                 placeholder="Tu contraseña"
                 required autocomplete="current-password">
          <button type="button" onclick="togglePassword()"
                  style="position:absolute;right:12px;top:50%;
                                   transform:translateY(-50%);border:none;
                                   background:none;cursor:pointer;font-size:1rem;"></button>
        </div>
      </div>

      <button type="submit" class="btn btn-primario btn-login">
        Iniciar Sesión →
      </button>
    </form>

    <div class="login-footer">
      ¿No tienes cuenta?
      <a href="${pageContext.request.contextPath}/registro"
         style="color:var(--uv-azul);font-weight:600;">Regístrate aquí</a>
    </div>
  </div>
</div>

<script>
  function togglePassword() {
    const i = document.getElementById('password');
    i.type = i.type === 'password' ? 'text' : 'password';
  }
  function validarLogin(form) {
    const btn = form.querySelector('button[type="submit"]');
    btn.disabled = true;
    btn.textContent = 'Iniciando sesión...';
    return true;
  }
</script>
</body>
</html>
