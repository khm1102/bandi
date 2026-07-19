import {ApiError, get, patch, post} from '../common/api.js';
import {all, bindPageActions, element, lookup, readValue} from '../common/dom.js';
import {openModal} from '../common/modal.js';
import {showToast} from '../common/toast.js';
import {badge, closeActionModal} from '../common/view.js';

const ACTIONS = Object.freeze({
    ADD_MEMBER: 'member-add',
    SAVE_ROLE: 'member-role-save',
    MANAGE_OPEN: 'member-manage-open',
    SAVE_CHANGE: 'member-change-save',
    HISTORY_OPEN: 'member-history-open',
});
const ROLE_LABELS = Object.freeze({
    MEMBER: '일반 부원',
    LEADER: '팀장',
    ADMIN: '운영진',
});
const ROLE_TONES = Object.freeze({
    MEMBER: 'neutral',
    LEADER: 'info',
    ADMIN: 'accent',
});
const SSO_LABELS = Object.freeze({
    WAITING: ['연결 대기', 'warning'],
    LINKED: ['연결 완료', 'success'],
    REVIEW_REQUIRED: ['확인 필요', 'danger'],
});
const STATUS_LABELS = Object.freeze({
    PRE_REGISTERED: '사전 등록',
    ACTIVE: '활동 중',
    SUSPENDED: '활동 중지',
    WITHDRAWN: '탈퇴',
    REGISTRATION_CANCELLED: '등록 취소',
});
const STATUS_TONES = Object.freeze({
    PRE_REGISTERED: 'warning',
    ACTIVE: 'success',
    SUSPENDED: 'danger',
    WITHDRAWN: 'neutral',
    REGISTRATION_CANCELLED: 'neutral',
});
const STATUS_TRANSITIONS = Object.freeze({
    PRE_REGISTERED: ['REGISTRATION_CANCELLED'],
    ACTIVE: ['SUSPENDED', 'WITHDRAWN'],
    SUSPENDED: ['ACTIVE', 'WITHDRAWN'],
    WITHDRAWN: [],
    REGISTRATION_CANCELLED: [],
});

let teamsById = new Map();
let cohortsById = new Map();
let membersById = new Map();
let teams = [];
let cohorts = [];
let pendingRoleChange = null;
let pendingMemberChange = null;

function errorMessage(error) {
    if (error instanceof ApiError && error.fieldErrors.length > 0) {
        return error.fieldErrors[0].reason;
    }
    return error.message || '요청을 처리하지 못했습니다.';
}

function setInlineError(selector, message) {
    const container = lookup(selector);
    container.textContent = message || '';
    container.classList.toggle('hidden', !message);
}

function setState(title, message, retry = false) {
    const state = lookup('[data-member-state]');
    state.hidden = false;
    lookup('[data-member-state-title]', state).textContent = title;
    lookup('[data-member-state-message]', state).textContent = message;
    lookup('[data-member-retry]', state).classList.toggle('hidden', !retry);
}

function clearRows() {
    all('[data-member-list] tr:not([data-member-state])')
            .forEach((row) => row.remove());
}

function setSelectOptions(selectId, items, idKey) {
    const select = document.getElementById(selectId);
    const placeholder = select.firstElementChild;
    select.replaceChildren(placeholder);
    items.forEach((item) => {
        const option = element('option', '', item.name);
        option.value = item[idKey];
        select.appendChild(option);
    });
}

function prepareRoleButtons(row, role) {
    all('[data-member-role]', row).forEach((button) => {
        const selected = button.dataset.memberRole === role;
        button.setAttribute('aria-pressed', String(selected));
        button.disabled = selected;
        button.classList.toggle('border', selected);
        button.classList.toggle('bg-card', selected);
        button.classList.toggle('text-foreground', selected);
        button.classList.toggle('text-muted-foreground', !selected);
    });
}

