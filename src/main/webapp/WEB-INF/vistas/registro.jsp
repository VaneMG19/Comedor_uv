<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="mx.uv.comedor.model.Usuario" %>
<%
    Usuario _u = (Usuario) session.getAttribute("usuario");
    if (_u!= null) { response.sendRedirect(request.getContextPath() + "/menu"); return; }
    String error = (String) request.getAttribute("error");
    String exito = (String) request.getAttribute("exito");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Registro - Comedor UV</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
    <style>
        body { background: var(--uv-azul); min-height: 100vh; display: flex;
            align-items: center; justify-content: center; padding: 20px; }
        .registro-wrapper {
            display: flex; width: 100%; max-width: 960px;
            border-radius: var(--radio-xl); overflow: hidden;
            box-shadow: 0 25px 60px rgba(0,0,0,.35);
        }
        .registro-branding {
            width: 300px; flex-shrink: 0;
            background: linear-gradient(145deg, var(--uv-azul-dark) 0%, #1a6abf 100%);
            padding: 40px 32px; display: flex; flex-direction: column;
            gap: 28px; position: relative; overflow: hidden;
        }
        .registro-branding::before {
            content: ''; position: absolute; width: 250px; height: 250px;
            background: rgba(255,255,255,.05); border-radius: 50%;
            top: -60px; right: -80px;
        }
        .brand-logo { display: flex; align-items: center; gap: 12px; position: relative; z-index: 1; }
        .brand-logo-icon {
            width: 44px; height: 44px; background: white; border-radius: 10px;
            display: flex; align-items: center; justify-content: center;
            font-family: var(--fuente-display); font-weight: 800;
            font-size: 1.1rem; color: var(--uv-azul);
        }
        .brand-logo-text { color: white; font-family: var(--fuente-display); }
        .brand-logo-text h1 { font-size: 1rem; font-weight: 700; }
        .brand-logo-text p  { font-size:.72rem; opacity:.75; }
        .brand-info { position: relative; z-index: 1; color: white; }
        .brand-info h2 {
            font-family: var(--fuente-display); font-size: 1.3rem;
            font-weight: 800; margin-bottom: 10px; line-height: 1.25;
        }
        .brand-info p { font-size:.82rem; opacity:.8; line-height: 1.6; }
        .brand-roles { position: relative; z-index: 1; display: flex; flex-direction: column; gap: 8px; }
        .brand-role {
            display: flex; align-items: center; gap: 10px;
            background: rgba(255,255,255,.1); border-radius: 10px;
            padding: 10px 12px; color: white; font-size:.78rem;
        }
        .brand-role.icon { font-size: 1.2rem; flex-shrink: 0; }
        .registro-form-panel { flex: 1; background: white; padding: 36px 40px;
            overflow-y: auto; max-height: 92vh; }
        .registro-title {
            font-family: var(--fuente-display); font-size: 1.5rem;
            font-weight: 800; color: var(--uv-gris-900); margin-bottom: 4px;
        }
        .registro-subtitle { color: var(--uv-gris-500); font-size:.875rem; margin-bottom: 24px; }
        .form-grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 0 16px; }
        .input-icon-wrapper { position: relative; }
        .input-icon-wrapper.form-control { padding-left: 42px; }
        .input-icon {
            position: absolute; left: 14px; top: 50%;
            transform: translateY(-50%); font-size: 1rem; pointer-events: none;
        }
        .form-control.valido  { border-color: var(--uv-verde); }
        .form-control.invalido { border-color: var(--uv-rojo);  }
        .campo-mensaje { font-size:.75rem; margin-top: 5px; display: none; }
        .campo-mensaje.visible { display: block; }
        .campo-mensaje.ok  { color: var(--uv-verde); }
        .campo-mensaje.error { color: var(--uv-rojo);  }
        .roles-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 8px;
            margin-top: 6px;
        }
        .rol-opcion { position: relative; }
        .rol-opcion input[type="radio"] { position: absolute; opacity: 0; width: 0; height: 0; }
        .rol-opcion label {
            display: flex; flex-direction: column; align-items: center; gap: 4px;
            padding: 12px 8px; border: 2px solid var(--color-borde);
            border-radius: var(--radio); cursor: pointer; text-align: center;
            transition: all var(--trans-rapida); font-size:.75rem;
            font-weight: 600; color: var(--uv-gris-700);
        }
        .rol-opcion label.rol-icon { font-size: 1.4rem; }
        .rol-opcion input:checked + label {
            border-color: var(--uv-azul); background: var(--uv-azul-light); color: var(--uv-azul);
        }
        .rol-opcion input:checked + label.becado-label {
            border-color: var(--uv-verde); background: var(--uv-verde-light); color: var(--uv-verde-dark);
        }
        .campos-rol { display: none; margin-top: 12px; padding: 14px;
            background: var(--uv-gris-100); border-radius: var(--radio); }
        .campos-rol.visible { display: block; }
        .info-becado {
            background: var(--uv-verde-light);
            border-left: 4px solid var(--uv-verde);
            padding: 12px 14px;
            border-radius: 8px;
            margin-bottom: 14px;
            font-size:.82rem;
            color: var(--uv-verde-dark);
        }
        .password-strength { margin-top: 6px; display: none; }
        .password-strength.visible { display: block; }
        .strength-bar { height: 4px; background: var(--uv-gris-300);
            border-radius: 2px; overflow: hidden; margin-bottom: 4px; }
        .strength-fill { height: 100%; border-radius: 2px;
            transition: width.3s, background.3s; }
        .strength-text { font-size:.72rem; }
        .login-link { text-align: center; margin-top: 20px;
            font-size:.85rem; color: var(--uv-gris-500); }
        .login-link a { color: var(--uv-azul); font-weight: 600; }
        @media (max-width: 700px) {
            .registro-branding { display: none; }
            .registro-form-panel { padding: 24px 18px; }
            body { background: white; padding: 0; }
            .registro-wrapper { border-radius: 0; box-shadow: none; max-height: none; }
            .form-grid-2 { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body>

<div class="registro-wrapper">
    <div class="registro-branding">
        <div class="brand-logo">
            <div class="brand-logo-icon">UV</div>
            <div class="brand-logo-text">
                <h1>Comedor Universitario</h1>
                <p>Universidad Veracruzana</p>
            </div>
        </div>
        <div class="brand-info">
            <h2>Crea tu cuenta universitaria</h2>
            <p>Accede al comedor con tu correo institucional de la UV.</p>
        </div>
        <div class="brand-roles">
            <div class="brand-role"><span class="icon"></span><span>Estudiantes - pedidos</span></div>
            <div class="brand-role"><span class="icon"></span><span>Becados - calendario de comidas</span></div>
            <div class="brand-role"><span class="icon"></span><span>Docentes - pedidos y pago</span></div>
        </div>
    </div>

    <div class="registro-form-panel">
        <div class="registro-title">Crear cuenta </div>
        <div class="registro-subtitle">
            Solo se aceptan correos <strong>@uv.mx</strong>o <strong>@estudiantes.uv.mx</strong>
        </div>

        <% if (error!= null) { %>
        <div class="alert alert-error" data-auto-close> <%= error %></div>
        <% } %>
        <% if (exito!= null) { %>
        <div class="alert alert-exito"> <%= exito %></div>
        <% } %>

        <form id="form-registro" method="post"
              action="${pageContext.request.contextPath}/registro"
              onsubmit="return validarFormulario()">

            <div class="form-grid-2">
                <div class="form-group">
                    <label class="form-label" for="nombre">Nombre(s)</label>
                    <div class="input-icon-wrapper">
                        <span class="input-icon"></span>
                        <input type="text" id="nombre" name="nombre" class="form-control"
                               placeholder="Tu nombre" required minlength="2">
                    </div>
                </div>
                <div class="form-group">
                    <label class="form-label" for="apellidos">Apellidos</label>
                    <div class="input-icon-wrapper">
                        <span class="input-icon"></span>
                        <input type="text" id="apellidos" name="apellidos" class="form-control"
                               placeholder="Tus apellidos" required minlength="2">
                    </div>
                </div>
            </div>

            <div class="form-group">
                <label class="form-label" for="email">Correo institucional UV</label>
                <div class="input-icon-wrapper">
                    <span class="input-icon"></span>
                    <input type="email" id="email" name="email" class="form-control"
                           placeholder="matricula@estudiantes.uv.mx"
                           required oninput="validarEmail(this)">
                </div>
                <div class="campo-mensaje" id="msg-email"></div>
            </div>

            <div class="form-group">
                <label class="form-label" for="telefono">Teléfono (opcional)</label>
                <div class="input-icon-wrapper">
                    <span class="input-icon"></span>
                    <input type="tel" id="telefono" name="telefono" class="form-control"
                           placeholder="10 dígitos" maxlength="10"
                           oninput="this.value=this.value.replace(/\D/g,'')">
                </div>
            </div>

            <div class="form-grid-2">
                <div class="form-group">
                    <label class="form-label" for="password">Contraseña</label>
                    <div class="input-icon-wrapper">
                        <span class="input-icon"></span>
                        <input type="password" id="password" name="password" class="form-control"
                               placeholder="Mínimo 8 caracteres" required minlength="8"
                               oninput="validarPassword(this)">
                        <button type="button" onclick="togglePass('password')"
                                style="position:absolute;right:10px;top:50%;
 transform:translateY(-50%);border:none;
 background:none;cursor:pointer;"></button>
                    </div>
                    <div class="password-strength" id="strength-bar">
                        <div class="strength-bar"><div class="strength-fill" id="strength-fill"></div></div>
                        <div class="strength-text" id="strength-text"></div>
                    </div>
                </div>
                <div class="form-group">
                    <label class="form-label" for="confirmarPassword">Confirmar contraseña</label>
                    <div class="input-icon-wrapper">
                        <span class="input-icon"></span>
                        <input type="password" id="confirmarPassword" class="form-control"
                               placeholder="Repite tu contraseña" required
                               oninput="validarConfirmacion(this)">
                        <button type="button" onclick="togglePass('confirmarPassword')"
                                style="position:absolute;right:10px;top:50%;
 transform:translateY(-50%);border:none;
 background:none;cursor:pointer;"></button>
                    </div>
                    <div class="campo-mensaje" id="msg-confirmar"></div>
                </div>
            </div>

            <!-- 3 opciones de rol -->
            <div class="form-group">
                <label class="form-label">¿Quién eres?</label>
                <div class="roles-grid">
                    <div class="rol-opcion">
                        <input type="radio" id="rol-estudiante" name="rol" value="ESTUDIANTE" checked
                               onchange="mostrarCamposRol('ESTUDIANTE')">
                        <label for="rol-estudiante">
                            <span class="rol-icon"></span>Estudiante
                        </label>
                    </div>
                    <div class="rol-opcion">
                        <input type="radio" id="rol-becado" name="rol" value="BECADO"
                               onchange="mostrarCamposRol('BECADO')">
                        <label for="rol-becado" class="becado-label">
                            <span class="rol-icon"></span>Becado
                        </label>
                    </div>
                    <div class="rol-opcion">
                        <input type="radio" id="rol-docente" name="rol" value="DOCENTE"
                               onchange="mostrarCamposRol('DOCENTE')">
                        <label for="rol-docente">
                            <span class="rol-icon"></span>Docente
                        </label>
                    </div>
                </div>
            </div>

            <!-- Campos ESTUDIANTE -->
            <div class="campos-rol visible" id="campos-ESTUDIANTE">
                <div class="form-grid-2">
                    <div class="form-group" style="margin-bottom:0;">
                        <label class="form-label" for="matricula">Matrícula</label>
                        <div class="input-icon-wrapper">
                            <span class="input-icon"></span>
                            <input type="text" id="matricula" name="matricula" class="form-control"
                                   placeholder="zS12345678">
                        </div>
                    </div>
                    <div class="form-group" style="margin-bottom:0;">
                        <label class="form-label" for="semestre">Semestre</label>
                        <select id="semestre" name="semestre" class="form-control">
                            <% for (int i = 1; i <= 12; i++) { %>
                            <option value="<%= i %>"><%= i %>° semestre</option>
                            <% } %>
                        </select>
                    </div>
                </div>
                <div class="form-group" style="margin-top:14px;margin-bottom:0;">
                    <label class="form-label" for="carrera">Carrera</label>
                    <div class="input-icon-wrapper">
                        <span class="input-icon"></span>
                        <input type="text" id="carrera" name="carrera" class="form-control"
                               placeholder="Ingeniería en Sistemas Computacionales">
                    </div>
                </div>
            </div>

            <!-- Campos BECADO -->
            <div class="campos-rol" id="campos-BECADO">
                <div class="info-becado">
                    <strong>Beca alimentaria</strong> - Tu correo debe estar en la lista
                    de becados autorizados por la universidad. Si tu beca está aprobada
                    pero tu correo no aparece, contacta a la administración del comedor.
                </div>
                <div class="form-group" style="margin-bottom:0;">
                    <label class="form-label" for="carreraBecado">Carrera</label>
                    <div class="input-icon-wrapper">
                        <span class="input-icon"></span>
                        <input type="text" id="carreraBecado" name="carrera" class="form-control"
                               placeholder="Ingeniería en Sistemas Computacionales">
                    </div>
                </div>
                <div class="form-group" style="margin-top:14px;margin-bottom:0;">
                    <label class="form-label" for="semestreBecado">Semestre</label>
                    <select id="semestreBecado" name="semestre" class="form-control">
                        <% for (int i = 1; i <= 12; i++) { %>
                        <option value="<%= i %>"><%= i %>° semestre</option>
                        <% } %>
                    </select>
                </div>
            </div>

            <!-- Campos DOCENTE -->
            <div class="campos-rol" id="campos-DOCENTE">
                <div class="form-grid-2">
                    <div class="form-group" style="margin-bottom:0;">
                        <label class="form-label" for="numEmpleadoDocente">Número empleado</label>
                        <div class="input-icon-wrapper">
                            <span class="input-icon"></span>
                            <input type="text" id="numEmpleadoDocente" name="numEmpleadoDocente"
                                   class="form-control" placeholder="DOC-0042">
                        </div>
                    </div>
                    <div class="form-group" style="margin-bottom:0;">
                        <label class="form-label" for="facultad">Facultad</label>
                        <div class="input-icon-wrapper">
                            <span class="input-icon"></span>
                            <input type="text" id="facultad" name="facultad" class="form-control"
                                   placeholder="Ingeniería">
                        </div>
                    </div>
                </div>
                <div class="form-grid-2" style="margin-top:14px;">
                    <div class="form-group" style="margin-bottom:0;">
                        <label class="form-label" for="departamento">Departamento</label>
                        <input type="text" id="departamento" name="departamento" class="form-control"
                               placeholder="Sistemas">
                    </div>
                    <div class="form-group" style="margin-bottom:0;">
                        <label class="form-label" for="categoria">Categoría</label>
                        <select id="categoria" name="categoria" class="form-control">
                            <option value="Profesor de Tiempo Completo">Tiempo Completo</option>
                            <option value="Profesor de Medio Tiempo">Medio Tiempo</option>
                            <option value="Profesor por Horas">Por Horas</option>
                            <option value="Técnico Académico">Técnico Académico</option>
                            <option value="Investigador">Investigador</option>
                        </select>
                    </div>
                </div>
            </div>

            <div class="form-group" style="margin-top:16px;">
                <label style="display:flex;align-items:flex-start;gap:10px;cursor:pointer;
 font-size:.85rem;color:var(--uv-gris-700);">
                    <input type="checkbox" id="terminos" required style="margin-top:3px;flex-shrink:0;">
                    Acepto que mis datos sean utilizados para la gestión de pedidos del Comedor UV.
                </label>
            </div>

            <button type="submit" id="btn-registro"
                    class="btn btn-primario btn-block btn-lg" style="margin-top:8px;">
                Crear mi cuenta
            </button>
        </form>

        <div class="login-link">
            ¿Ya tienes cuenta?
            <a href="${pageContext.request.contextPath}/login">Inicia sesión aquí</a>
        </div>
    </div>
</div>

<script>
    const DOMINIOS_UV = ['@uv.mx', '@estudiantes.uv.mx', '@alumni.uv.mx'];

    function validarEmail(input) {
        const val = input.value.trim().toLowerCase();
        const msg = document.getElementById('msg-email');
        const esUV = DOMINIOS_UV.some(d =>val.endsWith(d));
        const esEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val);
        msg.classList.add('visible');
        if (!val) setEstado(input, msg, null, '');
        else if (!esEmail) setEstado(input, msg, false, 'Escribe un correo válido');
        else if (!esUV) setEstado(input, msg, false, ' Solo se aceptan correos @uv.mx o @estudiantes.uv.mx');
        else setEstado(input, msg, true, ' Correo UV válido');
    }

    function validarPassword(input) {
        const val = input.value;
        const bar = document.getElementById('strength-bar');
        const fill = document.getElementById('strength-fill');
        const txt  = document.getElementById('strength-text');
        bar.classList.add('visible');
        let puntos = 0;
        if (val.length >= 8) puntos++;
        if (val.length >= 12) puntos++;
        if (/[A-Z]/.test(val)) puntos++;
        if (/[0-9]/.test(val)) puntos++;
        if (/[^A-Za-z0-9]/.test(val)) puntos++;
        const niveles = [
            { w:'20%', bg:'var(--uv-rojo)', t:'Muy débil' },
            { w:'40%', bg:'#f97316', t:'Débil' },
            { w:'60%', bg:'var(--uv-amarillo)',t:'Regular' },
            { w:'80%', bg:'#84cc16', t:'Buena' },
            { w:'100%',bg:'var(--uv-verde)', t:'Excelente' }
        ];
        const nivel = niveles[Math.min(puntos, 4)];
        fill.style.width = nivel.w;
        fill.style.background = nivel.bg;
        txt.textContent = nivel.t;
        txt.style.color = nivel.bg;
        const conf = document.getElementById('confirmarPassword');
        if (conf.value) validarConfirmacion(conf);
    }

    function validarConfirmacion(input) {
        const pass = document.getElementById('password').value;
        const msg  = document.getElementById('msg-confirmar');
        msg.classList.add('visible');
        if (input.value === pass && pass.length > 0)
            setEstado(input, msg, true, ' Las contraseñas coinciden');
        else
            setEstado(input, msg, false, 'Las contraseñas no coinciden');
    }

    function setEstado(input, msgEl, ok, texto) {
        input.classList.remove('valido', 'invalido');
        msgEl.classList.remove('ok', 'error');
        if (ok === true)  { input.classList.add('valido'); msgEl.classList.add('ok'); }
        if (ok === false) { input.classList.add('invalido'); msgEl.classList.add('error'); }
        msgEl.textContent = texto;
    }

    function togglePass(id) {
        const input = document.getElementById(id);
        input.type = input.type === 'password'? 'text' : 'password';
    }

    function mostrarCamposRol(rol) {
        document.querySelectorAll('.campos-rol').forEach(el =>el.classList.remove('visible'));
        document.getElementById('campos-' + rol)?.classList.add('visible');

        // Habilitar/deshabilitar campos según rol activo
        const allFields = document.querySelectorAll('.campos-rol input,.campos-rol select');
        allFields.forEach(f =>f.disabled = true);
        document.querySelectorAll('#campos-' + rol + ' input, #campos-' + rol + ' select')
            .forEach(f =>f.disabled = false);
    }

    // Inicializar
    mostrarCamposRol('ESTUDIANTE');

    function validarFormulario() {
        const email = document.getElementById('email').value.trim().toLowerCase();
        if (!DOMINIOS_UV.some(d =>email.endsWith(d))) {
            alert('Solo se aceptan correos @uv.mx o @estudiantes.uv.mx');
            return false;
        }
        const pass = document.getElementById('password').value;
        const conf = document.getElementById('confirmarPassword').value;
        if (pass!== conf) { alert('Las contraseñas no coinciden'); return false; }
        if (pass.length < 8) { alert('Mínimo 8 caracteres'); return false; }
        const btn = document.getElementById('btn-registro');
        btn.disabled = true;
        btn.textContent = 'Creando cuenta...';
        return true;
    }
</script>
</body>
</html>
