<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-bold text-muted-foreground"/>
<t:layout title="회비" active="dues" role="${role}" scriptPath="dues/list">
    <main class="w-full">
    <c:choose>
        <c:when test="${role != 'admin'}">
            <t:pageHead title="내 회비" description="아직 내지 않은 회비와 납부 기한부터 확인해요"/>

            <section class="mb-8" aria-labelledby="myFeeNextTitle">
                <p class="text-sm font-bold text-accent-foreground">다음에 확인할 회비</p>
                <div class="mt-2 border-l-4 border-primary bg-accent px-5 py-5">
                    <h2 id="myFeeNextTitle" class="text-lg font-bold" data-my-fee-next-title>회비 내역을 확인하고 있어요</h2>
                    <p class="mt-1 text-sm leading-6 text-muted-foreground" data-my-fee-next-message>잠시만 기다려 주세요.</p>
                </div>
            </section>

            <dl class="mb-6 grid grid-cols-3 divide-x border-y py-4 text-center" aria-label="내 회비 요약">
                <div class="px-2"><dt class="text-xs text-muted-foreground">전체</dt><dd class="mt-1 text-base font-bold tabular-nums"><span data-stat-value="my-total">—</span>원</dd></div>
                <div class="px-2"><dt class="text-xs text-muted-foreground">납부</dt><dd class="mt-1 text-base font-bold tabular-nums text-success"><span data-stat-value="my-paid">—</span>원</dd></div>
                <div class="px-2"><dt class="text-xs text-muted-foreground">미납</dt><dd class="mt-1 text-base font-bold tabular-nums text-destructive"><span data-stat-value="my-unpaid">—</span>원</dd></div>
            </dl>

            <div class="border-y">
                <div data-my-fee-list></div>
                <div data-my-fee-state class="px-5 py-12 text-center"><b data-my-fee-state-title>회비 내역을 불러오는 중입니다</b><p class="mt-1 text-sm text-muted-foreground" data-my-fee-state-message>잠시만 기다려 주세요.</p><button type="button" class="mx-auto mt-4 hidden min-h-11 rounded-md border bg-card px-4 text-sm font-bold" data-my-fee-retry>다시 시도</button></div>
            </div>
            <template data-my-fee-row-template>
                <article data-my-fee-row class="grid gap-3 border-b px-4 py-5 last:border-b-0 sm:grid-cols-[minmax(0,1fr)_auto] sm:items-center md:px-5">
                    <div class="min-w-0"><div class="flex flex-wrap items-center gap-2"><h2 class="text-base font-bold" data-my-fee-name></h2><span data-my-fee-status></span></div><p class="mt-1 text-xs text-muted-foreground"><span data-my-fee-term></span> · 납부 기한 <span data-my-fee-due-date></span></p></div>
                    <div class="sm:text-right"><b class="text-base tabular-nums" data-my-fee-amount></b><p class="mt-1 text-xs text-muted-foreground">납부일 <span data-my-fee-paid-date></span></p></div>
                </article>
            </template>
        </c:when>
        <c:otherwise>
            <t:pageHead title="회비 관리" description="현재 회비의 미납자를 확인하고 수납 상태를 기록해요">
                <t:button variant="outline" pageAction="fee-edit-open" cssClass="hidden">초안 수정</t:button>
                <t:button pageAction="fee-open" cssClass="hidden" confirm="전체 활성 멤버에게 이 회비를 부과할까요? 부과 후 항목 정보는 수정할 수 없습니다." confirmAction="부과 시작">전체 멤버에게 부과</t:button>
                <t:button variant="outline" pageAction="fee-close" cssClass="hidden" confirm="현재 회비 부과를 마감할까요? 마감 후에도 수납 상태는 정정할 수 있습니다." confirmAction="부과 마감">부과 마감</t:button>
                <t:button variant="outline" pageAction="fee-cancel-open" cssClass="hidden text-destructive">현재 항목 취소</t:button>
                <t:button variant="outline" pageAction="fee-create-open">새 회비 초안</t:button>
            </t:pageHead>

            <div class="mb-6 flex max-w-full gap-1 overflow-x-auto border-b pb-1" role="tablist" aria-label="회비 항목" data-fee-tabs></div>

            <div data-fee-workspace class="hidden">
                <section class="mb-6 border-b pb-5">
                    <div class="flex flex-col gap-2 md:flex-row md:items-start"><div class="min-w-0 flex-1"><div class="flex flex-wrap items-center gap-2"><h2 class="text-xl font-bold" data-fee-item-name></h2><span data-fee-item-status></span></div><p class="mt-1 text-sm leading-6 text-muted-foreground" data-fee-item-description></p></div><p class="shrink-0 text-sm font-bold text-muted-foreground" data-fee-item-due></p></div>
                </section>

                <dl class="mb-6 grid grid-cols-2 divide-x border-y py-4 text-center sm:grid-cols-4" aria-label="선택한 회비 요약">
                    <div class="px-2"><dt class="text-xs text-muted-foreground">항목 금액</dt><dd class="mt-1 text-base font-bold tabular-nums"><span data-stat-value="fee-amount">—</span>원</dd></div>
                    <div class="px-2"><dt class="text-xs text-muted-foreground">납부 완료</dt><dd class="mt-1 text-base font-bold tabular-nums text-success" data-stat-value="fee-paid">—</dd></div>
                    <div class="px-2"><dt class="text-xs text-muted-foreground">미납</dt><dd class="mt-1 text-base font-bold tabular-nums text-destructive" data-stat-value="fee-unpaid">—</dd></div>
                    <div class="px-2"><dt class="text-xs text-muted-foreground">수납액</dt><dd class="mt-1 text-base font-bold tabular-nums"><span data-stat-value="fee-collected">—</span>원</dd></div>
                </dl>

                <div class="mb-4 border-y px-4 py-3" data-fee-process-controls>
                    <div class="flex flex-col gap-3 sm:flex-row sm:items-center">
                        <label class="flex min-h-11 cursor-pointer items-center gap-2 text-sm font-bold"><input type="checkbox" data-fee-all class="size-4 accent-primary"> 현재 명단 전체 선택</label>
                        <div class="grid grid-cols-2 gap-2 sm:ml-auto"><t:button size="compact" pageAction="fee-pay">선택 납부 기록</t:button><t:button variant="outline" size="compact" pageAction="fee-unpay">선택 미납 정정</t:button></div>
                    </div>
                </div>
                <div class="border-y">
                    <div data-fee-charge-list></div>
                    <div data-fee-charge-state class="px-5 py-12 text-center"><b data-fee-charge-state-title>부과 명단을 불러오는 중입니다</b><p class="mt-1 text-sm text-muted-foreground" data-fee-charge-state-message>잠시만 기다려 주세요.</p></div>
                </div>
            </div>
            <div data-fee-empty class="hidden border-y"><t:emptyState title="등록된 회비 항목이 없습니다" message="회비 초안을 저장하고 내용을 확인한 뒤 전체 활성 멤버에게 부과할 수 있어요."><t:button pageAction="fee-create-open">첫 회비 초안 만들기</t:button></t:emptyState></div>

            <template data-fee-tab-template><button type="button" role="tab" aria-selected="false" data-fee-tab class="min-h-11 shrink-0 border-b-2 border-transparent px-3 text-sm font-bold text-muted-foreground transition-colors"></button></template>
            <template data-fee-charge-row-template>
                <article data-fee-charge-row class="grid grid-cols-[auto_minmax(0,1fr)] gap-x-3 gap-y-2 border-b px-4 py-4 last:border-b-0 md:grid-cols-[auto_minmax(0,1fr)_auto] md:items-center md:px-5">
                    <input type="checkbox" data-fee-person class="mt-1 size-4 accent-primary">
                    <div class="min-w-0"><div class="flex flex-wrap items-center gap-2"><span class="flex size-7 items-center justify-center rounded-full bg-primary text-xs font-extrabold text-primary-foreground" data-fee-avatar></span><b data-fee-member-name></b><span data-fee-status></span></div><p class="mt-1 text-xs text-muted-foreground"><span data-fee-charged-amount></span> · 납부일 <span data-fee-date></span></p></div>
                    <t:button variant="outline" size="compact" pageAction="fee-history-open" cssClass="col-start-2 w-full md:col-start-auto md:w-auto">변경 이력</t:button>
                </article>
            </template>

            <t:sheet id="feeSheet" title="회비 초안 추가" description="초안으로 저장한 뒤 내용을 확인하고 부과를 시작해요." presentation="form">
                <jsp:attribute name="footer"><t:button variant="outline" action="close-sheet">취소</t:button><t:button pageAction="fee-save"><span data-fee-submit-label>초안 저장</span></t:button></jsp:attribute>
                <jsp:body><div class="flex flex-col gap-4"><div><label class="${label}" for="feeName">항목명 <span class="text-accent-foreground">*</span></label><input class="${input}" id="feeName" type="text" maxlength="150" placeholder="예) 7월 회식비"></div><div><label class="${label}" for="feeDescription">설명</label><textarea class="min-h-24 w-full resize-y rounded-md border border-input bg-card px-3 py-2.5 text-sm" id="feeDescription"></textarea></div><div class="grid grid-cols-1 gap-4 sm:grid-cols-2"><div><label class="${label}" for="feeAmt">금액 <span class="text-accent-foreground">*</span></label><input class="${input}" id="feeAmt" type="number" min="1" step="1" inputmode="numeric"></div><div><label class="${label}" for="feeDue">납부 기한 <span class="text-accent-foreground">*</span></label><input class="${input}" id="feeDue" type="date"></div></div><div><label class="${label}" for="feeTerm">기준 시기</label><input class="${input}" id="feeTerm" type="text" maxlength="20" placeholder="예) 2026-1"></div><p class="hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-sm text-destructive" data-fee-form-error role="alert"></p></div></jsp:body>
            </t:sheet>

            <t:sheet id="feeCancelSheet" title="회비 항목 취소" description="부과 기록은 삭제하지 않고 취소 상태와 사유를 남겨요." presentation="form">
                <jsp:attribute name="footer"><t:button variant="outline" action="close-sheet">닫기</t:button><t:button variant="danger" pageAction="fee-cancel">회비 항목 취소</t:button></jsp:attribute>
                <jsp:body><label class="${label}" for="feeCancelReason">취소 사유 <span class="text-accent-foreground">*</span></label><textarea class="min-h-32 w-full resize-y rounded-md border border-input bg-card px-3 py-2.5 text-sm" id="feeCancelReason" maxlength="500"></textarea><p class="mt-2 hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-sm text-destructive" data-fee-cancel-error role="alert"></p></jsp:body>
            </t:sheet>

            <t:sheet id="feeHistorySheet" title="수납 상태 변경 이력" description="처리 상태와 담당자, 변경 사유를 시간순으로 확인해요."><jsp:body><p class="mb-3 text-sm font-bold" data-fee-history-member></p><div class="flex flex-col gap-2" data-fee-history></div></jsp:body></t:sheet>
        </c:otherwise>
    </c:choose>
    </main>
</t:layout>
