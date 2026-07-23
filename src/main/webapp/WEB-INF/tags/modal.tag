<%@ tag description="모달 — 기본 hidden, js/common/modal.js의 openModal(id)로 연다. footer에 액션 버튼" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="id" required="true" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="description" %>
<%@ attribute name="footer" fragment="true" %>
<%@ attribute name="size" %>
<%@ attribute name="mobileFullscreen" type="java.lang.Boolean" %>
<c:set var="descriptionId" value="${id}Description"/>
<c:set var="backdropSpacing" value="${mobileFullscreen ? 'p-0 md:p-6' : 'p-4 md:p-6'}"/>
<c:set var="panelWidth" value="${size == 'lg' ? 'max-w-3xl' : 'max-w-lg'}"/>
<c:set var="panelShape" value="${mobileFullscreen ? 'h-full rounded-none md:h-auto md:rounded-xl' : 'rounded-xl'}"/>
<div id="${id}" class="fixed inset-0 z-50 hidden items-center justify-center bg-sidebar/50 ${backdropSpacing} backdrop-blur-sm"
     data-modal-back aria-hidden="true">
    <div class="max-h-full w-full ${panelWidth} ${panelShape} overflow-y-auto overscroll-contain bg-card shadow-xl"
         role="dialog" aria-modal="true" aria-labelledby="${id}Title"
         aria-describedby="${not empty description ? descriptionId : ''}"
         data-modal-panel tabindex="-1">
        <header class="flex items-start gap-3 border-b px-6 py-5">
            <div class="min-w-0">
                <h2 id="${id}Title" class="text-base font-extrabold"><c:out value="${title}"/></h2>
                <c:if test="${not empty description}">
                    <p id="${descriptionId}" class="mt-1 text-sm text-muted-foreground"><c:out value="${description}"/></p>
                </c:if>
            </div>
            <button type="button" data-action="close-modal"
                    class="ml-auto flex size-11 shrink-0 items-center justify-center rounded-md border text-xl text-muted-foreground transition-colors hover:bg-secondary hover:text-foreground"
                    aria-label="닫기">
                <svg class="size-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M18 6 6 18M6 6l12 12"/></svg>
            </button>
        </header>
        <div class="px-6 py-5">
            <jsp:doBody/>
        </div>
        <c:if test="${not empty footer}">
            <footer class="flex justify-end gap-2 border-t px-6 py-4">
                <jsp:invoke fragment="footer"/>
            </footer>
        </c:if>
    </div>
</div>
