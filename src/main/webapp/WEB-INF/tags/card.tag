<%@ tag description="카드 — 제목/더보기 헤더(선택) + 본문. flush=true면 본문 패딩 제거(리스트/테이블용)" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="title" %>
<%@ attribute name="moreUrl" %>
<%@ attribute name="moreLabel" %>
<%@ attribute name="icon" %>
<%@ attribute name="flush" type="java.lang.Boolean" %>
<section class="overflow-hidden rounded-lg border bg-card">
    <c:if test="${not empty title}">
        <header class="flex items-center gap-2 border-b px-5 py-4">
            <h2 class="flex items-center gap-2 text-sm font-extrabold">
                <c:if test="${not empty icon}">
                    <svg class="size-4 text-accent-foreground" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                        <c:choose>
                            <c:when test="${icon == 'clock'}"><path d="M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2M12 6v6l4 2"/></c:when>
                            <c:when test="${icon == 'activity'}"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></c:when>
                            <c:when test="${icon == 'bell'}"><path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M10 21h4"/></c:when>
                            <c:when test="${icon == 'folder'}"><path d="M3 7a2 2 0 0 1 2-2h4l2 2h8a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></c:when>
                            <c:when test="${icon == 'check'}"><path d="M20 6L9 17l-5-5"/></c:when>
                        </c:choose>
                    </svg>
                </c:if>
                <c:out value="${title}"/>
            </h2>
            <c:choose>
                <c:when test="${not empty moreUrl}">
                    <a href="<c:url value='${moreUrl}'/>" class="ml-auto text-xs font-bold text-accent-foreground hover:underline"><c:out value="${empty moreLabel ? '더보기' : moreLabel}"/></a>
                </c:when>
                <c:when test="${not empty moreLabel}">
                    <span class="ml-auto text-xs font-bold text-muted-foreground"><c:out value="${moreLabel}"/></span>
                </c:when>
            </c:choose>
        </header>
    </c:if>
    <div class="${flush ? '' : 'p-5'}">
        <jsp:doBody/>
    </div>
</section>
