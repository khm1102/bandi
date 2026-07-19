import {del, get, patch, post, put} from '../common/api.js';
import {closeSheetOf, openSheet} from '../common/sheet.js';
import {showToast} from '../common/toast.js';
import {bindPageActions, element, lookup, readValue} from '../common/dom.js';
import {currentUserRole} from '../common/session.js';
import {activateFilterChip, badge} from '../common/view.js';

const ACTIONS = Object.freeze({
    CREATE_OPEN: 'checklist-create-open',
    EDIT_OPEN: 'checklist-edit-open',
    SAVE: 'checklist-save',
    TOGGLE: 'checklist-toggle',
    DELETE: 'checklist-delete',
    HISTORY: 'checklist-history',
});

const canManage = currentUserRole === 'admin' || currentUserRole === 'leader';
let loginMember = null;
let projects = [];
let rounds = [];
let teams = [];
let items = [];
let editingItem = null;

function projectMutable() {
    const selectedId = Number(readValue('checkProject'));
    const project = projects.find((candidate) => candidate.performanceProjectId === selectedId);
    return project && !['ENDED', 'CANCELLED', 'ARCHIVED'].includes(project.status);
}

function formatDateTime(value) {
    if (!value) {
        return '-';
    }
    return new Intl.DateTimeFormat('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,
    }).format(new Date(value));
}

function roundLabel(round) {
    return `${round.roundNo}회차 · ${formatDateTime(round.startDttm)}`;
}

function populateSelect(select, values, valueKey, labelBuilder) {
    select.replaceChildren();
    values.forEach((value) => {
        const option = element('option', '', labelBuilder(value));
        option.value = String(value[valueKey]);
        select.appendChild(option);
    });
}

function canManageItem(item) {
    return projectMutable() && (currentUserRole === 'admin'
            || (currentUserRole === 'leader'
            && item.teamId === loginMember?.teamId));
}

function canCompleteItem(item) {
    return projectMutable()
            && (currentUserRole === 'admin'
            || item.teamId === loginMember?.teamId);
}

function actionButton(label, action, variant = 'outline') {
    const tone = variant === 'primary'
            ? 'bg-primary text-primary-foreground hover:bg-primary-strong hover:text-white'
            : variant === 'danger'
                ? 'border border-destructive/30 bg-card text-destructive hover:bg-destructive-soft'
                : 'border bg-card hover:bg-secondary';
    const button = element('button', `inline-flex min-h-11 items-center justify-center rounded-md px-3 text-xs font-bold transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring ${tone}`, label);
    button.type = 'button';
    button.dataset.pageAction = action;
    return button;
}

function filteredItems() {
    const selected = lookup('[data-filter-group="checklist-status"][aria-pressed="true"]');
    const status = selected?.dataset.filterValue || 'ALL';
    const requiredOnly = lookup('[data-required-only]').checked;
    return items.filter((item) => {
        const statusMatched = status === 'ALL'
                || (status === 'COMPLETED' && item.completed)
                || (status === 'PENDING' && !item.completed);
        return statusMatched && (!requiredOnly || item.required);
    });
}

function updateSummary() {
    const completed = items.filter((item) => item.completed).length;
    const percent = items.length === 0
            ? 0
            : Math.round(completed / items.length * 100);
    lookup('[data-checklist-count]').textContent = `${completed} / ${items.length}`;
    const progress = lookup('[data-checklist-progress]');
    progress.max = items.length || 1;
    progress.value = completed;
    progress.textContent = `${percent}%`;
    lookup('[data-checklist-summary]').replaceChildren(
            badge(`준비 ${percent}%`, percent === 100 ? 'success' : 'warning'));
    updateNextAction();
}

function updateNextAction() {
    const title = lookup('[data-checklist-next-title]');
    const message = lookup('[data-checklist-next-message]');
    const action = lookup('[data-checklist-next-action]');
    const button = lookup('[data-page-action="checklist-toggle"]', action);
    const pending = items.filter((item) => !item.completed)
            .sort((first, second) => Number(second.required) - Number(first.required));
    const actionable = pending.find(canCompleteItem);
    action.classList.toggle('hidden', !actionable);
    if (actionable) {
        title.textContent = actionable.content;
        message.textContent = `${actionable.teamName || '담당 팀'}의 ${actionable.required ? '필수 ' : ''}준비 항목이에요.`;
        button.dataset.itemId = String(actionable.checklistItemId);
        return;
    }
    button.removeAttribute('data-item-id');
    if (items.length === 0) {
        title.textContent = '아직 등록된 준비 항목이 없어요';
        message.textContent = canManage
                ? '첫 준비 항목을 추가해 공연 준비를 시작해 주세요.'
                : '운영진이 준비 항목을 등록하면 이곳에 표시돼요.';
        return;
    }
    if (pending.length > 0) {
        title.textContent = '내가 바로 처리할 항목은 없어요';
        message.textContent = '다른 팀의 미완료 항목은 아래 목록에서 진행 상태를 확인할 수 있어요.';
        return;
    }
    title.textContent = '모든 준비 항목을 완료했어요';
    message.textContent = '변경 사항이 생기면 완료 상태를 다시 조정할 수 있어요.';
}

