<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout title="승인 활동 기록" active="activity-archive" role="${role}" scriptPath="activity/archive-detail">
    <jsp:body>
    <div data-archive-detail data-activity-record-id="<c:out value='${activityRecordId}'/>">
        <t:pageHead title="승인 활동 기록" description="최종 승인된 활동 기록입니다.">
            <a href="<c:url value='/activity/archive'/>" class="inline-flex min-h-11 items-center rounded-md border bg-card px-4 text-sm font-bold">승인 기록 목록</a>
        </t:pageHead>
        <article class="rounded-lg border bg-card p-5 lg:p-7" data-archive-detail-content>
            <div class="flex flex-wrap gap-2" data-archive-detail-badges></div>
            <h2 class="mt-3 text-2xl font-black" data-archive-detail-title></h2>
            <p class="mt-2 text-sm text-muted-foreground" data-archive-detail-meta></p>
            <div class="mt-6 border-t pt-6"><p class="whitespace-pre-wrap text-sm leading-7" data-archive-detail-body></p></div>
            <section class="mt-6 border-t pt-6"><h3 class="text-sm font-extrabold">증빙 및 문서</h3><div class="mt-3 grid gap-3 sm:grid-cols-2" data-archive-detail-files></div></section>
        </article>
        <div class="rounded-lg border bg-card px-5 py-11 text-center" data-archive-detail-state role="status" aria-live="polite"><b class="block text-sm font-extrabold" data-archive-detail-state-title>활동 기록을 불러오는 중입니다</b><p class="mt-1 text-xs text-muted-foreground" data-archive-detail-state-message>잠시만 기다려 주세요.</p><button type="button" class="mt-4 hidden min-h-11 rounded-md border px-4 text-xs font-bold" data-page-action="activity-archive-detail-retry" data-archive-detail-retry>다시 시도</button></div>
    </div>
    <template data-archive-detail-file-template><article class="overflow-hidden rounded-md border"><div class="relative flex h-48 items-center justify-center bg-secondary" data-archive-detail-preview><img class="size-full object-cover" data-archive-detail-image alt=""></div><div class="p-3"><b class="block truncate text-xs" data-archive-detail-file-name></b><a class="mt-3 inline-flex min-h-11 items-center rounded-md border px-3 text-xs font-bold hover:bg-secondary" data-archive-detail-download>다운로드</a></div></article></template>
    </jsp:body>
</t:layout>
