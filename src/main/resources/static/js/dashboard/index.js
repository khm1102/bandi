import {get} from '../common/api.js';
import {bindPageActions, element, lookup} from '../common/dom.js';

const ROLE_LABELS = Object.freeze({
    ADMIN: '운영진',
    LEADER: '팀장',
    MEMBER: '일반 부원',
});
const INACTIVE_PROJECT_STATUSES = new Set(['ENDED', 'CANCELLED', 'ARCHIVED']);
const ATTENTION_ASSET_STATUSES = new Set(['REPAIR', 'LOST']);
const ACTIVE_ASSET_STATUSES = new Set(['IN_USE', 'LOANED']);
const ACTIONS = Object.freeze({
    SCHEDULE_RETRY: 'dashboard-schedule-retry',
    NOTICE_RETRY: 'dashboard-notice-retry',
    PROGRESS_RETRY: 'dashboard-progress-retry',
});
const dashboardState = {
    schedules: undefined,
    notices: undefined,
    fee: undefined,
    assets: undefined,
    progress: undefined,
};

function nextActionCandidates() {
    const actions = [];
    const unreadImportant = dashboardState.notices?.find((notice) =>
        notice.important && !notice.read);
    if (unreadImportant) {
        actions.push({priority: 1, href: '/resources', label: '중요 공지 확인',
            title: unreadImportant.title,
            message: '아직 확인하지 않은 중요 공지가 있어요. 내용을 먼저 확인해 주세요.'});
    }
    const nextSchedule = dashboardState.schedules?.[0];
    if (nextSchedule) {
        actions.push({priority: 2, href: '/calendar', label: '오늘 일정 확인',
            title: `${formatTime(nextSchedule.startDttm, nextSchedule.allDay)} ${nextSchedule.title}`,
            message: `${nextSchedule.place || '장소 미정'}에서 진행해요. 일정 세부 내용을 확인해 주세요.`});
    }
    if (dashboardState.fee?.unpaidAmount > 0) {
        actions.push({priority: 3, href: '/dues', label: '미납 회비 확인',
            title: `${money(dashboardState.fee.unpaidAmount)}원 납부가 필요해요`,
            message: '납부할 회비 항목과 기한을 확인해 주세요.'});
    }
    const attentionProgress = dashboardState.progress?.reduce((summary, item) => ({
        overdue: summary.overdue + item.overdueCount,
        blocked: summary.blocked + item.blockedCount,
    }), {overdue: 0, blocked: 0});
    if (attentionProgress && (attentionProgress.overdue > 0 || attentionProgress.blocked > 0)) {
        actions.push({priority: 4, href: '/production', label: '제작 업무 확인',
            title: `지연 ${attentionProgress.overdue}건 · 막힘 ${attentionProgress.blocked}건`,
            message: '진행이 늦거나 막힌 제작 업무부터 확인해 주세요.'});
    }
    const attentionAssets = dashboardState.assets?.filter((asset) =>
        ATTENTION_ASSET_STATUSES.has(asset.status)).length || 0;
    if (attentionAssets > 0) {
        actions.push({priority: 5, href: '/props', label: '주의 자산 확인',
            title: `확인이 필요한 소품·장비가 ${attentionAssets}건 있어요`,
            message: '수리 또는 분실 상태의 품목을 확인해 주세요.'});
    }
    return actions.sort((left, right) => left.priority - right.priority);
}

function renderNextActions() {
    const actions = nextActionCandidates();
    const loading = Object.values(dashboardState).some((value) => value === undefined);
    const primary = actions[0] || (loading
        ? {href: '#', label: '불러오는 중', title: '오늘 할 일을 확인하고 있어요',
            message: '일정과 공지, 회비 상태를 불러오는 중이에요.'}
        : {href: '/calendar', label: '전체 일정 보기', title: '지금 바로 처리할 급한 일이 없어요',
            message: '오늘 일정을 한 번 확인하고 필요한 업무를 이어가세요.'});
    setText('[data-dashboard-next-title]', primary.title);
    setText('[data-dashboard-next-message]', primary.message);
    const link = lookup('[data-dashboard-next-link]');
    link.href = primary.href;
    link.textContent = primary.label;
    link.setAttribute('aria-disabled', String(loading && actions.length === 0));
    link.classList.toggle('pointer-events-none', loading && actions.length === 0);
    link.classList.toggle('opacity-60', loading && actions.length === 0);
    setText('[data-dashboard-next-status]', loading && actions.length === 0
        ? '오늘의 다음 행동을 불러오는 중입니다.'
        : `다음 행동을 갱신했습니다. ${primary.title}`);

    const secondary = lookup('[data-dashboard-secondary-actions]');
    secondary.replaceChildren();
    actions.slice(1, 3).forEach((action) => {
        const actionLink = element('a', 'inline-flex min-h-11 items-center rounded-md px-2 text-sm font-bold text-accent-foreground transition-colors hover:bg-card', action.label);
        actionLink.href = action.href;
        secondary.appendChild(actionLink);
    });
}

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
    lookup('.dashboard-state-retry', state)?.classList.toggle('hidden', !error);
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
    dashboardState.schedules = sorted;
    renderNextActions();
    const entireCount = sorted.filter((event) => !event.teamId).length;
    const teamCount = sorted.filter((event) => event.teamId).length;
    setText('[data-stat-value="dashboard-schedule-count"]', sorted.length);
    setText('[data-stat-delta="dashboard-schedule-summary"]', sorted.length === 0
        ? '오늘 등록된 일정이 없습니다'
        : `전체 ${entireCount} · 팀 ${teamCount}`);
    if (sorted.length === 0) {
        showState('[data-dashboard-schedule-state]', '오늘 일정이 없습니다',
                '캘린더에서 다음 일정을 확인해 보세요.');
        return;
    }
    sorted.forEach(appendSchedule);
    hideState('[data-dashboard-schedule-state]');
}

