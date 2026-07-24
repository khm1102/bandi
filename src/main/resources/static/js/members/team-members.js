import {ApiError, get, patch} from '../common/api.js';
import {debounce, element, lookup, readValue} from '../common/dom.js';
import {closeModal, openModal} from '../common/modal.js';
import {renderPagination, readPageFromUrl, setUrlPage, writeUrl, normalizePage} from '../common/pagination.js';
import {showToast} from '../common/toast.js';

const PAGE_SIZE = 20;
const root = lookup('[data-team-members-root]');
const searchInput = lookup('[data-team-members-search]');
const pagination = lookup('[data-pagination]');
const changeModal = lookup('#teamMemberChangeModal');
const changeForm = lookup('[data-team-member-change-form]');
const changeTeamSelect = lookup('[data-team-member-team-select]');
const changeReason = lookup('[data-team-member-reason]');
const changeError = lookup('[data-team-member-change-error]');
const changeSubmit = lookup('[data-team-member-change-submit]');
const cohortModal = lookup('#teamMemberCohortModal');
const cohortForm = lookup('[data-team-member-cohort-form]');
const cohortSelect = lookup('[data-team-member-cohort-select]');
const cohortReason = lookup('[data-team-member-cohort-reason]');
const cohortError = lookup('[data-team-member-cohort-error]');
const cohortSubmit = lookup('[data-team-member-cohort-submit]');
let members = [];
let teams = [];
let cohorts = [];
let selectedTeamMember = null;
let selectedCohortMember = null;
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
        lookup('[data-team-member-phone]', row).textContent = member.phoneNumber
            ? `휴대폰 ${member.phoneNumber}` : '휴대폰 정보 없음';
        lookup('[data-team-member-team]', row).textContent = member.teamName;
        lookup('[data-team-member-cohort]', row).textContent = member.cohortName || '미분류';
        lookup('[data-team-member-change-open]', row).addEventListener('click', (event) => {
            selectTeamMember(member.memberId, event.currentTarget);
        });
        lookup('[data-team-member-cohort-open]', row).addEventListener('click', (event) => {
            selectCohortMember(member.memberId, event.currentTarget);
        });
        list.appendChild(row);
    });
}

function renderTeamOptions() {
    changeTeamSelect.replaceChildren();
    teams.forEach((team) => {
        const option = element('option', '', team.name);
        option.value = String(team.teamId);
        option.selected = team.teamId === selectedTeamMember.teamId;
        changeTeamSelect.appendChild(option);
    });
}

function renderCohortOptions() {
    cohortSelect.replaceChildren();
    cohorts.forEach((cohort) => {
        const option = element('option', '', cohort.name);
        option.value = String(cohort.cohortId);
        option.selected = cohort.cohortId === selectedCohortMember.cohortId;
        cohortSelect.appendChild(option);
    });
}

function clearChangeError() {
    changeError.classList.add('hidden');
    changeError.textContent = '';
    changeTeamSelect.removeAttribute('aria-invalid');
    changeReason.removeAttribute('aria-invalid');
}

function selectTeamMember(memberId, trigger) {
    selectedTeamMember = members.find((member) => member.memberId === memberId);
    if (!selectedTeamMember) {
        return;
    }
    lookup('[data-team-member-change-summary]').textContent =
            `${selectedTeamMember.name} · 현재 ${selectedTeamMember.teamName}`;
    changeReason.value = '';
    clearChangeError();
    renderTeamOptions();
    openModal('teamMemberChangeModal', trigger);
    changeTeamSelect.focus();
}

function selectCohortMember(memberId, trigger) {
    selectedCohortMember = members.find((member) => member.memberId === memberId);
    if (!selectedCohortMember) {
        return;
    }
    lookup('[data-team-member-cohort-summary]').textContent =
            `${selectedCohortMember.name} · 현재 ${selectedCohortMember.cohortName || '미분류'}`;
    cohortReason.value = '';
    clearCohortError();
    renderCohortOptions();
    openModal('teamMemberCohortModal', trigger);
    cohortSelect.focus();
}

function clearSelectedTeamMember() {
    selectedTeamMember = null;
    changeReason.value = '';
    clearChangeError();
}

function clearSelectedCohortMember() {
    selectedCohortMember = null;
    cohortReason.value = '';
    clearCohortError();
}

