import {get} from '../common/api.js';
import {element, lookup} from '../common/dom.js';
import {badge} from '../common/view.js';

function formatDateTime(value) {
    return value ? new Intl.DateTimeFormat('ko-KR', {year: 'numeric', month: 'short',
        day: 'numeric', hour: '2-digit', minute: '2-digit', hour12: false}).format(new Date(value)) : '—';
}

function state(title, message, retry = false) {
    const root = lookup('[data-archive-detail-state]');
    root.classList.remove('hidden');
    lookup('[data-archive-detail-content]').classList.add('hidden');
    lookup('[data-archive-detail-state-title]', root).textContent = title;
    lookup('[data-archive-detail-state-message]', root).textContent = message;
    lookup('[data-archive-detail-retry]', root).classList.toggle('hidden', !retry);
}

function recordId() {
    return Number(lookup('[data-archive-detail]').dataset.activityRecordId);
}

function renderFiles(record) {
    const root = lookup('[data-archive-detail-files]');
    root.replaceChildren();
    (record.files || []).forEach((file) => {
        const card = lookup('[data-archive-detail-file-template]').content.firstElementChild.cloneNode(true);
        const href = `/api/activity-records/${record.activityRecordId}/files/${file.storedFileId}/download`;
        lookup('[data-archive-detail-file-name]', card).textContent = file.originalName;
        lookup('[data-archive-detail-download]', card).href = href;
        if (file.fileRole === 'DOCUMENT') {
            lookup('[data-archive-detail-preview]', card).replaceChildren(
                element('span', 'px-4 text-center text-sm font-bold text-info', 'HWPX 활동 내역서'));
        } else {
            const image = lookup('[data-archive-detail-image]', card);
            image.src = href;
            image.alt = `${record.title} 활동 사진`;
            image.addEventListener('error', () => image.replaceWith(element('span', 'px-4 text-center text-xs text-muted-foreground', '사진을 불러오지 못했습니다')), {once: true});
        }
        root.append(card);
    });
}

async function load() {
    state('활동 기록을 불러오는 중입니다', '잠시만 기다려 주세요.');
    try {
        const record = await get(`/api/activity-records/${recordId()}`);
        const badges = lookup('[data-archive-detail-badges]');
        badges.replaceChildren(badge(record.teamName || '팀 미정', 'neutral'),
            badge('승인 기록', 'success'));
        lookup('[data-archive-detail-title]').textContent = record.title;
        lookup('[data-archive-detail-meta]').textContent = `${record.createdByName || '작성자 미상'} 작성 · ${formatDateTime(record.activityDttm)} · 참여 ${record.participantCount}명`;
        lookup('[data-archive-detail-body]').textContent = record.body;
        renderFiles(record);
        lookup('[data-archive-detail-state]').classList.add('hidden');
        lookup('[data-archive-detail-content]').classList.remove('hidden');
    } catch (error) {
        state('활동 기록을 불러오지 못했습니다', error.message, true);
    }
}

document.addEventListener('click', (event) => {
    if (event.target.closest('[data-page-action="activity-archive-detail-retry"]')) {
        load();
    }
});
load();
