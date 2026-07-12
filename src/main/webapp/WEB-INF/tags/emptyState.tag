<%@ tag description="빈 상태 — 데이터 없는 목록/검색 결과. 행동 유도 버튼은 body로" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="message" %>
<div class="px-5 py-11 text-center">
    <b class="block text-sm font-extrabold"><c:out value="${title}"/></b>
    <c:if test="${not empty message}">
        <p class="mt-1 text-xs text-muted-foreground"><c:out value="${message}"/></p>
    </c:if>
    <div class="mt-4 flex justify-center gap-2 empty:hidden">
        <jsp:doBody/>
    </div>
</div>
