import {showToast} from '../common/toast.js';
import {bindPageActions, element, lookup, readValue} from '../common/dom.js';
import {currentUserRole} from '../common/session.js';
import {badge, closeActionModal, today} from '../common/view.js';

const ACTIONS = Object.freeze({ADD: 'activity-add'});

function addActivity(trigger) {
    const body = readValue('acBody');
    if (!body) {
        showToast('활동 내용을 입력해 주세요');
        return;
    }
    const team = currentUserRole === 'leader'
        ? '무대팀'
        : currentUserRole === 'admin' ? '운영진' : '배우연출팀';
    const card = element('div', 'rounded-lg border bg-card p-5');
    const header = element('div', 'mb-2 flex items-center gap-2');
    header.appendChild(badge(team, 'neutral'));
    const date = readValue('acDate') || today();
    const people = readValue('acPpl') || '0';
    header.appendChild(element('span', 'text-xs text-muted-foreground', `${date} · 참여 ${people}명`));
    card.append(header, element('p', 'text-sm leading-relaxed', body));
    lookup('[data-activity-list]').prepend(card);
    closeActionModal(trigger);
    showToast('활동을 기록했어요');
}

bindPageActions({[ACTIONS.ADD]: addActivity});
