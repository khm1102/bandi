import {ApiError, get, post, put} from '../common/api.js';
import {
    initializeDateTimeFields,
    readDateTimeValue,
    setDateTimeValue,
} from '../common/date-time-field.js';
import {all, bindPageActions, element, lookup, readValue} from '../common/dom.js';
import {closeModal, openModal} from '../common/modal.js';
import {currentUserRole} from '../common/session.js';
import {showToast} from '../common/toast.js';
import {activateFilterChip, badge, closeActionModal} from '../common/view.js';
import {normalizePage, readPageFromUrl, renderPagination, setUrlPage, writeUrl} from '../common/pagination.js';

const ACTIONS = Object.freeze({
    RETRY: 'activity-retry',
    CREATE_OPEN: 'activity-create-open',
    DETAIL_OPEN: 'activity-detail-open',
    EDIT_OPEN: 'activity-edit-open',
    SAVE: 'activity-save',
    SAVE_SUBMIT: 'activity-save-submit',
    SUBMIT: 'activity-submit',
    FILE_REPLACE: 'activity-file-replace',
});

const STATUS_META = Object.freeze({
    DRAFT: ['작성 중', 'neutral'],
    SUBMITTED: ['검수 대기', 'info'],
    TEAM_APPROVED: ['팀장 승인', 'info'],
    APPROVED: ['최종 승인', 'success'],
    REVISION_REQUESTED: ['보완 요청', 'warning'],
    ARCHIVED: ['보관', 'neutral'],
});

const FILE_ROLE_META = Object.freeze({
    EVIDENCE: ['인증', 'accent'],
    ADDITIONAL: ['추가', 'neutral'],
    DOCUMENT: ['HWPX', 'info'],
});

let activeStatus = 'ALL';
let records = [];
let teams = [];
let loginMember = null;
let currentDetail = null;
let editingDetail = null;
let pendingRecordId = null;
let pendingHasEvidence = false;
let replacementRecordFileId = null;

function errorMessage(error) {
    if (error instanceof ApiError && error.fieldErrors.length > 0) {
        return error.fieldErrors[0].reason;
    }
    return error?.message || '요청을 처리하지 못했습니다.';
}

function formatDateTime(value) {
    if (!value) {
        return '—';
    }
    return new Intl.DateTimeFormat('ko-KR', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,
    }).format(new Date(value));
}

function toDateTimeInput(value) {
    return value ? value.slice(0, 16) : '';
}

function localInputValue(date) {
    date.setMinutes(Math.round(date.getMinutes() / 5) * 5, 0, 0);
    const offset = date.getTimezoneOffset() * 60000;
    return new Date(date.getTime() - offset).toISOString().slice(0, 16);
}

function setText(selector, value, root = document) {
    const node = lookup(selector, root);
    if (node) {
        node.textContent = value ?? '';
    }
}

function setError(selector, message) {
    const node = lookup(selector);
    node.textContent = message || '';
    node.classList.toggle('hidden', !message);
}

function statusBadge(status) {
    const [label, tone] = STATUS_META[status] || [status || '미정', 'neutral'];
    return badge(label, tone);
}

function fileRoleBadge(role) {
    const [label, tone] = FILE_ROLE_META[role] || [role || '사진', 'neutral'];
    return badge(label, tone);
}

function setListState(title, message, retry = false) {
    const state = lookup('[data-activity-state]');
    state.classList.remove('hidden');
    lookup('[data-activity-list]').classList.add('hidden');
    setText('[data-activity-state-title]', title, state);
    setText('[data-activity-state-message]', message, state);
    lookup('[data-activity-retry]', state).classList.toggle('hidden', !retry);
}

function imageUrl(recordId, storedFileId) {
    return `/api/activity-management/${recordId}/files/${storedFileId}/download`;
}

function configureImage(image, fallback, src, alt) {
    if (!src) {
        image.classList.add('hidden');
        fallback.classList.remove('hidden');
        return;
    }
    image.alt = alt;
    image.addEventListener('load', () => {
        image.classList.remove('hidden');
        fallback.classList.add('hidden');
    }, {once: true});
    image.addEventListener('error', () => {
        image.classList.add('hidden');
        fallback.classList.remove('hidden');
        fallback.textContent = '사진을 불러오지 못했습니다';
    }, {once: true});
    image.src = src;
}

