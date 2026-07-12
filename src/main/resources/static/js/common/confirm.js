import { closeModal, openModal } from '/js/common/modal.js';

const CONFIRM_MODAL_ID = 'confirmModal';

let pendingForm = null;

document.addEventListener('click', (event) => {
    const trigger = event.target.closest('[data-confirm]');
    if (trigger) {
        const form = trigger.closest('form');
        if (!form) {
            return;
        }
        event.preventDefault();
        pendingForm = form;
        const message = document.querySelector(`#${CONFIRM_MODAL_ID} [data-confirm-message]`);
        message.textContent = trigger.dataset.confirm;
        openModal(CONFIRM_MODAL_ID);
        return;
    }
    const acceptButton = event.target.closest('[data-action="accept-confirm"]');
    if (!acceptButton) {
        return;
    }
    closeModal(acceptButton.closest('[data-modal-back]'));
    if (pendingForm) {
        pendingForm.submit();
        pendingForm = null;
    }
});
