import {ApiError, del, get, patch, post, put} from '../common/api.js';
import {bindPageActions, element, lookup, readValue} from '../common/dom.js';
import {openSheet, closeSheetOf} from '../common/sheet.js';
import {showToast} from '../common/toast.js';
import {badge} from '../common/view.js';

const ACTIONS = Object.freeze({
    RELOAD: 'performance-reload', RELOAD_CONTEXT: 'performance-reload-context',
    PROJECT_CREATE: 'performance-project-create-open', PROJECT_EDIT: 'performance-project-edit-open',
    PROJECT_SAVE: 'performance-project-save', ROUND_CREATE: 'performance-round-create-open',
    ROUND_EDIT: 'performance-round-edit-open', ROUND_SAVE: 'performance-round-save',
    ACCESS_OPEN: 'performance-accessibility-open', ACCESS_SAVE: 'performance-accessibility-save',
    ACCESS_EDIT: 'performance-accessibility-edit', ACCESS_DELETE: 'performance-accessibility-delete',
    STATUS_OPEN: 'performance-status-open', ROUND_STATUS_OPEN: 'performance-round-status-open',
    STATUS_SAVE: 'performance-status-save', PUBLIC_SAVE: 'performance-public-save',
    PUBLIC_STATUS: 'performance-public-status-open', GUIDE_SAVE: 'performance-guide-save',
    NOTICE_OPEN: 'performance-notice-open', NOTICE_SAVE: 'performance-notice-save',
    NOTICE_DELETE: 'performance-notice-delete', CHECK_STEP: 'performance-checklist-step',
});
const SECTIONS = ['overview', 'rounds', 'public', 'guide'];
const PROJECT_STATUS = {
    PLANNING: '기획', PRODUCING: '제작', RESERVATION_OPEN: '신청 중',
    PERFORMING: '공연 중', ENDED: '종료', CANCELLED: '취소', ARCHIVED: '보관',
};
const PROJECT_NEXT = {
    PLANNING: ['PRODUCING', 'CANCELLED'], PRODUCING: ['RESERVATION_OPEN', 'CANCELLED'],
    RESERVATION_OPEN: ['PERFORMING', 'CANCELLED'], PERFORMING: ['ENDED', 'CANCELLED'],
    ENDED: ['ARCHIVED'], CANCELLED: [], ARCHIVED: [],
};
const PROJECT_EFFECTS = {
    PRODUCING: '제작 단계로 넘어가요. 팀별 제작 진행이 이 프로젝트 기준으로 운영돼요.',
    RESERVATION_OPEN: '관람 신청을 받는 프로젝트로 표시돼요. 회차별 신청 기간도 함께 확인해 주세요.',
    PERFORMING: '공연이 진행 중인 프로젝트로 표시돼요.',
    ENDED: '공연이 끝난 프로젝트로 표시돼요.',
    CANCELLED: '프로젝트를 취소해요. 선택 목록에서 사라지고 회차 운영을 이어갈 수 없어요.',
    ARCHIVED: '보관 상태가 돼요. 이후에는 수정하지 않는 것을 전제로 해요.',
};
const ROUND_STATUS = {
    SCHEDULED: '예정', RESERVATION_OPEN: '신청 중', RESERVATION_CLOSED: '신청 마감',
    ENTRY_OPEN: '입장 중', ENDED: '종료', CANCELLED: '취소',
};
const ROUND_NEXT = {
    SCHEDULED: ['RESERVATION_OPEN', 'CANCELLED'],
    RESERVATION_OPEN: ['RESERVATION_CLOSED', 'CANCELLED'],
    RESERVATION_CLOSED: ['ENTRY_OPEN', 'CANCELLED'],
    ENTRY_OPEN: ['ENDED', 'CANCELLED'],
    ENDED: [], CANCELLED: [],
};
const ROUND_EFFECTS = {
    RESERVATION_OPEN: '이 회차의 관람 신청 접수를 시작해요.',
    RESERVATION_CLOSED: '신청 접수를 마감해요. 이미 들어온 신청은 유지돼요.',
    ENTRY_OPEN: '공연 당일 입장 화면에서 이 회차의 QR 입장 처리가 가능해져요.',
    ENDED: '회차를 종료 상태로 바꿔요.',
    CANCELLED: '회차를 취소해요. 신청과 입장 처리를 할 수 없게 돼요.',
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
const PAGE_EFFECTS = {
    DRAFT: '초안으로 되돌려요. 외부에는 보이지 않아요.',
    SCHEDULED: '공개 시작 시각부터 자동으로 공개돼요.',
    PUBLISHED: '지금부터 외부 관람객에게 공개돼요.',
    ENDED: '공연 종료로 표시돼요. 기록 페이지는 남고 신청 버튼은 사라져요.',
    CANCELLED: '공연 취소로 표시돼요. 페이지는 취소 안내와 함께 유지돼요.',
    ARCHIVED: '보관 상태가 돼요. 외부에 보이지 않아요.',
};

let projects = [];
let rounds = [];
let pages = [];
let linkedNotices = [];
let guideData = null;
let editingProject = null;
let editingRound = null;
let accessibilityRound = null;
let accessibilityItems = [];
let editingAccessibility = null;
let statusTarget = null;
let publicDirty = false;
let guideDirty = false;
let loadedProjectId = null;
const previewUrls = new Map();

function project() {
    const id = Number(readValue('performanceProjectSelect'));
    return projects.find((item) => item.performanceProjectId === id);
}

function publicPage() {
    return pages.find((item) => item.performanceProjectId === project()?.performanceProjectId) || null;
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

function describeError(error) {
    if (error instanceof ApiError && error.status === 401) {
        return '로그인이 만료됐어요. 다시 로그인해야 저장할 수 있어요. 작성한 내용은 이 화면에 그대로 남아 있어요.';
    }
    if (error instanceof ApiError && error.status === 409) {
        return '다른 곳에서 먼저 변경된 것 같아요. 화면을 새로고침해 최신 상태를 확인해 주세요.';
    }
    return error.message || '요청을 처리하지 못했어요. 잠시 후 다시 시도해 주세요.';
}

function showFormError(selector, message) {
    const area = lookup(selector);
    if (!area) {
        return;
    }
    if (!message) {
        area.classList.add('hidden');
        area.textContent = '';
        return;
    }
    area.textContent = message;
    area.classList.remove('hidden');
}

function isPublicPageVisible(page) {
    if (['ENDED', 'CANCELLED'].includes(page.status)) {
        return true;
    }
    if (page.status === 'ARCHIVED') {
        return false;
    }
    const now = new Date();
    const startsAt = page.publishStartDttm ? new Date(page.publishStartDttm) : null;
    const endsAt = page.publishEndDttm ? new Date(page.publishEndDttm) : null;
    if ((startsAt && startsAt > now) || (endsAt && endsAt <= now)) {
        return false;
    }
    return page.status === 'PUBLISHED' || (page.status === 'SCHEDULED' && Boolean(startsAt));
}

function compactButton(label, action, id) {
    const button = element('button', 'inline-flex min-h-11 items-center justify-center rounded-md border bg-card px-3 text-xs font-bold transition-colors hover:bg-secondary focus-visible:ring-2 focus-visible:ring-ring md:min-h-9', label);
    button.type = 'button';
    button.dataset.pageAction = action;
    if (id !== undefined) {
        button.dataset.targetId = String(id);
    }
    return button;
}

function statusTone(status) {
    if (['PUBLISHED', 'ENDED'].includes(status)) {
        return 'success';
    }
    if (['CANCELLED'].includes(status)) {
        return 'danger';
    }
    if (['SCHEDULED', 'RESERVATION_OPEN', 'ENTRY_OPEN', 'PERFORMING'].includes(status)) {
        return 'warning';
    }
    return 'neutral';
}

function setHidden(selector, hidden) {
    lookup(selector)?.classList.toggle('hidden', hidden);
}

// ---------- 섹션 전환 (URL hash 보존) ----------

function currentSection() {
    const name = window.location.hash.replace('#', '');
    return SECTIONS.includes(name) ? name : 'overview';
}

function showSection(name, moveFocus = false) {
    SECTIONS.forEach((section) => {
        const panel = lookup(`[data-section-panel="${section}"]`);
        const tab = lookup(`[data-section-tab="${section}"]`);
        const active = section === name;
        panel?.classList.toggle('hidden', !active);
        if (!tab) {
            return;
        }
        tab.classList.toggle('bg-card', active);
        tab.classList.toggle('border', active);
        tab.classList.toggle('text-foreground', active);
        tab.classList.toggle('text-muted-foreground', !active);
        tab.setAttribute('aria-current', active ? 'true' : 'false');
    });
    if (window.location.hash.replace('#', '') !== name) {
        window.history.replaceState(null, '', `#${name}`);
    }
    renderNextAction();
    if (moveFocus) {
        lookup(`[data-section-panel="${name}"] h2`)?.focus();
    }
}

// ---------- 준비 체크리스트 · 다음 행동 ----------

function checklistSteps() {
    const page = publicPage();
    return [
        {key: 'project', label: '프로젝트 기본 정보', done: Boolean(project()), go: 'overview'},
        {key: 'rounds', label: '공연 회차', done: rounds.length > 0, go: 'rounds'},
        {key: 'page', label: '외부 공연 페이지 저장', done: Boolean(page), go: 'public'},
        {key: 'guide', label: '관람 안내 저장', done: Boolean(guideData), go: 'guide'},
        {key: 'publish', label: '공개 상태 전환', done: Boolean(page) && page.status !== 'DRAFT', go: 'public'},
    ];
}

function renderChecklist() {
    const list = lookup('[data-checklist]');
    if (!list) {
        return;
    }
    list.replaceChildren();
    const steps = checklistSteps();
    const nextKey = steps.find((step) => !step.done)?.key;
    steps.forEach((step) => {
        const item = element('li');
        const button = element('button', 'flex min-h-11 w-full items-center gap-3 rounded-md px-2 text-left text-sm transition-colors hover:bg-secondary focus-visible:ring-2 focus-visible:ring-ring');
        button.type = 'button';
        button.dataset.pageAction = ACTIONS.CHECK_STEP;
        button.dataset.section = step.go;
        const isNext = step.key === nextKey;
        const mark = element('span', `flex size-5 shrink-0 items-center justify-center rounded-full text-xs font-bold ${step.done ? 'bg-success-soft text-success' : isNext ? 'bg-primary text-primary-foreground' : 'border text-muted-foreground'}`, step.done ? '✓' : isNext ? '→' : '');
        mark.setAttribute('aria-hidden', 'true');
        const text = element('span', step.done ? 'text-muted-foreground' : isNext ? 'font-bold' : '', step.label);
        const state = element('span', 'ml-auto text-xs text-muted-foreground', step.done ? '완료' : isNext ? '다음 할 일' : '대기');
        button.append(mark, text, state);
        item.appendChild(button);
        list.appendChild(item);
    });
}

function nextActionPlan() {
    const selected = project();
    if (!selected) {
        return null;
    }
    if (rounds.length === 0) {
        return {message: '첫 공연 회차를 만들어 주세요. 회차가 있어야 관람 신청을 받을 수 있어요.',
            label: '첫 회차 만들기', action: ACTIONS.ROUND_CREATE};
    }
    if (!publicPage()) {
        return {message: '외부 관람객에게 보여줄 공연 페이지를 작성해 주세요.',
            label: '외부 공개 작성하기', action: ACTIONS.CHECK_STEP, section: 'public'};
    }
    if (!guideData) {
        return {message: '입장·취소·접근성 정책을 담은 관람 안내를 작성해 주세요.',
            label: '관람 안내 작성하기', action: ACTIONS.CHECK_STEP, section: 'guide'};
    }
    if (publicPage().status === 'DRAFT') {
        return {message: '외부 공연 페이지가 아직 초안이에요. 공개 상태로 전환해 주세요.',
            label: '공개 상태 변경', action: ACTIONS.PUBLIC_STATUS};
    }
    return null;
}

function renderNextAction() {
    const plan = nextActionPlan();
    const message = lookup('[data-next-action-message]');
    const button = lookup('[data-next-action-button]');
    const done = lookup('[data-next-action-done]');
    if (!message || !button || !done) {
        return;
    }
    if (!plan) {
        message.textContent = '준비 단계를 모두 마쳤어요. 공연 당일에는 입장 화면에서 관람객을 맞아 주세요.';
        button.classList.add('hidden');
        done.classList.remove('hidden');
        return;
    }
    message.textContent = plan.message;
    button.textContent = plan.label;
    button.dataset.pageAction = plan.action;
    if (plan.section) {
        button.dataset.section = plan.section;
    } else {
        delete button.dataset.section;
    }
    // 저장 버튼(primary)이 이미 보이는 섹션에서는 primary CTA를 중복 배치하지 않는다
    const section = currentSection();
    const overlapsSectionPrimary = (plan.section === section && ['public', 'guide'].includes(section))
            || (plan.action === ACTIONS.PUBLIC_STATUS && section === 'public');
    button.classList.toggle('hidden', overlapsSectionPrimary);
    done.classList.add('hidden');
}

// ---------- 렌더링 ----------

function renderProjectSelect() {
    const select = document.getElementById('performanceProjectSelect');
    const previous = select.value;
    select.replaceChildren();
    projects.filter((item) => item.status !== 'CANCELLED').forEach((item) =>
        appendOption(select, item.performanceProjectId,
                `${item.academicYear} ${item.termCode === 'FIRST' ? '1학기' : '2학기'} · ${item.title}`));
    if (previous && Array.from(select.options).some((option) => option.value === previous)) {
        select.value = previous;
    }
}

function renderProject() {
    const selected = project();
    const summary = lookup('[data-project-summary]');
    setHidden('[data-project-empty]', Boolean(selected));
    setHidden('[data-project-context]', !selected);
    summary.replaceChildren();
    if (!selected) {
        return;
    }
    summary.append(badge(PROJECT_STATUS[selected.status] || selected.status, statusTone(selected.status)),
            element('strong', 'truncate text-foreground', selected.title));
    lookup('[data-overview-period]').textContent = `${selected.productionStartDate} — ${selected.productionEndDate}`;
    lookup('[data-overview-place]').textContent = selected.place;
}

function renderRounds() {
    const list = lookup('[data-rounds-list]');
    list.replaceChildren();
    setHidden('[data-rounds-empty]', rounds.length > 0);
    setHidden('[data-rounds-list]', rounds.length === 0);
    rounds.forEach((round) => {
        const row = element('article', 'flex flex-col gap-2 p-4 md:flex-row md:items-center md:gap-4');
        const head = element('div', 'flex min-w-0 flex-1 flex-col gap-1');
        const titleLine = element('div', 'flex flex-wrap items-center gap-2');
        titleLine.append(element('h3', 'text-sm font-bold', `${round.roundNo}회차`),
                badge(ROUND_STATUS[round.status] || round.status, statusTone(round.status)));
        head.append(titleLine,
                element('p', 'text-xs text-muted-foreground tabular-nums',
                        `공연 ${formatDateTime(round.startDttm)} · 입장 ${formatDateTime(round.entryStartDttm)}`),
                element('p', 'text-xs text-muted-foreground tabular-nums',
                        `신청 ${formatDateTime(round.reservationOpenDttm)} — ${formatDateTime(round.reservationCloseDttm)}`));
        const actions = element('div', 'flex flex-wrap gap-1.5 md:shrink-0');
        const status = compactButton('상태 변경', ACTIONS.ROUND_STATUS_OPEN, round.performanceRoundId);
        actions.append(compactButton('수정', ACTIONS.ROUND_EDIT, round.performanceRoundId), status,
                compactButton('접근성', ACTIONS.ACCESS_OPEN, round.performanceRoundId));
        row.append(head, actions);
        list.appendChild(row);
    });
}

function renderNotices() {
    const region = lookup('[data-performance-notices]');
    region.replaceChildren();
    if (!linkedNotices.length) {
        region.appendChild(element('p', 'rounded-md bg-secondary px-3 py-3 text-sm text-muted-foreground', '아직 연결된 공시가 없어요.'));
        return;
    }
    linkedNotices.forEach((notice) => {
        const row = element('div', 'flex items-center gap-2 rounded-md border px-3 py-2');
        row.appendChild(element('span', 'min-w-0 flex-1 truncate text-sm font-bold', notice.title));
        const remove = compactButton('연결 해제', ACTIONS.NOTICE_DELETE, notice.publicNoticeId);
        remove.classList.add('text-destructive', 'hover:bg-destructive-soft');
        remove.dataset.confirm = '공연 페이지에서 이 공시 연결을 해제할까요? 공시 자체는 삭제되지 않아요.';
        remove.dataset.confirmAction = '공시 연결 해제';
        row.appendChild(remove);
        region.appendChild(row);
    });
}

function clearImagePreviews() {
    previewUrls.forEach((url) => URL.revokeObjectURL(url));
    previewUrls.clear();
    document.querySelectorAll('[data-image-preview]').forEach((preview) => {
        preview.replaceChildren();
        preview.classList.add('hidden');
        preview.classList.remove('flex');
    });
}

function renderImagePreview(input) {
    const preview = lookup(`[data-image-preview="${input.id}"]`);
    if (!preview) {
        return;
    }
    const previous = previewUrls.get(input.id);
    if (previous) {
        URL.revokeObjectURL(previous);
        previewUrls.delete(input.id);
    }
    preview.replaceChildren();
    const file = input.files[0];
    if (!file) {
        preview.classList.add('hidden');
        preview.classList.remove('flex');
        return;
    }
    const url = URL.createObjectURL(file);
    previewUrls.set(input.id, url);
    const image = element('img', 'size-11 rounded-md border object-cover');
    image.src = url;
    image.alt = '';
    preview.append(image, element('span', 'min-w-0 truncate text-xs text-muted-foreground', file.name));
    preview.classList.remove('hidden');
    preview.classList.add('flex');
}

function setPublicDirty(dirty) {
    publicDirty = dirty;
    const area = lookup('[data-public-dirty]');
    if (area) {
        area.textContent = dirty ? '저장하지 않은 변경이 있어요.' : '';
    }
}

function setGuideDirty(dirty) {
    guideDirty = dirty;
    const area = lookup('[data-guide-dirty]');
    if (area) {
        area.textContent = dirty ? '저장하지 않은 변경이 있어요.' : '';
    }
}

function hasUnsavedChanges() {
    return publicDirty || guideDirty;
}

function confirmDiscardChanges() {
    if (!hasUnsavedChanges()) {
        return true;
    }
    const confirmed = window.confirm('저장하지 않은 변경을 버리고 이동할까요? 작성한 내용은 복구할 수 없어요.');
    if (confirmed) {
        setPublicDirty(false);
        setGuideDirty(false);
        clearImagePreviews();
    }
    return confirmed;
}

function markPublicSaved(savedNow = false) {
    setPublicDirty(false);
    if (savedNow) {
        const area = lookup('[data-public-dirty]');
        const now = new Date();
        area.textContent = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}에 저장했어요. (이 브라우저 기준)`;
    }
}

function markGuideSaved(savedNow = false) {
    setGuideDirty(false);
    if (savedNow) {
        const area = lookup('[data-guide-dirty]');
        const now = new Date();
        area.textContent = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}에 저장했어요. (이 브라우저 기준)`;
    }
}

function clearPublicForm() {
    lookup('[data-public-page-form]').reset();
    document.getElementById('publicFee').value = '0';
    lookup('[data-public-page-status]').replaceChildren(badge('페이지 없음', 'neutral'));
    const link = lookup('[data-public-page-link]');
    link.classList.add('hidden');
    link.classList.remove('inline-flex');
    link.removeAttribute('href');
    clearImagePreviews();
    setPublicDirty(false);
}

function fillPublicForm() {
    const page = publicPage();
    ['publicHero', 'publicPoster', 'publicOgImage'].forEach((id) => {
        document.getElementById(id).value = '';
    });
    clearImagePreviews();
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
    setPublicDirty(false);
}

function fillGuide(guide) {
    guideData = guide || null;
    const values = {
        guideEntry: guide?.entryPolicy, guideLate: guide?.lateEntryPolicy,
        guideRecording: guide?.recordingPolicy, guideCancellation: guide?.cancellationPolicy,
        guideAccessibility: guide?.accessibilityPolicy, guideDirections: guide?.directions,
        guideParking: guide?.parkingInformation,
    };
    Object.entries(values).forEach(([id, value]) => {
        document.getElementById(id).value = value || '';
    });
    setGuideDirty(false);
}

function renderAll() {
    renderProject();
    renderRounds();
    renderNotices();
    renderChecklist();
    renderNextAction();
}

// ---------- 로딩 ----------

async function loadProjectContext({preserveEdits = true} = {}) {
    const selected = project();
    const preservePublic = preserveEdits && publicDirty;
    const preserveGuide = preserveEdits && guideDirty;
    setHidden('[data-rounds-error]', true);
    if (!selected) {
        rounds = [];
        linkedNotices = [];
        guideData = null;
        renderAll();
        clearPublicForm();
        fillGuide(null);
        loadedProjectId = null;
        return;
    }
    const [roundsResult, noticesResult, guideResult] = await Promise.allSettled([
        get(`/api/performance-management/projects/${selected.performanceProjectId}/rounds`),
        get(`/api/performance-page-management/projects/${selected.performanceProjectId}/notices`),
        get(`/api/performance-page-management/projects/${selected.performanceProjectId}/viewing-guide`),
    ]);
    rounds = roundsResult.status === 'fulfilled' ? roundsResult.value : [];
    linkedNotices = noticesResult.status === 'fulfilled' ? noticesResult.value : [];
    if (!preserveGuide) {
        fillGuide(guideResult.status === 'fulfilled' ? guideResult.value : null);
    }
    renderAll();
    if (!preservePublic) {
        fillPublicForm();
    }
    loadedProjectId = selected.performanceProjectId;
    if (roundsResult.status === 'rejected') {
        setHidden('[data-rounds-error]', false);
    }
    if (noticesResult.status === 'rejected') {
        const region = lookup('[data-performance-notices]');
        region.replaceChildren(element('p', 'rounded-md bg-destructive-soft px-3 py-3 text-sm text-destructive',
                '공시 목록을 불러오지 못했어요. 다른 정보는 정상이에요.'));
    }
    if (guideResult.status === 'rejected' && !preserveGuide) {
        setGuideDirty(false);
        lookup('[data-guide-dirty]').textContent = '관람 안내를 불러오지 못했어요. 저장 전에 다시 불러와 주세요.';
    }
}

async function loadAll({preserveEdits = true} = {}) {
    setHidden('[data-page-loading]', false);
    setHidden('[data-page-error]', true);
    setHidden('[data-page-body]', true);
    try {
        [projects, pages] = await Promise.all([
            get('/api/performance-management/projects', {limit: 100}),
            get('/api/performance-page-management'),
        ]);
        renderProjectSelect();
        await loadProjectContext({preserveEdits});
        setHidden('[data-page-body]', false);
        showSection(currentSection());
    } catch (error) {
        lookup('[data-page-error-message]').textContent = describeError(error);
        setHidden('[data-page-error]', false);
    } finally {
        setHidden('[data-page-loading]', true);
    }
}

// ---------- 프로젝트 ----------

function openProjectForm(editing) {
    editingProject = editing ? project() : null;
    lookup('[data-project-form]').reset();
    showFormError('[data-project-form-error]', '');
    const now = new Date();
    document.getElementById('projectYear').value = editingProject?.academicYear || now.getFullYear();
    document.getElementById('projectTerm').value = editingProject?.termCode || 'FIRST';
    document.getElementById('projectTitle').value = editingProject?.title || '';
    document.getElementById('projectPlace').value = editingProject?.place || '';
    document.getElementById('projectStart').value = editingProject?.productionStartDate || '';
    document.getElementById('projectEnd').value = editingProject?.productionEndDate || '';
    openSheet('performanceProjectSheet');
}

async function withBusy(trigger, task, errorSelector) {
    trigger.disabled = true;
    try {
        await task();
    } catch (error) {
        const message = describeError(error);
        if (errorSelector) {
            showFormError(errorSelector, message);
        } else {
            showToast(message);
        }
    } finally {
        trigger.disabled = false;
    }
}

async function saveProject(trigger) {
    if (!lookup('[data-project-form]').reportValidity()) {
        return;
    }
    if (readValue('projectEnd') < readValue('projectStart')) {
        showFormError('[data-project-form-error]', '제작 종료일은 시작일보다 빠를 수 없어요. 날짜를 다시 확인해 주세요.');
        return;
    }
    await withBusy(trigger, async () => {
        const body = {academicYear: Number(readValue('projectYear')),
            termCode: readValue('projectTerm'), title: readValue('projectTitle'),
            place: readValue('projectPlace'), productionStartDate: readValue('projectStart'),
            productionEndDate: readValue('projectEnd')};
        if (editingProject) {
            await put(`/api/performance-management/projects/${editingProject.performanceProjectId}`, body);
        } else {
            await post('/api/performance-management/projects', body);
        }
        closeSheetOf(trigger);
        await loadAll();
        showToast('공연 프로젝트를 저장했어요.');
    }, '[data-project-form-error]');
}

// ---------- 회차 ----------

function openRoundForm(trigger) {
    editingRound = trigger.dataset.targetId
            ? rounds.find((item) => item.performanceRoundId === Number(trigger.dataset.targetId)) : null;
    lookup('[data-round-form]').reset();
    showFormError('[data-round-form-error]', '');
    document.getElementById('roundNo').value = editingRound?.roundNo || rounds.length + 1;
    document.getElementById('roundStart').value = toInput(editingRound?.startDttm);
    document.getElementById('roundEntry').value = toInput(editingRound?.entryStartDttm);
    document.getElementById('roundReservationOpen').value = toInput(editingRound?.reservationOpenDttm);
    document.getElementById('roundReservationClose').value = toInput(editingRound?.reservationCloseDttm);
    openSheet('performanceRoundSheet', trigger);
}

async function saveRound(trigger) {
    if (!lookup('[data-round-form]').reportValidity()) {
        return;
    }
    const body = {performanceProjectId: project().performanceProjectId,
        roundNo: Number(readValue('roundNo')), startDttm: readValue('roundStart'),
        entryStartDttm: readValue('roundEntry'),
        reservationOpenDttm: readValue('roundReservationOpen'),
        reservationCloseDttm: readValue('roundReservationClose')};
    if (!(body.reservationOpenDttm < body.reservationCloseDttm
            && body.reservationCloseDttm <= body.startDttm
            && body.entryStartDttm <= body.startDttm)) {
        showFormError('[data-round-form-error]', '신청 시작 → 신청 마감 → 공연 시작 순서여야 하고, 입장 시작도 공연 시작보다 앞서야 해요.');
        return;
    }
    await withBusy(trigger, async () => {
        if (editingRound) {
            await put(`/api/performance-management/rounds/${editingRound.performanceRoundId}`, body);
        } else {
            await post('/api/performance-management/rounds', body);
        }
        closeSheetOf(trigger);
        await loadProjectContext();
        showToast('공연 회차를 저장했어요.');
    }, '[data-round-form-error]');
}

// ---------- 상태 변경 ----------

function renderStatusOptions(statuses, labels, effects, current) {
    const host = lookup('[data-status-options]');
    host.replaceChildren();
    showFormError('[data-status-error]', '');
    if (statuses.length === 0) {
        host.appendChild(element('p', 'text-sm text-muted-foreground', '지금 상태에서는 더 바꿀 수 있는 상태가 없어요.'));
        return;
    }
    statuses.forEach((status, index) => {
        const label = element('label', 'flex min-h-11 cursor-pointer items-start gap-3 rounded-md border p-3 transition-colors has-[:checked]:border-primary has-[:checked]:bg-accent');
        const input = element('input', 'mt-1 size-4 shrink-0 accent-primary');
        input.type = 'radio';
        input.name = 'performanceStatusOption';
        input.value = status;
        input.checked = index === 0;
        const text = element('span', 'min-w-0');
        text.append(element('b', 'block text-sm font-bold', labels[status] || status),
                element('span', 'mt-0.5 block text-xs text-muted-foreground', effects[status] || ''));
        label.append(input, text);
        host.appendChild(label);
    });
    if (current) {
        lookup('[data-status-summary]').append(element('span', 'ml-2 text-xs font-medium text-muted-foreground', `현재 ${current}`));
    }
}

function selectedStatus() {
    return document.querySelector('input[name="performanceStatusOption"]:checked')?.value || '';
}

function openProjectStatus(trigger) {
    const target = project();
    statusTarget = {type: 'project', target};
    lookup('[data-status-summary]').textContent = target.title;
    renderStatusOptions(PROJECT_NEXT[target.status] || [], PROJECT_STATUS, PROJECT_EFFECTS,
            PROJECT_STATUS[target.status]);
    openSheet('performanceStatusSheet', trigger);
}

function openRoundStatus(trigger) {
    const target = rounds.find((item) => item.performanceRoundId === Number(trigger.dataset.targetId));
    statusTarget = {type: 'round', target};
    lookup('[data-status-summary]').textContent = `${target.roundNo}회차`;
    renderStatusOptions(ROUND_NEXT[target.status] || [],
            ROUND_STATUS, ROUND_EFFECTS, ROUND_STATUS[target.status]);
    openSheet('performanceStatusSheet', trigger);
}

function openPublicStatus(trigger) {
    const page = publicPage();
    if (!page) {
        showToast('먼저 외부 공연 페이지를 저장해 주세요.');
        return;
    }
    statusTarget = {type: 'public', target: page};
    lookup('[data-status-summary]').textContent = page.projectTitle || project().title;
    renderStatusOptions((PAGE_NEXT[page.status] || []).filter((status) =>
        status !== 'SCHEDULED' || page.publishStartDttm), PAGE_STATUS, PAGE_EFFECTS,
            PAGE_STATUS[page.status]);
    openSheet('performanceStatusSheet', trigger);
}

async function saveStatus(trigger) {
    const status = selectedStatus();
    if (!status) {
        showFormError('[data-status-error]', '변경할 상태를 선택해 주세요.');
        return;
    }
    if (statusTarget.type === 'public' && publicDirty) {
        showFormError('[data-status-error]', '외부 공연 페이지의 변경 내용을 먼저 저장해 주세요. 저장 후 공개 상태를 바꿀 수 있어요.');
        return;
    }
    await withBusy(trigger, async () => {
        const urls = {
            project: `/api/performance-management/projects/${statusTarget.target.performanceProjectId}/status`,
            round: `/api/performance-management/rounds/${statusTarget.target.performanceRoundId}/status`,
            public: `/api/performance-page-management/${statusTarget.target.performancePublicPageId}/status`,
        };
        await patch(urls[statusTarget.type], {status});
        closeSheetOf(trigger);
        await loadAll({preserveEdits: true});
        showToast('상태를 변경했어요.');
    }, '[data-status-error]');
}

// ---------- 외부 공개 저장 ----------

async function uploadPublicImage(file) {
    if (!file) {
        return null;
    }
    if (!file.type.startsWith('image/')) {
        throw new Error('이미지 파일만 업로드할 수 있어요.');
    }
    const data = new FormData();
    data.append('file', file);
    const privateFile = await post('/api/files/private', data, {query: {domain: 'performance'}});
    return (await post(`/api/files/${privateFile.id}/public-promotions`, {domain: 'performance'})).id;
}

async function savePublicPage(trigger) {
    if (!project() || !lookup('[data-public-page-form]').reportValidity()) {
        return;
    }
    const publishStartDttm = readValue('publicPublishStart') || null;
    const publishEndDttm = readValue('publicPublishEnd') || null;
    if (publishStartDttm && publishEndDttm && publishEndDttm <= publishStartDttm) {
        showToast('공개 종료 시각은 시작 시각보다 늦어야 해요.');
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
        if (current) {
            await put(`/api/performance-page-management/${current.performancePublicPageId}`, body);
        } else {
            await post('/api/performance-page-management', body);
        }
        setPublicDirty(false);
        await loadAll({preserveEdits: true});
        markPublicSaved(true);
        showToast('외부 공연 페이지를 저장했어요.');
    });
}

