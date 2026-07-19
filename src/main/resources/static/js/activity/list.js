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
    const proofFile = document.getElementById('acProof');
    if (!proofFile.files.length) {
        showToast('네이비즘 인증 사진을 첨부해 주세요');
        proofFile.focus();
        return;
    }
    const team = currentUserRole === 'leader'
        ? '무대팀'
        : currentUserRole === 'admin' ? '운영진' : '배우팀';
    const card = element('div', 'rounded-lg border bg-card p-4 md:p-5');
    const layout = element('div', 'flex flex-col gap-3 md:flex-row md:items-start');
    layout.appendChild(element(
        'div',
        'flex h-28 w-full shrink-0 items-center justify-center rounded-md border bg-secondary '
            + 'text-xs font-bold text-muted-foreground md:w-40',
        '네이비즘 인증 사진'
    ));
    const content = element('div', 'min-w-0 flex-1');
    const header = element('div', 'mb-2 flex flex-wrap items-center gap-2');
    header.appendChild(badge(team, 'neutral'));
    const date = readValue('acDate') || today();
    const people = readValue('acPpl') || '0';
    header.appendChild(element('span', 'text-xs text-muted-foreground', `${date} · 참여 ${people}명`));
    const status = element('span', 'md:ml-auto');
    status.appendChild(badge('검수 대기', 'info'));
    header.appendChild(status);
    content.append(
        header,
        element('p', 'text-sm leading-relaxed', body),
        element('p', 'mt-3 text-xs text-muted-foreground', '인증 사진 1장 · 방금 제출')
    );
    layout.appendChild(content);
    card.appendChild(layout);
    lookup('[data-activity-list]').prepend(card);
    closeActionModal(trigger);
    showToast('활동을 기록했어요');
}

bindPageActions({[ACTIONS.ADD]: addActivity});
