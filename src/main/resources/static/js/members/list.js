import {showToast} from '../common/toast.js';
import {all, bindPageActions, lookup, readValue} from '../common/dom.js';
import {badge, closeActionModal} from '../common/view.js';

const ACTIONS = Object.freeze({ADD_MEMBER: 'member-add'});

function memberName(row) {
    return lookup('[data-member-name]', row).textContent.trim();
}

function prepareMemberRoleButton(row, button) {
    const name = memberName(row);
    const roleCell = lookup('[data-member-role-cell]', row);
    const selected = button.textContent.trim() === roleCell.textContent.trim();
    button.dataset.memberRole = '';
    button.setAttribute('aria-pressed', String(selected));
    button.classList.toggle('border', selected);
    button.classList.toggle('bg-card', selected);
    button.classList.toggle('text-foreground', selected);
    button.classList.toggle('text-muted-foreground', !selected);
    if (selected) {
        delete button.dataset.confirm;
        delete button.dataset.confirmAction;
        return;
    }
    button.dataset.confirm = `${name}님의 권한을 ${button.textContent.trim()}(으)로 변경할까요?`;
    button.dataset.confirmAction = '권한 변경';
}

function changeMemberRole(row, button) {
    const roleName = button.textContent.trim();
    const tone = roleName === '운영진' ? 'accent' : roleName === '팀장' ? 'info' : 'neutral';
    lookup('[data-member-role-cell]', row).replaceChildren(badge(roleName, tone));
    all('td:last-child button', row).forEach((candidate) => prepareMemberRoleButton(row, candidate));
    showToast(`${memberName(row)}님 권한을 ${roleName}(으)로 변경했어요`);
}

function addMember(trigger) {
    const studentNo = readValue('mbStudentNo');
    const name = readValue('mbName');
    if (!studentNo) {
        showToast('학번을 입력해 주세요');
        return;
    }
    if (!name) {
        showToast('이름을 입력해 주세요');
        return;
    }
    const cohort = readValue('mbCohort') || '미분류';
    const team = readValue('mbTeam');
    const roleName = readValue('mbRole');
    const template = lookup('[data-member-row-template]');
    const row = template.content.firstElementChild.cloneNode(true);
    lookup('[data-member-avatar]', row).textContent = name.slice(0, 2);
    lookup('[data-member-name]', row).textContent = name;
    lookup('[data-member-student-no]', row).textContent = studentNo;
    lookup('[data-member-cohort]', row).appendChild(badge(cohort, 'info'));
    lookup('[data-member-team]', row).textContent = team;
    lookup('[data-member-sso]', row).appendChild(badge('연결 대기', 'warning'));
    const tone = roleName === '운영진' ? 'accent' : roleName === '팀장' ? 'info' : 'neutral';
    lookup('[data-member-role-cell]', row).appendChild(badge(roleName, tone));
    all('td:last-child button', row).forEach((button) => prepareMemberRoleButton(row, button));
    lookup('[data-member-list]').appendChild(row);
    closeActionModal(trigger);
    showToast(`${name}님을 사전 등록했어요. 첫 학교 로그인 후 계정이 연결됩니다`);
}

all('[data-member-list] tr').forEach((row) => {
    all('td:last-child button', row).forEach((button) => prepareMemberRoleButton(row, button));
});

document.addEventListener('click', (event) => {
    const button = event.target.closest('[data-member-role]');
    if (!button) {
        return;
    }
    changeMemberRole(button.closest('tr'), button);
});

bindPageActions({[ACTIONS.ADD_MEMBER]: addMember});