// ---------- 관람 안내 저장 ----------

async function saveGuide(trigger) {
    if (!project() || !lookup('[data-viewing-guide-form]').reportValidity()) {
        return;
    }
    await withBusy(trigger, async () => {
        await put('/api/performance-page-management/viewing-guide', {
            performanceProjectId: project().performanceProjectId,
            entryPolicy: readValue('guideEntry'), lateEntryPolicy: readValue('guideLate'),
            recordingPolicy: readValue('guideRecording'), cancellationPolicy: readValue('guideCancellation'),
            accessibilityPolicy: readValue('guideAccessibility'), directions: readValue('guideDirections'),
            parkingInformation: readValue('guideParking') || null});
        guideData = {entryPolicy: readValue('guideEntry')};
        renderChecklist();
        renderNextAction();
        markGuideSaved(true);
        showToast('관람 안내를 저장했어요.');
    });
}

// ---------- 회차 접근성 ----------

async function openAccessibility(trigger) {
    accessibilityRound = rounds.find((item) => item.performanceRoundId === Number(trigger.dataset.targetId));
    editingAccessibility = null;
    await renderAccessibilities();
    lookup('[data-accessibility-form]').reset();
    openSheet('performanceAccessibilitySheet', trigger);
}

async function renderAccessibilities() {
    const list = lookup('[data-accessibility-list]');
    list.replaceChildren();
    try {
        accessibilityItems = await get(`/api/performance-management/rounds/${accessibilityRound.performanceRoundId}/accessibilities`);
    } catch (error) {
        list.appendChild(element('p', 'rounded-md bg-destructive-soft px-3 py-2 text-sm text-destructive', describeError(error)));
        return;
    }
    if (!accessibilityItems.length) {
        list.appendChild(element('p', 'rounded-md bg-secondary px-3 py-2 text-sm text-muted-foreground', '아직 등록된 접근성 지원이 없어요.'));
    }
    accessibilityItems.forEach((item) => {
        const row = element('div', 'flex items-center gap-2 rounded-md border px-3 py-2');
        row.append(element('span', 'min-w-0 flex-1 text-sm font-bold', item.title));
        row.appendChild(compactButton('수정', ACTIONS.ACCESS_EDIT, item.performanceRoundAccessibilityId));
        const remove = compactButton('삭제', ACTIONS.ACCESS_DELETE, item.performanceRoundAccessibilityId);
        remove.classList.add('text-destructive', 'hover:bg-destructive-soft');
        remove.dataset.confirm = `'${item.title}' 접근성 지원을 삭제할까요? 삭제하면 공개 페이지에서도 사라져요.`;
        remove.dataset.confirmAction = '접근성 삭제';
        row.appendChild(remove);
        list.appendChild(row);
    });
}

