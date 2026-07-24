<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="segOff" value="min-h-11 rounded-md px-2.5 text-xs font-bold text-muted-foreground transition-colors md:min-h-9"/>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<t:layout title="멤버·권한" active="members" role="${role}" scriptPath="members/list">
    <t:pageHead title="멤버 · 권한 설정" description="운영진이 학번과 이름을 사전 등록하고 학교 SSO 연결, 단일 소속 팀과 역할을 관리합니다">
        <button type="button" class="min-h-11 rounded-md border bg-card px-4 text-sm font-bold transition-colors hover:bg-secondary" data-page-action="cohort-add-open">기수 관리</button>
        <t:button openModal="memberModal">+ 멤버 사전 등록</t:button>
    </t:pageHead>

    <div class="mb-4 grid grid-cols-1 gap-2.5 md:grid-cols-3 md:gap-4">
        <t:statCard label="활성 멤버" value="—" unit="명" valueHook="active-members"/>
        <t:statCard label="운영 중인 기수" value="—" unit="개" delta="불러오는 중" valueHook="active-cohorts" deltaHook="active-cohort-names"/>
        <t:statCard label="SSO 확인 필요" value="—" unit="명" tone="danger" valueHook="waiting-sso"/>
    </div>

    <div class="mb-4 flex items-start gap-3 rounded-lg border bg-accent/50 px-4 py-3.5">
        <span class="flex size-8 shrink-0 items-center justify-center rounded-md bg-primary text-sm font-black text-primary-foreground">SSO</span>
        <div class="min-w-0"><b class="block text-sm">멤버 등록은 운영진 사전 등록으로만 진행합니다</b><p class="mt-1 text-xs leading-5 text-muted-foreground">운영진이 멤버를 먼저 등록하면, 멤버가 학교 계정으로 처음 로그인할 때 학번을 기준으로 계정이 연결됩니다.</p></div>
    </div>

    <section class="mb-4 rounded-lg border bg-card p-4" aria-label="멤버 검색과 필터">
        <input class="${input}" type="search" data-member-search placeholder="이름 또는 학번으로 검색" aria-label="멤버 검색">
        <details class="mt-3" data-member-filter-details open>
            <summary class="flex min-h-11 cursor-pointer items-center text-sm font-bold md:hidden">상세 필터</summary>
            <div class="grid gap-3 pt-3 sm:grid-cols-2 lg:grid-cols-5 md:pt-0">
                <div><button type="button" class="flex min-h-11 w-full items-center justify-between rounded-md border border-input bg-card px-3 text-left text-base md:hidden" data-member-filter-picker="team" aria-haspopup="dialog" aria-controls="memberFilterPickerModal"><span>팀</span><span class="text-muted-foreground" data-member-filter-label="team">전체 팀</span></button><select class="${input} hidden md:block" data-member-filter="team" aria-label="팀 필터"><option value="">전체 팀</option></select></div>
                <div><button type="button" class="flex min-h-11 w-full items-center justify-between rounded-md border border-input bg-card px-3 text-left text-base md:hidden" data-member-filter-picker="cohort" aria-haspopup="dialog" aria-controls="memberFilterPickerModal"><span>기수</span><span class="text-muted-foreground" data-member-filter-label="cohort">전체 기수</span></button><select class="${input} hidden md:block" data-member-filter="cohort" aria-label="기수 필터"><option value="">전체 기수</option></select></div>
                <div><button type="button" class="flex min-h-11 w-full items-center justify-between rounded-md border border-input bg-card px-3 text-left text-base md:hidden" data-member-filter-picker="status" aria-haspopup="dialog" aria-controls="memberFilterPickerModal"><span>활동 상태</span><span class="text-muted-foreground" data-member-filter-label="status">전체 활동 상태</span></button><select class="${input} hidden md:block" data-member-filter="status" aria-label="활동 상태 필터"><option value="">전체 활동 상태</option><option value="PRE_REGISTERED">사전 등록</option><option value="ACTIVE">활동 중</option><option value="SUSPENDED">활동 중지</option><option value="WITHDRAWN">탈퇴</option><option value="REGISTRATION_CANCELLED">등록 취소</option></select></div>
                <div><button type="button" class="flex min-h-11 w-full items-center justify-between rounded-md border border-input bg-card px-3 text-left text-base md:hidden" data-member-filter-picker="role" aria-haspopup="dialog" aria-controls="memberFilterPickerModal"><span>역할</span><span class="text-muted-foreground" data-member-filter-label="role">전체 역할</span></button><select class="${input} hidden md:block" data-member-filter="role" aria-label="역할 필터"><option value="">전체 역할</option><option value="MEMBER">일반 부원</option><option value="LEADER">팀장</option><option value="ADMIN">운영진</option></select></div>
                <div><button type="button" class="flex min-h-11 w-full items-center justify-between rounded-md border border-input bg-card px-3 text-left text-base md:hidden" data-member-filter-picker="sso" aria-haspopup="dialog" aria-controls="memberFilterPickerModal"><span>SSO 상태</span><span class="text-muted-foreground" data-member-filter-label="sso">전체 SSO 상태</span></button><select class="${input} hidden md:block" data-member-filter="sso" aria-label="SSO 상태 필터"><option value="">전체 SSO 상태</option><option value="WAITING">연결 대기</option><option value="LINKED">연결 완료</option><option value="REVIEW_REQUIRED">확인 필요</option></select></div>
            </div>
        </details>
        <button type="button" class="mt-3 hidden min-h-11 rounded-md border px-4 text-sm font-bold" data-member-filter-reset>필터 초기화</button>
    </section>

    <div class="rounded-lg border bg-card">
        <p class="border-b px-4 py-2 text-xs text-muted-foreground md:hidden">표를 좌우로 밀어 권한과 관리 항목을 확인하세요.</p>
        <t:dataTable caption="멤버와 권한 목록" cssClass="min-w-max">
            <thead><tr><th class="min-w-40">이름</th><th>학번</th><th>휴대폰</th><th>기수</th><th>소속 팀</th><th>활동 상태</th><th>SSO 연결</th><th>역할</th><th class="text-right">권한 변경</th><th class="text-right">관리</th></tr></thead>
            <tbody data-member-list>
            <tr data-member-state>
                <td colspan="10" class="px-5 py-11 text-center">
                    <b class="block text-sm font-extrabold" data-member-state-title>멤버 목록을 불러오는 중입니다</b>
                    <p class="mt-1 text-xs text-muted-foreground" data-member-state-message>잠시만 기다려 주세요.</p>
                    <button type="button" class="mx-auto mt-4 hidden min-h-11 rounded-md border bg-card px-4 text-xs font-bold" data-member-retry>다시 시도</button>
                </td>
            </tr>
            </tbody>
        </t:dataTable>
    </div>
    <t:pagination id="memberPagination" label="멤버 목록 페이지"/>

    <template data-member-row-template>
        <tr>
            <td class="min-w-40 whitespace-nowrap"><span class="flex items-center gap-2"><span data-member-avatar class="flex size-7 items-center justify-center rounded-full bg-primary text-xs font-black text-primary-foreground"></span><b data-member-name></b></span></td>
            <td class="whitespace-nowrap" data-member-student-no></td>
            <td class="whitespace-nowrap tabular-nums" data-member-phone></td>
            <td class="whitespace-nowrap" data-member-cohort></td>
            <td class="whitespace-nowrap" data-member-team></td>
            <td class="whitespace-nowrap" data-member-status></td>
            <td class="whitespace-nowrap" data-member-sso></td>
            <td class="whitespace-nowrap" data-member-role-cell></td>
            <td class="text-right"><span class="inline-flex rounded-lg border bg-secondary p-0.5"><button type="button" data-member-role="MEMBER" class="${segOff}">일반 부원</button><button type="button" data-member-role="LEADER" class="${segOff}">팀장</button><button type="button" data-member-role="ADMIN" class="${segOff}">운영진</button></span></td>
            <td class="text-right"><span class="inline-flex gap-1"><button type="button" data-page-action="member-manage-open" class="min-h-11 rounded-md border bg-card px-3 text-xs font-bold hover:bg-secondary">변경</button><button type="button" data-page-action="member-history-open" class="min-h-11 rounded-md border bg-card px-3 text-xs font-bold hover:bg-secondary">이력</button></span></td>
        </tr>
    </template>

    <t:modal id="memberModal" title="멤버 사전 등록" description="학교 SSO 첫 로그인 전에 학번과 이름, 단일 소속 팀을 등록합니다.">
        <jsp:attribute name="footer"><t:button variant="outline" action="close-modal">취소</t:button><t:button pageAction="member-add">등록</t:button></jsp:attribute>
        <jsp:body>
            <div class="flex flex-col gap-3">
                <div class="grid grid-cols-1 gap-3 md:grid-cols-2">
                    <div><label class="${label}" for="mbStudentNo">학번 <span class="text-accent-foreground">*</span></label><input class="${input}" id="mbStudentNo" type="text" inputmode="numeric" maxlength="20" placeholder="학교 학번"></div>
                    <div><label class="${label}" for="mbName">이름 <span class="text-accent-foreground">*</span></label><input class="${input}" id="mbName" type="text" maxlength="50" placeholder="학교 등록 이름"></div>
                </div>
                <div><label class="${label}" for="mbTeam">소속 팀 <span class="text-accent-foreground">*</span></label><select class="${input}" id="mbTeam"><option value="">팀을 선택해 주세요</option></select><p class="mt-1.5 text-xs text-muted-foreground">한 멤버는 동시에 하나의 활성 팀에만 소속됩니다.</p></div>
                <div><label class="${label}" for="mbCohort">기수 <span class="text-accent-foreground">*</span></label><select class="${input}" id="mbCohort"><option value="">기수를 선택해 주세요</option></select></div>
                <p class="hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-xs text-destructive" data-member-form-error role="alert"></p>
            </div>
        </jsp:body>
    </t:modal>

    <t:modal id="cohortModal" title="기수 관리" description="기수 이름을 추가하면 멤버 등록과 기수 변경에서 바로 선택할 수 있어요.">
        <jsp:attribute name="footer"><t:button variant="outline" action="close-modal">취소</t:button><t:button pageAction="cohort-add">기수 추가</t:button></jsp:attribute>
        <jsp:body>
            <label class="${label}" for="cohortName">새 기수 이름 <span class="text-accent-foreground">*</span></label>
            <input class="${input}" id="cohortName" type="text" maxlength="30" placeholder="예) 1-2">
            <p class="mt-2 text-xs leading-5 text-muted-foreground">기수는 삭제하지 않고 추가만 할 수 있어요. 등록된 멤버와 변경 이력은 그대로 유지됩니다.</p>
            <p class="mt-3 hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-xs text-destructive" data-cohort-form-error role="alert"></p>
        </jsp:body>
    </t:modal>

    <t:modal id="memberRoleModal" title="멤버 권한 변경" description="권한 변경 이력에 남길 사유를 입력합니다.">
        <jsp:attribute name="footer"><t:button variant="outline" action="close-modal">취소</t:button><t:button pageAction="member-role-save">변경</t:button></jsp:attribute>
        <jsp:body>
            <p class="rounded-md bg-secondary px-3 py-2.5 text-sm font-bold" data-member-role-summary></p>
            <label class="${label} mt-4" for="memberRoleReason">변경 사유 <span class="text-accent-foreground">*</span></label>
            <textarea class="min-h-28 w-full resize-y rounded-md border border-input bg-card px-3 py-2.5 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm" id="memberRoleReason" maxlength="500" placeholder="예) 2026-1학기 팀장 지정"></textarea>
            <p class="mt-2 hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-xs text-destructive" data-member-role-error role="alert"></p>
        </jsp:body>
    </t:modal>

    <t:modal id="memberManageModal" title="멤버 정보 변경" description="팀, 기수 또는 활동 상태 중 한 항목씩 변경합니다. 탈퇴 멤버는 활동 중으로 복구할 수 있습니다.">
        <jsp:attribute name="footer"><t:button variant="outline" action="close-modal">취소</t:button><t:button pageAction="member-change-save">변경</t:button></jsp:attribute>
        <jsp:body>
            <p class="rounded-md bg-secondary px-3 py-2.5 text-sm font-bold" data-member-change-summary></p>
            <div class="mt-4 grid gap-3 md:grid-cols-2">
                <div><label class="${label}" for="memberChangeType">변경 항목 *</label><select class="${input}" id="memberChangeType"><option value="team">소속 팀</option><option value="cohort">기수</option><option value="status">활동 상태</option></select></div>
                <div><label class="${label}" for="memberChangeValue">변경 값 *</label><select class="${input}" id="memberChangeValue"></select></div>
            </div>
            <label class="${label} mt-4" for="memberChangeReason">변경 사유 *</label>
            <textarea class="min-h-28 w-full resize-y rounded-md border border-input bg-card px-3 py-2.5 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm" id="memberChangeReason" maxlength="500" placeholder="예) 2026-1학기 팀 배정 변경"></textarea>
            <p class="mt-2 hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-xs text-destructive" data-member-change-error role="alert"></p>
        </jsp:body>
    </t:modal>

    <t:modal id="memberHistoryModal" title="멤버 변경 이력" description="팀·기수·권한·활동 상태 변경 기록을 확인합니다.">
        <jsp:body><div data-member-history class="flex flex-col gap-2" aria-live="polite"></div></jsp:body>
    </t:modal>
    <t:modal id="memberFilterPickerModal" title="필터 선택" mobileFullscreen="true">
        <jsp:body>
            <p class="text-sm text-muted-foreground" data-member-filter-picker-description></p>
            <div class="mt-4 flex flex-col gap-2" role="listbox" data-member-filter-options></div>
        </jsp:body>
    </t:modal>
</t:layout>
