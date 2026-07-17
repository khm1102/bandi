import {showToast} from '../common/toast.js';
import {bindPageActions, lookup} from '../common/dom.js';
import {badge, closeActionModal} from '../common/view.js';

const ACTIONS = Object.freeze({
    SAVE: 'schedule-save',
    CONFIRM: 'schedule-confirm'
});

function toggleTimeCell(cell) {
    const selected = cell.classList.contains('bg-success');
    cell.classList.toggle('bg-success', !selected);
    cell.classList.toggle('border-success', !selected);
    cell.classList.toggle('bg-secondary', selected);
}

function saveSchedule(trigger) {
    lookup('[data-schedule-my-title]').textContent = '내 가능 시간을 입력했어요';
    lookup('[data-schedule-my-description]').textContent = '수정하려면 다시 시간 입력하기를 누르세요';
    lookup('[data-schedule-my-badge]').replaceChildren(badge('응답 완료', 'success'));
    closeActionModal(trigger);
    showToast('가능 시간을 저장했어요');
}

document.addEventListener('click', (event) => {
    const cell = event.target.closest('[data-time-cell]');
    if (cell) {
        toggleTimeCell(cell);
    }
});

bindPageActions({
    [ACTIONS.SAVE]: saveSchedule,
    [ACTIONS.CONFIRM]: () => showToast('추천 시간을 캘린더에 등록했어요')
});
