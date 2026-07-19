<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 disabled:cursor-not-allowed disabled:bg-secondary disabled:text-muted-foreground md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<t:layout title="공연 당일 입장" active="showops" role="${role}" scriptPath="showops/operations">
    <t:pageHead title="공연 당일 입장" description="QR 또는 신청 정보로 관람객을 확인하고 좌석별 입장을 처리합니다"/>

    <section class="mb-4 grid gap-4 rounded-lg border bg-card p-5 md:grid-cols-2" aria-label="입장 관리 회차 선택">
        <div>
            <label class="${label}" for="entryProject">공연 프로젝트</label>
            <select class="${input}" id="entryProject"></select>
        </div>
        <div>
            <label class="${label}" for="entryRound">공연 회차</label>
            <select class="${input}" id="entryRound"></select>
        </div>
        <div class="md:col-span-2 flex flex-wrap items-center gap-2 border-t pt-4">
            <span class="text-xs font-bold text-muted-foreground">현재 회차 상태</span>
            <span data-round-status></span>
            <span data-entry-guidance class="text-xs text-muted-foreground">입장할 회차를 선택해 주세요.</span>
        </div>
    </section>

    <section class="mb-4 grid grid-cols-2 gap-4 lg:grid-cols-4" aria-label="회차별 입장 지표">
        <t:statCard label="신청 건수" value="0" unit="건" valueHook="entry-reservation-count"/>
        <t:statCard label="신청 좌석" value="0" unit="석" valueHook="entry-reserved-seat-count"/>
        <t:statCard label="입장 좌석" value="0" unit="석" tone="success" valueHook="entry-checked-seat-count"/>
        <t:statCard label="미입장 좌석" value="0" unit="석" tone="danger" valueHook="entry-pending-seat-count" delta="입장률 0%" deltaHook="entry-rate"/>
    </section>

    <section class="mb-4 grid gap-4 lg:grid-cols-2" aria-label="관람 신청 조회">
        <form data-token-form class="rounded-lg border bg-card p-5">
            <div class="mb-4">
                <h2 class="text-base font-extrabold">QR 입장 확인</h2>
                <p class="mt-1 text-xs text-muted-foreground">QR 스캐너로 읽은 토큰을 입력해 신청 정보를 확인합니다.</p>
            </div>
            <label class="${label}" for="entryToken">QR 입장 토큰</label>
            <div class="flex flex-col gap-2 sm:flex-row">
                <input class="${input}" id="entryToken" type="password" required maxlength="200" autocomplete="off" spellcheck="false" aria-describedby="entryTokenHelp">
                <t:button type="submit" cssClass="shrink-0">QR 조회</t:button>
            </div>
            <p id="entryTokenHelp" class="mt-2 text-xs text-muted-foreground">토큰은 화면이나 주소에 표시하지 않고 이번 처리 중에만 사용합니다.</p>
        </form>

        <form data-manual-form class="rounded-lg border bg-card p-5">
            <div class="mb-4">
                <h2 class="text-base font-extrabold">신청 정보로 확인</h2>
                <p class="mt-1 text-xs text-muted-foreground">QR을 찾기 어려울 때 신청번호와 신청자 이름을 함께 확인합니다.</p>
            </div>
            <div class="grid gap-3 sm:grid-cols-2">
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
                <t:button type="submit" variant="outline">신청 정보 조회</t:button>
            </div>
        </form>
    </section>

    <section data-entry-result class="rounded-lg border bg-card" aria-live="polite" aria-busy="false">
        <div class="border-b px-5 py-4">
            <div class="flex flex-wrap items-start gap-3">
                <div>
                    <h2 class="text-base font-extrabold">조회 결과</h2>
                    <p class="mt-1 text-xs text-muted-foreground">신청자와 좌석을 확인한 뒤 입장 처리할 좌석을 선택합니다.</p>
                </div>
                <span data-reservation-status class="ml-auto hidden"></span>
            </div>
        </div>
        <div data-entry-empty class="px-5 py-12 text-center text-sm text-muted-foreground">
            QR 또는 신청 정보로 관람 신청을 조회해 주세요.
        </div>
        <div data-entry-detail class="hidden p-5">
            <dl class="mb-5 grid gap-3 rounded-md bg-secondary p-4 sm:grid-cols-3">
                <div><dt class="text-xs font-bold text-muted-foreground">신청번호</dt><dd data-entry-reservation-no class="mt-1 font-mono text-sm font-extrabold"></dd></div>
                <div><dt class="text-xs font-bold text-muted-foreground">신청자</dt><dd data-entry-applicant-name class="mt-1 text-sm font-extrabold"></dd></div>
                <div><dt class="text-xs font-bold text-muted-foreground">연락처</dt><dd data-entry-phone class="mt-1 text-sm font-semibold"></dd></div>
            </dl>
            <fieldset>
                <legend class="text-sm font-extrabold">좌석별 입장 상태</legend>
                <p class="mt-1 text-xs text-muted-foreground">미입장 좌석만 선택할 수 있으며, 이미 입장한 좌석은 사유를 기록한 뒤 취소할 수 있습니다.</p>
                <div data-entry-seats class="mt-4 grid gap-2 sm:grid-cols-2"></div>
            </fieldset>
            <div class="mt-5 flex flex-wrap items-center justify-between gap-3 border-t pt-4">
                <span data-entry-selection-summary class="text-xs font-bold text-muted-foreground">선택된 좌석 0개</span>
                <t:button pageAction="entry-check-in" cssClass="min-w-32" type="button">선택 좌석 입장</t:button>
            </div>
        </div>
    </section>

    <t:modal id="entryCancelModal" title="좌석 입장 취소" description="잘못 처리한 좌석의 입장 기록을 취소합니다.">
        <jsp:attribute name="footer">
            <t:button variant="outline" action="close-modal">닫기</t:button>
            <t:button variant="danger" pageAction="entry-cancel-save">입장 취소</t:button>
        </jsp:attribute>
        <jsp:body>
            <form data-entry-cancel-form class="flex flex-col gap-4">
                <input id="entryCancelSeatId" type="hidden">
                <div>
                    <span class="${label}">취소 대상 좌석</span>
                    <strong data-entry-cancel-seat class="block text-sm"></strong>
                </div>
                <div>
                    <label class="${label}" for="entryCancelReason">취소 사유 *</label>
                    <textarea class="${input} min-h-24 py-3" id="entryCancelReason" required maxlength="500"></textarea>
                </div>
            </form>
        </jsp:body>
    </t:modal>
</t:layout>
