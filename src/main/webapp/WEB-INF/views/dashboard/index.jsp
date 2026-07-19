<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout title="홈" active="dashboard" role="${role}" scriptPath="dashboard/index">
    <div class="mx-auto max-w-5xl">
    <header class="flex min-w-0 flex-wrap items-end gap-3.5">
        <div class="min-w-0">
            <h1 class="text-2xl font-extrabold tracking-tight" data-dashboard-greeting>오늘 할 일을 불러오고 있어요</h1>
            <p class="mt-1 text-sm text-muted-foreground" data-dashboard-date>오늘의 동아리 운영 정보를 정리하고 있습니다.</p>
        </div>
    </header>

    <p class="sr-only" data-dashboard-next-status role="status" aria-live="polite"></p>
    <section class="mt-6 border-y border-primary/20 bg-accent/40 px-4 py-5 sm:px-5" aria-labelledby="dashboardNextTitle">
        <p class="text-xs font-bold text-accent-foreground">다음에 할 일</p>
        <div class="mt-1.5 flex flex-col gap-4 md:flex-row md:items-end">
            <div class="min-w-0 flex-1">
                <h2 id="dashboardNextTitle" class="text-xl font-bold text-foreground" data-dashboard-next-title>오늘 할 일을 확인하고 있어요</h2>
                <p class="mt-1 max-w-2xl text-sm text-muted-foreground" data-dashboard-next-message>일정과 공지, 회비 상태를 불러오는 중이에요.</p>
            </div>
            <a data-dashboard-next-link href="<c:url value='/dashboard'/>" class="inline-flex min-h-11 w-full shrink-0 items-center justify-center rounded-md bg-primary px-4 text-sm font-bold text-primary-foreground transition-colors hover:bg-primary-strong md:w-auto">잠시만 기다려 주세요</a>
        </div>
        <div data-dashboard-secondary-actions class="mt-3 flex flex-col gap-1 border-t border-primary/10 pt-3 sm:flex-row sm:flex-wrap sm:gap-2"></div>
    </section>

    <dl class="grid grid-cols-2 border-b md:grid-cols-4" aria-label="오늘의 운영 요약">
        <div class="border-b py-4 pr-3 md:border-b-0 md:border-r"><dt class="text-xs font-bold text-muted-foreground">오늘 일정</dt><dd class="mt-1 text-lg font-bold tabular-nums"><span data-stat-value="dashboard-schedule-count">—</span><span class="ml-0.5 text-xs font-medium text-muted-foreground">건</span></dd><p class="mt-1 text-xs text-muted-foreground" data-stat-delta="dashboard-schedule-summary">확인 중</p></div>
        <div class="border-b border-l py-4 pl-3 pr-3 md:border-b-0 md:border-l-0 md:border-r md:pl-4"><dt class="text-xs font-bold text-muted-foreground">미확인 공지</dt><dd class="mt-1 text-lg font-bold tabular-nums"><span data-stat-value="dashboard-unread-count">—</span><span class="ml-0.5 text-xs font-medium text-muted-foreground">건</span></dd><p class="mt-1 text-xs text-muted-foreground" data-stat-delta="dashboard-notice-summary">확인 중</p></div>
        <div class="py-4 pr-3 md:border-r md:pl-4"><dt class="text-xs font-bold text-muted-foreground">내 미납 회비</dt><dd class="mt-1 text-lg font-bold tabular-nums"><span data-stat-value="dashboard-unpaid-amount">—</span><span class="ml-0.5 text-xs font-medium text-muted-foreground">원</span></dd><p class="mt-1 text-xs text-muted-foreground" data-stat-delta="dashboard-fee-summary">확인 중</p></div>
        <div class="border-l py-4 pl-3 md:border-l-0 md:pl-4"><dt class="text-xs font-bold text-muted-foreground">주의 자산</dt><dd class="mt-1 text-lg font-bold tabular-nums"><span data-stat-value="dashboard-asset-count">—</span><span class="ml-0.5 text-xs font-medium text-muted-foreground">건</span></dd><p class="mt-1 text-xs text-muted-foreground" data-stat-delta="dashboard-asset-summary">확인 중</p></div>
    </dl>

    <div class="mt-7 grid items-start gap-8 lg:grid-cols-[1.45fr_1fr]">
        <div class="flex flex-col gap-8">
            <section aria-labelledby="dashboardScheduleTitle">
                <div class="flex items-center gap-3 border-b pb-3"><h2 id="dashboardScheduleTitle" class="text-lg font-bold">오늘 일정</h2><a href="<c:url value='/calendar'/>" class="ml-auto inline-flex min-h-11 items-center text-sm font-bold text-accent-foreground">전체 일정 보기</a></div>
                <div data-dashboard-schedules></div>
                <div class="py-8 text-center" data-dashboard-schedule-state><b class="block text-sm">일정을 불러오고 있어요</b><p class="mt-1 text-xs text-muted-foreground">잠시만 기다려 주세요.</p><t:button variant="outline" pageAction="dashboard-schedule-retry" cssClass="dashboard-state-retry mt-3 hidden">일정 다시 불러오기</t:button></div>
            </section>

            <section aria-labelledby="dashboardProgressTitle">
                <div class="flex items-center gap-3 border-b pb-3"><h2 id="dashboardProgressTitle" class="text-lg font-bold">팀별 제작 진행</h2><a href="<c:url value='/production'/>" class="ml-auto inline-flex min-h-11 items-center text-sm font-bold text-accent-foreground">제작 업무 보기</a></div>
                <p class="mt-3 hidden text-xs font-bold text-muted-foreground" data-dashboard-project-title></p>
                <div class="mt-4 flex flex-col gap-4" data-dashboard-progress></div>
                <div class="py-8 text-center" data-dashboard-progress-state><b class="block text-sm">제작 진행을 불러오고 있어요</b><p class="mt-1 text-xs text-muted-foreground">잠시만 기다려 주세요.</p><t:button variant="outline" pageAction="dashboard-progress-retry" cssClass="dashboard-state-retry mt-3 hidden">진행 다시 불러오기</t:button></div>
            </section>
        </div>

        <section aria-labelledby="dashboardNoticeTitle">
            <div class="flex items-center gap-3 border-b pb-3"><h2 id="dashboardNoticeTitle" class="text-lg font-bold">중요·미확인 공지</h2><a href="<c:url value='/resources'/>" class="ml-auto inline-flex min-h-11 items-center text-sm font-bold text-accent-foreground">공지 모두 보기</a></div>
            <div data-dashboard-notices></div>
            <div class="py-8 text-center" data-dashboard-notice-state><b class="block text-sm">공지를 불러오고 있어요</b><p class="mt-1 text-xs text-muted-foreground">잠시만 기다려 주세요.</p><t:button variant="outline" pageAction="dashboard-notice-retry" cssClass="dashboard-state-retry mt-3 hidden">공지 다시 불러오기</t:button></div>
        </section>
    </div>
    </div>

    <template data-dashboard-schedule-template>
        <div class="flex items-center gap-3 border-b py-3 last:border-b-0">
            <span class="min-w-11 text-sm font-extrabold text-accent-foreground" data-schedule-time></span>
            <div class="min-w-0 flex-1">
                <p class="truncate text-sm font-bold" data-schedule-title></p>
                <p class="mt-0.5 truncate text-xs text-muted-foreground" data-schedule-meta></p>
            </div>
            <span class="shrink-0 rounded-full bg-secondary px-2 py-1 text-xs font-bold text-muted-foreground" data-schedule-scope></span>
        </div>
    </template>

    <template data-dashboard-notice-template>
        <a href="<c:url value='/resources'/>" class="flex min-h-16 items-center gap-3 border-b py-3 transition-colors last:border-b-0 hover:bg-secondary/70">
            <span class="flex size-8 shrink-0 items-center justify-center rounded-md bg-secondary text-xs font-extrabold text-muted-foreground" data-notice-mark></span>
            <div class="min-w-0 flex-1">
                <p class="truncate text-sm font-bold" data-notice-title></p>
                <p class="mt-0.5 truncate text-xs text-muted-foreground" data-notice-meta></p>
            </div>
        </a>
    </template>

    <template data-dashboard-progress-template>
        <div>
            <div class="mb-1.5 flex items-center gap-2">
                <span class="max-w-32 truncate rounded-full bg-secondary px-2 py-1 text-xs font-bold text-muted-foreground" data-progress-team></span>
                <span class="flex-1 truncate text-xs text-muted-foreground" data-progress-meta></span>
                <b class="text-xs tabular-nums" data-progress-rate></b>
            </div>
            <div class="h-2 overflow-hidden rounded-full bg-secondary" role="progressbar" aria-valuemin="0" aria-valuemax="100">
                <span class="block h-full rounded-full bg-primary transition-[width]" data-progress-bar></span>
            </div>
        </div>
    </template>
</t:layout>