function clearCohortError() {
    cohortError.classList.add('hidden');
    cohortError.textContent = '';
    cohortSelect.removeAttribute('aria-invalid');
    cohortReason.removeAttribute('aria-invalid');
}

function focusMemberChangeButton(memberId) {
    const row = Array.from(lookup('[data-team-members-list]').querySelectorAll('[data-team-member-row]'))
            .find((candidate) => candidate.dataset.memberId === String(memberId));
    if (!row) {
        return;
    }
    lookup('[data-team-member-change-open]', row).focus({preventScroll: true});
}

function focusMemberCohortButton(memberId) {
    const row = Array.from(lookup('[data-team-members-list]').querySelectorAll('[data-team-member-row]'))
            .find((candidate) => candidate.dataset.memberId === String(memberId));
    if (!row) {
        return;
    }
    lookup('[data-team-member-cohort-open]', row).focus({preventScroll: true});
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
    if (!selectedTeamMember) {
        return;
    }
    const changedMember = selectedTeamMember;
    const newTeamId = Number(readValue('teamMemberTeam'));
    const reason = readValue('teamMemberReason');
    if (!reason) {
        changeError.textContent = '변경 사유를 입력해 주세요.';
        changeError.classList.remove('hidden');
        changeReason.setAttribute('aria-invalid', 'true');
        changeReason.focus();
        return;
    }
    changeSubmit.disabled = true;
    clearChangeError();
    try {
        await patch(`/api/members/${changedMember.memberId}/team`, {newTeamId, reason});
        showToast(`${changedMember.name}님의 소속 팀을 변경했어요.`);
        closeModal(changeModal);
        clearSelectedTeamMember();
        await loadMembers();
        focusMemberChangeButton(changedMember.memberId);
    } catch (requestError) {
        changeError.textContent = messageFrom(requestError);
        changeError.classList.remove('hidden');
    } finally {
        changeSubmit.disabled = false;
    }
}

async function changeCohort(event) {
    event.preventDefault();
    if (!selectedCohortMember) {
        return;
    }
    const changedMember = selectedCohortMember;
    const newCohortId = Number(readValue('teamMemberCohort'));
    const reason = readValue('teamMemberCohortReason');
    if (!reason) {
        cohortError.textContent = '변경 사유를 입력해 주세요.';
        cohortError.classList.remove('hidden');
        cohortReason.setAttribute('aria-invalid', 'true');
        cohortReason.focus();
        return;
    }
    cohortSubmit.disabled = true;
    clearCohortError();
    try {
        await patch(`/api/members/${changedMember.memberId}/cohort`, {newCohortId, reason});
        showToast(`${changedMember.name}님의 기수를 변경했어요.`);
        closeModal(cohortModal);
        clearSelectedCohortMember();
        await loadMembers();
        focusMemberCohortButton(changedMember.memberId);
    } catch (requestError) {
        cohortError.textContent = messageFrom(requestError);
        cohortError.classList.remove('hidden');
    } finally {
        cohortSubmit.disabled = false;
    }
}

async function initialize() {
    [teams, cohorts] = await Promise.all([
        get('/api/members/reference/teams'),
        get('/api/members/reference/cohorts'),
    ]);
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
changeForm.addEventListener('submit', changeTeam);
cohortForm.addEventListener('submit', changeCohort);
lookup('[data-team-member-change-cancel]').addEventListener('click', () => {
    closeModal(changeModal);
    clearSelectedTeamMember();
});
lookup('[data-team-member-cohort-cancel]').addEventListener('click', () => {
    closeModal(cohortModal);
    clearSelectedCohortMember();
});
changeModal.addEventListener('click', (event) => {
    if (event.target === changeModal || event.target.closest('[data-action="close-modal"]')) {
        clearSelectedTeamMember();
    }
});
cohortModal.addEventListener('click', (event) => {
    if (event.target === cohortModal || event.target.closest('[data-action="close-modal"]')) {
        clearSelectedCohortMember();
    }
});
document.addEventListener('keydown', (event) => {
    if (event.key === 'Escape' && !changeModal.classList.contains('hidden')) {
        clearSelectedTeamMember();
    }
    if (event.key === 'Escape' && !cohortModal.classList.contains('hidden')) {
        clearSelectedCohortMember();
    }
}, true);

initialize().catch((error) => setState('팀 멤버 관리 화면을 준비하지 못했어요.', messageFrom(error), true));