function appendMemberRow(member) {
    const template = lookup('[data-member-row-template]');
    const row = template.content.firstElementChild.cloneNode(true);
    row.dataset.memberId = member.memberId;
    lookup('[data-member-avatar]', row).textContent =
            Array.from(member.name)[0] || '?';
    lookup('[data-member-name]', row).textContent = member.name;
    lookup('[data-member-student-no]', row).textContent = member.studentNo;
    lookup('[data-member-cohort]', row).appendChild(badge(
            cohortsById.get(member.cohortId)?.name || '미분류', 'info'));
    lookup('[data-member-team]', row).textContent =
            teamsById.get(member.teamId)?.name || '미배정';
    lookup('[data-member-status]', row).appendChild(badge(
            STATUS_LABELS[member.status] || member.status,
            STATUS_TONES[member.status] || 'neutral'));
    const [ssoLabel, ssoTone] = SSO_LABELS[member.ssoLinkStatus]
            || ['확인 필요', 'danger'];
    lookup('[data-member-sso]', row).appendChild(badge(ssoLabel, ssoTone));
    lookup('[data-member-role-cell]', row).appendChild(badge(
            ROLE_LABELS[member.role] || member.role,
            ROLE_TONES[member.role] || 'neutral'));
    prepareRoleButtons(row, member.role);
    lookup('[data-member-list]').appendChild(row);
}

function renderStats(members, cohorts) {
    lookup('[data-stat-value="active-members"]').textContent =
            members.filter((member) => member.status === 'ACTIVE').length;
    lookup('[data-stat-value="active-cohorts"]').textContent = cohorts.length;
    lookup('[data-stat-delta="active-cohort-names"]').textContent =
            cohorts.length > 0 ? cohorts.map((cohort) => cohort.name).join(' · ')
                : '운영 중인 기수가 없습니다';
    lookup('[data-stat-value="waiting-sso"]').textContent =
            members.filter((member) => member.ssoLinkStatus !== 'LINKED')
                    .length;
}

function renderMembers(members) {
    clearRows();
    if (members.length === 0) {
        setState('등록된 멤버가 없습니다',
                '멤버를 사전 등록하면 학교 SSO 연결을 시작할 수 있습니다.');
        return;
    }
    lookup('[data-member-state]').hidden = true;
    members.forEach(appendMemberRow);
}

async function loadMembers() {
    setState('멤버 목록을 불러오는 중입니다', '잠시만 기다려 주세요.');
    clearRows();
    try {
        const [members, nextTeams, nextCohorts] = await Promise.all([
            get('/api/members'),
            get('/api/members/reference/teams'),
            get('/api/members/reference/cohorts'),
        ]);
        teams = nextTeams;
        cohorts = nextCohorts;
        membersById = new Map(members.map((member) => [member.memberId,
            member]));
        teamsById = new Map(teams.map((team) => [team.teamId, team]));
        cohortsById = new Map(cohorts.map((cohort) => [cohort.cohortId,
            cohort]));
        setSelectOptions('mbTeam', teams, 'teamId');
        setSelectOptions('mbCohort', cohorts, 'cohortId');
        renderStats(members, cohorts);
        renderMembers(members);
    } catch (error) {
        setState('멤버 목록을 불러오지 못했습니다', errorMessage(error), true);
    }
}

function resetMemberForm() {
    ['mbStudentNo', 'mbName', 'mbTeam', 'mbCohort'].forEach((id) => {
        document.getElementById(id).value = '';
    });
    setInlineError('[data-member-form-error]', '');
}

async function addMember(trigger) {
    const request = {
        studentNo: readValue('mbStudentNo'),
        name: readValue('mbName'),
        teamId: Number(readValue('mbTeam')) || null,
        cohortId: Number(readValue('mbCohort')) || null,
    };
    setInlineError('[data-member-form-error]', '');
    trigger.disabled = true;
    try {
        await post('/api/members', request);
        closeActionModal(trigger);
        showToast(`${request.name}님을 사전 등록했습니다.`);
        resetMemberForm();
        await loadMembers();
    } catch (error) {
        setInlineError('[data-member-form-error]', errorMessage(error));
    } finally {
        trigger.disabled = false;
    }
}

