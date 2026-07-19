import {ApiError, get, post, put} from '../common/api.js';
import {all, bindPageActions, debounce, element, lookup, readValue} from '../common/dom.js';
import {openModal} from '../common/modal.js';
import {currentUserRole} from '../common/session.js';
import {showToast} from '../common/toast.js';
import {activateFilterChip, badge, closeActionModal} from '../common/view.js';

const ACTIONS = Object.freeze({
    DOWNLOAD: 'resource-download',
    RESOURCE_CREATE: 'resource-create-open',
    UPLOAD: 'resource-upload',
    NOTICE_CREATE: 'notice-create-open',
    NOTICE_ADD: 'notice-add',
    NOTICE_OPEN: 'notice-open',
    NOTICE_EDIT: 'notice-edit',
    NOTICE_READS: 'notice-read-statuses',
    NOTICE_CLOSE: 'notice-close',
    NOTICE_ARCHIVE: 'notice-archive',
    RESOURCE_EDIT: 'resource-edit',
    RESOURCE_HISTORY: 'resource-history',
    RESOURCE_ARCHIVE: 'resource-archive',
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
let teams = [];
let editingNotice = null;
let currentNoticeDetail = null;
let editingResource = null;

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

function actionButton(label, action, id, tone = 'outline') {
    const style = tone === 'danger'
            ? 'border-destructive/30 text-destructive hover:bg-destructive-soft'
            : 'hover:bg-secondary';
    const button = element('button',
            `min-h-11 rounded-md border bg-card px-3 text-xs font-bold transition-colors ${style}`,
            label);
    button.type = 'button';
    button.dataset.pageAction = action;
    if (id !== undefined) button.dataset.targetId = String(id);
    return button;
}

function canManage(item) {
    if (currentUserRole === 'admin') return true;
    return currentUserRole === 'leader' && item.targetScope === 'TEAM'
            && Number(item.teamId) === Number(loginMember?.teamId);
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
    const actions = lookup('[data-resource-actions]', row);
    actions.appendChild(actionButton('다운로드', ACTIONS.DOWNLOAD));
    if (canManage(resource)) {
        actions.append(actionButton('수정', ACTIONS.RESOURCE_EDIT,
                resource.resourceId), actionButton('이력', ACTIONS.RESOURCE_HISTORY,
                resource.resourceId));
        const archive = actionButton('보관', ACTIONS.RESOURCE_ARCHIVE,
                resource.resourceId, 'danger');
        archive.dataset.confirm = '이 자료를 보관할까요? 보관 후 일반 목록에서 숨겨집니다.';
        archive.dataset.confirmAction = '자료 보관';
        actions.appendChild(archive);
    }
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

function targetTeamId(targetScope, member, selectId) {
    if (targetScope !== 'TEAM') return null;
    return currentUserRole === 'admin'
            ? Number(readValue(selectId)) || null : member.teamId;
}

function updateTargetField(targetId, fieldSelector) {
    lookup(fieldSelector).classList.toggle('hidden', readValue(targetId) !== 'TEAM'
            || currentUserRole !== 'admin');
}

function fillTeamOptions() {
    for (const id of ['ntTeam', 'upTeam']) {
        const select = document.getElementById(id);
        select.replaceChildren();
        teams.forEach((team) => {
            const option = element('option', '', team.name);
            option.value = String(team.teamId);
            select.appendChild(option);
        });
    }
}

function resetNoticeForm() {
    document.getElementById('ntTarget').value = currentUserRole === 'admin'
            ? 'ALL' : 'TEAM';
    document.getElementById('ntTitle').value = '';
    document.getElementById('ntBody').value = '';
    document.getElementById('ntImportant').checked = false;
    updateTargetField('ntTarget', '[data-notice-team-field]');
    setInlineError('[data-notice-form-error]', '');
}

function openNoticeForm(trigger) {
    editingNotice = null;
    resetNoticeForm();
    document.getElementById('noticeModalTitle').textContent = '짧은 공지 작성';
    lookup('[data-notice-submit-label]').textContent = '공지 게시';
    openModal('noticeModal', trigger);
}

async function addNotice(trigger) {
    trigger.disabled = true;
    setInlineError('[data-notice-form-error]', '');
    try {
        const member = await lookupLoginMember();
        const targetScope = readValue('ntTarget');
        const body = {
            targetScope,
            teamId: targetTeamId(targetScope, member, 'ntTeam'),
            title: readValue('ntTitle'),
            body: readValue('ntBody'),
            important: document.getElementById('ntImportant').checked,
            attachmentFileIds: editingNotice?.attachments.map((file) =>
                file.storedFileId) || [],
        };
        if (editingNotice) {
            await put(`/api/internal-notice-management/${editingNotice.internalNoticeId}`,
                    body);
        } else {
            const created = await post('/api/internal-notice-management', body);
            await post(`/api/internal-notice-management/${created.internalNoticeId}/publish`, {
                publishStartDttm: null,
                publishEndDttm: null,
            });
        }
        closeActionModal(trigger);
        resetNoticeForm();
        showToast(editingNotice ? '공지를 수정했습니다.' : '공지를 게시했습니다.');
        editingNotice = null;
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
    document.getElementById('upTarget').value = currentUserRole === 'admin'
            ? 'ALL' : 'TEAM';
    document.getElementById('upDescription').value = '';
    document.getElementById('upFile').value = '';
    document.getElementById('upPinned').checked = false;
    updateTargetField('upTarget', '[data-resource-team-field]');
    setInlineError('[data-resource-form-error]', '');
}

function openResourceForm(trigger) {
    editingResource = null;
    resetResourceForm();
    document.getElementById('upFile').required = true;
    document.getElementById('uploadModalTitle').textContent = '자료 업로드';
    lookup('[data-resource-submit-label]').textContent = '업로드';
    openModal('uploadModal', trigger);
}

async function uploadResource(trigger) {
    const file = document.getElementById('upFile').files[0];
    if (!file && !editingResource) {
        setInlineError('[data-resource-form-error]', '파일을 선택해 주세요.');
        return;
    }
    trigger.disabled = true;
    setInlineError('[data-resource-form-error]', '');
    try {
        const member = await lookupLoginMember();
        const targetScope = readValue('upTarget');
        const metadata = {
            targetScope,
            teamId: targetTeamId(targetScope, member, 'upTeam'),
            categoryCode: readValue('upCat'),
            title: readValue('upName'),
            description: readValue('upDescription'),
            pinned: document.getElementById('upPinned').checked,
        };
        let uploaded = null;
        if (file) {
            const formData = new FormData();
            formData.append('file', file);
            uploaded = await post('/api/files/private', formData, {
                query: {domain: 'resource'},
            });
        }
        if (editingResource) {
            await put(`/api/resource-management/${editingResource.resourceId}`,
                    metadata);
            if (uploaded) {
                await post(`/api/resource-management/${editingResource.resourceId}/revisions`, {
                    storedFileIds: [uploaded.id],
                });
            }
        } else {
            const created = await post('/api/resource-management', {
                ...metadata, storedFileIds: [uploaded.id],
            });
            await post(`/api/resource-management/${created.resourceId}/publish`);
        }
        closeActionModal(trigger);
        resetResourceForm();
        showToast(editingResource ? '자료를 수정했습니다.'
            : '자료를 업로드하고 게시했습니다.');
        editingResource = null;
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
        currentNoticeDetail = detail;
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
        const managementActions = lookup('[data-notice-management-actions]');
        managementActions.classList.toggle('hidden', !canManage(detail));
        managementActions.classList.toggle('flex', canManage(detail));
        openModal('noticeDetailModal', trigger);
    } catch (error) {
        showToast(errorMessage(error));
    } finally {
        trigger.disabled = false;
    }
}

async function editNotice(trigger) {
    if (!currentNoticeDetail) return;
    trigger.disabled = true;
    try {
        editingNotice = await get(`/api/internal-notice-management/${currentNoticeDetail.internalNoticeId}`);
        document.getElementById('ntTarget').value = editingNotice.targetScope;
        if (editingNotice.teamId) document.getElementById('ntTeam').value = String(editingNotice.teamId);
        updateTargetField('ntTarget', '[data-notice-team-field]');
        document.getElementById('ntTitle').value = editingNotice.title;
        document.getElementById('ntBody').value = editingNotice.body;
        document.getElementById('ntImportant').checked = editingNotice.important;
        document.getElementById('noticeModalTitle').textContent = '공지 수정';
        lookup('[data-notice-submit-label]').textContent = '수정 저장';
        closeActionModal(trigger);
        openModal('noticeModal', trigger);
    } catch (error) {
        showToast(errorMessage(error));
    } finally {
        trigger.disabled = false;
    }
}

async function showNoticeReads(trigger) {
    if (!currentNoticeDetail) return;
    await withButtonBusy(trigger, async () => {
        const statuses = await get(`/api/internal-notice-management/${currentNoticeDetail.internalNoticeId}/read-statuses`);
        const list = lookup('[data-notice-read-list]');
        list.replaceChildren();
        if (!statuses.length) {
            list.appendChild(element('p', 'py-8 text-center text-sm text-muted-foreground',
                    '확인 대상 멤버가 없습니다.'));
        }
        statuses.forEach((status) => {
            const row = element('div', 'flex items-center gap-2 border-b py-3 last:border-b-0');
            row.append(element('strong', 'min-w-0 flex-1 text-sm',
                    `${status.memberName} · ${status.studentNo}`),
                    badge(status.read ? '확인' : '미확인', status.read ? 'success' : 'warning'));
            if (status.firstReadDttm) row.appendChild(element('span',
                    'text-xs text-muted-foreground', formatDate(status.firstReadDttm)));
            list.appendChild(row);
        });
        closeActionModal(trigger);
        openModal('noticeReadModal', trigger);
    });
}

async function changeNoticeState(trigger, action) {
    if (!currentNoticeDetail) return;
    await withButtonBusy(trigger, async () => {
        await post(`/api/internal-notice-management/${currentNoticeDetail.internalNoticeId}/${action}`);
        closeActionModal(trigger);
        currentNoticeDetail = null;
        await loadNotices();
        showToast(action === 'close' ? '공지 게시를 종료했습니다.' : '공지를 보관했습니다.');
    });
}

async function withButtonBusy(trigger, task) {
    trigger.disabled = true;
    try {
        await task();
    } catch (error) {
        showToast(errorMessage(error));
    } finally {
        trigger.disabled = false;
    }
}

async function editResource(trigger) {
    await withButtonBusy(trigger, async () => {
        editingResource = await get(`/api/resource-management/${trigger.dataset.targetId}`);
        document.getElementById('upName').value = editingResource.title;
        document.getElementById('upCat').value = editingResource.categoryCode;
        document.getElementById('upTarget').value = editingResource.targetScope;
        if (editingResource.teamId) document.getElementById('upTeam').value = String(editingResource.teamId);
        updateTargetField('upTarget', '[data-resource-team-field]');
        document.getElementById('upDescription').value = editingResource.description;
        document.getElementById('upPinned').checked = editingResource.pinned;
        document.getElementById('upFile').value = '';
        document.getElementById('upFile').required = false;
        document.getElementById('uploadModalTitle').textContent = '자료 수정·새 리비전';
        lookup('[data-resource-submit-label]').textContent = '변경 저장';
        openModal('uploadModal', trigger);
    });
}

async function showResourceHistory(trigger) {
    await withButtonBusy(trigger, async () => {
        const detail = await get(`/api/resource-management/${trigger.dataset.targetId}`);
        lookup('[data-resource-history-title]').textContent = detail.title;
        const list = lookup('[data-resource-history-list]');
        list.replaceChildren();
        if (!detail.revisions.length) {
            list.appendChild(element('p', 'py-8 text-center text-sm text-muted-foreground',
                    '등록된 파일 리비전이 없습니다.'));
        }
        detail.revisions.slice().reverse().forEach((revision) => {
            const section = element('section', 'border-b py-3 last:border-b-0');
            section.appendChild(element('h4', 'text-sm font-black', `v${revision.revisionNo}`));
            revision.files.forEach((file) => section.appendChild(element('p',
                    'mt-1 text-xs text-muted-foreground',
                    `${file.originalName} · ${file.uploadedByName || '업로더 미상'} · ${formatDate(file.uploadedDttm)}`)));
            list.appendChild(section);
        });
        openModal('resourceHistoryModal', trigger);
    });
}

async function archiveResource(trigger) {
    await withButtonBusy(trigger, async () => {
        await post(`/api/resource-management/${trigger.dataset.targetId}/archive`);
        await loadResources();
        showToast('자료를 보관했습니다.');
    });
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
document.getElementById('ntTarget').addEventListener('change', () =>
    updateTargetField('ntTarget', '[data-notice-team-field]'));
document.getElementById('upTarget').addEventListener('change', () =>
    updateTargetField('upTarget', '[data-resource-team-field]'));

bindPageActions({
    [ACTIONS.DOWNLOAD]: downloadResource,
    [ACTIONS.RESOURCE_CREATE]: openResourceForm,
    [ACTIONS.UPLOAD]: uploadResource,
    [ACTIONS.RESOURCE_EDIT]: editResource,
    [ACTIONS.RESOURCE_HISTORY]: showResourceHistory,
    [ACTIONS.RESOURCE_ARCHIVE]: archiveResource,
    [ACTIONS.NOTICE_CREATE]: openNoticeForm,
    [ACTIONS.NOTICE_ADD]: addNotice,
    [ACTIONS.NOTICE_OPEN]: openNotice,
    [ACTIONS.NOTICE_EDIT]: editNotice,
    [ACTIONS.NOTICE_READS]: showNoticeReads,
    [ACTIONS.NOTICE_CLOSE]: (trigger) => changeNoticeState(trigger, 'close'),
    [ACTIONS.NOTICE_ARCHIVE]: (trigger) => changeNoticeState(trigger, 'archive'),
});

async function initialize() {
    [loginMember, teams] = await Promise.all([
        lookupLoginMember(), get('/api/members/reference/teams'),
    ]);
    fillTeamOptions();
    await Promise.all([loadNotices(), loadResources()]);
}

initialize().catch((error) => showToast(errorMessage(error)));
