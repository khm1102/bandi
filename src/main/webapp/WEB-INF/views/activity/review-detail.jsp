<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout title="활동 기록 검수" active="activity-review" role="${role}" scriptPath="activity/review-detail">
    <jsp:body>
    <div data-review-detail data-activity-record-id="<c:out value='${activityRecordId}'/>">
        <t:pageHead title="활동 기록 검수" description="기록 내용과 제출 이력을 확인한 뒤 다음 단계를 결정합니다.">
            <a href="<c:url value='/activity/review'/>" class="inline-flex min-h-11 items-center justify-center rounded-md border bg-card px-4 text-sm font-bold">검수 목록</a>
        </t:pageHead>
        <div class="rounded-lg border bg-card p-5 lg:p-7" data-review-detail-content>
            <div class="flex flex-wrap gap-2" data-review-detail-badges></div>
            <h2 class="mt-3 text-2xl font-black" data-review-detail-title></h2>
            <p class="mt-2 text-sm text-muted-foreground" data-review-detail-meta></p>
            <div class="mt-6 border-t pt-6"><h3 class="text-sm font-extrabold">활동 내용</h3><p class="mt-3 whitespace-pre-wrap text-sm leading-7" data-review-detail-body></p></div>
            <section class="mt-6 border-t pt-6"><h3 class="text-sm font-extrabold">증빙 및 문서</h3><div class="mt-3 grid gap-3 sm:grid-cols-2" data-review-files></div><p class="mt-3 hidden text-xs text-muted-foreground" data-review-files-empty>등록된 첨부가 없습니다.</p></section>
            <section class="mt-6 border-t pt-6"><h3 class="text-sm font-extrabold">검수 이력</h3><div class="mt-3 flex flex-col gap-2" data-review-history></div></section>
            <section class="mt-6 border-t pt-6"><h3 class="text-sm font-extrabold">제출 리비전</h3><div class="mt-3 flex flex-col gap-2" data-review-revisions></div></section>
            <section class="mt-6 hidden rounded-lg border bg-secondary/50 p-4" data-review-action-panel><h3 class="text-sm font-extrabold" data-review-action-title>검수 처리</h3><p class="mt-1 text-xs leading-5 text-muted-foreground" data-review-action-description></p><label class="mt-4 hidden text-xs font-bold" for="reviewComment" data-review-comment-label>보완 사유</label><textarea id="reviewComment" class="mt-2 hidden min-h-28 w-full resize-y rounded-md border bg-card px-3 py-2.5 text-sm" maxlength="1000" data-review-comment></textarea><p class="mt-3 hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-xs text-destructive" data-review-action-error role="alert"></p><div class="mt-4 flex flex-wrap gap-2" data-review-actions></div></section>
        </div>
        <div class="rounded-lg border bg-card px-5 py-11 text-center" data-review-detail-state role="status" aria-live="polite"><b class="block text-sm font-extrabold" data-review-detail-state-title>활동 기록을 불러오는 중입니다</b><p class="mt-1 text-xs text-muted-foreground" data-review-detail-state-message>잠시만 기다려 주세요.</p><button type="button" class="mt-4 hidden min-h-11 rounded-md border px-4 text-xs font-bold" data-page-action="activity-review-detail-retry" data-review-detail-retry>다시 시도</button></div>
    </div>
    <template data-review-file-template><article class="overflow-hidden rounded-md border"><div class="relative flex h-48 items-center justify-center bg-secondary" data-review-file-preview><img class="size-full object-cover" data-review-file-image alt=""></div><div class="p-3"><b class="block truncate text-xs" data-review-file-name></b><a class="mt-3 inline-flex min-h-11 items-center rounded-md border px-3 text-xs font-bold hover:bg-secondary" data-review-file-download>다운로드</a></div></article></template>
    </jsp:body>
</t:layout>
