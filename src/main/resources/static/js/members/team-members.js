import {ApiError, get, patch} from '../common/api.js';
import {element, lookup, readValue} from '../common/dom.js';
import {showToast} from '../common/toast.js';

let members = [];
let teams = [];
let selectedMember = null;

function messageFrom(error) {
    if (error instanceof ApiError && error.fieldErrors.length > 0) {
        return error.fieldErrors[0].reason;
    }
    return error.message || '요청을 처리하지 못했습니다.';
}

function firstCharacter(name) {
    return Array.from(name || '?')[0] || '?';
}

function setState(title, message, retry) {
    lookup('[data-team-members-state-title]').textContent = title;
    lookup('[data-team-members-state-message]').textContent = message;
    lookup('[data-team-members-retry]').classList.toggle('hidden', !retry);
}

function clearRows() {
    lookup('[data-team-members-list]').replaceChildren();
}

function renderMembers() {
    const state = lookup('[data-team-members-state]');
    const list = lookup('[data-team-members-list]');
    clearRows();
    if (members.length === 0) {
        setState('표시할 팀 멤버가 없어요.', '팀 멤버가 생기면 여기에서 소속 팀을 변경할 수 있어요.', false);
        state.classList.remove('hidden');
        list.classList.add('hidden');
        return;
    }
    state.classList.add('hidden');
    list.classList.remove('hidden');
    const template = lookup('[data-team-member-row-template]');
    members.forEach((member) => {
        const row = template.content.firstElementChild.cloneNode(true);
        row.dataset.memberId = String(member.memberId);
        lookup('[data-team-member-initial]', row).textContent = firstCharacter(member.name);
        lookup('[data-team-member-name]', row).textContent = member.name;
        lookup('[data-team-member-meta]', row).textContent =
                `${member.studentNo} · ${member.role === 'LEADER' ? '팀장' : member.role === 'ADMIN' ? '운영진' : '일반 부원'}`;
        lookup('[data-team-member-team]', row).textContent = member.teamName;
        lookup('[data-team-member-change-open]', row).addEventListener('click', () => {
            selectMember(member.memberId);
        });
        list.appendChild(row);
    });
}

function renderTeamOptions() {
    const select = lookup('[data-team-member-team-select]');
    select.replaceChildren();
    teams.forEach((team) => {
        const option = element('option', '', team.name);
        option.value = String(team.teamId);
        option.selected = team.name === selectedMember.teamName;
        select.appendChild(option);
    });
}

function selectMember(memberId) {
    selectedMember = members.find((member) => member.memberId === memberId);
    if (!selectedMember) {
        return;
    }
    lookup('[data-team-member-change-section]').classList.remove('hidden');
    lookup('[data-team-member-change-summary]').textContent =
            `${selectedMember.name} · 현재 ${selectedMember.teamName}`;
    lookup('[data-team-member-reason]').value = '';
    lookup('[data-team-member-change-error]').classList.add('hidden');
    renderTeamOptions();
    lookup('[data-team-member-change-section]').scrollIntoView({block: 'nearest'});
}

async function loadMembers() {
    const root = lookup('[data-team-members-root]');
    setState('팀 멤버를 불러오는 중입니다.', '잠시만 기다려 주세요.', false);
    lookup('[data-team-members-state]').classList.remove('hidden');
    try {
        const [nextMembers, nextTeams] = await Promise.all([
            get('/api/members/team-members'),
            get('/api/members/reference/teams'),
        ]);
        members = nextMembers;
        teams = nextTeams;
        renderMembers();
    } catch (error) {
        setState('팀 멤버를 불러오지 못했어요.', messageFrom(error), true);
    } finally {
        root.setAttribute('aria-busy', 'false');
    }
}

async function changeTeam(event) {
    event.preventDefault();
    if (!selectedMember) {
        return;
    }
    const error = lookup('[data-team-member-change-error]');
    const newTeamId = Number(readValue('teamMemberTeam'));
    const reason = readValue('teamMemberReason');
    const submit = lookup('[data-team-member-change-submit]');
    if (!reason) {
        error.textContent = '변경 사유를 입력해 주세요.';
        error.classList.remove('hidden');
        return;
    }
    submit.disabled = true;
    error.classList.add('hidden');
    try {
        await patch(`/api/members/${selectedMember.memberId}/team`, {newTeamId, reason});
        showToast(`${selectedMember.name}님의 소속 팀을 변경했어요.`);
        selectedMember = null;
        lookup('[data-team-member-change-section]').classList.add('hidden');
        await loadMembers();
    } catch (requestError) {
        error.textContent = messageFrom(requestError);
        error.classList.remove('hidden');
    } finally {
        submit.disabled = false;
    }
}

lookup('[data-team-members-retry]').addEventListener('click', loadMembers);
lookup('[data-team-member-change-form]').addEventListener('submit', changeTeam);
lookup('[data-team-member-change-cancel]').addEventListener('click', () => {
    selectedMember = null;
    lookup('[data-team-member-change-section]').classList.add('hidden');
});

loadMembers();
