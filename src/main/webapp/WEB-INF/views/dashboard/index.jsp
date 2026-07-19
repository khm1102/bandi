<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="rq" value="?role=${role}"/>
<c:choose>
    <c:when test="${role == 'member'}"><c:set var="userName" value="김하늘"/></c:when>
    <c:when test="${role == 'leader'}"><c:set var="userName" value="정도윤"/></c:when>
    <c:otherwise><c:set var="userName" value="이서준"/></c:otherwise>
</c:choose>
<t:layout title="홈" active="dashboard" role="${role}">
    <t:pageHead title="안녕하세요, ${userName}님" description="2025년 6월 20일 금요일 · 정기공연 D-1">
        <t:button href="/resources${rq}" variant="outline">미확인 공지 보기</t:button>
        <c:if test="${role != 'member'}">
            <t:button href="/calendar${rq}">+ 일정 등록</t:button>
        </c:if>
    </t:pageHead>

    <div class="mb-4 grid grid-cols-1 gap-2.5 md:grid-cols-2 md:gap-4 lg:grid-cols-4">
        <t:statCard label="오늘 일정" value="4" unit="건" delta="연습 2 · 회의 1 · 홍보 1" icon="calendar" featured="true"/>
        <c:choose>
            <c:when test="${role == 'admin'}">
                <t:statCard label="회비 미납자" value="5" unit="명" delta="미수납 170,000원" tone="danger" icon="wallet" iconTone="danger"/>
            </c:when>
            <c:when test="${role == 'leader'}">
                <t:statCard label="내 회비" value="2/3" delta="납부 항목" tone="danger" icon="wallet"/>
            </c:when>
            <c:otherwise>
                <t:statCard label="내 회비" value="1/3" delta="납부 항목" tone="danger" icon="wallet"/>
            </c:otherwise>
        </c:choose>
        <t:statCard label="대여 반납대기" value="3" unit="건" delta="부원 물품" icon="box" iconTone="warning"/>
        <t:statCard label="관람 신청" value="9" unit="석" delta="2회차 합산" tone="success" icon="ticket" iconTone="info"/>
    </div>

    <div class="mb-4 flex flex-col items-start gap-3 rounded-lg border bg-accent/50 px-4 py-3.5 md:flex-row">
        <span class="flex size-10 shrink-0 items-center justify-center rounded-md bg-primary text-primary-foreground">
            <svg class="size-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M10 21h4"/></svg>
        </span>
        <div class="min-w-0 flex-1">
            <div class="flex items-center gap-2">
                <t:badge tone="accent">공지</t:badge>
                <b class="text-sm">정기공연 최종 리허설 안내</b>
            </div>
            <p class="mt-0.5 text-xs text-muted-foreground">6/20(금) 18시 전원 소집. 의상·소품 지참 바랍니다. · 이서준</p>
        </div>
        <a href="<c:url value='/resources'/>${rq}" class="inline-flex min-h-11 w-full shrink-0 items-center justify-center rounded-md bg-primary px-3 text-xs font-bold text-primary-foreground transition-colors hover:bg-primary-strong hover:text-white md:w-auto">공지 보기</a>
    </div>

    <div class="grid items-start gap-4 lg:grid-cols-[1.7fr_1fr]">
        <div class="flex flex-col gap-4">
            <t:card title="오늘의 일정" icon="clock" moreUrl="/calendar${rq}" moreLabel="캘린더 →" flush="true">
                <div class="flex items-center gap-3 border-b px-5 py-3">
                    <span class="min-w-11 text-sm font-extrabold text-accent-foreground">14:00</span>
                    <div class="min-w-0 flex-1"><p class="text-sm font-bold">2막 전체 런스루</p><p class="mt-0.5 text-xs text-muted-foreground">소극장 무대 · 배우</p></div>
                    <t:badge tone="neutral" dot="true">배우</t:badge>
                </div>
                <div class="flex items-center gap-3 border-b px-5 py-3">
                    <span class="min-w-11 text-sm font-extrabold text-accent-foreground">16:30</span>
                    <div class="min-w-0 flex-1"><p class="text-sm font-bold">조명·음향 큐 점검</p><p class="mt-0.5 text-xs text-muted-foreground">조정실 · 오퍼팀</p></div>
                    <t:badge tone="accent" dot="true">오퍼</t:badge>
                </div>
                <div class="flex items-center gap-3 border-b px-5 py-3">
                    <span class="min-w-11 text-sm font-extrabold text-accent-foreground">18:00</span>
                    <div class="min-w-0 flex-1"><p class="text-sm font-bold">주간 운영 회의</p><p class="mt-0.5 text-xs text-muted-foreground">동아리방 · 운영진</p></div>
                    <t:badge tone="info" dot="true">무대</t:badge>
                </div>
                <div class="flex items-center gap-3 px-5 py-3">
                    <span class="min-w-11 text-sm font-extrabold text-accent-foreground">20:00</span>
                    <div class="min-w-0 flex-1"><p class="text-sm font-bold">SNS 홍보 촬영</p><p class="mt-0.5 text-xs text-muted-foreground">로비 · 영상팀</p></div>
                    <t:badge tone="neutral" dot="true">영상</t:badge>
                </div>
            </t:card>

            <t:card title="팀별 진행 현황" icon="activity" moreUrl="/activity${rq}" moreLabel="활동 기록 →">
                <div class="flex flex-col gap-3.5">
                    <div>
                        <div class="mb-1.5 flex items-center gap-2"><t:badge tone="neutral" dot="true">배우</t:badge><span class="flex-1 truncate text-xs text-muted-foreground">런스루 진행 중</span><b class="text-xs">82%</b></div>
                        <div class="h-2 overflow-hidden rounded-full bg-secondary"><span class="block h-full w-4/5 rounded-full bg-primary"></span></div>
                    </div>
                    <div>
                        <div class="mb-1.5 flex items-center gap-2"><t:badge tone="info" dot="true">무대</t:badge><span class="flex-1 truncate text-xs text-muted-foreground">전환 리허설 예정</span><b class="text-xs">76%</b></div>
                        <div class="h-2 overflow-hidden rounded-full bg-secondary"><span class="block h-full w-3/4 rounded-full bg-primary"></span></div>
                    </div>
                    <div>
                        <div class="mb-1.5 flex items-center gap-2"><t:badge tone="accent" dot="true">오퍼</t:badge><span class="flex-1 truncate text-xs text-muted-foreground">큐 작성 중</span><b class="text-xs">54%</b></div>
                        <div class="h-2 overflow-hidden rounded-full bg-secondary"><span class="block h-full w-1/2 rounded-full bg-primary"></span></div>
                    </div>
                    <div>
                        <div class="mb-1.5 flex items-center gap-2"><t:badge tone="warning" dot="true">디자인</t:badge><span class="flex-1 truncate text-xs text-muted-foreground">팸플릿 인쇄 대기</span><b class="text-xs">88%</b></div>
                        <div class="h-2 overflow-hidden rounded-full bg-secondary"><span class="block h-full w-11/12 rounded-full bg-primary"></span></div>
                    </div>
                    <div>
                        <div class="mb-1.5 flex items-center gap-2"><t:badge tone="neutral" dot="true">영상</t:badge><span class="flex-1 truncate text-xs text-muted-foreground">예고편 편집 중</span><b class="text-xs">40%</b></div>
                        <div class="h-2 overflow-hidden rounded-full bg-secondary"><span class="block h-full w-2/5 rounded-full bg-primary"></span></div>
                    </div>
                </div>
            </t:card>
        </div>

        <div class="flex flex-col gap-4">
            <t:card title="중요·미확인 공지" icon="bell" moreUrl="/resources${rq}" moreLabel="전체 →" flush="true">
                <div class="flex items-center gap-3 border-b px-5 py-3">
                    <span class="flex size-8 shrink-0 items-center justify-center rounded-md bg-accent text-xs font-black text-accent-foreground">중요</span>
                    <div class="min-w-0 flex-1"><p class="truncate text-sm font-bold">정기공연 최종 리허설 안내</p><p class="mt-0.5 text-xs text-muted-foreground">전체 · 미확인 4명</p></div>
                </div>
                <div class="flex items-center gap-3 border-b px-5 py-3">
                    <span class="flex size-8 shrink-0 items-center justify-center rounded-md bg-secondary text-xs font-black text-muted-foreground">팀</span>
                    <div class="min-w-0 flex-1"><p class="truncate text-sm font-bold">2막 전환 리허설 집합 안내</p><p class="mt-0.5 text-xs text-muted-foreground">무대팀 · 내가 미확인</p></div>
                </div>
                <div class="flex items-center gap-3 border-b px-5 py-3">
                    <span class="flex size-8 shrink-0 items-center justify-center rounded-md bg-secondary text-xs font-black text-muted-foreground">전체</span>
                    <div class="min-w-0 flex-1"><p class="truncate text-sm font-bold">공연 당일 출석 처리 방법</p><p class="mt-0.5 text-xs text-muted-foreground">운영진 · 확인 완료</p></div>
                </div>
                <div class="flex items-center gap-3 px-5 py-3">
                    <span class="flex size-8 shrink-0 items-center justify-center rounded-md bg-secondary text-xs font-black text-muted-foreground">팀</span>
                    <div class="min-w-0 flex-1"><p class="truncate text-sm font-bold">최종 큐시트 확인 요청</p><p class="mt-0.5 text-xs text-muted-foreground">오퍼팀 · 미확인 1명</p></div>
                </div>
            </t:card>

            <t:card title="지금 확인할 일">
                <div class="flex flex-col gap-3">
                    <a href="<c:url value='/checklist'/>${rq}" class="flex min-h-11 items-center gap-3 rounded-md px-2 transition-colors hover:bg-secondary">
                        <span class="flex size-8 shrink-0 items-center justify-center rounded-md bg-warning-soft text-sm font-black text-warning">3</span>
                        <span class="min-w-0 flex-1"><b class="block text-sm">공연 전 체크리스트</b><span class="text-xs text-muted-foreground">오늘 완료할 항목</span></span>
                        <span class="text-xs font-bold text-accent-foreground">보기</span>
                    </a>
                    <a href="<c:url value='/props'/>${rq}" class="flex min-h-11 items-center gap-3 rounded-md px-2 transition-colors hover:bg-secondary">
                        <span class="flex size-8 shrink-0 items-center justify-center rounded-md bg-info-soft text-sm font-black text-info">3</span>
                        <span class="min-w-0 flex-1"><b class="block text-sm">반납 대기 물품</b><span class="text-xs text-muted-foreground">공연 후 반납 예정</span></span>
                        <span class="text-xs font-bold text-accent-foreground">보기</span>
                    </a>
                    <c:choose>
                        <c:when test="${role == 'admin'}">
                            <a href="<c:url value='/dues'/>${rq}" class="flex min-h-11 items-center gap-3 rounded-md px-2 transition-colors hover:bg-secondary">
                                <span class="flex size-8 shrink-0 items-center justify-center rounded-md bg-destructive-soft text-sm font-black text-destructive">5</span>
                                <span class="min-w-0 flex-1"><b class="block text-sm">회비 미납 확인</b><span class="text-xs text-muted-foreground">미수납 170,000원</span></span>
                                <span class="text-xs font-bold text-accent-foreground">보기</span>
                            </a>
                        </c:when>
                        <c:otherwise>
                            <a href="<c:url value='/dues'/>${rq}" class="flex min-h-11 items-center gap-3 rounded-md px-2 transition-colors hover:bg-secondary">
                                <span class="flex size-8 shrink-0 items-center justify-center rounded-md bg-destructive-soft text-sm font-black text-destructive">${role == 'leader' ? '1' : '2'}</span>
                                <span class="min-w-0 flex-1"><b class="block text-sm">내 회비 납부 확인</b><span class="text-xs text-muted-foreground">아직 납부하지 않은 항목</span></span>
                                <span class="text-xs font-bold text-accent-foreground">보기</span>
                            </a>
                        </c:otherwise>
                    </c:choose>
                </div>
            </t:card>
        </div>
    </div>
</t:layout>
