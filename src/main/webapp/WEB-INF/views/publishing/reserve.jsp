<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="seatFree" value="flex size-8 items-center justify-center rounded-md border bg-card text-xs font-extrabold text-muted-foreground transition-colors hover:border-primary hover:bg-accent hover:text-accent-foreground"/>
<c:set var="seatSel" value="flex size-8 items-center justify-center rounded-md border border-primary bg-primary text-xs font-extrabold text-primary-foreground"/>
<c:set var="seatTaken" value="flex size-8 cursor-not-allowed items-center justify-center rounded-md border bg-secondary text-xs font-extrabold text-muted-foreground/50"/>
<c:set var="input" value="h-10 w-full rounded-md border border-input bg-card px-3 text-sm transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<t:layoutPublic title="관람 신청">
    <div class="grid gap-5 lg:grid-cols-[1.3fr_1fr]">
        <div class="relative flex min-h-80 flex-col justify-between overflow-hidden rounded-xl bg-linear-to-br from-sidebar to-sidebar-accent p-6 text-white">
            <div class="absolute inset-0 bg-linear-to-br from-primary/20 to-transparent" aria-hidden="true"></div>
            <span class="relative text-xs font-extrabold tracking-widest text-primary">반디 정기공연</span>
            <div class="relative">
                <b class="block text-4xl font-black leading-none tracking-tight">소년 B가<br>사는 집</b>
                <span class="mt-2 block text-xs text-sidebar-muted">The House Where Boy B Lives</span>
            </div>
        </div>
        <section class="rounded-xl border bg-card p-6">
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
        </section>
    </div>

    <div class="mt-4 rounded-lg border-l-4 border-l-primary bg-secondary px-5 py-4">
        <b class="text-xs font-extrabold text-accent-foreground">작품 소개</b>
        <p class="mt-2 text-sm leading-relaxed text-foreground/80">그날, 우리집에 지독한 불행이 찾아왔습니다.<br>오랜 시간이 흘렀지만, 누군가는 그날에 머물러 있고 누군가는 그날을 견디며 살아갑니다.<br><br>지워지지 않는 기억, 남겨진 상처, 그리고 그럼에도 살아가려는 사람들의 이야기.<br>연극 〈소년 B가 사는 집〉 — 그들의 조용하고도 치열한 시간을 함께해주세요.</p>
    </div>

    <div class="mt-5 grid items-start gap-5 lg:grid-cols-[1.4fr_1fr]">
        <section class="rounded-xl border bg-card p-6">
            <div class="flex items-center">
                <h3 class="text-base font-black">좌석 선택</h3>
                <span class="ml-auto"><t:badge tone="success" dot="true">잔여 40석</t:badge></span>
            </div>
            <p class="mb-4 mt-1 text-xs text-muted-foreground">6/21 (토) 17:00 · 좌석을 눌러 선택하세요</p>
            <div class="rounded-md bg-sidebar py-2 text-center text-xs font-extrabold tracking-widest text-sidebar-foreground">S T A G E · 무대</div>
            <div class="mt-5 flex flex-col items-center gap-2 overflow-x-auto">
                <div class="flex items-center gap-1.5"><span class="w-5 text-center text-xs font-extrabold text-muted-foreground">A</span>
                    <button type="button" class="${seatFree}">1</button><button type="button" class="${seatFree}">2</button><button type="button" disabled class="${seatTaken}">3</button><button type="button" disabled class="${seatTaken}">4</button><button type="button" class="${seatFree}">5</button><button type="button" class="${seatFree}">6</button><button type="button" class="${seatFree}">7</button><button type="button" class="${seatFree}">8</button>
                </div>
                <div class="flex items-center gap-1.5"><span class="w-5 text-center text-xs font-extrabold text-muted-foreground">B</span>
                    <button type="button" class="${seatFree}">1</button><button type="button" class="${seatFree}">2</button><button type="button" class="${seatFree}">3</button><button type="button" class="${seatSel}">4</button><button type="button" class="${seatSel}">5</button><button type="button" class="${seatFree}">6</button><button type="button" class="${seatFree}">7</button><button type="button" class="${seatFree}">8</button>
                </div>
                <div class="flex items-center gap-1.5"><span class="w-5 text-center text-xs font-extrabold text-muted-foreground">C</span>
                    <button type="button" class="${seatFree}">1</button><button type="button" class="${seatFree}">2</button><button type="button" class="${seatFree}">3</button><button type="button" class="${seatFree}">4</button><button type="button" disabled class="${seatTaken}">5</button><button type="button" disabled class="${seatTaken}">6</button><button type="button" class="${seatFree}">7</button><button type="button" class="${seatFree}">8</button>
                </div>
                <div class="flex items-center gap-1.5"><span class="w-5 text-center text-xs font-extrabold text-muted-foreground">D</span>
                    <button type="button" disabled class="${seatTaken}">1</button><button type="button" disabled class="${seatTaken}">2</button><button type="button" class="${seatFree}">3</button><button type="button" class="${seatFree}">4</button><button type="button" class="${seatFree}">5</button><button type="button" class="${seatFree}">6</button><button type="button" class="${seatFree}">7</button><button type="button" class="${seatFree}">8</button>
                </div>
                <div class="flex items-center gap-1.5"><span class="w-5 text-center text-xs font-extrabold text-muted-foreground">E</span>
                    <button type="button" class="${seatFree}">1</button><button type="button" class="${seatFree}">2</button><button type="button" class="${seatFree}">3</button><button type="button" disabled class="${seatTaken}">4</button><button type="button" disabled class="${seatTaken}">5</button><button type="button" class="${seatFree}">6</button><button type="button" class="${seatFree}">7</button><button type="button" class="${seatFree}">8</button>
                </div>
                <div class="flex items-center gap-1.5"><span class="w-5 text-center text-xs font-extrabold text-muted-foreground">F</span>
                    <button type="button" class="${seatFree}">1</button><button type="button" class="${seatFree}">2</button><button type="button" class="${seatFree}">3</button><button type="button" class="${seatFree}">4</button><button type="button" class="${seatFree}">5</button><button type="button" class="${seatFree}">6</button><button type="button" class="${seatFree}">7</button><button type="button" class="${seatFree}">8</button>
                </div>
            </div>
            <div class="mt-5 flex justify-center gap-4 text-xs font-bold text-muted-foreground">
                <span class="flex items-center gap-1.5"><i class="size-3.5 rounded-sm border bg-card"></i> 선택 가능</span>
                <span class="flex items-center gap-1.5"><i class="size-3.5 rounded-sm bg-primary"></i> 선택함</span>
                <span class="flex items-center gap-1.5"><i class="size-3.5 rounded-sm border bg-secondary"></i> 매진</span>
            </div>
        </section>

        <section class="rounded-xl border bg-card p-6">
            <h3 class="text-base font-black">관람 신청</h3>
            <p class="text-xs text-muted-foreground">하루 한 회 공연입니다.</p>
            <p class="mb-2 mt-4 text-xs font-extrabold text-muted-foreground">관람일 선택</p>
            <div class="grid grid-cols-2 gap-2">
                <button type="button" class="rounded-lg border-2 border-primary bg-accent p-3 text-center text-base font-black text-accent-foreground">6.21<small class="mt-0.5 block text-xs font-bold text-muted-foreground">토 · 17:00</small></button>
                <button type="button" class="rounded-lg border-2 p-3 text-center text-base font-black transition-colors hover:border-primary">6.22<small class="mt-0.5 block text-xs font-bold text-muted-foreground">일 · 17:00</small></button>
            </div>
            <p class="mb-2 mt-4 text-xs font-extrabold text-muted-foreground">선택한 좌석</p>
            <div class="flex min-h-9 flex-wrap items-center gap-1.5">
                <span class="inline-flex items-center gap-1 rounded-md bg-accent py-1 pl-2.5 pr-1.5 text-xs font-extrabold text-accent-foreground">B4 <button type="button" class="flex size-4 items-center justify-center rounded-sm bg-primary/20">&times;</button></span>
                <span class="inline-flex items-center gap-1 rounded-md bg-accent py-1 pl-2.5 pr-1.5 text-xs font-extrabold text-accent-foreground">B5 <button type="button" class="flex size-4 items-center justify-center rounded-sm bg-primary/20">&times;</button></span>
            </div>
            <p class="mb-2 mt-4 text-xs font-extrabold text-muted-foreground">신청자 정보</p>
            <div class="flex flex-col gap-3">
                <div><label class="${label}" for="guestName">이름 <span class="text-accent-foreground">*</span></label><input class="${input}" id="guestName" type="text" placeholder="이름을 입력하세요"></div>
                <div><label class="${label}" for="guestPhone">연락처 <span class="text-accent-foreground">*</span></label><input class="${input}" id="guestPhone" type="text" placeholder="010-0000-0000"></div>
            </div>
            <div class="my-4 flex items-center justify-between rounded-lg bg-secondary px-4 py-3.5 text-xs font-bold text-muted-foreground">
                <span>6/21 토 17:00<br>TIP아트센터</span>
                <b class="text-base font-black text-foreground">2석</b>
            </div>
            <button type="button" class="h-12 w-full rounded-lg bg-primary text-base font-black text-primary-foreground transition-colors hover:bg-primary-strong hover:text-white">2석 관람 신청하기</button>
        </section>
    </div>
</t:layoutPublic>
