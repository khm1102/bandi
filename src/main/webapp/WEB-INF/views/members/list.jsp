<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-bold text-muted-foreground"/>
<t:layout title="멤버·권한" active="members" role="${role}" scriptPath="members/list">
    <main class="w-full">
        <t:pageHead title="멤버 · 권한" description="SSO 연결 대기와 확인이 필요한 멤버부터 처리해요"><t:button pageAction="member-create-open">멤버 사전 등록</t:button></t:pageHead>

        <section class="mb-8" aria-labelledby="memberNextTitle">
            <p class="text-sm font-bold text-accent-foreground">다음에 확인할 멤버</p>
            <div class="mt-2 grid gap-4 border-l-4 border-primary bg-accent px-5 py-5 md:grid-cols-[minmax(0,1fr)_auto] md:items-center">
                <div><h2 id="memberNextTitle" class="text-lg font-bold" data-member-next-title>멤버 정보를 확인하고 있어요</h2><p class="mt-1 text-sm leading-6 text-muted-foreground" data-member-next-message>잠시만 기다려 주세요.</p></div>
                <span class="hidden" data-member-next-action><t:button variant="outline" pageAction="member-manage-open" cssClass="w-full md:w-auto">멤버 정보 확인</t:button></span>
            </div>
        </section>

        <dl class="mb-6 grid grid-cols-3 divide-x border-y py-4 text-center" aria-label="멤버 요약">
            <div class="px-2"><dt class="text-xs text-muted-foreground">활성 멤버</dt><dd class="mt-1 text-lg font-bold tabular-nums"><span data-stat-value="active-members">—</span>명</dd></div>
            <div class="px-2"><dt class="text-xs text-muted-foreground">운영 기수</dt><dd class="mt-1 text-lg font-bold tabular-nums"><span data-stat-value="active-cohorts">—</span>개</dd><p class="sr-only" data-stat-delta="active-cohort-names"></p></div>
            <div class="px-2"><dt class="text-xs text-muted-foreground">SSO 확인 필요</dt><dd class="mt-1 text-lg font-bold tabular-nums text-destructive"><span data-stat-value="waiting-sso">—</span>명</dd></div>
        </dl>

        <aside class="mb-6 border-l-4 border-info bg-info-soft px-4 py-3"><b class="block text-sm">운영진이 먼저 등록한 멤버만 로그인할 수 있어요</b><p class="mt-1 text-sm leading-6 text-muted-foreground">학교 계정으로 처음 로그인하면 학번을 기준으로 사전 등록 정보와 SSO 계정이 연결돼요.</p></aside>

        <div class="mb-5 grid gap-3 md:grid-cols-[minmax(0,1fr)_14rem] md:items-end">
            <div><label class="${label}" for="memberQuery">멤버 검색</label><input class="${input}" id="memberQuery" type="search" placeholder="이름이나 학번 입력" data-member-search></div>
            <div><label class="${label}" for="memberFilter">확인 상태</label><select class="${input}" id="memberFilter" data-member-filter><option value="ALL">전체 멤버</option><option value="SSO_PENDING">SSO 연결 대기·확인 필요</option><option value="ACTIVE">활동 중</option><option value="INACTIVE">활동 중지·탈퇴</option></select></div>
        </div>

        <div class="border-y">
            <div data-member-list></div>
            <div data-member-state class="px-5 py-12 text-center"><b class="block text-sm font-bold" data-member-state-title>멤버 목록을 불러오는 중입니다</b><p class="mt-1 text-sm text-muted-foreground" data-member-state-message>잠시만 기다려 주세요.</p><button type="button" class="mx-auto mt-4 hidden min-h-11 rounded-md border bg-card px-4 text-sm font-bold" data-member-retry>다시 시도</button></div>
        </div>
    </main>

    <template data-member-row-template>
        <article data-member-row class="grid grid-cols-[auto_minmax(0,1fr)] gap-x-3 gap-y-3 border-b px-4 py-5 last:border-b-0 md:grid-cols-[auto_minmax(0,1fr)_auto] md:items-center md:px-5">
            <span data-member-avatar class="flex size-10 items-center justify-center rounded-full bg-primary text-sm font-bold text-primary-foreground"></span>
            <div class="min-w-0"><div class="flex flex-wrap items-center gap-2"><h2 class="text-base font-bold" data-member-name></h2><span data-member-role-cell></span><span data-member-sso></span></div><p class="mt-1 text-xs text-muted-foreground"><span data-member-student-no></span> · <span data-member-team></span> · <span data-member-cohort></span></p><div class="mt-2" data-member-status></div></div>
            <div class="col-start-2 grid grid-cols-3 gap-2 md:col-start-auto"><t:button variant="outline" size="compact" pageAction="member-manage-open">정보</t:button><t:button variant="outline" size="compact" pageAction="member-role-open">권한</t:button><t:button variant="outline" size="compact" pageAction="member-history-open">이력</t:button></div>
        </article>
    </template>

    <t:sheet id="memberSheet" title="멤버 사전 등록" description="학교 SSO 첫 로그인 전에 학번과 이름, 단일 소속 팀을 등록해요.">
        <jsp:attribute name="footer"><t:button variant="outline" action="close-sheet">취소</t:button><t:button pageAction="member-add">멤버 등록</t:button></jsp:attribute>
        <jsp:body><div class="flex flex-col gap-4"><div class="grid grid-cols-1 gap-4 sm:grid-cols-2"><div><label class="${label}" for="mbStudentNo">학번 <span class="text-accent-foreground">*</span></label><input class="${input}" id="mbStudentNo" type="text" inputmode="numeric" maxlength="20" placeholder="학교 학번"></div><div><label class="${label}" for="mbName">이름 <span class="text-accent-foreground">*</span></label><input class="${input}" id="mbName" type="text" maxlength="50" placeholder="학교 등록 이름"></div></div><div><label class="${label}" for="mbTeam">소속 팀 <span class="text-accent-foreground">*</span></label><select class="${input}" id="mbTeam"><option value="">팀을 선택해 주세요</option></select><p class="mt-1.5 text-xs text-muted-foreground">한 멤버는 동시에 하나의 활성 팀에만 소속돼요.</p></div><div><label class="${label}" for="mbCohort">기수 <span class="text-accent-foreground">*</span></label><select class="${input}" id="mbCohort"><option value="">기수를 선택해 주세요</option></select></div><p class="hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-sm text-destructive" data-member-form-error role="alert"></p></div></jsp:body>
    </t:sheet>

    <t:sheet id="memberRoleSheet" title="멤버 권한 변경" description="변경할 권한과 이력에 남길 사유를 입력해요.">
        <jsp:attribute name="footer"><t:button variant="outline" action="close-sheet">취소</t:button><t:button pageAction="member-role-save">권한 변경</t:button></jsp:attribute>
        <jsp:body><p class="border-l-4 border-primary bg-accent px-3 py-2.5 text-sm font-bold" data-member-role-summary></p><label class="${label} mt-4" for="memberRoleValue">변경할 권한 <span class="text-accent-foreground">*</span></label><select class="${input}" id="memberRoleValue"><option value="MEMBER">일반 부원</option><option value="LEADER">팀장</option><option value="ADMIN">운영진</option></select><label class="${label} mt-4" for="memberRoleReason">변경 사유 <span class="text-accent-foreground">*</span></label><textarea class="min-h-32 w-full resize-y rounded-md border border-input bg-card px-3 py-2.5 text-sm" id="memberRoleReason" maxlength="500" placeholder="예) 2026-1학기 팀장 지정"></textarea><p class="mt-2 hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-sm text-destructive" data-member-role-error role="alert"></p></jsp:body>
    </t:sheet>

    <t:sheet id="memberManageSheet" title="멤버 정보 변경" description="팀, 기수 또는 활동 상태 중 한 항목씩 변경해요.">
        <jsp:attribute name="footer"><t:button variant="outline" action="close-sheet">취소</t:button><t:button pageAction="member-change-save">정보 변경</t:button></jsp:attribute>
        <jsp:body><p class="border-l-4 border-primary bg-accent px-3 py-2.5 text-sm font-bold" data-member-change-summary></p><div class="mt-4 grid gap-4 sm:grid-cols-2"><div><label class="${label}" for="memberChangeType">변경 항목 *</label><select class="${input}" id="memberChangeType"><option value="team">소속 팀</option><option value="cohort">기수</option><option value="status">활동 상태</option></select></div><div><label class="${label}" for="memberChangeValue">변경 값 *</label><select class="${input}" id="memberChangeValue"></select></div></div><label class="${label} mt-4" for="memberChangeReason">변경 사유 *</label><textarea class="min-h-32 w-full resize-y rounded-md border border-input bg-card px-3 py-2.5 text-sm" id="memberChangeReason" maxlength="500" placeholder="예) 2026-1학기 제작팀 배정 변경"></textarea><p class="mt-2 hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-sm text-destructive" data-member-change-error role="alert"></p></jsp:body>
    </t:sheet>

    <t:sheet id="memberHistorySheet" title="멤버 변경 이력" description="팀·기수·권한·활동 상태 변경 기록을 확인해요."><jsp:body><div data-member-history class="flex flex-col gap-2"></div></jsp:body></t:sheet>
</t:layout>
