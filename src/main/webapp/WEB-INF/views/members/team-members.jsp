<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout title="팀 멤버 관리" active="team-members" role="${role}" scriptPath="members/team-members">
    <t:pageHead title="팀 멤버 관리" description="현재 소속 팀 멤버의 소속 팀과 기수를 관리할 수 있어요."/>
    <div class="flex flex-col gap-6" data-team-members-root aria-busy="true">
        <section>
            <div class="mb-4 grid gap-3 rounded-lg border bg-card p-4 md:grid-cols-3">
                <input type="search" class="min-h-11 rounded-md border border-input bg-card px-3 text-sm" data-team-members-search placeholder="이름 또는 학번으로 검색" aria-label="팀 멤버 검색">
                <select class="min-h-11 rounded-md border border-input bg-card px-3 text-sm" data-team-members-filter="status" aria-label="활동 상태 필터"><option value="">전체 활동 상태</option><option value="PRE_REGISTERED">사전 등록</option><option value="ACTIVE">활동 중</option><option value="SUSPENDED">활동 중지</option><option value="WITHDRAWN">탈퇴</option><option value="REGISTRATION_CANCELLED">등록 취소</option></select>
                <button type="button" class="hidden min-h-11 rounded-md border px-4 text-sm font-bold md:col-span-3 md:justify-self-start" data-team-members-reset>필터 초기화</button>
            </div>
            <div class="rounded-lg border bg-card" data-team-members-state role="status" aria-live="polite">
                <div class="px-5 py-10 text-center"><b class="block text-sm font-extrabold" data-team-members-state-title>팀 멤버를 불러오는 중입니다</b><p class="mt-1 text-sm text-muted-foreground" data-team-members-state-message>잠시만 기다려 주세요.</p><button type="button" class="mt-4 hidden min-h-11 rounded-md border px-4 text-sm font-bold" data-team-members-retry>다시 시도</button></div>
            </div>
            <div class="hidden divide-y rounded-lg border bg-card" data-team-members-list></div>
            <t:pagination id="teamMemberPagination" label="팀 멤버 목록 페이지"/>
        </section>

    </div>
    <t:modal id="teamMemberChangeModal" title="소속 팀 변경" description="새 소속 팀과 변경 사유를 입력해 주세요." mobileFullscreen="true">
        <jsp:attribute name="footer">
            <button type="button" class="min-h-11 rounded-md border px-4 text-sm font-bold" data-team-member-change-cancel>취소</button>
            <button type="submit" form="teamMemberChangeForm" class="inline-flex min-h-11 items-center justify-center rounded-md bg-primary px-4 text-sm font-bold text-primary-foreground transition-colors hover:bg-primary-strong disabled:cursor-not-allowed disabled:opacity-50" data-team-member-change-submit>소속 팀 변경</button>
        </jsp:attribute>
        <jsp:body>
            <p id="teamMemberChangeSummary" class="text-sm text-muted-foreground" data-team-member-change-summary></p>
            <form id="teamMemberChangeForm" class="mt-5 grid gap-4" data-team-member-change-form novalidate>
                <div>
                    <label class="block text-sm font-bold" for="teamMemberTeam">새 소속 팀</label>
                    <select id="teamMemberTeam" class="mt-2 min-h-11 w-full rounded-md border border-input bg-card px-3 text-sm focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20" data-team-member-team-select aria-describedby="teamMemberChangeError" required></select>
                </div>
                <div>
                    <label class="block text-sm font-bold" for="teamMemberReason">변경 사유</label>
                    <textarea id="teamMemberReason" class="mt-2 min-h-28 w-full resize-y rounded-md border border-input bg-card px-3 py-2.5 text-sm focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20" data-team-member-reason aria-describedby="teamMemberChangeError" maxlength="500" required placeholder="예) 팀 운영 조정"></textarea>
                </div>
                <p id="teamMemberChangeError" class="hidden text-sm text-destructive" data-team-member-change-error role="alert"></p>
            </form>
        </jsp:body>
    </t:modal>
    <t:modal id="teamMemberCohortModal" title="기수 변경" description="새 기수와 변경 사유를 입력해 주세요." mobileFullscreen="true">
        <jsp:attribute name="footer">
            <button type="button" class="min-h-11 rounded-md border px-4 text-sm font-bold" data-team-member-cohort-cancel>취소</button>
            <button type="submit" form="teamMemberCohortForm" class="inline-flex min-h-11 items-center justify-center rounded-md bg-primary px-4 text-sm font-bold text-primary-foreground transition-colors hover:bg-primary-strong disabled:cursor-not-allowed disabled:opacity-50" data-team-member-cohort-submit>기수 변경</button>
        </jsp:attribute>
        <jsp:body>
            <p id="teamMemberCohortSummary" class="text-sm text-muted-foreground" data-team-member-cohort-summary></p>
            <form id="teamMemberCohortForm" class="mt-5 grid gap-4" data-team-member-cohort-form novalidate>
                <div>
                    <label class="block text-sm font-bold" for="teamMemberCohort">새 기수</label>
                    <select id="teamMemberCohort" class="mt-2 min-h-11 w-full rounded-md border border-input bg-card px-3 text-sm focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20" data-team-member-cohort-select aria-describedby="teamMemberCohortError" required></select>
                </div>
                <div>
                    <label class="block text-sm font-bold" for="teamMemberCohortReason">변경 사유</label>
                    <textarea id="teamMemberCohortReason" class="mt-2 min-h-28 w-full resize-y rounded-md border border-input bg-card px-3 py-2.5 text-sm focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20" data-team-member-cohort-reason aria-describedby="teamMemberCohortError" maxlength="500" required placeholder="예) 기수 정보 정정"></textarea>
                </div>
                <p id="teamMemberCohortError" class="hidden text-sm text-destructive" data-team-member-cohort-error role="alert"></p>
            </form>
        </jsp:body>
    </t:modal>
    <template data-team-member-row-template>
        <article class="flex flex-col gap-3 px-4 py-4 sm:flex-row sm:items-center sm:justify-between" data-team-member-row>
            <div class="flex min-w-0 items-center gap-3"><span class="flex size-10 shrink-0 items-center justify-center rounded-full bg-secondary text-sm font-extrabold" data-team-member-initial></span><div class="min-w-0"><b class="block truncate text-sm" data-team-member-name></b><span class="mt-1 block text-xs text-muted-foreground" data-team-member-meta></span><span class="mt-1 block text-xs tabular-nums text-muted-foreground" data-team-member-phone></span></div></div>
            <div class="flex flex-wrap items-center gap-2"><span class="rounded-full bg-secondary px-2.5 py-1 text-xs font-bold text-muted-foreground" data-team-member-team></span><span class="rounded-full bg-secondary px-2.5 py-1 text-xs font-bold text-muted-foreground" data-team-member-cohort></span><button type="button" class="min-h-11 rounded-md border px-3 text-sm font-bold transition-colors hover:bg-secondary" data-team-member-cohort-open>기수 변경</button><button type="button" class="min-h-11 rounded-md border px-3 text-sm font-bold transition-colors hover:bg-secondary" data-team-member-change-open>소속 팀 변경</button></div>
        </article>
    </template>
</t:layout>