function createActivityCard(record) {
    const card = lookup('[data-activity-card-template]').content.firstElementChild.cloneNode(true);
    card.dataset.activityRecordId = record.activityRecordId;
    const badges = lookup('[data-activity-badges]', card);
    badges.appendChild(badge(record.teamName || '팀 미정', 'neutral'));
    badges.appendChild(statusBadge(record.status || 'APPROVED'));
    setText('[data-activity-title]', record.title, card);
    setText('[data-activity-meta]',
            `${formatDateTime(record.activityDttm)} · 참여 ${record.participantCount}명`, card);
    setText('[data-activity-author]',
            `${record.createdByName || '작성자 미상'} 작성 · ${formatDateTime(record.updatedDttm)} 수정`, card);
    const src = record.representativeStoredFileId
        ? imageUrl(record.activityRecordId, record.representativeStoredFileId) : null;
    configureImage(lookup('[data-activity-image]', card),
            lookup('[data-activity-image-fallback]', card), src,
            `${record.title} 인증 사진`);
    return card;
}

function renderRecords() {
    const list = lookup('[data-activity-list]');
    list.replaceChildren();
    if (records.length === 0) {
        setListState('표시할 활동 기록이 없습니다',
                '새 활동 기록을 작성하거나 다른 조건을 선택해 보세요.');
        return;
    }
    records.forEach((record) => list.appendChild(createActivityCard(record)));
    lookup('[data-activity-state]').classList.add('hidden');
    list.classList.remove('hidden');
    list.classList.add('grid');
}

async function loadRecords() {
    setListState('활동 기록을 불러오는 중입니다', '잠시만 기다려 주세요.');
    const params = new URLSearchParams(window.location.search);
    activeStatus = params.get('status') || 'ALL';
    const activeFilter = lookup(`[data-filter-group="activity-status"][data-filter-value="${activeStatus}"]`);
    if (activeFilter) {
        activateFilterChip(activeFilter);
    }
    const requestedPage = readPageFromUrl(params);
    const query = {page: requestedPage, pageSize: 20};
    if (activeStatus !== 'ALL') {
        query.status = activeStatus;
    }
    try {
        const response = await get('/api/activity-management/mine', query);
        const normalizedPage = normalizePage(response, requestedPage);
        if (normalizedPage !== requestedPage) {
            setUrlPage(params, normalizedPage);
            writeUrl(params, false);
            await loadRecords();
            return;
        }
        records = response.items;
        renderRecords();
        if (response.totalElements > 0) {
            renderPagination(lookup('[data-pagination]'), response, (page) => {
                setUrlPage(params, page);
                writeUrl(params, true);
                loadRecords();
            });
        } else {
            lookup('[data-pagination]').classList.add('hidden');
        }
    } catch (error) {
        setListState('활동 기록을 불러오지 못했습니다', errorMessage(error), true);
    }
}

function renderTeamOptions() {
    const filters = [document.getElementById('activityTeam')].filter(Boolean);
    filters.forEach((select) => {
        select.replaceChildren(element('option', '', '팀을 선택해 주세요'));
        select.firstElementChild.value = '';
        teams.forEach((team) => {
            const option = element('option', '', team.name);
            option.value = team.teamId;
            select.appendChild(option);
        });
    });
    if (currentUserRole !== 'admin') {
        const teamSelect = document.getElementById('activityTeam');
        teamSelect.value = loginMember.teamId || '';
        teamSelect.disabled = true;
    }
}

async function initializeReferences() {
    [loginMember, teams] = await Promise.all([
        get('/api/members/me'),
        get('/api/members/reference/teams'),
    ]);
    renderTeamOptions();
}

function resetActivityForm() {
    editingDetail = null;
    pendingRecordId = null;
    pendingHasEvidence = false;
    document.getElementById('activityTeam').disabled = currentUserRole !== 'admin';
    document.getElementById('activityTeam').value = currentUserRole === 'admin'
        ? '' : loginMember?.teamId || '';
    document.getElementById('activityTitle').value = '';
    setDateTimeValue('activityDttm', localInputValue(new Date()));
    document.getElementById('activityParticipantCount').value = '1';
    document.getElementById('activityBody').value = '';
    document.getElementById('activityEvidence').value = '';
    document.getElementById('activityAdditional').value = '';
    document.getElementById('activityChangeReason').value = '';
    lookup('[data-activity-change-reason-field]').classList.add('hidden');
    setText('#activityModalTitle', '활동 기록 작성');
    setError('[data-activity-form-error]', '');
}

function openCreateModal(trigger) {
    if (!loginMember) {
        showToast('로그인 멤버 정보를 불러오는 중입니다. 잠시 후 다시 시도해 주세요.');
        return;
    }
    resetActivityForm();
    closeModal(document.getElementById('activityChoiceModal'));
    openModal('activityModal', trigger);
}

