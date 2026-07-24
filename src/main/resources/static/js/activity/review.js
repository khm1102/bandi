import {get, getBlob} from '../common/api.js';
import {debounce, element, lookup} from '../common/dom.js';
import {normalizePage, readPageFromUrl, renderPagination, setUrlPage, writeUrl} from '../common/pagination.js';
import {currentUserRole} from '../common/session.js';
import {badge} from '../common/view.js';
import {showToast} from '../common/toast.js';

const STATUS_META = Object.freeze({
    SUBMITTED: ['검수 대기', 'info'],
    TEAM_APPROVED: ['팀장 승인', 'info'],
    REVISION_REQUESTED: ['보완 요청', 'warning'],
    APPROVED: ['최종 승인', 'success'],
    ARCHIVED: ['보관', 'neutral'],
});

function formatDateTime(value) {
    return value ? new Intl.DateTimeFormat('ko-KR', {
        year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit',
        minute: '2-digit', hour12: false,
    }).format(new Date(value)) : '—';
}

function statusBadge(status) {
    const [label, tone] = STATUS_META[status] || [status || '미정', 'neutral'];
    return badge(label, tone);
}

function typeLabel(recordType) {
    return recordType === 'HWPX' ? '한글 내역서' : '간단 기록';
}

function state(title, message, retry = false) {
    const root = lookup('[data-review-state]');
    root.classList.remove('hidden');
    lookup('[data-review-table-wrap]').classList.add('hidden');
    lookup('[data-review-card-list]').classList.add('hidden');
    lookup('[data-review-state-title]', root).textContent = title;
    lookup('[data-review-state-message]', root).textContent = message;
    lookup('[data-review-retry]', root).classList.toggle('hidden', !retry);
}

function configureTeamFilter(teams) {
    const root = lookup('[data-review-team-filter]');
    if (currentUserRole !== 'admin') {
        root.classList.add('hidden');
        return;
    }
    root.classList.remove('hidden');
    const select = document.getElementById('reviewTeam');
    select.replaceChildren(element('option', '', '전체 팀'));
    select.firstElementChild.value = '';
    teams.forEach((team) => {
        const option = element('option', '', team.name);
        option.value = String(team.teamId);
        select.append(option);
    });
}

function queryFromUrl() {
    const params = new URLSearchParams(window.location.search);
    return {
        params,
        page: readPageFromUrl(params),
        keyword: params.get('q') || '',
        status: params.get('status') || '',
        recordType: params.get('type') || '',
        teamId: params.get('team') || '',
    };
}

function syncControls(query) {
    document.getElementById('reviewKeyword').value = query.keyword;
    document.getElementById('reviewStatus').value = query.status;
    document.getElementById('reviewType').value = query.recordType;
    const team = document.getElementById('reviewTeam');
    if (team) {
        team.value = query.teamId;
    }
}

function writeConditions(push = false) {
    const {params} = queryFromUrl();
    const fields = [
        ['q', document.getElementById('reviewKeyword').value.trim()],
        ['status', document.getElementById('reviewStatus').value],
        ['type', document.getElementById('reviewType').value],
        ['team', document.getElementById('reviewTeam')?.value || ''],
    ];
    fields.forEach(([key, value]) => {
        if (value) {
            params.set(key, value);
        } else {
            params.delete(key);
        }
    });
    setUrlPage(params, 0);
    writeUrl(params, push);
}

function detailUrl(record) {
    return `/activity/review/${record.activityRecordId}`;
}

function appendBadges(root, record) {
    root.replaceChildren(badge(typeLabel(record.recordType), 'neutral'),
            statusBadge(record.status));
}

