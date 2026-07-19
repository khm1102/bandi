import {del, get, patch, post, put} from '../common/api.js';
import {openModal} from '../common/modal.js';
import {showToast} from '../common/toast.js';
import {bindPageActions, element, lookup, readValue} from '../common/dom.js';
import {currentUserRole} from '../common/session.js';
import {activateFilterChip, badge, closeActionModal} from '../common/view.js';

const ACTIONS = Object.freeze({
    CREATE_OPEN: 'production-create-open',
    EDIT_OPEN: 'production-edit-open',
    SAVE: 'production-save',
    STATUS_OPEN: 'production-status-open',
    STATUS_SAVE: 'production-status-save',
    HISTORY: 'production-history',
    DELETE: 'production-delete',
});

const STATUS = Object.freeze({
    TODO: ['할 일', 'neutral'],
    IN_PROGRESS: ['진행 중', 'info'],
    REVIEW_REQUIRED: ['검토 필요', 'warning'],
    BLOCKED: ['차단', 'danger'],
    COMPLETED: ['완료', 'success'],
});

let loginMember = null;
let projects = [];
let teams = [];
let tasks = [];
let editingTask = null;
let statusTask = null;

function selectedProject() {
    const projectId = Number(readValue('productionProject'));
    return projects.find((project) => project.performanceProjectId === projectId);
}

function projectMutable() {
    const project = selectedProject();
    return project && !['ENDED', 'CANCELLED', 'ARCHIVED'].includes(project.status);
}

function canContribute(teamId) {
    return currentUserRole === 'admin' || Number(loginMember?.teamId) === Number(teamId);
}

function canManage(teamId) {
    return currentUserRole === 'admin'
            || (currentUserRole === 'leader'
            && Number(loginMember?.teamId) === Number(teamId));
}

function formatDate(value) {
    if (!value) {
        return '-';
    }
    const [year, month, day] = value.split('-');
    return `${year}.${month}.${day}`;
}

function formatDateTime(value) {
    if (!value) {
        return '-';
    }
    return new Intl.DateTimeFormat('ko-KR', {
        year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit', hour12: false,
    }).format(new Date(value));
}

function currentDate() {
    const now = new Date();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    return `${now.getFullYear()}-${month}-${day}`;
}

function actionButton(label, action, variant = 'outline') {
    const tone = variant === 'danger'
            ? 'border border-destructive/30 bg-card text-destructive hover:bg-destructive-soft'
            : variant === 'primary'
                ? 'bg-primary text-primary-foreground hover:bg-primary-strong hover:text-white'
                : 'border bg-card hover:bg-secondary';
    const button = element('button',
            `inline-flex min-h-11 items-center justify-center rounded-md px-3 text-xs font-bold transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring ${tone}`,
            label);
    button.type = 'button';
    button.dataset.pageAction = action;
    return button;
}

function appendOption(select, value, label) {
    const option = element('option', '', label);
    option.value = String(value);
    select.appendChild(option);
}

function populateReferences() {
    const projectSelect = document.getElementById('productionProject');
    projectSelect.replaceChildren();
    projects.forEach((project) => appendOption(projectSelect,
            project.performanceProjectId,
            `${project.academicYear} ${project.termCode} · ${project.title}`));

    const filter = document.getElementById('productionTeamFilter');
    filter.replaceChildren();
    appendOption(filter, '', '전체 팀');
    teams.forEach((team) => appendOption(filter, team.teamId, team.name));

    const taskTeam = document.getElementById('productionTaskTeam');
    taskTeam.replaceChildren();
    const availableTeams = currentUserRole === 'admin'
            ? teams : teams.filter((team) => team.teamId === loginMember.teamId);
    availableTeams.forEach((team) => appendOption(taskTeam, team.teamId, team.name));
}

function renderProjectState() {
    const region = lookup('[data-production-project-state]');
    const project = selectedProject();
    if (!project) {
        region.replaceChildren(badge('프로젝트 없음', 'warning'));
        return;
    }
    const labels = {
        PLANNING: '기획 중', PRODUCING: '제작 중', RESERVATION_OPEN: '신청 중',
        PERFORMING: '공연 중', ENDED: '공연 종료', CANCELLED: '취소', ARCHIVED: '보관',
    };
    region.replaceChildren(badge(labels[project.status] || project.status,
            projectMutable() ? 'accent' : 'neutral'));
}

