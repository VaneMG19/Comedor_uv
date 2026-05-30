/*
  accesibilidad.js - Sistema de lectura por voz para Comedor UV
  Usa la Web Speech API (SpeechSynthesis) del navegador.

  Funciones:
 - Boton flotante para leer la pantalla en voz alta
  - Lectura automatica al pasar el cursor sobre elementos
  - Atajos de teclado (Alt+L, Alt+S, Alt+R)
  - Panel de configuracion (velocidad, voz, hover)
  - Recuerda preferencias en localStorage
 */
window.Accesibilidad = (() => {

    const LS_KEY = 'comedor_accesibilidad';
    let config = cargarConfig();
    let ultimoTexto = '';
    let utteranceActual = null;
    let hoverTimeout = null;

    function cargarConfig() {
        try {
            const raw = localStorage.getItem(LS_KEY);
            return raw ? JSON.parse(raw) : { activo: false, velocidad: 1.0, hover: false, vozIdx: 0 };
        } catch {
            return { activo: false, velocidad: 1.0, hover: false, vozIdx: 0 };
        }
    }

    function guardarConfig() {
        try { localStorage.setItem(LS_KEY, JSON.stringify(config)); } catch {}
    }

    function init() {
        if (!('speechSynthesis' in window)) {
            console.warn('[Accesibilidad] El navegador no soporta lectura por voz');
            return;
        }
        crearBoton();
        crearPanel();
        registrarAtajos();
        if (config.hover) activarHover();
        // Cargar voces (algunas requieren delay)
        speechSynthesis.onvoiceschanged = () => actualizarSelectorVoces();
        setTimeout(actualizarSelectorVoces, 500);
    }

    // BOTON FLOTANTE
    function crearBoton() {
        const btn = document.createElement('button');
        btn.id = 'a11y-btn';
        btn.title = 'Asistencia por voz (Alt+L para leer pantalla)';
        btn.setAttribute('aria-label', 'Activar lectura por voz de la pantalla');
        btn.innerHTML = '<svg width="28" height="28" viewBox="0 0 24 24" fill="white">' +
            '<path d="M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 7-8.77s-2.99-7.86-7-8.77z"/>' +
            '</svg>';
        btn.style.cssText = `
            position: fixed;
            bottom: 24px;
            right: 24px;
            width: 60px;
            height: 60px;
            border-radius: 50%;
            background: #1f4480;
            border: none;
            cursor: pointer;
            box-shadow: 0 4px 16px rgba(0,0,0,.3);
            z-index: 99998;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: transform .2s, background .2s;
        `;
        btn.onmouseenter = () => btn.style.transform = 'scale(1.1)';
        btn.onmouseleave = () => btn.style.transform = 'scale(1)';
        btn.onclick = (e) => {
            e.stopPropagation();
            if (speechSynthesis.speaking) {
                detener();
            } else {
                leerPantalla();
            }
        };
        // Long press = abrir panel
        let pressTimer = null;
        btn.onmousedown = () => {
            pressTimer = setTimeout(() => {
                togglePanel();
                pressTimer = null;
            }, 600);
        };
        btn.onmouseup = () => {
            if (pressTimer) clearTimeout(pressTimer);
        };

        // Boton de configuracion pequeno
        const btnConfig = document.createElement('button');
        btnConfig.id = 'a11y-config';
        btnConfig.title = 'Configurar asistencia por voz';
        btnConfig.setAttribute('aria-label', 'Configurar asistencia por voz');
        btnConfig.innerHTML = '<svg width="16" height="16" viewBox="0 0 24 24" fill="white">' +
            '<path d="M19.14,12.94c0.04-0.3,0.06-0.61,0.06-0.94c0-0.32-0.02-0.64-0.07-0.94l2.03-1.58c0.18-0.14,0.23-0.41,0.12-0.61 l-1.92-3.32c-0.12-0.22-0.37-0.29-0.59-0.22l-2.39,0.96c-0.5-0.38-1.03-0.7-1.62-0.94L14.4,2.81c-0.04-0.24-0.24-0.41-0.48-0.41 h-3.84c-0.24,0-0.43,0.17-0.47,0.41L9.25,5.35C8.66,5.59,8.12,5.92,7.63,6.29L5.24,5.33c-0.22-0.08-0.47,0-0.59,0.22L2.74,8.87 C2.62,9.08,2.66,9.34,2.86,9.48l2.03,1.58C4.84,11.36,4.8,11.69,4.8,12s0.02,0.64,0.07,0.94l-2.03,1.58 c-0.18,0.14-0.23,0.41-0.12,0.61l1.92,3.32c0.12,0.22,0.37,0.29,0.59,0.22l2.39-0.96c0.5,0.38,1.03,0.7,1.62,0.94l0.36,2.54 c0.05,0.24,0.24,0.41,0.48,0.41h3.84c0.24,0,0.44-0.17,0.47-0.41l0.36-2.54c0.59-0.24,1.13-0.56,1.62-0.94l2.39,0.96 c0.22,0.08,0.47,0,0.59-0.22l1.92-3.32c0.12-0.22,0.07-0.47-0.12-0.61L19.14,12.94z M12,15.6c-1.98,0-3.6-1.62-3.6-3.6 s1.62-3.6,3.6-3.6s3.6,1.62,3.6,3.6S13.98,15.6,12,15.6z"/>' +
            '</svg>';
        btnConfig.style.cssText = `
            position: fixed;
            bottom: 70px;
            right: 18px;
            width: 28px;
            height: 28px;
            border-radius: 50%;
            background: #475569;
            border: 2px solid white;
            cursor: pointer;
            z-index: 99999;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 0;
        `;
        btnConfig.onclick = (e) => { e.stopPropagation(); togglePanel(); };

        document.body.appendChild(btn);
        document.body.appendChild(btnConfig);
    }

    //  PANEL DE CONFIGURACION
    function crearPanel() {
        const panel = document.createElement('div');
        panel.id = 'a11y-panel';
        panel.setAttribute('role', 'dialog');
        panel.setAttribute('aria-label', 'Configuracion de asistencia por voz');
        panel.style.cssText = `
            position: fixed;
            bottom: 100px;
            right: 24px;
            width: 320px;
            background: white;
            border-radius: 16px;
            box-shadow: 0 10px 40px rgba(0,0,0,.25);
            z-index: 99997;
            padding: 18px;
            display: none;
            font-family: Arial, sans-serif;
        `;
        panel.innerHTML = `
            <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px;">
                <h3 style="margin:0;font-size:1rem;color:#1f4480;">Asistencia por voz</h3>
                <button id="a11y-close" aria-label="Cerrar panel"
                        style="border:none;background:none;font-size:1.2rem;cursor:pointer;color:#475569;">X</button>
            </div>

            <div style="margin-bottom:12px;">
                <button id="a11y-leer" style="width:100%;padding:12px;background:#1f4480;color:white;
                        border:none;border-radius:8px;font-weight:700;cursor:pointer;font-size:.95rem;">
                    Leer pantalla (Alt+L)
                </button>
            </div>

            <div style="margin-bottom:12px;">
                <button id="a11y-detener" style="width:100%;padding:10px;background:#dc2626;color:white;
                        border:none;border-radius:8px;font-weight:600;cursor:pointer;font-size:.85rem;">
                    Detener lectura (Alt+S)
                </button>
            </div>

            <div style="margin-bottom:10px;border-top:1px solid #e5e7eb;padding-top:12px;">
                <label style="display:flex;align-items:center;gap:8px;cursor:pointer;font-size:.85rem;">
                    <input type="checkbox" id="a11y-hover" ${config.hover ? 'checked' : ''}>
                    Leer al pasar el cursor
                </label>
            </div>

            <div style="margin-bottom:10px;">
                <label style="display:block;font-size:.8rem;color:#374151;margin-bottom:4px;font-weight:600;">
                    Velocidad: <span id="a11y-vel-val">${config.velocidad}x</span>
                </label>
                <input type="range" id="a11y-vel" min="0.5" max="2" step="0.1"
                       value="${config.velocidad}" style="width:100%;">
            </div>

            <div style="margin-bottom:10px;">
                <label style="display:block;font-size:.8rem;color:#374151;margin-bottom:4px;font-weight:600;">
                    Voz
                </label>
                <select id="a11y-voz" style="width:100%;padding:6px;border:1px solid #cbd5e1;
                        border-radius:6px;font-size:.85rem;">
                    <option>Cargando voces...</option>
                </select>
            </div>

            <div style="font-size:.7rem;color:#6b7280;border-top:1px solid #e5e7eb;padding-top:10px;line-height:1.5;">
                <strong>Atajos de teclado:</strong><br>
                Alt+L: Leer pantalla<br>
                Alt+S: Detener<br>
                Alt+R: Repetir<br>
                Tab: Navegar entre elementos<br>
                Enter/Espacio: Activar boton enfocado
            </div>
        `;
        document.body.appendChild(panel);

        // Eventos
        document.getElementById('a11y-close').onclick = () => panel.style.display = 'none';
        document.getElementById('a11y-leer').onclick = leerPantalla;
        document.getElementById('a11y-detener').onclick = detener;

        document.getElementById('a11y-vel').oninput = (e) => {
            config.velocidad = parseFloat(e.target.value);
            document.getElementById('a11y-vel-val').textContent = config.velocidad + 'x';
            guardarConfig();
        };
        document.getElementById('a11y-hover').onchange = (e) => {
            config.hover = e.target.checked;
            guardarConfig();
            if (config.hover) activarHover();
            else desactivarHover();
        };
        document.getElementById('a11y-voz').onchange = (e) => {
            config.vozIdx = parseInt(e.target.value);
            guardarConfig();
        };
    }

    function actualizarSelectorVoces() {
        const select = document.getElementById('a11y-voz');
        if (!select) return;
        const voces = speechSynthesis.getVoices().filter(v => v.lang.startsWith('es'));
        if (voces.length === 0) {
            select.innerHTML = '<option>Sin voces en espanol disponibles</option>';
            return;
        }
        select.innerHTML = voces.map((v, i) =>
            `<option value="${i}" ${i === config.vozIdx ? 'selected' : ''}>${v.name} (${v.lang})</option>`
        ).join('');
    }

    function togglePanel() {
        const panel = document.getElementById('a11y-panel');
        if (!panel) return;
        panel.style.display = panel.style.display === 'none' ? 'block' : 'none';
    }

    // LEER PANTALLA
    function leerPantalla() {
        detener();
        const texto = extraerTextoRelevante();
        if (!texto) {
            hablar('No hay contenido para leer en esta pantalla.');
            return;
        }
        hablar(texto);
    }

    function extraerTextoRelevante() {
        // Excluir: scripts, estilos, sidebar, botones de a11y, header repetido
        const partes = [];

        // Titulo de la pagina
        const titulo = document.querySelector('h1, .page-title');
        if (titulo) partes.push('Pagina actual: ' + titulo.textContent.trim());

        // Subtitulo si existe
        const subtitulo = document.querySelector('.page-subtitle');
        if (subtitulo) partes.push(subtitulo.textContent.trim());

        // Contenido principal
        const main = document.querySelector('main, .main-content, .page-wrapper');
        if (main) {
            // Recorrer elementos significativos
            const elementos = main.querySelectorAll('h1, h2, h3, h4, p, li, .nombre, .precio, button:not(#a11y-btn):not(#a11y-config), label, .alert, td, .platillo-card-nombre, .platillo-precio');
            const textosUnicos = new Set();
            elementos.forEach(el => {
                if (el.closest('#a11y-panel') || el.closest('#a11y-btn')) return;
                const t = (el.textContent || '').trim().replace(/\s+/g, ' ');
                if (t && t.length > 1 && !textosUnicos.has(t)) {
                    textosUnicos.add(t);
                    partes.push(t);
                }
            });
        }

        return partes.join('. ').substring(0, 4000);
    }

    function hablar(texto) {
        if (!texto) return;
        detener();
        ultimoTexto = texto;
        utteranceActual = new SpeechSynthesisUtterance(texto);
        utteranceActual.lang = 'es-MX';
        utteranceActual.rate = config.velocidad;
        const voces = speechSynthesis.getVoices().filter(v => v.lang.startsWith('es'));
        if (voces[config.vozIdx]) utteranceActual.voice = voces[config.vozIdx];
        speechSynthesis.speak(utteranceActual);
    }

    function detener() {
        if (speechSynthesis.speaking) speechSynthesis.cancel();
    }

    function repetir() {
        if (ultimoTexto) hablar(ultimoTexto);
    }

    //  HOVER (leer al pasar el cursor)
    function activarHover() {
        document.body.addEventListener('mouseover', onHover);
        document.body.addEventListener('focusin', onFocus);
    }
    function desactivarHover() {
        document.body.removeEventListener('mouseover', onHover);
        document.body.removeEventListener('focusin', onFocus);
    }
    function onHover(e) {
        if (e.target.closest('#a11y-btn') || e.target.closest('#a11y-panel') || e.target.closest('#a11y-config')) return;
        const el = e.target.closest('button, a, .platillo-card, .pos-platillo, input, select, label');
        if (!el) return;
        clearTimeout(hoverTimeout);
        hoverTimeout = setTimeout(() => {
            const texto = obtenerEtiqueta(el);
            if (texto) hablar(texto);
        }, 400);
    }
    function onFocus(e) {
        const el = e.target;
        if (el.closest('#a11y-btn') || el.closest('#a11y-panel')) return;
        const texto = obtenerEtiqueta(el);
        if (texto) hablar(texto);
    }
    function obtenerEtiqueta(el) {
        return el.getAttribute('aria-label') ||
            el.getAttribute('title') ||
            (el.textContent || '').trim().replace(/\s+/g, ' ').substring(0, 200);
    }

    // ATAJOS DE TECLADO
    function registrarAtajos() {
        document.addEventListener('keydown', (e) => {
            if (!e.altKey) return;
            if (e.key === 'l' || e.key === 'L') { e.preventDefault(); leerPantalla(); }
            else if (e.key === 's' || e.key === 'S') { e.preventDefault(); detener(); }
            else if (e.key === 'r' || e.key === 'R') { e.preventDefault(); repetir(); }
        });
    }

    return { init, leer: leerPantalla, hablar, detener, repetir };
})();

document.addEventListener('DOMContentLoaded', () => Accesibilidad.init());