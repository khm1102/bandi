import {ApiError, del, get, post, put} from '../common/api.js';
import {all, bindPageActions, element, lookup, readValue} from '../common/dom.js';
import {openModal} from '../common/modal.js';
import {currentUserRole} from '../common/session.js';
import {showToast} from '../common/toast.js';
import {activateFilterChip, closeActionModal} from '../common/view.js';

const ACTIONS = Object.freeze({
    PREVIOUS: 'calendar-prev',
    NEXT: 'calendar-next',
    CREATE: 'calendar-create',
    SAVE: 'calendar-save',
    DELETE: 'calendar-delete',
});
const TEAM_TONES = [
    'bg-secondary text-muted-foreground',
    'bg-info-soft text-info',
    'bg-accent text-accent-foreground',
    'bg-warning-soft text-warning',
];

const calendarState = {
    month: new Date(new Date().getFullYear(), new Date().getMonth(), 1),
    filterTeamId: 'ALL',
    events: [],
    teams: [],
    loginMember: null,
    editingEventId: null,
};

function errorMessage(error) {
    if (error instanceof ApiError && error.fieldErrors.length > 0) {
        return error.fieldErrors[0].reason;
    }
    return error.message || '요청을 처리하지 못했습니다.';
}

function pad(value) {
    return String(value).padStart(2, '0');
}

