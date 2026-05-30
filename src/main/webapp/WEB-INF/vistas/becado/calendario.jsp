<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="mx.uv.comedor.model.*" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.util.*" %>
<%
 Usuario usuario = (Usuario) session.getAttribute("usuario");
 if (usuario == null || usuario.getRol()!= RolEnum.BECADO) {
 response.sendRedirect(request.getContextPath() + "/login");
 return;
  }

 AlumnoBecado becado  = (AlumnoBecado) request.getAttribute("becado");
 List<ApartadoBecado>aps = (List<ApartadoBecado>) request.getAttribute("apartados");
 LocalDate lunes  = (LocalDate) request.getAttribute("lunes");
 LocalDate viernes  = (LocalDate) request.getAttribute("viernes");
 Integer offset  = (Integer) request.getAttribute("offset");
 Integer totalApartados  = (Integer) request.getAttribute("totalApartados");
 LocalDate hoy  = (LocalDate) request.getAttribute("hoy");
 if (offset == null) offset = 0;
 if (totalApartados == null) totalApartados = 0;
 if (aps == null) aps = new ArrayList<>();

 String exito = request.getParameter("exito");
 String error = request.getParameter("error");

 String[] nombreDias = {"Lunes","Martes","Miércoles","Jueves","Viernes"};
 DateTimeFormatter fmtCorto = DateTimeFormatter.ofPattern("d MMM",
 new java.util.Locale("es","MX"));
 DateTimeFormatter fmtCompleto = DateTimeFormatter.ofPattern("d 'de' MMMM",
 new java.util.Locale("es","MX"));

  // Mapa rápido: "fecha-tipo" apartado
 Map<String, ApartadoBecado>mapaApartados = new HashMap<>();
 for (ApartadoBecado a : aps) {
 if (!"CANCELADO".equals(a.getEstado())) {
 mapaApartados.put(a.getFechaConsumo() + "-" + a.getTipoComida(), a);
  }
  }

 int disponibles = becado.getComidasDisponiblesSemana() - totalApartados;
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width,initial-scale=1.0">
  <title>Mi Calendario - Comedor UV</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
  <style>
.cal-grid {
 display: grid;
 grid-template-columns: repeat(5, 1fr);
 gap: 12px;
 margin-top: 20px;
  }
.dia-col {
 background: white;
 border-radius: var(--radio-lg);
 border: 1px solid var(--color-borde);
 overflow: hidden;
 box-shadow: var(--sombra-sm);
  }
.dia-col.es-hoy { border: 2px solid var(--uv-amarillo); }
.dia-col.es-pasado { opacity:.55; }
.dia-header {
 padding: 12px 14px;
 background: var(--uv-azul);
 color: white;
 text-align: center;
  }
