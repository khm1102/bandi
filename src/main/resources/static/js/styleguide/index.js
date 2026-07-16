import { showToast } from '../common/toast.js';
import { openModal } from '../common/modal.js';

const ACTION_OPEN_DEMO_MODAL = 'open-demo-modal';
const ACTION_SHOW_DEMO_TOAST = 'show-demo-toast';

document.addEventListener('click', (event) => {
    const button = event.target.closest('[data-action]');
    if (!button) {
        return;
    }
    if (button.dataset.action === ACTION_OPEN_DEMO_MODAL) {
        openModal('demoModal');
        return;
    }
    if (button.dataset.action === ACTION_SHOW_DEMO_TOAST) {
        showToast('저장되었습니다.');
    }
});
