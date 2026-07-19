<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<t:layout title="팀별 제작 진행" active="production" role="${role}" scriptPath="production/index">
    <t:pageHead title="팀별 제작 진행" description="학기 공연의 팀별 업무, 마감과 차단 상태를 한곳에서 관리합니다">
        <t:button pageAction="production-create-open">업무 추가</t:button>
    </t:pageHead>

    <section class="mb-4 grid gap-4 rounded-lg border bg-card p-5 md:grid-cols-2 lg:grid-cols-4" aria-label="제작 업무 조회 기준">
        <div class="md:col-span-2">
            <label class="${label}" for="productionProject">공연 프로젝트</label>
            <select class="${input}" id="productionProject"></select>
        </div>
        <div>
            <label class="${label}" for="productionTeamFilter">담당 팀</label>
            <select class="${input}" id="productionTeamFilter"><option value="">전체 팀</option></select>
        </div>
        <label class="flex min-h-11 items-center gap-3 self-end rounded-md border px-3 text-sm font-bold text-muted-foreground">
            <input id="productionOverdueOnly" type="checkbox" class="size-4 rounded border-input accent-primary">
            마감 지연만 보기
        </label>
    </section>

    <section class="mb-4 grid gap-3 sm:grid-cols-2 lg:grid-cols-4" aria-label="프로젝트 제작 현황">
        <t:statCard label="전체 업무" value="0" valueHook="production-total"/>
        <t:statCard label="완료" value="0" tone="success" valueHook="production-completed"/>
        <t:statCard label="차단" value="0" tone="danger" valueHook="production-blocked"/>
        <t:statCard label="마감 지연" value="0" tone="danger" valueHook="production-overdue"/>
    </section>

    <section class="mb-5 rounded-lg border bg-card p-5" aria-labelledby="teamProgressTitle">
        <div class="flex flex-col gap-1 sm:flex-row sm:items-end sm:justify-between">
            <div><h2 id="teamProgressTitle" class="text-sm font-extrabold">팀별 진척</h2><p class="mt-1 text-xs text-muted-foreground">완료율과 차단 업무를 팀 단위로 확인합니다.</p></div>
            <span data-production-project-state><t:badge tone="neutral">프로젝트 확인 중</t:badge></span>
        </div>
        <div data-production-progress class="mt-4 grid gap-3 md:grid-cols-2 lg:grid-cols-3" aria-live="polite" aria-busy="true">
            <p class="text-sm text-muted-foreground">진척 정보를 불러오는 중입니다.</p>
        </div>
    </section>

    <div class="mb-4 flex flex-wrap gap-2" aria-label="업무 상태 필터">
        <t:filterChip group="production-status" value="ALL" label="전체" active="true"/>
        <t:filterChip group="production-status" value="TODO" label="할 일"/>
        <t:filterChip group="production-status" value="IN_PROGRESS" label="진행 중"/>
        <t:filterChip group="production-status" value="REVIEW_REQUIRED" label="검토 필요"/>
        <t:filterChip group="production-status" value="BLOCKED" label="차단"/>
        <t:filterChip group="production-status" value="COMPLETED" label="완료"/>
    </div>

    <section aria-labelledby="productionTaskTitle">
        <div class="mb-3 flex items-center justify-between gap-3"><h2 id="productionTaskTitle" class="text-lg font-extrabold">제작 업무</h2><span data-production-count class="text-xs font-bold text-muted-foreground">0건</span></div>
        <div data-production-tasks class="grid gap-4 md:grid-cols-2" aria-live="polite" aria-busy="true">
            <p class="rounded-lg border bg-card px-5 py-12 text-center text-sm text-muted-foreground md:col-span-2">제작 업무를 불러오는 중입니다.</p>
        </div>
    </section>

    <t:modal id="productionTaskModal" title="제작 업무 추가" description="담당 팀과 일정, 완료 기준이 드러나도록 작성합니다.">
        <jsp:attribute name="footer">
            <t:button variant="outline" action="close-modal">취소</t:button>
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
    </t:modal>

    <t:modal id="productionStatusModal" title="업무 상태 변경" description="상태 변경은 이력으로 남습니다.">
        <jsp:attribute name="footer">
            <t:button variant="outline" action="close-modal">취소</t:button>
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
    </t:modal>

    <t:modal id="productionHistoryModal" title="상태 변경 이력" description="업무가 언제, 누구에 의해 변경됐는지 확인합니다.">
        <jsp:body><div data-production-history class="flex flex-col gap-2" aria-live="polite"></div></jsp:body>
    </t:modal>
</t:layout>
