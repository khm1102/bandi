<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<c:set var="canManage" value="${role != 'member'}"/>
<t:layout title="통합 캘린더" active="calendar" role="${role}" scriptPath="schedule/calendar">
    <jsp:attribute name="css">
        <link rel="stylesheet" href="<c:url value='/css/vendor/fullcalendar/7.0.1/skeleton.css'/>">
        <link rel="stylesheet" href="<c:url value='/css/vendor/fullcalendar/7.0.1/classic-theme.css'/>">
        <link rel="stylesheet" href="<c:url value='/css/vendor/fullcalendar/7.0.1/classic-palette.css'/>">
        <link rel="stylesheet" href="<c:url value='/css/vendor/fullcalendar/7.0.1/bandi-adapter.css'/>">
        <link rel="stylesheet" href="<c:url value='/css/vendor/vanilla-calendar-pro/3.1.0/layout.css'/>">
        <link rel="stylesheet" href="<c:url value='/css/vendor/vanilla-calendar-pro/3.1.0/light.css'/>">
        <link rel="stylesheet" href="<c:url value='/css/vendor/vanilla-calendar-pro/3.1.0/bandi-adapter.css'/>">
    </jsp:attribute>
    <jsp:attribute name="script">
        <script src="<c:url value='/js/vendor/fullcalendar/7.0.1/fullcalendar.global.js'/>"></script>
        <script src="<c:url value='/js/vendor/fullcalendar/7.0.1/fullcalendar.classic.global.js'/>"></script>
        <script src="<c:url value='/js/vendor/fullcalendar/7.0.1/fullcalendar.ko.global.js'/>"></script>
        <script src="<c:url value='/js/vendor/vanilla-calendar-pro/3.1.0/vanilla-calendar-pro.js'/>"></script>
    </jsp:attribute>
    <jsp:body>
        <t:pageHead title="통합 캘린더" description="동아리 전체 일정과 팀 일정을 한곳에서 확인하세요."/>

        <section class="overflow-hidden rounded-lg border bg-card" aria-labelledby="calendarPeriodTitle">
            <div class="border-b px-4 py-4 md:px-5">
                <div class="flex flex-col gap-4 lg:flex-row lg:items-center">
                    <div class="flex min-w-0 items-center gap-2">
                        <button type="button" class="min-h-11 rounded-md border bg-card px-3 text-sm font-bold hover:bg-secondary focus-visible:ring-2 focus-visible:ring-ring" data-calendar-action="today">오늘</button>
                        <div class="flex" aria-label="기간 이동">
                            <button type="button" class="flex size-11 items-center justify-center rounded-l-md border bg-card text-lg hover:bg-secondary focus-visible:ring-2 focus-visible:ring-ring" data-calendar-action="previous" aria-label="이전 기간">‹</button>
                            <button type="button" class="flex size-11 items-center justify-center rounded-r-md border border-l-0 bg-card text-lg hover:bg-secondary focus-visible:ring-2 focus-visible:ring-ring" data-calendar-action="next" aria-label="다음 기간">›</button>
                        </div>
                        <h2 id="calendarPeriodTitle" class="min-w-0 truncate text-lg font-black tracking-tight" data-calendar-title>기간을 불러오는 중</h2>
                    </div>
                    <div class="flex flex-1 flex-col gap-3 md:flex-row md:items-end lg:justify-end">
                        <div class="flex rounded-md border bg-secondary p-1" role="group" aria-label="캘린더 보기">
                            <button type="button" class="min-h-9 rounded-sm bg-card px-3 text-sm font-bold" data-calendar-view="dayGridMonth" aria-pressed="true">월</button>
                            <button type="button" class="min-h-9 rounded-sm px-3 text-sm font-bold text-muted-foreground" data-calendar-view="timeGridWeek" aria-pressed="false">주</button>
                            <button type="button" class="min-h-9 rounded-sm px-3 text-sm font-bold text-muted-foreground" data-calendar-view="listWeek" aria-pressed="false">목록</button>
                        </div>
                        <div class="min-w-52">
                            <label class="sr-only" for="calendarTeamFilter">표시할 팀</label>
                            <select id="calendarTeamFilter" class="${input}" data-calendar-team-filter>
                                <option value="">전체 일정</option>
                            </select>
                        </div>
                        <c:if test="${canManage}">
                            <t:button pageAction="calendar-create">일정 등록</t:button>
                        </c:if>
                    </div>
                </div>
                <p class="mt-3 text-xs leading-5 text-muted-foreground" data-calendar-filter-help>팀을 선택하면 해당 팀 일정과 동아리 전체 일정을 함께 보여드려요.</p>
            </div>

            <div class="hidden border-b bg-secondary px-4 py-3" data-calendar-status role="status">
                <div class="flex flex-wrap items-center gap-3">
                    <div class="min-w-0 flex-1">
                        <b class="block text-sm" data-calendar-status-title></b>
                        <p class="mt-1 text-xs text-muted-foreground" data-calendar-status-message></p>
                    </div>
                    <button type="button" class="hidden min-h-11 rounded-md border bg-card px-4 text-sm font-bold hover:bg-background" data-calendar-retry>다시 시도</button>
                </div>
            </div>

            <div class="p-3 md:p-5" data-calendar-shell>
                <div data-calendar-root aria-label="일정 캘린더"></div>
            </div>
            <p class="sr-only" aria-live="polite" data-calendar-announcement></p>
        </section>

        <t:modal id="calendarDetailModal" title="일정 상세" size="lg" mobileFullscreen="true">
            <jsp:attribute name="footer">
                <t:button variant="danger" pageAction="calendar-delete" cssClass="mr-auto hidden" confirm="이 일정을 삭제할까요?" confirmAction="일정 삭제">일정 삭제</t:button>
                <t:button variant="outline" action="close-modal">닫기</t:button>
                <t:button pageAction="calendar-edit" cssClass="hidden">일정 수정</t:button>
            </jsp:attribute>
            <jsp:body>
                <div class="flex flex-wrap items-center gap-2">
                    <t:badge tone="neutral"><span data-calendar-detail-scope>전체 일정</span></t:badge>
                    <t:badge tone="accent"><span data-calendar-detail-all-day>시간 일정</span></t:badge>
                </div>
                <h3 class="mt-5 text-xl font-black tracking-tight" data-calendar-detail-title></h3>
                <dl class="mt-6 divide-y border-y">
                    <div class="grid gap-1 py-4 md:grid-cols-4 md:gap-4"><dt class="text-sm font-bold text-muted-foreground">일시</dt><dd class="text-sm font-semibold md:col-span-3" data-calendar-detail-period></dd></div>
                    <div class="grid gap-1 py-4 md:grid-cols-4 md:gap-4"><dt class="text-sm font-bold text-muted-foreground">장소</dt><dd class="text-sm md:col-span-3" data-calendar-detail-place></dd></div>
                    <div class="grid gap-1 py-4 md:grid-cols-4 md:gap-4"><dt class="text-sm font-bold text-muted-foreground">설명</dt><dd class="whitespace-pre-wrap text-sm leading-6 md:col-span-3" data-calendar-detail-description></dd></div>
                    <div class="grid gap-1 py-4 md:grid-cols-4 md:gap-4"><dt class="text-sm font-bold text-muted-foreground">마지막 수정</dt><dd class="text-sm md:col-span-3" data-calendar-detail-updated></dd></div>
                </dl>
                <p class="mt-4 hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-sm text-destructive" data-calendar-detail-error role="alert"></p>
            </jsp:body>
        </t:modal>

        <t:modal id="calendarEventModal" title="일정 등록" description="일정 범위와 시간을 확인한 뒤 저장하세요." size="lg" mobileFullscreen="true">
            <jsp:attribute name="footer">
                <t:button variant="outline" action="close-modal">취소</t:button>
                <t:button pageAction="calendar-save">일정 저장</t:button>
            </jsp:attribute>
            <jsp:body>
                <div class="space-y-5">
                    <div>
                        <label class="${label}" for="ceTitle">일정명 <span class="text-accent-foreground">*</span></label>
                        <input class="${input}" id="ceTitle" type="text" maxlength="150" required placeholder="예) 전체 연습">
                    </div>
                    <fieldset class="border-y py-4">
                        <legend class="sr-only">일정 기본 설정</legend>
                        <div class="grid grid-cols-1 gap-4 md:grid-cols-[minmax(0,1fr)_auto] md:items-end">
                            <div>
                                <label class="${label}" for="ceTeam">일정 범위</label>
                                <select class="${input}" id="ceTeam"></select>
                            </div>
                            <label class="flex min-h-11 items-center gap-3 self-end rounded-md border bg-card px-3 text-sm font-bold">
                                <input id="ceAllDay" type="checkbox" class="size-4 rounded border-input">
                                종일 일정
                            </label>
                        </div>
                    </fieldset>
                    <section aria-labelledby="calendarTimeHeading">
                        <div class="mb-3 flex items-center justify-between gap-3">
                            <h3 id="calendarTimeHeading" class="text-sm font-extrabold">일정 시간</h3>
                            <p class="text-xs text-muted-foreground">30분 단위</p>
                        </div>
                        <div class="grid grid-cols-1 gap-4 md:grid-cols-2">
                            <t:dateTimeField id="ceStart" label="시작" required="true" minuteStep="30"/>
                            <t:dateTimeField id="ceEnd" label="종료" required="true" minuteStep="30"/>
                        </div>
                    </section>
                    <p class="hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-xs text-destructive md:-mt-2" data-calendar-period-error role="alert"></p>
                    <details class="rounded-md border bg-secondary/40" data-calendar-extra-details>
                        <summary class="flex min-h-11 cursor-pointer list-none items-center justify-between gap-3 px-3 text-sm font-bold">
                            장소와 설명 추가
                            <span class="text-xs font-medium text-muted-foreground">선택</span>
                        </summary>
                        <div class="space-y-4 border-t px-3 py-4">
                            <div>
                                <label class="${label}" for="ceLoc">장소</label>
                                <input class="${input}" id="ceLoc" type="text" maxlength="200" placeholder="정해지지 않았다면 비워 두세요">
                            </div>
                            <div>
                                <label class="${label}" for="ceDescription">설명</label>
                                <textarea class="min-h-28 w-full resize-y rounded-md border border-input bg-card px-3 py-2.5 text-sm focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20" id="ceDescription" placeholder="준비물이나 진행 내용을 입력하세요"></textarea>
                            </div>
                        </div>
                    </details>
                    <p class="hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-sm text-destructive" data-calendar-form-error role="alert"></p>
                </div>
            </jsp:body>
        </t:modal>
    </jsp:body>
</t:layout>