async function saveAccessibility(trigger) {
    if (!lookup('[data-accessibility-form]').reportValidity()) {
        return;
    }
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
        lookup('[data-accessibility-form]').reset();
        editingAccessibility = null;
        await renderAccessibilities();
        showToast('회차 접근성 지원을 저장했어요.');
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
        await renderAccessibilities();
        showToast('접근성 지원을 삭제했어요.');
    });
}

// ---------- 공시 연결 ----------

async function openNoticeLink(trigger) {
    const [published, scheduled] = await Promise.all([
        get('/api/admin/public-notices', {status: 'PUBLISHED', pageSize: 100}),
        get('/api/admin/public-notices', {status: 'SCHEDULED', pageSize: 100})]);
    const linkedIds = new Set(linkedNotices.map((notice) => notice.publicNoticeId));
    const available = [...published, ...scheduled].filter((notice) => !linkedIds.has(notice.publicNoticeId));
    const select = document.getElementById('performanceNoticeSelect');
    select.replaceChildren();
    available.forEach((notice) => appendOption(select, notice.publicNoticeId, notice.title));
    if (!available.length) {
        appendOption(select, '', '연결할 수 있는 공시가 없어요');
    }
    openSheet('performanceNoticeSheet', trigger);
}

async function saveNoticeLink(trigger) {
    const noticeId = Number(readValue('performanceNoticeSelect'));
    if (!noticeId) {
        showToast('연결할 공시를 선택해 주세요.');
        return;
    }
    await withBusy(trigger, async () => {
        await post(`/api/performance-page-management/projects/${project().performanceProjectId}/notices`, {publicNoticeId: noticeId});
        closeSheetOf(trigger);
        await loadProjectContext();
        showToast('공시를 연결했어요.');
    });
}

