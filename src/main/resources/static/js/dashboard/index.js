import {get} from '../common/api.js';
import {lookup} from '../common/dom.js';

const ROLE_LABELS = Object.freeze({
    ADMIN: '운영진',
    LEADER: '팀장',
    MEMBER: '일반 부원',
});
const INACTIVE_PROJECT_STATUSES = new Set(['ENDED', 'CANCELLED', 'ARCHIVED']);
const ATTENTION_ASSET_STATUSES = new Set(['REPAIR', 'LOST']);
const ACTIVE_ASSET_STATUSES = new Set(['IN_USE', 'LOANED']);

function setText(selector, value) {
    const node = lookup(selector);
    if (node) {
        node.textContent = value;
    }
}

function pad(value) {
    return String(value).padStart(2, '0');
}

function localDateTime(date) {
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

function money(value) {
    return Number(value || 0).toLocaleString('ko-KR');
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
    const organization = member.department ? `${role} · ${member.department}` : role;
    setText('[data-dashboard-date]', `${formatToday()} · ${organization}`);
}

function renderMemberError() {
    setText('[data-dashboard-greeting]', '오늘의 운영 현황입니다');
    setText('[data-dashboard-date]', `${formatToday()} · 사용자 정보를 확인하지 못했습니다.`);
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
    const entireCount = sorted.filter((event) => !event.teamId).length;
    const teamCount = sorted.filter((event) => event.teamId).length;
    setText('[data-stat-value="dashboard-schedule-count"]', sorted.length);
    setText('[data-stat-delta="dashboard-schedule-summary"]', sorted.length === 0
        ? '오늘 등록된 일정이 없습니다'
        : `전체 ${entireCount} · 팀 ${teamCount}`);
    setText('[data-quick-schedule-count]', sorted.length);
    if (sorted.length === 0) {
        showState('[data-dashboard-schedule-state]', '오늘 일정이 없습니다',
                '캘린더에서 다음 일정을 확인해 보세요.');
        return;
    }
    sorted.forEach(appendSchedule);
    hideState('[data-dashboard-schedule-state]');
}

function renderScheduleError() {
    setText('[data-stat-value="dashboard-schedule-count"]', '—');
    setText('[data-stat-delta="dashboard-schedule-summary"]', '일정을 불러오지 못했습니다');
    setText('[data-quick-schedule-count]', '—');
    showState('[data-dashboard-schedule-state]', '일정을 불러오지 못했습니다',
            '잠시 후 새로고침해 주세요.', true);
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

function noticeScope(notice) {
    return notice.targetScope === 'TEAM' ? notice.teamName || '팀 공지' : '전체 공지';
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
            `${noticeScope(notice)} · ${formatShortDate(notice.publishStartDttm)}`;
    lookup('[data-dashboard-notices]').appendChild(item);
}

function renderHighlight(notices) {
    const highlight = notices.find((notice) => notice.important && !notice.read)
            || notices.find((notice) => notice.important);
    const container = lookup('[data-dashboard-highlight]');
    container.classList.toggle('hidden', !highlight);
    container.classList.toggle('flex', Boolean(highlight));
    if (!highlight) {
        return;
    }
    setText('[data-dashboard-highlight-title]', highlight.title);
    setText('[data-dashboard-highlight-meta]',
            `${noticeScope(highlight)} · ${formatShortDate(highlight.publishStartDttm)} 게시`);
}

function renderNotices(notices) {
    const unreadCount = notices.filter((notice) => !notice.read).length;
    const importantCount = notices.filter((notice) => notice.important).length;
    const visible = notices.filter((notice) => notice.important || !notice.read)
            .sort(noticePriority).slice(0, 4);
    setText('[data-stat-value="dashboard-unread-count"]', unreadCount);
    setText('[data-stat-delta="dashboard-notice-summary"]', `중요 ${importantCount}건`);
    renderHighlight(notices);
    if (visible.length === 0) {
        showState('[data-dashboard-notice-state]', '확인할 공지가 없습니다',
                '중요 공지와 미확인 공지를 모두 확인했습니다.');
        return;
    }
    visible.forEach(appendNotice);
    hideState('[data-dashboard-notice-state]');
}

function renderNoticeError() {
    setText('[data-stat-value="dashboard-unread-count"]', '—');
    setText('[data-stat-delta="dashboard-notice-summary"]', '공지를 불러오지 못했습니다');
    showState('[data-dashboard-notice-state]', '공지를 불러오지 못했습니다',
            '공지·자료실에서 다시 확인해 주세요.', true);
}

function renderFee(summary) {
    setText('[data-stat-value="dashboard-unpaid-amount"]', money(summary.unpaidAmount));
    setText('[data-stat-delta="dashboard-fee-summary"]', summary.unpaidAmount > 0
        ? `총 ${money(summary.totalAmount)}원 중 미납`
        : '미납 회비가 없습니다');
    setText('[data-quick-fee-count]', summary.unpaidAmount > 0 ? '미납' : '완료');
}

function renderFeeError() {
    setText('[data-stat-value="dashboard-unpaid-amount"]', '—');
    setText('[data-stat-delta="dashboard-fee-summary"]', '납부 현황을 불러오지 못했습니다');
    setText('[data-quick-fee-count]', '—');
}

function renderAssets(assets) {
    const attentionCount = assets.filter((asset) =>
        ATTENTION_ASSET_STATUSES.has(asset.status)).length;
    const activeCount = assets.filter((asset) =>
        ACTIVE_ASSET_STATUSES.has(asset.status)).length;
    setText('[data-stat-value="dashboard-asset-count"]', attentionCount);
    setText('[data-stat-delta="dashboard-asset-summary"]',
            `사용·대여 중 ${activeCount}건`);
    setText('[data-quick-asset-count]', attentionCount);
}

function renderAssetError() {
    setText('[data-stat-value="dashboard-asset-count"]', '—');
    setText('[data-stat-delta="dashboard-asset-summary"]', '자산 현황을 불러오지 못했습니다');
    setText('[data-quick-asset-count]', '—');
}

function progressRate(progress) {
    if (progress.totalCount === 0) {
        return 0;
    }
    return Math.round(progress.completedCount * 100 / progress.totalCount);
}

function appendProgress(progress) {
    const item = lookup('[data-dashboard-progress-template]').content
            .firstElementChild.cloneNode(true);
    const rate = progressRate(progress);
    lookup('[data-progress-team]', item).textContent = progress.teamName || '팀 미지정';
    lookup('[data-progress-meta]', item).textContent =
            `완료 ${progress.completedCount}/${progress.totalCount}`
            + ` · 지연 ${progress.overdueCount} · 막힘 ${progress.blockedCount}`;
    lookup('[data-progress-rate]', item).textContent = `${rate}%`;
    const progressBar = lookup('[role="progressbar"]', item);
    progressBar.setAttribute('aria-valuenow', String(rate));
    progressBar.setAttribute('aria-label', `${progress.teamName || '팀'} 제작 진행률`);
    lookup('[data-progress-bar]', item).style.width = `${Math.min(100, Math.max(0, rate))}%`;
    lookup('[data-dashboard-progress]').appendChild(item);
}

function renderProgress(project, progressItems) {
    const projectTitle = lookup('[data-dashboard-project-title]');
    projectTitle.textContent = `${project.title} · ${project.academicYear}년 ${project.termCode}`;
    projectTitle.classList.remove('hidden');
    if (progressItems.length === 0) {
        showState('[data-dashboard-progress-state]', '등록된 제작 업무가 없습니다',
                '현재 공연의 팀별 제작 업무가 등록되면 진행률이 표시됩니다.');
        return;
    }
    progressItems.forEach(appendProgress);
    hideState('[data-dashboard-progress-state]');
}

async function loadProgress() {
    try {
        const projects = await get('/api/performance-management/projects', {
            offset: 0,
            limit: 20,
        });
        const project = projects.find((item) =>
            !INACTIVE_PROJECT_STATUSES.has(item.status));
        if (!project) {
            showState('[data-dashboard-progress-state]', '진행 중인 공연이 없습니다',
                    '공연 프로젝트가 시작되면 팀별 진행률이 표시됩니다.');
            return;
        }
        const progressItems = await get(
                `/api/production-tasks/projects/${project.performanceProjectId}/team-progress`);
        renderProgress(project, progressItems);
    } catch (error) {
        showState('[data-dashboard-progress-state]', '제작 진행 현황을 불러오지 못했습니다',
                '잠시 후 새로고침해 주세요.', true);
    }
}

async function loadMember() {
    try {
        renderMember(await get('/api/members/me'));
    } catch (error) {
        renderMemberError();
    }
}

async function loadSchedules(range) {
    try {
        const events = await get('/api/calendar-events', {
            rangeStart: range.start,
            rangeEnd: range.end,
        });
        renderSchedules(events);
    } catch (error) {
        renderScheduleError();
    }
}

async function loadNotices() {
    try {
        renderNotices(await get('/api/internal-notices', {
            page: 0,
            pageSize: 100,
        }));
    } catch (error) {
        renderNoticeError();
    }
}

async function loadFee() {
    try {
        renderFee(await get('/api/fees/mine/summary'));
    } catch (error) {
        renderFeeError();
    }
}

async function loadAssets() {
    try {
        renderAssets(await get('/api/assets'));
    } catch (error) {
        renderAssetError();
    }
}

async function loadDashboard() {
    const range = todayRange();
    await Promise.all([
        loadMember(),
        loadSchedules(range),
        loadNotices(),
        loadFee(),
        loadAssets(),
        loadProgress(),
    ]);
}

loadDashboard();