function renderSummary(progress) {
    const values = {
        'production-total': progress?.totalCount || 0,
        'production-completed': progress?.completedCount || 0,
        'production-blocked': progress?.blockedCount || 0,
        'production-overdue': progress?.overdueCount || 0,
    };
    Object.entries(values).forEach(([hook, value]) => {
        lookup(`[data-stat-value="${hook}"]`).textContent = String(value);
    });
}

function renderTeamProgress(progressList) {
    const region = lookup('[data-production-progress]');
    region.replaceChildren();
    if (progressList.length === 0) {
        region.appendChild(element('p', 'rounded-md bg-secondary px-4 py-3 text-sm text-muted-foreground md:col-span-2 lg:col-span-3', '등록된 제작 업무가 없습니다.'));
    }
    progressList.forEach((progress) => {
        const percent = progress.totalCount === 0 ? 0
                : Math.round(progress.completedCount / progress.totalCount * 100);
        const card = element('article', 'rounded-lg border p-4');
        const head = element('div', 'flex items-center gap-2');
        head.append(element('h3', 'text-sm font-extrabold', progress.teamName),
                badge(`${percent}%`, percent === 100 ? 'success' : 'neutral'));
        if (progress.blockedCount > 0) {
            head.appendChild(badge(`차단 ${progress.blockedCount}`, 'danger'));
        }
        const track = element('div', 'mt-3 h-2 overflow-hidden rounded-full bg-secondary');
        const fill = element('div', `h-full rounded-full ${percent === 100 ? 'bg-success' : 'bg-primary'}`);
        fill.style.width = `${percent}%`;
        track.appendChild(fill);
        card.append(head, track, element('p', 'mt-2 text-xs text-muted-foreground', `${progress.completedCount}/${progress.totalCount} 완료 · 지연 ${progress.overdueCount}`));
        region.appendChild(card);
    });
    region.setAttribute('aria-busy', 'false');
}

function statusBadge(status) {
    const [label, tone] = STATUS[status] || [status, 'neutral'];
    return badge(label, tone);
}

function buildTaskCard(task) {
    const card = element('article', 'flex min-w-0 flex-col rounded-lg border bg-card p-5');
    const meta = element('div', 'flex flex-wrap items-center gap-2');
    meta.append(badge(task.teamName, 'accent'), statusBadge(task.status));
    if (task.status !== 'COMPLETED' && task.dueDate < currentDate()) {
        meta.appendChild(badge('마감 지연', 'danger'));
    }
    card.appendChild(meta);
    card.appendChild(element('h3', 'mt-3 text-base font-black', task.title));
    if (task.description) {
        card.appendChild(element('p', 'mt-2 whitespace-pre-line text-sm leading-6 text-muted-foreground', task.description));
    }
    card.appendChild(element('p', 'mt-4 text-xs font-bold text-muted-foreground', `${formatDate(task.startDate)} — ${formatDate(task.dueDate)}`));
    if (task.status === 'BLOCKED' && task.blockedReason) {
        const blocked = element('p', 'mt-3 rounded-md bg-destructive-soft px-3 py-2 text-xs font-bold text-destructive', `차단 사유 · ${task.blockedReason}`);
        card.appendChild(blocked);
    }
    const actions = element('div', 'mt-auto flex flex-wrap gap-2 pt-5');
    const history = actionButton('이력', ACTIONS.HISTORY);
    history.dataset.taskId = String(task.productionTaskId);
    history.dataset.taskTitle = task.title;
    actions.appendChild(history);
    if (projectMutable() && canContribute(task.teamId)) {
        const status = actionButton('상태 변경', ACTIONS.STATUS_OPEN, 'primary');
        status.dataset.taskId = String(task.productionTaskId);
        actions.appendChild(status);
    }
    if (projectMutable() && canManage(task.teamId)) {
        const edit = actionButton('수정', ACTIONS.EDIT_OPEN);
        edit.dataset.taskId = String(task.productionTaskId);
        const remove = actionButton('삭제', ACTIONS.DELETE, 'danger');
        remove.dataset.taskId = String(task.productionTaskId);
        remove.dataset.confirm = '이 제작 업무를 삭제할까요?';
        remove.dataset.confirmAction = '업무 삭제';
        actions.append(edit, remove);
    }
    card.appendChild(actions);
    return card;
}

function renderTasks() {
    const region = lookup('[data-production-tasks]');
    region.replaceChildren();
    lookup('[data-production-count]').textContent = `${tasks.length}건`;
    if (tasks.length === 0) {
        region.appendChild(element('p', 'rounded-lg border bg-card px-5 py-12 text-center text-sm text-muted-foreground md:col-span-2', projects.length === 0 ? '먼저 공연 프로젝트를 등록해 주세요.' : '조건에 맞는 제작 업무가 없습니다.'));
    } else {
        tasks.forEach((task) => region.appendChild(buildTaskCard(task)));
    }
    region.setAttribute('aria-busy', 'false');
}

