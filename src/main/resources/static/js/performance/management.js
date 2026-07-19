import {del, get, patch, post, put} from '../common/api.js';
import {bindPageActions, element, lookup, readValue} from '../common/dom.js';
import {openModal} from '../common/modal.js';
import {showToast} from '../common/toast.js';
import {badge, closeActionModal} from '../common/view.js';

const ACTIONS = Object.freeze({
    PROJECT_CREATE: 'performance-project-create-open', PROJECT_EDIT: 'performance-project-edit-open',
    PROJECT_SAVE: 'performance-project-save', ROUND_CREATE: 'performance-round-create-open',
    ROUND_EDIT: 'performance-round-edit-open', ROUND_SAVE: 'performance-round-save',
    ACCESS_OPEN: 'performance-accessibility-open', ACCESS_SAVE: 'performance-accessibility-save',
    ACCESS_EDIT: 'performance-accessibility-edit',
    ACCESS_DELETE: 'performance-accessibility-delete', STATUS_OPEN: 'performance-status-open',
    STATUS_SAVE: 'performance-status-save', PUBLIC_SAVE: 'performance-public-save',
    PUBLIC_STATUS: 'performance-public-status-open', GUIDE_SAVE: 'performance-guide-save',
    NOTICE_OPEN: 'performance-notice-open', NOTICE_SAVE: 'performance-notice-save',
    NOTICE_DELETE: 'performance-notice-delete',
});
const PROJECT_STATUS = {
    PLANNING: '기획', PRODUCING: '제작', RESERVATION_OPEN: '신청 중',
    PERFORMING: '공연 중', ENDED: '종료', CANCELLED: '취소', ARCHIVED: '보관',
};
const PROJECT_NEXT = {
    PLANNING: ['PRODUCING', 'CANCELLED'], PRODUCING: ['RESERVATION_OPEN', 'CANCELLED'],
    RESERVATION_OPEN: ['PERFORMING', 'CANCELLED'], PERFORMING: ['ENDED', 'CANCELLED'],
    ENDED: ['ARCHIVED'], CANCELLED: [], ARCHIVED: [],
};
const ROUND_STATUS = {
    SCHEDULED: '예정', RESERVATION_OPEN: '신청 중', RESERVATION_CLOSED: '신청 마감',
    ENTRY_OPEN: '입장 중', ENDED: '종료', CANCELLED: '취소',
};
const PAGE_STATUS = {
    DRAFT: '초안', SCHEDULED: '공개 예정', PUBLISHED: '공개',
    ENDED: '공연 종료', CANCELLED: '취소', ARCHIVED: '보관',
};
const PAGE_NEXT = {
    DRAFT: ['SCHEDULED', 'PUBLISHED', 'CANCELLED'],
    SCHEDULED: ['DRAFT', 'PUBLISHED', 'CANCELLED'],
    PUBLISHED: ['ENDED', 'CANCELLED'], ENDED: ['ARCHIVED'],
    CANCELLED: ['ARCHIVED'], ARCHIVED: [],
};

let projects = [];
let rounds = [];
let pages = [];
let linkedNotices = [];
let editingProject = null;
let editingRound = null;
let accessibilityRound = null;
let accessibilityItems = [];
let editingAccessibility = null;
let statusTarget = null;

function project() {
    const id = Number(readValue('performanceProjectSelect'));
    return projects.find((item) => item.performanceProjectId === id);
}

function publicPage() {
    return pages.find((item) => item.performanceProjectId === project()?.performanceProjectId) || null;
}

function hasProject() {
    return Boolean(project());
}

function appendOption(select, value, label) {
    const option = element('option', '', label);
    option.value = String(value);
    select.appendChild(option);
}

function formatDateTime(value) {
    return value ? value.replace('T', ' ').slice(0, 16) : '-';
}

function toInput(value) {
    return value ? value.slice(0, 16) : '';
}

