import {ApiError, get, post} from '../common/api.js';
import {all, bindPageActions, debounce, element, lookup, readValue} from '../common/dom.js';
import {openModal} from '../common/modal.js';
import {showToast} from '../common/toast.js';
import {activateFilterChip, badge, closeActionModal} from '../common/view.js';

const ACTIONS = Object.freeze({
    DOWNLOAD: 'resource-download',
    UPLOAD: 'resource-upload',
    NOTICE_ADD: 'notice-add',
    NOTICE_OPEN: 'notice-open',
});
const CATEGORY_LABELS = Object.freeze({
    SCRIPT: '대본',
    MINUTES: '회의록',
    PROMOTION: '홍보물',
    VIDEO: '영상',
    OTHER: '기타',
});

let notices = [];
let resources = [];
let loginMember = null;

function errorMessage(error) {
    if (error instanceof ApiError && error.fieldErrors.length > 0) {
        return error.fieldErrors[0].reason;
    }
    return error.message || '요청을 처리하지 못했습니다.';
}

function formatDate(value) {
    if (!value) {
        return '—';
    }
    return new Intl.DateTimeFormat('ko-KR', {
        month: '2-digit',
        day: '2-digit',
    }).format(new Date(value));
}

function setInlineError(selector, message) {
    const container = lookup(selector);
    container.textContent = message || '';
    container.classList.toggle('hidden', !message);
}

function activateInfoTab(button) {
    const selectedTab = button.dataset.infoTab;
    all('[data-info-tab]').forEach((tab) => {
        const selected = tab === button;
        tab.setAttribute('aria-selected', String(selected));
        tab.classList.toggle('border', selected);
        tab.classList.toggle('bg-card', selected);
        tab.classList.toggle('text-foreground', selected);
        tab.classList.toggle('text-muted-foreground', !selected);
    });
    all('[data-info-panel]').forEach((panel) => {
        panel.classList.toggle('hidden', panel.dataset.infoPanel !== selectedTab);
    });
}

function setNoticeState(title, message, retry = false) {
    const state = lookup('[data-notice-state]');
    state.classList.remove('hidden');
    lookup('[data-notice-state-title]', state).textContent = title;
    lookup('[data-notice-state-message]', state).textContent = message;
    lookup('[data-notice-retry]', state).classList.toggle('hidden', !retry);
}

function hideNoticeState() {
    lookup('[data-notice-state]').classList.add('hidden');
}

function noticeMatchesFilter(notice, filter) {
    if (filter === 'UNREAD') {
        return !notice.read;
    }
    if (filter === 'ALL_SCOPE') {
        return notice.targetScope === 'ALL';
    }
    if (filter === 'TEAM_SCOPE') {
        return notice.targetScope === 'TEAM';
    }
    return true;
}

function appendNoticeCard(notice) {
    const template = lookup('[data-notice-card-template]');
    const card = template.content.firstElementChild.cloneNode(true);
    card.dataset.noticeId = notice.internalNoticeId;
    card.dataset.read = String(notice.read);
    card.dataset.targetScope = notice.targetScope;
    const badges = lookup('[data-notice-badges]', card);
    if (notice.important) {
        badges.appendChild(badge('중요', 'accent'));
        card.classList.add('border-primary/40', 'bg-accent/40');
        card.classList.remove('bg-card');
    }
    badges.appendChild(badge(notice.targetScope === 'TEAM'
        ? notice.teamName || '팀 공지' : '전체', notice.targetScope === 'TEAM'
        ? 'info' : 'neutral'));
    badges.appendChild(badge(notice.read ? '확인 완료' : '내가 미확인',
            notice.read ? 'success' : 'warning'));
    lookup('[data-notice-title]', card).textContent = notice.title;
    lookup('[data-notice-meta]', card).textContent =
            `${formatDate(notice.publishStartDttm)} 게시`;
    lookup('[data-notice-list]').appendChild(card);
}

function filterNotices() {
    const selected = lookup('[data-filter-group="notice"][aria-pressed="true"]');
    const filter = selected?.dataset.filterValue || 'ALL';
    const visible = notices.filter((notice) => noticeMatchesFilter(notice,
            filter));
    const list = lookup('[data-notice-list]');
    list.replaceChildren();
    if (visible.length === 0) {
        list.classList.add('hidden');
        setNoticeState('조건에 맞는 공지가 없습니다',
                '다른 공지 분류를 선택해 확인해 보세요.');
        return;
    }
    hideNoticeState();
    list.classList.remove('hidden');
    list.classList.add('flex');
    visible.forEach(appendNoticeCard);
}

async function loadNotices() {
    lookup('[data-notice-list]').classList.add('hidden');
    setNoticeState('공지를 불러오는 중입니다', '잠시만 기다려 주세요.');
    try {
        notices = await get('/api/internal-notices', {pageSize: 100});
        filterNotices();
    } catch (error) {
        setNoticeState('공지를 불러오지 못했습니다', errorMessage(error),
                true);
    }
}