function localDateTime(date) {
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:00`;
}

function monthRange() {
    const start = new Date(calendarState.month);
    const end = new Date(start.getFullYear(), start.getMonth() + 1, 1);
    return {rangeStart: localDateTime(start), rangeEnd: localDateTime(end)};
}

function teamById(teamId) {
    return calendarState.teams.find((team) => team.teamId === teamId);
}

function teamLabel(teamId) {
    return teamId ? teamById(teamId)?.name || `팀 ${teamId}` : '전체';
}

function selectedEvents(day) {
    return calendarState.events.filter((eventData) => {
        const start = new Date(eventData.startDttm);
        const teamMatched = calendarState.filterTeamId === 'ALL'
                || String(eventData.teamId) === calendarState.filterTeamId;
        return start.getDate() === day && teamMatched;
    });
}

function calendarEventNode(eventData) {
    const teamIndex = Math.max(0, calendarState.teams.findIndex(
            (team) => team.teamId === eventData.teamId));
    const tone = eventData.teamId ? TEAM_TONES[teamIndex % TEAM_TONES.length]
        : 'bg-sidebar text-white';
    const node = element('button',
            `mt-1 block w-full truncate rounded-sm px-1 py-0.5 text-left text-xs font-bold ${tone}`,
            eventData.title);
    node.type = 'button';
    node.title = `${teamLabel(eventData.teamId)} · ${eventData.place || '장소 미정'}`;
    node.dataset.calendarEventId = eventData.calendarEventId;
    return node;
}

function renderCalendar() {
    const grid = lookup('[data-calendar-grid]');
    grid.replaceChildren();
    ['일', '월', '화', '수', '목', '금', '토'].forEach((dayName) => {
        grid.appendChild(element('div',
                'py-1 text-center text-xs font-extrabold text-muted-foreground',
                dayName));
    });
    const year = calendarState.month.getFullYear();
    const month = calendarState.month.getMonth();
    const firstDay = new Date(year, month, 1).getDay();
    const dayCount = new Date(year, month + 1, 0).getDate();
    for (let index = 0; index < firstDay; index += 1) {
        grid.appendChild(element('div',
                'min-h-20 rounded-md bg-secondary/50 opacity-50'));
    }
    const today = new Date();
    for (let day = 1; day <= dayCount; day += 1) {
        const isToday = today.getFullYear() === year
                && today.getMonth() === month && today.getDate() === day;
        const classes = isToday
            ? 'min-h-20 rounded-md border border-primary bg-card p-1.5 ring-2 ring-ring/20'
            : 'min-h-20 rounded-md border bg-card p-1.5';
        const cell = element('div', classes);
        cell.dataset.calendarDay = String(day);
        cell.appendChild(element('span',
                `text-xs font-extrabold ${isToday ? 'text-accent-foreground' : ''}`,
                String(day)));
        selectedEvents(day).forEach((eventData) => {
            cell.appendChild(calendarEventNode(eventData));
        });
        grid.appendChild(cell);
    }
    lookup('[data-calendar-month]').textContent = `${year}년 ${month + 1}월`;
    lookup('[data-calendar-filter-label]').textContent =
            `${calendarState.filterTeamId === 'ALL' ? '전체 팀' : teamLabel(Number(calendarState.filterTeamId))} 일정 표시 중`;
}

function setState(title, message, retry = false) {
    const state = lookup('[data-calendar-state]');
    state.classList.remove('hidden');
    lookup('[data-calendar-state-title]', state).textContent = title;
    lookup('[data-calendar-state-message]', state).textContent = message;
    lookup('[data-calendar-retry]', state).classList.toggle('hidden', !retry);
}

function hideState() {
    lookup('[data-calendar-state]').classList.add('hidden');
}

function createTeamFilter(team) {
    const button = element('button',
            'inline-flex h-11 items-center gap-1.5 rounded-md border bg-card px-3 text-xs font-bold text-muted-foreground transition-colors hover:border-sidebar-muted md:h-8',
            team.name);
    button.type = 'button';
    button.dataset.filterGroup = 'calendar';
    button.dataset.filterValue = team.teamId;
    button.setAttribute('aria-pressed', 'false');
    return button;
}

function renderTeamControls() {
    const filters = lookup('[data-calendar-filters]');
    all('[data-filter-value]:not([data-filter-value="ALL"])', filters)
            .forEach((button) => button.remove());
    calendarState.teams.forEach((team) => filters.appendChild(
            createTeamFilter(team)));

    const select = document.getElementById('ceTeam');
    select.replaceChildren();
    if (currentUserRole === 'admin') {
        const allOption = element('option', '', '전체 일정');
        allOption.value = '';
        select.appendChild(allOption);
        calendarState.teams.forEach((team) => {
            const option = element('option', '', team.name);
            option.value = team.teamId;
            select.appendChild(option);
        });
        return;
    }
    const allOption = element('option', '', '전체 일정');
    allOption.value = '';
    allOption.disabled = true;
    select.appendChild(allOption);
    calendarState.teams.forEach((team) => {
        const option = element('option', '', team.name);
        option.value = team.teamId;
        option.disabled = team.teamId !== calendarState.loginMember.teamId;
        select.appendChild(option);
    });
    select.value = calendarState.loginMember.teamId || '';
}

async function loadCalendar() {
    setState('일정을 불러오는 중입니다', '잠시만 기다려 주세요.');
    try {
        if (calendarState.teams.length === 0) {
            const [teams, member] = await Promise.all([
                get('/api/members/reference/teams'),
                get('/api/members/me'),
            ]);
            calendarState.teams = teams;
            calendarState.loginMember = member;
            renderTeamControls();
        }
        calendarState.events = await get('/api/calendar-events', monthRange());
        hideState();
        renderCalendar();
    } catch (error) {
        setState('일정을 불러오지 못했습니다', errorMessage(error), true);
        renderCalendar();
    }
}

function setInlineError(message) {
    const error = lookup('[data-calendar-form-error]');
    error.textContent = message || '';
    error.classList.toggle('hidden', !message);
}

function eventFormControls() {
    return ['ceTitle', 'ceTeam', 'ceStart', 'ceEnd', 'ceAllDay', 'ceLoc',
        'ceDescription'].map((id) => document.getElementById(id));
}

function canEditEvent(eventData) {
    return currentUserRole === 'admin' || currentUserRole === 'leader'
            && eventData.teamId === calendarState.loginMember.teamId;
}

function configureFormAccess(editable, existing) {
    eventFormControls().forEach((control) => {
        control.disabled = !editable;
    });
    const saveButton = lookup('[data-page-action="calendar-save"]');
    const deleteButton = lookup('[data-page-action="calendar-delete"]');
    saveButton.classList.toggle('hidden', !editable);
    deleteButton.classList.toggle('hidden', !editable || !existing);
}

function defaultStart() {
    const now = new Date();
    const sameMonth = now.getFullYear() === calendarState.month.getFullYear()
            && now.getMonth() === calendarState.month.getMonth();
    return sameMonth ? now : new Date(calendarState.month.getFullYear(),
            calendarState.month.getMonth(), 1, 18, 0);
}

function toInputValue(value) {
    return value ? value.slice(0, 16) : '';
}

function fillForm(eventData) {
    document.getElementById('ceTitle').value = eventData?.title || '';
    document.getElementById('ceTeam').value = eventData?.teamId
            || (currentUserRole === 'leader'
                ? calendarState.loginMember.teamId || '' : '');
    const start = eventData ? null : defaultStart();
    const end = start ? new Date(start.getTime() + 60 * 60 * 1000) : null;
    document.getElementById('ceStart').value = eventData
        ? toInputValue(eventData.startDttm) : toInputValue(localDateTime(start));
    document.getElementById('ceEnd').value = eventData
        ? toInputValue(eventData.endDttm) : toInputValue(localDateTime(end));
    document.getElementById('ceAllDay').checked = eventData?.allDay || false;
    document.getElementById('ceLoc').value = eventData?.place || '';
    document.getElementById('ceDescription').value = eventData?.description || '';
    setInlineError('');
}

function openCreateModal(trigger) {
    calendarState.editingEventId = null;
    fillForm(null);
    configureFormAccess(true, false);
    document.getElementById('calendarEventModalTitle').textContent = '일정 등록';
    openModal('calendarEventModal', trigger);
}

function openEventModal(eventId, trigger) {
    const eventData = calendarState.events.find((item) =>
        item.calendarEventId === Number(eventId));
    if (!eventData) {
        return;
    }
    calendarState.editingEventId = eventData.calendarEventId;
    fillForm(eventData);
    configureFormAccess(canEditEvent(eventData), true);
    document.getElementById('calendarEventModalTitle').textContent =
            canEditEvent(eventData) ? '일정 수정' : '일정 상세';
    openModal('calendarEventModal', trigger);
}

function formRequest() {
    return {
        teamId: Number(readValue('ceTeam')) || null,
        title: readValue('ceTitle'),
        description: readValue('ceDescription'),
        startDttm: readValue('ceStart'),
        endDttm: readValue('ceEnd'),
        allDay: document.getElementById('ceAllDay').checked,
        place: readValue('ceLoc'),
    };
}

async function saveEvent(trigger) {
    const request = formRequest();
    if (!request.startDttm || !request.endDttm
            || new Date(request.startDttm) >= new Date(request.endDttm)) {
        setInlineError('종료 일시는 시작 일시보다 뒤여야 합니다.');
        return;
    }
    trigger.disabled = true;
    setInlineError('');
    try {
        if (calendarState.editingEventId) {
            await put(`/api/calendar-events/${calendarState.editingEventId}`,
                    request);
        } else {
            await post('/api/calendar-events', request);
        }
        closeActionModal(trigger);
        showToast(calendarState.editingEventId ? '일정을 수정했습니다.'
            : '일정을 등록했습니다.');
        await loadCalendar();
    } catch (error) {
        setInlineError(errorMessage(error));
    } finally {
        trigger.disabled = false;
    }
}

async function deleteEvent(trigger) {
    if (!calendarState.editingEventId) {
        return;
    }
    trigger.disabled = true;
    try {
        await del(`/api/calendar-events/${calendarState.editingEventId}`);
        closeActionModal(trigger);
        showToast('일정을 삭제했습니다.');
        await loadCalendar();
    } catch (error) {
        setInlineError(errorMessage(error));
    } finally {
        trigger.disabled = false;
    }
}

document.addEventListener('click', (event) => {
    const filter = event.target.closest('[data-filter-group="calendar"]');
    if (filter) {
        calendarState.filterTeamId = filter.dataset.filterValue;
        activateFilterChip(filter);
        renderCalendar();
        return;
    }
    const eventButton = event.target.closest('[data-calendar-event-id]');
    if (eventButton) {
        openEventModal(eventButton.dataset.calendarEventId, eventButton);
        return;
    }
    const dayCell = event.target.closest('[data-calendar-day]');
    if (dayCell) {
        const day = Number(dayCell.dataset.calendarDay);
        showToast(`${calendarState.month.getMonth() + 1}월 ${day}일 · ${selectedEvents(day).length}개 일정`);
    }
});

lookup('[data-calendar-retry]').addEventListener('click', loadCalendar);
bindPageActions({
    [ACTIONS.PREVIOUS]: async () => {
        calendarState.month = new Date(calendarState.month.getFullYear(),
                calendarState.month.getMonth() - 1, 1);
        await loadCalendar();
    },
    [ACTIONS.NEXT]: async () => {
        calendarState.month = new Date(calendarState.month.getFullYear(),
                calendarState.month.getMonth() + 1, 1);
        await loadCalendar();
    },
    [ACTIONS.CREATE]: openCreateModal,
    [ACTIONS.SAVE]: saveEvent,
    [ACTIONS.DELETE]: deleteEvent,
});

renderCalendar();
loadCalendar();
