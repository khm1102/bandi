import {get} from '../common/api.js';
import {lookup, element, debounce} from '../common/dom.js';
import {renderPagination, readPageFromUrl, setUrlPage, writeUrl, normalizePage} from '../common/pagination.js';
import {activateFilterChip} from '../common/view.js';

const PAGE_SIZE = 20;
const list = lookup('[data-notice-list]');
const state = lookup('[data-notice-state]');
const searchInput = lookup('[data-notice-search]');
const pagination = lookup('[data-pagination]');
let requestGeneration = 0;

function readUrlState() {
    const params = new URLSearchParams(window.location.search);
    return {
        params,
        query: params.get('q') || '',
        readFilter: params.get('read') || 'ALL',
        targetScope: params.get('scope') || null,
        page: readPageFromUrl(params),
    };
}

function activateChip(group, value) {
    const chip = document.querySelector(`[data-filter-group="${group}"][data-filter-value="${value}"]`);
    if (chip) {
        activateFilterChip(chip);
    }
}

function syncControls(stateValue) {
    searchInput.value = stateValue.query;
    activateChip('notice-read', stateValue.readFilter);
    activateChip('notice-scope', stateValue.targetScope || 'ANY');
}

function setState(title, message, options = {}) {
    lookup('[data-notice-state-title]', state).textContent = title;
    lookup('[data-notice-state-message]', state).textContent = message;
    lookup('[data-notice-retry]', state).classList.toggle('hidden', !options.retry);
    lookup('[data-notice-reset]', state).classList.toggle('hidden', !options.reset);
    state.classList.remove('hidden');
}

function appendNotice(notice) {
    const row = lookup('[data-notice-row-template]').content.firstElementChild.cloneNode(true);
    row.href = `/notices/${notice.internalNoticeId}`;
    const badges = lookup('[data-notice-badges]', row);
    if (notice.important) {
        badges.appendChild(element('span', 'rounded-full bg-warning-soft px-2 py-1 text-xs font-bold text-warning', '중요'));
    }
    const target = notice.targetScope === 'TEAM' ? notice.teamName || '팀 공지' : '전체 공지';
    badges.appendChild(element('span', 'rounded-full bg-info-soft px-2 py-1 text-xs font-bold text-info', target));
    if (!notice.read) {
        badges.appendChild(element('span', 'rounded-full bg-accent px-2 py-1 text-xs font-bold text-accent-foreground', '미확인'));
    }
    lookup('[data-notice-title]', row).textContent = notice.title;
    lookup('[data-notice-meta]', row).textContent =
        `작성 ${notice.createdByName} · ${new Date(notice.publishStartDttm).toLocaleString('ko-KR')} 게시`;
    list.appendChild(row);
}

function hasFilter(urlState) {
    return Boolean(urlState.query || urlState.readFilter !== 'ALL' || urlState.targetScope);
}

function focusList() {
    const first = list.querySelector('[data-notice-link]');
    (first || list).focus({preventScroll: true});
    list.scrollIntoView({behavior: 'smooth', block: 'start'});
}

async function load(options = {}) {
    const urlState = readUrlState();
    syncControls(urlState);
    const generation = ++requestGeneration;
    state.classList.remove('hidden');
    lookup('[data-notice-state-title]', state).textContent = '공지를 불러오는 중입니다';
    lookup('[data-notice-state-message]', state).textContent = '잠시만 기다려 주세요.';
    try {
        const response = await get('/api/internal-notices', {
            keyword: urlState.query,
            readFilter: urlState.readFilter,
            targetScope: urlState.targetScope,
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
            setState(filtered ? '조건에 맞는 공지가 없습니다' : '아직 게시된 공지가 없습니다',
                filtered ? '검색어나 필터를 바꿔 보세요.' : '새 공지가 게시되면 여기에 표시됩니다.',
                {reset: filtered});
        } else if (options.focus) {
            focusList();
        }
    } catch (error) {
        if (generation === requestGeneration) {
            setState('공지를 불러오지 못했습니다', error.message || '잠시 후 다시 시도해 주세요.', {retry: true});
        }
    }
}

function replaceFilters(changes) {
    const urlState = readUrlState();
    Object.entries(changes).forEach(([key, value]) => {
        if (!value || value === 'ALL' || value === 'ANY') {
            urlState.params.delete(key);
        } else {
            urlState.params.set(key, value);
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

searchInput.addEventListener('input', debounce(() => replaceFilters({q: searchInput.value.trim()}), 300));
lookup('[data-notice-retry]').addEventListener('click', () => load());
lookup('[data-notice-reset]').addEventListener('click', () => {
    writeUrl(new URLSearchParams(), false);
    load();
});
document.addEventListener('click', (event) => {
    const chip = event.target.closest('[data-filter-group="notice-read"], [data-filter-group="notice-scope"]');
    if (!chip) {
        return;
    }
    activateFilterChip(chip);
    const key = chip.dataset.filterGroup === 'notice-read' ? 'read' : 'scope';
    replaceFilters({[key]: chip.dataset.filterValue});
});
window.addEventListener('popstate', () => load({focus: true}));
load();
