import {get, post, put} from '../common/api.js';
import {all, bindPageActions, element, lookup, readValue} from '../common/dom.js';
import {openModal} from '../common/modal.js';
import {currentUserRole} from '../common/session.js';
import {showToast} from '../common/toast.js';
import {activateFilterChip, badge, closeActionModal} from '../common/view.js';

const ACTIONS = Object.freeze({
    RETRY: 'event-retry',
    CREATE_OPEN: 'event-create-open',
    EDIT: 'event-edit',
    SAVE: 'event-save',
    TARGET_OPEN: 'event-target-open',
    TARGET_CONFIRM: 'event-target-confirm',
    CHECK_IN_OPEN: 'event-checkin-open',
    CHECK_IN_CLOSE: 'event-checkin-close',
    ROSTER_OPEN: 'event-roster-open',
    ATTENDANCE_PROCESS: 'attendance-process',
    ARCHIVE: 'event-archive',
});

const STATUS_META = Object.freeze({
    DRAFT: ['초안', 'neutral'],
    SCHEDULED: ['예정', 'info'],
    IN_PROGRESS: ['출석 확인 중', 'accent'],
    CLOSED: ['종료', 'warning'],
    ARCHIVED: ['보관', 'neutral'],
});

const ATTENDANCE_META = Object.freeze({
    PENDING: ['미처리', 'warning'],
    PRESENT: ['출석', 'success'],
    LATE: ['지각', 'warning'],
    ABSENT: ['결석', 'danger'],
    EXCUSED: ['공결', 'info'],
});

const SCOPE_LABELS = Object.freeze({
    ALL: '전체 활성 멤버',
    TEAM: '특정 팀',
    SELECTED: '선택한 멤버',
});

const canManage = currentUserRole === 'admin';
let clubEvents = [];
let myAttendances = new Map();
let teams = [];
let members = [];
let activeFilter = 'ALL';
let editingEvent = null;
let targetEvent = null;
let rosterEvent = null;

function errorMessage(error) {
    return error?.message || '요청을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요.';
}

function formatDateTime(value) {
    if (!value) {
        return '—';
    }
    const date = new Date(value);
    return new Intl.DateTimeFormat('ko-KR', {
        month: 'short',
        day: 'numeric',
        weekday: 'short',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,
    }).format(date);
}

function formatProcessedTime(value) {
    if (!value) {
        return '';
    }
    return new Intl.DateTimeFormat('ko-KR', {
        month: 'numeric',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,
    }).format(new Date(value));
}

function toDateTimeInput(value) {
    return value ? value.slice(0, 16) : '';
}

function toLocalDateTime(value) {
    return value ? `${value}:00` : null;
}

function initials(name) {
    return Array.from(name || '?').slice(0, 2).join('');
}

function setText(selector, value, root = document) {
    const node = lookup(selector, root);
    if (node) {
        node.textContent = value || '';
    }
}

function setError(selector, message) {
    const node = lookup(selector);
    node.textContent = message || '';
    node.classList.toggle('hidden', !message);
}

function setListState(title, message, retry = false) {
    const state = lookup('[data-event-state]');
    state.classList.remove('hidden');
    lookup('[data-event-list]').classList.add('hidden');
    setText('[data-event-state-title]', title, state);
    setText('[data-event-state-message]', message, state);
    lookup('[data-event-retry]', state).classList.toggle('hidden', !retry);
}

function createStatusBadge(status, attendance = false) {
    const meta = attendance ? ATTENDANCE_META[status] : STATUS_META[status];
    const [label, tone] = meta || [status || '미정', 'neutral'];
    return badge(label, tone);
}

function targetLabel(event) {
    if (event.targetScope === 'TEAM') {
        return `${event.teamName || '팀 미정'} · ${event.targetCount}명`;
    }
    return `${SCOPE_LABELS[event.targetScope] || event.targetScope} · ${event.targetCount}명`;
}

function attendanceWindow(event) {
    return `${formatDateTime(event.checkInStartDttm)} ~ ${formatDateTime(event.checkInEndDttm)}`;
}

function showCardAction(card, role, visible) {
    const button = lookup(`[data-role="${role}"]`, card);
    if (button) {
        button.classList.toggle('hidden', !visible);
    }
}