.es-hoy.dia-header { background: var(--uv-amarillo); color: #000; }
.es-pasado.dia-header { background: var(--uv-gris-500); }
.dia-nombre {
 font-family: var(--fuente-display);
 font-weight: 700;
 font-size:.85rem;
 text-transform: uppercase;
 letter-spacing:.5px;
  }
.dia-fecha {
 font-size:.75rem;
 opacity:.9;
 margin-top: 2px;
  }
.comida-slot {
 padding: 14px 12px;
 border-bottom: 1px solid var(--color-borde);
 text-align: center;
  }
.comida-slot:last-child { border-bottom: none; }
.comida-icono { font-size: 1.6rem; margin-bottom: 4px; }
.comida-label {
 font-size:.72rem;
 color: var(--uv-gris-500);
 text-transform: uppercase;
 font-weight: 700;
 letter-spacing:.4px;
 margin-bottom: 8px;
  }
.comida-btn {
 width: 100%;
 padding: 7px 10px;
 border-radius: 8px;
 font-family: var(--fuente-display);
 font-weight: 600;
 font-size:.75rem;
 cursor: pointer;
 border: none;
 transition: all.15s;
  }
.comida-btn.apartar {
 background: var(--uv-gris-200);
 color: var(--uv-gris-700);
  }
.comida-btn.apartar:hover { background: var(--uv-azul); color: white; }
.comida-btn.apartado {
 background: var(--uv-verde-light);
 color: var(--uv-verde-dark);
 font-weight: 700;
  }
.comida-btn.apartado:hover { background: var(--uv-rojo-light); color: var(--uv-rojo); }
.comida-btn:disabled {
 background: var(--uv-gris-200);
 color: var(--uv-gris-500);
 cursor: not-allowed;
  }
.resumen-card {
 display: grid;
 grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
 gap: 12px;
 margin-bottom: 20px;
  }
.navega-semana {
 display: flex;
 align-items: center;
 justify-content: space-between;
 margin-bottom: 20px;
 gap: 12px;
 flex-wrap: wrap;
  }
.semana-titulo {
 font-family: var(--fuente-display);
 font-weight: 700;
 font-size: 1rem;
 color: var(--uv-azul);
  }
  @media (max-width: 768px) {
.cal-grid { grid-template-columns: repeat(2, 1fr); }
  }
  </style>
</head>
<body data-context-path="${pageContext.request.contextPath}">

<%@ include file="../_header.jsp" %>

<main class="page-wrapper">

  <div class="page-header">
  <div class="page-title">Mi Calendario de Comidas </div>
  <div class="page-subtitle">
 Aparta tus desayunos y comidas con anticipación.
 Tu beca te da <strong><%= becado.getComidasDisponiblesSemana() %></strong>
 comidas por semana.
  </div>
  </div>

  <% if (exito!= null) { %>
  <div class="alert alert-exito" data-auto-close> <%= exito %></div>
  <% } %>
  <% if (error!= null) { %>
  <div class="alert alert-error" data-auto-close> <%= error %></div>
  <% } %>

  <!-- Resumen -->
  <div class="resumen-card">
  <div class="stat-card">
  <div class="stat-icon verde"></div>
  <div>
  <div class="stat-valor"><%= becado.getComidasDisponiblesSemana() %></div>
  <div class="stat-label">Comidas por semana</div>
  </div>
  </div>
  <div class="stat-card">
  <div class="stat-icon azul"></div>
  <div>
  <div class="stat-valor"><%= totalApartados %></div>
  <div class="stat-label">Apartadas esta semana</div>
  </div>
  </div>
  <div class="stat-card">
  <div class="stat-icon <%= disponibles > 0? "verde" : "rojo" %>">
  <%= disponibles > 0? "" : "" %>
  </div>
  <div>
  <div class="stat-valor"><%= disponibles %></div>
  <div class="stat-label">Disponibles esta semana</div>
  </div>
  </div>
  </div>

  <!-- Navegador de semana -->
  <div class="navega-semana">
  <a href="?semana=<%= offset - 1 %>" class="btn btn-ghost btn-sm">
 Semana anterior
  </a>
  <div class="semana-titulo">
 Semana del <%= lunes.format(fmtCorto) %>al <%= viernes.format(fmtCompleto) %>
  <% if (offset == 0) { %><span style="color:var(--uv-amarillo);"> (esta semana)</span><% } %>
  </div>
  <a href="?semana=<%= offset + 1 %>" class="btn btn-ghost btn-sm">
 Semana siguiente
  </a>
  </div>

  <!-- Calendario -->
  <div class="cal-grid">
  <%
 for (int i = 0; i < 5; i++) {
 LocalDate fecha = lunes.plusDays(i);
 boolean esHoy = fecha.equals(hoy);
 boolean esPasado = fecha.isBefore(hoy);
 String claseDia = esHoy? "es-hoy" : (esPasado? "es-pasado" : "");
  %>
  <div class="dia-col <%= claseDia %>">
  <div class="dia-header">
  <div class="dia-nombre"><%= nombreDias[i] %></div>
  <div class="dia-fecha"><%= fecha.format(fmtCorto) %></div>
  </div>

  <!-- DESAYUNO -->
  <%
 ApartadoBecado apDes = mapaApartados.get(fecha + "-DESAYUNO");
  %>
  <div class="comida-slot">
  <div class="comida-icono"></div>
  <div class="comida-label">Desayuno</div>
  <% if (apDes!= null) { %>
  <form method="post" action="${pageContext.request.contextPath}/becado/calendario/cancelar">
  <input type="hidden" name="idApartado" value="<%= apDes.getIdApartado() %>">
  <button type="submit" class="comida-btn apartado"
  <%= esPasado? "disabled" : "" %>
 onclick="return confirm('¿Cancelar este apartado?');"
 title="Clic para cancelar">
 Apartado
  </button>
  </form>
  <% } else if (esPasado) { %>
  <button class="comida-btn" disabled>-</button>
  <% } else { %>
  <form method="post" action="${pageContext.request.contextPath}/becado/calendario/apartar">
  <input type="hidden" name="fecha" value="<%= fecha %>">
  <input type="hidden" name="tipoComida" value="DESAYUNO">
  <button type="submit" class="comida-btn apartar">
  + Apartar
  </button>
  </form>
  <% } %>
  </div>

  <!-- COMIDA -->
  <%
 ApartadoBecado apCom = mapaApartados.get(fecha + "-COMIDA");
  %>
  <div class="comida-slot">
  <div class="comida-icono"></div>
  <div class="comida-label">Comida</div>
  <% if (apCom!= null) { %>
  <form method="post" action="${pageContext.request.contextPath}/becado/calendario/cancelar">
  <input type="hidden" name="idApartado" value="<%= apCom.getIdApartado() %>">
  <button type="submit" class="comida-btn apartado"
  <%= esPasado? "disabled" : "" %>
 onclick="return confirm('¿Cancelar este apartado?');"
 title="Clic para cancelar">
 Apartado
  </button>
  </form>
  <% } else if (esPasado) { %>
  <button class="comida-btn" disabled>-</button>
  <% } else { %>
  <form method="post" action="${pageContext.request.contextPath}/becado/calendario/apartar">
  <input type="hidden" name="fecha" value="<%= fecha %>">
  <input type="hidden" name="tipoComida" value="COMIDA">
  <button type="submit" class="comida-btn apartar">
  + Apartar
  </button>
  </form>
  <% } %>
  </div>
  </div>
  <% } %>
  </div>

  <!-- Instrucciones -->
  <div class="alert alert-info mt-4">
  ℹ <strong>¿Cómo funciona?</strong>Aparta tus comidas con anticipación
 para asegurar tu lugar. Cada apartado descuenta una comida de tu cupo semanal.
 Puedes cancelar apartados antes del día consumo si cambias de planes.
  </div>

</main>

<%@ include file="../_footer.jsp" %>
</body>
</html>