function renderRows(items) {
    const body = lookup('[data-review-table-body]');
    const cards = lookup('[data-review-card-list]');
    body.replaceChildren();
    cards.replaceChildren();
    items.forEach((record) => {
        const row = lookup('[data-review-row-template]').content.firstElementChild.cloneNode(true);
        appendBadges(lookup('[data-review-row-badges]', row), record);
        lookup('[data-review-row-title]', row).textContent = record.title;
        lookup('[data-review-row-format]', row).textContent = typeLabel(record.recordType);
        lookup('[data-review-row-team]', row).textContent = record.teamName || '팀 미정';
        lookup('[data-review-row-author]', row).textContent = record.createdByName || '작성자 미상';
        lookup('[data-review-row-date]', row).textContent = formatDateTime(record.activityDttm);
        lookup('[data-review-row-status]', row).append(statusBadge(record.status));
        lookup('[data-review-row-link]', row).href = detailUrl(record);
        body.append(row);

        const card = lookup('[data-review-card-template]').content.firstElementChild.cloneNode(true);
        appendBadges(lookup('[data-review-card-badges]', card), record);
        lookup('[data-review-card-title]', card).textContent = record.title;
        lookup('[data-review-card-team]', card).textContent = record.teamName || '팀 미정';
        lookup('[data-review-card-author]', card).textContent = record.createdByName || '작성자 미상';
        lookup('[data-review-card-date]', card).textContent = formatDateTime(record.activityDttm);
        lookup('[data-review-card-link]', card).href = detailUrl(record);
        cards.append(card);
    });
}

async function load() {
    state('검수 기록을 불러오는 중입니다', '잠시만 기다려 주세요.');
    const query = queryFromUrl();
    syncControls(query);
    try {
        const response = await get('/api/activity-reviews', {
            keyword: query.keyword, status: query.status, recordType: query.recordType,
            teamId: query.teamId, page: query.page, pageSize: 20,
        });
        const normalized = normalizePage(response, query.page);
        if (normalized !== query.page) {
            setUrlPage(query.params, normalized);
            writeUrl(query.params, false);
            await load();
            return;
        }
        lookup('[data-review-result-summary]').textContent = `총 ${response.totalElements.toLocaleString('ko-KR')}건`;
        if (response.items.length === 0) {
            state('검수할 활동 기록이 없습니다', query.keyword || query.status || query.recordType
                ? '조건을 바꾸거나 초기화해 보세요.' : '제출된 기록이 생기면 이곳에서 검수할 수 있어요.');
            return;
        }
        renderRows(response.items);
        lookup('[data-review-state]').classList.add('hidden');
        lookup('[data-review-table-wrap]').classList.remove('hidden');
        lookup('[data-review-card-list]').classList.remove('hidden');
        renderPagination(lookup('[data-pagination]'), response, (page) => {
            setUrlPage(query.params, page);
            writeUrl(query.params, true);
            load();
        });
    } catch (error) {
        state('검수 기록을 불러오지 못했습니다', error.message, true);
    }
}

function downloadCsv() {
    const query = queryFromUrl();
    getBlob('/api/activity-reviews/export', {
        keyword: query.keyword, status: query.status, recordType: query.recordType,
        teamId: query.teamId,
    }).then((result) => {
        const link = document.createElement('a');
        const url = URL.createObjectURL(result.blob);
        link.href = url;
        link.download = result.filename || '활동-기록-검수.csv';
        document.body.append(link);
        link.click();
        link.remove();
        URL.revokeObjectURL(url);
    }).catch((error) => showToast(error.message));
}

document.getElementById('reviewKeyword').addEventListener('input', debounce(() => {
    writeConditions();
    load();
}));
['reviewStatus', 'reviewType', 'reviewTeam'].forEach((id) => {
    const input = document.getElementById(id);
    if (input) {
        input.addEventListener('change', () => {
            writeConditions();
            load();
        });
    }
});
document.addEventListener('click', (event) => {
    if (event.target.closest('[data-page-action="activity-review-retry"]')) {
        load();
    }
    if (event.target.closest('[data-page-action="activity-review-export"]')) {
        downloadCsv();
    }
});
window.addEventListener('popstate', load);

get('/api/members/reference/teams').then(configureTeamFilter).then(load)
    .catch((error) => state('검수 화면을 준비하지 못했습니다', error.message, true));