function buildChecklistItem(item) {
    const row = element('li', 'grid grid-cols-[auto_minmax(0,1fr)] gap-x-3 gap-y-2 px-4 py-4 md:px-5');
    const toggle = element('button', `flex size-11 shrink-0 items-center justify-center rounded-md border text-lg font-black transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring ${item.completed ? 'border-success bg-success text-white' : 'bg-card text-muted-foreground hover:border-primary'}`, item.completed ? '✓' : '');
    toggle.type = 'button';
    toggle.dataset.pageAction = ACTIONS.TOGGLE;
    toggle.dataset.itemId = String(item.checklistItemId);
    toggle.setAttribute('aria-pressed', String(item.completed));
    toggle.setAttribute('aria-label', `${item.content} ${item.completed ? '미완료로 변경' : '완료로 변경'}`);
    toggle.disabled = !canCompleteItem(item);
    if (!canCompleteItem(item)) {
        toggle.title = '소속 팀 항목만 완료 처리할 수 있습니다.';
    }

    const body = element('div', 'min-w-0 flex-1 pt-1');
    const title = element('strong', `block text-sm ${item.completed ? 'text-muted-foreground line-through' : ''}`, item.content);
    body.appendChild(title);
    const meta = element('div', 'mt-1.5 flex flex-wrap items-center gap-2 text-xs text-muted-foreground');
    if (item.required) {
        meta.appendChild(badge('필수', 'warning'));
    }
    meta.appendChild(element('span', '', item.scope === 'ROUND' ? '회차별 준비' : '프로젝트 공통'));
    if (item.completed) {
        meta.appendChild(element('span', '', `${item.completedByMemberName || '부원'} · ${formatDateTime(item.completedDttm)}`));
    }
    body.appendChild(meta);

    const actions = element('div', 'col-start-2 flex flex-wrap gap-2');
    const history = actionButton('이력', ACTIONS.HISTORY);
    history.dataset.itemId = String(item.checklistItemId);
    history.dataset.itemContent = item.content;
    actions.appendChild(history);
    if (canManageItem(item)) {
        const edit = actionButton('수정', ACTIONS.EDIT_OPEN);
        edit.dataset.itemId = String(item.checklistItemId);
        const remove = actionButton('삭제', ACTIONS.DELETE, 'danger');
        remove.dataset.itemId = String(item.checklistItemId);
        remove.dataset.confirm = '이 체크리스트 항목을 삭제할까요?';
        remove.dataset.confirmAction = '항목 삭제';
        actions.append(edit, remove);
    }
    row.append(toggle, body, actions);
    return row;
}

function buildTeamCard(teamItems) {
    const completed = teamItems.filter((item) => item.completed).length;
    const team = teams.find((candidate) => candidate.teamId === teamItems[0].teamId);
    const card = element('section', 'py-2');
    const header = element('header', 'flex items-center gap-3 px-4 py-3 md:px-5');
    header.appendChild(element('h2', 'text-base font-bold', team?.name || teamItems[0].teamName));
    header.appendChild(badge(`${completed}/${teamItems.length}`, completed === teamItems.length ? 'success' : 'neutral'));
    if (canManageItem(teamItems[0])) {
        const add = actionButton('항목 추가', ACTIONS.CREATE_OPEN);
        add.dataset.teamId = String(teamItems[0].teamId);
        add.classList.add('ml-auto');
        header.appendChild(add);
    }
    const list = element('ul', 'divide-y');
    teamItems.forEach((item) => list.appendChild(buildChecklistItem(item)));
    card.append(header, list);
    return card;
}

function renderItems() {
    const region = lookup('[data-checklist-region]');
    const visible = filteredItems();
    region.replaceChildren();
    if (visible.length === 0) {
        const message = element('div', 'px-5 py-12 text-center text-sm text-muted-foreground', items.length === 0
                ? '이 범위에 등록된 체크리스트가 없습니다.'
                : '필터 조건에 맞는 체크리스트가 없습니다.');
        region.appendChild(message);
    } else {
        const grouped = new Map();
        visible.forEach((item) => {
            const teamItems = grouped.get(item.teamId) || [];
            teamItems.push(item);
            grouped.set(item.teamId, teamItems);
        });
        grouped.values().forEach((teamItems) => {
            region.appendChild(buildTeamCard(teamItems));
        });
    }
    region.setAttribute('aria-busy', 'false');
    lookup('[data-checklist-status-message]').textContent =
            `체크리스트 ${visible.length}개를 표시했어요.`;
}

