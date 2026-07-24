<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout title="공지" active="notices" role="${role}">
    <jsp:attribute name="css">
        <link rel="stylesheet" href="<c:url value='/css/notice/markdown.css'/>">
    </jsp:attribute>
    <jsp:attribute name="script">
        <script type="module">
            import {initializeShareActions} from '<c:url value="/js/common/share.js"/>';
            initializeShareActions();
        </script>
    </jsp:attribute>
    <jsp:body>
    <article class="mx-auto max-w-4xl rounded-xl border bg-card p-5 md:p-8">
        <div class="border-b pb-5">
            <div class="flex flex-wrap gap-2"><t:badge tone="info"><c:choose><c:when test="${notice.teamNotice}"><c:out value="${notice.teamName}"/></c:when><c:otherwise>전체 공지</c:otherwise></c:choose></t:badge><c:if test="${notice.important}"><t:badge tone="warning" dot="true">중요</t:badge></c:if></div>
            <h1 class="mt-3 text-2xl font-black tracking-tight"><c:out value="${notice.title}"/></h1>
            <p class="mt-3 text-xs text-muted-foreground">작성 <c:out value="${notice.createdByName}"/> · <c:out value="${notice.publishedAt}"/> 게시<c:if test="${notice.publishedByName != notice.createdByName}"> · 게시 <c:out value="${notice.publishedByName}"/></c:if><c:if test="${not empty notice.updatedAt}"> · <c:out value="${notice.updatedAt}"/> 수정</c:if></p>
        </div>
        <div class="markdown-content mt-7" data-markdown-content><t:markdown html="${notice.bodyHtml}"/></div>
        <c:if test="${not empty notice.attachments}"><section class="mt-8 border-t pt-5"><h2 class="text-sm font-extrabold">첨부 파일</h2><ul class="mt-3 flex flex-col gap-2"><c:forEach items="${notice.attachments}" var="file"><li><a class="inline-flex min-h-11 items-center rounded-md border px-3 text-sm font-bold hover:bg-secondary" href="<c:url value='/api/internal-notices/${notice.internalNoticeId}/attachments/${file.storedFileId}/download'/>"><c:out value="${file.originalName}"/></a></li></c:forEach></ul></section></c:if>
        <div class="mt-8 flex flex-wrap gap-2 border-t pt-5">
            <t:button variant="outline" href="/notices">목록으로</t:button>
            <button id="noticeShareButton" type="button" class="inline-flex min-h-11 items-center justify-center gap-2 rounded-md border bg-card px-4 text-sm font-bold hover:bg-secondary" data-share-button data-share-can-issue="<c:out value='${notice.canIssuePublicShare}'/>" data-share-enabled="<c:out value='${notice.shareEnabled}'/>" data-share-title="<c:out value='${notice.title}'/>" data-share-internal-url="<c:url value='/notices/${notice.internalNoticeId}'/>" data-share-issue-url="<c:url value='/api/internal-notices/${notice.internalNoticeId}/share-link'/>" data-share-revoke-target="#noticeShareRevoke">
                <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/><path d="m8.6 13.5 6.8 4M15.4 6.5l-6.8 4"/></svg>
                공유
            </button>
            <c:if test="${notice.canIssuePublicShare}"><button id="noticeShareRevoke" type="button" class="<c:if test='${not notice.shareEnabled}'>hidden </c:if>inline-flex min-h-11 items-center justify-center rounded-md border px-4 text-sm font-bold text-destructive hover:bg-destructive-soft" data-share-revoke data-share-revoke-url="<c:url value='/api/internal-notices/${notice.internalNoticeId}/share-link'/>" data-share-button-target="#noticeShareButton">공유 중단</button></c:if>
            <c:if test="${notice.canManage}"><t:button href="/notices/manage/${notice.internalNoticeId}">공지 관리</t:button></c:if>
        </div>
    </article>
    </jsp:body>
</t:layout>
