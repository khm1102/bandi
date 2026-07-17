<%@ tag description="관리자 셸 — 네이비 사이드바 + 탑바. 로그인 후 모든 관리 화면이 이 태그로 감싼다. role 미지정 시 admin 기준 전체 메뉴 노출" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="active" %>
<%@ attribute name="crumb" %>
<%@ attribute name="role" %>
<%@ attribute name="css" fragment="true" %>
<%@ attribute name="script" fragment="true" %>
<c:set var="navRole" value="${empty role ? 'admin' : role}"/>
<c:choose>
    <c:when test="${empty role}"><c:set var="roleQuery" value=""/></c:when>
    <c:otherwise><c:set var="roleQuery" value="?role=${role}"/></c:otherwise>
</c:choose>
<c:set var="navBase" value="flex items-center gap-2.5 whitespace-nowrap rounded-md px-3 py-2 text-sm font-semibold transition-colors"/>
<c:set var="navOff" value="hover:bg-sidebar-accent hover:text-white"/>
<c:set var="navOn" value="bg-primary font-extrabold text-primary-foreground"/>
<!DOCTYPE html>
<html lang="ko">
<head>
<t:head title="${title}"/>
<jsp:invoke fragment="css"/>
</head>
<body>
<div class="flex min-h-screen flex-col lg:flex-row">
    <aside class="flex shrink-0 flex-row items-center gap-1 overflow-x-auto bg-sidebar p-2 text-sidebar-foreground lg:sticky lg:top-0 lg:h-screen lg:w-56 lg:flex-col lg:items-stretch lg:overflow-y-auto lg:p-4">
        <a href="<c:url value='/dashboard'/>${roleQuery}" class="hidden items-center gap-2.5 border-b border-sidebar-border px-2 pb-4 lg:flex">
            <span class="flex size-8 items-center justify-center rounded-md bg-primary text-base">🎭</span>
            <span>
                <b class="block text-sm font-black text-white">bandi</b>
                <span class="block text-xs font-semibold text-sidebar-muted">연극 동아리 통합관리</span>
            </span>
        </a>
        <nav class="flex flex-row gap-1 lg:mt-3 lg:flex-col" aria-label="주 메뉴">
            <p class="hidden px-2 pb-1 pt-2 text-xs font-extrabold tracking-widest text-sidebar-muted lg:block">메뉴</p>
            <a href="<c:url value='/dashboard'/>${roleQuery}" class="${navBase} ${active == 'dashboard' ? navOn : navOff}">
                <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M3 11l9-8 9 8M5 10v10h5v-6h4v6h5V10"/></svg>홈
            </a>
            <a href="<c:url value='/calendar'/>${roleQuery}" class="${navBase} ${active == 'calendar' ? navOn : navOff}">
                <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M4 5h16v16H4zM4 9h16M8 3v4M16 3v4"/></svg>통합 캘린더
            </a>
            <a href="<c:url value='/schedule'/>${roleQuery}" class="${navBase} ${active == 'schedule' ? navOn : navOff}">
                <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M4 20V9M10 20V4M16 20v-7M22 20H2"/></svg>일정 조율
            </a>
            <a href="<c:url value='/resources'/>${roleQuery}" class="${navBase} ${active == 'resources' ? navOn : navOff}">
                <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M3 7a2 2 0 0 1 2-2h4l2 2h8a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg>자료실
            </a>
            <a href="<c:url value='/activity'/>${roleQuery}" class="${navBase} ${active == 'activity' ? navOn : navOff}">
                <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>활동 기록
            </a>
            <a href="<c:url value='/community'/>${roleQuery}" class="${navBase} ${active == 'community' ? navOn : navOff}">
                <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M4 4h16v14H4zM4 9h16M9 18v3M15 18v3"/></svg>게시판
                <span class="ml-auto rounded-full bg-sidebar-accent px-2 py-0.5 text-xs font-extrabold ${active == 'community' ? 'bg-primary-strong/40 text-primary-foreground' : 'text-sidebar-foreground'}">1</span>
            </a>
            <a href="<c:url value='/props'/>${roleQuery}" class="${navBase} ${active == 'props' ? navOn : navOff}">
                <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M21 8l-9-5-9 5 9 5 9-5zM3 8v8l9 5 9-5V8M12 13v8"/></svg>소품
            </a>
            <c:if test="${navRole == 'admin'}">
                <a href="<c:url value='/reservations'/>${roleQuery}" class="${navBase} ${active == 'reservations' ? navOn : navOff}">
                    <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M3 7a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v3a2 2 0 0 0 0 4v3a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-3a2 2 0 0 0 0-4z"/></svg>신청 관리
                    <span class="ml-auto rounded-full bg-sidebar-accent px-2 py-0.5 text-xs font-extrabold ${active == 'reservations' ? 'bg-primary-strong/40 text-primary-foreground' : 'text-sidebar-foreground'}">2</span>
                </a>
                <a href="<c:url value='/showops'/>${roleQuery}" class="${navBase} ${active == 'showops' ? navOn : navOff}">
                    <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M4 4h16v13H4zM8 21h8M12 17v4M9 8l4 2.5L9 13z"/></svg>공연 운영
                </a>
            </c:if>
            <a href="<c:url value='/checklist'/>${roleQuery}" class="${navBase} ${active == 'checklist' ? navOn : navOff}">
                <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M20 6L9 17l-5-5"/></svg>체크리스트
            </a>
            <a href="<c:url value='/attendance'/>${roleQuery}" class="${navBase} ${active == 'attendance' ? navOn : navOff}">
                <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8M16 11l2 2 4-4"/></svg>출석
            </a>
            <a href="<c:url value='/dues'/>${roleQuery}" class="${navBase} ${active == 'dues' ? navOn : navOff}">
                <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M3 7h16a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2zM3 7V6a2 2 0 0 1 2-2h11M17 13h.01"/></svg>회비
                <c:if test="${navRole == 'admin'}">
                    <span class="ml-auto rounded-full bg-sidebar-accent px-2 py-0.5 text-xs font-extrabold ${active == 'dues' ? 'bg-primary-strong/40 text-primary-foreground' : 'text-sidebar-foreground'}">5</span>
                </c:if>
            </a>
            <c:if test="${navRole != 'member'}">
                <a href="<c:url value='/members'/>${roleQuery}" class="${navBase} ${active == 'members' ? navOn : navOff}">
                    <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/></svg>멤버·권한
                </a>
            </c:if>
        </nav>
        <div class="mt-auto hidden items-center gap-2.5 border-t border-sidebar-border px-2 pt-3 lg:flex">
            <c:choose>
                <c:when test="${navRole == 'member'}">
                    <span class="flex size-8 shrink-0 items-center justify-center rounded-full bg-sidebar-accent text-xs font-black text-white">KH</span>
                    <span class="min-w-0">
                        <b class="block truncate text-sm text-white">김하늘</b>
                        <span class="block truncate text-xs text-sidebar-muted">일반 부원 · 배우연출팀</span>
                    </span>
                </c:when>
                <c:when test="${navRole == 'leader'}">
                    <span class="flex size-8 shrink-0 items-center justify-center rounded-full bg-info text-xs font-black text-white">JD</span>
                    <span class="min-w-0">
                        <b class="block truncate text-sm text-white">정도윤</b>
                        <span class="block truncate text-xs text-sidebar-muted">무대팀장</span>
                    </span>
                </c:when>
                <c:otherwise>
                    <span class="flex size-8 shrink-0 items-center justify-center rounded-full bg-primary text-xs font-black text-primary-foreground">LS</span>
                    <span class="min-w-0">
                        <b class="block truncate text-sm text-white">이서준</b>
                        <span class="block truncate text-xs text-sidebar-muted">운영진 · 회장</span>
                    </span>
                </c:otherwise>
            </c:choose>
            <a href="<c:url value='/login'/>" class="ml-auto rounded-md border border-sidebar-border px-2.5 py-1.5 text-xs font-extrabold text-sidebar-muted transition-colors hover:bg-sidebar-accent hover:text-white">로그아웃</a>
        </div>
    </aside>

    <div class="min-w-0 flex-1">
        <header class="sticky top-0 z-20 flex items-center gap-3 border-b bg-card/90 px-7 py-3 backdrop-blur">
            <p class="text-xs font-semibold text-muted-foreground">bandi · <b class="font-extrabold text-foreground"><c:out value="${empty crumb ? title : crumb}"/></b></p>
            <%-- 퍼블리싱 미리보기용 역할 전환 — 해당 화면이 허용하는 역할만 노출한다(허용 역할 정본: PublishingController.PAGE_ROLES) --%>
            <c:if test="${not empty role and not empty active}">
                <div class="ml-auto inline-flex rounded-lg border bg-secondary p-0.5">
                    <c:if test="${empty allowedRoles or allowedRoles.contains('member')}">
                        <a href="<c:url value='/${active}'/>?role=member" class="rounded-md px-3 py-1.5 text-xs font-bold transition-colors ${role == 'member' ? 'border bg-card text-foreground' : 'text-muted-foreground'}">일반 부원</a>
                    </c:if>
                    <c:if test="${empty allowedRoles or allowedRoles.contains('leader')}">
                        <a href="<c:url value='/${active}'/>?role=leader" class="rounded-md px-3 py-1.5 text-xs font-bold transition-colors ${role == 'leader' ? 'border bg-card text-foreground' : 'text-muted-foreground'}">팀장</a>
                    </c:if>
                    <c:if test="${empty allowedRoles or allowedRoles.contains('admin')}">
                        <a href="<c:url value='/${active}'/>?role=admin" class="rounded-md px-3 py-1.5 text-xs font-bold transition-colors ${role == 'admin' ? 'border bg-card text-foreground' : 'text-muted-foreground'}">운영진</a>
                    </c:if>
                </div>
            </c:if>
        </header>
        <main class="mx-auto max-w-6xl p-7">
            <jsp:doBody/>
        </main>
    </div>
</div>

<c:if test="${not empty toast}">
    <div data-flash-toast="<c:out value='${toast}'/>" hidden></div>
</c:if>

<%-- 공용 확인 모달 — data-confirm 버튼이 js/common/confirm.js를 통해 사용 --%>
<t:modal id="confirmModal" title="확인">
    <jsp:attribute name="footer">
        <button type="button" data-action="close-modal" class="inline-flex h-9 items-center justify-center rounded-md border bg-card px-4 text-sm font-bold transition-colors hover:bg-secondary">취소</button>
        <button type="button" data-action="accept-confirm" class="inline-flex h-9 items-center justify-center rounded-md bg-destructive px-4 text-sm font-bold text-destructive-foreground transition-colors hover:bg-destructive/90">확인</button>
    </jsp:attribute>
    <jsp:body>
        <p class="text-sm text-muted-foreground" data-confirm-message></p>
    </jsp:body>
</t:modal>

<script type="module" src="<c:url value='/js/common/toast.js'/>"></script>
<script type="module" src="<c:url value='/js/common/confirm.js'/>"></script>
<script type="module" src="<c:url value='/js/common/form-guard.js'/>"></script>
<jsp:invoke fragment="script"/>
</body>
</html>
