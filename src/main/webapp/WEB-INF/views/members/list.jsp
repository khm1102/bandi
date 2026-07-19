<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="segOff" value="min-h-11 rounded-md px-2.5 text-xs font-bold text-muted-foreground transition-colors md:min-h-9"/>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<t:layout title="멤버·권한" active="members" role="${role}" scriptPath="members/list">
    <t:pageHead title="멤버 · 권한 설정" description="운영진이 학번과 이름을 사전 등록하고 학교 SSO 연결, 단일 소속 팀과 역할을 관리합니다">
        <t:button openModal="memberModal">+ 멤버 사전 등록</t:button>
    </t:pageHead>

    <div class="mb-4 grid grid-cols-1 gap-2.5 md:grid-cols-3 md:gap-4">
        <t:statCard label="활성 멤버" value="—" unit="명" valueHook="active-members"/>
        <t:statCard label="운영 중인 기수" value="—" unit="개" delta="불러오는 중" valueHook="active-cohorts" deltaHook="active-cohort-names"/>
        <t:statCard label="SSO 연결 대기" value="—" unit="명" tone="danger" valueHook="waiting-sso"/>
    </div>

    <div class="mb-4 flex items-start gap-3 rounded-lg border bg-accent/50 px-4 py-3.5">
        <span class="flex size-8 shrink-0 items-center justify-center rounded-md bg-primary text-sm font-black text-primary-foreground">SSO</span>
        <div class="min-w-0"><b class="block text-sm">멤버 등록은 운영진 사전 등록으로만 진행합니다</b><p class="mt-1 text-xs leading-5 text-muted-foreground">운영진이 멤버를 먼저 등록하면, 멤버가 학교 계정으로 처음 로그인할 때 학번을 기준으로 계정이 연결됩니다.</p></div>
    </div>

    <div class="rounded-lg border bg-card">
        <t:dataTable caption="멤버와 권한 목록">
            <thead><tr><th>이름</th><th>학번</th><th>기수</th><th>소속 팀</th><th>SSO 연결</th><th>역할</th><th class="text-right">권한 변경</th></tr></thead>
            <tbody data-member-list>
            <tr data-member-state>
                <td colspan="7" class="px-5 py-11 text-center">
                    <b class="block text-sm font-extrabold" data-member-state-title>멤버 목록을 불러오는 중입니다</b>
                    <p class="mt-1 text-xs text-muted-foreground" data-member-state-message>잠시만 기다려 주세요.</p>
                    <button type="button" class="mx-auto mt-4 hidden min-h-11 rounded-md border bg-card px-4 text-xs font-bold" data-member-retry>다시 시도</button>
                </td>
            </tr>
            </tbody>
        </t:dataTable>
    </div>

    <template data-member-row-template>
        <tr>
            <td><span class="flex items-center gap-2"><span data-member-avatar class="flex size-7 items-center justify-center rounded-full bg-primary text-xs font-black text-primary-foreground"></span><b data-member-name></b></span></td>
            <td data-member-student-no></td>
            <td data-member-cohort></td>
            <td data-member-team></td>
            <td data-member-sso></td>
            <td data-member-role-cell></td>
            <td class="text-right"><span class="inline-flex rounded-lg border bg-secondary p-0.5"><button type="button" data-member-role="MEMBER" class="${segOff}">일반 부원</button><button type="button" data-member-role="LEADER" class="${segOff}">팀장</button><button type="button" data-member-role="ADMIN" class="${segOff}">운영진</button></span></td>
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

    <t:modal id="memberRoleModal" title="멤버 권한 변경" description="권한 변경 이력에 남길 사유를 입력합니다.">
        <jsp:attribute name="footer"><t:button variant="outline" action="close-modal">취소</t:button><t:button pageAction="member-role-save">변경</t:button></jsp:attribute>
        <jsp:body>
            <p class="rounded-md bg-secondary px-3 py-2.5 text-sm font-bold" data-member-role-summary></p>
            <label class="${label} mt-4" for="memberRoleReason">변경 사유 <span class="text-accent-foreground">*</span></label>
            <textarea class="min-h-28 w-full resize-y rounded-md border border-input bg-card px-3 py-2.5 text-sm transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20" id="memberRoleReason" maxlength="500" placeholder="예) 2026-1학기 팀장 지정"></textarea>
            <p class="mt-2 hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-xs text-destructive" data-member-role-error role="alert"></p>
        </jsp:body>
    </t:modal>
</t:layout>
