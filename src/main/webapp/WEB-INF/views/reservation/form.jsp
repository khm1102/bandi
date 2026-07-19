<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 disabled:cursor-not-allowed disabled:bg-secondary md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<t:layoutPublic title="관람 신청" active="reserve" scriptPath="reservation/form">
    <div data-reservation-page aria-busy="true">
        <header class="border-b pb-6">
            <a data-performance-link href="#" class="inline-flex min-h-11 items-center text-sm font-bold text-accent-foreground underline-offset-4 hover:underline">공연 소개로 돌아가기</a>
            <h1 data-performance-title class="mt-4 text-3xl font-extrabold tracking-tight">관람 신청</h1>
            <p data-performance-description class="mt-3 max-w-2xl text-sm leading-6 text-muted-foreground">공연과 신청 정보를 불러오는 중이에요.</p>
        </header>

        <ol data-reservation-progress class="mt-5 grid grid-cols-3 border-y text-center text-xs font-bold" aria-label="관람 신청 단계">
            <li data-progress-step="round" class="border-r border-primary bg-accent px-2 py-3 text-accent-foreground" aria-current="step">1. 회차</li>
            <li data-progress-step="seat" class="border-r px-2 py-3 text-muted-foreground">2. 좌석</li>
            <li data-progress-step="applicant" class="px-2 py-3 text-muted-foreground">3. 신청 정보</li>
        </ol>

        <div data-reservation-content class="mt-8 hidden">
            <section data-round-section aria-labelledby="roundSelectionTitle">
                <div class="max-w-2xl">
                    <p class="text-xs font-bold text-accent-foreground">1단계</p>
                    <h2 id="roundSelectionTitle" class="mt-1 text-2xl font-extrabold">관람할 회차를 선택해 주세요</h2>
                    <p class="mt-2 text-sm leading-6 text-muted-foreground">신청 가능한 회차와 제공되는 접근성 지원을 함께 확인할 수 있어요.</p>
                </div>
                <div data-round-list class="mt-5 grid gap-2 sm:grid-cols-2"></div>
            </section>

            <section id="seatSelection" data-seat-section class="mt-10 hidden scroll-mt-20 border-t pt-8" aria-labelledby="seatSelectionTitle">
                <div class="flex flex-col gap-3 sm:flex-row sm:items-end">
                    <div class="min-w-0 flex-1">
                        <p class="text-xs font-bold text-accent-foreground">2단계</p>
                        <h2 id="seatSelectionTitle" class="mt-1 text-2xl font-extrabold">좌석을 선택해 주세요</h2>
                        <p data-seat-guidance class="mt-2 text-sm text-muted-foreground">회차를 선택해 주세요.</p>
                    </div>
                    <span data-seat-remaining></span>
                </div>
                <div class="mt-5 rounded-md bg-sidebar py-2 text-center text-xs font-extrabold tracking-[0.2em] text-sidebar-foreground">무대</div>
                <p class="mt-3 text-xs leading-5 text-muted-foreground">좌석표는 좌우로 이동할 수 있어요. 현재 신청할 수 있는 좌석만 표시해요.</p>
                <div class="mt-4 overflow-x-auto pb-2">
                    <div data-seat-map class="flex min-h-36 min-w-max flex-col items-start gap-2" role="group" aria-label="신청 가능한 좌석" aria-busy="false"></div>
                </div>
                <div class="mt-5 border-t pt-4">
                    <span class="text-xs font-bold text-muted-foreground">선택한 좌석</span>
                    <div data-selected-seats class="mt-2 flex min-h-9 flex-wrap items-center gap-1.5"><span class="text-xs text-muted-foreground">아직 선택한 좌석이 없어요.</span></div>
                </div>
                <div class="sticky bottom-0 -mx-4 mt-6 border-t bg-background/95 px-4 py-4 backdrop-blur-sm sm:static sm:mx-0 sm:bg-transparent sm:px-0 sm:backdrop-blur-none">
                    <button type="button" data-page-action="reservation-next" disabled
                            class="min-h-12 w-full rounded-md bg-primary px-5 text-sm font-bold text-primary-foreground transition-colors hover:bg-primary-strong disabled:cursor-not-allowed disabled:opacity-50 sm:ml-auto sm:block sm:w-auto">
                        선택한 좌석으로 계속
                    </button>
                </div>
            </section>

            <section data-applicant-section class="mt-10 hidden scroll-mt-20 border-t pt-8" aria-labelledby="applicantTitle">
                <div class="max-w-xl">
                    <p class="text-xs font-bold text-accent-foreground">3단계</p>
                    <h2 id="applicantTitle" class="mt-1 text-2xl font-extrabold">신청 정보를 확인해 주세요</h2>
                    <p class="mt-2 text-sm leading-6 text-muted-foreground">입장 확인과 공연 운영 안내에만 사용하며 보관 기간이 지나면 파기해요.</p>
                    <form data-reservation-form class="mt-6 flex flex-col gap-5">
                        <div class="rounded-md bg-secondary p-4">
                            <span class="text-xs font-bold text-muted-foreground">선택 내용</span>
                            <strong data-reservation-summary class="mt-1 block text-sm">회차와 좌석을 선택해 주세요.</strong>
                            <button type="button" data-page-action="reservation-change-seats" class="mt-2 inline-flex min-h-11 items-center text-xs font-bold text-accent-foreground underline-offset-4 hover:underline">좌석 다시 선택</button>
                        </div>
                        <div>
                            <label class="${label}" for="guestName">이름 *</label>
                            <input class="${input}" id="guestName" type="text" required maxlength="100" autocomplete="name">
                        </div>
                        <div>
                            <label class="${label}" for="guestPhone">연락처 *</label>
                            <input class="${input}" id="guestPhone" type="tel" required maxlength="20" pattern="[0-9\- ]{10,20}" inputmode="tel" autocomplete="tel" placeholder="010-0000-0000" aria-describedby="guestPhoneHelp">
                            <p id="guestPhoneHelp" class="mt-1 text-xs text-muted-foreground">공연 변경 안내와 현장 확인에 사용할 연락처를 입력해 주세요.</p>
                        </div>
                        <details class="border-y py-3">
                            <summary class="min-h-11 cursor-pointer py-3 text-sm font-bold">개인정보 수집·이용문 보기</summary>
                            <p data-policy-body class="max-h-48 overflow-y-auto whitespace-pre-line pb-3 text-xs leading-6 text-muted-foreground">동의문을 불러오는 중이에요.</p>
                        </details>
                        <label class="flex min-h-11 cursor-pointer items-start gap-3 rounded-md border p-3 text-sm font-bold">
                            <input id="privacyConsent" type="checkbox" required class="mt-0.5 size-5 shrink-0 accent-primary">
                            <span>개인정보 수집·이용 내용을 확인했으며 필수 수집에 동의해요.</span>
                        </label>
                        <p data-reservation-feedback class="hidden rounded-md border px-3 py-2.5 text-sm" role="status" aria-live="polite"></p>
                        <div class="sticky bottom-0 -mx-4 border-t bg-background/95 px-4 py-4 backdrop-blur-sm sm:static sm:mx-0 sm:bg-transparent sm:px-0 sm:backdrop-blur-none">
                            <t:button type="submit" cssClass="w-full sm:w-auto">이 내용으로 관람 신청</t:button>
                        </div>
                    </form>
                </div>
            </section>
        </div>

        <section data-reservation-complete class="mt-8 hidden border-y border-primary/40 bg-accent/40 py-7 md:px-6" aria-labelledby="reservationCompleteTitle" aria-live="polite">
            <h2 id="reservationCompleteTitle" class="text-2xl font-extrabold">관람 신청이 완료됐어요</h2>
            <p class="mt-2 max-w-2xl text-sm leading-6 text-muted-foreground">신청번호와 조회 토큰을 별도로 보관해 주세요. 조회 토큰은 이 화면을 떠난 뒤 다시 표시되지 않아요.</p>
            <div class="mt-6 grid gap-6 md:grid-cols-[1fr_0.8fr]">
                <div class="flex flex-col gap-4">
                    <div class="border-b pb-4"><span class="text-xs font-bold text-muted-foreground">신청번호</span><strong data-created-reservation-no class="mt-1 block font-mono text-lg font-extrabold"></strong></div>
                    <div class="border-b pb-4"><span class="text-xs font-bold text-muted-foreground">신청 조회 토큰</span><code data-created-lookup-token class="mt-2 block break-all rounded bg-secondary p-3 text-xs font-bold"></code><button type="button" data-page-action="copy-lookup-token" class="mt-2 inline-flex min-h-11 items-center text-xs font-bold text-accent-foreground underline-offset-4 hover:underline">조회 토큰 복사</button></div>
                    <a href="<c:url value='/reserve/lookup'/>" class="inline-flex min-h-11 items-center justify-center rounded-md border bg-card px-4 text-sm font-bold hover:bg-secondary">신청 조회·취소로 이동</a>
                </div>
                <div class="text-center">
                    <span class="text-xs font-bold text-muted-foreground">공연 당일 입장 QR</span>
                    <img data-entry-qr alt="공연 당일 입장 QR" width="280" height="280" class="mx-auto mt-3 aspect-square w-full max-w-64 rounded-md bg-card p-3">
                    <button type="button" data-page-action="download-entry-qr" class="mt-2 inline-flex min-h-11 items-center text-xs font-bold text-accent-foreground underline-offset-4 hover:underline">입장 QR 저장</button>
                    <p class="mt-2 text-xs leading-5 text-muted-foreground">QR에는 개인정보가 아닌 입장 토큰만 포함돼요.</p>
                </div>
            </div>
        </section>

        <section data-reservation-error class="mt-8 hidden border-y border-destructive bg-destructive-soft py-6 md:px-5" role="alert">
            <h2 class="text-lg font-bold text-destructive">관람 신청 정보를 불러오지 못했어요</h2>
            <p data-reservation-error-message class="mt-2 text-sm leading-6 text-destructive"></p>
            <div class="mt-4 flex flex-wrap gap-2"><button type="button" data-page-action="reservation-retry" class="inline-flex min-h-11 items-center rounded-md bg-destructive px-4 text-sm font-bold text-destructive-foreground">다시 불러오기</button><a href="<c:url value='/notices'/>" class="inline-flex min-h-11 items-center px-3 text-sm font-bold text-destructive underline-offset-4 hover:underline">공시 확인</a></div>
        </section>
    </div>
</t:layoutPublic>
