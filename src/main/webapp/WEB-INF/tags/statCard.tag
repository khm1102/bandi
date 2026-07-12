<%@ tag description="통계 타일 — 라벨/값/증감. tone: default|success|danger" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="label" required="true" %>
<%@ attribute name="value" required="true" %>
<%@ attribute name="unit" %>
<%@ attribute name="delta" %>
<%@ attribute name="tone" %>
<c:set var="toneClass" value="${tone == 'success' ? 'text-success' : tone == 'danger' ? 'text-destructive' : 'text-foreground'}"/>
<div class="rounded-lg border bg-card p-4">
    <span class="text-xs font-bold text-muted-foreground"><c:out value="${label}"/></span>
    <strong class="mt-2 block text-2xl font-black tracking-tight ${toneClass}">
        <c:out value="${value}"/>
        <c:if test="${not empty unit}"><small class="text-sm font-bold text-muted-foreground"><c:out value="${unit}"/></small></c:if>
    </strong>
    <c:if test="${not empty delta}">
        <span class="mt-0.5 block text-xs font-bold ${tone == 'default' or empty tone ? 'text-muted-foreground' : toneClass}"><c:out value="${delta}"/></span>
    </c:if>
</div>