function isPublicPageVisible(page) {
    if (['ENDED', 'CANCELLED', 'ARCHIVED'].includes(page.status)) return true;
    const now = new Date();
    const startsAt = page.publishStartDttm ? new Date(page.publishStartDttm) : null;
    const endsAt = page.publishEndDttm ? new Date(page.publishEndDttm) : null;
    if ((startsAt && startsAt > now) || (endsAt && endsAt <= now)) return false;
    return page.status === 'PUBLISHED'
            || (page.status === 'SCHEDULED' && Boolean(startsAt));
}

function actionButton(label, action, id, variant = 'outline') {
    const tone = variant === 'danger'
            ? 'border-destructive/30 text-destructive hover:bg-destructive-soft'
            : variant === 'primary'
                ? 'border-primary bg-primary text-primary-foreground hover:bg-primary-strong hover:text-white'
                : 'hover:bg-secondary';
    const button = element('button', `min-h-11 rounded-md border bg-card px-3 text-xs font-bold transition-colors ${tone}`, label);
    button.type = 'button';
    button.dataset.pageAction = action;
    if (id !== undefined) button.dataset.targetId = String(id);
    return button;
}

function statusTone(status) {
    if (['PUBLISHED', 'COMPLETED', 'ENDED'].includes(status)) return 'success';
    if (['CANCELLED', 'BLOCKED'].includes(status)) return 'danger';
    if (['SCHEDULED', 'RESERVATION_OPEN', 'ENTRY_OPEN'].includes(status)) return 'warning';
    return 'neutral';
}

function renderProjectSelect() {
    const select = document.getElementById('performanceProjectSelect');
    const previous = select.value;
    select.replaceChildren();
    projects.filter((item) => item.status !== 'CANCELLED').forEach((item) =>
        appendOption(select, item.performanceProjectId,
                `${item.academicYear} ${item.termCode} · ${item.title}`));
    if (previous && Array.from(select.options).some((option) => option.value === previous)) {
        select.value = previous;
    }
}

function renderProject() {
    const selected = project();
    const summary = lookup('[data-performance-project-summary]');
    const actions = lookup('[data-performance-project-actions]');
    actions.replaceChildren();
    lookup(`[data-page-action="${ACTIONS.ROUND_CREATE}"]`).disabled = !selected;
    lookup(`[data-page-action="${ACTIONS.NOTICE_OPEN}"]`).disabled = !selected;
    lookup(`[data-page-action="${ACTIONS.PUBLIC_SAVE}"]`).disabled = !selected;
    lookup(`[data-page-action="${ACTIONS.GUIDE_SAVE}"]`).disabled = !selected;
    lookup(`[data-page-action="${ACTIONS.PUBLIC_STATUS}"]`).disabled =
            !publicPage() || PAGE_NEXT[publicPage().status].length === 0;
    if (!selected) {
        summary.textContent = '등록된 공연 프로젝트가 없습니다.';
        return;
    }
    summary.replaceChildren(
            badge(PROJECT_STATUS[selected.status] || selected.status, statusTone(selected.status)),
            element('strong', 'ml-2', selected.title),
            element('span', 'ml-2 text-xs', `${selected.productionStartDate} — ${selected.productionEndDate} · ${selected.place}`));
    if (!['CANCELLED', 'ARCHIVED'].includes(selected.status)) {
        actions.appendChild(actionButton('프로젝트 수정', ACTIONS.PROJECT_EDIT));
    }
    if (PROJECT_NEXT[selected.status]?.length) {
        const status = actionButton('상태 변경', ACTIONS.STATUS_OPEN);
        status.dataset.statusType = 'project';
        actions.appendChild(status);
    }
}