function configureAdminActions(card, event) {
    if (!canManage) {
        return;
    }
    const actions = lookup('[data-event-admin-actions]', card);
    actions.classList.remove('hidden');
    actions.classList.add('flex');
    showCardAction(card, 'edit', event.status === 'DRAFT');
    showCardAction(card, 'target', event.status === 'DRAFT');
    showCardAction(card, 'open', event.status === 'SCHEDULED' || event.status === 'CLOSED');
    showCardAction(card, 'close', event.status === 'IN_PROGRESS');
    showCardAction(card, 'roster', event.status !== 'DRAFT');
    showCardAction(card, 'archive', event.status !== 'ARCHIVED');
}

function renderMyAttendance(card, eventId) {
    const attendance = myAttendances.get(eventId);
    if (!attendance) {
        return;
    }
    const panel = lookup('[data-my-attendance]', card);
    panel.classList.remove('hidden');
    lookup('[data-my-attendance-status]', panel).appendChild(
            createStatusBadge(attendance.status, true));
    setText('[data-my-attendance-time]', formatProcessedTime(attendance.processedDttm), panel);
    const reason = lookup('[data-my-attendance-reason]', panel);
    reason.textContent = attendance.reason ? `사유: ${attendance.reason}` : '';
    reason.classList.toggle('hidden', !attendance.reason);
}

function createEventCard(event) {
    const template = lookup('[data-event-card-template]');
    const card = template.content.firstElementChild.cloneNode(true);
    card.dataset.eventId = event.clubEventId;
    setText('[data-event-title]', event.title, card);
    lookup('[data-event-status]', card).appendChild(createStatusBadge(event.status));
    setText('[data-event-schedule]',
            `${formatDateTime(event.startDttm)} · ${event.place}`, card);
    const description = lookup('[data-event-description]', card);
    description.textContent = event.description || '';
    description.classList.toggle('hidden', !event.description);
    setText('[data-event-target]', targetLabel(event), card);
    setText('[data-event-checkin-window]', attendanceWindow(event), card);
    renderMyAttendance(card, event.clubEventId);
    configureAdminActions(card, event);
    return card;
}

function visibleEvents() {
    const roleVisible = canManage
        ? clubEvents : clubEvents.filter((event) => event.status !== 'DRAFT');
    if (activeFilter === 'ALL') {
        return roleVisible;
    }
    return roleVisible.filter((event) => event.status === activeFilter);
}

function renderStats() {
    const visible = canManage
        ? clubEvents : clubEvents.filter((event) => event.status !== 'DRAFT');
    const activeEventIds = new Set(visible.filter((event) =>
        event.status !== 'ARCHIVED').map((event) => event.clubEventId));
    setText('[data-stat-value="event-total"]', visible.length);
    setText('[data-stat-value="event-scheduled"]',
            visible.filter((event) => event.status === 'SCHEDULED').length);
    setText('[data-stat-value="event-progress"]',
            visible.filter((event) => event.status === 'IN_PROGRESS').length);
    setText('[data-stat-value="attendance-pending"]',
            Array.from(myAttendances.values()).filter((attendance) =>
                attendance.status === 'PENDING'
                && activeEventIds.has(attendance.clubEventId)).length);
}

function renderEvents() {
    renderStats();
    const list = lookup('[data-event-list]');
    list.replaceChildren();
    const visible = visibleEvents();
    if (visible.length === 0) {
        const message = activeFilter === 'ALL'
            ? '등록된 행사가 없습니다.' : '선택한 상태의 행사가 없습니다.';
        setListState('표시할 행사가 없습니다', message);
        return;
    }
    visible.forEach((event) => list.appendChild(createEventCard(event)));
    lookup('[data-event-state]').classList.add('hidden');
    list.classList.remove('hidden');
    list.classList.add('grid');
}

async function loadReferences() {
    if (!canManage) {
        return;
    }
    const [teamItems, memberItems] = await Promise.all([
        get('/api/members/reference/teams'),
        get('/api/members', {status: 'ACTIVE'}),
    ]);
    teams = teamItems;
    members = memberItems;
    renderTeamOptions();
}

