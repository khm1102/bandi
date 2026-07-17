<%@ tag description="페이지 헤더 — 제목/설명 + 우측 액션(body)" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="description" %>
<div class="mb-5 flex flex-wrap items-end gap-3.5">
    <div>
        <h1 class="text-2xl font-black tracking-tight"><c:out value="${title}"/></h1>
        <c:if test="${not empty description}">
            <p class="mt-1 text-sm text-muted-foreground"><c:out value="${description}"/></p>
        </c:if>
    </div>
    <div class="ml-auto flex flex-wrap items-center gap-2">
        <jsp:doBody/>
    </div>
</div>
