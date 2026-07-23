import {ApiError, del, get, post} from '../common/api.js';
import {all, bindPageActions, element, lookup} from '../common/dom.js';
import {showToast} from '../common/toast.js';

const root = lookup('[data-manage-detail]');
const noticeId = root.dataset.noticeId;
const readStatusList = lookup('[data-read-status-list]');
const readStatusPanel = lookup('[data-read-status-panel]');
const readSummary = lookup('[data-read-summary]');
let readStatusesLoaded = false;

function formatDateTime(value) {
    if (!value) {
        return '미확인';
    }
    return new Date(value).toLocaleString('ko-KR', {
        year: 'numeric',
        month: 'numeric',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
    });
}

function setActionButtonsDisabled(disabled) {
    all('[data-page-action]', root).forEach((button) => {
        if (button.dataset.pageAction !== 'reload'
                && button.dataset.pageAction !== 'toggle-read-status') {
            button.disabled = disabled;
        }
    });
}

function showActionError(error) {
    const errorBox = lookup('[data-manage-error]');
    const conflict = error instanceof ApiError && error.code === 'NI002';
    lookup('[data-manage-error-message]', errorBox).textContent = conflict
        ? '공지 상태가 다른 운영자에 의해 변경됐어요. 새 상태를 불러온 뒤 다시 시도해 주세요.'
        : error.message || '요청을 처리하지 못했습니다. 새로고침 후 다시 시도해 주세요.';
    errorBox.classList.remove('hidden');
    errorBox.focus?.();
}

async function runStateAction(action, successMessage) {
    setActionButtonsDisabled(true);
    try {
        await post(`/api/internal-notice-management/${noticeId}/${action}`);
        showToast(successMessage);
        window.setTimeout(() => window.location.reload(), 350);
    } catch (error) {
        showActionError(error);
        setActionButtonsDisabled(false);
    }
}

async function deleteDraft() {
    setActionButtonsDisabled(true);
    try {
        await del(`/api/internal-notice-management/${noticeId}`);
        window.location.assign('/notices/manage');
    } catch (error) {
        showActionError(error);
        setActionButtonsDisabled(false);
    }
}

function appendReadStatus(status) {
    const row = element('li', 'flex flex-col gap-1 py-3 text-sm sm:flex-row sm:items-center sm:justify-between');
    const member = element('div');
    member.appendChild(element('b', 'block', status.memberName));
    member.appendChild(element('span', 'text-xs text-muted-foreground',
        `${status.teamName} · ${status.studentNo}`));
    const hasRead = Boolean(status.firstReadDttm);
    const read = element('span', hasRead
        ? 'font-bold text-success' : 'font-bold text-muted-foreground',
    hasRead ? formatDateTime(status.lastReadDttm) : '미확인');
    row.append(member, read);
    readStatusList.appendChild(row);
}

async function loadReadStatuses() {
    if (!readStatusList || !readSummary) {
        return;
    }
    try {
        const statuses = await get(
            `/api/internal-notice-management/${noticeId}/read-statuses`);
        readStatusList.replaceChildren();
        statuses.forEach(appendReadStatus);
        const readCount = statuses.filter((status) => status.firstReadDttm).length;
        readSummary.textContent = `${statuses.length}명 중 ${readCount}명이 확인했어요.`;
        lookup('[data-page-action="retry-read-status"]')?.classList.add('hidden');
        readStatusesLoaded = true;
    } catch (error) {
        readSummary.textContent = '확인 현황을 불러오지 못했습니다.';
        lookup('[data-page-action="retry-read-status"]')?.classList.remove('hidden');
    }
}

function toggleReadStatuses(trigger) {
    const expanded = trigger.getAttribute('aria-expanded') === 'true';
    trigger.setAttribute('aria-expanded', String(!expanded));
    trigger.textContent = expanded ? '멤버별 현황 보기' : '멤버별 현황 접기';
    readStatusPanel?.classList.toggle('hidden', expanded);
    if (!expanded && !readStatusesLoaded) {
        loadReadStatuses();
    }
}

bindPageActions({
    'return-draft': () => runStateAction('draft', '공지를 초안으로 되돌렸어요.'),
    close: () => runStateAction('close', '공지 게시를 종료했어요.'),
    archive: () => runStateAction('archive', '공지를 보관했어요.'),
    'delete-draft': deleteDraft,
    reload: () => window.location.reload(),
    'toggle-read-status': toggleReadStatuses,
    'retry-read-status': loadReadStatuses,
}, document);

loadReadStatuses();