async function loadEvents() {
    setListState('행사를 불러오는 중입니다', '잠시만 기다려 주세요.');
    try {
        const [eventItems, attendanceItems] = await Promise.all([
            get('/api/events', {limit: 100}),
            get('/api/events/my-attendances'),
        ]);
        clubEvents = eventItems;
        myAttendances = new Map(attendanceItems.map((attendance) =>
            [attendance.clubEventId, attendance]));
        renderEvents();
    } catch (error) {
        setListState('행사를 불러오지 못했습니다', errorMessage(error), true);
    }
}

function renderTeamOptions() {
    const select = document.getElementById('eventTeam');
    if (!select) {
        return;
    }
    select.replaceChildren(element('option', '', '팀을 선택해 주세요'));
    select.firstElementChild.value = '';
    teams.forEach((team) => {
        const option = element('option', '', team.name);
        option.value = team.teamId;
        select.appendChild(option);
    });
}

function setDefaultDates() {
    const start = new Date();
    start.setMinutes(0, 0, 0);
    start.setHours(start.getHours() + 1);
    const end = new Date(start);
    end.setHours(end.getHours() + 2);
    const checkInStart = new Date(start);
    checkInStart.setMinutes(checkInStart.getMinutes() - 30);
    const checkInEnd = new Date(start);
    checkInEnd.setMinutes(checkInEnd.getMinutes() + 30);
    document.getElementById('eventStart').value = localInputValue(start);
    document.getElementById('eventEnd').value = localInputValue(end);
    document.getElementById('checkInStart').value = localInputValue(checkInStart);
    document.getElementById('checkInEnd').value = localInputValue(checkInEnd);
}

function localInputValue(date) {
    const offset = date.getTimezoneOffset() * 60000;
    return new Date(date.getTime() - offset).toISOString().slice(0, 16);
}

function updateTargetScopeFields() {
    const scope = readValue('eventTargetScope');
    lookup('[data-event-team-field]').classList.toggle('hidden', scope !== 'TEAM');
    const help = {
        ALL: '저장 후 전체 활성 멤버를 대상으로 확정합니다.',
        TEAM: '저장 후 선택한 팀의 활성 멤버를 대상으로 확정합니다.',
        SELECTED: '저장 후 대상 확정 단계에서 활성 멤버를 직접 선택합니다.',
    };
    setText('[data-event-target-help]', help[scope]);
}

function resetEventForm() {
    editingEvent = null;
    document.getElementById('eventTargetScope').value = 'ALL';
    document.getElementById('eventTeam').value = '';
    document.getElementById('eventTitle').value = '';
    document.getElementById('eventDescription').value = '';
    document.getElementById('eventPlace').value = '';
    setDefaultDates();
    updateTargetScopeFields();
    setText('#eventModalTitle', '행사 생성');
    setText('[data-event-save-label]', '행사 저장');
    setError('[data-event-form-error]', '');
}

function openCreateModal(trigger) {
    resetEventForm();
    openModal('eventModal', trigger);
}

function lookupEventFromTrigger(trigger) {
    const card = trigger.closest('[data-event-card]');
    return clubEvents.find((event) =>
        event.clubEventId === Number(card?.dataset.eventId));
}

function openEditModal(trigger) {
    const event = lookupEventFromTrigger(trigger);
    if (!event) {
        showToast('수정할 행사를 찾을 수 없습니다.');
        return;
    }
    editingEvent = event;
    document.getElementById('eventTargetScope').value = event.targetScope;
    document.getElementById('eventTeam').value = event.teamId || '';
    document.getElementById('eventTitle').value = event.title;
    document.getElementById('eventDescription').value = event.description || '';
    document.getElementById('eventPlace').value = event.place;
    document.getElementById('eventStart').value = toDateTimeInput(event.startDttm);
    document.getElementById('eventEnd').value = toDateTimeInput(event.endDttm);
    document.getElementById('checkInStart').value = toDateTimeInput(event.checkInStartDttm);
    document.getElementById('checkInEnd').value = toDateTimeInput(event.checkInEndDttm);
    updateTargetScopeFields();
    setText('#eventModalTitle', '행사 초안 수정');
    setText('[data-event-save-label]', '수정 저장');
    setError('[data-event-form-error]', '');
    openModal('eventModal', trigger);
}

