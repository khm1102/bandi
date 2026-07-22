<%@ tag description="공개 셸 — 네이비 상단 내비와 푸터를 사용하는 비로그인 화면용" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="active" %>
<%@ attribute name="scriptPath" %>
<%@ attribute name="css" fragment="true" %>
<%@ attribute name="script" fragment="true" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<t:head title="${title}"/>
<jsp:invoke fragment="css"/>
</head>
<body class="flex min-h-screen flex-col">
<a href="#mainContent" class="fixed left-4 top-4 z-50 -translate-y-24 rounded-md bg-primary px-4 py-2 text-sm font-bold text-primary-foreground transition-transform focus:translate-y-0">본문으로 바로가기</a>
<t:header active="${active}"/>

<main id="mainContent" class="mx-auto w-full max-w-5xl flex-1 px-4 py-6 md:px-6 md:py-7" tabindex="-1">
    <jsp:doBody/>
</main>

<t:footer/>

<c:if test="${not empty toast}">
    <div data-flash-toast="<c:out value='${toast}'/>" hidden></div>
</c:if>

<script type="module" src="<c:url value='/js/common/toast.js'/>"></script>
<script type="module" src="<c:url value='/js/common/form-guard.js'/>"></script>
<c:if test="${not empty scriptPath}">
    <script type="module" src="<c:url value='/js/${scriptPath}.js'/>"></script>
</c:if>
<jsp:invoke fragment="script"/>
</body>
</html>
