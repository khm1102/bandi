import {lockBodyScroll, unlockBodyScroll} from './scroll-lock.js';

const FOCUSABLE_SELECTOR = 'a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])';
const modalTriggers = new WeakMap();

export function openModal(modalId, trigger = document.activeElement) {
    const modal = document.getElementById(modalId);
    if (!modal) {
        return;
    }
    modalTriggers.set(modal, trigger);
    modal.classList.remove('hidden');
    modal.classList.add('flex');
    modal.setAttribute('aria-hidden', 'false');
    lockBodyScroll(modal);
    const focusables = modal.querySelectorAll(FOCUSABLE_SELECTOR);
    if (focusables.length > 0) {
        focusables[0].focus();
        return;
    }
    const panel = modal.querySelector('[data-modal-panel]');
    if (panel) {
        panel.focus();
    }
}

export function closeModal(modal) {
    if (!modal) {
        return;
    }
    modal.classList.add('hidden');
    modal.classList.remove('flex');
    modal.setAttribute('aria-hidden', 'true');
    unlockBodyScroll(modal);
    const trigger = modalTriggers.get(modal);
    if (trigger instanceof HTMLElement) {
        trigger.focus();
    }
    modalTriggers.delete(modal);
}

function lookupOpenedModal() {
    return document.querySelector('[data-modal-back].flex');
}

document.addEventListener('click', (event) => {
    const opener = event.target.closest('[data-open-modal]:not([data-open-modal=""])');
    if (opener) {
        openModal(opener.dataset.openModal, opener);
        return;
    }
    const closeButton = event.target.closest('[data-action="close-modal"]');
    if (closeButton) {
        closeModal(closeButton.closest('[data-modal-back]'));
        return;
    }
    if (event.target.matches('[data-modal-back]')) {
        closeModal(event.target);
    }
});

document.addEventListener('keydown', (event) => {
    const modal = lookupOpenedModal();
    if (!modal) {
        return;
    }
    if (event.key === 'Escape') {
        closeModal(modal);
        return;
    }
    if (event.key !== 'Tab') {
        return;
    }
    const focusables = modal.querySelectorAll(FOCUSABLE_SELECTOR);
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