function renderRounds() {
    const region = lookup('[data-performance-rounds]');
    region.replaceChildren();
    if (!rounds.length) {
        region.appendChild(element('p', 'px-5 py-10 text-center text-sm text-muted-foreground', '등록된 회차가 없습니다.'));
        return;
    }
    rounds.forEach((round) => {
        const row = element('article', 'px-5 py-4');
        const head = element('div', 'flex flex-wrap items-center gap-2');
        head.append(element('h3', 'text-sm font-black', `${round.roundNo}회차`),
                badge(ROUND_STATUS[round.status] || round.status, statusTone(round.status)));
        const actions = element('div', 'ml-auto flex flex-wrap gap-1');
        const edit = actionButton('수정', ACTIONS.ROUND_EDIT, round.performanceRoundId);
        const status = actionButton('상태', ACTIONS.STATUS_OPEN, round.performanceRoundId);
        status.dataset.statusType = 'round';
        const access = actionButton('접근성', ACTIONS.ACCESS_OPEN, round.performanceRoundId);
        actions.append(edit, status, access);
        head.appendChild(actions);
        row.append(head, element('p', 'mt-2 text-xs text-muted-foreground',
                `공연 ${formatDateTime(round.startDttm)} · 입장 ${formatDateTime(round.entryStartDttm)}`),
                element('p', 'mt-1 text-xs text-muted-foreground',
                        `신청 ${formatDateTime(round.reservationOpenDttm)} — ${formatDateTime(round.reservationCloseDttm)}`));
        region.appendChild(row);
    });
}

function clearPublicForm() {
    lookup('[data-public-page-form]').reset();
    document.getElementById('publicFee').value = '0';
    lookup('[data-public-page-status]').replaceChildren(badge('페이지 없음', 'neutral'));
    const link = lookup('[data-public-page-link]');
    link.classList.add('hidden');
    link.classList.remove('inline-flex');
    link.removeAttribute('href');
}

function fillPublicForm() {
    const page = publicPage();
    document.getElementById('publicHero').value = '';
    document.getElementById('publicPoster').value = '';
    document.getElementById('publicOgImage').value = '';
    if (!page) {
        clearPublicForm();
        return;
    }
    const values = {
        publicSlug: page.slug, publicGenre: page.genre,
        publicShortDescription: page.shortDescription, publicSynopsis: page.synopsis,
        publicDirectorNote: page.directorNote, publicAgeRating: page.ageRating,
        publicRuntime: page.runtimeMinutes, publicIntermission: page.intermissionMinutes,
        publicFee: page.admissionFee, publicOrganizer: page.organizerName,
        publicContactName: page.contactName, publicContactChannel: page.contactChannel,
        publicAccent: page.accentColor, publicPublishStart: toInput(page.publishStartDttm),
        publicPublishEnd: toInput(page.publishEndDttm), publicOgTitle: page.ogTitle,
        publicOgDescription: page.ogDescription,
    };
    Object.entries(values).forEach(([id, value]) => {
        document.getElementById(id).value = value ?? '';
    });
    lookup('[data-public-page-status]').replaceChildren(
            badge(PAGE_STATUS[page.status] || page.status, statusTone(page.status)));
    const link = lookup('[data-public-page-link]');
    if (isPublicPageVisible(page)) {
        link.href = `/performances/${encodeURIComponent(page.slug)}`;
        link.classList.remove('hidden');
        link.classList.add('inline-flex');
    } else {
        link.classList.add('hidden');
        link.classList.remove('inline-flex');
        link.removeAttribute('href');
    }
}

function fillGuide(guide) {
    const values = {
        guideEntry: guide?.entryPolicy, guideLate: guide?.lateEntryPolicy,
        guideRecording: guide?.recordingPolicy, guideCancellation: guide?.cancellationPolicy,
        guideAccessibility: guide?.accessibilityPolicy, guideDirections: guide?.directions,
        guideParking: guide?.parkingInformation,
    };
    Object.entries(values).forEach(([id, value]) => {
        document.getElementById(id).value = value || '';
    });
}

function renderNotices() {
    const region = lookup('[data-performance-notices]');
    region.replaceChildren();
    if (!linkedNotices.length) {
        region.appendChild(element('p', 'rounded-md bg-secondary px-3 py-3 text-sm text-muted-foreground', '연결된 공시가 없습니다.'));
        return;
    }
    linkedNotices.forEach((notice) => {
        const row = element('div', 'flex items-center gap-2 rounded-md border px-3 py-2');
        row.appendChild(element('span', 'min-w-0 flex-1 truncate text-sm font-bold', notice.title));
        const remove = actionButton('해제', ACTIONS.NOTICE_DELETE, notice.publicNoticeId, 'danger');
        remove.dataset.confirm = '공연 페이지에서 이 공시 연결을 해제할까요?';
        remove.dataset.confirmAction = '공시 연결 해제';
        row.appendChild(remove);
        region.appendChild(row);
    });
}

