<%@ tag description="공통 필터 칩 — 필터 그룹·값과 활성 상태를 일관되게 관리" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="group" required="true" %>
<%@ attribute name="value" required="true" %>
<%@ attribute name="label" required="true" %>
<%@ attribute name="active" type="java.lang.Boolean" %>
<%@ attribute name="count" %>
<%@ attribute name="dot" type="java.lang.Boolean" %>
<c:set var="baseClass" value="inline-flex h-11 items-center gap-1.5 rounded-md border px-3 text-xs font-bold transition-colors focus-visible:ring-2 focus-visible:ring-ring md:h-8"/>
<c:choose>
    <c:when test="${active}">
        <c:set var="stateClass" value=" border-sidebar bg-sidebar text-white"/>
    </c:when>
    <c:otherwise>
        <c:set var="stateClass" value=" bg-card text-muted-foreground hover:border-sidebar-muted"/>
    </c:otherwise>
</c:choose>
<button type="button"
        class="${baseClass}${stateClass}"
        data-filter-group="<c:out value='${group}'/>"
        data-filter-value="<c:out value='${value}'/>"
        aria-pressed="${active ? 'true' : 'false'}">
    <c:if test="${dot}"><span class="size-2 rounded-full bg-primary" aria-hidden="true"></span></c:if>
    <c:out value="${label}"/>
    <c:if test="${not empty count}"><span class="opacity-70"><c:out value="${count}"/></span></c:if>
</button>
