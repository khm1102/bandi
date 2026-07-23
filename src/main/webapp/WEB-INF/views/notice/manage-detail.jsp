<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout title="공지 관리" active="notices" role="${role}" scriptPath="notice/manage-detail">
    <jsp:attribute name="css">
        <link rel="stylesheet" href="<c:url value='/css/notice/markdown.css'/>">
    </jsp:attribute>
    <jsp:body>
    <div class="mx-auto max-w-4xl" data-manage-detail data-notice-id="<c:out value='${notice.internalNoticeId}'/>" data-notice-status="<c:out value='${notice.statusCode}'/>">
        <div class="mb-5 flex items-start justify-between gap-4">
            <div>
                <p class="text-sm font-bold text-muted-foreground">공지 관리</p>
                <h1 class="mt-1 text-2xl font-extrabold tracking-tight">공지 상태와 확인 현황을 관리하세요</h1>
            </div>
            <t:button variant="outline" href="/notices/manage">관리 목록</t:button>
        </div>
        <div class="mb-4 hidden rounded-lg border border-destructive bg-destructive-soft px-4 py-3 text-sm text-destructive" data-manage-error>
            <p data-manage-error-message></p>
            <button type="button" class="mt-2 min-h-11 font-bold underline" data-page-action="reload">새로고침</button>
        </div>
        <article class="rounded-xl border bg-card p-5 md:p-8">
            <header class="border-b pb-6">
                <div class="flex flex-wrap gap-2">
                    <t:badge tone="${notice.statusTone}"><c:out value="${notice.statusLabel}"/></t:badge>
                    <t:badge tone="info"><c:choose><c:when test="${notice.teamNotice}"><c:out value="${notice.teamName}"/></c:when><c:otherwise>전체 공지</c:otherwise></c:choose></t:badge>
                    <c:if test="${notice.important}"><t:badge tone="warning" dot="true">중요</t:badge></c:if>
                </div>
                <h2 class="mt-4 text-2xl font-extrabold tracking-tight"><c:out value="${notice.title}"/></h2>
                <dl class="mt-4 grid gap-x-8 gap-y-2 text-sm text-muted-foreground sm:grid-cols-2">
                    <div class="flex gap-2"><dt class="font-bold text-foreground">작성</dt><dd><c:out value="${notice.createdByName}"/></dd></div>
                    <div class="flex gap-2"><dt class="font-bold text-foreground">최근 수정</dt><dd><c:out value="${notice.updatedByName}"/> · <c:out value="${notice.updatedAt}"/></dd></div>
                    <c:if test="${not empty notice.publishedByName}"><div class="flex gap-2"><dt class="font-bold text-foreground">게시</dt><dd><c:out value="${notice.publishedByName}"/><c:if test="${not empty notice.publishedAt}"> · <c:out value="${notice.publishedAt}"/></c:if></dd></div></c:if>
                </dl>
            </header>
            <div class="markdown-content mt-7" data-markdown-content><t:markdown html="${notice.bodyHtml}"/></div>
            <c:if test="${not empty notice.attachments}">
                <section class="mt-8 border-t pt-5">
                    <h3 class="text-sm font-extrabold">첨부 파일</h3>
                    <ul class="mt-3 flex flex-col gap-2">
                        <c:forEach items="${notice.attachments}" var="file">
                            <li><a class="inline-flex min-h-11 items-center rounded-md border px-3 text-sm font-bold hover:bg-secondary" href="<c:url value='/api/internal-notice-management/${notice.internalNoticeId}/attachments/${file.storedFileId}/download'/>"><c:out value="${file.originalName}"/></a></li>
                        </c:forEach>
                    </ul>
                </section>
            </c:if>
        </article>
        <c:if test="${notice.readStatusAvailable}">
            <section class="mt-5 rounded-xl border bg-card p-5" aria-labelledby="readStatusTitle">
                <div class="flex flex-wrap items-center justify-between gap-3">
                    <div>
                        <h2 id="readStatusTitle" class="text-base font-extrabold">확인 현황</h2>
                        <p class="mt-1 text-sm text-muted-foreground" data-read-summary>확인 인원을 불러오는 중입니다.</p>
                    </div>
                    <button type="button" class="min-h-11 rounded-md border px-4 text-sm font-bold" data-page-action="toggle-read-status" aria-expanded="false">멤버별 현황 보기</button>
                </div>
                <div class="mt-4 hidden border-t pt-4" data-read-status-panel>
                    <ul class="divide-y" data-read-status-list></ul>
                    <button type="button" class="mt-4 hidden min-h-11 rounded-md border px-4 text-sm font-bold" data-page-action="retry-read-status">다시 시도</button>
                </div>
            </section>
        </c:if>
        <section class="mt-5 rounded-xl border bg-card p-5" aria-labelledby="noticeActionsTitle">
            <h2 id="noticeActionsTitle" class="text-base font-extrabold">가능한 작업</h2>
            <p class="mt-1 text-sm text-muted-foreground">현재 상태에서 안전하게 실행할 수 있는 작업만 보여드려요.</p>
            <div class="mt-4 flex flex-wrap gap-2">
                <c:if test="${notice.draft}">
                    <t:button href="/notices/${notice.internalNoticeId}/edit">초안 수정·게시</t:button>
                    <t:button variant="danger" pageAction="delete-draft" confirm="이 초안을 삭제합니다. 목록과 상세에서 더 이상 찾을 수 없어요." confirmAction="초안 삭제">초안 삭제</t:button>
                </c:if>
                <c:if test="${notice.scheduled}">
                    <t:button href="/notices/${notice.internalNoticeId}/edit">예약 변경</t:button>
                    <t:button variant="outline" pageAction="return-draft" confirm="예약을 취소하고 초안으로 되돌립니다. 게시 예정 시각은 지워져요." confirmAction="초안으로 이동">예약 취소</t:button>
                </c:if>
                <c:if test="${notice.published}">
                    <t:button href="/notices/${notice.internalNoticeId}/edit">공지 수정</t:button>
                    <t:button variant="danger" pageAction="close" confirm="공지 게시를 종료합니다. 멤버 공지 목록에서 즉시 내려가요." confirmAction="게시 종료">게시 종료</t:button>
                </c:if>
                <c:if test="${notice.closed}">
                    <t:button pageAction="archive" confirm="종료된 공지를 보관합니다. 관리 목록의 보관 필터에서 계속 확인할 수 있어요." confirmAction="공지 보관">공지 보관</t:button>
                </c:if>
                <c:if test="${notice.archived}">
                    <t:button pageAction="return-draft" confirm="보관 공지를 초안으로 되돌립니다. 이전 게시 시각과 읽음 현황은 초기화돼요." confirmAction="초안으로 복구">초안으로 복구</t:button>
                </c:if>
            </div>
        </section>
    </div>
    </jsp:body>
</t:layout>