async function loadProjectContext() {
    renderProject();
    const selected = project();
    if (!selected) {
        rounds = []; linkedNotices = []; renderRounds(); renderNotices();
        clearPublicForm(); fillGuide(null); return;
    }
    const [nextRounds, notices, guide] = await Promise.all([
        get(`/api/performance-management/projects/${selected.performanceProjectId}/rounds`),
        get(`/api/performance-page-management/projects/${selected.performanceProjectId}/notices`),
        get(`/api/performance-page-management/projects/${selected.performanceProjectId}/viewing-guide`),
    ]);
    rounds = nextRounds;
    linkedNotices = notices;
    renderRounds(); renderNotices(); fillPublicForm(); fillGuide(guide);
}

async function loadAll() {
    [projects, pages] = await Promise.all([
        get('/api/performance-management/projects', {limit: 100}),
        get('/api/performance-page-management'),
    ]);
    renderProjectSelect();
    await loadProjectContext();
}

function openProjectForm(editing) {
    editingProject = editing ? project() : null;
    lookup('[data-project-form]').reset();
    const now = new Date();
    document.getElementById('projectYear').value = editingProject?.academicYear || now.getFullYear();
    document.getElementById('projectTerm').value = editingProject?.termCode || 'FIRST';
    document.getElementById('projectTitle').value = editingProject?.title || '';
    document.getElementById('projectPlace').value = editingProject?.place || '';
    document.getElementById('projectStart').value = editingProject?.productionStartDate || '';
    document.getElementById('projectEnd').value = editingProject?.productionEndDate || '';
    openModal('performanceProjectModal');
}

async function withBusy(trigger, task) {
    trigger.disabled = true;
    try { await task(); } catch (error) {
        showToast(error.message || '요청을 처리하지 못했습니다.');
    } finally { trigger.disabled = false; }
}

async function saveProject(trigger) {
    if (!lookup('[data-project-form]').reportValidity()) return;
    if (readValue('projectEnd') < readValue('projectStart')) {
        showToast('제작 종료일은 시작일보다 빠를 수 없습니다.'); return;
    }
    await withBusy(trigger, async () => {
        const body = {academicYear: Number(readValue('projectYear')),
            termCode: readValue('projectTerm'), title: readValue('projectTitle'),
            place: readValue('projectPlace'), productionStartDate: readValue('projectStart'),
            productionEndDate: readValue('projectEnd')};
        if (editingProject) await put(`/api/performance-management/projects/${editingProject.performanceProjectId}`, body);
        else await post('/api/performance-management/projects', body);
        closeActionModal(trigger); await loadAll(); showToast('공연 프로젝트를 저장했습니다.');
    });
}

function openRoundForm(trigger) {
    editingRound = trigger.dataset.targetId
            ? rounds.find((item) => item.performanceRoundId === Number(trigger.dataset.targetId)) : null;
    lookup('[data-round-form]').reset();
    document.getElementById('roundNo').value = editingRound?.roundNo || rounds.length + 1;
    document.getElementById('roundStart').value = toInput(editingRound?.startDttm);
    document.getElementById('roundEntry').value = toInput(editingRound?.entryStartDttm);
    document.getElementById('roundReservationOpen').value = toInput(editingRound?.reservationOpenDttm);
    document.getElementById('roundReservationClose').value = toInput(editingRound?.reservationCloseDttm);
    openModal('performanceRoundModal', trigger);
}

