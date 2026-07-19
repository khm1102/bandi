<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="segOff" value="min-h-11 rounded-md px-2.5 text-xs font-bold text-muted-foreground transition-colors md:min-h-9"/>
<c:set var="segOn" value="min-h-11 rounded-md border bg-card px-2.5 text-xs font-bold text-foreground md:min-h-9"/>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<t:layout title="멤버·권한" active="members" role="${role}" scriptPath="members/list">
    <t:pageHead title="멤버 · 권한 설정" description="운영진이 학번과 이름을 사전 등록하고 학교 SSO 연결, 단일 소속 팀과 역할을 관리합니다">
        <t:button openModal="memberModal">+ 멤버 사전 등록</t:button>
    </t:pageHead>

    <div class="mb-4 grid grid-cols-1 gap-2.5 md:grid-cols-3 md:gap-4">
        <t:statCard label="활성 멤버" value="6" unit="명"/>
        <t:statCard label="운영 중인 기수" value="2" unit="개" delta="26-1기 · 26-2기"/>
        <t:statCard label="SSO 연결 대기" value="2" unit="명" tone="danger"/>
    </div>

    <div class="mb-4 flex items-start gap-3 rounded-lg border bg-accent/50 px-4 py-3.5">
        <span class="flex size-8 shrink-0 items-center justify-center rounded-md bg-primary text-sm font-black text-primary-foreground">SSO</span>
        <div class="min-w-0"><b class="block text-sm">멤버 등록은 운영진 사전 등록으로만 진행합니다</b><p class="mt-1 text-xs leading-5 text-muted-foreground">운영진이 멤버를 먼저 등록하면, 멤버가 학교 계정으로 처음 로그인할 때 학번을 기준으로 계정이 연결됩니다.</p></div>
    </div>

    <div class="rounded-lg border bg-card">
        <t:dataTable caption="멤버와 권한 목록">
            <thead><tr><th>이름</th><th>학번</th><th>기수</th><th>소속 팀</th><th>SSO 연결</th><th>역할</th><th class="text-right">권한 변경</th></tr></thead>
            <tbody data-member-list>
            <tr><td><span class="flex items-center gap-2"><span class="flex size-7 items-center justify-center rounded-full bg-primary text-xs font-black text-primary-foreground">LS</span><b data-member-name>이서준</b></span></td><td>202012345</td><td><t:badge tone="info">26-1기</t:badge></td><td>연출</td><td><t:badge tone="success">연결 완료</t:badge></td><td data-member-role-cell><t:badge tone="accent">운영진</t:badge></td><td class="text-right"><span class="inline-flex rounded-lg border bg-secondary p-0.5"><button type="button" class="${segOff}">일반 부원</button><button type="button" class="${segOff}">팀장</button><button type="button" class="${segOn}">운영진</button></span></td></tr>
            <tr><td><span class="flex items-center gap-2"><span class="flex size-7 items-center justify-center rounded-full bg-info text-xs font-black text-white">JD</span><b data-member-name>정도윤</b></span></td><td>202112346</td><td><t:badge tone="info">26-1기</t:badge></td><td>무대팀</td><td><t:badge tone="success">연결 완료</t:badge></td><td data-member-role-cell><t:badge tone="info">팀장</t:badge></td><td class="text-right"><span class="inline-flex rounded-lg border bg-secondary p-0.5"><button type="button" class="${segOff}">일반 부원</button><button type="button" class="${segOn}">팀장</button><button type="button" class="${segOff}">운영진</button></span></td></tr>
            <tr><td><span class="flex items-center gap-2"><span class="flex size-7 items-center justify-center rounded-full bg-sidebar-accent text-xs font-black text-white">KH</span><b data-member-name>김하늘</b></span></td><td>202412347</td><td><t:badge tone="info">26-2기</t:badge></td><td>배우</td><td><t:badge tone="warning">연결 대기</t:badge></td><td data-member-role-cell><t:badge tone="neutral">일반 부원</t:badge></td><td class="text-right"><span class="inline-flex rounded-lg border bg-secondary p-0.5"><button type="button" class="${segOn}">일반 부원</button><button type="button" class="${segOff}">팀장</button><button type="button" class="${segOff}">운영진</button></span></td></tr>
            <tr><td><span class="flex items-center gap-2"><span class="flex size-7 items-center justify-center rounded-full bg-accent-foreground text-xs font-black text-white">PS</span><b data-member-name>박서연</b></span></td><td>202312348</td><td><t:badge tone="info">26-2기</t:badge></td><td>오퍼팀</td><td><t:badge tone="success">연결 완료</t:badge></td><td data-member-role-cell><t:badge tone="neutral">일반 부원</t:badge></td><td class="text-right"><span class="inline-flex rounded-lg border bg-secondary p-0.5"><button type="button" class="${segOn}">일반 부원</button><button type="button" class="${segOff}">팀장</button><button type="button" class="${segOff}">운영진</button></span></td></tr>
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
            <td class="text-right"><span class="inline-flex rounded-lg border bg-secondary p-0.5"><button type="button" class="${segOff}">일반 부원</button><button type="button" class="${segOff}">팀장</button><button type="button" class="${segOff}">운영진</button></span></td>
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
                <div><label class="${label}" for="mbTeam">소속 팀 <span class="text-accent-foreground">*</span></label><select class="${input}" id="mbTeam"><option>연출</option><option>조연출</option><option>배우</option><option>무대팀</option><option>오퍼팀</option><option>디자인팀</option><option>영상팀</option><option>영상 배우</option><option>영상 촬영</option><option>영상 연출</option><option>영상 편집</option></select><p class="mt-1.5 text-xs text-muted-foreground">한 멤버는 동시에 하나의 활성 팀에만 소속됩니다.</p></div>
                <div class="grid grid-cols-1 gap-3 md:grid-cols-2">
                    <div><label class="${label}" for="mbCohort">기수</label><input class="${input}" id="mbCohort" type="text" value="26-2기" placeholder="예) 26-2기"></div>
                    <div><label class="${label}" for="mbRole">역할</label><select class="${input}" id="mbRole"><option>일반 부원</option><option>팀장</option><option>운영진</option></select></div>
                </div>
            </div>
        </jsp:body>
    </t:modal>
</t:layout>
