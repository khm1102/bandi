import {showToast} from '../common/toast.js';
import {all, bindPageActions, debounce, lookup, readValue} from '../common/dom.js';
import {currentUserRole, memberProfiles} from '../common/session.js';
import {activateFilterChip, badge, closeActionModal, today} from '../common/view.js';

const ACTIONS = Object.freeze({
    OPEN: 'resource-open',
    DOWNLOAD: 'resource-download',
    UPLOAD: 'resource-upload',
    NOTICE: 'resource-notice'
});

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

function buildResourceRow(name, category, notice) {
    const template = lookup('[data-resource-row-template]');
    const row = template.content.firstElementChild.cloneNode(true);
    row.dataset.category = notice ? `공지 ${category}` : category;
    lookup('[data-resource-name]', row).textContent = name;
    const categoryCell = lookup('[data-resource-category]', row);
    categoryCell.appendChild(badge(notice ? '공지' : category, notice ? 'accent' : 'neutral'));
    lookup('[data-resource-uploader]', row).textContent = memberProfiles[currentUserRole][0];
    lookup('[data-resource-date]', row).textContent = today();
    lookup('[data-resource-quality]', row).textContent = notice ? '—' : '원본';
    return row;
}

function addResource(trigger, noticeOnly) {
    const name = readValue(noticeOnly ? 'ntName' : 'upName');
    if (!name) {
        showToast(noticeOnly ? '제목을 입력해 주세요' : '파일명을 입력해 주세요');
        return;
    }
    const category = readValue(noticeOnly ? 'ntCat' : 'upCat');
    const noticeCheckbox = document.getElementById('upNotice');
    const notice = noticeOnly || Boolean(noticeCheckbox && noticeCheckbox.checked);
    lookup('[data-resource-list]').prepend(buildResourceRow(name, category, notice));
    closeActionModal(trigger);
    filterResources();
    showToast(notice ? '공지를 등록했어요. 상단에 고정됩니다' : '파일을 업로드했어요');
}

lookup('[data-resource-search]').addEventListener('input', debounce(filterResources));

document.addEventListener('click', (event) => {
    const filter = event.target.closest('[data-filter-group="resource"]');
    if (!filter) {
        return;
    }
    activateFilterChip(filter);
    filterResources();
});

bindPageActions({
    [ACTIONS.OPEN]: () => showToast('공지 파일을 열었어요'),
    [ACTIONS.DOWNLOAD]: () => showToast('다운로드를 시작했어요'),
    [ACTIONS.UPLOAD]: (trigger) => addResource(trigger, false),
    [ACTIONS.NOTICE]: (trigger) => addResource(trigger, true)
});
