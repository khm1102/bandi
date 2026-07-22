import {get, post} from '../common/api.js';
import {bindPageActions, element, lookup, readValue} from '../common/dom.js';
import {openModal} from '../common/modal.js';
import {showToast} from '../common/toast.js';
import {badge, closeActionModal} from '../common/view.js';

const ACTIONS = Object.freeze({
    SEARCH: 'notice-search', PUBLISH_OPEN: 'notice-publish-open',
    PUBLISH_SAVE: 'notice-publish-save', CLOSE: 'notice-close',
    ARCHIVE: 'notice-archive',
});
const STATUS = Object.freeze({
    DRAFT: ['초안', 'neutral'], SCHEDULED: ['게시 예정', 'warning'],
    PUBLISHED: ['게시 중', 'success'], CLOSED: ['게시 종료', 'neutral'],
    ARCHIVED: ['보관', 'neutral'],
});
let notices = [];
let publishingNotice = null;

function formatDateTime(value) {
    return value ? value.replace('T', ' ').slice(0, 16) : '-';
}

function actionButton(label, action, noticeId, danger = false) {
    const button = element('button', `min-h-11 rounded-md border bg-card px-3 text-xs font-bold hover:bg-secondary ${danger ? 'text-destructive' : ''}`, label);
    button.type = 'button';
    button.dataset.pageAction = action;
    button.dataset.noticeId = String(noticeId);
    return button;
}

function render() {
    const body = lookup('[data-notice-manage-list]');
    body.replaceChildren();
    if (notices.length === 0) {
        const row = element('tr');
        const cell = element('td', 'px-5 py-12 text-center text-sm text-muted-foreground', '조건에 맞는 공시가 없습니다.');
        cell.colSpan = 5;
        row.appendChild(cell);
        body.appendChild(row);
        return;
    }
    notices.forEach((notice) => {
        const row = element('tr');
        const info = element('td');
        const title = element(notice.status === 'ARCHIVED' ? 'span' : 'a',
                'inline-flex min-h-11 items-center font-black hover:text-accent-foreground hover:underline',
                notice.title);
        if (notice.status !== 'ARCHIVED') {
            title.href = `/notice-management/${notice.publicNoticeId}/edit`;
        }
        info.append(title, element('p', 'text-xs text-muted-foreground', notice.categoryCode));
        const statusCell = element('td');
        const [statusLabel, tone] = STATUS[notice.status] || [notice.status, 'neutral'];
        statusCell.appendChild(badge(statusLabel, tone));
        const period = element('td', 'text-xs text-muted-foreground', `${formatDateTime(notice.publishStartDttm)} — ${formatDateTime(notice.publishEndDttm)}`);
        const updated = element('td', 'text-xs text-muted-foreground', `${notice.updatedByName || '-'} · ${formatDateTime(notice.updatedDttm)}`);
        const actions = element('td', 'text-right');
        const group = element('div', 'inline-flex flex-wrap justify-end gap-1');
        if (['DRAFT', 'SCHEDULED', 'PUBLISHED'].includes(notice.status)) {
            group.appendChild(actionButton('게시 설정', ACTIONS.PUBLISH_OPEN, notice.publicNoticeId));
        }
        if (['SCHEDULED', 'PUBLISHED'].includes(notice.status)) {
            group.appendChild(actionButton('게시 종료', ACTIONS.CLOSE, notice.publicNoticeId, true));
        }
        if (notice.status !== 'ARCHIVED') {
            const archive = actionButton('보관', ACTIONS.ARCHIVE, notice.publicNoticeId, true);
            archive.dataset.confirm = '이 공시를 보관할까요?';
            archive.dataset.confirmAction = '공시 보관';
            group.appendChild(archive);
        }
        actions.appendChild(group);
        row.append(info, statusCell, period, updated, actions);
        body.appendChild(row);
    });
}

async function load() {
    notices = await get('/api/admin/public-notices', {
        keyword: readValue('noticeManageKeyword'),
        status: readValue('noticeManageStatus'), pageSize: 100,
    });
    render();
}

function toLocalInput(value) {
    return value ? value.slice(0, 16) : '';
}

function openPublish(trigger) {
    publishingNotice = notices.find((notice) =>
        notice.publicNoticeId === Number(trigger.dataset.noticeId));
    lookup('[data-notice-publish-title]').textContent = publishingNotice.title;
    document.getElementById('noticePublishStart').value =
            toLocalInput(publishingNotice.publishStartDttm)
            || new Date(Date.now() - new Date().getTimezoneOffset() * 60000)
                    .toISOString().slice(0, 16);
    document.getElementById('noticePublishEnd').value =
            toLocalInput(publishingNotice.publishEndDttm);
    openModal('noticePublishModal', trigger);
}

async function withBusy(trigger, task) {
    trigger.disabled = true;
    try { await task(); } catch (error) {
        showToast(error.message || '요청을 처리하지 못했습니다.');
    } finally { trigger.disabled = false; }
}

async function savePublish(trigger) {
    if (!lookup('[data-notice-publish-form]').reportValidity()) return;
    const start = readValue('noticePublishStart');
    const end = readValue('noticePublishEnd');
    if (end && end <= start) {
        showToast('게시 종료 시각은 시작 시각보다 늦어야 합니다.');
        document.getElementById('noticePublishEnd').focus();
        return;
    }
    await withBusy(trigger, async () => {
        await post(`/api/admin/public-notices/${publishingNotice.publicNoticeId}/publish`, {
            publishStartDttm: start,
            publishEndDttm: end || null,
        });
        closeActionModal(trigger);
        await load();
        showToast('공시 게시 설정을 저장했습니다.');
    });
}

async function transition(trigger, action, message) {
    await withBusy(trigger, async () => {
        await post(`/api/admin/public-notices/${trigger.dataset.noticeId}/${action}`);
        await load();
        showToast(message);
    });
}

bindPageActions({
    [ACTIONS.SEARCH]: () => load().catch((error) => showToast(error.message)),
    [ACTIONS.PUBLISH_OPEN]: openPublish,
    [ACTIONS.PUBLISH_SAVE]: savePublish,
    [ACTIONS.CLOSE]: (trigger) => transition(trigger, 'close', '공시 게시를 종료했습니다.'),
    [ACTIONS.ARCHIVE]: (trigger) => transition(trigger, 'archive', '공시를 보관했습니다.'),
});
load().catch((error) => showToast(error.message || '공시를 불러오지 못했습니다.'));