async function loadRounds(projectId) {
    rounds = projectId
            ? await get(`/api/performance-management/projects/${projectId}/rounds`)
            : [];
    populateSelect(document.getElementById('checkRound'), rounds,
            'performanceRoundId', roundLabel);
    populateSelect(document.getElementById('checkItemRound'), rounds,
            'performanceRoundId', roundLabel);
}

async function loadItems() {
    const projectId = Number(readValue('checkProject'));
    if (!projectId) {
        items = [];
        updateSummary();
        renderItems();
        return;
    }
    const scope = readValue('checkScope');
    const query = {
        performanceProjectId: projectId,
        scope,
    };
    if (scope === 'ROUND') {
        query.performanceRoundId = Number(readValue('checkRound'));
        if (!query.performanceRoundId) {
            items = [];
            updateSummary();
            renderItems();
            return;
        }
    }
    lookup('[data-checklist-region]').setAttribute('aria-busy', 'true');
    items = await get('/api/checklist-items', query);
    updateSummary();
    renderItems();
}

async function loadReferences() {
    [loginMember, teams, projects] = await Promise.all([
        get('/api/members/me'),
        get('/api/members/reference/teams'),
        get('/api/performance-management/projects', {limit: 100}),
    ]);
    projects = projects.filter((project) => project.status !== 'CANCELLED');
    populateSelect(document.getElementById('checkProject'), projects,
            'performanceProjectId',
            (project) => `${project.academicYear} ${project.termCode} · ${project.title}`);
    const manageableTeams = currentUserRole === 'admin'
            ? teams
            : teams.filter((team) => team.teamId === loginMember.teamId);
    populateSelect(document.getElementById('checkTeam'), manageableTeams,
            'teamId', (team) => team.name);
    await loadRounds(Number(readValue('checkProject')));
    await loadItems();
}

function updateScopeFields() {
    const roundScope = readValue('checkScope') === 'ROUND';
    lookup('[data-check-round-filter]').hidden = !roundScope;
    document.getElementById('checkRound').required = roundScope;
}

function updateItemScopeFields() {
    const roundScope = readValue('checkItemScope') === 'ROUND';
    lookup('[data-check-item-round-field]').hidden = !roundScope;
    document.getElementById('checkItemRound').required = roundScope;
}

function resetForm(teamId) {
    const form = lookup('[data-checklist-form]');
    form.reset();
    document.getElementById('checkItemId').value = '';
    document.getElementById('checkDisplayOrder').value = String(items.length);
    document.getElementById('checkTeam').disabled = false;
    document.getElementById('checkItemScope').disabled = false;
    if (teamId) {
        document.getElementById('checkTeam').value = String(teamId);
    }
    document.getElementById('checkItemScope').value = readValue('checkScope');
    document.getElementById('checkItemRound').value = readValue('checkRound');
    updateItemScopeFields();
    document.getElementById('checkSheetTitle').textContent = '체크리스트 항목 추가';
    editingItem = null;
}

function openCreate(trigger) {
    if (!projectMutable()) {
        showToast('종료·보관된 공연의 체크리스트는 변경할 수 없습니다.');
        return;
    }
    resetForm(trigger.dataset.teamId ? Number(trigger.dataset.teamId) : null);
    openSheet('checkSheet', trigger);
}

