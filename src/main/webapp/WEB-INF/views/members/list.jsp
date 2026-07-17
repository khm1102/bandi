<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="segOff" value="rounded-md px-2.5 py-1 text-xs font-bold text-muted-foreground transition-colors"/>
<c:set var="segOn" value="rounded-md border bg-card px-2.5 py-1 text-xs font-bold text-foreground"/>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<t:layout title="멤버·권한" active="members" role="${role}" scriptPath="members/list">
    <t:pageHead title="멤버 · 권한 설정" description="부원을 기수와 팀별로 관리하고 가입용 초대코드를 발급합니다">
        <t:button variant="outline" openModal="inviteModal">+ 초대코드 생성</t:button>
        <t:button openModal="memberModal">+ 부원 추가</t:button>
    </t:pageHead>

    <div class="mb-4 grid grid-cols-3 gap-4">
        <t:statCard label="전체 부원" value="6" unit="명"/>
        <t:statCard label="운영 중인 기수" value="2" unit="개" delta="26-1기 · 26-2기"/>
        <t:statCard label="활성 초대코드" value="2" unit="개"/>
    </div>

    <t:card title="기수별 초대코드">
        <div class="flex flex-col gap-2.5" data-invite-list>
            <div data-invite-card class="flex items-center gap-3 rounded-lg border px-4 py-3">
                <div class="min-w-0 flex-1">
                    <div class="flex items-center gap-2"><t:badge tone="accent">26-2기</t:badge><span data-invite-code class="font-mono text-sm font-black tracking-widest text-accent-foreground">BANDI-262-M9Q4</span></div>
                    <p data-invite-meta class="mt-1 text-xs text-muted-foreground">07/08 생성 · 가입 2명 · 사용 가능</p>
                </div>
                <t:button variant="outline" size="compact" pageAction="invite-copy">복사</t:button>
                <t:button variant="outline" size="compact" pageAction="invite-toggle">중지</t:button>
            </div>
            <div data-invite-card class="flex items-center gap-3 rounded-lg border px-4 py-3">
                <div class="min-w-0 flex-1">
                    <div class="flex items-center gap-2"><t:badge tone="accent">26-1기</t:badge><span data-invite-code class="font-mono text-sm font-black tracking-widest text-accent-foreground">BANDI-261-A7K2</span></div>
                    <p data-invite-meta class="mt-1 text-xs text-muted-foreground">07/01 생성 · 가입 4명 · 사용 가능</p>
                </div>
                <t:button variant="outline" size="compact" pageAction="invite-copy">복사</t:button>
                <t:button variant="outline" size="compact" pageAction="invite-toggle">중지</t:button>
            </div>
        </div>
    </t:card>

    <div class="mt-4 rounded-lg border bg-card">
        <t:dataTable caption="멤버와 권한 목록">
            <thead><tr><th>이름</th><th>기수</th><th>소속팀</th><th>역할</th><th class="text-right">권한 변경</th></tr></thead>
            <tbody data-member-list>
            <tr><td><span class="flex items-center gap-2"><span class="flex size-7 items-center justify-center rounded-full bg-primary text-xs font-black text-primary-foreground">LS</span><b>이서준</b></span></td><td><t:badge tone="info">26-1기</t:badge></td><td>운영진</td><td><t:badge tone="accent">운영진</t:badge></td><td class="text-right"><span class="inline-flex rounded-lg border bg-secondary p-0.5"><button type="button" class="${segOff}">일반 부원</button><button type="button" class="${segOff}">팀장</button><button type="button" class="${segOn}">운영진</button></span></td></tr>
            <tr><td><span class="flex items-center gap-2"><span class="flex size-7 items-center justify-center rounded-full bg-info text-xs font-black text-white">JD</span><b>정도윤</b></span></td><td><t:badge tone="info">26-1기</t:badge></td><td>무대</td><td><t:badge tone="info">팀장</t:badge></td><td class="text-right"><span class="inline-flex rounded-lg border bg-secondary p-0.5"><button type="button" class="${segOff}">일반 부원</button><button type="button" class="${segOn}">팀장</button><button type="button" class="${segOff}">운영진</button></span></td></tr>
            <tr><td><span class="flex items-center gap-2"><span class="flex size-7 items-center justify-center rounded-full bg-sidebar-accent text-xs font-black text-white">KH</span><b>김하늘</b></span></td><td><t:badge tone="info">26-2기</t:badge></td><td>배우연출</td><td><t:badge tone="neutral">일반 부원</t:badge></td><td class="text-right"><span class="inline-flex rounded-lg border bg-secondary p-0.5"><button type="button" class="${segOn}">일반 부원</button><button type="button" class="${segOff}">팀장</button><button type="button" class="${segOff}">운영진</button></span></td></tr>
            <tr><td><span class="flex items-center gap-2"><span class="flex size-7 items-center justify-center rounded-full bg-accent-foreground text-xs font-black text-white">PS</span><b>박서연</b></span></td><td><t:badge tone="info">26-2기</t:badge></td><td>오퍼</td><td><t:badge tone="neutral">일반 부원</t:badge></td><td class="text-right"><span class="inline-flex rounded-lg border bg-secondary p-0.5"><button type="button" class="${segOn}">일반 부원</button><button type="button" class="${segOff}">팀장</button><button type="button" class="${segOff}">운영진</button></span></td></tr>
            <tr><td><span class="flex items-center gap-2"><span class="flex size-7 items-center justify-center rounded-full bg-success text-xs font-black text-white">HJ</span><b>한지우</b></span></td><td><t:badge tone="info">26-2기</t:badge></td><td>디자인</td><td><t:badge tone="neutral">일반 부원</t:badge></td><td class="text-right"><span class="inline-flex rounded-lg border bg-secondary p-0.5"><button type="button" class="${segOn}">일반 부원</button><button type="button" class="${segOff}">팀장</button><button type="button" class="${segOff}">운영진</button></span></td></tr>
            <tr><td><span class="flex items-center gap-2"><span class="flex size-7 items-center justify-center rounded-full bg-warning text-xs font-black text-white">CM</span><b>최민준</b></span></td><td><t:badge tone="info">26-1기</t:badge></td><td>영상</td><td><t:badge tone="neutral">일반 부원</t:badge></td><td class="text-right"><span class="inline-flex rounded-lg border bg-secondary p-0.5"><button type="button" class="${segOn}">일반 부원</button><button type="button" class="${segOff}">팀장</button><button type="button" class="${segOff}">운영진</button></span></td></tr>
            </tbody>
        </t:dataTable>
    </div>

    <template data-invite-card-template>
        <div data-invite-card class="flex items-center gap-3 rounded-lg border px-4 py-3">
            <div class="min-w-0 flex-1">
                <div class="flex items-center gap-2"><span data-invite-cohort></span><span data-invite-code class="font-mono text-sm font-black tracking-widest text-accent-foreground"></span></div>
                <p data-invite-meta class="mt-1 text-xs text-muted-foreground"></p>
            </div>
            <t:button variant="outline" size="compact" pageAction="invite-copy">복사</t:button>
            <t:button variant="outline" size="compact" pageAction="invite-toggle">중지</t:button>
        </div>
    </template>

    <template data-member-row-template>
        <tr>
            <td><span class="flex items-center gap-2"><span data-member-avatar class="flex size-7 items-center justify-center rounded-full bg-primary text-xs font-black text-primary-foreground"></span><b data-member-name></b></span></td>
            <td data-member-cohort></td>
            <td data-member-team></td>
            <td data-member-role-cell></td>
            <td class="text-right">
                <span class="inline-flex rounded-lg border bg-secondary p-0.5">
                    <button type="button" class="${segOff}">일반 부원</button>
                    <button type="button" class="${segOff}">팀장</button>
                    <button type="button" class="${segOff}">운영진</button>
                </span>
            </td>
        </tr>
    </template>

    <t:modal id="inviteModal" title="초대코드 생성" description="이 코드로 가입한 회원은 선택한 기수에 자동 소속됩니다.">
        <jsp:attribute name="footer">
            <t:button variant="outline" action="close-modal">취소</t:button>
            <t:button pageAction="invite-add">코드 생성</t:button>
        </jsp:attribute>
        <jsp:body>
            <div class="flex flex-col gap-3">
                <div><label class="${label}" for="ivCohort">가입 기수 <span class="text-accent-foreground">*</span></label><input class="${input}" id="ivCohort" type="text" value="26-2기" placeholder="예) 26-2기"></div>
                <p class="text-xs text-muted-foreground">생성된 코드는 멤버 관리 화면에서 복사하거나 사용 중지할 수 있습니다.</p>
            </div>
        </jsp:body>
    </t:modal>

    <t:modal id="memberModal" title="부원 추가" description="부원이 이름과 소속팀을 직접 적을 수 있어, 팀을 일일이 나눌 필요가 없어요.">
        <jsp:attribute name="footer">
            <t:button variant="outline" action="close-modal">취소</t:button>
            <t:button pageAction="member-add">추가</t:button>
        </jsp:attribute>
        <jsp:body>
            <div class="flex flex-col gap-3">
                <div><label class="${label}" for="mbName">이름 <span class="text-accent-foreground">*</span></label><input class="${input}" id="mbName" type="text" placeholder="이름"></div>
                <div>
                    <label class="${label}" for="mbTeam">소속팀 <span class="text-accent-foreground">*</span></label>
                    <input class="${input}" id="mbTeam" type="text" list="teamList" placeholder="팀 이름을 자유롭게 입력 (예: 배우연출, 소품)">
                    <datalist id="teamList"><option value="운영진"><option value="무대"><option value="배우연출"><option value="오퍼"><option value="디자인"><option value="영상"></datalist>
                    <p class="mt-1.5 text-xs text-muted-foreground">기존 팀을 고르거나 새 팀 이름을 그대로 입력하면 됩니다.</p>
                </div>
                <div><label class="${label}" for="mbCohort">기수</label><input class="${input}" id="mbCohort" type="text" value="26-2기" placeholder="예) 26-2기"></div>
                <div><label class="${label}" for="mbRole">역할</label><select class="${input}" id="mbRole"><option>일반 부원</option><option>팀장</option><option>운영진</option></select></div>
            </div>
        </jsp:body>
    </t:modal>
</t:layout>
