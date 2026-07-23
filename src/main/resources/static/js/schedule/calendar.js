import {ApiError, del, get, post, put} from '../common/api.js';
import {
    focusDateTimeField,
    initializeDateTimeFields,
    readDateTimeValue,
    setDateTimeMode,
    setDateTimeValue,
} from '../common/date-time-field.js';
import {all, bindPageActions, element, lookup, readValue} from '../common/dom.js';
import {closeModal, openModal} from '../common/modal.js';
import {currentUserRole} from '../common/session.js';
import {showToast} from '../common/toast.js';

const ACTIONS = Object.freeze({
    CREATE: 'calendar-create',
    EDIT: 'calendar-edit',
    SAVE: 'calendar-save',
    DELETE: 'calendar-delete',
});
const VIEW_STORAGE_KEY = 'bandi.calendar.view';
const DESKTOP_VIEW_QUERY = '(min-width: 768px) and (orientation: landscape)';
const VALID_VIEWS = new Set(['dayGridMonth', 'timeGridWeek', 'listWeek']);
const FULL_CALENDAR = window.FullCalendar;

const state = {
    calendar: null,
    teams: [],
    loginMember: null,
    events: [],
    selectedEvent: null,
    editingEventId: null,
    selectedTeamId: null,
    requestGeneration: 0,
    loading: false,
    referenceFailed: false,
    focusEventId: null,
    requestController: null,
};

const teamFilter = lookup('[data-calendar-team-filter]');
const statusBox = lookup('[data-calendar-status]');
const statusTitle = lookup('[data-calendar-status-title]');
const statusMessage = lookup('[data-calendar-status-message]');
const retryButton = lookup('[data-calendar-retry]');
const announcement = lookup('[data-calendar-announcement]');
const formError = lookup('[data-calendar-form-error]');
const periodError = lookup('[data-calendar-period-error]');
const detailError = lookup('[data-calendar-detail-error]');

function errorMessage(error) {
    if (error instanceof ApiError && error.fieldErrors.length > 0) {
        return error.fieldErrors[0].reason;
    }
    return error?.message || '요청을 처리하지 못했습니다.';
}

function pad(value) {
    return String(value).padStart(2, '0');
}

