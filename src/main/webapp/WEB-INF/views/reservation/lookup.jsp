<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<t:layoutPublic title="관람 신청 조회" active="reserve" scriptPath="reservation/lookup">
    <header class="border-b pb-6">
        <h1 class="text-3xl font-extrabold tracking-tight">관람 신청 조회·취소</h1>
        <p class="mt-3 max-w-2xl text-sm leading-6 text-muted-foreground">신청 완료 화면에서 받은 조회 토큰으로 공연 회차와 좌석, 입장 상태를 확인해요. 토큰은 주소나 브라우저 저장소에 남기지 않아요.</p>
    </header>

    <section data-lookup-entry class="mt-8 max-w-2xl" aria-labelledby="lookupFormTitle">
        <h2 id="lookupFormTitle" class="text-xl font-bold">조회 토큰을 입력해 주세요</h2>
        <p class="mt-2 text-sm text-muted-foreground">토큰을 잃어버린 경우 최신 공시에 안내된 운영진에게 문의해 주세요.</p>
        <form data-lookup-form class="mt-5 flex flex-col gap-3 sm:flex-row sm:items-end">
            <div class="min-w-0 flex-1">
                <label class="${label}" for="lookupToken">신청 조회 토큰</label>
                <input class="${input}" id="lookupToken" type="password" required maxlength="200" autocomplete="off" spellcheck="false" aria-describedby="lookupFeedback">
            </div>
            <t:button type="submit" cssClass="shrink-0">내 신청 확인</t:button>
        </form>
        <p id="lookupFeedback" data-lookup-feedback class="mt-3 hidden rounded-md border px-3 py-2.5 text-sm" role="status" aria-live="polite"></p>
    </section>

    <section data-lookup-result class="mt-8 hidden border-y" aria-labelledby="lookupPerformanceTitle" aria-busy="false">
        <header class="py-5">
            <div class="flex flex-wrap items-start gap-3">
                <div class="min-w-0 flex-1"><p class="text-xs font-bold text-accent-foreground">신청번호 <span data-lookup-reservation-no class="font-mono"></span></p><h2 id="lookupPerformanceTitle" data-lookup-performance-title class="mt-2 text-2xl font-extrabold"></h2><p data-lookup-round class="mt-2 text-sm text-muted-foreground"></p></div>
                <span data-lookup-status></span>
            </div>
        </header>
        <div class="grid gap-7 border-t py-5 lg:grid-cols-[0.75fr_1.25fr]">
            <dl class="grid content-start gap-4">
                <div><dt class="text-xs font-bold text-muted-foreground">신청자</dt><dd data-lookup-name class="mt-1 text-sm font-bold"></dd></div>
                <div><dt class="text-xs font-bold text-muted-foreground">연락처</dt><dd data-lookup-phone class="mt-1 text-sm font-semibold"></dd></div>
                <div><dt class="text-xs font-bold text-muted-foreground">공연장</dt><dd data-lookup-place class="mt-1 text-sm font-semibold"></dd></div>
            </dl>
            <div>
                <h3 class="text-sm font-bold">좌석과 입장 상태</h3>
                <div data-lookup-seats class="mt-3 grid gap-2 sm:grid-cols-2"></div>
            </div>
        </div>
        <footer class="flex flex-col gap-3 border-t py-5 sm:flex-row sm:items-center">
            <a data-lookup-performance-link href="#" class="inline-flex min-h-11 items-center text-sm font-bold text-accent-foreground underline-offset-4 hover:underline">공연 소개 보기</a>
            <div class="flex flex-wrap gap-2 sm:ml-auto"><t:button variant="outline" pageAction="reservation-lookup-reset">다른 신청 조회</t:button><t:button variant="danger" pageAction="reservation-cancel-open">신청 전체 취소</t:button></div>
        </footer>
    </section>

    <section data-lookup-empty class="mt-8 border-y py-10 text-center">
        <h2 class="text-base font-bold">아직 조회한 신청이 없어요</h2>
        <p class="mt-2 text-sm text-muted-foreground">조회 토큰을 입력하면 신청 정보가 이곳에 표시돼요.</p>
    </section>

    <t:sheet id="publicReservationCancelSheet" title="관람 신청 전체 취소" description="신청한 모든 좌석이 취소되며 되돌릴 수 없어요.">
        <jsp:attribute name="footer">
            <t:button variant="outline" action="close-sheet">취소하지 않기</t:button>
            <t:button variant="danger" pageAction="reservation-cancel-save">모든 좌석 취소</t:button>
        </jsp:attribute>
        <jsp:body>
            <div class="mb-5 rounded-md bg-destructive-soft p-4 text-sm text-destructive">
                <strong data-cancel-reservation-summary class="block font-bold"></strong>
                <p class="mt-1 leading-6">취소 후에는 같은 좌석을 다시 보장할 수 없어요.</p>
            </div>
            <form data-public-cancel-form>
                <label class="${label}" for="publicCancelReason">취소 사유 *</label>
                <textarea class="${input} min-h-24 py-3" id="publicCancelReason" required maxlength="500" aria-describedby="publicCancelReasonHelp"></textarea>
                <p id="publicCancelReasonHelp" class="mt-1 text-xs text-muted-foreground">운영 기록에 남으며 외부에 공개되지 않아요.</p>
            </form>
        </jsp:body>
    </t:sheet>
</t:layoutPublic>