function setResourceState(title, message, retry = false) {
    const state = lookup('[data-resource-state]');
    state.hidden = false;
    lookup('[data-resource-state-title]', state).textContent = title;
    lookup('[data-resource-state-message]', state).textContent = message;
    lookup('[data-resource-retry]', state).classList.toggle('hidden', !retry);
}

function clearResourceRows() {
    all('[data-resource-row]').forEach((row) => row.remove());
}

function appendResourceRow(resource) {
    const template = lookup('[data-resource-row-template]');
    const row = template.content.firstElementChild.cloneNode(true);
    row.dataset.resourceId = resource.resourceId;
    row.dataset.category = resource.categoryCode;
    lookup('[data-resource-name]', row).textContent = resource.title;
    lookup('[data-resource-category]', row).appendChild(badge(
            CATEGORY_LABELS[resource.categoryCode] || resource.categoryCode,
            'neutral'));
    lookup('[data-resource-version]', row).textContent =
            resource.currentRevisionNo ? `v${resource.currentRevisionNo}` : '—';
    lookup('[data-resource-uploader]', row).textContent =
            resource.updatedByName || '—';
    lookup('[data-resource-date]', row).textContent =
            formatDate(resource.updatedDttm);
    lookup('[data-resource-list]').appendChild(row);
}

function renderPinnedResource() {
    const container = lookup('[data-pinned-resource]');
    const pinned = resources.find((resource) => resource.pinned);
    container.classList.toggle('hidden', !pinned);
    container.classList.toggle('flex', Boolean(pinned));
    if (!pinned) {
        return;
    }
    lookup('[data-pinned-resource-title]', container).textContent = pinned.title;
    lookup('[data-pinned-resource-meta]', container).textContent =
            `${CATEGORY_LABELS[pinned.categoryCode] || pinned.categoryCode} · ${pinned.updatedByName || '수정자 미상'} · ${formatDate(pinned.updatedDttm)} 수정`;
}

function filterResources() {
    const selected = lookup('[data-filter-group="resource"][aria-pressed="true"]');
    const category = selected?.dataset.filterValue || 'ALL';
    const query = lookup('[data-resource-search]').value.trim().toLowerCase();
    const visible = resources.filter((resource) => {
        const categoryMatched = category === 'ALL'
                || resource.categoryCode === category;
        const searchable = `${resource.title} ${resource.updatedByName || ''}`
                .toLowerCase();
        return categoryMatched && (!query || searchable.includes(query));
    });
    clearResourceRows();
    if (visible.length === 0) {
        setResourceState('조건에 맞는 자료가 없습니다',
                '검색어나 분류를 바꿔 다시 확인해 보세요.');
        return;
    }
    lookup('[data-resource-state]').hidden = true;
    visible.forEach(appendResourceRow);
}

async function loadResources() {
    clearResourceRows();
    setResourceState('자료를 불러오는 중입니다', '잠시만 기다려 주세요.');
    try {
        resources = await get('/api/resources', {pageSize: 100});
        renderPinnedResource();
        filterResources();
    } catch (error) {
        setResourceState('자료를 불러오지 못했습니다', errorMessage(error),
                true);
    }
}

async function lookupLoginMember() {
    if (!loginMember) {
        loginMember = await get('/api/members/me');
    }
    return loginMember;
}

function targetTeamId(targetScope, member) {
    return targetScope === 'TEAM' ? member.teamId : null;
}

function resetNoticeForm() {
    document.getElementById('ntTarget').value = 'ALL';
    document.getElementById('ntTitle').value = '';
    document.getElementById('ntBody').value = '';
    document.getElementById('ntImportant').checked = false;
    setInlineError('[data-notice-form-error]', '');
}

async function addNotice(trigger) {
    trigger.disabled = true;
    setInlineError('[data-notice-form-error]', '');
    try {
        const member = await lookupLoginMember();
        const targetScope = readValue('ntTarget');
        const created = await post('/api/internal-notice-management', {
            targetScope,
            teamId: targetTeamId(targetScope, member),
            title: readValue('ntTitle'),
            body: readValue('ntBody'),
            important: document.getElementById('ntImportant').checked,
            attachmentFileIds: [],
        });
        await post(`/api/internal-notice-management/${created.internalNoticeId}/publish`, {
            publishStartDttm: null,
            publishEndDttm: null,
        });
        closeActionModal(trigger);
        resetNoticeForm();
        showToast('공지를 게시했습니다.');
        await loadNotices();
    } catch (error) {
        setInlineError('[data-notice-form-error]', errorMessage(error));
    } finally {
        trigger.disabled = false;
    }
}

