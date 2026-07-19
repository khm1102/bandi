<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-bold text-muted-foreground"/>
<c:set var="canManage" value="${role eq 'admin' or role eq 'leader'}"/>
<t:layout title="체크리스트" active="checklist" role="${role}" scriptPath="checklist/index">
    <main class="mx-auto w-full max-w-5xl">
        <t:pageHead title="공연 체크리스트" description="이번 공연에서 아직 끝내지 못한 준비부터 처리해요">
            <c:if test="${canManage}"><t:button pageAction="checklist-create-open">준비 항목 추가</t:button></c:if>
        </t:pageHead>

        <section class="grid gap-4 border-y py-5 md:grid-cols-3" aria-label="체크리스트 조회 기준">
            <div>
                <label class="${label}" for="checkProject">공연 프로젝트</label>
                <select class="${input}" id="checkProject" data-check-project></select>
            </div>
            <div>
                <label class="${label}" for="checkScope">준비 범위</label>
                <select class="${input}" id="checkScope" data-check-scope><option value="PROJECT">프로젝트 공통</option><option value="ROUND">공연 회차</option></select>
            </div>
            <div data-check-round-filter hidden>
                <label class="${label}" for="checkRound">공연 회차</label>
                <select class="${input}" id="checkRound" data-check-round></select>
            </div>
        </section>

        <section class="my-8" aria-labelledby="checklistNextTitle">
            <p class="text-sm font-bold text-accent-foreground">다음에 할 일</p>
            <div class="mt-2 grid gap-4 border-l-4 border-primary bg-accent px-5 py-5 md:grid-cols-[minmax(0,1fr)_auto] md:items-center">
                <div>
                    <h2 id="checklistNextTitle" class="text-lg font-bold" data-checklist-next-title>준비 항목을 확인하고 있어요</h2>
                    <p class="mt-1 text-sm leading-6 text-muted-foreground" data-checklist-next-message>잠시만 기다려 주세요.</p>
                </div>
                <span class="hidden" data-checklist-next-action><t:button pageAction="checklist-toggle" cssClass="w-full md:w-auto">완료로 기록</t:button></span>
            </div>
        </section>

        <section class="mb-6 border-b pb-6" aria-label="전체 완료율">
            <div class="mb-2 flex items-center gap-3">
                <b class="text-sm">전체 준비 진행률</b>
                <span data-checklist-summary><t:badge tone="warning">준비 0%</t:badge></span>
                <b class="ml-auto text-sm text-accent-foreground tabular-nums" data-checklist-count>0 / 0</b>
            </div>
            <progress data-checklist-progress class="h-2 w-full overflow-hidden rounded-full accent-primary" value="0" max="1" aria-label="전체 체크리스트 완료율">0%</progress>
        </section>

        <div class="mb-5 flex flex-col gap-3 sm:flex-row sm:items-center" aria-label="완료 상태 필터">
            <div class="flex gap-2 overflow-x-auto pb-1">
                <t:filterChip group="checklist-status" value="ALL" label="전체" active="true"/>
                <t:filterChip group="checklist-status" value="PENDING" label="미완료"/>
                <t:filterChip group="checklist-status" value="COMPLETED" label="완료"/>
            </div>
            <label class="flex min-h-11 items-center gap-2 text-sm font-bold text-muted-foreground sm:ml-auto">
                <input data-required-only type="checkbox" class="size-4 rounded border-input accent-primary"> 필수 항목만
            </label>
        </div>

        <p class="sr-only" data-checklist-status-message aria-live="polite"></p>
        <div data-checklist-region class="divide-y border-y" aria-busy="true">
            <div class="px-5 py-12 text-center text-sm text-muted-foreground">체크리스트를 불러오는 중입니다.</div>
        </div>
    </main>

    <c:if test="${canManage}">
        <t:sheet id="checkSheet" title="체크리스트 항목 추가" description="담당 팀과 적용 범위를 지정해요.">
            <jsp:attribute name="footer"><t:button variant="outline" action="close-sheet">취소</t:button><t:button pageAction="checklist-save">준비 항목 저장</t:button></jsp:attribute>
            <jsp:body>
                <form data-checklist-form class="flex flex-col gap-4">
                    <input id="checkItemId" type="hidden">
                    <div><label class="${label}" for="checkTeam">담당 팀 *</label><select class="${input}" id="checkTeam" required></select></div>
                    <div class="grid gap-4 sm:grid-cols-2">
                        <div><label class="${label}" for="checkItemScope">적용 범위 *</label><select class="${input}" id="checkItemScope" required><option value="PROJECT">프로젝트 공통</option><option value="ROUND">공연 회차</option></select></div>
                        <div data-check-item-round-field hidden><label class="${label}" for="checkItemRound">공연 회차 *</label><select class="${input}" id="checkItemRound"></select></div>
                    </div>
                    <div><label class="${label}" for="checkContent">준비 항목 *</label><input class="${input}" id="checkContent" required maxlength="500" autocomplete="off" placeholder="예) 무대 전환 리허설 완료"></div>
                    <div><label class="${label}" for="checkDisplayOrder">표시 순서 *</label><input class="${input}" id="checkDisplayOrder" type="number" min="0" step="1" value="0" required></div>
                    <label class="flex min-h-11 items-center gap-3 border px-4 text-sm font-bold"><input id="checkRequired" type="checkbox" class="size-4 rounded border-input accent-primary"> 공연 전 반드시 완료해야 하는 필수 항목</label>
                </form>
            </jsp:body>
        </t:sheet>
    </c:if>

    <t:sheet id="checkHistorySheet" title="완료 변경 이력" description="누가 언제 완료 상태를 바꿨는지 확인해요."><jsp:body><div data-check-history class="flex flex-col gap-2"></div></jsp:body></t:sheet>
</t:layout>