async function saveRound(trigger) {
    if (!lookup('[data-round-form]').reportValidity()) return;
    const body = {performanceProjectId: project().performanceProjectId,
        roundNo: Number(readValue('roundNo')), startDttm: readValue('roundStart'),
        entryStartDttm: readValue('roundEntry'),
        reservationOpenDttm: readValue('roundReservationOpen'),
        reservationCloseDttm: readValue('roundReservationClose')};
    if (!(body.reservationOpenDttm < body.reservationCloseDttm
            && body.reservationCloseDttm <= body.startDttm
            && body.entryStartDttm <= body.startDttm)) {
        showToast('신청·입장·공연 시각의 순서를 확인해 주세요.'); return;
    }
    await withBusy(trigger, async () => {
        if (editingRound) await put(`/api/performance-management/rounds/${editingRound.performanceRoundId}`, body);
        else await post('/api/performance-management/rounds', body);
        closeActionModal(trigger); await loadProjectContext(); showToast('공연 회차를 저장했습니다.');
    });
}

function openStatus(trigger) {
    const type = trigger.dataset.statusType;
    const target = type === 'project' ? project()
            : rounds.find((item) => item.performanceRoundId === Number(trigger.dataset.targetId));
    statusTarget = {type, target};
    const statuses = type === 'project' ? PROJECT_NEXT[target.status]
            : Object.keys(ROUND_STATUS).filter((status) => status !== target.status);
    const select = document.getElementById('performanceStatusValue');
    select.replaceChildren();
    statuses.forEach((status) => appendOption(select, status,
            (type === 'project' ? PROJECT_STATUS : ROUND_STATUS)[status]));
    lookup('[data-status-summary]').textContent = type === 'project'
            ? target.title : `${target.roundNo}회차 · ${ROUND_STATUS[target.status]}`;
    openModal('performanceStatusModal', trigger);
}

async function saveStatus(trigger) {
    await withBusy(trigger, async () => {
        const status = readValue('performanceStatusValue');
        const url = statusTarget.type === 'project'
                ? `/api/performance-management/projects/${statusTarget.target.performanceProjectId}/status`
                : `/api/performance-management/rounds/${statusTarget.target.performanceRoundId}/status`;
        await patch(url, {status}); closeActionModal(trigger); await loadAll();
        showToast('상태를 변경했습니다.');
    });
}

async function uploadPublicImage(file) {
    if (!file) return null;
    if (!file.type.startsWith('image/')) throw new Error('이미지 파일만 업로드할 수 있습니다.');
    const data = new FormData(); data.append('file', file);
    const privateFile = await post('/api/files/private', data, {query: {domain: 'performance'}});
    return (await post(`/api/files/${privateFile.id}/public-promotions`, {domain: 'performance'})).id;
}

async function savePublicPage(trigger) {
    if (!hasProject() || !lookup('[data-public-page-form]').reportValidity()) return;
    const publishStartDttm = readValue('publicPublishStart') || null;
    const publishEndDttm = readValue('publicPublishEnd') || null;
    if (publishStartDttm && publishEndDttm && publishEndDttm <= publishStartDttm) {
        showToast('공개 종료 시각은 시작 시각보다 늦어야 합니다.');
        return;
    }
    await withBusy(trigger, async () => {
        const current = publicPage();
        const hero = await uploadPublicImage(document.getElementById('publicHero').files[0]);
        const poster = await uploadPublicImage(document.getElementById('publicPoster').files[0]);
        const ogImage = await uploadPublicImage(document.getElementById('publicOgImage').files[0]);
        const body = {performanceProjectId: project().performanceProjectId,
            slug: readValue('publicSlug'), shortDescription: readValue('publicShortDescription'),
            synopsis: readValue('publicSynopsis'), directorNote: readValue('publicDirectorNote') || null,
            genre: readValue('publicGenre'), ageRating: readValue('publicAgeRating'),
            runtimeMinutes: Number(readValue('publicRuntime')),
            intermissionMinutes: Number(readValue('publicIntermission')) || null,
            admissionFee: Number(readValue('publicFee')), heroFileId: hero || current?.heroFileId || null,
            posterFileId: poster || current?.posterFileId || null,
            accentColor: readValue('publicAccent') || null,
            contactName: readValue('publicContactName'), contactChannel: readValue('publicContactChannel'),
            organizerName: readValue('publicOrganizer'), ogTitle: readValue('publicOgTitle') || null,
            ogDescription: readValue('publicOgDescription') || null,
            ogImageFileId: ogImage || current?.ogImageFileId || null,
            publishStartDttm, publishEndDttm};
        if (current) await put(`/api/performance-page-management/${current.performancePublicPageId}`, body);
        else await post('/api/performance-page-management', body);
        await loadAll(); showToast('외부 공연 페이지를 저장했습니다.');
    });
}