function populateEditForm(detail) {
    editingDetail = detail;
    pendingRecordId = detail.activityRecordId;
    pendingHasEvidence = detail.currentFiles.some((file) =>
        file.fileRole === 'EVIDENCE');
    document.getElementById('activityTeam').disabled = true;
    document.getElementById('activityTeam').value = detail.teamId;
    document.getElementById('activityTitle').value = detail.title;
    setDateTimeValue('activityDttm', toDateTimeInput(detail.activityDttm));
    document.getElementById('activityParticipantCount').value = detail.participantCount;
    document.getElementById('activityBody').value = detail.body;
    document.getElementById('activityEvidence').value = '';
    document.getElementById('activityAdditional').value = '';
    document.getElementById('activityChangeReason').value = '';
    lookup('[data-activity-change-reason-field]').classList.toggle('hidden',
            detail.status !== 'REVISION_REQUESTED');
    setText('#activityModalTitle', detail.status === 'REVISION_REQUESTED'
        ? '활동 기록 보완·재제출' : '활동 기록 수정');
    setError('[data-activity-form-error]', '');
}

function openEditModal(trigger) {
    if (!currentDetail) {
        return;
    }
    if (currentDetail.reportDocument) {
        window.location.href = `/activity-documents?activityRecordId=`
                + `${currentDetail.activityRecordId}`;
        return;
    }
    populateEditForm(currentDetail);
    closeModal(document.getElementById('activityDetailModal'));
    openModal('activityModal', trigger);
}

function activityPayload() {
    return {
        teamId: Number(readValue('activityTeam')),
        activityDttm: readDateTimeValue('activityDttm')
            ? `${readDateTimeValue('activityDttm')}:00` : null,
        title: readValue('activityTitle'),
        body: readValue('activityBody'),
        participantCount: Number(readValue('activityParticipantCount')),
    };
}

function validateActivityForm(payload, submitAfterSave) {
    if (!payload.teamId || !payload.activityDttm || !payload.title
            || !payload.body || payload.participantCount < 1) {
        return '담당 팀과 필수 활동 정보를 모두 입력해 주세요.';
    }
    if (Number(payload.activityDttm.slice(14, 16)) % 5 !== 0) {
        return '활동 일시는 5분 단위로 입력해 주세요.';
    }
    const evidence = document.getElementById('activityEvidence').files[0];
    const files = [evidence, ...document.getElementById('activityAdditional').files]
            .filter(Boolean);
    if (files.some((file) => !file.type.startsWith('image/'))) {
        return '활동 기록에는 이미지 파일만 첨부할 수 있습니다.';
    }
    return '';
}

async function uploadImage(recordId, file, fileRole) {
    const formData = new FormData();
    formData.append('file', file);
    const uploaded = await post('/api/files/private', formData, {
        query: {domain: 'activity'},
    });
    await post(`/api/activity-management/${recordId}/files`, {
        storedFileId: uploaded.id,
        fileRole,
    });
}

async function uploadSelectedFiles(recordId) {
    const evidence = document.getElementById('activityEvidence').files[0];
    if (evidence) {
        await uploadImage(recordId, evidence, 'EVIDENCE');
        pendingHasEvidence = true;
        document.getElementById('activityEvidence').value = '';
    }
    const additionalFiles = Array.from(
            document.getElementById('activityAdditional').files);
    for (const file of additionalFiles) {
        await uploadImage(recordId, file, 'ADDITIONAL');
    }
    document.getElementById('activityAdditional').value = '';
}

async function saveActivity(trigger, submitAfterSave) {
    const payload = activityPayload();
    const validationMessage = validateActivityForm(payload, submitAfterSave);
    setError('[data-activity-form-error]', validationMessage);
    if (validationMessage) {
        return;
    }
    trigger.disabled = true;
    try {
        if (editingDetail) {
            const updatePayload = {...payload};
            delete updatePayload.teamId;
            await put(`/api/activity-management/${pendingRecordId}`, updatePayload);
        } else if (!pendingRecordId) {
            const created = await post('/api/activity-management', payload);
            pendingRecordId = created.activityRecordId;
        }
        await uploadSelectedFiles(pendingRecordId);
        if (submitAfterSave) {
            await post(`/api/activity-management/${pendingRecordId}/submit`, {
                changeReason: readValue('activityChangeReason') || null,
            });
        }
        closeActionModal(trigger);
        showToast(submitAfterSave ? '활동 기록을 제출했습니다.' : '활동 기록 초안을 저장했습니다.');
        editingDetail = null;
        pendingRecordId = null;
        pendingHasEvidence = false;
        await loadRecords();
    } catch (error) {
        const prefix = pendingRecordId && !editingDetail
            ? '초안은 저장됐습니다. 이어서 다시 시도해 주세요. ' : '';
        setError('[data-activity-form-error]', `${prefix}${errorMessage(error)}`);
        if (pendingRecordId) {
            loadRecords();
        }
    } finally {
        trigger.disabled = false;
    }
}

