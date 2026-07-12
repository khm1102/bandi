<%@ tag description="모달 — 기본 hidden, js/common/modal.js의 openModal(id)로 연다. footer에 액션 버튼" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="id" required="true" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="description" %>
<%@ attribute name="footer" fragment="true" %>
<div id="${id}" class="fixed inset-0 z-50 hidden items-center justify-center bg-sidebar/50 p-6 backdrop-blur-sm" data-modal-back>
    <div class="max-h-[88vh] w-full max-w-lg overflow-y-auto rounded-xl bg-card shadow-xl" role="dialog" aria-modal="true">
        <header class="flex items-start gap-3 border-b px-6 py-5">
            <div class="min-w-0">
                <h3 class="text-sm font-extrabold"><c:out value="${title}"/></h3>
                <c:if test="${not empty description}">
                    <p class="mt-1 text-xs text-muted-foreground"><c:out value="${description}"/></p>
                </c:if>
            </div>
            <button type="button" data-action="close-modal"
                    class="ml-auto flex size-8 shrink-0 items-center justify-center rounded-md border text-muted-foreground hover:bg-secondary hover:text-foreground"
                    aria-label="닫기">&times;</button>
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