function selectedStatus() {
    return lookup('[data-filter-group="production-status"][aria-pressed="true"]')?.dataset.filterValue || 'ALL';
}

async function loadData() {
    const projectId = Number(readValue('productionProject'));
    renderProjectState();
    if (!projectId) {
        tasks = [];
        renderSummary(null);
        renderTeamProgress([]);
        renderTasks();
        return;
    }
    lookup('[data-production-tasks]').setAttribute('aria-busy', 'true');
    lookup('[data-production-progress]').setAttribute('aria-busy', 'true');
    const query = {
        performanceProjectId: projectId,
        teamId: readValue('productionTeamFilter'),
        status: selectedStatus() === 'ALL' ? null : selectedStatus(),
        overdueOnly: document.getElementById('productionOverdueOnly').checked,
        limit: 100,
    };
    const [nextTasks, progress, teamProgress] = await Promise.all([
        get('/api/production-tasks', query),
        get(`/api/production-tasks/projects/${projectId}/progress`),
        get(`/api/production-tasks/projects/${projectId}/team-progress`),
    ]);
    tasks = nextTasks;
    renderSummary(progress);
    renderTeamProgress(teamProgress);
    renderTasks();
}

async function loadReferences() {
    [loginMember, teams, projects] = await Promise.all([
        get('/api/members/me'),
        get('/api/members/reference/teams'),
        get('/api/performance-management/projects', {limit: 100}),
    ]);
    projects = projects.filter((project) => project.status !== 'CANCELLED');
    populateReferences();
    const addButton = lookup(`[data-page-action="${ACTIONS.CREATE_OPEN}"]`);
    addButton.disabled = projects.length === 0;
    await loadData();
}

function resetTaskForm() {
    lookup('[data-production-form]').reset();
    document.getElementById('productionTaskId').value = '';
    document.getElementById('productionTaskTeam').disabled = false;
    document.getElementById('productionStartDate').value = selectedProject()?.productionStartDate || '';
    document.getElementById('productionDueDate').value = selectedProject()?.productionEndDate || '';
    document.getElementById('productionTaskModalTitle').textContent = '제작 업무 추가';
    editingTask = null;
}

function openCreate() {
    if (!projectMutable()) {
        showToast('종료·취소·보관된 공연에는 업무를 추가할 수 없습니다.');
        return;
    }
    resetTaskForm();
    openModal('productionTaskModal');
}

function openEdit(trigger) {
    editingTask = tasks.find((task) => task.productionTaskId === Number(trigger.dataset.taskId));
    document.getElementById('productionTaskId').value = String(editingTask.productionTaskId);
    document.getElementById('productionTaskTeam').value = String(editingTask.teamId);
    document.getElementById('productionTaskTeam').disabled = true;
    document.getElementById('productionTaskName').value = editingTask.title;
    document.getElementById('productionTaskDescription').value = editingTask.description || '';
    document.getElementById('productionStartDate').value = editingTask.startDate;
    document.getElementById('productionDueDate').value = editingTask.dueDate;
    document.getElementById('productionTaskModalTitle').textContent = '제작 업무 수정';
    openModal('productionTaskModal');
}

async function withBusy(trigger, task) {
    trigger.disabled = true;
    try {
        await task();
    } catch (error) {
        showToast(error.message || '요청을 처리하지 못했습니다.');
    } finally {
        trigger.disabled = false;
    }
}

async function saveTask(trigger) {
    const form = lookup('[data-production-form]');
    if (!form.reportValidity()) {
        return;
    }
    if (readValue('productionDueDate') < readValue('productionStartDate')) {
        showToast('마감일은 시작일보다 빠를 수 없습니다.');
        document.getElementById('productionDueDate').focus();
        return;
    }
    await withBusy(trigger, async () => {
        const body = {
            title: readValue('productionTaskName'),
            description: readValue('productionTaskDescription') || null,
            startDate: readValue('productionStartDate'),
            dueDate: readValue('productionDueDate'),
        };
        if (editingTask) {
            await put(`/api/production-tasks/${editingTask.productionTaskId}`, body);
        } else {
            await post('/api/production-tasks', {
                ...body,
                performanceProjectId: Number(readValue('productionProject')),
                teamId: Number(readValue('productionTaskTeam')),
            });
        }
        closeActionModal(trigger);
        await loadData();
        showToast(editingTask ? '제작 업무를 수정했습니다.' : '제작 업무를 추가했습니다.');
        editingTask = null;
    });
}

