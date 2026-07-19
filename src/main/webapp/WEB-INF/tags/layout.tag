<%@ tag description="관리자 셸 조립 — 모바일 헤더, 사이드바, 탑바, 본문, 공통 모달·스크립트 로딩" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="active" %>
<%@ attribute name="crumb" %>
<%@ attribute name="role" %>
<%@ attribute name="scriptPath" %>
<%@ attribute name="css" fragment="true" %>
<%@ attribute name="script" fragment="true" %>
<c:set var="navRole" value="${empty role ? 'admin' : role}"/>
<!DOCTYPE html>
<html lang="ko">
<head>
<t:head title="${title}"/>
<jsp:invoke fragment="css"/>
</head>
<body data-user-role="${navRole}">
<a href="#mainContent" class="fixed left-4 top-4 z-50 inline-flex min-h-11 -translate-y-24 items-center rounded-md bg-primary px-4 text-sm font-bold text-primary-foreground transition-transform focus:translate-y-0">본문으로 바로가기</a>
<div class="min-h-screen lg:flex">
    <header class="sticky top-0 z-30 flex h-14 items-center gap-2.5 border-b bg-card px-4 lg:hidden">
        <a href="<c:url value='/dashboard'/>" class="flex min-h-11 shrink-0 items-center font-extrabold" aria-label="반디 홈">
            <span class="flex size-8 items-center justify-center rounded-md bg-primary text-sm font-extrabold text-primary-foreground">B</span>
        </a>
        <p class="min-w-0 flex-1 truncate text-sm font-bold"><c:out value="${title}"/></p>
        <button type="button" class="ml-auto flex size-11 shrink-0 items-center justify-center rounded-md border bg-card text-foreground"
                data-navigation-toggle aria-controls="mainNavigation" aria-expanded="false" aria-label="전체 메뉴 열기">
            <svg class="size-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" aria-hidden="true"><path d="M4 7h16M4 12h16M4 17h16"/></svg>
        </button>
    </header>
    <button type="button" class="fixed inset-0 z-30 hidden bg-sidebar/60 lg:hidden" data-navigation-backdrop aria-label="메뉴 닫기"></button>
    <t:sidebar active="${active}" role="${navRole}"/>

    <div class="min-w-0 flex-1">
        <div data-session-expired hidden tabindex="-1" class="border-b border-warning/30 bg-warning/10 px-4 py-3 md:px-6 lg:px-7" role="alert">
            <div class="mx-auto flex max-w-screen-2xl flex-col gap-2 sm:flex-row sm:items-center">
                <p class="min-w-0 flex-1 text-sm"><b>로그인이 만료됐어요.</b> 작성 중인 내용은 이 화면에 남아 있어요. 다시 로그인한 뒤 저장을 다시 시도해 주세요.</p>
                <a href="<c:url value='/login'/>" class="inline-flex min-h-11 shrink-0 items-center justify-center rounded-md border border-warning/40 bg-card px-4 text-sm font-bold">다시 로그인</a>
            </div>
        </div>
        <header class="sticky top-14 z-20 hidden min-h-12 items-center gap-3 border-b bg-card/95 px-4 py-2.5 backdrop-blur md:flex md:px-6 lg:top-0 lg:px-7">
            <p class="hidden text-xs font-semibold text-muted-foreground md:block">반디 / <b class="font-extrabold text-foreground"><c:out value="${empty crumb ? title : crumb}"/></b></p>
        </header>
        <main id="mainContent" class="mx-auto w-full max-w-screen-2xl p-4 md:p-6 lg:p-7" tabindex="-1">
            <jsp:doBody/>
        </main>
    </div>
</div>

<c:if test="${not empty toast}">
    <div data-flash-toast="<c:out value='${toast}'/>" hidden></div>
</c:if>

<%-- 공용 확인 모달 — data-confirm 버튼이 js/common/confirm.js를 통해 사용 --%>
<t:modal id="confirmModal" title="작업 확인">
    <jsp:attribute name="footer">
        <t:button variant="outline" action="close-modal">취소</t:button>
        <t:button variant="danger" action="accept-confirm">계속</t:button>
    </jsp:attribute>
    <jsp:body>
        <p class="text-sm text-muted-foreground" data-confirm-message></p>
    </jsp:body>
</t:modal>

<script type="module" src="<c:url value='/js/common/toast.js'/>"></script>
<script type="module" src="<c:url value='/js/common/modal.js'/>"></script>
<script type="module" src="<c:url value='/js/common/sheet.js'/>"></script>
<script type="module" src="<c:url value='/js/common/confirm.js'/>"></script>
<script type="module" src="<c:url value='/js/common/form-guard.js'/>"></script>
<script type="module" src="<c:url value='/js/common/navigation.js'/>"></script>
<script type="module" src="<c:url value='/js/common/shell.js'/>"></script>
<c:if test="${not empty scriptPath}">
    <script type="module" src="<c:url value='/js/${scriptPath}.js'/>"></script>
</c:if>
<jsp:invoke fragment="script"/>
</body>
</html>
