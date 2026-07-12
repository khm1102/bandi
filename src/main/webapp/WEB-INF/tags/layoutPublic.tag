<%@ tag description="공개 셸 — 네이비 상단 내비 + 푸터. 예매 등 비로그인 화면용" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="css" fragment="true" %>
<%@ attribute name="script" fragment="true" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<t:head title="${title}"/>
<jsp:invoke fragment="css"/>
</head>
<body class="flex min-h-screen flex-col">
<t:header/>

<main class="mx-auto w-full max-w-5xl flex-1 px-6 py-7">
    <jsp:doBody/>
</main>

<t:footer/>

<c:if test="${not empty toast}">
    <div data-flash-toast="<c:out value='${toast}'/>" hidden></div>
</c:if>

<script type="module" src="<c:url value='/js/common/toast.js'/>"></script>
<script type="module" src="<c:url value='/js/common/form-guard.js'/>"></script>
<jsp:invoke fragment="script"/>
</body>
</html>
