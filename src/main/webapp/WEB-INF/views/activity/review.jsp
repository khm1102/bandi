<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout title="활동 기록 검수" active="activity-review" role="${role}" scriptPath="activity/review">
    <jsp:body>
    <t:pageHead title="활동 기록 검수" description="제출된 기록을 확인하고 팀장 승인과 최종 승인을 처리합니다.">
        <c:if test="${role == 'admin'}">
            <t:button variant="outline" pageAction="activity-review-export">CSV 내보내기</t:button>
        </c:if>
    </t:pageHead>

    <section class="rounded-lg border bg-card p-4" aria-label="검수 기록 필터">
        <div class="grid gap-3 lg:grid-cols-[minmax(0,1fr)_12rem_12rem_auto] lg:items-end">
            <div>
                <label class="mb-1.5 block text-xs font-extrabold text-muted-foreground" for="reviewKeyword">제목 검색</label>
                <input id="reviewKeyword" type="search" maxlength="150" placeholder="활동 기록 제목으로 검색" class="h-11 w-full rounded-md border border-input bg-background px-3 text-base md:text-sm">
            </div>
            <div>
                <label class="mb-1.5 block text-xs font-extrabold text-muted-foreground" for="reviewStatus">검수 상태</label>
                <select id="reviewStatus" class="h-11 w-full rounded-md border border-input bg-background px-3 text-base md:text-sm">
                    <option value="">전체 상태</option>
                    <option value="SUBMITTED">검수 대기</option>
                    <option value="TEAM_APPROVED">팀장 승인</option>
                    <option value="REVISION_REQUESTED">보완 요청</option>
                    <option value="APPROVED">최종 승인</option>
                    <option value="ARCHIVED">보관</option>
                </select>
            </div>
            <div>
                <label class="mb-1.5 block text-xs font-extrabold text-muted-foreground" for="reviewType">기록 형식</label>
                <select id="reviewType" class="h-11 w-full rounded-md border border-input bg-background px-3 text-base md:text-sm">
                    <option value="">전체 형식</option>
                    <option value="SIMPLE">간단 기록</option>
                    <option value="HWPX">한글 내역서</option>
                </select>
            </div>
            <div class="hidden" data-review-team-filter>
                <label class="mb-1.5 block text-xs font-extrabold text-muted-foreground" for="reviewTeam">팀</label>
                <select id="reviewTeam" class="h-11 w-full rounded-md border border-input bg-background px-3 text-base md:text-sm"></select>
            </div>
        </div>
        <p class="mt-3 text-xs leading-5 text-muted-foreground" data-review-scope>팀장은 현재 소속 팀의 기록만 검수할 수 있어요.</p>
    </section>

    <section class="mt-5" aria-labelledby="reviewListTitle">
        <div class="mb-3 flex items-baseline justify-between gap-3">
            <h2 id="reviewListTitle" class="text-lg font-black">검수 기록</h2>
            <p class="text-xs text-muted-foreground" data-review-result-summary></p>
        </div>
        <div class="hidden overflow-x-auto rounded-lg border bg-card md:block" data-review-table-wrap>
            <table class="min-w-[860px] w-full text-left text-sm">
                <thead class="border-b bg-secondary/70 text-xs text-muted-foreground">
                <tr>
                    <th class="px-4 py-3 font-extrabold">기록</th>
                    <th class="px-4 py-3 font-extrabold">팀</th>
                    <th class="px-4 py-3 font-extrabold">작성자</th>
                    <th class="px-4 py-3 font-extrabold">활동 일시</th>
                    <th class="px-4 py-3 font-extrabold">현재 단계</th>
                    <th class="px-4 py-3 text-right font-extrabold">검수</th>
                </tr>
                </thead>
                <tbody data-review-table-body></tbody>
            </table>
        </div>
        <div class="grid gap-3 md:hidden" data-review-card-list></div>
        <div class="rounded-lg border bg-card px-5 py-11 text-center" data-review-state role="status" aria-live="polite">
            <b class="block text-sm font-extrabold" data-review-state-title>검수 기록을 불러오는 중입니다</b>
            <p class="mt-1 text-xs leading-5 text-muted-foreground" data-review-state-message>잠시만 기다려 주세요.</p>
            <button type="button" class="mt-4 hidden min-h-11 rounded-md border px-4 text-xs font-bold" data-page-action="activity-review-retry" data-review-retry>다시 시도</button>
        </div>
        <t:pagination id="activityReviewPagination" label="활동 기록 검수 페이지"/>
    </section>

    <template data-review-row-template>
        <tr class="border-b last:border-b-0">
            <td class="px-4 py-3"><div class="flex flex-wrap items-center gap-2" data-review-row-badges></div><b class="mt-2 block" data-review-row-title></b><span class="mt-1 block text-xs text-muted-foreground" data-review-row-format></span></td>
            <td class="px-4 py-3" data-review-row-team></td>
            <td class="px-4 py-3" data-review-row-author></td>
            <td class="px-4 py-3 tabular-nums" data-review-row-date></td>
            <td class="px-4 py-3" data-review-row-status></td>
            <td class="px-4 py-3 text-right"><a class="inline-flex min-h-11 items-center rounded-md border px-3 text-xs font-bold hover:bg-secondary" data-review-row-link>검수 열기</a></td>
        </tr>
    </template>
    <template data-review-card-template>
        <article class="rounded-lg border bg-card p-4"><div class="flex flex-wrap gap-2" data-review-card-badges></div><h3 class="mt-3 text-base font-black" data-review-card-title></h3><dl class="mt-3 grid grid-cols-2 gap-x-3 gap-y-2 text-xs"><div><dt class="text-muted-foreground">팀</dt><dd class="mt-0.5" data-review-card-team></dd></div><div><dt class="text-muted-foreground">작성자</dt><dd class="mt-0.5" data-review-card-author></dd></div><div class="col-span-2"><dt class="text-muted-foreground">활동 일시</dt><dd class="mt-0.5 tabular-nums" data-review-card-date></dd></div></dl><a class="mt-4 inline-flex min-h-11 items-center rounded-md border px-3 text-xs font-bold hover:bg-secondary" data-review-card-link>검수 열기</a></article>
    </template>
    </jsp:body>
</t:layout>