function renderDetailFiles(detail, manageable) {
    const files = detail.currentFiles || detail.files || [];
    const container = lookup('[data-activity-detail-files]');
    container.replaceChildren();
    setText('[data-activity-file-count]', `${files.length}개`);
    lookup('[data-activity-file-empty]').classList.toggle('hidden', files.length > 0);
    files.forEach((file) => {
        const figure = lookup('[data-activity-file-template]').content.firstElementChild.cloneNode(true);
        figure.dataset.activityRecordFileId = file.activityRecordFileId;
        const fileUrl = imageUrl(detail.activityRecordId, file.storedFileId, manageable);
        const image = lookup('[data-activity-file-image]', figure);
        if (file.fileRole === 'DOCUMENT') {
            const media = image.parentElement;
            const link = element('a',
                    'flex min-h-40 w-full items-center justify-center px-4 text-center text-sm font-extrabold text-info underline underline-offset-4',
                    'HWPX 활동 내역서 다운로드');
            link.href = fileUrl;
            media.replaceChildren(link);
        } else {
            image.src = fileUrl;
            image.alt = `${detail.title} ${FILE_ROLE_META[file.fileRole]?.[0] || '활동'} 사진`;
            image.addEventListener('error', () => {
                image.replaceWith(element('span',
                        'px-4 text-center text-xs font-bold text-muted-foreground',
                        '사진을 불러오지 못했습니다'));
            }, {once: true});
        }
        lookup('[data-activity-file-role]', figure).appendChild(fileRoleBadge(file.fileRole));
        setText('[data-activity-file-name]', file.originalName, figure);
        setText('[data-activity-file-meta]',
                `${file.uploadedByName || '업로더 미상'} · ${formatDateTime(file.uploadedDttm)}`, figure);
        const replaceAction = lookup('[data-file-replace-action]', figure);
        replaceAction.classList.toggle('hidden',
                file.fileRole === 'DOCUMENT' || !manageable
                || !['DRAFT', 'REVISION_REQUESTED'].includes(detail.status));
        container.appendChild(figure);
    });
}

function renderReviewHistory(detail) {
    const histories = detail.reviewHistories || [];
    const section = lookup('[data-activity-review-section]');
    section.classList.toggle('hidden', histories.length === 0);
    const container = lookup('[data-activity-review-history]');
    container.replaceChildren();
    histories.forEach((history) => {
        const item = element('div', 'rounded-md border bg-secondary/60 px-3 py-2.5');
        const row = element('div', 'flex flex-wrap items-center gap-2');
        row.append(statusBadge(history.previousStatus),
                element('span', 'text-xs text-muted-foreground', '→'),
                statusBadge(history.newStatus));
        item.append(row, element('p', 'mt-2 text-xs', history.comment || '상태 변경'),
                element('p', 'mt-1 text-xs text-muted-foreground',
                        `${history.reviewedByName || '처리자 미상'} · ${formatDateTime(history.reviewedDttm)}`));
        container.appendChild(item);
    });
}

function renderRevisions(detail) {
    const revisions = detail.revisions || [];
    const section = lookup('[data-activity-revision-section]');
    section.classList.toggle('hidden', revisions.length === 0);
    const container = lookup('[data-activity-revisions]');
    container.replaceChildren();
    revisions.forEach((revision) => {
        const item = element('div', 'rounded-md border px-3 py-2.5');
        item.append(
                element('b', 'text-xs', `v${revision.revisionNo} · ${revision.title}`),
                element('p', 'mt-1 text-xs text-muted-foreground',
                        `${revision.changedByName || '작성자 미상'} · ${formatDateTime(revision.changedDttm)}`),
                element('p', 'mt-2 text-xs', revision.changeReason || '최초 제출'));
        container.appendChild(item);
    });
}

function showDetailAction(action, visible) {
    const node = lookup(`[data-detail-action="${action}"]`);
    if (node) {
        node.classList.toggle('hidden', !visible);
    }
}

function renderDetailActions(detail, manageable) {
    const editable = manageable
        && ['DRAFT', 'REVISION_REQUESTED'].includes(detail.status);
    showDetailAction('edit', editable);
    showDetailAction('submit', manageable && detail.status === 'DRAFT');
    const editButton = lookup('[data-detail-action="edit"] [data-page-action]');
    if (editButton) {
        editButton.textContent = detail.reportDocument
            ? '활동 내역서 수정' : '수정·사진 추가';
    }
}