function eventPayload() {
    const scope = readValue('eventTargetScope');
    return {
        targetScope: scope,
        teamId: scope === 'TEAM' ? Number(readValue('eventTeam')) : null,
        title: readValue('eventTitle'),
        description: readValue('eventDescription') || null,
        place: readValue('eventPlace'),
        startDttm: toLocalDateTime(readValue('eventStart')),
        endDttm: toLocalDateTime(readValue('eventEnd')),
        checkInStartDttm: toLocalDateTime(readValue('checkInStart')),
        checkInEndDttm: toLocalDateTime(readValue('checkInEnd')),
    };
}

function validateEventPayload(payload) {
    if (!payload.title || !payload.place || !payload.startDttm
            || !payload.endDttm || !payload.checkInStartDttm
            || !payload.checkInEndDttm) {
        return '필수 항목을 모두 입력해 주세요.';
    }
    if (payload.targetScope === 'TEAM' && !payload.teamId) {
        return '참석 대상 팀을 선택해 주세요.';
    }
    if (payload.endDttm <= payload.startDttm) {
        return '행사 종료 시간은 시작 시간보다 뒤여야 합니다.';
    }
    if (payload.checkInEndDttm <= payload.checkInStartDttm) {
        return '출석 확인 종료 시간은 시작 시간보다 뒤여야 합니다.';
    }
    return '';
}

async function saveEvent(trigger) {
    const payload = eventPayload();
    const validationMessage = validateEventPayload(payload);
    setError('[data-event-form-error]', validationMessage);
    if (validationMessage) {
        return;
    }
    trigger.disabled = true;
    try {
        if (editingEvent) {
            await put(`/api/event-management/${editingEvent.clubEventId}`, payload);
            showToast('행사 초안을 수정했습니다.');
        } else {
            await post('/api/event-management', payload);
            showToast('행사를 저장했습니다. 참석 대상을 확정해 주세요.');
        }
        closeActionModal(trigger);
        await loadEvents();
    } catch (error) {
        setError('[data-event-form-error]', errorMessage(error));
    } finally {
        trigger.disabled = false;
    }
}

function renderTargetMembers() {
    const list = lookup('[data-target-member-list]');
    list.replaceChildren();
    lookup('[data-target-member-empty]').classList.toggle('hidden', members.length > 0);
    members.forEach((member) => {
        const row = lookup('[data-target-member-template]').content.firstElementChild.cloneNode(true);
        const checkbox = lookup('[data-target-member]', row);
        checkbox.value = member.memberId;
        checkbox.setAttribute('aria-label', `${member.name} 선택`);
        setText('[data-target-member-avatar]', initials(member.name), row);
        setText('[data-target-member-name]', member.name, row);
        const team = teams.find((item) => item.teamId === member.teamId);
        setText('[data-target-member-meta]',
                `${member.studentNo} · ${team?.name || '팀 미배정'}`, row);
        list.appendChild(row);
    });
    lookup('[data-target-all]').checked = false;
}

function openTargetModal(trigger) {
    const event = lookupEventFromTrigger(trigger);
    if (!event) {
        showToast('대상을 확정할 행사를 찾을 수 없습니다.');
        return;
    }
    targetEvent = event;
    setText('[data-target-event-title]', event.title);
    const summary = event.targetScope === 'TEAM'
        ? `${event.teamName || '선택한 팀'}의 활성 멤버를 명단에 추가합니다.`
        : `${SCOPE_LABELS[event.targetScope]}를 출석 명단에 추가합니다.`;
    setText('[data-target-summary]', summary);
    const selected = event.targetScope === 'SELECTED';
    lookup('[data-selected-member-section]').classList.toggle('hidden', !selected);
    if (selected) {
        renderTargetMembers();
    }
    setError('[data-target-error]', '');
    openModal('targetModal', trigger);
}

