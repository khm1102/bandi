<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-bold text-muted-foreground"/>
<t:layout title="통합 캘린더" active="calendar" role="${role}" scriptPath="schedule/calendar">
    <main class="w-full">
    <t:pageHead title="통합 캘린더" description="다가오는 전체·팀 일정을 한곳에서 확인해요">
        <c:if test="${role != 'member'}">
            <t:button pageAction="calendar-create">새 일정 등록</t:button>
        </c:if>
    </t:pageHead>

    <section class="mb-8" aria-labelledby="calendarNextTitle">
        <p class="text-sm font-bold text-accent-foreground">다음 일정</p>
        <div class="mt-2 grid gap-4 border-l-4 border-primary bg-accent px-5 py-5 md:grid-cols-[minmax(0,1fr)_auto] md:items-center"><div><h2 id="calendarNextTitle" class="text-lg font-bold" data-calendar-next-title>일정을 확인하고 있어요</h2><p class="mt-1 text-sm leading-6 text-muted-foreground" data-calendar-next-message>잠시만 기다려 주세요.</p></div><span class="hidden" data-calendar-next-action><t:button variant="outline" pageAction="calendar-next-open" cssClass="w-full md:w-auto">일정 상세 보기</t:button></span></div>
    </section>

    <div class="mb-5 flex gap-2 overflow-x-auto pb-1" data-calendar-filters>
        <t:filterChip group="calendar" value="ALL" label="전체" active="true"/>
    </div>

    <section class="border-y py-5" aria-labelledby="calendarMonthTitle">
        <div class="mb-5 flex flex-wrap items-center gap-3">
            <button type="button" data-page-action="calendar-prev" class="inline-flex size-11 items-center justify-center rounded-md border bg-card text-lg font-bold transition-colors hover:bg-secondary" aria-label="이전 달">‹</button>
            <h2 id="calendarMonthTitle" class="text-lg font-bold" data-calendar-month></h2>
            <button type="button" data-page-action="calendar-next" class="inline-flex size-11 items-center justify-center rounded-md border bg-card text-lg font-bold transition-colors hover:bg-secondary" aria-label="다음 달">›</button>
            <span class="w-full text-sm text-muted-foreground md:ml-auto md:w-auto" data-calendar-filter-label>전체 팀 일정 표시 중</span>
        </div>
        <div class="hidden grid-cols-7 gap-1.5 md:grid" data-calendar-grid></div>
        <div class="divide-y border-y md:hidden" data-calendar-list></div>
        <div class="mt-4 hidden border-y px-4 py-8 text-center" data-calendar-state role="status" aria-live="polite">
            <b class="block text-sm" data-calendar-state-title></b>
            <p class="mt-1 text-sm text-muted-foreground" data-calendar-state-message></p>
            <button type="button" class="mx-auto mt-3 hidden min-h-11 rounded-md border bg-card px-4 text-sm font-bold" data-calendar-retry>다시 시도</button>
        </div>
    </section>
    </main>

    <t:sheet id="calendarEventSheet" title="일정 등록" description="전체 또는 팀 일정을 등록하고 기간과 장소를 관리해요.">
        <jsp:attribute name="footer">
            <t:button variant="danger" pageAction="calendar-delete" cssClass="mr-auto hidden" confirm="이 일정을 삭제할까요?" confirmAction="일정 삭제">삭제</t:button>
            <t:button variant="outline" action="close-sheet">취소</t:button>
            <t:button pageAction="calendar-save">저장</t:button>
        </jsp:attribute>
        <jsp:body>
            <div class="flex flex-col gap-3">
                <div><label class="${label}" for="ceTitle">일정명 <span class="text-accent-foreground">*</span></label><input class="${input}" id="ceTitle" type="text" maxlength="200" placeholder="예) 전체 연습"></div>
                <div><label class="${label}" for="ceTeam">담당 범위</label><select class="${input}" id="ceTeam"></select></div>
                <div class="grid grid-cols-1 gap-3 md:grid-cols-2">
                    <div><label class="${label}" for="ceStart">시작 <span class="text-accent-foreground">*</span></label><input class="${input}" id="ceStart" type="datetime-local"></div>
                    <div><label class="${label}" for="ceEnd">종료 <span class="text-accent-foreground">*</span></label><input class="${input}" id="ceEnd" type="datetime-local"></div>
                </div>
                <label class="flex min-h-11 items-center gap-2 text-sm font-bold"><input id="ceAllDay" type="checkbox" class="size-4 rounded border-input"> 종일 일정</label>
                <div><label class="${label}" for="ceLoc">장소</label><input class="${input}" id="ceLoc" type="text" maxlength="200" placeholder="예) TIP아트센터"></div>
                <div><label class="${label}" for="ceDescription">설명</label><textarea class="min-h-24 w-full resize-y rounded-md border border-input bg-card px-3 py-2.5 text-sm" id="ceDescription" placeholder="준비물과 진행 내용을 입력하세요"></textarea></div>
                <p class="hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-xs text-destructive" data-calendar-form-error role="alert"></p>
            </div>
        </jsp:body>
    </t:sheet>
</t:layout>
