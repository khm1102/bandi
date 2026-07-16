import { closeModal, openModal } from './modal.js';

const CONFIRM_MODAL_ID = 'confirmModal';

let pendingForm = null;

document.addEventListener('click', (event) => {
    const trigger = event.target.closest('[data-confirm]');
    if (trigger) {
        const message = document.querySelector(`#${CONFIRM_MODAL_ID} [data-confirm-message]`);
        if (!message) {
            return;
        }
        event.preventDefault();
        // form 밖의 트리거는 pendingForm이 null — 모달만 띄우고 확인 시 닫기만 한다
        pendingForm = trigger.closest('form');
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
