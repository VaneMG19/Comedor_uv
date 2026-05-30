/*
  notificaciones.js
  Toast flotante para notificaciones nuevas
 */
window.NotificacionesPolling = (() => {
    const INTERVALO_MS = 15000;
    const TOAST_DURACION_MS = 8000;
    const LS_KEY = 'comedor_notif_mostradas';
    let notificacionesActuales = [];
    let ctxPath = '';

    const getBadge    = () => document.getElementById('notif-badge');
    const getDropdown = () => document.getElementById('notif-dropdown');

    // localStorage helpers
    function obtenerIdsMostrados() {
        try {
            const raw = localStorage.getItem(LS_KEY);
            return raw ? new Set(JSON.parse(raw)) : new Set();
        } catch { return new Set(); }
    }
    function guardarIdsMostrados(set) {
        try {
            // Limitar a los ultimos 100 para no crecer infinitamente
            const arr = Array.from(set).slice(-100);
            localStorage.setItem(LS_KEY, JSON.stringify(arr));
        } catch {}
    }

    function iniciar(contextPath) {
        ctxPath = contextPath;
        console.log('[Notif] Iniciado con ctx:', ctxPath);
        crearContenedorToasts();
        consultar();
        setInterval(consultar, INTERVALO_MS);

        document.addEventListener('click', (e) => {
            const dd = getDropdown();
            if (dd && dd.style.display === 'block'
                && !dd.contains(e.target)
                && !e.target.closest('#notif-campana')) {
                dd.style.display = 'none';
            }
        });
    }

    function crearContenedorToasts() {
        if (document.getElementById('notif-toasts')) return;
        const div = document.createElement('div');
        div.id = 'notif-toasts';
        div.style.cssText = `
            position: fixed;
            top: 80px;
            right: 20px;
            display: flex;
            flex-direction: column;
            gap: 10px;
            z-index: 9999;
            max-width: 340px;
            pointer-events: none;
        `;
        document.body.appendChild(div);
    }

    async function consultar() {
        try {
            const resp = await fetch(`${ctxPath}/notificaciones/nuevas`,
                { credentials: 'same-origin' });
            if (!resp.ok) {
                console.warn('[Notif] HTTP', resp.status);
                return;
            }
            const data = await resp.json();
            const notifs = data.notificaciones || [];

            // Mostrar toast SOLO para IDs que no esten en localStorage
            const yaMostrados = obtenerIdsMostrados();
            let huboNuevos = false;
            notifs.forEach(n => {
                if (!yaMostrados.has(n.id)) {
                    console.log('[Notif] Mostrando toast para ID:', n.id);
                    mostrarToast(n);
                    yaMostrados.add(n.id);
                    huboNuevos = true;
                }
            });
            if (huboNuevos) guardarIdsMostrados(yaMostrados);

            notificacionesActuales = notifs;
            actualizarBadge(data.total != null ? data.total : notifs.length);
            const dd = getDropdown();
            if (dd && dd.style.display === 'block') renderDropdown();
        } catch (err) {
            console.warn('[Notif] Error:', err);
        }
    }

    function mostrarToast(notif) {
        const cont = document.getElementById('notif-toasts');
        if (!cont) {
            console.warn('[Notif] No hay contenedor de toasts');
            return;
        }

        const toast = document.createElement('div');
        toast.style.cssText = `
            background: white;
            border-left: 4px solid #1f4480;
            border-radius: 10px;
            padding: 14px 18px;
            box-shadow: 0 6px 20px rgba(0,0,0,.18);
            display: flex;
            gap: 12px;
            align-items: flex-start;
            opacity: 0;
            transform: translateX(360px);
            transition: opacity .3s, transform .3s;
            pointer-events: auto;
            cursor: pointer;
        `;

        toast.innerHTML = `
            <div style="flex:1;min-width:0;">
                <div style="font-weight:700;font-size:.9rem;color:#1f4480;
                            margin-bottom:3px;">
                    ${escapeHtml(notif.titulo)}
                </div>
                <div style="font-size:.8rem;color:#374151;line-height:1.4;">
                    ${escapeHtml(notif.mensaje)}
                </div>
            </div>
            <button style="border:none;background:none;cursor:pointer;
                           color:#6b7280;font-size:1rem;font-weight:700;
                           flex-shrink:0;padding:0 4px;line-height:1;">X</button>
        `;

        const btnCerrar = toast.querySelector('button');
        btnCerrar.addEventListener('click', (e) => {
            e.stopPropagation();
            cerrarToast(toast);
        });

        toast.addEventListener('click', () => {
            marcarLeida(notif.id);
            cerrarToast(toast);
        });

        cont.appendChild(toast);

        requestAnimationFrame(() => {
            toast.style.opacity = '1';
            toast.style.transform = 'translateX(0)';
        });

        setTimeout(() => cerrarToast(toast), TOAST_DURACION_MS);
    }

    function cerrarToast(toast) {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(360px)';
        setTimeout(() => toast.remove(), 300);
    }

    function actualizarBadge(total) {
        const badge = getBadge();
        if (!badge) return;
        badge.textContent = total > 9 ? '9+' : total;
        badge.style.display = total > 0 ? 'inline-flex' : 'none';
    }

    function toggleDropdown() {
        const dd = getDropdown();
        if (!dd) return;
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
                <div style="padding:20px;text-align:center;color:#6b7280;">
                    <div style="font-size:.9rem;font-weight:600;">Sin notificaciones</div>
                </div>`;
            return;
        }

        let html = `
            <div style="padding:14px 16px;border-bottom:1px solid #e5e7eb;
                        display:flex;justify-content:space-between;align-items:center;">
                <span style="font-weight:700;color:#1f4480;font-size:.95rem;">
                    Notificaciones
                </span>
                <button onclick="NotificacionesPolling.marcarTodasLeidas()"
                        style="border:none;background:none;color:#1f4480;
                               font-size:.75rem;cursor:pointer;font-weight:600;">
                    Marcar todas leidas
                </button>
            </div>
            <div style="max-height:380px;overflow-y:auto;">`;

        notificacionesActuales.forEach(n => {
            html += `
                <div style="padding:12px 16px;border-bottom:1px solid #e5e7eb;
                            display:flex;gap:10px;align-items:flex-start;">
                    <div style="flex:1;min-width:0;">
                        <div style="font-weight:700;font-size:.85rem;margin-bottom:2px;">
                            ${escapeHtml(n.titulo)}
                        </div>
                        <div style="font-size:.78rem;color:#374151;line-height:1.4;">
                            ${escapeHtml(n.mensaje)}
                        </div>
                    </div>
                    <button onclick="event.stopPropagation();NotificacionesPolling.marcarLeida(${n.id});"
                            title="Marcar como leida"
                            style="border:none;background:none;cursor:pointer;
                                   color:#6b7280;font-size:1rem;font-weight:700;
                                   flex-shrink:0;padding:4px 8px;">X</button>
                </div>`;
        });
        html += `</div>`;
        dd.innerHTML = html;
    }

    async function marcarLeida(id) {
        console.log('[Notif] Marcar leida ID:', id);
        try {
            const resp = await fetch(`${ctxPath}/notificaciones/leer`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: 'id=' + encodeURIComponent(id),
                credentials: 'same-origin'
            });
            console.log('[Notif] Respuesta:', resp.status);
            if (resp.ok) {
                notificacionesActuales = notificacionesActuales.filter(n => n.id !== id);
                actualizarBadge(notificacionesActuales.length);
                renderDropdown();
                setTimeout(() => consultar(), 500);
            }
        } catch (e) {
            console.warn('[Notif] Error:', e);
        }
    }

    async function marcarTodasLeidas() {
        const copia = notificacionesActuales.slice();
        for (const n of copia) {
            try {
                await fetch(`${ctxPath}/notificaciones/leer`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: 'id=' + encodeURIComponent(n.id),
                    credentials: 'same-origin'
                });
            } catch (e) { console.warn(e); }
        }
        notificacionesActuales = [];
        actualizarBadge(0);
        renderDropdown();
        setTimeout(() => consultar(), 500);
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