function prepareRoleChange(button) {
    const row = button.closest('tr');
    pendingRoleChange = {
        memberId: row.dataset.memberId,
        memberName: lookup('[data-member-name]', row).textContent.trim(),
        newRole: button.dataset.memberRole,
    };
    lookup('[data-member-role-summary]').textContent =
            `${pendingRoleChange.memberName}님의 권한을 ${ROLE_LABELS[pendingRoleChange.newRole]}(으)로 변경합니다.`;
    document.getElementById('memberRoleReason').value = '';
    setInlineError('[data-member-role-error]', '');
    openModal('memberRoleModal', button);
}

async function saveRole(trigger) {
    const reason = readValue('memberRoleReason');
    if (!pendingRoleChange) {
        return;
    }
    setInlineError('[data-member-role-error]', '');
    trigger.disabled = true;
    try {
        await patch(`/api/members/${pendingRoleChange.memberId}/role`, {
            newRole: pendingRoleChange.newRole,
            reason,
        });
        closeActionModal(trigger);
        showToast(`${pendingRoleChange.memberName}님의 권한을 변경했습니다.`);
        pendingRoleChange = null;
        await loadMembers();
    } catch (error) {
        setInlineError('[data-member-role-error]', errorMessage(error));
    } finally {
        trigger.disabled = false;
    }
}

function appendChangeOption(select, value, label) {
    const option = element('option', '', label);
    option.value = String(value);
    select.appendChild(option);
}

function updateChangeOptions() {
    if (!pendingMemberChange) {
        return;
    }
    const type = readValue('memberChangeType');
    const select = document.getElementById('memberChangeValue');
    select.replaceChildren();
    if (type === 'team') {
        teams.filter((team) => team.teamId !== pendingMemberChange.teamId)
                .forEach((team) => appendChangeOption(select, team.teamId,
                        team.name));
    } else if (type === 'cohort') {
        cohorts.filter((cohort) => cohort.cohortId
                        !== pendingMemberChange.cohortId)
                .forEach((cohort) => appendChangeOption(select,
                        cohort.cohortId, cohort.name));
    } else {
        STATUS_TRANSITIONS[pendingMemberChange.status]
                .forEach((status) => appendChangeOption(select, status,
                        STATUS_LABELS[status]));
    }
    const saveButton = lookup('[data-page-action="member-change-save"]');
    saveButton.disabled = select.options.length === 0;
    if (select.options.length === 0) {
        appendChangeOption(select, '', '변경 가능한 값이 없습니다');
    }
}

function prepareMemberChange(trigger) {
    const row = trigger.closest('tr');
    const member = membersById.get(Number(row.dataset.memberId));
    pendingMemberChange = {...member};
    lookup('[data-member-change-summary]').textContent =
            `${member.name} · ${teamsById.get(member.teamId)?.name || '미배정'} · ${STATUS_LABELS[member.status]}`;
    document.getElementById('memberChangeType').value = 'team';
    document.getElementById('memberChangeReason').value = '';
    setInlineError('[data-member-change-error]', '');
    updateChangeOptions();
    openModal('memberManageModal', trigger);
}

async function saveMemberChange(trigger) {
    if (!pendingMemberChange) {
        return;
    }
    const type = readValue('memberChangeType');
    const value = readValue('memberChangeValue');
    const reason = readValue('memberChangeReason');
    if (!value || !reason) {
        setInlineError('[data-member-change-error]',
                '변경 값과 사유를 모두 입력해 주세요.');
        return;
    }
    const bodies = {
        team: {newTeamId: Number(value), reason},
        cohort: {newCohortId: Number(value), reason},
        status: {newStatus: value, reason},
    };
    trigger.disabled = true;
    setInlineError('[data-member-change-error]', '');
    try {
        await patch(`/api/members/${pendingMemberChange.memberId}/${type}`,
                bodies[type]);
        closeActionModal(trigger);
        showToast(`${pendingMemberChange.name}님의 정보를 변경했습니다.`);
        pendingMemberChange = null;
        await loadMembers();
    } catch (error) {
        setInlineError('[data-member-change-error]', errorMessage(error));
    } finally {
        trigger.disabled = false;
    }
}

