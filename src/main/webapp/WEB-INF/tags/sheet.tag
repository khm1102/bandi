<%@ tag description="sheet — 모바일 하단 sheet. 데스크톱은 presentation에 따라 우측 패널·중앙 폼·넓은 작업공간으로 표시한다" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="id" required="true" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="description" %>
<%@ attribute name="presentation" %>
<%@ attribute name="footer" fragment="true" %>
<c:set var="descriptionId" value="${id}Description"/>
<c:choose>
    <c:when test="${presentation eq 'form'}">
        <c:set var="panelClass" value="md:bottom-auto md:left-1/2 md:right-auto md:top-1/2 md:h-fit md:max-h-[calc(100dvh-3rem)] md:w-11/12 md:max-w-2xl md:-translate-x-1/2 md:-translate-y-1/2 md:rounded-xl md:border"/>
    </c:when>
    <c:when test="${presentation eq 'workspace'}">
        <c:set var="panelClass" value="md:bottom-auto md:left-1/2 md:right-auto md:top-1/2 md:h-fit md:max-h-[calc(100dvh-3rem)] md:w-11/12 md:max-w-4xl md:-translate-x-1/2 md:-translate-y-1/2 md:rounded-xl md:border"/>
    </c:when>
    <c:otherwise>
        <c:set var="panelClass" value="md:inset-y-0 md:left-auto md:right-0 md:top-0 md:w-[30rem] md:rounded-none md:border-l"/>
    </c:otherwise>
</c:choose>
<div id="${id}" class="fixed inset-0 z-50 hidden bg-sidebar/50 backdrop-blur-sm"
     data-sheet-back aria-hidden="true">
    <div class="fixed inset-x-0 bottom-0 top-20 flex flex-col overflow-hidden rounded-t-xl bg-card shadow-xl ${panelClass}"
         role="dialog" aria-modal="true" aria-labelledby="${id}Title"
         aria-describedby="${not empty description ? descriptionId : ''}"
         data-sheet-panel data-sheet-presentation="${empty presentation ? 'panel' : presentation}" tabindex="-1">
        <header class="flex items-start gap-3 border-b px-5 py-4">
            <div class="min-w-0">
                <h2 id="${id}Title" class="text-base font-bold"><c:out value="${title}"/></h2>
                <c:if test="${not empty description}">
                    <p id="${descriptionId}" class="mt-1 text-sm text-muted-foreground"><c:out value="${description}"/></p>
                </c:if>
            </div>
            <button type="button" data-action="close-sheet"
                    class="ml-auto flex size-11 shrink-0 items-center justify-center rounded-md border text-muted-foreground transition-colors hover:bg-secondary hover:text-foreground"
                    aria-label="닫기">
                <svg class="size-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M18 6 6 18M6 6l12 12"/></svg>
            </button>
        </header>
        <div class="min-h-0 flex-1 overflow-y-auto overscroll-contain px-5 py-4">
            <jsp:doBody/>
        </div>
        <c:if test="${not empty footer}">
            <footer class="flex justify-end gap-2 border-t bg-card px-5 py-4">
                <jsp:invoke fragment="footer"/>
            </footer>
        </c:if>
    </div>
</div>
