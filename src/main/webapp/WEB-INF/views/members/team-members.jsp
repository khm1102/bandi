<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout title="팀 멤버 관리" active="team-members" role="${role}" scriptPath="members/team-members">
    <t:pageHead title="팀 멤버 관리" description="팀장은 현재 팀 멤버의 소속 팀과 기수를, 운영진은 전체 멤버의 정보를 변경할 수 있어요."/>
    <div class="flex flex-col gap-6" data-team-members-root data-role="<c:out value='${role}'/>" aria-busy="true">
        <section>
            <div class="mb-4 grid gap-3 rounded-lg border bg-card p-4 md:grid-cols-3">
                <input type="search" class="min-h-11 rounded-md border border-input bg-card px-3 text-sm" data-team-members-search placeholder="이름 또는 학번으로 검색" aria-label="팀 멤버 검색">
                <c:if test="${role == 'admin'}"><select class="min-h-11 rounded-md border border-input bg-card px-3 text-sm" data-team-members-filter="team" aria-label="팀 필터"><option value="">전체 팀</option></select></c:if>
                <select class="min-h-11 rounded-md border border-input bg-card px-3 text-sm" data-team-members-filter="status" aria-label="활동 상태 필터"><option value="">전체 활동 상태</option><option value="PRE_REGISTERED">사전 등록</option><option value="ACTIVE">활동 중</option><option value="SUSPENDED">활동 중지</option><option value="WITHDRAWN">탈퇴</option><option value="REGISTRATION_CANCELLED">등록 취소</option></select>
                <button type="button" class="hidden min-h-11 rounded-md border px-4 text-sm font-bold md:col-span-3 md:justify-self-start" data-team-members-reset>필터 초기화</button>
            </div>
            <div class="rounded-lg border bg-card" data-team-members-state role="status" aria-live="polite">
                <div class="px-5 py-10 text-center"><b class="block text-sm font-extrabold" data-team-members-state-title>팀 멤버를 불러오는 중입니다</b><p class="mt-1 text-sm text-muted-foreground" data-team-members-state-message>잠시만 기다려 주세요.</p><button type="button" class="mt-4 hidden min-h-11 rounded-md border px-4 text-sm font-bold" data-team-members-retry>다시 시도</button></div>
            </div>
            <div class="hidden divide-y rounded-lg border bg-card" data-team-members-list></div>
            <t:pagination id="teamMemberPagination" label="팀 멤버 목록 페이지"/>
        </section>

        <section class="hidden border-t pt-6" data-team-member-change-section aria-labelledby="teamMemberChangeTitle">
            <h2 id="teamMemberChangeTitle" class="text-lg font-extrabold" data-team-member-change-title>선택한 멤버 정보 변경</h2>
            <p class="mt-1 text-sm text-muted-foreground" data-team-member-change-summary></p>
            <form class="mt-5 grid gap-4 lg:max-w-2xl" data-team-member-change-form novalidate>
                <div><label class="block text-sm font-bold" for="teamMemberChangeValue" data-team-member-change-label>변경 값</label><select id="teamMemberChangeValue" class="mt-2 min-h-11 w-full rounded-md border border-input bg-card px-3 text-sm focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20" data-team-member-change-select required></select></div>
                <div><label class="block text-sm font-bold" for="teamMemberReason">변경 사유</label><textarea id="teamMemberReason" class="mt-2 min-h-28 w-full resize-y rounded-md border border-input bg-card px-3 py-2.5 text-sm focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20" maxlength="500" data-team-member-reason required placeholder="예) 팀 운영 조정"></textarea></div>
                <p class="hidden text-sm text-destructive" data-team-member-change-error role="alert"></p>
                <div class="flex flex-wrap gap-2"><button type="submit" class="inline-flex min-h-11 items-center justify-center rounded-md bg-primary px-4 text-sm font-bold text-primary-foreground transition-colors hover:bg-primary-strong" data-team-member-change-submit>변경</button><button type="button" class="min-h-11 rounded-md border px-4 text-sm font-bold" data-team-member-change-cancel>선택 취소</button></div>
            </form>
        </section>
    </div>
    <template data-team-member-row-template>
        <article class="flex flex-col gap-3 px-4 py-4 sm:flex-row sm:items-center sm:justify-between" data-team-member-row>
            <div class="flex min-w-0 items-center gap-3"><span class="flex size-10 shrink-0 items-center justify-center rounded-full bg-secondary text-sm font-extrabold" data-team-member-initial></span><div class="min-w-0"><b class="block truncate text-sm" data-team-member-name></b><span class="mt-1 block text-xs text-muted-foreground" data-team-member-meta></span></div></div>
            <div class="flex flex-wrap items-center gap-2"><span class="rounded-full bg-secondary px-2.5 py-1 text-xs font-bold text-muted-foreground" data-team-member-team></span><span class="rounded-full bg-info-soft px-2.5 py-1 text-xs font-bold text-info" data-team-member-cohort></span><button type="button" class="min-h-11 rounded-md border px-3 text-sm font-bold transition-colors hover:bg-secondary" data-team-member-change-open data-team-member-change-type="team">소속 팀 변경</button><button type="button" class="min-h-11 rounded-md border px-3 text-sm font-bold transition-colors hover:bg-secondary" data-team-member-change-open data-team-member-change-type="cohort">기수 변경</button></div>
        </article>
    </template>
</t:layout>
