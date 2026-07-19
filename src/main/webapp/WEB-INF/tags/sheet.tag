<%@ tag description="sheet — 짧은 생성·수정 폼과 목록 상세용 패널. 모바일 하단, 데스크톱 우측. js/common/sheet.js의 openSheet(id) 또는 data-open-sheet로 연다" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="id" required="true" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="description" %>
<%@ attribute name="footer" fragment="true" %>
<c:set var="descriptionId" value="${id}Description"/>
<div id="${id}" class="fixed inset-0 z-50 hidden bg-sidebar/50 backdrop-blur-sm"
     data-sheet-back aria-hidden="true">
    <div class="fixed inset-x-0 bottom-0 top-20 flex flex-col overflow-hidden rounded-t-xl bg-card shadow-xl md:inset-y-0 md:left-auto md:right-0 md:top-0 md:w-96 md:rounded-none md:border-l"
         role="dialog" aria-modal="true" aria-labelledby="${id}Title"
         aria-describedby="${not empty description ? descriptionId : ''}"
         data-sheet-panel tabindex="-1">
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
