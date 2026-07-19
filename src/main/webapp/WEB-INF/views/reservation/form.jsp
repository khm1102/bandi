<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 disabled:cursor-not-allowed disabled:bg-secondary md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<t:layoutPublic title="관람 신청" active="reserve" scriptPath="reservation/form">
    <div data-reservation-page aria-busy="true">
        <section class="border-b pb-6">
            <a data-performance-link href="#" class="inline-flex min-h-11 items-center text-xs font-black text-accent-foreground underline-offset-4 hover:underline">공연 소개로 돌아가기</a>
            <p class="mt-4 text-xs font-black uppercase tracking-[0.18em] text-accent-foreground">Reservation</p>
            <h1 data-performance-title class="mt-2 text-3xl font-black tracking-tight">관람 신청</h1>
            <p data-performance-description class="mt-3 max-w-2xl text-sm leading-6 text-muted-foreground">공연과 신청 정보를 불러오는 중입니다.</p>
        </section>

        <div data-reservation-content class="mt-6 hidden grid items-start gap-5 lg:grid-cols-[1.35fr_0.85fr]">
            <div class="flex min-w-0 flex-col gap-5">
                <section class="rounded-lg border bg-card p-5" aria-labelledby="roundSelectionTitle">
                    <h2 id="roundSelectionTitle" class="text-base font-black">1. 공연 회차 선택</h2>
                    <p class="mt-1 text-xs text-muted-foreground">신청 가능한 회차만 선택할 수 있습니다.</p>
                    <div data-round-list class="mt-4 grid gap-2 sm:grid-cols-2"></div>
                </section>

                <section id="seatSelection" class="scroll-mt-20 rounded-lg border bg-card p-5" aria-labelledby="seatSelectionTitle">
                    <div class="flex flex-wrap items-center gap-3">
                        <div>
                            <h2 id="seatSelectionTitle" class="text-base font-black">2. 좌석 선택</h2>
                            <p data-seat-guidance class="mt-1 text-xs text-muted-foreground">회차를 선택해 주세요.</p>
                        </div>
                        <span data-seat-remaining class="ml-auto"></span>
                    </div>
                    <div class="mt-5 rounded-md bg-sidebar py-2 text-center text-xs font-extrabold tracking-[0.25em] text-sidebar-foreground">STAGE · 무대</div>
                    <p class="mt-3 text-xs text-muted-foreground">좌석표는 좌우로 이동할 수 있습니다. 표시된 좌석은 현재 신청 가능한 좌석입니다.</p>
                    <div class="mt-4 overflow-x-auto pb-2">
                        <div data-seat-map class="grid min-h-36 min-w-max place-content-start gap-2" role="group" aria-label="신청 가능한 좌석"></div>
                    </div>
                    <div class="mt-5 border-t pt-4">
                        <span class="text-xs font-black text-muted-foreground">선택한 좌석</span>
                        <div data-selected-seats class="mt-2 flex min-h-9 flex-wrap items-center gap-1.5"><span class="text-xs text-muted-foreground">아직 선택한 좌석이 없습니다.</span></div>
                    </div>
                </section>
            </div>

            <section class="rounded-lg border bg-card p-5 lg:sticky lg:top-20" aria-labelledby="applicantTitle">
                <h2 id="applicantTitle" class="text-base font-black">3. 신청자 정보</h2>
                <p class="mt-1 text-xs text-muted-foreground">입장 확인과 공연 운영 안내에 사용합니다.</p>
                <form data-reservation-form class="mt-5 flex flex-col gap-4">
                    <div>
                        <label class="${label}" for="guestName">이름 *</label>
                        <input class="${input}" id="guestName" type="text" required maxlength="100" autocomplete="name">
                    </div>
                    <div>
                        <label class="${label}" for="guestPhone">연락처 *</label>
                        <input class="${input}" id="guestPhone" type="tel" required maxlength="20" pattern="[0-9\- ]{10,20}" inputmode="tel" autocomplete="tel" placeholder="010-0000-0000">
                    </div>
                    <details class="rounded-md border bg-secondary p-3">
                        <summary class="cursor-pointer text-xs font-black">개인정보 수집·이용문 보기</summary>
                        <p data-policy-body class="mt-3 max-h-48 overflow-y-auto whitespace-pre-line text-xs leading-6 text-muted-foreground">동의문을 불러오는 중입니다.</p>
                    </details>
                    <label class="flex min-h-11 cursor-pointer items-start gap-3 rounded-md border p-3 text-sm font-bold">
                        <input id="privacyConsent" type="checkbox" required class="mt-0.5 size-5 shrink-0 accent-primary">
                        <span>개인정보 수집·이용 내용을 확인했으며 필수 수집에 동의합니다.</span>
                    </label>
                    <div class="rounded-md bg-secondary p-4">
                        <span class="text-xs font-bold text-muted-foreground">신청 요약</span>
                        <strong data-reservation-summary class="mt-1 block text-sm">회차와 좌석을 선택해 주세요.</strong>
                    </div>
                    <t:button type="submit" cssClass="w-full">관람 신청하기</t:button>
                    <p data-reservation-feedback class="hidden rounded-md border px-3 py-2.5 text-sm" role="status" aria-live="polite"></p>
                </form>
            </section>
        </div>

        <section data-reservation-complete class="mt-6 hidden rounded-xl border border-primary/40 bg-accent/40 p-5 md:p-8" aria-labelledby="reservationCompleteTitle" aria-live="polite">
            <p class="text-xs font-black uppercase tracking-[0.18em] text-accent-foreground">Reservation complete</p>
            <h2 id="reservationCompleteTitle" class="mt-2 text-2xl font-black">관람 신청이 완료되었습니다</h2>
            <p class="mt-2 text-sm leading-6 text-muted-foreground">아래 신청번호와 조회 토큰을 별도로 보관하세요. 조회 토큰은 다시 표시되지 않습니다.</p>
            <div class="mt-6 grid gap-5 md:grid-cols-[1fr_0.8fr]">
                <div class="flex flex-col gap-4">
                    <div class="rounded-lg border bg-card p-4"><span class="text-xs font-bold text-muted-foreground">신청번호</span><strong data-created-reservation-no class="mt-1 block font-mono text-lg font-black"></strong></div>
                    <div class="rounded-lg border bg-card p-4"><span class="text-xs font-bold text-muted-foreground">신청 조회 토큰</span><code data-created-lookup-token class="mt-2 block break-all rounded bg-secondary p-3 text-xs font-bold"></code><button type="button" data-page-action="copy-lookup-token" class="mt-2 inline-flex min-h-11 items-center text-xs font-black text-accent-foreground underline-offset-4 hover:underline">조회 토큰 복사</button></div>
                    <a href="<c:url value='/reserve/lookup'/>" class="inline-flex min-h-11 items-center justify-center rounded-md border bg-card px-4 text-sm font-black hover:bg-secondary">신청 조회·취소로 이동</a>
                </div>
                <div class="rounded-lg bg-card p-4 text-center">
                    <span class="text-xs font-bold text-muted-foreground">공연 당일 입장 QR</span>
                    <img data-entry-qr alt="공연 당일 입장 QR" width="280" height="280" class="mx-auto mt-3 aspect-square w-full max-w-64">
                    <button type="button" data-page-action="download-entry-qr" class="mt-2 inline-flex min-h-11 items-center text-xs font-black text-accent-foreground underline-offset-4 hover:underline">입장 QR 저장</button>
                    <p class="mt-2 text-xs leading-5 text-muted-foreground">QR에는 개인정보가 아닌 입장 토큰만 포함됩니다.</p>
                </div>
            </div>
        </section>

        <section data-reservation-error class="mt-6 hidden rounded-lg border border-destructive bg-destructive-soft p-5" role="alert">
            <h2 class="text-base font-black text-destructive">관람 신청 정보를 불러오지 못했습니다</h2>
            <p data-reservation-error-message class="mt-2 text-sm text-destructive"></p>
            <a href="<c:url value='/notices'/>" class="mt-3 inline-flex min-h-11 items-center text-sm font-black text-destructive underline-offset-4 hover:underline">공시 확인</a>
        </section>
    </div>
</t:layoutPublic>