function resetResourceForm() {
    document.getElementById('upName').value = '';
    document.getElementById('upCat').value = 'SCRIPT';
    document.getElementById('upTarget').value = 'ALL';
    document.getElementById('upDescription').value = '';
    document.getElementById('upFile').value = '';
    document.getElementById('upPinned').checked = false;
    setInlineError('[data-resource-form-error]', '');
}

async function uploadResource(trigger) {
    const file = document.getElementById('upFile').files[0];
    if (!file) {
        setInlineError('[data-resource-form-error]', '파일을 선택해 주세요.');
        return;
    }
    trigger.disabled = true;
    setInlineError('[data-resource-form-error]', '');
    try {
        const member = await lookupLoginMember();
        const formData = new FormData();
        formData.append('file', file);
        const uploaded = await post('/api/files/private', formData, {
            query: {domain: 'resource'},
        });
        const targetScope = readValue('upTarget');
        const created = await post('/api/resource-management', {
            targetScope,
            teamId: targetTeamId(targetScope, member),
            categoryCode: readValue('upCat'),
            title: readValue('upName'),
            description: readValue('upDescription'),
            pinned: document.getElementById('upPinned').checked,
            storedFileIds: [uploaded.id],
        });
        await post(`/api/resource-management/${created.resourceId}/publish`);
        closeActionModal(trigger);
        resetResourceForm();
        showToast('자료를 업로드하고 게시했습니다.');
        await loadResources();
    } catch (error) {
        setInlineError('[data-resource-form-error]', errorMessage(error));
    } finally {
        trigger.disabled = false;
    }
}

function appendDetailFile(noticeId, attachment) {
    const link = element('a',
            'inline-flex min-h-11 items-center rounded-md border bg-card px-3 text-xs font-bold transition-colors hover:bg-secondary',
            attachment.originalName);
    link.href = `/api/internal-notices/${noticeId}/attachments/${attachment.storedFileId}/download`;
    lookup('[data-notice-detail-files]').appendChild(link);
}

async function openNotice(trigger) {
    trigger.disabled = true;
    try {
        const card = trigger.closest('[data-notice-card]');
        const detail = await get(`/api/internal-notices/${card.dataset.noticeId}`);
        const badges = lookup('[data-notice-detail-badges]');
        badges.replaceChildren();
        if (detail.important) {
            badges.appendChild(badge('중요', 'accent'));
        }
        badges.appendChild(badge(detail.targetScope === 'TEAM'
            ? detail.teamName || '팀 공지' : '전체', detail.targetScope === 'TEAM'
            ? 'info' : 'neutral'));
        lookup('[data-notice-detail-title]').textContent = detail.title;
        lookup('[data-notice-detail-body]').textContent = detail.body;
        lookup('[data-notice-detail-meta]').textContent =
                `${detail.publishedByName || '작성자 미상'} · ${formatDate(detail.publishStartDttm)} 게시`;
        const files = lookup('[data-notice-detail-files]');
        files.replaceChildren();
        detail.attachments.forEach((attachment) => appendDetailFile(
                detail.internalNoticeId, attachment));
        const notice = notices.find((item) => item.internalNoticeId
                === detail.internalNoticeId);
        if (notice) {
            notice.read = true;
        }
        openModal('noticeDetailModal', trigger);
    } catch (error) {
        showToast(errorMessage(error));
    } finally {
        trigger.disabled = false;
    }
}

async function downloadResource(trigger) {
    trigger.disabled = true;
    try {
        const resourceId = trigger.closest('[data-resource-row]')
                .dataset.resourceId;
        const detail = await get(`/api/resources/${resourceId}`);
        const file = detail.files[0];
        if (!file) {
            showToast('다운로드할 파일이 없습니다.');
            return;
        }
        window.location.assign(
                `/api/resources/${resourceId}/files/${file.storedFileId}/download`);
    } catch (error) {
        showToast(errorMessage(error));
    } finally {
        trigger.disabled = false;
    }
}

lookup('[data-resource-search]').addEventListener('input',
        debounce(filterResources));
lookup('[data-notice-retry]').addEventListener('click', loadNotices);
lookup('[data-resource-retry]').addEventListener('click', loadResources);

document.addEventListener('click', (event) => {
    const infoTab = event.target.closest('[data-info-tab]');
    if (infoTab) {
        activateInfoTab(infoTab);
        return;
    }
    const filter = event.target.closest('[data-filter-group]');
    if (!filter) {
        return;
    }
    activateFilterChip(filter);
    if (filter.dataset.filterGroup === 'notice') {
        filterNotices();
        return;
    }
    filterResources();
});

bindPageActions({
    [ACTIONS.DOWNLOAD]: downloadResource,
    [ACTIONS.UPLOAD]: uploadResource,
    [ACTIONS.NOTICE_ADD]: addNotice,
    [ACTIONS.NOTICE_OPEN]: openNotice,
});

loadNotices();
loadResources();
