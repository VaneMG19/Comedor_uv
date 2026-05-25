<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="mx.uv.comedor.model.*" %>
<%@ page import="mx.uv.comedor.dao.*" %>
<%@ page import="mx.uv.comedor.model.TarjetaUsuario" %>
<%@ page import="mx.uv.comedor.dao.TarjetaUsuarioDAO" %>

<%
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    if (usuario == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    String exito = request.getParameter("exito");
    String error = request.getParameter("error");

    AlumnoBecado becado = usuario.getRol() == RolEnum.BECADO
            ? (AlumnoBecado) session.getAttribute("becado") : null;
    Estudiante estudiante = (usuario.getRol() == RolEnum.ESTUDIANTE
            || usuario.getRol() == RolEnum.BECADO)
            ? (Estudiante) session.getAttribute("estudiante") : null;

    TarjetaUsuarioDAO _tarjetaDAO = new TarjetaUsuarioDAO();
    java.util.List<TarjetaUsuario> _tarjetas =
            _tarjetaDAO.listarPorUsuario(usuario.getIdUsuario());
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1.0">
    <title>Mi Perfil — Comedor UV</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
    <style>
        .perfil-avatar {
            width: 90px; height: 90px;
            border-radius: 50%;
            background: linear-gradient(135deg, var(--uv-azul), var(--uv-verde));
            display: flex; align-items: center; justify-content: center;
            font-family: var(--fuente-display);
            font-weight: 800; font-size: 2rem;
            color: white;
            box-shadow: var(--sombra-azul);
            flex-shrink: 0;
        }
        .perfil-tabs {
            display: flex;
            border-bottom: 2px solid var(--color-borde);
            margin-bottom: 28px;
            gap: 0;
            flex-wrap: wrap;
        }
        .perfil-tab {
            padding: 12px 20px;
            border: none;
            background: none;
            font-family: var(--fuente-display);
            font-weight: 600;
            font-size: .875rem;
            color: var(--uv-gris-500);
            cursor: pointer;
            border-bottom: 2px solid transparent;
            margin-bottom: -2px;
            transition: all var(--trans-rapida);
        }
        .perfil-tab:hover { color: var(--uv-azul); }
        .perfil-tab.activo {
            color: var(--uv-azul);
            border-bottom-color: var(--uv-azul);
        }
        .beca-progress {
            height: 10px;
            background: var(--uv-gris-200);
            border-radius: 5px;
            overflow: hidden;
            margin-top: 8px;
        }
        .beca-progress-fill {
            height: 100%;
            background: linear-gradient(90deg, var(--uv-verde), #84cc16);
            border-radius: 5px;
            transition: width .5s ease;
        }
    </style>
</head>
<body data-context-path="${pageContext.request.contextPath}">

<%@ include file="_header.jsp" %>

<main class="page-wrapper">

    <!-- Header con avatar -->
    <div style="display:flex;align-items:center;gap:20px;margin-bottom:28px;
                flex-wrap:wrap;">
        <div class="perfil-avatar">
            <%= usuario.getNombre().charAt(0) %>
            <%= usuario.getApellidos().charAt(0) %>
        </div>
        <div>
            <div class="page-title"><%= usuario.getNombreCompleto() %></div>
            <div style="display:flex;align-items:center;gap:10px;margin-top:6px;
                        flex-wrap:wrap;">
                <span style="font-size:.85rem;color:var(--uv-gris-500);">
                    <%= usuario.getEmail() %>
                </span>
                <span style="background:var(--uv-azul-light);color:var(--uv-azul);
                             font-size:.72rem;font-weight:700;padding:3px 10px;
                             border-radius:12px;text-transform:uppercase;">
                    <%= usuario.getRol().name() %>
                </span>
                <% if (usuario.isActivo()) { %>
                <span style="background:var(--uv-verde-light);color:var(--uv-verde-dark);
                             font-size:.72rem;font-weight:700;padding:3px 10px;
                             border-radius:12px;">
                    ● Cuenta activa
                </span>
                <% } %>
            </div>
        </div>
    </div>

    <% if (exito != null) { %>
    <div class="alert alert-exito" data-auto-close> <%= exito %></div>
    <% } %>
    <% if (error != null) { %>
    <div class="alert alert-error" data-auto-close> <%= error %></div>
    <% } %>

    <!-- Tabs -->
    <div class="perfil-tabs">
        <button class="perfil-tab activo"
                onclick="switchPTab('datos', this)"> Datos personales</button>
        <button class="perfil-tab"
                onclick="switchPTab('seguridad', this)"> Seguridad</button>
        <button class="perfil-tab"
                onclick="switchPTab('tarjetas', this)"> Mis Tarjetas</button>
        <% if (usuario.getRol() == RolEnum.BECADO && becado != null) { %>
        <button class="perfil-tab"
                onclick="switchPTab('beca', this)"> Mi beca</button>
        <% } %>
        <% if (estudiante != null || usuario.getRol() == RolEnum.ESTUDIANTE
                || usuario.getRol() == RolEnum.BECADO) { %>
        <button class="perfil-tab"
                onclick="switchPTab('academico', this)">🎓 Datos académicos</button>
        <% } %>
    </div>

    <!-- Tab: Datos personales -->
    <div id="ptab-datos">
        <div class="card" style="max-width:600px;">
            <div class="card-header">
                <div class="card-title">Información personal</div>
            </div>
            <div class="card-body">
                <form method="post"
                      action="${pageContext.request.contextPath}/perfil/actualizar">
                    <input type="hidden" name="accion" value="datos">

                    <div style="display:grid;grid-template-columns:1fr 1fr;gap:0 16px;">
                        <div class="form-group">
                            <label class="form-label" for="nombre">Nombre(s)</label>
                            <input type="text" id="nombre" name="nombre"
                                   class="form-control"
                                   value="<%= usuario.getNombre() %>"
                                   required minlength="2">
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="apellidos">Apellidos</label>
                            <input type="text" id="apellidos" name="apellidos"
                                   class="form-control"
                                   value="<%= usuario.getApellidos() %>"
                                   required minlength="2">
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="form-label">Correo electrónico</label>
                        <input type="email" class="form-control"
                               value="<%= usuario.getEmail() %>"
                               disabled
                               style="background:var(--uv-gris-100);
                                      color:var(--uv-gris-500);">
                        <div style="font-size:.75rem;color:var(--uv-gris-500);margin-top:4px;">
                            El correo no puede modificarse. Contacta a soporte si necesitas cambiarlo.
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="telefono">Teléfono</label>
                        <input type="tel" id="telefono" name="telefono"
                               class="form-control"
                               value="<%= usuario.getTelefono() != null ? usuario.getTelefono() : "" %>"
                               placeholder="10 dígitos"
                               maxlength="10">
                    </div>

                    <button type="submit" class="btn btn-primario">
                         Guardar cambios
                    </button>
                </form>
            </div>
        </div>
    </div>

    <!-- Tab: Seguridad -->
    <div id="ptab-seguridad" style="display:none;">
        <div class="card" style="max-width:600px;">
            <div class="card-header">
                <div class="card-title">Cambiar contraseña</div>
            </div>
            <div class="card-body">
                <form method="post"
                      action="${pageContext.request.contextPath}/perfil/actualizar"
                      onsubmit="return validarCambioPass()">
                    <input type="hidden" name="accion" value="password">

                    <div class="form-group">
                        <label class="form-label" for="passActual">Contraseña actual</label>
                        <input type="password" id="passActual" name="passwordActual"
                               class="form-control"
                               placeholder="Tu contraseña actual" required>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="passNueva">Nueva contraseña</label>
                        <input type="password" id="passNueva" name="passwordNueva"
                               class="form-control"
                               placeholder="Mínimo 8 caracteres"
                               required minlength="8"
                               oninput="checkPassNueva(this)">
                        <div id="msg-pass-nueva"
                             style="font-size:.75rem;margin-top:4px;display:none;"></div>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="passConfirmar">Confirmar nueva contraseña</label>
                        <input type="password" id="passConfirmar"
                               class="form-control"
                               placeholder="Repite la nueva contraseña" required>
                    </div>

                    <div class="alert alert-aviso" style="margin-bottom:16px;">
                         Después de cambiar tu contraseña, se cerrará tu sesión y
                        tendrás que volver a iniciarla.
                    </div>

                    <button type="submit" class="btn btn-primario">
                         Cambiar contraseña
                    </button>
                </form>
            </div>
        </div>
    </div>

    <!-- Tab: Tarjetas -->
    <div id="ptab-tarjetas" style="display:none;">

        <div class="alert alert-info" style="margin-bottom:20px;">
             <strong>Seguridad:</strong> Solo se guardan los últimos 4 dígitos
            de tu tarjeta. Nunca guardamos el número completo ni el CVV.
        </div>

        <!-- Lista de tarjetas guardadas -->
        <% if (_tarjetas.isEmpty()) { %>
        <div style="text-align:center;padding:40px;color:var(--uv-gris-500);">
            <div style="font-size:3rem;"></div>
            <div style="font-family:var(--fuente-display);font-weight:700;font-size:1rem;margin-top:10px;">
                No tienes tarjetas guardadas
            </div>
            <div style="font-size:.85rem;margin-top:6px;">
                Agrega una para hacer pagos más rápido.
            </div>
        </div>
        <% } else { %>
        <div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(280px,1fr));gap:16px;margin-bottom:24px;">
            <% for (TarjetaUsuario t : _tarjetas) { %>
            <div style="position:relative;padding:18px;border-radius:14px;
                        background:linear-gradient(135deg, var(--uv-azul) 0%, var(--uv-azul-dark) 100%);
                        color:white;box-shadow:var(--sombra-md);">
                <% if (t.isEsPredeterminada()) { %>
                <span style="position:absolute;top:10px;right:10px;background:var(--uv-amarillo);
                             color:#000;font-size:.65rem;font-weight:800;padding:3px 8px;
                             border-radius:10px;">PREDETERMINADA</span>
                <% } %>
                <div style="font-size:.7rem;opacity:.7;text-transform:uppercase;letter-spacing:1px;">
                    <%= t.getAlias() %>
                </div>
                <div style="font-size:1.2rem;font-family:'Courier New',monospace;margin-top:14px;letter-spacing:2px;">
                    <%= t.getNumeroEnmascarado() %>
                </div>
                <div style="display:flex;justify-content:space-between;margin-top:16px;font-size:.75rem;">
                    <div>
                        <div style="opacity:.6;text-transform:uppercase;font-size:.65rem;">Titular</div>
                        <div><%= t.getNombreTitular() %></div>
                    </div>
                    <div style="text-align:right;">
                        <div style="opacity:.6;text-transform:uppercase;font-size:.65rem;">Vence</div>
                        <div><%= t.getVencimientoFormateado() %></div>
                    </div>
                </div>
                <div style="margin-top:14px;display:flex;gap:8px;">
                    <% if (!t.isEsPredeterminada()) { %>
                    <form method="post" action="${pageContext.request.contextPath}/tarjeta/predeterminada" style="display:inline;">
                        <input type="hidden" name="idTarjeta" value="<%= t.getIdTarjeta() %>">
                        <button type="submit" style="background:rgba(255,255,255,.2);border:none;color:white;
                                                     padding:5px 10px;border-radius:6px;font-size:.75rem;
                                                     cursor:pointer;">
                            Marcar predeterminada
                        </button>
                    </form>
                    <% } %>
                    <form method="post" action="${pageContext.request.contextPath}/tarjeta/eliminar" style="display:inline;"
                          onsubmit="return confirm('¿Eliminar esta tarjeta?');">
                        <input type="hidden" name="idTarjeta" value="<%= t.getIdTarjeta() %>">
                        <button type="submit" style="background:rgba(255,80,80,.5);border:none;color:white;
                                                     padding:5px 10px;border-radius:6px;font-size:.75rem;
                                                     cursor:pointer;">
                            🗑️ Eliminar
                        </button>
                    </form>
                </div>
            </div>
            <% } %>
        </div>
        <% } %>

        <!-- Formulario para agregar nueva tarjeta -->
        <div class="card" style="max-width:600px;">
            <div class="card-header">
                <div class="card-title">+ Agregar nueva tarjeta</div>
            </div>
            <div class="card-body">
                <form method="post" action="${pageContext.request.contextPath}/tarjeta/agregar">
                    <div class="form-group">
                        <label class="form-label" for="alias">Alias / Apodo</label>
                        <input type="text" id="alias" name="alias" class="form-control"
                               placeholder="Mi tarjeta principal" required maxlength="50">
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="numeroTarjeta">Número de tarjeta</label>
                        <input type="text" id="numeroTarjeta" name="numeroTarjeta" class="form-control"
                               placeholder="0000 0000 0000 0000" required
                               inputmode="numeric" maxlength="23"
                               oninput="formatearNumero(this)">
                        <div style="font-size:.72rem;color:var(--uv-gris-500);margin-top:4px;">
                             Solo se guardarán los últimos 4 dígitos. Tu número completo nunca se almacena.
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="nombreTitular">Nombre del titular</label>
                        <input type="text" id="nombreTitular" name="nombreTitular" class="form-control"
                               placeholder="Como aparece en la tarjeta" required maxlength="120">
                    </div>

                    <div class="form-group">
                        <label class="form-label">Vencimiento</label>
                        <div style="display:flex;gap:10px;">
                            <select name="mesVencimiento" class="form-control" required>
                                <option value="">Mes</option>
                                <% for (int m = 1; m <= 12; m++) { %>
                                <option value="<%= m %>"><%= String.format("%02d", m) %></option>
                                <% } %>
                            </select>
                            <select name="anioVencimiento" class="form-control" required>
                                <option value="">Año</option>
                                <% for (int a = 2026; a <= 2040; a++) { %>
                                <option value="<%= a %>"><%= a %></option>
                                <% } %>
                            </select>
                        </div>
                    </div>

                    <div class="form-group">
                        <label style="display:flex;align-items:center;gap:8px;cursor:pointer;font-size:.875rem;">
                            <input type="checkbox" name="esPredeterminada">
                            Usar como tarjeta predeterminada
                        </label>
                    </div>

                    <button type="submit" class="btn btn-primario btn-block">
                         Guardar tarjeta
                    </button>
                </form>
            </div>
        </div>
    </div>

    <!-- Tab: Beca (solo becados) -->
    <% if (usuario.getRol() == RolEnum.BECADO && becado != null) { %>
    <div id="ptab-beca" style="display:none;">
        <div class="card" style="max-width:600px;">
            <div class="card-header">
                <div class="card-title"> Mi Beca Alimentaria</div>
            </div>
            <div class="card-body">

                <!-- Uso semanal -->
                <div style="margin-bottom:24px;">
                    <div style="display:flex;justify-content:space-between;margin-bottom:6px;">
                        <span style="font-weight:600;">Comidas usadas esta semana</span>
                        <span style="font-family:var(--fuente-display);font-weight:700;
                                     color:var(--uv-azul);">
                            <%= becado.getComidasUsadasSemana() %> /
                            <%= becado.getComidasDisponiblesSemana() %>
                        </span>
                    </div>
                    <div class="beca-progress">
                        <%
                            int pct = becado.getComidasDisponiblesSemana() > 0
                                    ? (int)(becado.getComidasUsadasSemana() * 100.0
                                    / becado.getComidasDisponiblesSemana())
                                    : 0;
                        %>
                        <div class="beca-progress-fill"
                             style="width:<%= pct %>%;
                                     background:<%= pct >= 100
                                        ? "var(--uv-rojo)"
                                        : pct >= 75
                                            ? "var(--uv-amarillo)"
                                            : "linear-gradient(90deg,var(--uv-verde),#84cc16)" %>;">
                        </div>
                    </div>
                    <div style="font-size:.8rem;color:var(--uv-gris-500);margin-top:6px;">
                        <% if (becado.getComidasRestantesSemana() <= 0) { %>
                         Has agotado tus comidas de beca esta semana.
                        El contador se reinicia cada lunes.
                        <% } else { %>
                        Te quedan <strong><%= becado.getComidasRestantesSemana() %></strong>
                        comidas de beca disponibles esta semana.
                        <% } %>
                    </div>
                </div>

                <!-- Datos de la beca -->
                <div style="display:grid;gap:12px;">
                    <div style="display:flex;justify-content:space-between;
                                padding:12px;background:var(--uv-gris-100);
                                border-radius:var(--radio);">
                        <span style="color:var(--uv-gris-700);">Tipo de beca</span>
                        <span style="font-weight:600;"><%= becado.getTipoBeca() %></span>
                    </div>
                    <div style="display:flex;justify-content:space-between;
                                padding:12px;background:var(--uv-gris-100);
                                border-radius:var(--radio);">
                        <span style="color:var(--uv-gris-700);">Vigencia</span>
                        <span style="font-weight:600;">
                            <%= becado.getVigenciaDesde() %> al
                            <%= becado.getVigenciaHasta() %>
                        </span>
                    </div>
                    <div style="display:flex;justify-content:space-between;
                                padding:12px;background:var(--uv-gris-100);
                                border-radius:var(--radio);">
                        <span style="color:var(--uv-gris-700);">Estado</span>
                        <span style="font-weight:600;
                                color:<%= becado.esBecaVigente()
                                        ? "var(--uv-verde)" : "var(--uv-rojo)" %>;">
                            <%= becado.esBecaVigente() ? "✅ Activa" : "❌ Vencida" %>
                        </span>
                    </div>
                </div>

                <div class="alert alert-info" style="margin-top:20px;">
                     La beca cubre el 100% del costo de los platillos del
                    <strong>menú del día</strong>.
                    Los platillos a la carta siempre tienen costo.
                </div>
            </div>
        </div>
    </div>
    <% } %>

    <!-- Tab: Datos académicos -->
    <% if (estudiante != null) { %>
    <div id="ptab-academico" style="display:none;">
        <div class="card" style="max-width:600px;">
            <div class="card-header">
                <div class="card-title"> Datos académicos</div>
            </div>
            <div class="card-body">
                <div style="display:grid;gap:12px;">
                    <div style="display:flex;justify-content:space-between;
                                padding:12px;background:var(--uv-gris-100);
                                border-radius:var(--radio);">
                        <span style="color:var(--uv-gris-700);">Matrícula</span>
                        <span style="font-weight:600;font-family:var(--fuente-display);">
                            <%= estudiante.getMatricula() %>
                        </span>
                    </div>
                    <div style="display:flex;justify-content:space-between;
                                padding:12px;background:var(--uv-gris-100);
                                border-radius:var(--radio);">
                        <span style="color:var(--uv-gris-700);">Carrera</span>
                        <span style="font-weight:600;">
                            <%= estudiante.getCarrera() %>
                        </span>
                    </div>
                    <div style="display:flex;justify-content:space-between;
                                padding:12px;background:var(--uv-gris-100);
                                border-radius:var(--radio);">
                        <span style="color:var(--uv-gris-700);">Semestre</span>
                        <span style="font-weight:600;">
                            <%= estudiante.getSemestre() %>°
                        </span>
                    </div>
                </div>
                <div class="alert alert-info" style="margin-top:20px;">
                     Para actualizar tus datos académicos contacta
                    a la administración del comedor.
                </div>
            </div>
        </div>
    </div>
    <% } %>

</main>

<%@ include file="_footer.jsp" %>

<script>
    function switchPTab(tab, btn) {
        document.querySelectorAll('[id^="ptab-"]')
            .forEach(el => el.style.display = 'none');
        document.querySelectorAll('.perfil-tab')
            .forEach(b => b.classList.remove('activo'));
        const panel = document.getElementById('ptab-' + tab);
        if (panel) panel.style.display = 'block';
        btn.classList.add('activo');
    }

    function checkPassNueva(input) {
        const msg = document.getElementById('msg-pass-nueva');
        msg.style.display = 'block';
        if (input.value.length < 8) {
            msg.style.color = 'var(--uv-rojo)';
            msg.textContent = ' Mínimo 8 caracteres';
        } else if (!/[A-Z]/.test(input.value)) {
            msg.style.color = 'var(--uv-amarillo)';
            msg.textContent = 'Agrega al menos una mayúscula';
        } else {
            msg.style.color = 'var(--uv-verde)';
            msg.textContent = 'Contraseña segura';
        }
    }

    function validarCambioPass() {
        const nueva    = document.getElementById('passNueva').value;
        const confirmar = document.getElementById('passConfirmar').value;
        if (nueva !== confirmar) {
            alert('Las contraseñas no coinciden');
            return false;
        }
        if (nueva.length < 8) {
            alert('La nueva contraseña debe tener al menos 8 caracteres');
            return false;
        }
        return true;
    }

    function formatearNumero(input) {
        let v = input.value.replace(/\D/g, '');
        let formatted = '';
        for (let i = 0; i < v.length; i += 4) {
            if (i > 0) formatted += ' ';
            formatted += v.substr(i, 4);
        }
        input.value = formatted;
    }

    // Activar la tab según el parámetro ?tab=X de la URL
    (function() {
        const params = new URLSearchParams(window.location.search);
        const tab = params.get('tab');
        if (tab) {
            const btn = document.querySelector('.perfil-tab[onclick*="' + tab + '"]');
            if (btn) switchPTab(tab, btn);
        }
    })();
</script>
</body>
</html>
v