async function deleteNoticeLink(trigger) {
    await withBusy(trigger, async () => {
        await del(`/api/performance-page-management/projects/${project().performanceProjectId}/notices/${trigger.dataset.targetId}`);
        await loadProjectContext();
        showToast('공시 연결을 해제했어요.');
    });
}

// ---------- 이벤트 바인딩 ----------

document.getElementById('performanceProjectSelect').addEventListener('change', (event) => {
    if (!confirmDiscardChanges()) {
        event.target.value = loadedProjectId ? String(loadedProjectId) : '';
        return;
    }
    loadProjectContext({preserveEdits: false})
            .catch((error) => showToast(describeError(error)));
});

document.querySelectorAll('[data-section-tab]').forEach((tab) => {
    tab.addEventListener('click', () => showSection(tab.dataset.sectionTab, true));
});
window.addEventListener('hashchange', () => showSection(currentSection(), true));

lookup('[data-public-page-form]')?.addEventListener('input', () => setPublicDirty(true));
lookup('[data-viewing-guide-form]')?.addEventListener('input', () => setGuideDirty(true));
document.querySelectorAll('[data-image-input]').forEach((input) => {
    input.addEventListener('change', () => renderImagePreview(input));
});
window.addEventListener('beforeunload', (event) => {
    if (publicDirty || guideDirty) {
        event.preventDefault();
    }
});