function openPublicStatus(trigger) {
    const page = publicPage();
    if (!page) { showToast('먼저 외부 공연 페이지를 저장해 주세요.'); return; }
    statusTarget = {type: 'public', target: page};
    const select = document.getElementById('performanceStatusValue'); select.replaceChildren();
    PAGE_NEXT[page.status].filter((status) => status !== 'SCHEDULED'
            || page.publishStartDttm).forEach((status) =>
        appendOption(select, status, PAGE_STATUS[status]));
    lookup('[data-status-summary]').textContent = `${page.projectTitle} · ${PAGE_STATUS[page.status]}`;
    openModal('performanceStatusModal', trigger);
}

async function saveGuide(trigger) {
    if (!hasProject() || !lookup('[data-viewing-guide-form]').reportValidity()) return;
    await withBusy(trigger, async () => {
        await put('/api/performance-page-management/viewing-guide', {
            performanceProjectId: project().performanceProjectId,
            entryPolicy: readValue('guideEntry'), lateEntryPolicy: readValue('guideLate'),
            recordingPolicy: readValue('guideRecording'), cancellationPolicy: readValue('guideCancellation'),
            accessibilityPolicy: readValue('guideAccessibility'), directions: readValue('guideDirections'),
            parkingInformation: readValue('guideParking') || null});
        showToast('관람 안내를 저장했습니다.');
    });
}

async function openAccessibility(trigger) {
    accessibilityRound = rounds.find((item) => item.performanceRoundId === Number(trigger.dataset.targetId));
    editingAccessibility = null;
    await renderAccessibilities();
    lookup('[data-accessibility-form]').reset();
    openModal('performanceAccessibilityModal', trigger);
}

async function renderAccessibilities() {
    const list = lookup('[data-accessibility-list]'); list.replaceChildren();
    accessibilityItems = await get(`/api/performance-management/rounds/${accessibilityRound.performanceRoundId}/accessibilities`);
    if (!accessibilityItems.length) list.appendChild(element('p', 'rounded-md bg-secondary px-3 py-2 text-sm text-muted-foreground', '등록된 접근성 지원이 없습니다.'));
    accessibilityItems.forEach((item) => {
        const row = element('div', 'flex items-center gap-2 rounded-md border px-3 py-2');
        row.append(element('span', 'min-w-0 flex-1 text-sm font-bold', item.title));
        row.appendChild(actionButton('수정', ACTIONS.ACCESS_EDIT,
                item.performanceRoundAccessibilityId));
        const remove = actionButton('삭제', ACTIONS.ACCESS_DELETE, item.performanceRoundAccessibilityId, 'danger');
        row.appendChild(remove); list.appendChild(row);
    });
}

async function saveAccessibility(trigger) {
    if (!lookup('[data-accessibility-form]').reportValidity()) return;
    await withBusy(trigger, async () => {
        const body = {
            supportType: readValue('accessibilityType'), title: readValue('accessibilityTitle'),
            description: readValue('accessibilityDescription') || null,
            displayOrder: Number(readValue('accessibilityOrder'))};
        if (editingAccessibility) {
            await put(`/api/performance-management/rounds/${accessibilityRound.performanceRoundId}/accessibilities/${editingAccessibility.performanceRoundAccessibilityId}`, body);
        } else {
            await post(`/api/performance-management/rounds/${accessibilityRound.performanceRoundId}/accessibilities`, body);
        }
        lookup('[data-accessibility-form]').reset(); editingAccessibility = null;
        await renderAccessibilities(); showToast('회차 접근성 지원을 저장했습니다.');
    });
}

