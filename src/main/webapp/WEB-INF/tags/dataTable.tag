<%@ tag description="공통 데이터 테이블 — 가로 스크롤, caption, 헤더·셀·행 기본 스타일 관리" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="caption" required="true" %>
<%@ attribute name="cssClass" %>
<div class="overflow-x-auto">
    <table class="w-full border-collapse text-left [&_th]:whitespace-nowrap [&_th]:border-b [&_th]:bg-secondary [&_th]:px-4 [&_th]:py-3 [&_th]:text-xs [&_th]:font-extrabold [&_th]:text-muted-foreground [&_td]:border-b [&_td]:px-4 [&_td]:py-3 [&_td]:text-sm [&_tbody_tr]:transition-colors [&_tbody_tr]:hover:bg-secondary/50 [&_tbody_tr:last-child_td]:border-b-0 ${cssClass}">
        <caption class="sr-only"><c:out value="${caption}"/></caption>
        <jsp:doBody/>
    </table>
</div>
