<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="hi" value="flex h-8 items-center justify-center rounded-md bg-success text-xs font-extrabold text-white"/>
<c:set var="mid" value="flex h-8 items-center justify-center rounded-md bg-warning/80 text-xs font-extrabold text-white"/>
<c:set var="lo" value="flex h-8 items-center justify-center rounded-md bg-destructive/70 text-xs font-extrabold text-white"/>
<c:set var="hcell" value="py-1 text-center text-xs font-extrabold text-muted-foreground"/>
<c:set var="rcell" value="flex items-center text-xs font-extrabold text-muted-foreground"/>
<c:choose>
    <c:when test="${role == 'member'}"><c:set var="userName" value="김하늘"/><c:set var="userIni" value="KH"/></c:when>
    <c:when test="${role == 'leader'}"><c:set var="userName" value="정도윤"/><c:set var="userIni" value="JD"/></c:when>
    <c:otherwise><c:set var="userName" value="이서준"/><c:set var="userIni" value="LS"/></c:otherwise>
</c:choose>
<t:layout title="일정 조율" active="schedule" role="${role}" scriptPath="schedule/coordination">
    <t:pageHead title="일정 조율" description="부원 가능 시간을 모아 공통 시간을 자동으로 찾습니다">
        <t:button openModal="timePickModal">시간 입력하기</t:button>
    </t:pageHead>

    <div class="grid items-start gap-4 lg:grid-cols-[1.7fr_1fr]">
        <div class="flex flex-col gap-4">
            <section class="overflow-hidden rounded-lg border bg-card">
                <header class="flex items-center gap-2 border-b px-5 py-4">
                    <h3 class="text-sm font-extrabold">6월 4주차 정기연습 시간</h3>
                    <span class="ml-auto"><t:badge tone="warning" dot="true">응답 진행 중</t:badge></span>
                </header>
                <div class="p-5">
                    <p class="mb-3 text-xs text-muted-foreground">후보 5일 × 4시간대 · 숫자는 가능 인원 (총 12명) · 부원 전원의 응답이 한 화면에 모입니다</p>
                    <div class="grid grid-cols-[64px_repeat(5,1fr)] gap-1">
                        <div></div>
                        <div class="${hcell}">6/23<br><span class="font-semibold">월</span></div>
                        <div class="${hcell}">6/24<br><span class="font-semibold">화</span></div>
                        <div class="${hcell}">6/25<br><span class="font-semibold">수</span></div>
                        <div class="${hcell}">6/26<br><span class="font-semibold">목</span></div>
                        <div class="${hcell}">6/27<br><span class="font-semibold">금</span></div>
                        <div class="${rcell}">18시</div><div class="${mid}">8</div><div class="${hi}">10</div><div class="${hi}">11</div><div class="${mid}">6</div><div class="${hi}">9</div>
                        <div class="${rcell}">19시</div><div class="${hi}">11</div><div class="${hi}">11</div><div class="${hi}">9</div><div class="${mid}">8</div><div class="${hi}">10</div>
                        <div class="${rcell}">20시</div><div class="${hi}">9</div><div class="${mid}">7</div><div class="${hi}">11</div><div class="${lo}">5</div><div class="${mid}">8</div>
                        <div class="${rcell}">21시</div><div class="${lo}">4</div><div class="${mid}">6</div><div class="${mid}">8</div><div class="${lo}">3</div><div class="${lo}">5</div>
                    </div>
                    <div class="mt-3 flex gap-4 text-xs font-bold text-muted-foreground">
                        <span class="flex items-center gap-1.5"><i class="size-3.5 rounded-sm bg-success"></i> 전원</span>
                        <span class="flex items-center gap-1.5"><i class="size-3.5 rounded-sm bg-warning/80"></i> 절반</span>
                        <span class="flex items-center gap-1.5"><i class="size-3.5 rounded-sm bg-destructive/70"></i> 소수</span>
                    </div>
                    <div class="my-3.5 h-px bg-border"></div>
                    <div class="flex items-center gap-3">
                        <div class="min-w-0 flex-1">
                            <b class="text-sm">✅ 추천 · 6/25(수) 18시</b>
                            <p class="text-xs text-muted-foreground">가능 인원 11명</p>
                        </div>
                        <c:if test="${role != 'member'}">
                            <t:button size="compact" pageAction="schedule-confirm">이 시간으로 확정</t:button>
                        </c:if>
                    </div>
                </div>
            </section>

            <div class="flex items-center gap-3.5 rounded-lg border bg-card p-5">
                <span class="flex size-10 shrink-0 items-center justify-center rounded-md bg-accent text-accent-foreground text-base">🕒</span>
                <div class="min-w-0 flex-1">
                    <b class="text-sm" data-schedule-my-title>내 가능 시간을 아직 입력하지 않았어요</b>
                    <p class="text-xs text-muted-foreground" data-schedule-my-description>시간 입력하기를 눌러 가능한 칸을 칠해주세요</p>
                </div>
                <t:button size="compact" openModal="timePickModal" cssClass="shrink-0">시간 입력</t:button>
            </div>
        </div>

        <t:card title="응답 현황" moreLabel="4 / 12" flush="true">
            <div class="p-5 pb-3">
                <div class="h-2 overflow-hidden rounded-full bg-secondary"><span class="block h-full w-1/3 rounded-full bg-success"></span></div>
            </div>
            <div class="flex items-center gap-3 border-b px-5 py-3">
                <span class="flex size-7 items-center justify-center rounded-full bg-primary text-xs font-black text-primary-foreground"><c:out value="${userIni}"/></span>
                <p class="min-w-0 flex-1 truncate text-sm font-bold"><c:out value="${userName}"/> <span class="text-xs font-semibold text-muted-foreground">(나)</span></p>
                <span data-schedule-my-badge><t:badge tone="neutral">미응답</t:badge></span>
            </div>
            <div class="flex items-center gap-3 border-b px-5 py-3">
                <span class="flex size-7 items-center justify-center rounded-full bg-info text-xs font-black text-white">JD</span>
                <p class="min-w-0 flex-1 truncate text-sm font-bold">정도윤</p>
                <t:badge tone="success">응답 완료</t:badge>
            </div>
            <div class="flex items-center gap-3 border-b px-5 py-3">
                <span class="flex size-7 items-center justify-center rounded-full bg-sidebar-accent text-xs font-black text-white">PS</span>
                <p class="min-w-0 flex-1 truncate text-sm font-bold">박서연</p>
                <t:badge tone="success">응답 완료</t:badge>
            </div>
            <div class="flex items-center gap-3 border-b px-5 py-3">
                <span class="flex size-7 items-center justify-center rounded-full bg-warning text-xs font-black text-white">CM</span>
                <p class="min-w-0 flex-1 truncate text-sm font-bold">최민준</p>
                <t:badge tone="success">응답 완료</t:badge>
            </div>
            <div class="flex items-center gap-3 px-5 py-3">
                <span class="flex size-7 items-center justify-center rounded-full bg-success text-xs font-black text-white">HJ</span>
                <p class="min-w-0 flex-1 truncate text-sm font-bold">한지우</p>
                <t:badge tone="success">응답 완료</t:badge>
            </div>
        </t:card>
    </div>

    <t:modal id="timePickModal" title="내 가능 시간 입력" description="가능한 칸을 눌러 칠하세요. 드래그하면 여러 칸을 한 번에 선택할 수 있어요.">
        <jsp:attribute name="footer">
            <t:button variant="outline" action="close-modal">취소</t:button>
            <t:button pageAction="schedule-save">가능 시간 저장</t:button>
        </jsp:attribute>
        <jsp:body>
            <div class="mb-3 flex gap-3.5 text-xs font-bold text-muted-foreground">
                <span class="flex items-center gap-1.5"><i class="inline-block size-3.5 rounded-sm border bg-secondary"></i> 불가</span>
                <span class="flex items-center gap-1.5"><i class="inline-block size-3.5 rounded-sm bg-success"></i> 가능</span>
            </div>
            <div class="grid grid-cols-[64px_repeat(5,1fr)] gap-1">
                <div></div>
                <div class="${hcell}">6/23<br><span class="font-semibold">월</span></div>
                <div class="${hcell}">6/24<br><span class="font-semibold">화</span></div>
                <div class="${hcell}">6/25<br><span class="font-semibold">수</span></div>
                <div class="${hcell}">6/26<br><span class="font-semibold">목</span></div>
                <div class="${hcell}">6/27<br><span class="font-semibold">금</span></div>
                <div class="${rcell}">18시</div><button type="button" data-time-cell class="h-8 rounded-md border bg-secondary transition-colors hover:border-primary"></button><button type="button" data-time-cell class="h-8 rounded-md border bg-secondary transition-colors hover:border-primary"></button><button type="button" data-time-cell class="h-8 rounded-md border bg-secondary transition-colors hover:border-primary"></button><button type="button" data-time-cell class="h-8 rounded-md border bg-secondary transition-colors hover:border-primary"></button><button type="button" data-time-cell class="h-8 rounded-md border bg-secondary transition-colors hover:border-primary"></button>
                <div class="${rcell}">19시</div><button type="button" data-time-cell class="h-8 rounded-md border bg-secondary transition-colors hover:border-primary"></button><button type="button" data-time-cell class="h-8 rounded-md border bg-secondary transition-colors hover:border-primary"></button><button type="button" data-time-cell class="h-8 rounded-md border bg-secondary transition-colors hover:border-primary"></button><button type="button" data-time-cell class="h-8 rounded-md border bg-secondary transition-colors hover:border-primary"></button><button type="button" data-time-cell class="h-8 rounded-md border bg-secondary transition-colors hover:border-primary"></button>
                <div class="${rcell}">20시</div><button type="button" data-time-cell class="h-8 rounded-md border bg-secondary transition-colors hover:border-primary"></button><button type="button" data-time-cell class="h-8 rounded-md border bg-secondary transition-colors hover:border-primary"></button><button type="button" data-time-cell class="h-8 rounded-md border bg-secondary transition-colors hover:border-primary"></button><button type="button" data-time-cell class="h-8 rounded-md border bg-secondary transition-colors hover:border-primary"></button><button type="button" data-time-cell class="h-8 rounded-md border bg-secondary transition-colors hover:border-primary"></button>
                <div class="${rcell}">21시</div><button type="button" data-time-cell class="h-8 rounded-md border bg-secondary transition-colors hover:border-primary"></button><button type="button" data-time-cell class="h-8 rounded-md border bg-secondary transition-colors hover:border-primary"></button><button type="button" data-time-cell class="h-8 rounded-md border bg-secondary transition-colors hover:border-primary"></button><button type="button" data-time-cell class="h-8 rounded-md border bg-secondary transition-colors hover:border-primary"></button><button type="button" data-time-cell class="h-8 rounded-md border bg-secondary transition-colors hover:border-primary"></button>
            </div>
            <p class="mt-2.5 text-center text-xs text-muted-foreground">6월 4주차 정기연습 · 후보 5일 × 4시간대</p>
        </jsp:body>
    </t:modal>
</t:layout>
