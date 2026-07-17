<%@ tag description="통계 타일 — 라벨/값/증감. tone: default|success|danger" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="label" required="true" %>
<%@ attribute name="value" required="true" %>
<%@ attribute name="unit" %>
<%@ attribute name="delta" %>
<%@ attribute name="tone" %>
<%@ attribute name="icon" %>
<%@ attribute name="iconTone" %>
<%@ attribute name="featured" type="java.lang.Boolean" %>
<%@ attribute name="valueHook" %>
<%@ attribute name="deltaHook" %>
<c:set var="toneClass" value="${tone == 'success' ? 'text-success' : tone == 'danger' ? 'text-destructive' : 'text-foreground'}"/>
<div class="rounded-lg border p-3.5 md:p-4 ${featured ? 'border-primary/40 bg-accent/60' : 'bg-card'}">
    <span class="flex items-center gap-1.5 text-xs font-bold text-muted-foreground md:gap-2">
        <c:if test="${not empty icon}">
            <span class="flex size-6 shrink-0 items-center justify-center rounded-md md:size-7 ${iconTone == 'danger' ? 'bg-destructive-soft text-destructive' : iconTone == 'warning' ? 'bg-warning-soft text-warning' : iconTone == 'info' ? 'bg-info-soft text-info' : 'bg-accent text-accent-foreground'}">
                <svg class="size-3.5 md:size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <c:choose>
                        <c:when test="${icon == 'calendar'}"><path d="M4 5h16v16H4zM4 9h16M8 3v4M16 3v4"/></c:when>
                        <c:when test="${icon == 'wallet'}"><path d="M3 7h16a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2zM3 7V6a2 2 0 0 1 2-2h11M17 13h.01"/></c:when>
                        <c:when test="${icon == 'box'}"><path d="M21 8l-9-5-9 5 9 5 9-5zM3 8v8l9 5 9-5V8M12 13v8"/></c:when>
                        <c:when test="${icon == 'ticket'}"><path d="M3 7a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v3a2 2 0 0 0 0 4v3a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-3a2 2 0 0 0 0-4z"/></c:when>
                    </c:choose>
                </svg>
            </span>
        </c:if>
        <c:out value="${label}"/>
    </span>
    <strong class="mt-2 block text-xl font-black tracking-tight tabular-nums md:text-2xl ${featured ? 'text-accent-foreground' : toneClass}">
        <span data-stat-value="${valueHook}"><c:out value="${value}"/></span>
        <c:if test="${not empty unit}"><small class="text-sm font-bold text-muted-foreground"><c:out value="${unit}"/></small></c:if>
    </strong>
    <c:if test="${not empty delta}">
        <span data-stat-delta="${deltaHook}" class="mt-0.5 block text-xs font-bold text-muted-foreground"><c:out value="${delta}"/></span>
    </c:if>
</div>