function openEdit(trigger) {
    const itemId = Number(trigger.dataset.itemId);
    editingItem = items.find((item) => item.checklistItemId === itemId);
    document.getElementById('checkItemId').value = String(itemId);
    document.getElementById('checkTeam').value = String(editingItem.teamId);
    document.getElementById('checkTeam').disabled = true;
    document.getElementById('checkItemScope').value = editingItem.scope;
    document.getElementById('checkItemScope').disabled = true;
    document.getElementById('checkItemRound').value = editingItem.performanceRoundId || '';
    document.getElementById('checkContent').value = editingItem.content;
    document.getElementById('checkDisplayOrder').value = String(editingItem.displayOrder);
    document.getElementById('checkRequired').checked = editingItem.required;
    updateItemScopeFields();
    document.getElementById('checkSheetTitle').textContent = '체크리스트 항목 수정';
    openSheet('checkSheet', trigger);
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

async function saveItem(trigger) {
    const form = lookup('[data-checklist-form]');
    if (!form.reportValidity()) {
        return;
    }
    await withBusy(trigger, async () => {
        const body = {
            content: readValue('checkContent'),
            required: document.getElementById('checkRequired').checked,
            displayOrder: Number(readValue('checkDisplayOrder')),
        };
        if (editingItem) {
            await put(`/api/checklist-items/${editingItem.checklistItemId}`, body);
        } else {
            const scope = readValue('checkItemScope');
            await post('/api/checklist-items', {
                ...body,
                performanceProjectId: Number(readValue('checkProject')),
                performanceRoundId: scope === 'ROUND'
                        ? Number(readValue('checkItemRound')) : null,
                teamId: Number(readValue('checkTeam')),
                scope,
            });
        }
        closeSheetOf(trigger);
        await loadItems();
        showToast(editingItem ? '체크리스트 항목을 수정했습니다.' : '체크리스트 항목을 추가했습니다.');
        editingItem = null;
    });
}

async function toggleItem(trigger) {
    const itemId = Number(trigger.dataset.itemId);
    const item = items.find((candidate) => candidate.checklistItemId === itemId);
    await withBusy(trigger, async () => {
        await patch(`/api/checklist-items/${itemId}/completion`, {
            completed: !item.completed,
            reason: null,
        });
        await loadItems();
        showToast(item.completed ? '미완료로 변경했습니다.' : '완료로 기록했습니다.');
    });
}

async function deleteItem(trigger) {
    await withBusy(trigger, async () => {
        await del(`/api/checklist-items/${trigger.dataset.itemId}`);
        await loadItems();
        showToast('체크리스트 항목을 삭제했습니다.');
    });
}

async function showHistory(trigger) {
    openSheet('checkHistorySheet', trigger);
    const region = lookup('[data-check-history]');
    region.replaceChildren(element('p', 'py-8 text-center text-sm text-muted-foreground', '이력을 불러오는 중입니다.'));
    try {
        const histories = await get(`/api/checklist-items/${trigger.dataset.itemId}/histories`);
        region.replaceChildren();
        region.appendChild(element('strong', 'mb-2 text-sm', trigger.dataset.itemContent));
        if (histories.length === 0) {
            region.appendChild(element('p', 'rounded-lg bg-secondary px-4 py-3 text-sm text-muted-foreground', '완료 상태 변경 이력이 없습니다.'));
            return;
        }
        histories.forEach((history) => {
            const row = element('div', 'flex items-start gap-3 border-b py-3 last:border-b-0');
            row.appendChild(badge(history.newCompleted ? '완료' : '미완료', history.newCompleted ? 'success' : 'warning'));
            const body = element('div', 'min-w-0 flex-1');
            body.appendChild(element('strong', 'block text-sm', history.changedByMemberName || `부원 #${history.changedByMemberId}`));
            body.appendChild(element('span', 'mt-1 block text-xs text-muted-foreground', formatDateTime(history.changedDttm)));
            if (history.reason) {
                body.appendChild(element('p', 'mt-2 text-sm', history.reason));
            }
            row.appendChild(body);
            region.appendChild(row);
        });
    } catch (error) {
        region.replaceChildren(element('p', 'rounded-lg bg-destructive-soft px-4 py-3 text-sm text-destructive', error.message || '이력을 불러오지 못했습니다.'));
    }
}

document.getElementById('checkProject').addEventListener('change', async () => {
    try {
        await loadRounds(Number(readValue('checkProject')));
        await loadItems();
    } catch (error) {
        showToast(error.message || '공연 정보를 불러오지 못했습니다.');
    }
});
document.getElementById('checkScope').addEventListener('change', async () => {
    updateScopeFields();
    await loadItems().catch((error) => showToast(error.message));
});
document.getElementById('checkRound').addEventListener('change', () => {
    loadItems().catch((error) => showToast(error.message));
});
document.getElementById('checkItemScope')?.addEventListener('change', updateItemScopeFields);
lookup('[data-required-only]').addEventListener('change', renderItems);
document.addEventListener('click', (event) => {
    const filter = event.target.closest('[data-filter-group="checklist-status"]');
    if (filter) {
        activateFilterChip(filter);
        renderItems();
    }
});

bindPageActions({
    [ACTIONS.CREATE_OPEN]: openCreate,
    [ACTIONS.EDIT_OPEN]: openEdit,
    [ACTIONS.SAVE]: saveItem,
    [ACTIONS.TOGGLE]: toggleItem,
    [ACTIONS.DELETE]: deleteItem,
    [ACTIONS.HISTORY]: showHistory,
});

updateScopeFields();
loadReferences().catch((error) => {
    lookup('[data-checklist-region]').setAttribute('aria-busy', 'false');
    lookup('[data-checklist-region]').replaceChildren(element('div', 'rounded-lg border bg-card px-5 py-11 text-center text-sm text-destructive md:col-span-2', '체크리스트를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.'));
    showToast(error.message || '체크리스트를 불러오지 못했습니다.');
});
