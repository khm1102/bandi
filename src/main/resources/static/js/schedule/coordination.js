import {showToast} from '../common/toast.js';
import {all, bindPageActions, lookup} from '../common/dom.js';
import {badge, closeActionModal} from '../common/view.js';

const ACTIONS = Object.freeze({
    SAVE: 'schedule-save',
    CONFIRM: 'schedule-confirm'
});
const SCHEDULE_DATES = ['6월 23일', '6월 24일', '6월 25일', '6월 26일', '6월 27일'];

function toggleTimeCell(cell) {
    const selected = cell.classList.contains('bg-success');
    cell.classList.toggle('bg-success', !selected);
    cell.classList.toggle('border-success', !selected);
    cell.classList.toggle('bg-secondary', selected);
    cell.setAttribute('aria-pressed', String(!selected));
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

all('[data-time-cell]').forEach((cell, index) => {
    const date = SCHEDULE_DATES[index % SCHEDULE_DATES.length];
    const hour = 18 + Math.floor(index / SCHEDULE_DATES.length);
    const selected = cell.classList.contains('bg-success');
    cell.setAttribute('aria-label', `${date} ${hour}시 가능`);
    cell.setAttribute('aria-pressed', String(selected));
});
