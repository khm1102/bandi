import {lockBodyScroll, unlockBodyScroll} from './scroll-lock.js';
import {pushLayer, removeLayer} from './layer.js';

const navigationPanel = document.querySelector('[data-navigation-panel]');
const navigationBackdrop = document.querySelector('[data-navigation-backdrop]');
const navigationToggle = document.querySelector('[data-navigation-toggle]');
const desktopMedia = window.matchMedia('(min-width: 1024px)');

function setNavigation(open) {
    if (!navigationPanel || !navigationBackdrop || !navigationToggle) {
        return;
    }
    navigationPanel.classList.toggle('-translate-x-full', !open);
    navigationBackdrop.classList.toggle('hidden', !open);
    navigationToggle.setAttribute('aria-expanded', String(open));
    navigationToggle.setAttribute('aria-label', open ? '전체 메뉴 닫기' : '전체 메뉴 열기');
    navigationPanel.setAttribute('aria-hidden', String(!open));
    navigationPanel.inert = !open;
    if (open) {
        lockBodyScroll(navigationPanel);
        pushLayer(navigationPanel, () => setNavigation(false));
        const currentLink = navigationPanel.querySelector('[aria-current="page"]');
        const firstLink = navigationPanel.querySelector('a[href]');
        (currentLink || firstLink)?.focus();
        return;
    }
    unlockBodyScroll(navigationPanel);
    removeLayer(navigationPanel);
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
    navigationPanel.inert = !desktop;
    unlockBodyScroll(navigationPanel);
    removeLayer(navigationPanel);
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

desktopMedia.addEventListener('change', (event) => {
    syncNavigationViewport(event.matches);
});

syncNavigationViewport(desktopMedia.matches);