function historyValue(type, value) {
    if (value === null || value === undefined) {
        return '없음';
    }
    if (type === '팀') {
        return teamsById.get(value)?.name || `팀 #${value}`;
    }
    if (type === '기수') {
        return cohortsById.get(value)?.name || `기수 #${value}`;
    }
    if (type === '권한') {
        return ROLE_LABELS[value] || value;
    }
    return STATUS_LABELS[value] || value;
}

function formatDateTime(value) {
    return new Intl.DateTimeFormat('ko-KR', {
        year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit', hour12: false,
    }).format(new Date(value));
}

function flattenHistories(response) {
    const definitions = [
        ['팀', response.teamHistories, 'previousTeamId', 'newTeamId'],
        ['기수', response.cohortHistories, 'previousCohortId', 'newCohortId'],
        ['권한', response.roleHistories, 'previousRole', 'newRole'],
        ['상태', response.statusHistories, 'previousStatus', 'newStatus'],
    ];
    return definitions.flatMap(([type, histories, previousKey, newKey]) =>
        histories.map((history) => ({
            ...history,
            type,
            previousValue: history[previousKey],
            newValue: history[newKey],
        }))).sort((left, right) =>
        new Date(right.changedDttm) - new Date(left.changedDttm));
}

async function showMemberHistory(trigger) {
    const row = trigger.closest('tr');
    const member = membersById.get(Number(row.dataset.memberId));
    const region = lookup('[data-member-history]');
    region.replaceChildren(element('p', 'py-8 text-center text-sm text-muted-foreground',
            '이력을 불러오는 중입니다.'));
    openModal('memberHistoryModal', trigger);
    try {
        const response = await get(`/api/members/${member.memberId}/histories`);
        const histories = flattenHistories(response);
        region.replaceChildren(element('strong', 'mb-2 text-sm', member.name));
        if (histories.length === 0) {
            region.appendChild(element('p',
                    'rounded-md bg-secondary px-4 py-3 text-sm text-muted-foreground',
                    '변경 이력이 없습니다.'));
        }
        histories.forEach((history) => {
            const card = element('article', 'rounded-lg border px-4 py-3');
            const head = element('div', 'flex flex-wrap items-center gap-2');
            head.append(badge(history.type, 'info'),
                    element('strong', 'text-sm',
                            `${historyValue(history.type, history.previousValue)} → ${historyValue(history.type, history.newValue)}`));
            const actor = membersById.get(history.changedByMemberId)?.name
                    || `멤버 #${history.changedByMemberId}`;
            card.append(head, element('p',
                    'mt-2 text-xs text-muted-foreground',
                    `${actor} · ${formatDateTime(history.changedDttm)}`));
            if (history.reason) {
                card.appendChild(element('p', 'mt-2 text-sm', history.reason));
            }
            region.appendChild(card);
        });
    } catch (error) {
        region.replaceChildren(element('p',
                'rounded-md bg-destructive-soft px-4 py-3 text-sm text-destructive',
                errorMessage(error)));
    }
}

lookup('[data-member-retry]').addEventListener('click', loadMembers);
lookup('[data-member-list]').addEventListener('click', (event) => {
    const button = event.target.closest('[data-member-role]');
    if (button && !button.disabled) {
        prepareRoleChange(button);
    }
});
document.getElementById('memberChangeType')
        .addEventListener('change', updateChangeOptions);

bindPageActions({
    [ACTIONS.ADD_MEMBER]: addMember,
    [ACTIONS.SAVE_ROLE]: saveRole,
    [ACTIONS.MANAGE_OPEN]: prepareMemberChange,
    [ACTIONS.SAVE_CHANGE]: saveMemberChange,
    [ACTIONS.HISTORY_OPEN]: showMemberHistory,
});

loadMembers();
