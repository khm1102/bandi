<%@ tag description="관리자 셸 — 네이비 사이드바 + 탑바. 로그인 후 모든 관리 화면이 이 태그로 감싼다" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="active" %>
<%@ attribute name="crumb" %>
<%@ attribute name="css" fragment="true" %>
<%@ attribute name="script" fragment="true" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<t:head title="${title}"/>
<jsp:invoke fragment="css"/>
</head>
<body>
<div class="flex min-h-screen flex-col lg:flex-row">
    <aside class="flex shrink-0 flex-row items-center gap-1 overflow-x-auto bg-sidebar p-2 text-sidebar-foreground lg:sticky lg:top-0 lg:h-screen lg:w-56 lg:flex-col lg:items-stretch lg:overflow-y-auto lg:p-4">
        <a href="<c:url value='/'/>" class="hidden items-center gap-2.5 border-b border-sidebar-border px-2 pb-4 lg:flex">
            <span class="flex size-8 items-center justify-center rounded-md bg-primary text-base">🎭</span>
            <span>
                <b class="block text-sm font-black text-white">bandi</b>
                <span class="block text-xs font-semibold text-sidebar-muted">연극 동아리 관리</span>
            </span>
        </a>
        <nav class="flex flex-row gap-1 lg:mt-3 lg:flex-col" aria-label="주 메뉴">
            <%-- feature 확정 시 항목 추가 — active 값과 비교해 활성 표시 --%>
            <a href="<c:url value='/'/>"
               class="flex items-center gap-2.5 whitespace-nowrap rounded-md px-3 py-2 text-sm font-semibold transition-colors ${active == 'dashboard' ? 'bg-primary font-extrabold text-primary-foreground' : 'hover:bg-sidebar-accent hover:text-white'}">
                대시보드
            </a>
        </nav>
        <div class="mt-auto hidden items-center gap-2.5 border-t border-sidebar-border px-2 pt-3 lg:flex">
            <span class="flex size-8 items-center justify-center rounded-full bg-sidebar-accent text-xs font-black text-white">B</span>
            <span class="min-w-0">
                <b class="block truncate text-sm text-white">bandi</b>
                <span class="block text-xs text-sidebar-muted">로그인 전</span>
            </span>
        </div>
    </aside>

    <div class="min-w-0 flex-1">
        <header class="sticky top-0 z-20 flex items-center gap-3 border-b bg-card/90 px-7 py-3 backdrop-blur">
            <p class="text-xs font-semibold text-muted-foreground">bandi · <b class="font-extrabold text-foreground"><c:out value="${empty crumb ? title : crumb}"/></b></p>
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
