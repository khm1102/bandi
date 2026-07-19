import {lockBodyScroll, unlockBodyScroll} from './scroll-lock.js';
import {focusables, pushLayer, removeLayer} from './layer.js';

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
    pushLayer(modal, () => closeModal(modal));
    const items = focusables(modal);
    if (items.length > 0) {
        items[0].focus();
        return;
    }
    modal.querySelector('[data-modal-panel]')?.focus();
}

export function closeModal(modal) {
    if (!modal) {
        return;
    }
    modal.classList.add('hidden');
    modal.classList.remove('flex');
    modal.setAttribute('aria-hidden', 'true');
    unlockBodyScroll(modal);
    removeLayer(modal);
    const trigger = modalTriggers.get(modal);
    if (trigger instanceof HTMLElement && trigger.getClientRects().length > 0) {
        trigger.focus();
    }
    modalTriggers.delete(modal);
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