function localDateTime(date, includeSeconds = true) {
    const base = `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
        + `T${pad(date.getHours())}:${pad(date.getMinutes())}`;
    return includeSeconds ? `${base}:00` : base;
}

function parseLocalDate(dateText) {
    const [year, month, day] = dateText.split('-').map(Number);
    return new Date(year, month - 1, day);
}

function addDays(date, amount) {
    const next = new Date(date);
    next.setDate(next.getDate() + amount);
    return next;
}

function dateValue(date) {
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

function roundToNextHalfHour(date) {
    const rounded = new Date(date);
    rounded.setSeconds(0, 0);
    const remainder = rounded.getMinutes() % 30;
    if (remainder !== 0) {
        rounded.setMinutes(rounded.getMinutes() + 30 - remainder);
    }
    return rounded;
}

function teamById(teamId) {
    return state.teams.find((team) => team.teamId === Number(teamId));
}

function teamName(teamId) {
    if (!teamId) {
        return '동아리 전체';
    }
    return teamById(teamId)?.name || `팀 ${teamId}`;
}

function setAnnouncement(message) {
    announcement.textContent = '';
    window.setTimeout(() => {
        announcement.textContent = message;
    }, 20);
}

function showStatus(title, message, retry = false) {
    statusTitle.textContent = title;
    statusMessage.textContent = message;
    statusBox.classList.remove('hidden');
    retryButton.classList.toggle('hidden', !retry);
}

function hideStatus() {
    statusBox.classList.add('hidden');
    retryButton.classList.add('hidden');
}

function showReferenceFailure(eventsEmpty = false) {
    showStatus('팀 기준 정보를 불러오지 못했어요',
            eventsEmpty
                ? '현재 기간의 일정도 없어요. 팀 이름과 필터를 복구하려면 다시 시도해 주세요.'
                : '일정은 계속 볼 수 있지만 팀 이름과 필터가 일부 제한돼요. 다시 시도해 주세요.',
            true);
}

function setInlineError(target, message = '') {
    target.textContent = message;
    target.classList.toggle('hidden', !message);
}

function initialView() {
    try {
        const stored = window.sessionStorage.getItem(VIEW_STORAGE_KEY);
        if (VALID_VIEWS.has(stored)) {
            return stored;
        }
    } catch (error) {
        // 세션 저장소를 사용할 수 없어도 화면 기본값으로 계속한다.
    }
    return window.matchMedia(DESKTOP_VIEW_QUERY).matches
        ? 'dayGridMonth' : 'listWeek';
}

function rememberView(viewName) {
    try {
        window.sessionStorage.setItem(VIEW_STORAGE_KEY, viewName);
    } catch (error) {
        // 사생활 보호 모드 등 저장소 제한은 캘린더 사용을 막지 않는다.
    }
}

function activateViewButton(viewName) {
    all('[data-calendar-view]').forEach((button) => {
        const active = button.dataset.calendarView === viewName;
        button.setAttribute('aria-pressed', String(active));
        button.classList.toggle('bg-card', active);
        button.classList.toggle('text-muted-foreground', !active);
    });
}

function renderTeamFilter() {
    teamFilter.replaceChildren();
    const allOption = element('option', '', '전체 일정');
    allOption.value = '';
    teamFilter.appendChild(allOption);
    state.teams.forEach((team) => {
        const option = element('option', '', team.name);
        option.value = String(team.teamId);
        teamFilter.appendChild(option);
    });
    teamFilter.value = state.selectedTeamId ? String(state.selectedTeamId) : '';
}

function renderEditorTeamOptions() {
    const select = document.getElementById('ceTeam');
    select.replaceChildren();
    if (currentUserRole === 'admin') {
        const allOption = element('option', '', '동아리 전체');
        allOption.value = '';
        select.appendChild(allOption);
        state.teams.forEach((team) => {
            const option = element('option', '', team.name);
            option.value = String(team.teamId);
            select.appendChild(option);
        });
        return;
    }
    const ownTeam = teamById(state.loginMember?.teamId);
    if (ownTeam) {
        const option = element('option', '', ownTeam.name);
        option.value = String(ownTeam.teamId);
        select.appendChild(option);
    }
}

async function loadReferences() {
    try {
        const [teams, member] = await Promise.all([
            get('/api/members/reference/teams'),
            get('/api/members/me'),
        ]);
        state.teams = teams;
        state.loginMember = member;
        renderTeamFilter();
        renderEditorTeamOptions();
    } catch (error) {
        state.referenceFailed = true;
        showReferenceFailure();
    }
}

function eventColor(eventData) {
    return eventData.teamId ? 'var(--primary-strong)' : 'var(--sidebar)';
}

function toFullCalendarEvent(eventData) {
    return {
        id: String(eventData.calendarEventId),
        title: eventData.title,
        start: eventData.startDttm,
        end: eventData.endDttm,
        allDay: eventData.allDay,
        color: eventColor(eventData),
        textColor: 'var(--destructive-foreground)',
        extendedProps: {raw: eventData},
    };
}

async function fetchCalendarEvents(fetchInfo, successCallback, failureCallback) {
    const generation = state.requestGeneration + 1;
    state.requestGeneration = generation;
    state.requestController?.abort();
    const controller = new AbortController();
    state.requestController = controller;
    state.loading = true;
    showStatus('일정을 불러오는 중이에요', '선택한 기간의 일정을 확인하고 있어요.');
    try {
        const events = await get('/api/calendar-events', {
            rangeStart: localDateTime(fetchInfo.start),
            rangeEnd: localDateTime(fetchInfo.end),
            teamId: state.selectedTeamId,
        }, {signal: controller.signal});
        if (generation !== state.requestGeneration) {
            successCallback([]);
            return;
        }
        state.events = events;
        successCallback(events.map(toFullCalendarEvent));
        if (state.referenceFailed) {
            showReferenceFailure(events.length === 0);
        } else if (events.length === 0) {
            const filtered = Boolean(state.selectedTeamId);
            showStatus(filtered ? '이 팀의 일정이 없어요' : '등록된 일정이 없어요',
                    filtered
                        ? '다른 팀을 선택하거나 전체 일정을 확인해 보세요.'
                        : '일정을 등록하면 이 기간에 바로 표시돼요.');
        } else {
            hideStatus();
        }
        state.loading = false;
    } catch (error) {
        if (generation !== state.requestGeneration) {
            successCallback([]);
            return;
        }
        state.loading = false;
        showStatus('일정을 불러오지 못했어요',
                `${errorMessage(error)} 입력한 필터는 유지했어요.`, true);
        failureCallback(error);
    }
}

function renderEventContent(info) {
    const raw = info.event.extendedProps.raw;
    const content = element('div', 'bandi-calendar-event-content');
    const scope = element('span', 'bandi-calendar-event-team', teamName(raw.teamId));
    const title = element('span', 'bandi-calendar-event-title', raw.title);
    content.append(scope, title);
    if (info.view.type === 'listWeek' && raw.place) {
        content.appendChild(element('span', 'bandi-calendar-event-place', `· ${raw.place}`));
    }
    return {domNodes: [content]};
}

function eventAriaLabel(eventData) {
    return `${eventData.title}, ${teamName(eventData.teamId)}, ${formatPeriod(eventData)}`;
}

function configureEventElement(info) {
    const raw = info.event.extendedProps.raw;
    info.el.setAttribute('aria-label', eventAriaLabel(raw));
    info.el.setAttribute('role', 'button');
    info.el.tabIndex = 0;
    info.el.addEventListener('keydown', (event) => {
        if (event.key === 'Enter' || event.key === ' ') {
            event.preventDefault();
            openDetail(raw, info.el);
        }
    });
    if (state.focusEventId === raw.calendarEventId) {
        state.focusEventId = null;
        window.requestAnimationFrame(() => info.el.focus());
    }
}

function createCalendar() {
    if (!FULL_CALENDAR?.Calendar) {
        showStatus('캘린더를 시작하지 못했어요',
                '화면 파일을 다시 불러온 뒤에도 같은 문제가 있으면 운영진에게 알려 주세요.', true);
        return;
    }
    state.calendar = new FULL_CALENDAR.Calendar(lookup('[data-calendar-root]'), {
        locale: 'ko',
        initialView: initialView(),
        headerToolbar: false,
        height: 'auto',
        dayMaxEvents: 3,
        nowIndicator: true,
        selectable: currentUserRole !== 'member',
        selectMirror: true,
        editable: false,
        eventStartEditable: false,
        eventDurationEditable: false,
        eventInteractive: true,
        allDaySlot: true,
        slotMinTime: '07:00:00',
        slotMaxTime: '24:00:00',
        slotDuration: '00:30:00',
        events: fetchCalendarEvents,
        eventContent: renderEventContent,
        eventDidMount: configureEventElement,
        eventClick: (info) => openDetail(info.event.extendedProps.raw, info.el),
        select: openCreateFromSelection,
        dateClick: openCreateFromDate,
        datesSet: (info) => {
            lookup('[data-calendar-title]').textContent = info.view.title;
            activateViewButton(info.view.type);
            rememberView(info.view.type);
            setAnnouncement(`${info.view.title} 일정을 표시합니다.`);
        },
        moreLinkContent: (argument) => `${argument.num}개 더 보기`,
        noEventsContent: '이 기간에는 일정이 없어요.',
    });
    state.calendar.render();
}

function formatDateTime(value) {
    if (!value) {
        return '기록 없음';
    }
    return new Intl.DateTimeFormat('ko-KR', {
        year: 'numeric', month: 'long', day: 'numeric', weekday: 'short',
        hour: '2-digit', minute: '2-digit', hour12: false,
    }).format(new Date(value));
}

function formatDateOnly(value) {
    return new Intl.DateTimeFormat('ko-KR', {
        year: 'numeric', month: 'long', day: 'numeric', weekday: 'short',
    }).format(new Date(value));
}

function formatPeriod(eventData) {
    if (!eventData.allDay) {
        return `${formatDateTime(eventData.startDttm)} ~ ${formatDateTime(eventData.endDttm)}`;
    }
    const inclusiveEnd = addDays(new Date(eventData.endDttm), -1);
    const startLabel = formatDateOnly(eventData.startDttm);
    const endLabel = formatDateOnly(inclusiveEnd);
    return startLabel === endLabel ? `${startLabel} 종일` : `${startLabel} ~ ${endLabel} 종일`;
}

function canEdit(eventData) {
    if (currentUserRole === 'admin') {
        return true;
    }
    return currentUserRole === 'leader'
        && eventData.teamId === state.loginMember?.teamId;
}

function openDetail(eventData, trigger) {
    state.selectedEvent = eventData;
    setInlineError(detailError);
    lookup('[data-calendar-detail-scope]').textContent = teamName(eventData.teamId);
    lookup('[data-calendar-detail-all-day]').textContent = eventData.allDay ? '종일' : '시간 일정';
    lookup('[data-calendar-detail-title]').textContent = eventData.title;
    lookup('[data-calendar-detail-period]').textContent = formatPeriod(eventData);
    lookup('[data-calendar-detail-place]').textContent = eventData.place || '장소가 정해지지 않았어요.';
    lookup('[data-calendar-detail-description]').textContent = eventData.description || '추가 설명이 없어요.';
    lookup('[data-calendar-detail-updated]').textContent = formatDateTime(eventData.updatedDttm);
    lookup('[data-page-action="calendar-edit"]').classList.toggle('hidden', !canEdit(eventData));
    lookup('[data-page-action="calendar-delete"]').classList.toggle('hidden', !canEdit(eventData));
    openModal('calendarDetailModal', trigger);
}

function defaultTeamId() {
    return currentUserRole === 'leader' ? state.loginMember?.teamId || '' : '';
}

function configureDateMode(allDay) {
    setDateTimeMode('ceStart', allDay);
    setDateTimeMode('ceEnd', allDay);
}

function fillCreateForm(startDate, endDate, allDay) {
    state.editingEventId = null;
    document.getElementById('calendarEventModalTitle').textContent = '일정 등록';
    document.getElementById('ceTitle').value = '';
    document.getElementById('ceTeam').value = defaultTeamId();
    document.getElementById('ceAllDay').checked = allDay;
    configureDateMode(allDay);
    if (allDay) {
        setDateTimeValue('ceStart', dateValue(startDate));
        setDateTimeValue('ceEnd', dateValue(endDate));
    } else {
        setDateTimeValue('ceStart', localDateTime(startDate, false));
        setDateTimeValue('ceEnd', localDateTime(endDate, false));
    }
    document.getElementById('ceLoc').value = '';
    document.getElementById('ceDescription').value = '';
    lookup('[data-calendar-extra-details]').open = false;
    setInlineError(formError);
    setInlineError(periodError);
}

function openCreate(trigger, start = roundToNextHalfHour(new Date()), end = null,
                    allDay = false) {
    if (currentUserRole === 'member') {
        return;
    }
    const resolvedEnd = end || new Date(start.getTime() + 60 * 60 * 1000);
    fillCreateForm(start, resolvedEnd, allDay);
    openModal('calendarEventModal', trigger);
}

function openCreateFromDate(info) {
    if (currentUserRole === 'member') {
        return;
    }
    const selected = new Date(info.date);
    const rounded = roundToNextHalfHour(new Date());
    selected.setHours(rounded.getHours(), rounded.getMinutes(), 0, 0);
    openCreate(info.dayEl, selected, new Date(selected.getTime() + 60 * 60 * 1000), false);
}

function openCreateFromSelection(info) {
    if (currentUserRole === 'member') {
        return;
    }
    if (info.allDay) {
        const inclusiveEnd = addDays(info.end, -1);
        openCreate(info.jsEvent?.target || document.activeElement,
                info.start, inclusiveEnd, true);
    } else {
        openCreate(info.jsEvent?.target || document.activeElement,
                info.start, info.end, false);
    }
    state.calendar.unselect();
}

function fillEditForm(eventData) {
    state.editingEventId = eventData.calendarEventId;
    document.getElementById('calendarEventModalTitle').textContent = '일정 수정';
    document.getElementById('ceTitle').value = eventData.title;
    document.getElementById('ceTeam').value = eventData.teamId || '';
    document.getElementById('ceAllDay').checked = eventData.allDay;
    configureDateMode(eventData.allDay);
    if (eventData.allDay) {
        setDateTimeValue('ceStart', eventData.startDttm.slice(0, 10));
        const inclusiveEnd = addDays(new Date(eventData.endDttm), -1);
        setDateTimeValue('ceEnd', dateValue(inclusiveEnd));
    } else {
        setDateTimeValue('ceStart', eventData.startDttm.slice(0, 16));
        setDateTimeValue('ceEnd', eventData.endDttm.slice(0, 16));
    }
    document.getElementById('ceLoc').value = eventData.place || '';
    document.getElementById('ceDescription').value = eventData.description || '';
    lookup('[data-calendar-extra-details]').open = Boolean(eventData.place || eventData.description);
    setInlineError(formError);
    setInlineError(periodError);
}

function openEdit(trigger) {
    if (!state.selectedEvent || !canEdit(state.selectedEvent)) {
        return;
    }
    fillEditForm(state.selectedEvent);
    closeModal(document.getElementById('calendarDetailModal'));
    openModal('calendarEventModal', trigger);
}

function toggleAllDayMode() {
    const allDay = document.getElementById('ceAllDay').checked;
    const startValue = readDateTimeValue('ceStart');
    const endValue = readDateTimeValue('ceEnd');
    configureDateMode(allDay);
    if (allDay) {
        setDateTimeValue('ceStart', startValue.slice(0, 10));
        setDateTimeValue('ceEnd', endValue.slice(0, 10));
        return;
    }
    setDateTimeValue('ceStart', `${startValue.slice(0, 10)}T18:00`);
    setDateTimeValue('ceEnd', `${endValue.slice(0, 10)}T19:00`);
}

function timedValue(value) {
    return value ? `${value}:00` : null;
}

function allDayValues(startValue, endValue) {
    const start = parseLocalDate(startValue);
    const inclusiveEnd = parseLocalDate(endValue);
    return {
        startDttm: `${dateValue(start)}T00:00:00`,
        endDttm: `${dateValue(addDays(inclusiveEnd, 1))}T00:00:00`,
    };
}

function validatePeriod(startValue, endValue, allDay) {
    if (!startValue || !endValue) {
        return '시작과 종료를 모두 입력해 주세요.';
    }
    if (allDay) {
        return parseLocalDate(endValue) < parseLocalDate(startValue)
            ? '종료일은 시작일과 같거나 뒤여야 해요.' : '';
    }
    if (new Date(endValue) <= new Date(startValue)) {
        return '종료 시각은 시작 시각보다 뒤여야 해요.';
    }
    const startMinute = Number(startValue.slice(14, 16));
    const endMinute = Number(endValue.slice(14, 16));
    return startMinute % 30 !== 0 || endMinute % 30 !== 0
        ? '일정 시간은 30분 단위로 입력해 주세요.' : '';
}

function formRequest() {
    const allDay = document.getElementById('ceAllDay').checked;
    const startValue = readDateTimeValue('ceStart');
    const endValue = readDateTimeValue('ceEnd');
    const period = allDay
        ? allDayValues(startValue, endValue)
        : {startDttm: timedValue(startValue), endDttm: timedValue(endValue)};
    return {
        teamId: Number(readValue('ceTeam')) || null,
        title: readValue('ceTitle'),
        description: readValue('ceDescription') || null,
        startDttm: period.startDttm,
        endDttm: period.endDttm,
        allDay,
        place: readValue('ceLoc') || null,
        rawStart: startValue,
        rawEnd: endValue,
    };
}

async function saveEvent(trigger) {
    setInlineError(formError);
    setInlineError(periodError);
    const request = formRequest();
    if (!request.title) {
        setInlineError(formError, '일정명을 입력해 주세요.');
        document.getElementById('ceTitle').focus();
        return;
    }
    const invalidPeriod = validatePeriod(request.rawStart, request.rawEnd, request.allDay);
    if (invalidPeriod) {
        setInlineError(periodError, invalidPeriod);
        focusDateTimeField('ceStart');
        return;
    }
    delete request.rawStart;
    delete request.rawEnd;
    trigger.disabled = true;
    try {
        if (state.editingEventId) {
            await put(`/api/calendar-events/${state.editingEventId}`, request);
            state.focusEventId = state.editingEventId;
        } else {
            const created = await post('/api/calendar-events', request);
            state.focusEventId = created.calendarEventId;
        }
        closeModal(document.getElementById('calendarEventModal'));
        showToast(state.editingEventId ? '일정을 수정했어요.' : '일정을 등록했어요.');
        setAnnouncement(state.editingEventId
            ? '일정을 수정하고 캘린더를 갱신했어요.'
            : '일정을 등록하고 캘린더를 갱신했어요.');
        state.calendar.refetchEvents();
    } catch (error) {
        setInlineError(formError,
                `${errorMessage(error)} 입력한 내용은 그대로 유지했어요.`);
    } finally {
        trigger.disabled = false;
    }
}

async function deleteEvent(trigger) {
    if (!state.selectedEvent || !canEdit(state.selectedEvent)) {
        return;
    }
    trigger.disabled = true;
    setInlineError(detailError);
    try {
        await del(`/api/calendar-events/${state.selectedEvent.calendarEventId}`);
        closeModal(document.getElementById('calendarDetailModal'));
        state.selectedEvent = null;
        showToast('일정을 삭제했어요.');
        setAnnouncement('일정을 삭제하고 캘린더를 갱신했어요.');
        state.calendar.refetchEvents();
    } catch (error) {
        setInlineError(detailError, errorMessage(error));
    } finally {
        trigger.disabled = false;
    }
}

function moveCalendar(action) {
    if (!state.calendar) {
        return;
    }
    const methods = {
        today: 'today',
        previous: 'prev',
        next: 'next',
    };
    state.calendar[methods[action]]();
}

all('[data-calendar-action]').forEach((button) => {
    button.addEventListener('click', () => moveCalendar(button.dataset.calendarAction));
});
all('[data-calendar-view]').forEach((button) => {
    button.addEventListener('click', () => {
        state.calendar?.changeView(button.dataset.calendarView);
    });
});
teamFilter.addEventListener('change', () => {
    state.selectedTeamId = Number(teamFilter.value) || null;
    const selectedName = state.selectedTeamId ? teamName(state.selectedTeamId) : '전체';
    lookup('[data-calendar-filter-help]').textContent = state.selectedTeamId
        ? `${selectedName} 일정과 동아리 전체 일정을 함께 보여드려요.`
        : '모든 팀 일정과 동아리 전체 일정을 함께 보여드려요.';
    state.calendar?.refetchEvents();
});
document.getElementById('ceAllDay').addEventListener('change', toggleAllDayMode);
retryButton.addEventListener('click', async () => {
    state.referenceFailed = false;
    await loadReferences();
    state.calendar?.refetchEvents();
});

bindPageActions({
    [ACTIONS.CREATE]: (trigger) => openCreate(trigger),
    [ACTIONS.EDIT]: openEdit,
    [ACTIONS.SAVE]: saveEvent,
    [ACTIONS.DELETE]: deleteEvent,
});

initializeDateTimeFields();
loadReferences().finally(createCalendar);