function renderDetail(detail, manageable) {
    currentDetail = {...detail, manageable};
    const badges = lookup('[data-activity-detail-badges]');
    badges.replaceChildren(badge(detail.teamName || '팀 미정', 'neutral'),
            statusBadge(detail.status || 'APPROVED'));
    setText('[data-activity-detail-title]', detail.title);
    setText('[data-activity-detail-meta]',
            `${formatDateTime(detail.activityDttm)} · 참여 ${detail.participantCount}명 · ${detail.createdByName || '작성자 미상'} 작성`);
    setText('[data-activity-detail-body]', detail.body);
    renderDetailFiles(detail, manageable);
    renderReviewHistory(detail);
    renderRevisions(detail);
    renderDetailActions(detail, manageable);
    setError('[data-activity-detail-error]', '');
}

async function openDetail(trigger) {
    const recordId = Number(trigger.closest('[data-activity-card]')?.dataset.activityRecordId);
    if (!recordId) {
        return;
    }
    trigger.disabled = true;
    try {
        const detail = await get(`/api/activity-management/${recordId}`);
        renderDetail(detail, true);
        openModal('activityDetailModal', trigger);
    } catch (error) {
        showToast(errorMessage(error));
    } finally {
        trigger.disabled = false;
    }
}

async function submitCurrentDetail(trigger) {
    if (!currentDetail) {
        return;
    }
    trigger.disabled = true;
    try {
        await post(`/api/activity-management/${currentDetail.activityRecordId}/submit`, {
            changeReason: null,
        });
        closeActionModal(trigger);
        showToast('활동 기록을 제출했습니다.');
        await loadRecords();
    } catch (error) {
        setError('[data-activity-detail-error]', errorMessage(error));
    } finally {
        trigger.disabled = false;
    }
}

function selectReplacementFile(trigger) {
    replacementRecordFileId = Number(trigger.closest('[data-activity-file]')
            ?.dataset.activityRecordFileId);
    document.getElementById('activityReplacementFile').click();
}

async function replaceActivityFile(event) {
    const file = event.target.files[0];
    if (!file || !replacementRecordFileId) {
        return;
    }
    if (!file.type.startsWith('image/')) {
        setError('[data-activity-detail-error]', '이미지 파일만 교체할 수 있습니다.');
        event.target.value = '';
        return;
    }
    setError('[data-activity-detail-error]', '');
    try {
        const formData = new FormData();
        formData.append('file', file);
        const uploaded = await post('/api/files/private', formData, {
            query: {domain: 'activity'},
        });
        await put(`/api/activity-management/files/${replacementRecordFileId}`, {
            newStoredFileId: uploaded.id,
        });
        const detail = await get(`/api/activity-management/${currentDetail.activityRecordId}`);
        renderDetail(detail, true);
        showToast('증빙 사진을 교체했습니다.');
    } catch (error) {
        setError('[data-activity-detail-error]', errorMessage(error));
    } finally {
        event.target.value = '';
        replacementRecordFileId = null;
    }
}

all('[data-filter-group="activity-status"]').forEach((button) => {
    button.addEventListener('click', () => {
        activateFilterChip(button);
        activeStatus = button.dataset.filterValue;
        const params = new URLSearchParams(window.location.search);
        if (activeStatus === 'ALL') {
            params.delete('status');
        } else {
            params.set('status', activeStatus);
        }
        setUrlPage(params, 0);
        writeUrl(params, false);
        loadRecords();
    });
});

document.getElementById('activityReplacementFile').addEventListener('change', replaceActivityFile);

bindPageActions({
    [ACTIONS.RETRY]: loadRecords,
    [ACTIONS.CREATE_OPEN]: openCreateModal,
    [ACTIONS.DETAIL_OPEN]: openDetail,
    [ACTIONS.EDIT_OPEN]: openEditModal,
    [ACTIONS.SAVE]: (trigger) => saveActivity(trigger, false),
    [ACTIONS.SAVE_SUBMIT]: (trigger) => saveActivity(trigger, true),
    [ACTIONS.SUBMIT]: submitCurrentDetail,
    [ACTIONS.FILE_REPLACE]: selectReplacementFile,
});

initializeDateTimeFields();
initializeReferences().catch((error) => {
    showToast(`기준 정보를 불러오지 못했습니다. ${errorMessage(error)}`);
});
window.addEventListener('popstate', loadRecords);
loadRecords();
