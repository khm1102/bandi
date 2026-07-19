<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-bold text-muted-foreground"/>
<t:layout title="팀별 제작 진행" active="production" role="${role}" scriptPath="production/index">
    <main class="w-full">
    <t:pageHead title="팀별 제작 진행" description="차단되거나 마감이 지난 업무부터 해결해요">
        <t:button pageAction="production-create-open">새 제작 업무</t:button>
    </t:pageHead>

    <section class="mb-8" aria-labelledby="productionNextTitle">
        <p class="text-sm font-bold text-accent-foreground">다음에 처리할 업무</p>
        <div class="mt-2 grid gap-4 border-l-4 border-primary bg-accent px-5 py-5 md:grid-cols-[minmax(0,1fr)_auto] md:items-center"><div><h2 id="productionNextTitle" class="text-lg font-bold" data-production-next-title>제작 업무를 확인하고 있어요</h2><p class="mt-1 text-sm leading-6 text-muted-foreground" data-production-next-message>잠시만 기다려 주세요.</p></div><span class="hidden" data-production-next-action><t:button pageAction="production-status-open" cssClass="w-full md:w-auto">업무 상태 변경</t:button></span></div>
    </section>

    <section class="mb-6 grid gap-4 border-y py-5 md:grid-cols-2 lg:grid-cols-4" aria-label="제작 업무 조회 기준">
        <div class="md:col-span-2">
            <label class="${label}" for="productionProject">공연 프로젝트</label>
            <select class="${input}" id="productionProject"></select>
        </div>
        <div>
            <label class="${label}" for="productionTeamFilter">담당 팀</label>
            <select class="${input}" id="productionTeamFilter"><option value="">전체 팀</option></select>
        </div>
        <label class="flex min-h-11 items-center gap-3 self-end border px-3 text-sm font-bold text-muted-foreground">
            <input id="productionOverdueOnly" type="checkbox" class="size-4 rounded border-input accent-primary">
            마감 지연만 보기
        </label>
    </section>

    <dl class="mb-6 grid grid-cols-2 divide-x border-y py-4 text-center sm:grid-cols-4" aria-label="프로젝트 제작 현황"><div class="px-2"><dt class="text-xs text-muted-foreground">전체 업무</dt><dd class="mt-1 text-lg font-bold tabular-nums" data-stat-value="production-total">0</dd></div><div class="px-2"><dt class="text-xs text-muted-foreground">완료</dt><dd class="mt-1 text-lg font-bold tabular-nums text-success" data-stat-value="production-completed">0</dd></div><div class="px-2"><dt class="text-xs text-muted-foreground">차단</dt><dd class="mt-1 text-lg font-bold tabular-nums text-destructive" data-stat-value="production-blocked">0</dd></div><div class="px-2"><dt class="text-xs text-muted-foreground">마감 지연</dt><dd class="mt-1 text-lg font-bold tabular-nums text-destructive" data-stat-value="production-overdue">0</dd></div></dl>

    <section class="mb-8 border-b pb-6" aria-labelledby="teamProgressTitle">
        <div class="flex flex-col gap-1 sm:flex-row sm:items-end sm:justify-between">
            <div><h2 id="teamProgressTitle" class="text-sm font-extrabold">팀별 진척</h2><p class="mt-1 text-xs text-muted-foreground">완료율과 차단 업무를 팀 단위로 확인합니다.</p></div>
            <span data-production-project-state><t:badge tone="neutral">프로젝트 확인 중</t:badge></span>
        </div>
        <div data-production-progress class="mt-4 divide-y border-y" aria-busy="true">
            <p class="text-sm text-muted-foreground">진척 정보를 불러오는 중입니다.</p>
        </div>
    </section>

    <div class="mb-5 flex gap-2 overflow-x-auto pb-1" aria-label="업무 상태 필터">
        <t:filterChip group="production-status" value="ALL" label="전체" active="true"/>
        <t:filterChip group="production-status" value="TODO" label="할 일"/>
        <t:filterChip group="production-status" value="IN_PROGRESS" label="진행 중"/>
        <t:filterChip group="production-status" value="REVIEW_REQUIRED" label="검토 필요"/>
        <t:filterChip group="production-status" value="BLOCKED" label="차단"/>
        <t:filterChip group="production-status" value="COMPLETED" label="완료"/>
    </div>

    <section aria-labelledby="productionTaskTitle">
        <div class="mb-3 flex items-center justify-between gap-3"><h2 id="productionTaskTitle" class="text-lg font-extrabold">제작 업무</h2><span data-production-count class="text-xs font-bold text-muted-foreground">0건</span></div>
        <p class="sr-only" data-production-status-message aria-live="polite"></p>
        <div data-production-tasks class="divide-y border-y" aria-busy="true">
            <p class="px-5 py-12 text-center text-sm text-muted-foreground">제작 업무를 불러오는 중입니다.</p>
        </div>
    </section>
    </main>

    <t:sheet id="productionTaskSheet" title="제작 업무 추가" description="담당 팀과 일정, 완료 기준이 드러나도록 작성해요.">
        <jsp:attribute name="footer">
            <t:button variant="outline" action="close-sheet">취소</t:button>
            <t:button pageAction="production-save">업무 저장</t:button>
        </jsp:attribute>
        <jsp:body>
            <form data-production-form class="flex flex-col gap-4">
                <input id="productionTaskId" type="hidden">
                <div>
                    <label class="${label}" for="productionTaskTeam">담당 팀 *</label>
                    <select class="${input}" id="productionTaskTeam" required></select>
                </div>
                <div>
                    <label class="${label}" for="productionTaskName">업무 제목 *</label>
                    <input class="${input}" id="productionTaskName" maxlength="200" required autocomplete="off" placeholder="예) 1막 무대 구조물 제작">
                </div>
                <div>
                    <label class="${label}" for="productionTaskDescription">설명</label>
                    <textarea class="min-h-28 w-full rounded-md border border-input bg-card px-3 py-2 text-base focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm" id="productionTaskDescription" placeholder="완료 기준, 필요한 협업과 참고 사항"></textarea>
                </div>
                <div class="grid gap-3 sm:grid-cols-2">
                    <div><label class="${label}" for="productionStartDate">시작일 *</label><input class="${input}" id="productionStartDate" type="date" required></div>
                    <div><label class="${label}" for="productionDueDate">마감일 *</label><input class="${input}" id="productionDueDate" type="date" required></div>
                </div>
            </form>
        </jsp:body>
    </t:sheet>

    <t:sheet id="productionStatusSheet" title="업무 상태 변경" description="상태 변경은 이력으로 남아요.">
        <jsp:attribute name="footer">
            <t:button variant="outline" action="close-sheet">취소</t:button>
            <t:button pageAction="production-status-save">상태 저장</t:button>
        </jsp:attribute>
        <jsp:body>
            <form data-production-status-form class="flex flex-col gap-4">
                <p data-production-status-task class="rounded-md bg-secondary px-4 py-3 text-sm font-bold"></p>
                <div><label class="${label}" for="productionTaskStatus">변경 상태 *</label><select class="${input}" id="productionTaskStatus" required><option value="TODO">할 일</option><option value="IN_PROGRESS">진행 중</option><option value="REVIEW_REQUIRED">검토 필요</option><option value="BLOCKED">차단</option><option value="COMPLETED">완료</option></select></div>
                <div data-production-blocked-field hidden><label class="${label}" for="productionBlockedReason">차단 사유 *</label><textarea class="min-h-24 w-full rounded-md border border-input bg-card px-3 py-2 text-sm focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20" id="productionBlockedReason" maxlength="500"></textarea></div>
                <div><label class="${label}" for="productionStatusComment">변경 메모</label><textarea class="min-h-20 w-full rounded-md border border-input bg-card px-3 py-2 text-sm focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20" id="productionStatusComment" maxlength="500" placeholder="인계나 확인이 필요한 내용을 남겨 주세요"></textarea></div>
            </form>
        </jsp:body>
    </t:sheet>

    <t:sheet id="productionHistorySheet" title="상태 변경 이력" description="업무가 언제, 누구에 의해 변경됐는지 확인해요."><jsp:body><div data-production-history class="flex flex-col gap-2"></div></jsp:body></t:sheet>
</t:layout>
