import {showToast} from '../common/toast.js';
import {all, bindPageActions, debounce, lookup, readValue} from '../common/dom.js';
import {currentUserRole, memberProfiles} from '../common/session.js';
import {activateFilterChip, badge, closeActionModal, today} from '../common/view.js';

const ACTIONS = Object.freeze({
    DOWNLOAD: 'resource-download',
    UPLOAD: 'resource-upload',
    NOTICE_ADD: 'notice-add',
    NOTICE_OPEN: 'notice-open'
});

function activateInfoTab(button) {
    const selectedTab = button.dataset.infoTab;
    all('[data-info-tab]').forEach((tab) => {
        const selected = tab === button;
        tab.setAttribute('aria-selected', String(selected));
        tab.classList.toggle('border', selected);
        tab.classList.toggle('bg-card', selected);
        tab.classList.toggle('text-foreground', selected);
        tab.classList.toggle('text-muted-foreground', !selected);
    });
    all('[data-info-panel]').forEach((panel) => {
        panel.classList.toggle('hidden', panel.dataset.infoPanel !== selectedTab);
    });
}

function filterNotices() {
    const selected = lookup('[data-filter-group="notice"][aria-pressed="true"]');
    const category = selected ? selected.dataset.filterValue : '전체';
    all('[data-notice-card]').forEach((card) => {
        card.hidden = category !== '전체' && !card.dataset.category.split(' ').includes(category);
    });
}

function filterResources() {
    const selected = lookup('[data-filter-group="resource"][aria-pressed="true"]');
    const category = selected ? selected.dataset.filterValue : '전체';
    const query = lookup('[data-resource-search]').value.trim().toLowerCase();
    all('[data-resource-row]').forEach((row) => {
        const categoryMatched = category === '전체' || row.dataset.category.split(' ').includes(category);
        const queryMatched = !query || row.textContent.toLowerCase().includes(query);
        row.hidden = !(categoryMatched && queryMatched);
    });
    const hasVisibleResource = all('[data-resource-row]').some((row) => !row.hidden);
    lookup('[data-resource-empty]').classList.toggle('hidden', hasVisibleResource);
}

function buildResourceRow(name, category) {
    const template = lookup('[data-resource-row-template]');
    const row = template.content.firstElementChild.cloneNode(true);
    row.dataset.category = category;
    lookup('[data-resource-name]', row).textContent = name;
    lookup('[data-resource-category]', row).appendChild(badge(category, 'neutral'));
    lookup('[data-resource-version]', row).textContent = 'v1';
    lookup('[data-resource-uploader]', row).textContent = memberProfiles[currentUserRole][0];
    lookup('[data-resource-date]', row).textContent = today();
    return row;
}

function addResource(trigger) {
    const name = readValue('upName');
    if (!name) {
        showToast('자료 제목을 입력해 주세요');
        return;
    }
    const category = readValue('upCat');
    lookup('[data-resource-list]').prepend(buildResourceRow(name, category));
    closeActionModal(trigger);
    filterResources();
    showToast('자료를 업로드했어요');
}

function buildNoticeCard(title, body, target) {
    const template = lookup('[data-notice-card-template]');
    const card = template.content.firstElementChild.cloneNode(true);
    const teamNotice = target === '내 팀';
    card.dataset.category = teamNotice ? '팀공지' : '전체공지';
    const badgeContainer = lookup('[data-notice-badges]', card);
    badgeContainer.appendChild(badge(teamNotice ? '내 팀' : '전체', teamNotice ? 'info' : 'neutral'));
    badgeContainer.appendChild(badge('게시 중', 'success'));
    lookup('[data-notice-title]', card).textContent = title;
    lookup('[data-notice-body]', card).textContent = body || '내용이 없는 짧은 공지입니다.';
    lookup('[data-notice-meta]', card).textContent = `${memberProfiles[currentUserRole][0]} · ${today()} 게시 · 확인 0명`;
    return card;
}

function addNotice(trigger) {
    const title = readValue('ntTitle');
    if (!title) {
        showToast('공지 제목을 입력해 주세요');
        return;
    }
    lookup('[data-notice-list]').prepend(buildNoticeCard(title, readValue('ntBody'), readValue('ntTarget')));
    closeActionModal(trigger);
    filterNotices();
    showToast('짧은 공지를 게시했어요');
}

lookup('[data-resource-search]').addEventListener('input', debounce(filterResources));

document.addEventListener('click', (event) => {
    const infoTab = event.target.closest('[data-info-tab]');
    if (infoTab) {
        activateInfoTab(infoTab);
        return;
    }
    const filter = event.target.closest('[data-filter-group]');
    if (!filter) {
        return;
    }
    activateFilterChip(filter);
    if (filter.dataset.filterGroup === 'notice') {
        filterNotices();
        return;
    }
    filterResources();
});

bindPageActions({
    [ACTIONS.DOWNLOAD]: () => showToast('다운로드를 시작했어요'),
    [ACTIONS.UPLOAD]: addResource,
    [ACTIONS.NOTICE_ADD]: addNotice,
    [ACTIONS.NOTICE_OPEN]: () => showToast('공지 상세 화면은 다음 구현 단계에서 연결합니다')
});