function updateBlockedField() {
    const blocked = readValue('productionTaskStatus') === 'BLOCKED';
    lookup('[data-production-blocked-field]').hidden = !blocked;
    document.getElementById('productionBlockedReason').required = blocked;
}

function openStatus(trigger) {
    statusTask = tasks.find((task) => task.productionTaskId === Number(trigger.dataset.taskId));
    lookup('[data-production-status-form]').reset();
    lookup('[data-production-status-task]').textContent = statusTask.title;
    document.getElementById('productionTaskStatus').value = statusTask.status;
    document.getElementById('productionBlockedReason').value = statusTask.blockedReason || '';
    updateBlockedField();
    openModal('productionStatusModal');
}

async function saveStatus(trigger) {
    const form = lookup('[data-production-status-form]');
    if (!form.reportValidity()) {
        return;
    }
    const status = readValue('productionTaskStatus');
    if (status === statusTask.status) {
        showToast('현재와 다른 상태를 선택해 주세요.');
        return;
    }
    await withBusy(trigger, async () => {
        await patch(`/api/production-tasks/${statusTask.productionTaskId}/status`, {
            status,
            blockedReason: status === 'BLOCKED' ? readValue('productionBlockedReason') : null,
            comment: readValue('productionStatusComment') || null,
        });
        closeActionModal(trigger);
        await loadData();
        showToast('제작 업무 상태를 변경했습니다.');
    });
}

async function deleteTask(trigger) {
    await withBusy(trigger, async () => {
        await del(`/api/production-tasks/${trigger.dataset.taskId}`);
        await loadData();
        showToast('제작 업무를 삭제했습니다.');
    });
}

async function showHistory(trigger) {
    openModal('productionHistoryModal');
    const region = lookup('[data-production-history]');
    region.replaceChildren(element('p', 'py-8 text-center text-sm text-muted-foreground', '이력을 불러오는 중입니다.'));
    try {
        const histories = await get(`/api/production-tasks/${trigger.dataset.taskId}/histories`);
        region.replaceChildren(element('strong', 'mb-2 text-sm', trigger.dataset.taskTitle));
        if (histories.length === 0) {
            region.appendChild(element('p', 'rounded-md bg-secondary px-4 py-3 text-sm text-muted-foreground', '상태 변경 이력이 없습니다.'));
        }
        histories.forEach((history) => {
            const row = element('div', 'rounded-lg border px-4 py-3');
            const head = element('div', 'flex flex-wrap items-center gap-2');
            head.append(statusBadge(history.previousStatus), element('span', 'text-xs text-muted-foreground', '→'), statusBadge(history.newStatus));
            row.append(head, element('p', 'mt-2 text-xs text-muted-foreground', `${history.changedByName || `부원 #${history.changedByMemberId}`} · ${formatDateTime(history.changedDttm)}`));
            if (history.comment) {
                row.appendChild(element('p', 'mt-2 text-sm', history.comment));
            }
            region.appendChild(row);
        });
    } catch (error) {
        region.replaceChildren(element('p', 'rounded-md bg-destructive-soft px-4 py-3 text-sm text-destructive', error.message || '이력을 불러오지 못했습니다.'));
    }
}

document.getElementById('productionProject').addEventListener('change', () => loadData().catch((error) => showToast(error.message)));
document.getElementById('productionTeamFilter').addEventListener('change', () => loadData().catch((error) => showToast(error.message)));
document.getElementById('productionOverdueOnly').addEventListener('change', () => loadData().catch((error) => showToast(error.message)));
document.getElementById('productionTaskStatus').addEventListener('change', updateBlockedField);
document.addEventListener('click', (event) => {
    const chip = event.target.closest('[data-filter-group="production-status"]');
    if (chip) {
        activateFilterChip(chip);
        loadData().catch((error) => showToast(error.message));
    }
});

bindPageActions({
    [ACTIONS.CREATE_OPEN]: openCreate,
    [ACTIONS.EDIT_OPEN]: openEdit,
    [ACTIONS.SAVE]: saveTask,
    [ACTIONS.STATUS_OPEN]: openStatus,
    [ACTIONS.STATUS_SAVE]: saveStatus,
    [ACTIONS.HISTORY]: showHistory,
    [ACTIONS.DELETE]: deleteTask,
});

loadReferences().catch((error) => {
    showToast(error.message || '제작 진행 정보를 불러오지 못했습니다.');
    tasks = [];
    renderSummary(null);
    renderTeamProgress([]);
    renderTasks();
});
