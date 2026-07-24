<%@ tag description="관리자 셸 사이드바 — 역할별 메뉴, 활성 상태, 프로필을 관리" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ attribute name="active" %>
<c:set var="navBase" value="flex min-h-11 items-center gap-2.5 whitespace-nowrap rounded-md px-3 py-2 text-sm font-semibold transition-colors"/>
<c:set var="navOff" value=" hover:bg-sidebar-accent hover:text-white"/>
<c:set var="navOn" value=" bg-primary font-extrabold text-primary-foreground"/>
<aside id="mainNavigation"
       class="fixed inset-y-0 left-0 z-40 flex w-72 -translate-x-full flex-col overflow-y-auto overscroll-contain bg-sidebar p-4 text-sidebar-foreground transition-transform duration-200 lg:sticky lg:top-0 lg:h-screen lg:w-56 lg:translate-x-0"
       data-navigation-panel aria-hidden="true" inert>
    <a href="<c:url value='/dashboard'/>" class="flex min-h-14 items-center gap-2.5 border-b border-sidebar-border px-2 pb-4">
        <img src="<c:url value='/images/bandi-icon.png'/>" class="h-8 w-auto rounded-md object-contain" alt="반디">
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
        <a href="<c:url value='/notices'/>" class="${navBase} ${active == 'notices' ? navOn : navOff}" aria-current="${active == 'notices' ? 'page' : 'false'}">
            <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M4 4h16v16H4zM8 8h8M8 12h8M8 16h5"/></svg>공지
        </a>
        <a href="<c:url value='/resources'/>" class="${navBase} ${active == 'resources' ? navOn : navOff}" aria-current="${active == 'resources' ? 'page' : 'false'}">
            <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M3 7a2 2 0 0 1 2-2h4l2 2h8a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg>자료실
        </a>
        <a href="<c:url value='/activity'/>" class="${navBase} ${active == 'activity' ? navOn : navOff}" aria-current="${active == 'activity' ? 'page' : 'false'}">
            <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>내 활동 기록
        </a>
        <sec:authorize access="hasAnyRole('ADMIN', 'LEADER')">
            <a href="<c:url value='/activity/archive'/>" class="${navBase} ${active == 'activity-archive' ? navOn : navOff}" aria-current="${active == 'activity-archive' ? 'page' : 'false'}">
                <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M4 5h16v14H4zM8 3v4M16 3v4M8 12h8M8 16h5"/></svg>승인 기록
            </a>
        </sec:authorize>
        <a href="<c:url value='/props'/>" class="${navBase} ${active == 'props' ? navOn : navOff}" aria-current="${active == 'props' ? 'page' : 'false'}">
            <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M21 8l-9-5-9 5 9 5 9-5zM3 8v8l9 5 9-5V8M12 13v8"/></svg>소품·장비
        </a>
        <sec:authorize access="hasRole('ADMIN')">
            <a href="<c:url value='/members'/>" class="${navBase} ${active == 'members' ? navOn : navOff}" aria-current="${active == 'members' ? 'page' : 'false'}">
                <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/></svg>멤버·권한
            </a>
        </sec:authorize>
        <sec:authorize access="hasAnyRole('ADMIN', 'LEADER')">
            <a href="<c:url value='/activity/review'/>" class="${navBase} ${active == 'activity-review' ? navOn : navOff}" aria-current="${active == 'activity-review' ? 'page' : 'false'}">
                <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M9 11l3 3L22 4M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg>활동 기록 검수
            </a>
        </sec:authorize>
        <sec:authorize access="hasRole('LEADER')">
            <a href="<c:url value='/team-members'/>" class="${navBase} ${active == 'team-members' ? navOn : navOff}" aria-current="${active == 'team-members' ? 'page' : 'false'}">
                <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8M22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/></svg>팀 멤버 관리
            </a>
        </sec:authorize>
    </nav>
    <div class="mt-auto flex flex-col gap-3 border-t border-sidebar-border px-2 pt-3">
        <a href="<c:url value='/profile'/>" class="flex min-w-0 items-center gap-2.5 rounded-md p-1.5 transition-colors hover:bg-sidebar-accent" data-session-profile aria-busy="true">
            <span class="relative flex size-8 shrink-0 items-center justify-center overflow-hidden rounded-full bg-primary text-xs font-black text-primary-foreground">
                <img class="hidden size-full object-cover" data-session-photo alt="">
                <span data-session-initial>·</span>
            </span>
            <span class="min-w-0 flex-1">
                <b class="block truncate text-sm text-white" data-session-name>사용자 정보 불러오는 중</b>
                <span class="block truncate text-xs leading-4 text-sidebar-muted" data-session-meta>잠시만 기다려 주세요</span>
            </span>
        </a>
        <p class="text-center text-xs text-sidebar-muted">개인정보 안내 · 운영 문의</p>
        <form action="<c:url value='/logout'/>" method="post">
            <input type="hidden" name="<c:out value='${_csrf.parameterName}'/>" value="<c:out value='${_csrf.token}'/>">
            <button type="submit" class="inline-flex min-h-11 w-full items-center justify-center whitespace-nowrap rounded-md border border-sidebar-border px-3 text-xs font-extrabold text-sidebar-muted transition-colors hover:bg-sidebar-accent hover:text-white">로그아웃</button>
        </form>
    </div>
</aside>
