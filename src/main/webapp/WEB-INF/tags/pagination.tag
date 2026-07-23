<%@ tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="id" required="true" type="java.lang.String" %>
<%@ attribute name="label" required="true" type="java.lang.String" %>
<nav id="<c:out value="${id}"/>"
     class="mt-6 hidden flex-col gap-3 border-t border-border pt-4 sm:flex-row sm:items-center sm:justify-between"
     data-pagination aria-label="<c:out value="${label}"/>">
    <p class="text-sm text-muted-foreground" data-pagination-total aria-live="polite"></p>
    <div class="flex items-center justify-between gap-2 sm:justify-end">
        <button type="button"
                class="inline-flex h-11 items-center justify-center rounded-md border border-border bg-background px-4 text-sm font-medium text-foreground hover:bg-secondary focus-visible:ring-2 focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50"
                data-pagination-action="previous">이전</button>
        <div class="hidden items-center gap-1 sm:flex" data-pagination-pages></div>
        <span class="min-w-20 text-center text-sm font-medium text-foreground sm:hidden"
              data-pagination-mobile></span>
        <button type="button"
                class="inline-flex h-11 items-center justify-center rounded-md border border-border bg-background px-4 text-sm font-medium text-foreground hover:bg-secondary focus-visible:ring-2 focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50"
                data-pagination-action="next">다음</button>
    </div>
</nav>
