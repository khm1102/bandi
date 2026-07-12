<%@ tag description="카드 — 제목/더보기 헤더(선택) + 본문. flush=true면 본문 패딩 제거(리스트/테이블용)" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="title" %>
<%@ attribute name="moreUrl" %>
<%@ attribute name="moreLabel" %>
<%@ attribute name="flush" type="java.lang.Boolean" %>
<section class="overflow-hidden rounded-lg border bg-card">
    <c:if test="${not empty title}">
        <header class="flex items-center gap-2 border-b px-5 py-4">
            <h3 class="text-sm font-extrabold"><c:out value="${title}"/></h3>
            <c:if test="${not empty moreUrl}">
                <a href="${moreUrl}" class="ml-auto text-xs font-bold text-accent-foreground hover:underline"><c:out value="${empty moreLabel ? '더보기' : moreLabel}"/></a>
            </c:if>
        </header>
    </c:if>
    <div class="${flush ? '' : 'p-5'}">
        <jsp:doBody/>
    </div>
</section>
