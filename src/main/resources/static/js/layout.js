/** Điều hướng dùng chung; Bootstrap quản lý overlay, focus trap và khóa cuộn. */
document.addEventListener('DOMContentLoaded', function () {
    const sidebar = document.getElementById('sidebarMenu');
    if (!sidebar) return;
    if (!document.documentElement.lang) document.documentElement.lang = 'vi';

    const main = document.querySelector('main');
    if (main) {
        if (!main.id) main.id = 'app-main';
        if (!main.hasAttribute('tabindex')) main.setAttribute('tabindex', '-1');
        const skipLink = document.querySelector('.skip-link');
        if (skipLink) skipLink.setAttribute('href', '#' + main.id);
    }

    // So khớp URL được server sinh, kể cả context path và trang con /new, /{id}/edit.
    const links = Array.from(sidebar.querySelectorAll('.nav-link'));
    const path = window.location.pathname.replace(/\/$/, '');
    const matches = links.map(function (link) {
        const routes = [new URL(link.href, window.location.origin).pathname, link.dataset.navAlias]
            .filter(Boolean).map(route => route.replace(/\/$/, ''));
        const length = Math.max(0, ...routes.filter(route => path === route || path.startsWith(route + '/')).map(route => route.length));
        return { link, length };
    }).sort((a, b) => b.length - a.length);
    if (matches[0] && matches[0].length > 0) {
        links.forEach(function (link) {
            const selected = link === matches[0].link;
            link.classList.toggle('active', selected);
            if (selected) link.setAttribute('aria-current', 'page');
            else link.removeAttribute('aria-current');
        });
    }

    const toggles = document.querySelectorAll('[aria-controls="sidebarMenu"]');
    function updateExpanded(expanded) {
        toggles.forEach(toggle => toggle.setAttribute('aria-expanded', String(expanded)));
    }
    sidebar.addEventListener('show.bs.offcanvas', () => updateExpanded(true));
    sidebar.addEventListener('hide.bs.offcanvas', () => updateExpanded(false));

    // Khi chuyển từ overlay sang desktop, giải phóng backdrop/khóa cuộn nếu đang mở.
    const desktop = window.matchMedia('(min-width: 1280px)');
    desktop.addEventListener('change', function (event) {
        if (event.matches && window.bootstrap) {
            const offcanvas = window.bootstrap.Offcanvas.getInstance(sidebar);
            if (offcanvas) offcanvas.hide();
            updateExpanded(false);
        }
    });
});
