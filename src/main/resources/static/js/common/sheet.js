import {lockBodyScroll, unlockBodyScroll} from './scroll-lock.js';
import {focusables, pushLayer, removeLayer} from './layer.js';

const sheetTriggers = new WeakMap();

export function openSheet(sheetId, trigger = document.activeElement) {
    const sheet = document.getElementById(sheetId);
    if (!sheet) {
        return;
    }
    sheetTriggers.set(sheet, trigger);
    sheet.classList.remove('hidden');
    sheet.setAttribute('aria-hidden', 'false');
    lockBodyScroll(sheet);
    pushLayer(sheet, () => closeSheet(sheet));
    const panel = sheet.querySelector('[data-sheet-panel]');
    const items = panel ? focusables(panel) : focusables(sheet);
    const initial = items.find((item) =>
        item.dataset.action !== 'close-sheet');
    if (initial) {
        initial.focus();
        return;
    }
    panel?.focus();
}

export function closeSheet(sheet) {
    if (!sheet) {
        return;
    }
    sheet.classList.add('hidden');
    sheet.setAttribute('aria-hidden', 'true');
    unlockBodyScroll(sheet);
    removeLayer(sheet);
    const trigger = sheetTriggers.get(sheet);
    if (trigger instanceof HTMLElement && trigger.getClientRects().length > 0) {
        trigger.focus();
    }
    sheetTriggers.delete(sheet);
}

export function closeSheetOf(element) {
    closeSheet(element.closest('[data-sheet-back]'));
}

document.addEventListener('click', (event) => {
    const opener = event.target.closest('[data-open-sheet]:not([data-open-sheet=""])');
    if (opener) {
        openSheet(opener.dataset.openSheet, opener);
        return;
    }
    const closeButton = event.target.closest('[data-action="close-sheet"]');
    if (closeButton) {
        closeSheetOf(closeButton);
        return;
    }
    if (event.target.matches('[data-sheet-back]')) {
        closeSheet(event.target);
    }
});
