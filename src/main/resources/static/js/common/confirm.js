import { closeModal, openModal } from './modal.js';

const CONFIRM_MODAL_ID = 'confirmModal';

let pendingForm = null;
let pendingTrigger = null;
let confirmedTrigger = null;

document.addEventListener('click', (event) => {
    const confirmSource = event.target.closest('[data-confirm]:not([data-confirm=""])');
    if (confirmSource) {
        if (confirmSource === confirmedTrigger) {
            confirmedTrigger = null;
            return;
        }
        const message = document.querySelector(`#${CONFIRM_MODAL_ID} [data-confirm-message]`);
        const acceptButton = document.querySelector(`#${CONFIRM_MODAL_ID} [data-action="accept-confirm"]`);
        if (!message || !acceptButton) {
            return;
        }
        event.preventDefault();
        event.stopImmediatePropagation();
        pendingForm = confirmSource.closest('form');
        pendingTrigger = confirmSource;
        message.textContent = confirmSource.dataset.confirm;
        acceptButton.textContent = confirmSource.dataset.confirmAction || '계속';
        openModal(CONFIRM_MODAL_ID, confirmSource);
        return;
    }
    const acceptButton = event.target.closest('[data-action="accept-confirm"]');
    if (!acceptButton) {
        return;
    }
    const confirmedSource = pendingTrigger;
    const form = pendingForm;
    closeModal(acceptButton.closest('[data-modal-back]'));
    pendingTrigger = null;
    pendingForm = null;
    if (form) {
        form.requestSubmit(confirmedSource);
        return;
    }
    if (confirmedSource) {
        confirmedTrigger = confirmedSource;
        confirmedSource.click();
    }
});
