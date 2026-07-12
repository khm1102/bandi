<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="rq" value="?role=${role}"/>
<c:set var="btnPrimary" value="inline-flex h-9 items-center justify-center gap-1.5 rounded-md bg-primary px-4 text-sm font-bold text-primary-foreground transition-colors hover:bg-primary-strong hover:text-white"/>
<c:set var="btnOutline" value="inline-flex h-9 items-center justify-center gap-1.5 rounded-md border bg-card px-4 text-sm font-bold transition-colors hover:bg-secondary"/>
<c:choose>
    <c:when test="${role == 'member'}"><c:set var="userName" value="김하늘"/></c:when>
    <c:when test="${role == 'leader'}"><c:set var="userName" value="정도윤"/></c:when>
    <c:otherwise><c:set var="userName" value="이서준"/></c:otherwise>
</c:choose>
<t:layout title="홈" active="dashboard" role="${role}">
    <t:pageHead title="안녕하세요, ${userName}님" description="2025년 6월 20일 금요일 · 정기공연 D-1">
        <a href="<c:url value='/community'/>${rq}" class="${btnOutline}">공지 보기</a>
        <c:if test="${role != 'member'}">
            <a href="<c:url value='/calendar'/>${rq}" class="${btnPrimary}">+ 일정 등록</a>
        </c:if>
    </t:pageHead>

    <div class="mb-4 grid grid-cols-2 gap-4 lg:grid-cols-4">
        <t:statCard label="오늘 일정" value="4" unit="건" delta="연습 2 · 회의 1 · 홍보 1"/>
        <c:choose>
            <c:when test="${role == 'admin'}">
                <t:statCard label="회비 미납자" value="5" unit="명" delta="미수납 170,000원" tone="danger"/>
            </c:when>
            <c:when test="${role == 'leader'}">
                <t:statCard label="내 회비" value="2/3" delta="납부 항목" tone="danger"/>
            </c:when>
            <c:otherwise>
                <t:statCard label="내 회비" value="1/3" delta="납부 항목" tone="danger"/>
            </c:otherwise>
        </c:choose>
        <t:statCard label="대여 반납대기" value="3" unit="건" delta="부원 물품"/>
        <t:statCard label="관람 신청" value="9" unit="석" delta="▲ 2회차 합산" tone="success"/>
    </div>

    <div class="mb-4 flex items-start gap-3 rounded-lg border border-l-4 border-l-primary bg-accent/50 px-4 py-3.5">
        <div class="min-w-0 flex-1">
            <div class="flex items-center gap-2">
                <t:badge tone="accent">📌 공지</t:badge>
                <b class="text-sm">정기공연 최종 리허설 안내</b>
            </div>
            <p class="mt-0.5 text-xs text-muted-foreground">6/20(금) 18시 전원 소집. 의상·소품 지참 바랍니다. · 이서준</p>
        </div>
        <a href="<c:url value='/community'/>${rq}" class="inline-flex h-8 shrink-0 items-center rounded-md bg-primary px-3 text-xs font-bold text-primary-foreground transition-colors hover:bg-primary-strong hover:text-white">확인</a>
    </div>

    <div class="grid items-start gap-4 lg:grid-cols-[1.7fr_1fr]">
        <div class="flex flex-col gap-4">
            <t:card title="오늘의 일정" moreUrl="/calendar${rq}" moreLabel="캘린더 →" flush="true">
                <div class="flex items-center gap-3 border-b px-5 py-3">
                    <span class="min-w-11 text-sm font-extrabold text-accent-foreground">14:00</span>
                    <div class="min-w-0 flex-1"><p class="text-sm font-bold">2막 전체 런스루</p><p class="mt-0.5 text-xs text-muted-foreground">소극장 무대 · 배우연출팀</p></div>
                    <t:badge tone="neutral" dot="true">배우연출</t:badge>
                </div>
                <div class="flex items-center gap-3 border-b px-5 py-3">
                    <span class="min-w-11 text-sm font-extrabold text-accent-foreground">16:30</span>
                    <div class="min-w-0 flex-1"><p class="text-sm font-bold">조명·음향 큐 점검</p><p class="mt-0.5 text-xs text-muted-foreground">조정실 · 오퍼팀</p></div>
                    <t:badge tone="neutral" dot="true">오퍼</t:badge>
                </div>
                <div class="flex items-center gap-3 border-b px-5 py-3">
                    <span class="min-w-11 text-sm font-extrabold text-accent-foreground">18:00</span>
                    <div class="min-w-0 flex-1"><p class="text-sm font-bold">주간 운영 회의</p><p class="mt-0.5 text-xs text-muted-foreground">동아리방 · 운영진</p></div>
                    <t:badge tone="neutral" dot="true">무대</t:badge>
                </div>
                <div class="flex items-center gap-3 px-5 py-3">
                    <span class="min-w-11 text-sm font-extrabold text-accent-foreground">20:00</span>
                    <div class="min-w-0 flex-1"><p class="text-sm font-bold">SNS 홍보 촬영</p><p class="mt-0.5 text-xs text-muted-foreground">로비 · 영상팀</p></div>
                    <t:badge tone="neutral" dot="true">영상</t:badge>
                </div>
            </t:card>

            <t:card title="팀별 진행 현황" moreUrl="/activity${rq}" moreLabel="활동 기록 →">
                <div class="flex flex-col gap-3.5">
                    <div>
                        <div class="mb-1.5 flex items-center gap-2"><t:badge tone="neutral" dot="true">배우연출</t:badge><span class="flex-1 truncate text-xs text-muted-foreground">런스루 진행 중</span><b class="text-xs">82%</b></div>
                        <div class="h-2 overflow-hidden rounded-full bg-secondary"><span class="block h-full rounded-full bg-primary" style="width:82%"></span></div>
                    </div>
                    <div>
                        <div class="mb-1.5 flex items-center gap-2"><t:badge tone="neutral" dot="true">무대</t:badge><span class="flex-1 truncate text-xs text-muted-foreground">전환 리허설 예정</span><b class="text-xs">76%</b></div>
                        <div class="h-2 overflow-hidden rounded-full bg-secondary"><span class="block h-full rounded-full bg-primary" style="width:76%"></span></div>
                    </div>
                    <div>
                        <div class="mb-1.5 flex items-center gap-2"><t:badge tone="neutral" dot="true">오퍼</t:badge><span class="flex-1 truncate text-xs text-muted-foreground">큐 작성 중</span><b class="text-xs">54%</b></div>
                        <div class="h-2 overflow-hidden rounded-full bg-secondary"><span class="block h-full rounded-full bg-primary" style="width:54%"></span></div>
                    </div>
                    <div>
                        <div class="mb-1.5 flex items-center gap-2"><t:badge tone="neutral" dot="true">디자인</t:badge><span class="flex-1 truncate text-xs text-muted-foreground">팸플릿 인쇄 대기</span><b class="text-xs">88%</b></div>
                        <div class="h-2 overflow-hidden rounded-full bg-secondary"><span class="block h-full rounded-full bg-primary" style="width:88%"></span></div>
                    </div>
                    <div>
                        <div class="mb-1.5 flex items-center gap-2"><t:badge tone="neutral" dot="true">영상</t:badge><span class="flex-1 truncate text-xs text-muted-foreground">예고편 편집 중</span><b class="text-xs">40%</b></div>
                        <div class="h-2 overflow-hidden rounded-full bg-secondary"><span class="block h-full rounded-full bg-primary" style="width:40%"></span></div>
                    </div>
                </div>
            </t:card>
        </div>

        <div class="flex flex-col gap-4">
            <t:card title="최근 게시판" moreUrl="/community${rq}" moreLabel="전체 →" flush="true">
                <div class="flex items-center gap-3 border-b px-5 py-3">
                    <span class="flex size-8 shrink-0 items-center justify-center rounded-md bg-accent text-xs font-black text-accent-foreground">공지</span>
                    <div class="min-w-0 flex-1"><p class="truncate text-sm font-bold">정기공연 최종 리허설 안내</p><p class="mt-0.5 text-xs text-muted-foreground">이서준 · 2시간 전</p></div>
                </div>
                <div class="flex items-center gap-3 border-b px-5 py-3">
                    <span class="flex size-8 shrink-0 items-center justify-center rounded-md bg-secondary text-xs font-black text-muted-foreground">자유</span>
                    <div class="min-w-0 flex-1"><p class="truncate text-sm font-bold">2막 전환 타이밍 관련 아이디어</p><p class="mt-0.5 text-xs text-muted-foreground">박서연 · 5시간 전</p></div>
                </div>
                <div class="flex items-center gap-3 border-b px-5 py-3">
                    <span class="flex size-8 shrink-0 items-center justify-center rounded-md bg-secondary text-xs font-black text-muted-foreground">질문</span>
                    <div class="min-w-0 flex-1"><p class="truncate text-sm font-bold">MT 회비 언제까지 내면 되나요?</p><p class="mt-0.5 text-xs text-muted-foreground">김하늘 · 어제</p></div>
                </div>
                <div class="flex items-center gap-3 px-5 py-3">
                    <span class="flex size-8 shrink-0 items-center justify-center rounded-md bg-secondary text-xs font-black text-muted-foreground">자유</span>
                    <div class="min-w-0 flex-1"><p class="truncate text-sm font-bold">포스터 시안 투표해주세요 🎨</p><p class="mt-0.5 text-xs text-muted-foreground">한지우 · 2일 전</p></div>
                </div>
            </t:card>

            <t:card title="빠른 이동">
                <div class="grid grid-cols-2 gap-2.5">
                    <a href="<c:url value='/schedule'/>${rq}" class="${btnOutline} h-12 justify-start">일정 조율</a>
                    <a href="<c:url value='/resources'/>${rq}" class="${btnOutline} h-12 justify-start">자료실</a>
                    <a href="<c:url value='/props'/>${rq}" class="${btnOutline} h-12 justify-start">소품</a>
                    <a href="<c:url value='/checklist'/>${rq}" class="${btnOutline} h-12 justify-start">체크리스트</a>
                </div>
            </t:card>
        </div>
    </div>
</t:layout>
