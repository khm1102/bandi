<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<t:layout title="회비" active="dues" role="${role}" scriptPath="dues/list">
    <c:choose>
        <c:when test="${role != 'admin'}">
            <t:pageHead title="내 회비 납부 현황" description="항목별 부과 금액과 납부 여부, 납부일을 확인합니다"/>
            <div class="mb-4 grid grid-cols-1 gap-2.5 md:grid-cols-3 md:gap-4">
                <t:statCard label="전체 부과액" value="—" unit="원" valueHook="my-total"/>
                <t:statCard label="납부 완료" value="—" unit="원" tone="success" valueHook="my-paid"/>
                <t:statCard label="미납" value="—" unit="원" tone="danger" valueHook="my-unpaid"/>
            </div>
            <div class="rounded-lg border bg-card">
                <t:dataTable caption="내 회비 납부 현황">
                    <thead><tr><th>회비 항목</th><th>기준 시기</th><th>금액</th><th>납부 상태</th><th>납부일</th><th>납부 기한</th></tr></thead>
                    <tbody data-my-fee-list><tr data-my-fee-state><td colspan="6" class="px-5 py-11 text-center"><b data-my-fee-state-title>회비 내역을 불러오는 중입니다</b><p class="mt-1 text-xs text-muted-foreground" data-my-fee-state-message>잠시만 기다려 주세요.</p><button type="button" class="mx-auto mt-4 hidden min-h-11 rounded-md border bg-card px-4 text-xs font-bold" data-my-fee-retry>다시 시도</button></td></tr></tbody>
                </t:dataTable>
            </div>
            <template data-my-fee-row-template><tr><td class="font-bold" data-my-fee-name></td><td data-my-fee-term></td><td data-my-fee-amount></td><td data-my-fee-status></td><td data-my-fee-paid-date></td><td data-my-fee-due-date></td></tr></template>
        </c:when>
        <c:otherwise>
            <t:pageHead title="회비 관리" description="회비 항목을 부과하고 멤버별 수납 상태를 일괄 처리합니다">
                <t:button variant="outline" pageAction="fee-edit-open" cssClass="hidden">초안 수정</t:button>
                <t:button pageAction="fee-open" cssClass="hidden" confirm="전체 활성 멤버에게 이 회비를 부과할까요? 부과 후 항목 정보는 수정할 수 없습니다." confirmAction="부과 시작">부과 시작</t:button>
                <t:button variant="outline" pageAction="fee-close" cssClass="hidden" confirm="현재 회비 부과를 마감할까요? 마감 후에도 수납 상태는 정정할 수 있습니다." confirmAction="부과 마감">부과 마감</t:button>
                <t:button variant="outline" pageAction="fee-cancel-open" cssClass="hidden text-destructive">현재 항목 취소</t:button>
                <t:button pageAction="fee-create-open">+ 회비 초안 추가</t:button>
            </t:pageHead>

            <div class="mb-4 flex max-w-full flex-wrap gap-1 rounded-lg border bg-secondary p-1" role="tablist" aria-label="회비 항목" data-fee-tabs></div>

            <div data-fee-workspace class="hidden">
                <div class="mb-4 flex flex-col gap-2 rounded-lg border bg-card px-4 py-4 md:flex-row md:items-center">
                    <div class="min-w-0 flex-1"><div class="flex flex-wrap items-center gap-2"><h2 class="text-base font-extrabold" data-fee-item-name></h2><span data-fee-item-status></span></div><p class="mt-1 text-sm text-muted-foreground" data-fee-item-description></p></div>
                    <p class="shrink-0 text-xs font-bold text-muted-foreground" data-fee-item-due></p>
                </div>
                <div class="mb-4 grid grid-cols-2 gap-2.5 lg:grid-cols-4 lg:gap-4">
                    <t:statCard label="항목 금액" value="—" unit="원" valueHook="fee-amount"/>
                    <t:statCard label="납부 완료" value="—" tone="success" valueHook="fee-paid"/>
                    <t:statCard label="미납" value="—" tone="danger" valueHook="fee-unpaid"/>
                    <t:statCard label="수납액" value="—" unit="원" valueHook="fee-collected"/>
                </div>
                <div class="mb-3 rounded-lg border bg-card px-4 py-3" data-fee-process-controls>
                    <div class="flex flex-wrap items-center gap-3">
                        <label class="flex min-h-11 cursor-pointer items-center gap-2 text-xs font-extrabold"><input type="checkbox" data-fee-all class="size-4 accent-primary"> 전체 선택</label>
                        <span class="text-xs text-muted-foreground">체크한 멤버에게 일괄 적용</span>
                        <div class="ml-auto flex flex-wrap gap-1.5"><t:button size="compact" pageAction="fee-pay">선택 납부 처리</t:button><t:button variant="outline" size="compact" pageAction="fee-unpay">선택 미납 처리</t:button></div>
                    </div>
                </div>
                <div class="rounded-lg border bg-card">
                    <t:dataTable caption="멤버별 회비 납부 현황">
                        <thead><tr><th class="w-11">선택</th><th>멤버</th><th>부과 금액</th><th>납부 상태</th><th>납부일</th><th class="text-right">이력</th></tr></thead>
                        <tbody data-fee-charge-list><tr data-fee-charge-state><td colspan="6" class="px-5 py-11 text-center"><b data-fee-charge-state-title>부과 명단을 불러오는 중입니다</b><p class="mt-1 text-xs text-muted-foreground" data-fee-charge-state-message>잠시만 기다려 주세요.</p></td></tr></tbody>
                    </t:dataTable>
                </div>
            </div>
            <div data-fee-empty class="hidden rounded-lg border bg-card"><t:emptyState title="등록된 회비 항목이 없습니다" message="회비 초안을 저장하고 내용을 확인한 뒤 전체 활성 멤버에게 부과할 수 있습니다."><t:button pageAction="fee-create-open">+ 회비 초안 추가</t:button></t:emptyState></div>

            <template data-fee-tab-template><button type="button" role="tab" aria-selected="false" data-fee-tab class="min-h-11 rounded-md px-3 text-xs font-bold text-muted-foreground transition-colors"></button></template>
            <template data-fee-charge-row-template><tr data-fee-charge-row><td><input type="checkbox" data-fee-person class="size-4 accent-primary"></td><td><span class="flex items-center gap-2"><span class="flex size-7 items-center justify-center rounded-full bg-primary text-xs font-black text-primary-foreground" data-fee-avatar></span><b data-fee-member-name></b></span></td><td data-fee-charged-amount></td><td data-fee-status></td><td data-fee-date></td><td class="text-right"><t:button variant="outline" size="compact" pageAction="fee-history-open" cssClass="min-h-9">이력</t:button></td></tr></template>

            <t:modal id="feeModal" title="회비 초안 추가" description="초안으로 저장한 뒤 내용을 확인하고 부과를 시작합니다.">
                <jsp:attribute name="footer"><t:button variant="outline" action="close-modal">취소</t:button><t:button pageAction="fee-save"><span data-fee-submit-label>초안 저장</span></t:button></jsp:attribute>
                <jsp:body><div class="flex flex-col gap-3"><div><label class="${label}" for="feeName">항목명 <span class="text-accent-foreground">*</span></label><input class="${input}" id="feeName" type="text" maxlength="150" placeholder="예) 7월 회식비"></div><div><label class="${label}" for="feeDescription">설명</label><textarea class="min-h-20 w-full resize-y rounded-md border border-input bg-card px-3 py-2.5 text-sm" id="feeDescription"></textarea></div><div class="grid grid-cols-1 gap-3 md:grid-cols-2"><div><label class="${label}" for="feeAmt">금액 <span class="text-accent-foreground">*</span></label><input class="${input}" id="feeAmt" type="number" min="1" step="1" inputmode="numeric"></div><div><label class="${label}" for="feeDue">납부 기한 <span class="text-accent-foreground">*</span></label><input class="${input}" id="feeDue" type="date"></div></div><div><label class="${label}" for="feeTerm">기준 시기</label><input class="${input}" id="feeTerm" type="text" maxlength="20" placeholder="예) 2026-1"></div><p class="hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-xs text-destructive" data-fee-form-error role="alert"></p></div></jsp:body>
            </t:modal>

            <t:modal id="feeCancelModal" title="회비 항목 취소" description="부과 기록은 삭제하지 않고 취소 상태와 사유를 남깁니다.">
                <jsp:attribute name="footer"><t:button variant="outline" action="close-modal">닫기</t:button><t:button variant="danger" pageAction="fee-cancel">항목 취소</t:button></jsp:attribute>
                <jsp:body><label class="${label}" for="feeCancelReason">취소 사유 <span class="text-accent-foreground">*</span></label><textarea class="min-h-28 w-full resize-y rounded-md border border-input bg-card px-3 py-2.5 text-sm" id="feeCancelReason" maxlength="500"></textarea><p class="mt-2 hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-xs text-destructive" data-fee-cancel-error role="alert"></p></jsp:body>
            </t:modal>

            <t:modal id="feeHistoryModal" title="수납 상태 변경 이력" description="처리 상태와 담당자, 변경 사유를 시간순으로 확인합니다.">
                <jsp:attribute name="footer"><t:button variant="outline" action="close-modal">닫기</t:button></jsp:attribute>
                <jsp:body><p class="mb-3 text-sm font-extrabold" data-fee-history-member></p><div class="flex flex-col gap-2" data-fee-history></div></jsp:body>
            </t:modal>
        </c:otherwise>
    </c:choose>
</t:layout>
