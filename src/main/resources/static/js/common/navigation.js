const navigationPanel = document.querySelector('[data-navigation-panel]');
const navigationBackdrop = document.querySelector('[data-navigation-backdrop]');
const navigationToggle = document.querySelector('[data-navigation-toggle]');
const desktopMedia = window.matchMedia('(min-width: 1024px)');
const FOCUSABLE_SELECTOR = 'a[href], button:not([disabled]), [tabindex]:not([tabindex="-1"])';

function setNavigation(open) {
    if (!navigationPanel || !navigationBackdrop || !navigationToggle) {
        return;
    }
    navigationPanel.classList.toggle('-translate-x-full', !open);
    navigationBackdrop.classList.toggle('hidden', !open);
    navigationToggle.setAttribute('aria-expanded', String(open));
    navigationToggle.setAttribute('aria-label', open ? '전체 메뉴 닫기' : '전체 메뉴 열기');
    navigationPanel.setAttribute('aria-hidden', String(!open));
    document.body.classList.toggle('overflow-hidden', open);
    if (open) {
        const currentLink = navigationPanel.querySelector('[aria-current="page"]');
        const firstLink = navigationPanel.querySelector('a[href]');
        (currentLink || firstLink)?.focus();
        return;
    }
    navigationToggle.focus();
}

function syncNavigationViewport(desktop) {
    if (!navigationPanel || !navigationBackdrop || !navigationToggle) {
        return;
    }
    navigationPanel.classList.toggle('-translate-x-full', !desktop);
    navigationBackdrop.classList.add('hidden');
    navigationToggle.setAttribute('aria-expanded', 'false');
    navigationToggle.setAttribute('aria-label', '전체 메뉴 열기');
    navigationPanel.setAttribute('aria-hidden', String(!desktop));
    document.body.classList.remove('overflow-hidden');
}

navigationToggle?.addEventListener('click', () => {
    setNavigation(navigationToggle.getAttribute('aria-expanded') !== 'true');
});

navigationBackdrop?.addEventListener('click', () => {
    setNavigation(false);
});

navigationPanel?.addEventListener('click', (event) => {
    if (event.target.closest('a[href]') && !desktopMedia.matches) {
        setNavigation(false);
    }
});

document.addEventListener('keydown', (event) => {
    const navigationOpen = navigationToggle?.getAttribute('aria-expanded') === 'true';
    if (event.key === 'Escape' && navigationOpen) {
        setNavigation(false);
        return;
    }
    if (event.key !== 'Tab' || !navigationOpen || !navigationPanel) {
        return;
    }
    const focusables = navigationPanel.querySelectorAll(FOCUSABLE_SELECTOR);
    if (focusables.length === 0) {
        return;
    }
    const first = focusables[0];
    const last = focusables[focusables.length - 1];
    if (event.shiftKey && document.activeElement === first) {
        event.preventDefault();
        last.focus();
        return;
    }
    if (!event.shiftKey && document.activeElement === last) {
        event.preventDefault();
        first.focus();
    }
});

desktopMedia.addEventListener('change', (event) => {
    syncNavigationViewport(event.matches);
});

syncNavigationViewport(desktopMedia.matches);