async function confirmTargets(trigger) {
    if (!targetEvent) {
        return;
    }
    const selectedMemberIds = targetEvent.targetScope === 'SELECTED'
        ? all('[data-target-member]:checked').map((checkbox) => Number(checkbox.value))
        : [];
    if (targetEvent.targetScope === 'SELECTED' && selectedMemberIds.length === 0) {
        setError('[data-target-error]', '대상 멤버를 한 명 이상 선택해 주세요.');
        return;
    }
    trigger.disabled = true;
    try {
        const result = await post(`/api/event-management/${targetEvent.clubEventId}/targets`, {
            selectedMemberIds,
        });
        closeActionModal(trigger);
        showToast(`${result.targetCount}명을 참석 대상으로 확정했습니다.`);
        targetEvent = null;
        await loadEvents();
    } catch (error) {
        setError('[data-target-error]', errorMessage(error));
    } finally {
        trigger.disabled = false;
    }
}

async function changeCheckIn(trigger, action) {
    const event = lookupEventFromTrigger(trigger);
    if (!event) {
        return;
    }
    trigger.disabled = true;
    try {
        await post(`/api/event-management/${event.clubEventId}/check-in/${action}`);
        showToast(action === 'open' ? '출석 확인을 시작했습니다.' : '출석 확인을 종료했습니다.');
        await loadEvents();
    } catch (error) {
        showToast(errorMessage(error));
    } finally {
        trigger.disabled = false;
    }
}

function setRosterState(title, message) {
    const state = lookup('[data-roster-state]');
    state.hidden = false;
    setText('[data-roster-state-title]', title, state);
    setText('[data-roster-state-message]', message, state);
}

function renderRosterCounts(counts) {
    const container = lookup('[data-roster-counts]');
    container.replaceChildren();
    Object.keys(ATTENDANCE_META).forEach((status) => {
        const count = counts.find((item) => item.status === status)?.count || 0;
        const [label, tone] = ATTENDANCE_META[status];
        container.appendChild(badge(`${label} ${count}`, tone));
    });
}

function renderRoster(rows, counts) {
    all('[data-roster-row]').forEach((row) => row.remove());
    renderRosterCounts(counts);
    const state = lookup('[data-roster-state]');
    if (rows.length === 0) {
        setRosterState('출석 대상이 없습니다', '행사 대상을 먼저 확인해 주세요.');
        return;
    }
    state.hidden = true;
    const list = lookup('[data-roster-list]');
    rows.forEach((item) => {
        const row = lookup('[data-roster-row-template]').content.firstElementChild.cloneNode(true);
        row.dataset.attendanceId = item.eventAttendanceId;
        row.dataset.attendanceStatus = item.status;
        const checkbox = lookup('[data-roster-member]', row);
        checkbox.setAttribute('aria-label', `${item.memberName} 선택`);
        checkbox.disabled = rosterEvent.status !== 'IN_PROGRESS';
        setText('[data-roster-avatar]', initials(item.memberName), row);
        setText('[data-roster-name]', item.memberName, row);
        setText('[data-roster-team]', item.teamName || '미배정', row);
        lookup('[data-roster-status]', row).appendChild(createStatusBadge(item.status, true));
        const processor = item.processedByName
            ? `${item.processedByName} · ${formatProcessedTime(item.processedDttm)}` : '미처리';
        setText('[data-roster-processor]', processor, row);
        setText('[data-roster-reason]', item.reason || '', row);
        list.appendChild(row);
    });
    lookup('[data-roster-all]').checked = false;
    lookup('[data-roster-all]').disabled = rosterEvent.status !== 'IN_PROGRESS';
}

async function loadRoster() {
    all('[data-roster-row]').forEach((row) => row.remove());
    lookup('[data-roster-counts]').replaceChildren();
    setRosterState('명단을 불러오는 중입니다', '잠시만 기다려 주세요.');
    setError('[data-roster-error]', '');
    try {
        const [rows, counts] = await Promise.all([
            get(`/api/event-management/${rosterEvent.clubEventId}/attendances`),
            get(`/api/event-management/${rosterEvent.clubEventId}/attendance-counts`),
        ]);
        renderRoster(rows, counts);
    } catch (error) {
        setRosterState('명단을 불러오지 못했습니다', errorMessage(error));
    }
}

