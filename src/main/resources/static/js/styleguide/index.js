import {openModal} from '../common/modal.js';
import {openSheet} from '../common/sheet.js';
import {showToast} from '../common/toast.js';
import {activateFilterChip} from '../common/view.js';

const ACTION_OPEN_DEMO_MODAL = 'open-demo-modal';
const ACTION_SHOW_DEMO_TOAST = 'show-demo-toast';
const SHEET_BY_ACTION = {
    'open-form-sheet': 'demoFormSheet',
    'open-workspace-sheet': 'demoWorkspaceSheet',
    'open-panel-sheet': 'demoPanelSheet'
};

document.addEventListener('click', (event) => {
    const filterChip = event.target.closest('[data-filter-group="style-guide"]');
    if (filterChip) {
        activateFilterChip(filterChip);
        return;
    }
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
        return;
    }
    const sheetId = SHEET_BY_ACTION[button.dataset.pageAction];
    if (sheetId) {
        openSheet(sheetId, button);
    }
});
