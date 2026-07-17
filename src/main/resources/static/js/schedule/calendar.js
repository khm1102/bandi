import {showToast} from '../common/toast.js';
import {all, bindPageActions, element, lookup, readValue} from '../common/dom.js';
import {activateFilterChip, closeActionModal} from '../common/view.js';

const ACTIONS = Object.freeze({
    PREVIOUS: 'calendar-prev',
    NEXT: 'calendar-next',
    ADD: 'calendar-add'
});

const calendarState = {
    month: 5,
    filter: '전체',
    events: []
};

function visibleEvents(day) {
    return calendarState.events.filter((eventData) => eventData.month === calendarState.month
        && eventData.day === day
        && (calendarState.filter === '전체' || eventData.team === calendarState.filter));
}

function calendarEventNode(eventData) {
    const hot = eventData.title.includes('공연');
    const teamClasses = {
        배우연출: 'bg-secondary text-muted-foreground',
        무대: 'bg-info-soft text-info',
        오퍼: 'bg-accent text-accent-foreground',
        디자인: 'bg-warning-soft text-warning',
        영상: 'bg-secondary text-muted-foreground'
    };
    const classes = hot
        ? 'mt-1 block truncate rounded-sm bg-accent px-1 py-0.5 text-xs font-bold text-accent-foreground'
        : `mt-1 block truncate rounded-sm px-1 py-0.5 text-xs font-bold ${teamClasses[eventData.team]}`;
    const node = element('button', `${classes} w-full text-left`, eventData.title);
    node.type = 'button';
    node.title = eventData.place;
    node.dataset.calendarEventButton = '';
    node.dataset.eventTeam = eventData.team;
    return node;
}

function renderCalendar() {
    const grid = lookup('[data-calendar-grid]');
    if (!grid) {
        return;
    }
    grid.replaceChildren();
    ['일', '월', '화', '수', '목', '금', '토'].forEach((dayName) => {
        const classes = 'py-1 text-center text-xs font-extrabold text-muted-foreground';
        grid.appendChild(element('div', classes, dayName));
    });
    const firstDay = new Date(2025, calendarState.month, 1).getDay();
    const dayCount = new Date(2025, calendarState.month + 1, 0).getDate();
    for (let index = 0; index < firstDay; index += 1) {
        grid.appendChild(element('div', 'min-h-20 rounded-md bg-secondary/50 opacity-50'));
    }
    for (let day = 1; day <= dayCount; day += 1) {
        const isToday = calendarState.month === 5 && day === 20;
        const classes = isToday
            ? 'min-h-20 cursor-pointer rounded-md border border-primary bg-card p-1.5 ring-2 ring-ring/20'
            : 'min-h-20 cursor-pointer rounded-md border bg-card p-1.5 transition-colors hover:border-primary';
        const cell = element('div', classes);
        cell.dataset.calendarDay = String(day);
        const dayClasses = `text-xs font-extrabold ${isToday ? 'text-accent-foreground' : ''}`;
        cell.appendChild(element('span', dayClasses, String(day)));
        visibleEvents(day).forEach((eventData) => cell.appendChild(calendarEventNode(eventData)));
        grid.appendChild(cell);
    }
    lookup('[data-calendar-month]').textContent = `2025년 ${calendarState.month + 1}월`;
    const filterName = calendarState.filter === '전체' ? '전체 팀' : `${calendarState.filter}팀`;
    lookup('[data-calendar-filter-label]').textContent = `${filterName} 일정 표시 중`;
}

function addCalendarEvent(trigger) {
    const title = readValue('ceTitle');
    if (!title) {
        showToast('일정명을 입력해 주세요');
        return;
    }
    calendarState.events.push({
        month: calendarState.month,
        day: Number(readValue('ceDay')) || 25,
        title,
        team: readValue('ceTeam'),
        place: readValue('ceLoc') || '미정'
    });
    calendarState.filter = '전체';
    const allFilter = lookup('[data-filter-group="calendar"][data-filter-value="전체"]');
    if (allFilter) {
        activateFilterChip(allFilter);
    }
    closeActionModal(trigger);
    renderCalendar();
    showToast('일정을 등록했어요');
}

calendarState.events = all('[data-calendar-event]').map((eventNode) => ({
    month: 5,
    day: Number(eventNode.closest('[data-calendar-day]').dataset.calendarDay),
    title: eventNode.textContent.trim(),
    team: eventNode.dataset.team,
    place: eventNode.title
}));

document.addEventListener('click', (event) => {
    const filter = event.target.closest('[data-filter-group="calendar"]');
    if (filter) {
        calendarState.filter = filter.dataset.filterValue;
        activateFilterChip(filter);
        renderCalendar();
        return;
    }
    const eventButton = event.target.closest('[data-calendar-event-button]');
    if (eventButton) {
        showToast(`${eventButton.textContent} · ${eventButton.dataset.eventTeam}팀 · ${eventButton.title}`);
        return;
    }
    const dayCell = event.target.closest('[data-calendar-day]');
    if (!dayCell) {
        return;
    }
    const day = Number(dayCell.dataset.calendarDay);
    const events = visibleEvents(day);
    const summary = events.length > 0
        ? events.map((item) => `${item.title} (${item.team}팀·${item.place})`).join(' / ')
        : '일정 없음';
    showToast(`${calendarState.month + 1}월 ${day}일 · ${summary}`);
});

bindPageActions({
    [ACTIONS.PREVIOUS]: () => {
        calendarState.month = Math.max(0, calendarState.month - 1);
        renderCalendar();
    },
    [ACTIONS.NEXT]: () => {
        calendarState.month = Math.min(11, calendarState.month + 1);
        renderCalendar();
    },
    [ACTIONS.ADD]: addCalendarEvent
});

renderCalendar();
