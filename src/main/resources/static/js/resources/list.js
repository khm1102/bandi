import {get} from '../common/api.js';
import {debounce, lookup} from '../common/dom.js';
import {normalizePage, readPageFromUrl, renderPagination, setUrlPage, writeUrl} from '../common/pagination.js';

const PAGE_SIZE = 20;
const list = lookup('[data-resource-list]');
const empty = lookup('[data-resource-empty]');
const pagination = lookup('[data-pagination]');
const searchInput = lookup('[data-resource-search]');
let requestGeneration = 0;

function queryState() {
    const params = new URLSearchParams(window.location.search);
    return {params, keyword: params.get('q') || '', page: readPageFromUrl(params)};
}

function formatDate(value) {
    if (!value) {
        return '';
    }
    return new Intl.DateTimeFormat('ko-KR', {
        year: 'numeric', month: 'long', day: 'numeric',
    }).format(new Date(value));
}

function summary(markdown) {
    return (markdown || '')
        .replace(/!?(?:\[[^\]]*\]\([^)]*\))/g, '')
        .replace(/[#>*_`|~-]/g, ' ')
        .replace(/\s+/g, ' ')
        .trim() || '본문 미리보기가 없습니다.';
}

function displayState(title, message) {
    lookup('[data-resource-state-title]', empty).textContent = title;
    lookup('[data-resource-state-message]', empty).textContent = message;
    empty.classList.remove('hidden');
}

function appendCard(item) {
    const card = lookup('[data-resource-card-template]').content.firstElementChild.cloneNode(true);
    lookup('[data-resource-title]', card).textContent = item.title;
    lookup('[data-resource-summary]', card).textContent = summary(item.bodyMarkdown);
    lookup('[data-resource-meta]', card).textContent = `${item.createdByName} 작성 · ${formatDate(item.updatedDttm)} 수정 · 첨부 ${item.attachmentCount}개`;

    const detail = lookup('[data-resource-detail]', card);
    detail.href = `/resources/${item.resourceId}`;

    if (item.coverStoredFileId) {
        const cover = lookup('[data-resource-cover]', card);
        cover.src = item.coverImageSource === 'LINK_PREVIEW'
            ? `/api/resources/${item.resourceId}/link-previews/${item.coverStoredFileId}/inline`
            : `/api/resources/${item.resourceId}/files/${item.coverStoredFileId}/inline`;
        cover.alt = `${item.title} 대표 이미지`;
        cover.classList.remove('hidden');
        cover.addEventListener('error', () => {
            cover.classList.add('hidden');
        }, {once: true});
    }
    list.append(card);
}

function clearCards() {
    list.replaceChildren();
}

function updateSearchUrl(keyword) {
    const {params} = queryState();
    if (keyword) {
        params.set('q', keyword);
    } else {
        params.delete('q');
    }
    setUrlPage(params, 0);
    writeUrl(params, false);
}

function focusResults() {
    list.scrollIntoView({block: 'start'});
    list.querySelector('a')?.focus({preventScroll: true});
}

async function load({focus = false} = {}) {
    const state = queryState();
    searchInput.value = state.keyword;
    const generation = ++requestGeneration;
    clearCards();
    empty.classList.add('hidden');

    try {
        const response = await get('/api/resources', {
            q: state.keyword,
            page: state.page,
            pageSize: PAGE_SIZE,
        });
        if (generation !== requestGeneration) {
            return;
        }
        const normalized = normalizePage(response, state.page);
        if (normalized !== state.page) {
            setUrlPage(state.params, normalized);
            writeUrl(state.params, false);
            await load({focus});
            return;
        }
        response.items.forEach(appendCard);
        renderPagination(pagination, response, changePage);
        if (response.items.length === 0) {
            pagination.classList.add('hidden');
            displayState(state.keyword ? '검색 결과가 없습니다' : '아직 등록된 자료가 없습니다',
                state.keyword ? '검색어를 바꾸거나 지워서 다시 확인해 보세요.' : '첫 자료를 작성해 동아리 멤버와 공유해 보세요.');
        }
        if (focus && response.items.length > 0) {
            focusResults();
        }
    } catch (error) {
        if (generation === requestGeneration) {
            pagination.classList.add('hidden');
            displayState('자료를 불러오지 못했습니다', error.message || '잠시 후 다시 시도해 주세요.');
        }
    }
}

function changePage(page) {
    const {params} = queryState();
    setUrlPage(params, page);
    writeUrl(params, true);
    load({focus: true});
}

searchInput.addEventListener('input', debounce(() => {
    updateSearchUrl(searchInput.value.trim());
    load();
}, 300));
window.addEventListener('popstate', () => load());
load();
