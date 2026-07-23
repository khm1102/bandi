import {ApiError, get, patch} from '../common/api.js';
import {debounce, element, lookup, readValue} from '../common/dom.js';
import {renderPagination, readPageFromUrl, setUrlPage, writeUrl, normalizePage} from '../common/pagination.js';
import {showToast} from '../common/toast.js';

const PAGE_SIZE = 20;
const root = lookup('[data-team-members-root]');
const searchInput = lookup('[data-team-members-search]');
const pagination = lookup('[data-pagination]');
let members = [];
let teams = [];
let selectedMember = null;
let requestGeneration = 0;

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

function readUrlState() {
    const params = new URLSearchParams(window.location.search);
    return {
        params,
        query: params.get('q') || '',
        team: params.get('team') || '',
        status: params.get('status') || '',
        page: readPageFromUrl(params),
    };
}

function syncControls(urlState) {
    searchInput.value = urlState.query;
    const team = lookup('[data-team-members-filter="team"]', root);
    if (team) {
        team.value = urlState.team;
    }
    lookup('[data-team-members-filter="status"]').value = urlState.status;
    lookup('[data-team-members-reset]').classList.toggle('hidden',
            !Boolean(urlState.query || urlState.team || urlState.status));
}

function clearRows() {
    lookup('[data-team-members-list]').replaceChildren();
}

function renderMembers(filtered) {
    const state = lookup('[data-team-members-state]');
    const list = lookup('[data-team-members-list]');
    clearRows();
    if (members.length === 0) {
        setState(filtered ? '조건에 맞는 팀 멤버가 없어요.' : '표시할 팀 멤버가 없어요.',
                filtered ? '검색어나 필터를 초기화해 보세요.'
                    : '팀 멤버가 생기면 여기에서 소속 팀을 변경할 수 있어요.', false);
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
        const role = member.role === 'LEADER' ? '팀장' : member.role === 'ADMIN' ? '운영진' : '일반 부원';
        lookup('[data-team-member-meta]', row).textContent = `${member.studentNo} · ${role}`;
        lookup('[data-team-member-team]', row).textContent = member.teamName;
        lookup('[data-team-member-change-open]', row).addEventListener('click', () => selectMember(member.memberId));
        list.appendChild(row);
    });
}

function renderTeamOptions() {
    const select = lookup('[data-team-member-team-select]');
    select.replaceChildren();
    teams.forEach((team) => {
        const option = element('option', '', team.name);
        option.value = String(team.teamId);
        option.selected = team.teamId === selectedMember.teamId;
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

function replaceFilters(changes) {
    const urlState = readUrlState();
    Object.entries(changes).forEach(([key, value]) => {
        if (value) {
            urlState.params.set(key, value);
        } else {
            urlState.params.delete(key);
        }
    });
    setUrlPage(urlState.params, 0);
    writeUrl(urlState.params, false);
    loadMembers();
}

function changePage(page) {
    const urlState = readUrlState();
    setUrlPage(urlState.params, page);
    writeUrl(urlState.params, true);
    loadMembers(true);
}

async function loadMembers(focus = false) {
    const urlState = readUrlState();
    syncControls(urlState);
    const generation = ++requestGeneration;
    root.setAttribute('aria-busy', 'true');
    setState('팀 멤버를 불러오는 중입니다.', '잠시만 기다려 주세요.', false);
    lookup('[data-team-members-state]').classList.remove('hidden');
    try {
        const response = await get('/api/members/team-members', {
            keyword: urlState.query,
            teamId: urlState.team,
            status: urlState.status,
            page: urlState.page,
            pageSize: PAGE_SIZE,
        });
        if (generation !== requestGeneration) {
            return;
        }
        const normalized = normalizePage(response, urlState.page);
        if (normalized !== urlState.page) {
            setUrlPage(urlState.params, normalized);
            writeUrl(urlState.params, false);
            await loadMembers(focus);
            return;
        }
        members = response.items;
        renderMembers(Boolean(urlState.query || urlState.team || urlState.status));
        if (response.totalElements > 0) {
            renderPagination(pagination, response, changePage);
        } else {
            pagination.classList.add('hidden');
        }
        if (focus && members.length > 0) {
            lookup('[data-team-member-change-open]').focus({preventScroll: true});
            lookup('[data-team-members-list]').scrollIntoView({behavior: 'smooth', block: 'start'});
        }
    } catch (error) {
        if (generation === requestGeneration) {
            setState('팀 멤버를 불러오지 못했어요.', messageFrom(error), true);
        }
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

async function initialize() {
    teams = await get('/api/members/reference/teams');
    const teamFilter = lookup('[data-team-members-filter="team"]', root);
    if (teamFilter) {
        teams.forEach((team) => {
            const option = element('option', '', team.name);
            option.value = String(team.teamId);
            teamFilter.appendChild(option);
        });
    }
    await loadMembers();
}

lookup('[data-team-members-retry]').addEventListener('click', () => loadMembers());
searchInput.addEventListener('input', debounce(() => replaceFilters({q: searchInput.value.trim()}), 300));
root.querySelectorAll('[data-team-members-filter]').forEach((select) => {
    select.addEventListener('change', () => {
        replaceFilters({[select.dataset.teamMembersFilter]: select.value});
    });
});
lookup('[data-team-members-reset]').addEventListener('click', () => {
    writeUrl(new URLSearchParams(), false);
    loadMembers();
});
window.addEventListener('popstate', () => loadMembers(true));
lookup('[data-team-member-change-form]').addEventListener('submit', changeTeam);
lookup('[data-team-member-change-cancel]').addEventListener('click', () => {
    selectedMember = null;
    lookup('[data-team-member-change-section]').classList.add('hidden');
});

initialize().catch((error) => setState('팀 멤버 관리 화면을 준비하지 못했어요.', messageFrom(error), true));
