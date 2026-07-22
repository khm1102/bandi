import {get} from '../common/api.js';
import {lookup, element} from '../common/dom.js';
import {activateFilterChip} from '../common/view.js';

let page = 0;
let loading = false;
let lastPage = false;
let query = '';
let readFilter = 'ALL';
let targetScope = null;

function setState(title, message, retry = false) {
    const state = lookup('[data-notice-state]');
    lookup('[data-notice-state-title]', state).textContent = title;
    lookup('[data-notice-state-message]', state).textContent = message;
    lookup('[data-notice-retry]', state).classList.toggle('hidden', !retry);
    state.classList.remove('hidden');
}

function appendNotice(notice) {
    const row = lookup('[data-notice-row-template]').content.firstElementChild.cloneNode(true);
    lookup('[data-notice-link]', row).href = `/notices/${notice.internalNoticeId}`;
    const badges = lookup('[data-notice-badges]', row);
    if (notice.important) badges.appendChild(element('span', 'rounded-full bg-warning-soft px-2 py-1 text-xs font-bold text-warning', '중요'));
    badges.appendChild(element('span', 'rounded-full bg-info-soft px-2 py-1 text-xs font-bold text-info', notice.targetScope === 'TEAM' ? notice.teamName || '팀 공지' : '전체 공지'));
    if (!notice.read) badges.appendChild(element('span', 'rounded-full bg-accent px-2 py-1 text-xs font-bold text-accent-foreground', '미확인'));
    lookup('[data-notice-title]', row).textContent = notice.title;
    lookup('[data-notice-meta]', row).textContent = `${new Date(notice.publishStartDttm).toLocaleString('ko-KR')} 게시`;
    lookup('[data-notice-list]').appendChild(row);
}

async function load(reset = false) {
    if (loading || lastPage && !reset) return;
    if (reset) { page = 0; lastPage = false; lookup('[data-notice-list]').replaceChildren(); }
    loading = true;
    try {
        const currentPage = page;
        const notices = await get('/api/internal-notices', {
            keyword: query,
            readFilter,
            targetScope,
            page: currentPage,
            pageSize: 20
        });
        notices.forEach(appendNotice);
        lastPage = notices.length < 20;
        page += 1;
        lookup('[data-notice-more]').classList.toggle('hidden', lastPage);
        lookup('[data-notice-state]').classList.toggle('hidden', notices.length > 0 || currentPage > 0);
        if (!notices.length && currentPage === 0) {
            setState('표시할 공지가 없습니다', '필터를 바꾸거나 새 공지가 게시될 때 다시 확인해 주세요.');
        }
    } catch (error) { setState('공지를 불러오지 못했습니다', error.message, true); }
    finally { loading = false; }
}

let timeout;
lookup('[data-notice-search]').addEventListener('input', (event) => { clearTimeout(timeout); timeout = setTimeout(() => { query = event.target.value.trim(); load(true); }, 250); });
lookup('[data-notice-retry]').addEventListener('click', () => load(true));
lookup('[data-notice-more]').addEventListener('click', () => load());
document.addEventListener('click', (event) => {
    const chip = event.target.closest('[data-filter-group="notice-read"], [data-filter-group="notice-scope"]');
    if (!chip) return;
    activateFilterChip(chip);
    const value = chip.dataset.filterValue;
    if (chip.dataset.filterGroup === 'notice-read') {
        readFilter = value;
    } else {
        targetScope = value === 'ANY' ? null : value;
    }
    load(true);
});
load(true);