function renderScheduleError() {
    dashboardState.schedules = null;
    renderNextActions();
    setText('[data-stat-value="dashboard-schedule-count"]', '—');
    setText('[data-stat-delta="dashboard-schedule-summary"]', '일정을 불러오지 못했습니다');
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

function renderNotices(notices) {
    dashboardState.notices = notices;
    renderNextActions();
    const unreadCount = notices.filter((notice) => !notice.read).length;
    const importantCount = notices.filter((notice) => notice.important).length;
    const visible = notices.filter((notice) => notice.important || !notice.read)
            .sort(noticePriority).slice(0, 4);
    setText('[data-stat-value="dashboard-unread-count"]', unreadCount);
    setText('[data-stat-delta="dashboard-notice-summary"]', `중요 ${importantCount}건`);
    if (visible.length === 0) {
        showState('[data-dashboard-notice-state]', '확인할 공지가 없습니다',
                '중요 공지와 미확인 공지를 모두 확인했습니다.');
        return;
    }
    visible.forEach(appendNotice);
    hideState('[data-dashboard-notice-state]');
}

function renderNoticeError() {
    dashboardState.notices = null;
    renderNextActions();
    setText('[data-stat-value="dashboard-unread-count"]', '—');
    setText('[data-stat-delta="dashboard-notice-summary"]', '공지를 불러오지 못했습니다');
    showState('[data-dashboard-notice-state]', '공지를 불러오지 못했습니다',
            '공지·자료실에서 다시 확인해 주세요.', true);
}

function renderFee(summary) {
    dashboardState.fee = summary;
    renderNextActions();
    setText('[data-stat-value="dashboard-unpaid-amount"]', money(summary.unpaidAmount));
    setText('[data-stat-delta="dashboard-fee-summary"]', summary.unpaidAmount > 0
        ? `총 ${money(summary.totalAmount)}원 중 미납`
        : '미납 회비가 없습니다');
}

function renderFeeError() {
    dashboardState.fee = null;
    renderNextActions();
    setText('[data-stat-value="dashboard-unpaid-amount"]', '—');
    setText('[data-stat-delta="dashboard-fee-summary"]', '납부 현황을 불러오지 못했습니다');
}

function renderAssets(assets) {
    dashboardState.assets = assets;
    renderNextActions();
    const attentionCount = assets.filter((asset) =>
        ATTENTION_ASSET_STATUSES.has(asset.status)).length;
    const activeCount = assets.filter((asset) =>
        ACTIVE_ASSET_STATUSES.has(asset.status)).length;
    setText('[data-stat-value="dashboard-asset-count"]', attentionCount);
    setText('[data-stat-delta="dashboard-asset-summary"]',
            `사용·대여 중 ${activeCount}건`);
}

function renderAssetError() {
    dashboardState.assets = null;
    renderNextActions();
    setText('[data-stat-value="dashboard-asset-count"]', '—');
    setText('[data-stat-delta="dashboard-asset-summary"]', '자산 현황을 불러오지 못했습니다');
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
    dashboardState.progress = progressItems;
    renderNextActions();
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
            dashboardState.progress = [];
            renderNextActions();
            showState('[data-dashboard-progress-state]', '진행 중인 공연이 없습니다',
                    '공연 프로젝트가 시작되면 팀별 진행률이 표시됩니다.');
            return;
        }
        const progressItems = await get(
                `/api/production-tasks/projects/${project.performanceProjectId}/team-progress`);
        renderProgress(project, progressItems);
    } catch (error) {
        dashboardState.progress = null;
        renderNextActions();
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

bindPageActions({
    [ACTIONS.SCHEDULE_RETRY]: () => loadSchedules(todayRange()),
    [ACTIONS.NOTICE_RETRY]: loadNotices,
    [ACTIONS.PROGRESS_RETRY]: loadProgress,
});

loadDashboard();
