import {get} from '../common/api.js';
import {lookup} from '../common/dom.js';

const ROLE_LABELS = Object.freeze({
    ADMIN: '운영진',
    LEADER: '팀장',
    MEMBER: '일반 부원',
});
const ATTENTION_ASSET_STATUSES = new Set(['REPAIR', 'LOST']);

function setText(selector, value) {
    const node = lookup(selector);
    if (node) {
        node.textContent = value;
    }
}

function localDateTime(date) {
    const pad = (value) => String(value).padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
            + `T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
}

function todayRange() {
    const now = new Date();
    const start = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const end = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1);
    return {start: localDateTime(start), end: localDateTime(end)};
}

function formatToday() {
    return new Intl.DateTimeFormat('ko-KR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        weekday: 'long',
    }).format(new Date());
}

function formatTime(value, allDay) {
    if (allDay) {
        return '종일';
    }
    return new Intl.DateTimeFormat('ko-KR', {
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,
    }).format(new Date(value));
}

function formatShortDate(value) {
    if (!value) {
        return '게시일 미정';
    }
    return new Intl.DateTimeFormat('ko-KR', {
        month: 'numeric',
        day: 'numeric',
    }).format(new Date(value));
}

function showState(selector, title, message, error = false) {
    const state = lookup(selector);
    state.hidden = false;
    state.classList.toggle('text-destructive', error);
    lookup('b', state).textContent = title;
    lookup('p', state).textContent = message;
}

function hideState(selector) {
    lookup(selector).hidden = true;
}

function renderMember(member) {
    setText('[data-dashboard-greeting]', `안녕하세요, ${member.name}님`);
    const role = ROLE_LABELS[member.role] || '멤버';
    setText('[data-dashboard-date]',
            `${formatToday()} · ${member.department ? `${role} · ${member.department}` : role}`);
}

function appendSchedule(event) {
    const item = lookup('[data-dashboard-schedule-template]').content
            .firstElementChild.cloneNode(true);
    lookup('[data-schedule-time]', item).textContent = formatTime(event.startDttm,
            event.allDay);
    lookup('[data-schedule-title]', item).textContent = event.title;
    lookup('[data-schedule-meta]', item).textContent = event.place || '장소 미정';
    lookup('[data-schedule-scope]', item).textContent = event.teamId ? '팀' : '전체';
    lookup('[data-dashboard-schedules]').appendChild(item);
}

function renderSchedules(events) {
    const sorted = [...events].sort((left, right) =>
        left.startDttm.localeCompare(right.startDttm));
    setText('[data-stat-value="dashboard-schedule-count"]', sorted.length);
    setText('[data-stat-delta="dashboard-schedule-summary"]',
            sorted.length === 0 ? '오늘 등록된 일정이 없습니다' : `오늘 ${sorted.length}건`);
    setText('[data-quick-schedule-count]', sorted.length);
    if (sorted.length === 0) {
        showState('[data-dashboard-schedule-state]', '오늘 일정이 없습니다',
                '캘린더에서 다음 일정을 확인해 보세요.');
        return;
    }
    sorted.forEach(appendSchedule);
    hideState('[data-dashboard-schedule-state]');
}

function noticePriority(left, right) {
    if (left.important !== right.important) {
        return left.important ? -1 : 1;
    }
    if (left.read !== right.read) {
        return left.read ? 1 : -1;
    }
    return String(right.publishStartDttm || '').localeCompare(
            String(left.publishStartDttm || ''));
}

function appendNotice(notice) {
    const item = lookup('[data-dashboard-notice-template]').content
            .firstElementChild.cloneNode(true);
    const mark = lookup('[data-notice-mark]', item);
    mark.textContent = notice.important ? '중요' : notice.read ? '확인' : '미확인';
    if (notice.important) {
        mark.classList.remove('bg-secondary', 'text-muted-foreground');
        mark.classList.add('bg-accent', 'text-accent-foreground');
    }
    lookup('[data-notice-title]', item).textContent = notice.title;
    lookup('[data-notice-meta]', item).textContent =
            `${notice.targetScope === 'TEAM' ? notice.teamName || '팀 공지' : '전체 공지'} · ${formatShortDate(notice.publishStartDttm)}`;
    lookup('[data-dashboard-notices]').appendChild(item);
}

function renderNotices(notices) {
    const unreadCount = notices.filter((notice) => !notice.read).length;
    const visible = notices.filter((notice) => notice.important || !notice.read)
            .sort(noticePriority).slice(0, 4);
    setText('[data-stat-value="dashboard-unread-count"]', unreadCount);
    setText('[data-stat-delta="dashboard-notice-summary"]',
            `중요 ${notices.filter((notice) => notice.important).length}건`);
    if (visible.length === 0) {
        showState('[data-dashboard-notice-state]', '확인할 공지가 없습니다',
                '중요 공지와 미확인 공지를 모두 확인했습니다.');
        return;
    }
    visible.forEach(appendNotice);
    hideState('[data-dashboard-notice-state]');
}

function renderAssets(assets) {
    const attentionCount = assets.filter((asset) =>
        ATTENTION_ASSET_STATUSES.has(asset.status)).length;
    setText('[data-stat-value="dashboard-asset-count"]', attentionCount);
    setText('[data-stat-delta="dashboard-asset-summary"]',
            attentionCount === 0 ? '점검이 필요한 품목이 없습니다' : '수리·분실 상태 품목');
    setText('[data-quick-asset-count]', attentionCount);
}

async function loadDashboard() {
    const range = todayRange();
    await Promise.all([
        get('/api/members/me').then(renderMember),
        get('/api/calendar-events', {
            rangeStart: range.start,
            rangeEnd: range.end,
        }).then(renderSchedules),
        get('/api/internal-notices', {page: 0, pageSize: 100})
                .then((response) => renderNotices(response.items)),
        get('/api/assets').then((response) => renderAssets(response.items)),
    ]).catch(() => {
        showState('[data-dashboard-schedule-state]', '일부 정보를 불러오지 못했습니다',
                '잠시 후 새로고침해 주세요.', true);
    });
}

loadDashboard();
