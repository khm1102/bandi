<%@ tag description="상태 배지 — tone: accent|success|warning|danger|info|neutral, dot=true면 색점 표시" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="tone" required="true" %>
<%@ attribute name="dot" type="java.lang.Boolean" %>
<c:choose>
    <c:when test="${tone == 'success'}"><c:set var="toneClass" value="bg-success-soft text-success"/></c:when>
    <c:when test="${tone == 'warning'}"><c:set var="toneClass" value="bg-warning-soft text-warning"/></c:when>
    <c:when test="${tone == 'danger'}"><c:set var="toneClass" value="bg-destructive-soft text-destructive"/></c:when>
    <c:when test="${tone == 'info'}"><c:set var="toneClass" value="bg-info-soft text-info"/></c:when>
    <c:when test="${tone == 'neutral'}"><c:set var="toneClass" value="bg-secondary text-muted-foreground"/></c:when>
    <c:otherwise><c:set var="toneClass" value="bg-accent text-accent-foreground"/></c:otherwise>
</c:choose>
<span class="inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-xs font-extrabold ${toneClass}">
    <c:if test="${dot}"><i class="size-1.5 rounded-full bg-current"></i></c:if>
    <jsp:doBody/>
</span>
