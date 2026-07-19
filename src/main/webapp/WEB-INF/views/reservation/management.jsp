<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-bold text-muted-foreground"/>
<t:layout title="관람 신청 관리" active="reservations" role="${role}" scriptPath="reservation/management">
    <div class="mx-auto max-w-4xl">
        <h1 class="text-2xl font-extrabold tracking-tight">관람 신청 관리</h1>
        <p class="mt-1 text-sm text-muted-foreground">회차를 선택해 신청을 확인하고 필요한 조치를 시작해요.</p>

        <%-- 프로젝트·회차 컨텍스트 --%>
        <section class="mt-5 grid gap-3 md:grid-cols-2" aria-label="공연과 회차 선택">
            <div>
                <label class="${label}" for="reservationProject">공연 프로젝트</label>
                <select class="${input}" id="reservationProject"></select>
            </div>
            <div>
                <label class="${label}" for="reservationRound">공연 회차</label>
                <select class="${input}" id="reservationRound"></select>
            </div>
        </section>
        <div class="mt-3 flex flex-wrap items-center gap-2 text-sm" aria-live="polite">
            <span class="text-xs font-bold text-muted-foreground">회차 상태</span>
            <span data-round-status></span>
        </div>

        <%-- compact summary --%>
        <p data-reservation-summary class="mt-4 flex flex-wrap items-center gap-x-3 gap-y-1 border-y py-3 text-sm text-muted-foreground" aria-live="polite">
            <span>신청 <b data-summary-count class="font-bold text-foreground tabular-nums">0</b>건</span>
            <span>유효 좌석 <b data-summary-seats class="font-bold text-foreground tabular-nums">0</b>석</span>
            <span>입장 <b data-summary-checked class="font-bold text-foreground tabular-nums">0</b>석</span>
            <span>입장률 <b data-summary-rate class="font-bold text-foreground tabular-nums">0</b>%</span>
        </p>
        <div data-metrics-error class="mt-2 hidden rounded-md bg-warning-soft px-3 py-2 text-sm text-foreground" role="status">
            <div class="flex flex-wrap items-center gap-2">
                <span data-metrics-error-message class="min-w-0 flex-1"></span>
                <t:button variant="outline" pageAction="reservation-metrics-retry">요약 다시 불러오기</t:button>
            </div>
        </div>

        <%-- 상태 필터 --%>
        <div class="mt-4 flex flex-wrap gap-2" role="group" aria-label="신청 상태 필터">
            <t:filterChip group="reservation-status" value="ALL" label="전체" active="true"/>
            <t:filterChip group="reservation-status" value="CONFIRMED" label="확정"/>
            <t:filterChip group="reservation-status" value="PARTIALLY_CANCELLED" label="일부 취소"/>
            <t:filterChip group="reservation-status" value="CANCELLED" label="취소"/>
        </div>

        <%-- 목록 --%>
        <section class="mt-4" aria-label="관람 신청 목록" data-reservation-region aria-busy="true">
            <div class="hidden grid-cols-[7rem_1fr_1fr_6rem_4rem] items-center gap-3 border-b px-4 pb-2 text-xs font-bold text-muted-foreground md:grid">
                <button type="button" data-sort-key="reservationNo" class="flex min-h-9 items-center gap-1 text-left font-bold hover:text-foreground">신청번호<span data-sort-mark="reservationNo" aria-hidden="true"></span></button>
                <button type="button" data-sort-key="applicantName" class="flex min-h-9 items-center gap-1 text-left font-bold hover:text-foreground">관람객<span data-sort-mark="applicantName" aria-hidden="true"></span></button>
                <span>좌석</span>
                <button type="button" data-sort-key="status" class="flex min-h-9 items-center gap-1 text-left font-bold hover:text-foreground">상태<span data-sort-mark="status" aria-hidden="true"></span></button>
                <span class="sr-only">상세</span>
            </div>
            <p data-sort-note class="hidden pt-1 text-right text-xs text-muted-foreground md:block">정렬은 지금까지 불러온 목록에만 적용돼요.</p>
            <div data-reservation-list class="mt-2 flex flex-col gap-2 md:mt-1 md:gap-0"></div>
            <div data-list-loading class="hidden flex-col gap-2 pt-2" aria-hidden="true">
                <div class="h-20 rounded-lg border bg-card md:h-12"></div>
                <div class="h-20 rounded-lg border bg-card md:h-12"></div>
            </div>
            <div data-list-empty class="hidden rounded-lg border bg-card px-6 py-10 text-center">
                <b data-list-empty-title class="block text-sm font-bold"></b>
                <p data-list-empty-message class="mx-auto mt-1 max-w-prose text-sm text-muted-foreground"></p>
                <div class="mt-4 flex justify-center gap-2">
                    <t:button variant="outline" pageAction="reservation-filter-reset" cssClass="hidden" >필터 초기화</t:button>
                    <a data-empty-management-link href="<c:url value='/performance-management'/>" class="hidden min-h-11 items-center rounded-md border bg-card px-4 text-sm font-bold hover:bg-secondary">공연 운영 설정으로 가기</a>
                </div>
            </div>
            <div data-list-error class="hidden rounded-lg border border-destructive/30 bg-destructive-soft px-5 py-4" role="alert">
                <p class="text-sm text-destructive"><b class="font-bold">신청 목록을 불러오지 못했어요.</b> 이미 불러온 목록은 그대로 남아 있어요.</p>
                <div class="mt-3"><t:button variant="outline" pageAction="reservation-retry">다시 시도</t:button></div>
            </div>
            <div class="mt-4 flex flex-col items-center gap-3">
                <t:button variant="outline" pageAction="reservation-more" cssClass="hidden w-full md:w-auto">25건 더 보기</t:button>
                <p data-list-end class="hidden text-xs text-muted-foreground">마지막 신청까지 모두 확인했어요.</p>
            </div>
        </section>

        <%-- CSV --%>
        <div class="mt-6 flex flex-wrap items-center gap-3 border-t pt-4">
            <t:button variant="outline" pageAction="reservation-export">명단 CSV 내보내기</t:button>
            <p data-export-progress class="text-xs text-muted-foreground" aria-live="polite"></p>
        </div>
    </div>

    <%-- 신청 상세 sheet --%>
    <t:sheet id="reservationDetailSheet" title="신청 상세" description="좌석별 상태와 취소 가능 여부를 확인해요.">
        <jsp:body>
            <dl class="grid gap-3 rounded-md bg-secondary p-4">
                <div><dt class="text-xs font-bold text-muted-foreground">신청번호</dt><dd data-detail-no class="mt-0.5 font-mono text-sm font-bold"></dd></div>
                <div><dt class="text-xs font-bold text-muted-foreground">관람객</dt><dd data-detail-name class="mt-0.5 text-sm font-bold"></dd></div>
                <div><dt class="text-xs font-bold text-muted-foreground">연락처</dt><dd data-detail-phone class="mt-0.5 text-sm"></dd></div>
                <div><dt class="text-xs font-bold text-muted-foreground">신청 상태</dt><dd data-detail-status class="mt-1"></dd></div>
            </dl>
            <h3 class="mt-5 text-sm font-bold">좌석별 상태</h3>
            <ul data-detail-seats class="mt-2 flex flex-col gap-2"></ul>

            <div data-detail-cancel-open-area class="mt-6 border-t pt-4">
                <t:button variant="outline" pageAction="reservation-cancel-open" cssClass="w-full text-destructive hover:bg-destructive-soft">신청 취소 시작</t:button>
                <p data-detail-not-cancelable class="hidden text-sm text-muted-foreground">이 신청은 취소할 수 없어요. 이미 취소됐거나 입장 처리가 시작된 신청이에요.</p>
            </div>

            <div data-detail-cancel-area class="mt-6 hidden rounded-lg border border-destructive/30 bg-destructive-soft/60 p-4">
                <div class="flex items-start gap-2">
                    <svg class="mt-0.5 size-5 shrink-0 text-destructive" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0zM12 9v4M12 17h.01"/></svg>
                    <div class="min-w-0">
                        <b class="block text-sm font-bold text-destructive">신청 취소</b>
                        <p data-cancel-summary class="mt-1 text-sm text-foreground"></p>
                        <p class="mt-1 text-xs text-muted-foreground">취소한 좌석은 다시 신청할 수 있는 상태로 돌아가고, 사유는 운영 기록에 남아요.</p>
                    </div>
                </div>
                <div class="mt-3">
                    <label class="${label}" for="cancelReason">취소 사유 *</label>
                    <textarea class="min-h-24 w-full resize-y rounded-md border border-input bg-card px-3 py-2.5 text-sm focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20" id="cancelReason" required maxlength="500"></textarea>
                </div>
                <p data-cancel-error class="mt-2 hidden rounded-md bg-card px-3 py-2 text-sm text-destructive" role="alert"></p>
                <div class="mt-3 flex justify-end gap-2">
                    <t:button variant="outline" pageAction="reservation-cancel-close">돌아가기</t:button>
                    <t:button variant="danger" pageAction="reservation-cancel-save">신청 취소</t:button>
                </div>
            </div>

            <div data-detail-cancel-success class="mt-6 hidden rounded-lg border bg-success-soft p-4">
                <p class="flex items-start gap-2 text-sm font-bold text-success">
                    <svg class="mt-0.5 size-5 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M20 6L9 17l-5-5"/></svg>
                    신청을 취소했어요. 좌석은 다시 신청 가능한 상태로 돌아갔어요.
                </p>
                <div class="mt-3 flex justify-end"><t:button variant="outline" action="close-sheet">닫기</t:button></div>
            </div>
        </jsp:body>
    </t:sheet>
</t:layout>
