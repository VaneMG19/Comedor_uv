/**
 * styles.js — UI global: sidebar, dropdowns, tabs, responsive
 */
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('sidebar-overlay');
    if (!sidebar) return;
    const abierto = sidebar.classList.toggle('movil-abierto');
    if (overlay) overlay.style.display = abierto ? 'block' : 'none';
    document.body.style.overflow = abierto ? 'hidden' : '';
}

function toggleDropdown() {
    document.getElementById('user-dropdown')?.classList.toggle('activo');
}

document.addEventListener('click', e => {
    const menu = document.querySelector('.user-menu');
    if (menu && !menu.contains(e.target))
        document.getElementById('user-dropdown')?.classList.remove('activo');
});

function checkResponsive() {
    const toggle = document.getElementById('sidebar-toggle');
    if (toggle) toggle.style.display = window.innerWidth <= 768 ? 'flex' : 'none';
}
window.addEventListener('resize', checkResponsive);
document.addEventListener('DOMContentLoaded', checkResponsive);

function switchModalTab(tabId, btn) {
    ['detalle','nutricion'].forEach(t => {
        const el = document.getElementById('modal-tab-' + t);
        if (el) el.style.display = 'none';
    });
    document.querySelectorAll('.modal .tab-btn').forEach(b => b.classList.remove('activo'));
    const target = document.getElementById('modal-tab-' + tabId);
    if (target) target.style.display = 'block';
    btn.classList.add('activo');
}

function actualizarTipoPedido(tipo) {
    const campos = document.getElementById('campos-anticipado');
    if (campos) campos.style.display = tipo === 'ANTICIPADO' ? 'block' : 'none';
}

document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.alert[data-auto-close]').forEach(alert => {
        setTimeout(() => {
            alert.style.transition = 'opacity .4s';
            alert.style.opacity = '0';
            setTimeout(() => alert.remove(), 400);
        }, 3500);
    });
    document.querySelectorAll('[data-confirm]').forEach(el => {
        el.addEventListener('click', e => {
            if (!confirm(el.dataset.confirm || '¿Estás seguro?')) e.preventDefault();
        });
    });
});
