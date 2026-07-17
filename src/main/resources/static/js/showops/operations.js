import {openModal} from '../common/modal.js';
import {showToast} from '../common/toast.js';
import {all, bindPageActions, lookup, readValue} from '../common/dom.js';
import {badge, closeActionModal} from '../common/view.js';

const ACTIONS = Object.freeze({
    CHECK_IN: 'entry-checkin',
    CANCEL_ENTRY: 'entry-cancel',
    EDIT: 'show-edit',
    CREATE: 'show-create',
    DELETE: 'show-delete',
    SAVE: 'show-save'
});

let editingShowCard = null;
let showCardTemplate = null;
let showCardHost = null;
let showCardAnchor = null;

const SHOW_STATUS_TONES = Object.freeze({
    '신청 진행 중': ['bg-success-soft', 'text-success'],
    '준비 중': ['bg-warning-soft', 'text-warning'],
    '마감': ['bg-destructive-soft', 'text-destructive']
});
const SHOW_STATUS_CLASSES = Object.values(SHOW_STATUS_TONES).flat();

function updateEntrySummary() {
    const rows = all('[data-entry-list] tr');
    const completedRows = rows.filter((row) => row.cells[4].textContent.trim() === '입장 완료');
    const completedSeats = completedRows.reduce(
        (total, row) => total + row.cells[2].querySelectorAll('span').length,
        0
    );
    const pendingCount = rows.length - completedRows.length;
    const entryRate = rows.length === 0 ? 0 : Math.round((completedRows.length / rows.length) * 100);
    lookup('[data-entry-pending-summary]').textContent = `미입장 ${pendingCount}건`;
    lookup('[data-stat-value="entry-completed"]').textContent = String(completedRows.length);
    lookup('[data-stat-delta="entry-completed-seats"]').textContent = `${completedSeats}석 입장`;
    lookup('[data-stat-value="entry-pending"]').textContent = String(pendingCount);
    lookup('[data-stat-delta="entry-rate"]').textContent = `입장률 ${entryRate}%`;
}

function updateShowStatus(card, status) {
    const statusText = lookup('[data-show-status]', card);
    const statusBadge = statusText.parentElement;
    statusText.textContent = status;
    statusBadge.classList.remove(...SHOW_STATUS_CLASSES);
    statusBadge.classList.add(...SHOW_STATUS_TONES[status]);
}

function changeEntry(trigger, checkedIn) {
    const row = trigger.closest('tr');
    const statusCell = row.cells[4];
    const timeCell = row.cells[5];
    const status = checkedIn ? '입장 완료' : '미입장';
    statusCell.replaceChildren(badge(status, checkedIn ? 'success' : 'warning'));
    if (checkedIn) {
        const now = new Date();
        const hour = String(now.getHours()).padStart(2, '0');
        const minute = String(now.getMinutes()).padStart(2, '0');
        timeCell.textContent = `${hour}:${minute}`;
        trigger.textContent = '입장 취소';
        trigger.dataset.pageAction = ACTIONS.CANCEL_ENTRY;
        updateEntrySummary();
        showToast(`${row.cells[0].textContent.trim()}님 입장 처리했어요`);
        return;
    }
    timeCell.textContent = '—';
    trigger.textContent = '입장 완료';
    trigger.dataset.pageAction = ACTIONS.CHECK_IN;
    updateEntrySummary();
}

function editShow(trigger) {
    const card = trigger.closest('[data-show-card]');
    document.getElementById('shTitle').value = lookup('[data-show-title]', card).textContent.trim();
    document.getElementById('shDesc').value = lookup('[data-show-description]', card).textContent.trim();
    document.getElementById('shPeriod').value = lookup('[data-show-period]', card).textContent.trim();
    document.getElementById('shTime').value = lookup('[data-show-time]', card).textContent.trim();
    document.getElementById('shPlace').value = lookup('[data-show-place]', card).textContent.trim();
    document.getElementById('shStatus').value = lookup('[data-show-status]', card).textContent.trim();
    const viewing = lookup('[data-show-viewing]', card).textContent.trim().split(' · ');
    document.getElementById('shAge').value = viewing[0];
    document.getElementById('shRuntime').value = viewing[1] || '';
    editingShowCard = card;
    lookup('#showModal h2').textContent = '공연 수정';
    lookup('#showModal [data-page-action="show-save"]').textContent = '수정 저장';
    openModal('showModal');
}

function createShow() {
    editingShowCard = null;
    ['shTitle', 'shPeriod', 'shTime', 'shPlace', 'shAge', 'shRuntime', 'shDesc'].forEach((id) => {
        document.getElementById(id).value = '';
    });
    document.getElementById('shStatus').value = '신청 진행 중';
    lookup('#showModal h2').textContent = '공연 등록';
    lookup('#showModal [data-page-action="show-save"]').textContent = '등록';
    openModal('showModal');
}

function saveShow(trigger) {
    const title = readValue('shTitle');
    if (!title) {
        showToast('공연명을 입력해 주세요');
        return;
    }
    const firstCard = lookup('[data-show-card]');
    const baseCard = firstCard || showCardTemplate;
    if (!baseCard || !showCardHost) {
        showToast('공연 카드를 표시할 수 없어요');
        return;
    }
    const card = editingShowCard || baseCard.cloneNode(true);
    if (!editingShowCard) {
        showCardHost.insertBefore(card, showCardAnchor);
    }
    lookup('[data-show-title]', card).textContent = title;
    lookup('[data-show-description]', card).textContent = readValue('shDesc');
    lookup('[data-show-period]', card).textContent = readValue('shPeriod');
    lookup('[data-show-time]', card).textContent = readValue('shTime');
    lookup('[data-show-place]', card).textContent = readValue('shPlace');
    lookup('[data-show-viewing]', card).textContent = `${readValue('shAge')} · ${readValue('shRuntime')}`;
    updateShowStatus(card, readValue('shStatus'));
    closeActionModal(trigger);
    showToast(editingShowCard ? '공연 정보를 수정했어요' : '공연을 등록했어요');
    editingShowCard = null;
}

const firstCard = lookup('[data-show-card]');
showCardTemplate = firstCard ? firstCard.cloneNode(true) : null;
showCardHost = firstCard ? firstCard.parentElement : null;
showCardAnchor = firstCard ? firstCard.nextElementSibling : null;
updateEntrySummary();

bindPageActions({
    [ACTIONS.CHECK_IN]: (trigger) => changeEntry(trigger, true),
    [ACTIONS.CANCEL_ENTRY]: (trigger) => changeEntry(trigger, false),
    [ACTIONS.EDIT]: editShow,
    [ACTIONS.CREATE]: createShow,
    [ACTIONS.DELETE]: (trigger) => {
        trigger.closest('[data-show-card]').remove();
        showToast('공연을 삭제했어요');
    },
    [ACTIONS.SAVE]: saveShow
});
