<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<t:layoutPublic title="관람 신청 조회" active="reserve" scriptPath="reservation/lookup">
    <section class="border-b pb-6">
        <p class="text-xs font-black uppercase tracking-[0.18em] text-accent-foreground">Reservation lookup</p>
        <h1 class="mt-2 text-3xl font-black tracking-tight">관람 신청 조회·취소</h1>
        <p class="mt-3 max-w-2xl text-sm leading-6 text-muted-foreground">신청 완료 시 발급받은 조회 토큰으로 공연, 회차, 좌석과 입장 상태를 확인합니다. 토큰은 주소에 저장되지 않습니다.</p>
    </section>

    <section class="mt-6 rounded-lg border bg-card p-5" aria-labelledby="lookupFormTitle">
        <h2 id="lookupFormTitle" class="text-base font-black">조회 토큰 입력</h2>
        <form data-lookup-form class="mt-4 flex flex-col gap-2 sm:flex-row">
            <div class="min-w-0 flex-1">
                <label class="${label}" for="lookupToken">신청 조회 토큰</label>
                <input class="${input}" id="lookupToken" type="password" required maxlength="200" autocomplete="off" spellcheck="false">
            </div>
            <t:button type="submit" cssClass="mt-auto shrink-0">신청 조회</t:button>
        </form>
    </section>

    <section data-lookup-result class="mt-6 hidden rounded-lg border bg-card" aria-live="polite" aria-busy="false">
        <div class="border-b p-5">
            <div class="flex flex-wrap items-start gap-3">
                <div><p class="text-xs font-black text-accent-foreground">신청번호 <span data-lookup-reservation-no class="font-mono"></span></p><h2 data-lookup-performance-title class="mt-2 text-2xl font-black"></h2><p data-lookup-round class="mt-2 text-sm text-muted-foreground"></p></div>
                <span data-lookup-status class="ml-auto"></span>
            </div>
        </div>
        <div class="grid gap-5 p-5 lg:grid-cols-[0.8fr_1.2fr]">
            <dl class="grid content-start gap-3 rounded-md bg-secondary p-4">
                <div><dt class="text-xs font-bold text-muted-foreground">신청자</dt><dd data-lookup-name class="mt-1 text-sm font-black"></dd></div>
                <div><dt class="text-xs font-bold text-muted-foreground">연락처</dt><dd data-lookup-phone class="mt-1 text-sm font-semibold"></dd></div>
                <div><dt class="text-xs font-bold text-muted-foreground">공연장</dt><dd data-lookup-place class="mt-1 text-sm font-semibold"></dd></div>
            </dl>
            <div>
                <h3 class="text-sm font-black">좌석과 입장 상태</h3>
                <div data-lookup-seats class="mt-3 grid gap-2 sm:grid-cols-2"></div>
            </div>
        </div>
        <div class="flex flex-wrap items-center justify-between gap-3 border-t p-5">
            <a data-lookup-performance-link href="#" class="inline-flex min-h-11 items-center text-xs font-black text-accent-foreground underline-offset-4 hover:underline">공연 소개 보기</a>
            <t:button variant="danger" pageAction="reservation-cancel-open">신청 전체 취소</t:button>
        </div>
    </section>

    <p data-lookup-empty class="mt-6 rounded-lg border bg-card p-8 text-center text-sm text-muted-foreground">조회 토큰을 입력하면 신청 정보를 확인할 수 있습니다.</p>

    <t:modal id="publicReservationCancelModal" title="관람 신청 전체 취소" description="신청한 모든 좌석이 취소되며 되돌릴 수 없습니다.">
        <jsp:attribute name="footer">
            <t:button variant="outline" action="close-modal">닫기</t:button>
            <t:button variant="danger" pageAction="reservation-cancel-save">전체 취소</t:button>
        </jsp:attribute>
        <jsp:body>
            <form data-public-cancel-form>
                <label class="${label}" for="publicCancelReason">취소 사유 *</label>
                <textarea class="${input} min-h-24 py-3" id="publicCancelReason" required maxlength="500"></textarea>
            </form>
        </jsp:body>
    </t:modal>
</t:layoutPublic>
