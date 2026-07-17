import {showToast} from '../common/toast.js';
import {bindPageActions, element, lookup, readValue} from '../common/dom.js';
import {currentUserRole, memberProfiles} from '../common/session.js';
import {closeActionModal, today} from '../common/view.js';

const ACTIONS = Object.freeze({
    CHECK_IN: 'attendance-checkin',
    ADD_EVENT: 'event-add'
});

function attendanceIdentity() {
    return memberProfiles[currentUserRole] || memberProfiles.member;
}

function checkInAttendance(trigger) {
    if (trigger.dataset.checkedIn === 'true') {
        return;
    }
    const card = trigger.closest('[data-event-card]');
    const list = lookup('[data-attendance-list]', card);
    const identity = attendanceIdentity();
    const chipClasses = 'inline-flex items-center gap-1.5 rounded-full bg-secondary '
        + 'py-1 pl-1 pr-2.5 text-xs font-bold';
    const chip = element('span', chipClasses);
    const avatarClasses = 'flex size-5 items-center justify-center rounded-full bg-primary '
        + 'text-xs font-black text-primary-foreground';
    chip.append(element('span', avatarClasses, identity[1]), document.createTextNode(identity[0]));
    list.appendChild(chip);
    trigger.dataset.checkedIn = 'true';
    trigger.textContent = '체크인 완료';
    trigger.classList.remove('bg-primary', 'text-primary-foreground');
    trigger.classList.add('border', 'bg-card');
    lookup('[data-attendance-count]', card).textContent = `참가 ${list.children.length}명`;
    showToast('체크인 완료!');
}

function addEvent(trigger) {
    const name = readValue('evName');
    if (!name) {
        showToast('행사명을 입력해 주세요');
        return;
    }
    const wrapper = element('div', 'mt-4');
    wrapper.dataset.eventCard = '';
    const section = element('section', 'overflow-hidden rounded-lg border bg-card');
    const header = element('header', 'flex items-center gap-2 border-b px-5 py-4');
    const date = readValue('evDate') || today();
    const place = readValue('evPlace') || '미정';
    header.append(
        element('h3', 'text-sm font-extrabold', name),
        element('span', 'ml-auto text-xs font-bold text-muted-foreground', `${date} · ${place}`)
    );
    const body = element('div', 'p-5');
    const actionRow = element('div', 'mb-3.5 flex items-center');
    const count = element('b', 'text-sm', '참가 0명');
    count.dataset.attendanceCount = '';
    const buttonTemplate = lookup('[data-attendance-checkin-template]');
    const button = buttonTemplate.content.firstElementChild.cloneNode(true);
    actionRow.append(count, button);
    const list = element('div', 'flex flex-wrap gap-2');
    list.dataset.attendanceList = '';
    body.append(actionRow, list);
    section.append(header, body);
    wrapper.appendChild(section);
    const firstEvent = lookup('[data-event-card]');
    firstEvent.parentElement.insertBefore(wrapper, firstEvent);
    closeActionModal(trigger);
    showToast('행사를 생성했어요');
}

bindPageActions({
    [ACTIONS.CHECK_IN]: checkInAttendance,
    [ACTIONS.ADD_EVENT]: addEvent
});
