import {get} from '../common/api.js';
import {debounce, element, lookup} from '../common/dom.js';
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
const moreButton = lookup('[data-manage-more]');

let page = 0;
let loading = false;
let lastPage = false;
let requestGeneration = 0;

function formatDateTime(value) {
    if (!value) {
        return '';
    }
    return new Date(value).toLocaleString('ko-KR', {
        year: 'numeric',
        month: 'numeric',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
    });
}

function setState(title, message, options = {}) {
    lookup('[data-manage-state-title]', state).textContent = title;
    lookup('[data-manage-state-message]', state).textContent = message;
    lookup('[data-manage-reset]', state).classList.toggle('hidden', !options.reset);
    lookup('[data-manage-retry]', state).classList.toggle('hidden', !options.retry);
    state.classList.remove('hidden');
}

function targetLabel(notice) {
    return notice.targetScope === 'TEAM' ? notice.teamName : '전체 공지';
}

function appendNotice(notice) {
    const row = lookup('[data-manage-row-template]').content.firstElementChild.cloneNode(true);
    row.href = `/notices/manage/${notice.internalNoticeId}`;
    const badges = lookup('[data-manage-badges]', row);
    const presentation = STATUS_PRESENTATION[notice.status] || STATUS_PRESENTATION.DRAFT;
    badges.appendChild(badge(presentation.label, presentation.tone));
    badges.appendChild(badge(targetLabel(notice), 'info'));
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

function hasFilter() {
    return Boolean(searchInput.value.trim() || statusSelect.value
        || scopeSelect?.value || teamSelect?.value);
}

function query() {
    return {
        keyword: searchInput.value.trim(),
        status: statusSelect.value,
        targetScope: scopeSelect?.value,
        teamId: scopeSelect?.value === 'TEAM' ? teamSelect?.value : null,
        page,
        pageSize: PAGE_SIZE,
    };
}

async function load(reset = false) {
    if (loading && !reset) {
        return;
    }
    if (lastPage && !reset) {
        return;
    }
    if (reset) {
        page = 0;
        lastPage = false;
        list.replaceChildren();
    }
    const generation = ++requestGeneration;
    loading = true;
    try {
        const currentPage = page;
        const notices = await get('/api/internal-notice-management', query());
        if (generation !== requestGeneration) {
            return;
        }
        notices.forEach(appendNotice);
        lastPage = notices.length < PAGE_SIZE;
        page += 1;
        moreButton.classList.toggle('hidden', lastPage);
        state.classList.toggle('hidden', notices.length > 0 || currentPage > 0);
        if (notices.length === 0 && currentPage === 0) {
            setState(hasFilter() ? '조건에 맞는 공지가 없습니다' : '관리할 공지가 없습니다',
                hasFilter() ? '검색어나 필터를 초기화해 보세요.'
                    : '새 공지를 작성하면 여기에 표시됩니다.',
                {reset: hasFilter()});
        }
    } catch (error) {
        if (generation === requestGeneration) {
            setState('공지 관리 목록을 불러오지 못했습니다',
                error.message || '잠시 후 다시 시도해 주세요.', {retry: true});
        }
    } finally {
        if (generation === requestGeneration) {
            loading = false;
        }
    }
}

function resetFilters() {
    searchInput.value = '';
    statusSelect.value = '';
    if (scopeSelect) {
        scopeSelect.value = '';
    }
    if (teamSelect) {
        teamSelect.value = '';
        teamSelect.classList.add('hidden');
    }
    load(true);
}

async function initializeTeams() {
    if (root.dataset.role !== 'admin' || !teamSelect) {
        return;
    }
    try {
        const teams = await get('/api/members/reference/teams', {activeOnly: true});
        teams.forEach((team) => {
            teamSelect.appendChild(element('option', '', team.name));
            teamSelect.lastElementChild.value = String(team.teamId);
        });
    } catch (error) {
        const unavailableOption = element('option', '', '팀 목록을 불러오지 못했어요');
        unavailableOption.value = '';
        teamSelect.replaceChildren(unavailableOption);
        teamSelect.disabled = true;
    }
}

searchInput.addEventListener('input', debounce(() => load(true)));
statusSelect.addEventListener('change', () => load(true));
scopeSelect?.addEventListener('change', () => {
    const teamScope = scopeSelect.value === 'TEAM';
    teamSelect.classList.toggle('hidden', !teamScope);
    if (!teamScope) {
        teamSelect.value = '';
    }
    load(true);
});
teamSelect?.addEventListener('change', () => load(true));
lookup('[data-manage-reset]').addEventListener('click', resetFilters);
lookup('[data-manage-retry]').addEventListener('click', () => load(true));
moreButton.addEventListener('click', () => load());

await initializeTeams();
load(true);