function openRosterModal(trigger) {
    const event = lookupEventFromTrigger(trigger);
    if (!event) {
        showToast('출석 명단을 찾을 수 없습니다.');
        return;
    }
    rosterEvent = event;
    setText('[data-roster-event-title]', event.title);
    const processing = event.status === 'IN_PROGRESS';
    lookup('[data-roster-controls]').classList.toggle('hidden', !processing);
    lookup('[data-roster-readonly]').classList.toggle('hidden', processing);
    lookup('[data-roster-process-button]').classList.toggle('hidden', !processing);
    document.getElementById('attendanceStatus').value = 'PRESENT';
    document.getElementById('attendanceReason').value = '';
    openModal('rosterModal', trigger);
    loadRoster();
}

async function processAttendance(trigger) {
    const status = readValue('attendanceStatus');
    const reason = readValue('attendanceReason');
    const rows = all('[data-roster-row]').filter((row) =>
        lookup('[data-roster-member]', row).checked
        && row.dataset.attendanceStatus !== status);
    if (rows.length === 0) {
        setError('[data-roster-error]', '처리할 멤버를 선택해 주세요. 같은 상태의 멤버는 제외됩니다.');
        return;
    }
    if (status === 'EXCUSED' && !reason) {
        setError('[data-roster-error]', '공결 처리 사유를 입력해 주세요.');
        return;
    }
    trigger.disabled = true;
    try {
        const result = await post(
                `/api/event-management/${rosterEvent.clubEventId}/attendances/process`, {
                    eventAttendanceIds: rows.map((row) => Number(row.dataset.attendanceId)),
                    status,
                    reason: reason || null,
                });
        showToast(`${result.processedCount}명의 출석 상태를 반영했습니다.`);
        await Promise.all([loadRoster(), loadEvents()]);
    } catch (error) {
        setError('[data-roster-error]', errorMessage(error));
    } finally {
        trigger.disabled = false;
    }
}

async function archiveEvent(trigger) {
    const event = lookupEventFromTrigger(trigger);
    if (!event || !window.confirm(`'${event.title}' 행사를 보관하시겠어요?`)) {
        return;
    }
    trigger.disabled = true;
    try {
        await post(`/api/event-management/${event.clubEventId}/archive`);
        showToast('행사를 보관했습니다.');
        await loadEvents();
    } catch (error) {
        showToast(errorMessage(error));
    } finally {
        trigger.disabled = false;
    }
}

all('[data-filter-group="event"]').forEach((button) => {
    button.addEventListener('click', () => {
        activateFilterChip(button);
        activeFilter = button.dataset.filterValue;
        renderEvents();
    });
});

if (canManage) {
    document.getElementById('eventTargetScope').addEventListener('change', updateTargetScopeFields);
    lookup('[data-target-all]').addEventListener('change', (event) => {
        all('[data-target-member]').forEach((checkbox) => {
            checkbox.checked = event.target.checked;
        });
    });
    lookup('[data-roster-all]').addEventListener('change', (event) => {
        all('[data-roster-member]:not(:disabled)').forEach((checkbox) => {
            checkbox.checked = event.target.checked;
        });
    });
    bindPageActions({
        [ACTIONS.CREATE_OPEN]: openCreateModal,
        [ACTIONS.EDIT]: openEditModal,
        [ACTIONS.SAVE]: saveEvent,
        [ACTIONS.TARGET_OPEN]: openTargetModal,
        [ACTIONS.TARGET_CONFIRM]: confirmTargets,
        [ACTIONS.CHECK_IN_OPEN]: (trigger) => changeCheckIn(trigger, 'open'),
        [ACTIONS.CHECK_IN_CLOSE]: (trigger) => changeCheckIn(trigger, 'close'),
        [ACTIONS.ROSTER_OPEN]: openRosterModal,
        [ACTIONS.ATTENDANCE_PROCESS]: processAttendance,
        [ACTIONS.ARCHIVE]: archiveEvent,
        [ACTIONS.RETRY]: loadEvents,
    });
    loadReferences().catch((error) => showToast(`기준 정보를 불러오지 못했습니다. ${errorMessage(error)}`));
} else {
    bindPageActions({[ACTIONS.RETRY]: loadEvents});
}

loadEvents();
