<%@ tag description="관리자 셸 사이드바 — 역할별 메뉴, 활성 상태, 프로필을 관리" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="active" %>
<%@ attribute name="role" required="true" %>
<c:set var="navBase" value="flex min-h-11 items-center gap-2.5 whitespace-nowrap rounded-md px-3 py-2 text-sm font-semibold transition-colors"/>
<c:set var="navOff" value=" hover:bg-sidebar-accent hover:text-white"/>
<c:set var="navOn" value=" bg-primary font-extrabold text-primary-foreground"/>
<aside id="mainNavigation"
       class="fixed inset-y-0 left-0 z-40 flex w-72 -translate-x-full flex-col overflow-y-auto overscroll-contain bg-sidebar p-4 text-sidebar-foreground transition-transform duration-200 lg:sticky lg:top-0 lg:h-screen lg:w-56 lg:translate-x-0"
       data-navigation-panel aria-hidden="true" inert>
    <a href="<c:url value='/dashboard'/>" class="flex min-h-14 items-center gap-2.5 border-b border-sidebar-border px-2 pb-4">
        <span class="flex size-8 items-center justify-center rounded-md bg-primary text-sm font-black text-primary-foreground">B</span>
        <span>
            <b class="block text-sm font-black text-white">반디</b>
            <span class="block text-xs font-semibold text-sidebar-muted">연극 동아리</span>
        </span>
    </a>
    <nav class="mt-3 flex flex-col gap-1" aria-label="주 메뉴">
        <p class="px-2 pb-1 pt-2 text-xs font-extrabold text-sidebar-muted">메뉴</p>
        <a href="<c:url value='/dashboard'/>" class="${navBase} ${active == 'dashboard' ? navOn : navOff}" aria-current="${active == 'dashboard' ? 'page' : 'false'}">
            <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M3 11l9-8 9 8M5 10v10h5v-6h4v6h5V10"/></svg>홈
        </a>
        <a href="<c:url value='/calendar'/>" class="${navBase} ${active == 'calendar' ? navOn : navOff}" aria-current="${active == 'calendar' ? 'page' : 'false'}">
            <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M4 5h16v16H4zM4 9h16M8 3v4M16 3v4"/></svg>통합 캘린더
        </a>
        <a href="<c:url value='/resources'/>" class="${navBase} ${active == 'resources' ? navOn : navOff}" aria-current="${active == 'resources' ? 'page' : 'false'}">
            <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M3 7a2 2 0 0 1 2-2h4l2 2h8a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg>공지·자료실
        </a>
        <a href="<c:url value='/activity'/>" class="${navBase} ${active == 'activity' ? navOn : navOff}" aria-current="${active == 'activity' ? 'page' : 'false'}">
            <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>활동 기록
        </a>
        <a href="<c:url value='/production'/>" class="${navBase} ${active == 'production' ? navOn : navOff}" aria-current="${active == 'production' ? 'page' : 'false'}">
            <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M4 19V5M4 19h16M8 15l3-3 3 2 5-6"/></svg>팀별 제작 진행
        </a>
        <a href="<c:url value='/props'/>" class="${navBase} ${active == 'props' ? navOn : navOff}" aria-current="${active == 'props' ? 'page' : 'false'}">
            <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M21 8l-9-5-9 5 9 5 9-5zM3 8v8l9 5 9-5V8M12 13v8"/></svg>소품·장비
        </a>
        <c:if test="${role == 'admin'}">
            <a href="<c:url value='/reservations'/>" class="${navBase} ${active == 'reservations' ? navOn : navOff}" aria-current="${active == 'reservations' ? 'page' : 'false'}">
                <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M3 7a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v3a2 2 0 0 0 0 4v3a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-3a2 2 0 0 0 0-4z"/></svg>관람 신청 관리
            </a>
            <a href="<c:url value='/showops'/>" class="${navBase} ${active == 'showops' ? navOn : navOff}" aria-current="${active == 'showops' ? 'page' : 'false'}">
                <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M4 4h16v13H4zM8 21h8M12 17v4M9 8l4 2.5L9 13z"/></svg>공연 당일 입장
            </a>
        </c:if>
        <a href="<c:url value='/checklist'/>" class="${navBase} ${active == 'checklist' ? navOn : navOff}" aria-current="${active == 'checklist' ? 'page' : 'false'}">
            <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M20 6L9 17l-5-5"/></svg>체크리스트
        </a>
        <a href="<c:url value='/attendance'/>" class="${navBase} ${active == 'attendance' ? navOn : navOff}" aria-current="${active == 'attendance' ? 'page' : 'false'}">
            <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8M16 11l2 2 4-4"/></svg>행사·출석
        </a>
        <a href="<c:url value='/dues'/>" class="${navBase} ${active == 'dues' ? navOn : navOff}" aria-current="${active == 'dues' ? 'page' : 'false'}">
            <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M3 7h16a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2zM3 7V6a2 2 0 0 1 2-2h11M17 13h.01"/></svg>회비
        </a>
        <c:if test="${role == 'admin'}">
            <a href="<c:url value='/members'/>" class="${navBase} ${active == 'members' ? navOn : navOff}" aria-current="${active == 'members' ? 'page' : 'false'}">
                <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/></svg>멤버·권한
            </a>
        </c:if>
    </nav>
    <div class="mt-auto flex flex-col gap-3 border-t border-sidebar-border px-2 pt-3">
        <div class="flex min-w-0 items-center gap-2.5" data-session-profile aria-busy="true">
            <span class="flex size-8 shrink-0 items-center justify-center rounded-full bg-primary text-xs font-black text-primary-foreground" data-session-initial>·</span>
            <span class="min-w-0 flex-1">
                <b class="block truncate text-sm text-white" data-session-name>사용자 정보 불러오는 중</b>
                <span class="block truncate text-xs leading-4 text-sidebar-muted" data-session-meta>잠시만 기다려 주세요</span>
            </span>
        </div>
        <a href="<c:url value='/notices'/>" class="inline-flex min-h-11 items-center justify-center rounded-md px-3 text-xs font-bold text-sidebar-muted transition-colors hover:bg-sidebar-accent hover:text-white">공시·운영 안내</a>
        <p class="text-center text-xs text-sidebar-muted">개인정보 안내 · 운영 문의</p>
        <form action="<c:url value='/logout'/>" method="post">
            <input type="hidden" name="<c:out value='${_csrf.parameterName}'/>" value="<c:out value='${_csrf.token}'/>">
            <button type="submit" class="inline-flex min-h-11 w-full items-center justify-center whitespace-nowrap rounded-md border border-sidebar-border px-3 text-xs font-extrabold text-sidebar-muted transition-colors hover:bg-sidebar-accent hover:text-white">로그아웃</button>
        </form>
    </div>
</aside>
