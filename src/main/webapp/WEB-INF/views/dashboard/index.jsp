<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout title="홈" active="dashboard" role="${role}" scriptPath="dashboard/index">
    <div class="mb-5 flex min-w-0 flex-wrap items-end gap-3.5">
        <div class="min-w-0">
            <h1 class="text-2xl font-black tracking-tight" data-dashboard-greeting>운영 현황을 불러오는 중입니다</h1>
            <p class="mt-1 text-sm text-muted-foreground" data-dashboard-date>오늘의 동아리 운영 정보를 정리하고 있습니다.</p>
        </div>
        <div class="grid w-full grid-cols-1 gap-2 md:ml-auto md:flex md:w-auto md:flex-wrap md:items-center">
            <t:button href="/resources" variant="outline">미확인 공지 보기</t:button>
            <sec:authorize access="hasAnyRole('ADMIN', 'LEADER')">
                <t:button href="/calendar">일정 관리</t:button>
            </sec:authorize>
        </div>
    </div>

    <div class="mb-4 grid grid-cols-1 gap-2.5 md:grid-cols-2 md:gap-4 lg:grid-cols-4">
        <t:statCard label="오늘 일정" value="—" unit="건" delta="일정을 확인하고 있습니다" icon="calendar" featured="true"
                    valueHook="dashboard-schedule-count" deltaHook="dashboard-schedule-summary"/>
        <t:statCard label="미확인 공지" value="—" unit="건" delta="공지를 확인하고 있습니다" icon="ticket"
                    valueHook="dashboard-unread-count" deltaHook="dashboard-notice-summary"/>
        <t:statCard label="내 미납 회비" value="—" unit="원" delta="납부 현황을 확인하고 있습니다" tone="danger" icon="wallet" iconTone="danger"
                    valueHook="dashboard-unpaid-amount" deltaHook="dashboard-fee-summary"/>
        <t:statCard label="주의 자산" value="—" unit="건" delta="수리·분실 상태를 확인하고 있습니다" icon="box" iconTone="warning"
                    valueHook="dashboard-asset-count" deltaHook="dashboard-asset-summary"/>
    </div>

    <div class="mb-4 hidden flex-col items-start gap-3 rounded-lg border border-primary/30 bg-accent/50 px-4 py-3.5 md:flex-row"
         data-dashboard-highlight>
        <span class="flex size-10 shrink-0 items-center justify-center rounded-md bg-primary text-primary-foreground">
            <svg class="size-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M10 21h4"/></svg>
        </span>
        <div class="min-w-0 flex-1">
            <div class="flex items-center gap-2">
                <t:badge tone="accent">중요</t:badge>
                <b class="truncate text-sm" data-dashboard-highlight-title></b>
            </div>
            <p class="mt-0.5 text-xs text-muted-foreground" data-dashboard-highlight-meta></p>
        </div>
        <a href="<c:url value='/resources'/>" class="inline-flex min-h-11 w-full shrink-0 items-center justify-center rounded-md bg-primary px-3 text-xs font-bold text-primary-foreground transition-colors hover:bg-primary-strong hover:text-white md:w-auto">공지 보기</a>
    </div>

    <div class="grid items-start gap-4 lg:grid-cols-[1.7fr_1fr]">
        <div class="flex flex-col gap-4">
            <t:card title="오늘의 일정" icon="clock" moreUrl="/calendar" moreLabel="캘린더 →" flush="true">
                <div data-dashboard-schedules></div>
                <div class="px-5 py-8 text-center" data-dashboard-schedule-state>
                    <b class="block text-sm">일정을 불러오는 중입니다</b>
                    <p class="mt-1 text-xs text-muted-foreground">잠시만 기다려 주세요.</p>
                </div>
            </t:card>

            <t:card title="팀별 제작 진행" icon="activity" moreLabel="현재 공연 기준">
                <p class="mb-4 hidden text-xs font-bold text-muted-foreground" data-dashboard-project-title></p>
                <div class="flex flex-col gap-3.5" data-dashboard-progress></div>
                <div class="py-4 text-center" data-dashboard-progress-state>
                    <b class="block text-sm">제작 진행 현황을 불러오는 중입니다</b>
                    <p class="mt-1 text-xs text-muted-foreground">잠시만 기다려 주세요.</p>
                </div>
            </t:card>
        </div>

        <div class="flex flex-col gap-4">
            <t:card title="중요·미확인 공지" icon="bell" moreUrl="/resources" moreLabel="전체 →" flush="true">
                <div data-dashboard-notices></div>
                <div class="px-5 py-8 text-center" data-dashboard-notice-state>
                    <b class="block text-sm">공지를 불러오는 중입니다</b>
                    <p class="mt-1 text-xs text-muted-foreground">잠시만 기다려 주세요.</p>
                </div>
            </t:card>

            <t:card title="빠른 이동">
                <div class="flex flex-col gap-1">
                    <a href="<c:url value='/calendar'/>" class="flex min-h-11 items-center gap-3 rounded-md px-2 transition-colors hover:bg-secondary">
                        <span class="flex size-8 shrink-0 items-center justify-center rounded-md bg-accent text-sm font-black text-accent-foreground" data-quick-schedule-count>—</span>
                        <span class="min-w-0 flex-1"><b class="block text-sm">오늘 일정 확인</b><span class="text-xs text-muted-foreground">전체·팀 일정을 한곳에서 확인</span></span>
                        <span class="text-xs font-bold text-accent-foreground">보기</span>
                    </a>
                    <a href="<c:url value='/dues'/>" class="flex min-h-11 items-center gap-3 rounded-md px-2 transition-colors hover:bg-secondary">
                        <span class="flex size-8 shrink-0 items-center justify-center rounded-md bg-destructive-soft text-sm font-black text-destructive" data-quick-fee-count>—</span>
                        <span class="min-w-0 flex-1"><b class="block text-sm">내 회비 확인</b><span class="text-xs text-muted-foreground">미납 금액과 납부 내역 확인</span></span>
                        <span class="text-xs font-bold text-accent-foreground">보기</span>
                    </a>
                    <a href="<c:url value='/props'/>" class="flex min-h-11 items-center gap-3 rounded-md px-2 transition-colors hover:bg-secondary">
                        <span class="flex size-8 shrink-0 items-center justify-center rounded-md bg-warning-soft text-sm font-black text-warning" data-quick-asset-count>—</span>
                        <span class="min-w-0 flex-1"><b class="block text-sm">주의 자산 확인</b><span class="text-xs text-muted-foreground">수리·분실 상태의 소품과 장비</span></span>
                        <span class="text-xs font-bold text-accent-foreground">보기</span>
                    </a>
                    <sec:authorize access="hasRole('ADMIN')">
                        <a href="<c:url value='/members'/>" class="flex min-h-11 items-center gap-3 rounded-md px-2 transition-colors hover:bg-secondary">
                            <span class="flex size-8 shrink-0 items-center justify-center rounded-md bg-info-soft text-sm font-black text-info">권한</span>
                            <span class="min-w-0 flex-1"><b class="block text-sm">멤버·권한 관리</b><span class="text-xs text-muted-foreground">등록 상태와 역할 변경</span></span>
                            <span class="text-xs font-bold text-accent-foreground">보기</span>
                        </a>
                    </sec:authorize>
                </div>
            </t:card>
        </div>
    </div>

    <template data-dashboard-schedule-template>
        <div class="flex items-center gap-3 border-b px-5 py-3 last:border-b-0">
            <span class="min-w-11 text-sm font-extrabold text-accent-foreground" data-schedule-time></span>
            <div class="min-w-0 flex-1">
                <p class="truncate text-sm font-bold" data-schedule-title></p>
                <p class="mt-0.5 truncate text-xs text-muted-foreground" data-schedule-meta></p>
            </div>
            <span class="shrink-0 rounded-full bg-secondary px-2 py-1 text-xs font-bold text-muted-foreground" data-schedule-scope></span>
        </div>
    </template>

    <template data-dashboard-notice-template>
        <a href="<c:url value='/resources'/>" class="flex min-h-16 items-center gap-3 border-b px-5 py-3 transition-colors last:border-b-0 hover:bg-secondary/70">
            <span class="flex size-8 shrink-0 items-center justify-center rounded-md bg-secondary text-xs font-black text-muted-foreground" data-notice-mark></span>
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