function editAccessibility(trigger) {
    editingAccessibility = accessibilityItems.find((item) =>
        item.performanceRoundAccessibilityId === Number(trigger.dataset.targetId));
    document.getElementById('accessibilityType').value = editingAccessibility.supportType;
    document.getElementById('accessibilityTitle').value = editingAccessibility.title;
    document.getElementById('accessibilityDescription').value = editingAccessibility.description || '';
    document.getElementById('accessibilityOrder').value = String(editingAccessibility.displayOrder);
    document.getElementById('accessibilityTitle').focus();
}

async function deleteAccessibility(trigger) {
    await withBusy(trigger, async () => {
        await del(`/api/performance-management/accessibilities/${trigger.dataset.targetId}`);
        await renderAccessibilities(); showToast('접근성 지원을 삭제했습니다.');
    });
}

async function openNoticeLink(trigger) {
    const [published, scheduled] = await Promise.all([
        get('/api/admin/public-notices', {status: 'PUBLISHED', pageSize: 100}),
        get('/api/admin/public-notices', {status: 'SCHEDULED', pageSize: 100})]);
    const linkedIds = new Set(linkedNotices.map((notice) => notice.publicNoticeId));
    const available = [...published, ...scheduled].filter((notice) => !linkedIds.has(notice.publicNoticeId));
    const select = document.getElementById('performanceNoticeSelect'); select.replaceChildren();
    available.forEach((notice) => appendOption(select, notice.publicNoticeId, notice.title));
    if (!available.length) appendOption(select, '', '연결 가능한 공시가 없습니다');
    openModal('performanceNoticeModal', trigger);
}

async function saveNoticeLink(trigger) {
    const noticeId = Number(readValue('performanceNoticeSelect'));
    if (!noticeId) { showToast('연결할 공시를 선택해 주세요.'); return; }
    await withBusy(trigger, async () => {
        await post(`/api/performance-page-management/projects/${project().performanceProjectId}/notices`, {publicNoticeId: noticeId});
        closeActionModal(trigger); await loadProjectContext(); showToast('공시를 연결했습니다.');
    });
}

async function deleteNoticeLink(trigger) {
    await withBusy(trigger, async () => {
        await del(`/api/performance-page-management/projects/${project().performanceProjectId}/notices/${trigger.dataset.targetId}`);
        await loadProjectContext(); showToast('공시 연결을 해제했습니다.');
    });
}

document.getElementById('performanceProjectSelect').addEventListener('change', () =>
    loadProjectContext().catch((error) => showToast(error.message)));
bindPageActions({
    [ACTIONS.PROJECT_CREATE]: () => openProjectForm(false), [ACTIONS.PROJECT_EDIT]: () => openProjectForm(true),
    [ACTIONS.PROJECT_SAVE]: saveProject, [ACTIONS.ROUND_CREATE]: openRoundForm,
    [ACTIONS.ROUND_EDIT]: openRoundForm, [ACTIONS.ROUND_SAVE]: saveRound,
    [ACTIONS.STATUS_OPEN]: openStatus, [ACTIONS.STATUS_SAVE]: (trigger) =>
        statusTarget?.type === 'public' ? withBusy(trigger, async () => {
            await patch(`/api/performance-page-management/${statusTarget.target.performancePublicPageId}/status`, {status: readValue('performanceStatusValue')});
            closeActionModal(trigger); await loadAll(); showToast('공개 상태를 변경했습니다.');
        }) : saveStatus(trigger),
    [ACTIONS.PUBLIC_STATUS]: openPublicStatus, [ACTIONS.PUBLIC_SAVE]: savePublicPage,
    [ACTIONS.GUIDE_SAVE]: saveGuide, [ACTIONS.ACCESS_OPEN]: openAccessibility,
    [ACTIONS.ACCESS_SAVE]: saveAccessibility, [ACTIONS.ACCESS_EDIT]: editAccessibility,
    [ACTIONS.ACCESS_DELETE]: deleteAccessibility,
    [ACTIONS.NOTICE_OPEN]: openNoticeLink, [ACTIONS.NOTICE_SAVE]: saveNoticeLink,
    [ACTIONS.NOTICE_DELETE]: deleteNoticeLink,
});
loadAll().catch((error) => showToast(error.message || '공연 운영 정보를 불러오지 못했습니다.'));
