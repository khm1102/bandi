import {get} from '../common/api.js';
import {debounce, element, lookup} from '../common/dom.js';
import {renderPagination, readPageFromUrl, setUrlPage, writeUrl, normalizePage} from '../common/pagination.js';
import {badge} from '../common/view.js';

const PAGE_SIZE = 20;
const STATUS_PRESENTATION = {
    DRAFT: {label: '초안', tone: 'neutral'},
    SCHEDULED: {label: '예약', tone: 'info'},
    PUBLISHED: {label: '게시 중', tone: 'success'},
    CLOSED: {label: '게시 종료', tone: 'warning'},
    ARCHIVED: {label: '보관', tone: 'neutral'},
};

const root = lookup('[data-notice-manage-root]');
const list = lookup('[data-manage-list]');
const state = lookup('[data-manage-state]');
const searchInput = lookup('[data-manage-search]');
const statusSelect = lookup('[data-manage-status]');
const scopeSelect = lookup('[data-manage-scope]');
const teamSelect = lookup('[data-manage-team]');
const pagination = lookup('[data-pagination]');
let requestGeneration = 0;

function readUrlState() {
    const params = new URLSearchParams(window.location.search);
    return {
        params,
        query: params.get('q') || '',
        status: params.get('status') || '',
        scope: params.get('scope') || '',
        team: params.get('team') || '',
        page: readPageFromUrl(params),
    };
}

function syncControls(urlState) {
    searchInput.value = urlState.query;
    statusSelect.value = urlState.status;
    if (scopeSelect) {
        scopeSelect.value = urlState.scope;
    }
    if (teamSelect) {
        teamSelect.value = urlState.team;
        teamSelect.classList.toggle('hidden', urlState.scope !== 'TEAM');
    }
}

function formatDateTime(value) {
    return value ? new Date(value).toLocaleString('ko-KR', {
        year: 'numeric', month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit',
    }) : '';
}

function setState(title, message, options = {}) {
    lookup('[data-manage-state-title]', state).textContent = title;
    lookup('[data-manage-state-message]', state).textContent = message;
    lookup('[data-manage-reset]', state).classList.toggle('hidden', !options.reset);
    lookup('[data-manage-retry]', state).classList.toggle('hidden', !options.retry);
    state.classList.remove('hidden');
}

function appendNotice(notice) {
    const row = lookup('[data-manage-row-template]').content.firstElementChild.cloneNode(true);
    row.href = `/notices/manage/${notice.internalNoticeId}`;
    const badges = lookup('[data-manage-badges]', row);
    const presentation = STATUS_PRESENTATION[notice.status] || STATUS_PRESENTATION.DRAFT;
    badges.appendChild(badge(presentation.label, presentation.tone));
    badges.appendChild(badge(notice.targetScope === 'TEAM' ? notice.teamName : '전체 공지', 'info'));
    if (notice.important) {
        badges.appendChild(badge('중요', 'warning'));
    }
    lookup('[data-manage-title]', row).textContent = notice.title;
    lookup('[data-manage-created]', row).textContent = `작성 ${notice.createdByName}`;
    lookup('[data-manage-updated]', row).textContent =
        `최근 수정 ${notice.updatedByName} · ${formatDateTime(notice.updatedDttm)}`;
    lookup('[data-manage-action-label]', row).textContent =
        notice.status === 'DRAFT' ? '이어서 작성' : '상세 관리';
    list.appendChild(row);
}

function hasFilter(urlState) {
    return Boolean(urlState.query || urlState.status || urlState.scope || urlState.team);
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
    load();
}

function changePage(page) {
    const urlState = readUrlState();
    setUrlPage(urlState.params, page);
    writeUrl(urlState.params, true);
    load({focus: true});
}

function focusList() {
    const first = list.querySelector('[data-manage-link]');
    if (first) {
        first.focus({preventScroll: true});
    }
    list.scrollIntoView({behavior: 'smooth', block: 'start'});
}

async function load(options = {}) {
    const urlState = readUrlState();
    syncControls(urlState);
    const generation = ++requestGeneration;
    try {
        const response = await get('/api/internal-notice-management', {
            keyword: urlState.query,
            status: urlState.status,
            targetScope: urlState.scope,
            teamId: urlState.scope === 'TEAM' ? urlState.team : null,
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
            await load(options);
            return;
        }
        list.replaceChildren();
        response.items.forEach(appendNotice);
        state.classList.toggle('hidden', response.items.length > 0);
        if (response.totalElements > 0) {
            renderPagination(pagination, response, changePage);
        } else {
            pagination.classList.add('hidden');
        }
        if (response.items.length === 0) {
            const filtered = hasFilter(urlState);
            setState(filtered ? '조건에 맞는 공지가 없습니다' : '관리할 공지가 없습니다',
                filtered ? '검색어나 필터를 초기화해 보세요.' : '새 공지를 작성하면 여기에 표시됩니다.',
                {reset: filtered});
        } else if (options.focus) {
            focusList();
        }
    } catch (error) {
        if (generation === requestGeneration) {
            setState('공지 관리 목록을 불러오지 못했습니다',
                error.message || '잠시 후 다시 시도해 주세요.', {retry: true});
        }
    }
}

async function initializeTeams() {
    if (root.dataset.role !== 'admin' || !teamSelect) {
        return;
    }
    try {
        const teams = await get('/api/members/reference/teams', {activeOnly: true});
        teams.forEach((team) => {
            const option = element('option', '', team.name);
            option.value = String(team.teamId);
            teamSelect.appendChild(option);
        });
    } catch {
        const unavailable = element('option', '', '팀 목록을 불러오지 못했어요');
        unavailable.value = '';
        teamSelect.replaceChildren(unavailable);
        teamSelect.disabled = true;
    }
}

searchInput.addEventListener('input', debounce(() => replaceFilters({q: searchInput.value.trim()}), 300));
statusSelect.addEventListener('change', () => replaceFilters({status: statusSelect.value}));
scopeSelect?.addEventListener('change', () => {
    if (scopeSelect.value !== 'TEAM' && teamSelect) {
        teamSelect.value = '';
    }
    replaceFilters({scope: scopeSelect.value, team: ''});
});
teamSelect?.addEventListener('change', () => replaceFilters({team: teamSelect.value}));
lookup('[data-manage-reset]').addEventListener('click', () => {
    writeUrl(new URLSearchParams(), false);
    load();
});
lookup('[data-manage-retry]').addEventListener('click', () => load());
window.addEventListener('popstate', () => load({focus: true}));

await initializeTeams();
load();
