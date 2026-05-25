/**
 * notificaciones.js — Polling + dropdown clickeable
 * v2 — campana funcional
 */
window.NotificacionesPolling = (() => {
    const INTERVALO_MS = 10000;
    let notificacionesActuales = [];

    const getBadge    = () => document.getElementById('notif-badge');
    const getDropdown = () => document.getElementById('notif-dropdown');

    function iniciar(contextPath) {
        const url = `${contextPath}/notificaciones/nuevas`;
        consultar(url);
        setInterval(() => consultar(url), INTERVALO_MS);

        // Cerrar dropdown al hacer clic fuera
        document.addEventListener('click', (e) => {
            const dd = getDropdown();
            if (dd && dd.style.display === 'block'
                && !dd.contains(e.target)
                && !e.target.closest('#notif-campana')) {
                dd.style.display = 'none';
            }
        });
    }

    async function consultar(url) {
        try {
            const resp = await fetch(url, { credentials: 'same-origin' });
            if (!resp.ok) return;
            const data = await resp.json();
            notificacionesActuales = data.notificaciones || [];
            actualizarBadge(data.total);
            const dd = getDropdown();
            if (dd && dd.style.display === 'block') renderDropdown();
        } catch (err) {
            console.warn('[Notif] Error:', err);
        }
    }

    function actualizarBadge(total) {
        const badge = getBadge();
        if (!badge) return;
        badge.textContent = total > 9 ? '9+' : total;
        badge.style.display = total > 0 ? 'inline-flex' : 'none';
    }

    function toggleDropdown() {
        const dd = getDropdown();
        if (!dd) {
            console.warn('[Notif] #notif-dropdown no existe en el DOM');
            return;
        }
        if (dd.style.display === 'block') {
            dd.style.display = 'none';
        } else {
            renderDropdown();
            dd.style.display = 'block';
        }
    }

    function renderDropdown() {
        const dd = getDropdown();
        if (!dd) return;

        if (notificacionesActuales.length === 0) {
            dd.innerHTML = `
                <div style="padding:20px;text-align:center;color:var(--uv-gris-500);">
                    <div style="font-size:2rem;margin-bottom:8px;">🔕</div>
                    <div style="font-size:.9rem;font-weight:600;">Sin notificaciones</div>
                    <div style="font-size:.75rem;margin-top:4px;">
                        Te avisaremos cuando algo importante pase
                    </div>
                </div>`;
            return;
        }

        const ctx = document.body.dataset.contextPath || '';
        let html = `
            <div style="padding:14px 16px;border-bottom:1px solid var(--color-borde);
                        display:flex;justify-content:space-between;align-items:center;">
                <span style="font-family:var(--fuente-display);font-weight:700;
                             color:var(--uv-azul);font-size:.95rem;">
                    🔔 Notificaciones
                </span>
                <button onclick="NotificacionesPolling.marcarTodasLeidas()"
                        style="border:none;background:none;color:var(--uv-azul);
                               font-size:.75rem;cursor:pointer;font-weight:600;">
                    Marcar todas leídas
                </button>
            </div>
            <div style="max-height:380px;overflow-y:auto;">`;

        notificacionesActuales.forEach(n => {
            const icono = n.icono || '🔔';
            const clickable = n.idReferencia && n.modulo === 'PEDIDO';
            html += `
                <div style="padding:12px 16px;border-bottom:1px solid var(--color-borde);
                            display:flex;gap:10px;align-items:flex-start;
                            cursor:${clickable ? 'pointer' : 'default'};
                            transition:background .15s;"
                     onmouseover="this.style.background='var(--uv-gris-100)';"
                     onmouseout="this.style.background='white';"
                     ${clickable ? `onclick="window.location.href='${ctx}/pedido/detalle?id=${n.idReferencia}'"` : ''}>
                    <div style="font-size:1.4rem;flex-shrink:0;">${icono}</div>
                    <div style="flex:1;min-width:0;">
                        <div style="font-weight:700;font-size:.85rem;margin-bottom:2px;">
                            ${escapeHtml(n.titulo)}
                        </div>
                        <div style="font-size:.78rem;color:var(--uv-gris-700);line-height:1.4;">
                            ${escapeHtml(n.mensaje)}
                        </div>
                    </div>
                    <button onclick="event.stopPropagation();NotificacionesPolling.marcarLeida(${n.id})"
                            title="Marcar como leída"
                            style="border:none;background:none;cursor:pointer;
                                   color:var(--uv-gris-500);font-size:.85rem;flex-shrink:0;">✕</button>
                </div>`;
        });
        html += `</div>`;
        dd.innerHTML = html;
    }

    async function marcarLeida(id) {
        const ctx = document.body.dataset.contextPath || '';
        try {
            const form = new FormData();
            form.append('id', id);
            await fetch(`${ctx}/notificaciones/leer`, {
                method: 'POST', body: form, credentials: 'same-origin'
            });
            notificacionesActuales = notificacionesActuales.filter(n => n.id !== id);
            actualizarBadge(notificacionesActuales.length);
            renderDropdown();
        } catch (e) { console.warn(e); }
    }

    async function marcarTodasLeidas() {
        for (const n of notificacionesActuales.slice()) {
            await marcarLeida(n.id);
        }
    }

    function escapeHtml(s) {
        if (s == null) return '';
        return String(s)
            .replace(/&/g, '&amp;').replace(/</g, '&lt;')
            .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
    }

    return { iniciar, marcarLeida, marcarTodasLeidas, toggleDropdown };
})();

document.addEventListener('DOMContentLoaded', () => {
    const ctx = document.body.dataset.contextPath || '';
    NotificacionesPolling.iniciar(ctx);
});
