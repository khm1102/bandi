<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layoutPublic title="${notice.title}" active="notices">
    <article class="mx-auto max-w-3xl">
        <a href="<c:url value='/notices'/>" class="inline-flex min-h-11 items-center text-xs font-black text-accent-foreground underline-offset-4 hover:underline">공시 목록으로 돌아가기</a>
        <header class="mt-4 border-b pb-6">
            <div class="flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
                <c:if test="${notice.pinned}"><t:badge tone="accent">중요</t:badge></c:if>
                <t:badge tone="neutral"><c:choose><c:when test="${notice.categoryCode == 'PERFORMANCE'}">공연</c:when><c:when test="${notice.categoryCode == 'RECRUITMENT'}">모집</c:when><c:when test="${notice.categoryCode == 'RESERVATION'}">관람</c:when><c:otherwise><c:out value="${notice.categoryCode}"/></c:otherwise></c:choose></t:badge>
                <time datetime="<c:out value='${notice.publishStartDttm}'/>"><c:out value="${fn:replace(notice.publishStartDttm, 'T', ' ')}"/></time>
            </div>
            <h1 class="mt-4 text-3xl font-black leading-tight tracking-tight"><c:out value="${notice.title}"/></h1>
            <p class="mt-3 text-xs text-muted-foreground">게시 <c:out value="${notice.createdByName}"/> · 최종 수정 <time datetime="<c:out value='${notice.updatedDttm}'/>"><c:out value="${fn:replace(notice.updatedDttm, 'T', ' ')}"/></time></p>
        </header>

        <div class="min-h-64 whitespace-pre-line py-8 text-sm leading-8 text-foreground/90"><c:out value="${notice.body}"/></div>

        <c:if test="${not empty notice.attachments}">
            <section class="border-t py-6" aria-labelledby="noticeAttachmentsTitle">
                <h2 id="noticeAttachmentsTitle" class="text-sm font-black">첨부파일</h2>
                <ul class="mt-3 divide-y rounded-lg border bg-card">
                    <c:forEach var="attachment" items="${notice.attachments}">
                        <c:url var="downloadUrl" value="/api/public-notices/${notice.publicNoticeId}/attachments/${attachment.storedFileId}/download"/>
                        <li class="flex flex-col gap-2 p-4 sm:flex-row sm:items-center">
                            <div class="min-w-0 flex-1"><strong class="block truncate text-sm"><c:out value="${attachment.originalName}"/></strong><span class="mt-1 block text-xs text-muted-foreground"><c:out value="${attachment.contentType}"/> · <fmt:formatNumber value="${attachment.sizeBytes}"/> bytes</span></div>
                            <a href="<c:out value='${downloadUrl}'/>" class="inline-flex min-h-11 shrink-0 items-center justify-center rounded-md border bg-card px-3 text-xs font-black hover:bg-secondary">다운로드</a>
                        </li>
                    </c:forEach>
                </ul>
            </section>
        </c:if>

        <footer class="border-t py-6">
            <a href="<c:url value='/notices'/>" class="inline-flex min-h-11 items-center rounded-md border bg-card px-4 text-sm font-black hover:bg-secondary">목록으로</a>
        </footer>
    </article>
</t:layoutPublic>
