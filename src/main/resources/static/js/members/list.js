import {showToast} from '../common/toast.js';
import {all, bindPageActions, lookup, readValue} from '../common/dom.js';
import {badge, closeActionModal, today} from '../common/view.js';

const ACTIONS = Object.freeze({
    COPY_INVITE: 'invite-copy',
    TOGGLE_INVITE: 'invite-toggle',
    ADD_INVITE: 'invite-add',
    ADD_MEMBER: 'member-add'
});

function prepareMemberRoleButton(row, button) {
    const memberName = row.cells[0].textContent.trim();
    const currentRole = row.cells[3].textContent.trim();
    const selected = button.textContent.trim() === currentRole;
    button.dataset.memberRole = '';
    button.setAttribute('aria-pressed', String(selected));
    if (!selected) {
        button.dataset.confirm = `${memberName}님의 권한을 ${button.textContent.trim()}(으)로 변경할까요?`;
        button.dataset.confirmAction = '권한 변경';
    }
}

function changeMemberRole(row, button) {
    const roleName = button.textContent.trim();
    const tone = roleName === '운영진' ? 'accent' : roleName === '팀장' ? 'info' : 'neutral';
    row.cells[3].replaceChildren(badge(roleName, tone));
    all('td:last-child button', row).forEach((candidate) => {
        const selected = candidate === button;
        candidate.classList.remove('border', 'bg-card', 'text-foreground');
        candidate.classList.add('text-muted-foreground');
        candidate.setAttribute('aria-pressed', String(selected));
        if (selected) {
            delete candidate.dataset.confirm;
            delete candidate.dataset.confirmAction;
            return;
        }
        const memberName = row.cells[0].textContent.trim();
        candidate.dataset.confirm = `${memberName}님의 권한을 ${candidate.textContent.trim()}(으)로 변경할까요?`;
        candidate.dataset.confirmAction = '권한 변경';
    });
    button.classList.add('border', 'bg-card', 'text-foreground');
    button.classList.remove('text-muted-foreground');
    showToast(`${row.cells[0].textContent.trim()}님 권한을 ${roleName}(으)로 변경했어요`);
}

function inviteCard(code, cohort) {
    const template = lookup('[data-invite-card-template]');
    const card = template.content.firstElementChild.cloneNode(true);
    lookup('[data-invite-cohort]', card).replaceWith(badge(cohort, 'accent'));
    lookup('[data-invite-code]', card).textContent = code;
    lookup('[data-invite-meta]', card).textContent = `${today()} 생성 · 가입 0명 · 사용 가능`;
    return card;
}

function addInvite(trigger) {
    const cohort = readValue('ivCohort');
    if (!cohort) {
        showToast('기수를 입력해 주세요');
        return;
    }
    const clean = cohort.replace(/[^0-9]/g, '').slice(0, 3) || 'NEW';
    const random = Math.random().toString(36).slice(2, 6).toUpperCase();
    const code = `BANDI-${clean}-${random}`;
    lookup('[data-invite-list]').prepend(inviteCard(code, cohort));
    closeActionModal(trigger);
    showToast(`${cohort} 초대코드를 생성했어요`);
}

function addMember(trigger) {
    const name = readValue('mbName');
    if (!name) {
        showToast('이름을 입력해 주세요');
        return;
    }
    const cohort = readValue('mbCohort') || '미분류';
    const team = readValue('mbTeam') || '미소속';
    const roleName = readValue('mbRole');
    const template = lookup('[data-member-row-template]');
    const row = template.content.firstElementChild.cloneNode(true);
    lookup('[data-member-avatar]', row).textContent = name.slice(0, 2);
    lookup('[data-member-name]', row).textContent = name;
    lookup('[data-member-cohort]', row).appendChild(badge(cohort, 'info'));
    lookup('[data-member-team]', row).textContent = team;
    const roleCell = lookup('[data-member-role-cell]', row);
    const tone = roleName === '운영진' ? 'accent' : roleName === '팀장' ? 'info' : 'neutral';
    roleCell.appendChild(badge(roleName, tone));
    all('td:last-child button', row).forEach((button) => prepareMemberRoleButton(row, button));
    lookup('[data-member-list]').appendChild(row);
    closeActionModal(trigger);
    showToast(`${name}님을 ${team} 소속으로 추가했어요`);
}

async function copyInviteCode(trigger) {
    const code = lookup('[data-invite-code]', trigger.closest('[data-invite-card]')).textContent.trim();
    if (!navigator.clipboard) {
        showToast('이 브라우저에서는 클립보드 복사를 지원하지 않아요');
        return;
    }
    try {
        await navigator.clipboard.writeText(code);
        showToast(`${code} 복사 완료`);
    } catch {
        showToast('초대코드를 복사하지 못했어요');
    }
}

all('[data-member-list] tr').forEach((row) => {
    all('td:last-child button', row).forEach((button) => prepareMemberRoleButton(row, button));
});

document.addEventListener('click', (event) => {
    const button = event.target.closest('[data-member-role]');
    if (button) {
        changeMemberRole(button.closest('tr'), button);
    }
});

bindPageActions({
    [ACTIONS.COPY_INVITE]: copyInviteCode,
    [ACTIONS.TOGGLE_INVITE]: (trigger) => {
        const activating = trigger.textContent.trim() === '활성화';
        const card = trigger.closest('[data-invite-card]');
        const metadata = lookup('[data-invite-meta]', card);
        trigger.textContent = activating ? '중지' : '활성화';
        metadata.textContent = metadata.textContent.replace(
            /사용 가능|사용 중지$/,
            activating ? '사용 가능' : '사용 중지'
        );
        showToast(activating ? '초대코드를 활성화했어요' : '초대코드를 사용 중지했어요');
    },
    [ACTIONS.ADD_INVITE]: addInvite,
    [ACTIONS.ADD_MEMBER]: addMember
});
