/**
 * notificaciones.js
 * Polling de notificaciones en tiempo real.
 * Incluir en el layout principal: <script src="${pageContext.request.contextPath}/js/notificaciones.js"></script>
 */

const NotificacionesPolling = (() => {

    const INTERVALO_MS = 10000; // cada 10 segundos
    let intervaloId    = null;

    // ── Elementos del DOM ──────────────────────────────────────────
    const getBadge  = () => document.getElementById('notif-badge');
    const getCampana = () => document.getElementById('notif-campana');
    const getLista  = () => document.getElementById('notif-lista');

    // ── Iniciar polling ───────────────────────────────────────────
    function iniciar(contextPath) {
        const url = `${contextPath}/notificaciones/nuevas`;
        consultar(url);
        intervaloId = setInterval(() => consultar(url), INTERVALO_MS);
    }

    function detener() {
        if (intervaloId) clearInterval(intervaloId);
    }

    // ── Consultar al servidor ─────────────────────────────────────
    async function consultar(url) {
        try {
            const resp = await fetch(url, { credentials: 'same-origin' });
            if (!resp.ok) return;

            const data = await resp.json();
            actualizarBadge(data.total);

            if (data.total > 0) {
                mostrarToast(data.notificaciones[0]);
                actualizarLista(data.notificaciones);
            }

        } catch (err) {
            // Silenciar errores de red — el usuario no debe ver alertas por esto
        }
    }

    // ── Actualizar badge (número en la campana) ───────────────────
    function actualizarBadge(total) {
        const badge = getBadge();
        if (!badge) return;

        if (total > 0) {
            badge.textContent = total > 9 ? '9+' : total;
            badge.style.display = 'inline-block';
        } else {
            badge.style.display = 'none';
        }
    }

    // ── Toast: notificación flotante ──────────────────────────────
    function mostrarToast(notif) {
        // Evitar duplicados — verificar si ya se mostró este ID
        const yaVisto = sessionStorage.getItem(`notif_${notif.id}`);
        if (yaVisto) return;
        sessionStorage.setItem(`notif_${notif.id}`, '1');

        const toast = document.createElement('div');
        toast.className = 'notif-toast';
        toast.innerHTML = `
            <span class="notif-icono">${notif.icono}</span>
            <div class="notif-contenido">
                <strong>${notif.titulo}</strong>
                <p>${notif.mensaje}</p>
            </div>
            <button onclick="this.parentElement.remove()">✕</button>
        `;

        // Navegar al objeto relacionado al hacer clic
        if (notif.idReferencia && notif.modulo === 'PEDIDO') {
            toast.style.cursor = 'pointer';
            toast.addEventListener('click', () => {
                const ctx = document.body.dataset.contextPath || '';
                window.location.href = `${ctx}/pedido/detalle?id=${notif.idReferencia}`;
            });
        }

        document.body.appendChild(toast);

        // Auto-ocultar después de 5 segundos
        setTimeout(() => toast.remove(), 5000);

        // Marcar como leída en el servidor
        marcarLeida(notif.id);
    }

    // ── Actualizar lista desplegable ──────────────────────────────
    function actualizarLista(notificaciones) {
        const lista = getLista();
        if (!lista) return;

        lista.innerHTML = notificaciones.map(n => `
            <li class="notif-item notif-${n.tipo.toLowerCase()}">
                <span>${n.icono}</span>
                <div>
                    <strong>${n.titulo}</strong>
                    <small>${n.mensaje}</small>
                </div>
            </li>
        `).join('');
    }

    // ── Marcar leída ──────────────────────────────────────────────
    async function marcarLeida(id) {
        const ctx = document.body.dataset.contextPath || '';
        const form = new FormData();
        form.append('id', id);
        await fetch(`${ctx}/notificaciones/leer`, {
            method: 'POST',
            body: form,
            credentials: 'same-origin'
        });
    }

    async function marcarTodasLeidas(contextPath) {
        await fetch(`${contextPath}/notificaciones/leerTodas`, {
            method: 'POST',
            credentials: 'same-origin'
        });
        actualizarBadge(0);
        const lista = getLista();
        if (lista) lista.innerHTML = '<li>No hay notificaciones pendientes</li>';
    }

    return { iniciar, detener, marcarTodasLeidas };

})();

// Auto-iniciar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', () => {
    const ctx = document.body.dataset.contextPath || '';
    NotificacionesPolling.iniciar(ctx);
});
