<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 disabled:cursor-not-allowed disabled:bg-secondary disabled:text-muted-foreground md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-bold text-muted-foreground"/>
<t:layout title="공연 당일 입장" active="showops" role="${role}" scriptPath="showops/operations">
    <div class="mx-auto max-w-2xl">
        <h1 class="text-2xl font-extrabold tracking-tight">공연 당일 입장</h1>
        <p class="mt-1 text-sm text-muted-foreground">QR을 읽고 실제 입장한 좌석만 처리해요.</p>

        <%-- 회차 컨텍스트 (선택 후 접기 가능) --%>
        <details data-context-details open class="mt-5 rounded-lg border bg-card">
            <summary class="flex min-h-11 cursor-pointer list-none flex-wrap items-center gap-2 px-4 py-2.5 [&::-webkit-details-marker]:hidden">
                <svg class="size-4 shrink-0 text-muted-foreground transition-transform [details[open]_&]:rotate-90" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="m9 18 6-6-6-6"/></svg>
                <span data-context-summary class="min-w-0 flex-1 truncate text-sm font-bold">공연과 회차를 선택해 주세요</span>
                <span data-round-status class="shrink-0"></span>
                <span data-context-metrics class="shrink-0 text-xs text-muted-foreground tabular-nums" aria-live="polite"></span>
            </summary>
            <div class="grid gap-3 border-t px-4 py-4 md:grid-cols-2">
                <div>
                    <label class="${label}" for="entryProject">공연 프로젝트</label>
                    <select class="${input}" id="entryProject"></select>
                </div>
                <div>
                    <label class="${label}" for="entryRound">공연 회차</label>
                    <select class="${input}" id="entryRound"></select>
                </div>
                <p data-entry-guidance class="text-sm text-muted-foreground md:col-span-2">입장할 회차를 선택해 주세요.</p>
            </div>
        </details>

        <%-- QR 조회 --%>
        <form data-token-form class="mt-5">
            <label class="${label}" for="entryToken">QR 입장 확인</label>
            <div class="flex gap-2">
                <input class="${input}" id="entryToken" type="password" required maxlength="200"
                       autocomplete="off" spellcheck="false" aria-describedby="entryTokenHelp"
                       placeholder="스캐너로 QR을 읽어 주세요">
                <t:button type="submit" variant="dark" cssClass="shrink-0">조회</t:button>
            </div>
            <p id="entryTokenHelp" class="mt-1.5 text-xs text-muted-foreground">토큰은 화면과 주소에 남기지 않고 이번 처리에만 사용해요. 스캐너 입력 후 Enter로 조회돼요.</p>
        </form>

        <%-- 수동 조회 (접힌 보조 기능) --%>
        <details data-manual-details class="mt-3">
            <summary class="flex min-h-11 cursor-pointer list-none items-center gap-1.5 text-sm font-bold text-accent-foreground [&::-webkit-details-marker]:hidden">
                <svg class="size-4 transition-transform [details[open]_&]:rotate-90" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="m9 18 6-6-6-6"/></svg>
                QR을 읽을 수 없나요?
            </summary>
            <form data-manual-form class="mt-2 rounded-lg border bg-card p-4">
                <div class="grid gap-3 md:grid-cols-2">
                    <div>
                        <label class="${label}" for="entryReservationNo">신청번호</label>
                        <input class="${input}" id="entryReservationNo" type="text" required maxlength="30" autocomplete="off">
                    </div>
                    <div>
                        <label class="${label}" for="entryApplicantName">신청자 이름</label>
                        <input class="${input}" id="entryApplicantName" type="text" required maxlength="100" autocomplete="off">
                    </div>
                </div>
                <div class="mt-3 flex justify-end">
                    <t:button type="submit" variant="outline">신청 정보로 조회</t:button>
                </div>
            </form>
        </details>

        <%-- 결과 영역 --%>
        <section class="mt-6" aria-label="조회 결과">
            <div data-entry-feedback class="hidden rounded-lg border p-4" role="status"></div>

            <div data-entry-empty class="rounded-lg border border-dashed bg-card px-6 py-10 text-center text-sm text-muted-foreground">
                QR 또는 신청 정보로 관람 신청을 조회해 주세요.
            </div>

            <div data-entry-detail class="hidden rounded-lg border bg-card">
                <div class="flex flex-wrap items-center gap-2 border-b px-5 py-4">
                    <h2 data-entry-result-title tabindex="-1" class="text-base font-bold">조회 결과</h2>
                    <span data-reservation-status class="ml-auto"></span>
                </div>
                <div class="p-5">
                    <dl class="grid gap-3 rounded-md bg-secondary p-4 md:grid-cols-3">
                        <div><dt class="text-xs font-bold text-muted-foreground">신청번호</dt><dd data-entry-reservation-no class="mt-0.5 font-mono text-sm font-bold"></dd></div>
                        <div><dt class="text-xs font-bold text-muted-foreground">신청자</dt><dd data-entry-applicant-name class="mt-0.5 text-sm font-bold"></dd></div>
                        <div><dt class="text-xs font-bold text-muted-foreground">연락처</dt><dd data-entry-phone class="mt-0.5 text-sm"></dd></div>
                    </dl>
                    <fieldset class="mt-5">
                        <legend class="text-sm font-bold">입장할 좌석 선택</legend>
                        <p class="mt-1 text-xs text-muted-foreground">미입장 좌석만 선택할 수 있어요. 이미 입장한 좌석은 취소로만 되돌릴 수 있어요.</p>
                        <div data-entry-seats class="mt-3 grid gap-2 md:grid-cols-2"></div>
                        <div class="mt-3">
                            <t:button variant="outline" size="compact" pageAction="entry-select-all">미입장 좌석 모두 선택</t:button>
                        </div>
                    </fieldset>
                    <div class="mt-5 border-t pt-4">
                        <p data-entry-selection-summary class="text-sm font-bold text-muted-foreground" aria-live="polite">선택한 좌석이 없어요</p>
                        <div class="mt-3 flex flex-col gap-2 md:flex-row md:justify-end">
                            <t:button pageAction="entry-check-in" type="button" cssClass="w-full md:w-auto">선택 좌석 입장</t:button>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    </div>

    <%-- 입장 취소 sheet --%>
    <t:sheet id="entryCancelSheet" title="좌석 입장 취소" description="잘못 처리한 좌석의 입장 기록을 되돌려요.">
        <jsp:attribute name="footer">
            <t:button variant="outline" action="close-sheet">닫기</t:button>
            <t:button variant="danger" pageAction="entry-cancel-save">입장 취소</t:button>
        </jsp:attribute>
        <jsp:body>
            <div class="flex items-start gap-2 rounded-md bg-destructive-soft p-3">
                <svg class="mt-0.5 size-5 shrink-0 text-destructive" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0zM12 9v4M12 17h.01"/></svg>
                <div class="min-w-0 text-sm">
                    <b data-entry-cancel-seat class="block font-bold text-destructive"></b>
                    <p data-entry-cancel-time class="mt-0.5 text-xs text-muted-foreground"></p>
                    <p class="mt-1 text-xs text-muted-foreground">취소하면 이 좌석은 다시 미입장 상태가 되고, 사유는 운영 기록에 남아요.</p>
                </div>
            </div>
            <form data-entry-cancel-form class="mt-4 flex flex-col gap-3">
                <input id="entryCancelSeatId" type="hidden">
                <div>
                    <label class="${label}" for="entryCancelReason">취소 사유 *</label>
                    <textarea class="min-h-24 w-full resize-y rounded-md border border-input bg-card px-3 py-2.5 text-sm focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20" id="entryCancelReason" required maxlength="500"></textarea>
                </div>
                <p data-entry-cancel-error class="hidden rounded-md bg-destructive-soft px-3 py-2 text-sm text-destructive" role="alert"></p>
            </form>
        </jsp:body>
    </t:sheet>
</t:layout>
