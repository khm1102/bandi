import {get, post} from '../common/api.js';
import {bindPageActions, element, lookup, readValue} from '../common/dom.js';
import {closeSheetOf, openSheet} from '../common/sheet.js';
import {showToast} from '../common/toast.js';
import {badge} from '../common/view.js';

const ACTIONS = Object.freeze({
    SEARCH: 'notice-search', PUBLISH_OPEN: 'notice-publish-open',
    PUBLISH_SAVE: 'notice-publish-save', CLOSE: 'notice-close',
    ARCHIVE: 'notice-archive', NEXT: 'notice-next-action',
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
    renderNextNotice();
    if (notices.length === 0) {
        body.appendChild(element('p', 'px-5 py-12 text-center text-sm text-muted-foreground',
                '조건에 맞는 공시가 없습니다.'));
        return;
    }
    notices.forEach((notice) => {
        const row = element('article',
                'grid gap-3 border-b px-4 py-5 last:border-b-0 md:grid-cols-[minmax(0,1fr)_auto] md:items-center md:px-5');
        const info = element('div', 'min-w-0');
        const title = element(notice.status === 'ARCHIVED' ? 'span' : 'a',
                'inline-flex min-h-11 items-center text-base font-bold hover:text-accent-foreground hover:underline',
                notice.title);
        if (notice.status !== 'ARCHIVED') {
            title.href = `/notice-management/${notice.publicNoticeId}/edit`;
        }
        const meta = element('div', 'flex flex-wrap items-center gap-2');
        const [statusLabel, tone] = STATUS[notice.status] || [notice.status, 'neutral'];
        meta.append(badge(statusLabel, tone), element('span', 'text-xs text-muted-foreground',
                notice.categoryCode));
        info.append(meta, title, element('p', 'text-xs text-muted-foreground',
                `게시 ${formatDateTime(notice.publishStartDttm)} — ${formatDateTime(notice.publishEndDttm)} · ${notice.updatedByName || '-'} 수정`));
        const group = element('div', 'grid grid-cols-2 gap-2 sm:flex sm:flex-wrap sm:justify-end');
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
        row.append(info, group);
        body.appendChild(row);
    });
}

function renderNextNotice() {
    const notice = notices.find((item) => item.status === 'DRAFT')
            || notices.find((item) => item.status === 'SCHEDULED')
            || notices.find((item) => item.status === 'PUBLISHED');
    const action = lookup('[data-notice-next-action]');
    action.classList.toggle('hidden', !notice);
    if (!notice) {
        lookup('[data-notice-next-title]').textContent = notices.length === 0
                ? '작성 중이거나 게시 중인 공시가 없어요' : '지금 처리할 공시가 없어요';
        lookup('[data-notice-next-message]').textContent =
                '새 공식 안내가 필요하면 공시를 작성해 주세요.';
        return;
    }
    lookup('[data-notice-next-title]').textContent = notice.title;
    lookup('[data-notice-next-message]').textContent = notice.status === 'DRAFT'
            ? '초안의 본문과 첨부를 마치고 게시 기간을 설정해 주세요.'
            : notice.status === 'SCHEDULED'
                ? `${formatDateTime(notice.publishStartDttm)}에 게시될 예정이에요.`
                : `현재 외부에 게시 중이며 ${formatDateTime(notice.publishEndDttm)}에 종료돼요.`;
    const button = lookup('button', action);
    button.dataset.noticeId = String(notice.publicNoticeId);
    button.dataset.nextMode = notice.status === 'DRAFT' ? 'edit' : 'publish';
    button.textContent = notice.status === 'DRAFT' ? '공시 이어서 작성' : '게시 설정 확인';
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
    openSheet('noticePublishSheet', trigger);
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
        closeSheetOf(trigger);
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
    [ACTIONS.NEXT]: (trigger) => {
        if (trigger.dataset.nextMode === 'edit') {
            window.location.assign(`/notice-management/${trigger.dataset.noticeId}/edit`);
            return;
        }
        openPublish(trigger);
    },
});
load().catch((error) => showToast(error.message || '공시를 불러오지 못했습니다.'));
