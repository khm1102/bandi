<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="seatFree" value="flex size-11 shrink-0 items-center justify-center rounded-md border bg-card text-xs font-extrabold text-muted-foreground transition-colors hover:border-primary hover:bg-accent hover:text-accent-foreground"/>
<c:set var="seatSel" value="flex size-11 shrink-0 items-center justify-center rounded-md border border-primary bg-primary text-xs font-extrabold text-primary-foreground"/>
<c:set var="seatTaken" value="flex size-11 shrink-0 cursor-not-allowed items-center justify-center rounded-md border bg-secondary text-xs font-extrabold text-muted-foreground/50"/>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<t:layoutPublic title="관람 신청" active="reserve" scriptPath="reservation/form">
    <div class="grid gap-5 lg:grid-cols-[1.3fr_1fr]">
        <div class="relative flex min-h-96 flex-col justify-between overflow-hidden rounded-xl bg-sidebar p-6 text-white">
            <img src="<c:url value='/images/performance/show-house-boy.webp'/>" alt="어두운 무대 위 집 세트의 문 앞에 선 인물" width="960" height="1200" class="absolute inset-0 size-full object-cover" fetchpriority="high">
            <div class="absolute inset-0 bg-sidebar/35" aria-hidden="true"></div>
            <span class="relative text-xs font-extrabold tracking-widest text-primary">반디 정기공연</span>
            <div class="relative">
                <b class="block text-4xl font-black leading-none tracking-tight">소년 B가<br>사는 집</b>
                <span class="mt-2 block text-xs text-sidebar-muted">The House Where Boy B Lives</span>
            </div>
        </div>
        <section class="min-w-0 rounded-xl border bg-card p-6">
            <div class="flex gap-1.5">
                <t:badge tone="danger" dot="true">관람 신청 오픈</t:badge>
                <t:badge tone="neutral">전석 무료</t:badge>
            </div>
            <h2 class="mt-2 text-2xl font-black tracking-tight">연극 〈소년 B가 사는 집〉</h2>
            <p class="text-sm text-muted-foreground">연극 동아리 반디 정기공연 · 러닝타임 90분</p>
            <dl class="mt-4">
                <div class="flex gap-3 border-b py-2.5 text-sm"><dt class="w-16 shrink-0 font-bold text-muted-foreground">장소</dt><dd class="font-semibold">한국공학대학교 TIP아트센터</dd></div>
                <div class="flex gap-3 border-b py-2.5 text-sm"><dt class="w-16 shrink-0 font-bold text-muted-foreground">공연기간</dt><dd class="font-semibold">2025.06.21(토) - 06.22(일)</dd></div>
                <div class="flex gap-3 border-b py-2.5 text-sm"><dt class="w-16 shrink-0 font-bold text-muted-foreground">공연시간</dt><dd class="font-semibold">하루 1회 · 매일 17:00 (총 2회)</dd></div>
                <div class="flex gap-3 py-2.5 text-sm"><dt class="w-16 shrink-0 font-bold text-muted-foreground">관람연령</dt><dd class="font-semibold">만 12세 이상</dd></div>
            </dl>
            <a href="#seatSelection" class="mt-4 inline-flex min-h-11 w-full items-center justify-center rounded-md bg-primary px-4 text-sm font-black text-primary-foreground transition-colors hover:bg-primary-strong hover:text-white">좌석 선택 시작</a>
        </section>
    </div>

    <div class="mt-4 rounded-lg border bg-secondary px-5 py-4">
        <h2 class="text-sm font-extrabold">작품 소개</h2>
        <p class="mt-2 max-w-3xl text-sm leading-relaxed text-foreground/80">그날, 우리 집에 지독한 불행이 찾아왔습니다. 오랜 시간이 흘렀지만, 누군가는 그날에 머물러 있고 누군가는 그날을 견디며 살아갑니다. 지워지지 않는 기억과 남겨진 상처, 그럼에도 살아가려는 사람들의 이야기입니다.</p>
    </div>

    <div class="mt-5 grid items-start gap-5 lg:grid-cols-[1.4fr_1fr]">
        <section id="seatSelection" class="min-w-0 scroll-mt-20 rounded-xl border bg-card p-6">
            <div class="flex items-center">
                <h3 class="text-base font-black">좌석 선택</h3>
                <span class="ml-auto" data-seat-remaining><t:badge tone="success" dot="true">잔여 40석</t:badge></span>
            </div>
            <p class="mb-4 mt-1 text-xs text-muted-foreground" data-seat-date-label>6/21 (토) 17:00, 좌석을 눌러 선택하세요</p>
            <div class="rounded-md bg-sidebar py-2 text-center text-xs font-extrabold tracking-widest text-sidebar-foreground">S T A G E · 무대</div>
            <p class="mb-2 text-xs text-muted-foreground md:hidden">좌석표는 좌우로 이동해 확인할 수 있습니다.</p>
            <div class="mt-3 flex flex-col items-start gap-2 overflow-x-auto pb-2 md:mt-5 md:items-center" data-seat-map role="group" aria-label="좌석 선택표">
                <div class="flex min-w-max items-center gap-1.5"><span class="w-5 text-center text-xs font-extrabold text-muted-foreground">A</span>
                    <button type="button" class="${seatFree}">1</button><button type="button" class="${seatFree}">2</button><button type="button" disabled class="${seatTaken}">3</button><button type="button" disabled class="${seatTaken}">4</button><button type="button" class="${seatFree}">5</button><button type="button" class="${seatFree}">6</button><button type="button" class="${seatFree}">7</button><button type="button" class="${seatFree}">8</button>
                </div>
                <div class="flex min-w-max items-center gap-1.5"><span class="w-5 text-center text-xs font-extrabold text-muted-foreground">B</span>
                    <button type="button" class="${seatFree}">1</button><button type="button" class="${seatFree}">2</button><button type="button" class="${seatFree}">3</button><button type="button" class="${seatFree}">4</button><button type="button" class="${seatFree}">5</button><button type="button" class="${seatFree}">6</button><button type="button" class="${seatFree}">7</button><button type="button" class="${seatFree}">8</button>
                </div>
                <div class="flex min-w-max items-center gap-1.5"><span class="w-5 text-center text-xs font-extrabold text-muted-foreground">C</span>
                    <button type="button" class="${seatFree}">1</button><button type="button" class="${seatFree}">2</button><button type="button" class="${seatFree}">3</button><button type="button" class="${seatFree}">4</button><button type="button" disabled class="${seatTaken}">5</button><button type="button" disabled class="${seatTaken}">6</button><button type="button" class="${seatFree}">7</button><button type="button" class="${seatFree}">8</button>
                </div>
                <div class="flex min-w-max items-center gap-1.5"><span class="w-5 text-center text-xs font-extrabold text-muted-foreground">D</span>
                    <button type="button" disabled class="${seatTaken}">1</button><button type="button" disabled class="${seatTaken}">2</button><button type="button" class="${seatFree}">3</button><button type="button" class="${seatFree}">4</button><button type="button" class="${seatFree}">5</button><button type="button" class="${seatFree}">6</button><button type="button" class="${seatFree}">7</button><button type="button" class="${seatFree}">8</button>
                </div>
                <div class="flex min-w-max items-center gap-1.5"><span class="w-5 text-center text-xs font-extrabold text-muted-foreground">E</span>
                    <button type="button" class="${seatFree}">1</button><button type="button" class="${seatFree}">2</button><button type="button" class="${seatFree}">3</button><button type="button" disabled class="${seatTaken}">4</button><button type="button" disabled class="${seatTaken}">5</button><button type="button" class="${seatFree}">6</button><button type="button" class="${seatFree}">7</button><button type="button" class="${seatFree}">8</button>
                </div>
                <div class="flex min-w-max items-center gap-1.5"><span class="w-5 text-center text-xs font-extrabold text-muted-foreground">F</span>
                    <button type="button" class="${seatFree}">1</button><button type="button" class="${seatFree}">2</button><button type="button" class="${seatFree}">3</button><button type="button" class="${seatFree}">4</button><button type="button" class="${seatFree}">5</button><button type="button" class="${seatFree}">6</button><button type="button" class="${seatFree}">7</button><button type="button" class="${seatFree}">8</button>
                </div>
            </div>
            <div class="mt-5 flex justify-center gap-4 text-xs font-bold text-muted-foreground">
                <span class="flex items-center gap-1.5"><i class="size-3.5 rounded-sm border bg-card"></i> 선택 가능</span>
                <span class="flex items-center gap-1.5"><i class="size-3.5 rounded-sm bg-primary"></i> 선택함</span>
                <span class="flex items-center gap-1.5"><i class="size-3.5 rounded-sm border bg-secondary"></i> 매진</span>
            </div>
        </section>

        <section class="min-w-0 rounded-xl border bg-card p-6">
            <h3 class="text-base font-black">관람 신청</h3>
            <p class="text-xs text-muted-foreground">하루 한 회 공연입니다.</p>
            <form method="post" data-reservation-form>
                <p class="mb-2 mt-4 text-xs font-extrabold text-muted-foreground">관람일 선택</p>
                <div class="grid grid-cols-2 gap-2">
                    <button type="button" data-reservation-date="6/21" aria-pressed="true" class="min-h-14 rounded-lg border-2 border-primary bg-accent p-3 text-center text-base font-black text-accent-foreground">6.21<small class="mt-0.5 block text-xs font-bold text-muted-foreground">토 · 17:00</small></button>
                    <button type="button" data-reservation-date="6/22" aria-pressed="false" class="min-h-14 rounded-lg border-2 p-3 text-center text-base font-black transition-colors hover:border-primary">6.22<small class="mt-0.5 block text-xs font-bold text-muted-foreground">일 · 17:00</small></button>
                </div>
                <p class="mb-2 mt-4 text-xs font-extrabold text-muted-foreground">선택한 좌석</p>
                <div class="flex min-h-9 flex-wrap items-center gap-1.5" data-selected-seats>
                    <span class="text-xs text-muted-foreground">아직 선택한 좌석이 없어요</span>
                </div>
                <p class="mb-2 mt-4 text-xs font-extrabold text-muted-foreground">신청자 정보</p>
                <div class="flex flex-col gap-3">
                    <div><label class="${label}" for="guestName">이름 <span aria-hidden="true">*</span></label><input class="${input}" id="guestName" name="guestName" type="text" autocomplete="name" placeholder="이름을 입력하세요" required maxlength="50" aria-describedby="reservationFeedback"></div>
                    <div><label class="${label}" for="guestPhone">연락처 <span aria-hidden="true">*</span></label><input class="${input}" id="guestPhone" name="guestPhone" type="tel" inputmode="tel" autocomplete="tel" placeholder="010-0000-0000" required maxlength="20" aria-describedby="reservationFeedback"></div>
                </div>
                <div class="my-4 flex items-center justify-between rounded-lg bg-secondary px-4 py-3.5 text-xs font-bold text-muted-foreground">
                    <span data-reservation-summary>6/21 토 17:00<br>TIP아트센터</span>
                    <b class="text-base font-black text-foreground" data-reservation-count>0석</b>
                </div>
                <button type="submit" data-reservation-submit class="h-12 w-full rounded-lg bg-primary text-base font-black text-primary-foreground transition-colors hover:bg-primary-strong hover:text-white">좌석을 선택하세요</button>
                <p id="reservationFeedback" class="mt-3 hidden rounded-md border px-3 py-2.5 text-sm" data-reservation-feedback role="status" aria-live="polite"></p>
            </form>
        </section>
    </div>
</t:layoutPublic>