bindPageActions({
    [ACTIONS.RELOAD]: () => {
        if (confirmDiscardChanges()) {
            loadAll({preserveEdits: false});
        }
    },
    [ACTIONS.RELOAD_CONTEXT]: () => {
        if (confirmDiscardChanges()) {
            loadProjectContext({preserveEdits: false})
                    .catch((error) => showToast(describeError(error)));
        }
    },
    [ACTIONS.PROJECT_CREATE]: () => openProjectForm(false),
    [ACTIONS.PROJECT_EDIT]: () => openProjectForm(true),
    [ACTIONS.PROJECT_SAVE]: saveProject,
    [ACTIONS.ROUND_CREATE]: openRoundForm, [ACTIONS.ROUND_EDIT]: openRoundForm,
    [ACTIONS.ROUND_SAVE]: saveRound,
    [ACTIONS.STATUS_OPEN]: openProjectStatus, [ACTIONS.ROUND_STATUS_OPEN]: openRoundStatus,
    [ACTIONS.PUBLIC_STATUS]: openPublicStatus, [ACTIONS.STATUS_SAVE]: saveStatus,
    [ACTIONS.PUBLIC_SAVE]: savePublicPage, [ACTIONS.GUIDE_SAVE]: saveGuide,
    [ACTIONS.ACCESS_OPEN]: openAccessibility, [ACTIONS.ACCESS_SAVE]: saveAccessibility,
    [ACTIONS.ACCESS_EDIT]: editAccessibility, [ACTIONS.ACCESS_DELETE]: deleteAccessibility,
    [ACTIONS.NOTICE_OPEN]: (trigger) => openNoticeLink(trigger).catch((error) => showToast(describeError(error))),
    [ACTIONS.NOTICE_SAVE]: saveNoticeLink, [ACTIONS.NOTICE_DELETE]: deleteNoticeLink,
    [ACTIONS.CHECK_STEP]: (trigger) => showSection(trigger.dataset.section || 'overview', true),
});

loadAll();
