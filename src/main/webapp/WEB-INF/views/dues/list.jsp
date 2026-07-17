<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<t:layout title="회비" active="dues" role="${role}" scriptPath="dues/list">
    <c:choose>
        <%-- ───── 부원/팀장 뷰: 내 납부 현황 ───── --%>
        <c:when test="${role != 'admin'}">
            <c:choose>
                <c:when test="${role == 'leader'}"><c:set var="who" value="26-1기 무대팀"/></c:when>
                <c:otherwise><c:set var="who" value="26-2기 배우연출팀"/></c:otherwise>
            </c:choose>
            <t:pageHead title="내 회비 납부 현황" description="${who} · 항목별 납부 여부와 납부일을 확인할 수 있습니다"/>
            <div class="mb-4 grid grid-cols-3 gap-4">
                <t:statCard label="전체 항목" value="3"/>
                <t:statCard label="납부 완료" value="${role == 'leader' ? '2' : '1'}" tone="success"/>
                <t:statCard label="미납" value="${role == 'leader' ? '1' : '2'}" tone="danger"/>
            </div>
            <div class="rounded-lg border bg-card">
                <t:dataTable caption="내 회비 납부 현황">
                    <thead><tr><th>회비 항목</th><th>기준 시기</th><th>금액</th><th>납부 여부</th><th>납부일</th></tr></thead>
                    <tbody>
                    <tr><td class="font-bold">동아리 가입 회비</td><td>2025-1학기</td><td>30,000원</td><td><t:badge tone="success">납부 완료</t:badge></td><td>${role == 'leader' ? '05/03' : '05/05'}</td></tr>
                    <c:choose>
                        <c:when test="${role == 'leader'}">
                            <tr><td class="font-bold">6월 정기 회식비</td><td>2025-06</td><td>15,000원</td><td><t:badge tone="success">납부 완료</t:badge></td><td>06/11</td></tr>
                        </c:when>
                        <c:otherwise>
                            <tr><td class="font-bold">6월 정기 회식비</td><td>2025-06</td><td>15,000원</td><td><t:badge tone="warning">미납</t:badge></td><td class="text-muted-foreground">—</td></tr>
                        </c:otherwise>
                    </c:choose>
                    <tr><td class="font-bold">공연 추가 납부</td><td>2025-06</td><td>10,000원</td><td><t:badge tone="warning">미납</t:badge></td><td class="text-muted-foreground">—</td></tr>
                    </tbody>
                </t:dataTable>
            </div>
        </c:when>

        <%-- ───── 운영진 뷰: 회비 관리 ───── --%>
        <c:otherwise>
            <t:pageHead title="회비 관리" description="부원을 선택하여 납부·미납 처리를 한 번에 적용할 수 있습니다">
                <t:button variant="outline" pageAction="fee-delete" confirm="현재 회비 항목과 화면에 표시된 납부 기록을 삭제할까요?" confirmAction="항목 삭제" cssClass="text-destructive">현재 항목 삭제</t:button>
                <t:button openModal="feeModal">+ 회비 항목 추가</t:button>
            </t:pageHead>

            <div class="mb-4 inline-flex max-w-full flex-wrap rounded-lg border bg-secondary p-0.5" role="tablist" aria-label="회비 항목">
                <button type="button" role="tab" aria-selected="true" data-fee-tab data-amount="30000" data-paid-rows="0:05/02,1:05/03,2:05/05,5:05/06" class="min-h-11 rounded-md border bg-card px-3 text-xs font-bold text-foreground">동아리 가입 회비</button>
                <button type="button" role="tab" aria-selected="false" data-fee-tab data-amount="15000" data-paid-rows="0:06/11,1:06/11,3:06/12,5:06/12" class="min-h-11 rounded-md px-3 text-xs font-bold text-muted-foreground transition-colors">6월 정기 회식비</button>
                <button type="button" role="tab" aria-selected="false" data-fee-tab data-amount="10000" data-paid-rows="0:06/19,3:06/19,4:06/20" class="min-h-11 rounded-md px-3 text-xs font-bold text-muted-foreground transition-colors">공연 추가 납부</button>
            </div>

            <div data-fee-workspace>
                <div class="mb-4 grid grid-cols-2 gap-4 lg:grid-cols-4">
                    <t:statCard label="항목 금액" value="30,000" unit="원" valueHook="fee-amount"/>
                    <t:statCard label="납부 완료" value="4" tone="success" valueHook="fee-paid"/>
                    <t:statCard label="미납" value="2" tone="danger" valueHook="fee-unpaid"/>
                    <t:statCard label="수납액" value="120,000" unit="원" valueHook="fee-collected"/>
                </div>

                <div class="mb-3 rounded-lg border bg-card px-4 py-3">
                    <div class="flex flex-wrap items-center gap-3">
                        <label class="flex cursor-pointer items-center gap-2 text-xs font-extrabold"><input type="checkbox" data-fee-all class="size-4 accent-primary"> 전체 선택</label>
                        <span class="text-xs text-muted-foreground">체크한 부원에게 일괄 적용</span>
                        <div class="ml-auto flex gap-1.5">
                            <t:button size="compact" pageAction="fee-pay">선택 납부 처리</t:button>
                            <t:button variant="outline" size="compact" pageAction="fee-unpay">선택 미납 처리</t:button>
                        </div>
                    </div>
                </div>

                <div class="rounded-lg border bg-card">
                    <t:dataTable caption="부원별 회비 납부 현황">
                        <thead><tr><th class="w-11">선택</th><th>부원</th><th>기수</th><th>소속팀</th><th>납부 상태</th><th>납부일</th></tr></thead>
                        <tbody>
                    <tr data-fee-row><td><input type="checkbox" data-fee-person class="size-4 accent-primary" aria-label="이서준 선택"></td><td><span class="flex items-center gap-2"><span class="flex size-7 items-center justify-center rounded-full bg-primary text-xs font-black text-primary-foreground">LS</span><b>이서준</b></span></td><td>26-1기</td><td>운영진</td><td data-fee-status><t:badge tone="success">납부 완료</t:badge></td><td data-fee-date>05/02</td></tr>
                    <tr data-fee-row><td><input type="checkbox" data-fee-person class="size-4 accent-primary" aria-label="정도윤 선택"></td><td><span class="flex items-center gap-2"><span class="flex size-7 items-center justify-center rounded-full bg-info text-xs font-black text-white">JD</span><b>정도윤</b></span></td><td>26-1기</td><td>무대</td><td data-fee-status><t:badge tone="success">납부 완료</t:badge></td><td data-fee-date>05/03</td></tr>
                    <tr data-fee-row><td><input type="checkbox" data-fee-person class="size-4 accent-primary" aria-label="김하늘 선택"></td><td><span class="flex items-center gap-2"><span class="flex size-7 items-center justify-center rounded-full bg-sidebar-accent text-xs font-black text-white">KH</span><b>김하늘</b></span></td><td>26-2기</td><td>배우연출</td><td data-fee-status><t:badge tone="success">납부 완료</t:badge></td><td data-fee-date>05/05</td></tr>
                    <tr data-fee-row><td><input type="checkbox" data-fee-person class="size-4 accent-primary" aria-label="박서연 선택"></td><td><span class="flex items-center gap-2"><span class="flex size-7 items-center justify-center rounded-full bg-accent-foreground text-xs font-black text-white">PS</span><b>박서연</b></span></td><td>26-2기</td><td>오퍼</td><td data-fee-status><t:badge tone="warning">미납</t:badge></td><td class="text-muted-foreground" data-fee-date>—</td></tr>
                    <tr data-fee-row><td><input type="checkbox" data-fee-person class="size-4 accent-primary" aria-label="한지우 선택"></td><td><span class="flex items-center gap-2"><span class="flex size-7 items-center justify-center rounded-full bg-success text-xs font-black text-white">HJ</span><b>한지우</b></span></td><td>26-2기</td><td>디자인</td><td data-fee-status><t:badge tone="warning">미납</t:badge></td><td class="text-muted-foreground" data-fee-date>—</td></tr>
                    <tr data-fee-row><td><input type="checkbox" data-fee-person class="size-4 accent-primary" aria-label="최민준 선택"></td><td><span class="flex items-center gap-2"><span class="flex size-7 items-center justify-center rounded-full bg-warning text-xs font-black text-white">CM</span><b>최민준</b></span></td><td>26-1기</td><td>영상</td><td data-fee-status><t:badge tone="success">납부 완료</t:badge></td><td data-fee-date>05/06</td></tr>
                        </tbody>
                    </t:dataTable>
                </div>
            </div>

            <div data-fee-empty class="hidden rounded-lg border bg-card">
                <t:emptyState title="등록된 회비 항목이 없습니다" message="새 회비 항목을 추가하면 부원별 납부 현황을 관리할 수 있습니다.">
                    <t:button openModal="feeModal">+ 회비 항목 추가</t:button>
                </t:emptyState>
            </div>

            <t:modal id="feeModal" title="회비 항목 추가" description="가입비·회식비·추가 납부 등 새 항목을 만듭니다.">
                <jsp:attribute name="footer">
                    <t:button variant="outline" action="close-modal">취소</t:button>
                    <t:button pageAction="fee-add">항목 추가</t:button>
                </jsp:attribute>
                <jsp:body>
                    <div class="flex flex-col gap-3">
                        <div><label class="${label}" for="feeName">항목명 <span aria-hidden="true">*</span></label><input class="${input}" id="feeName" type="text" placeholder="예) 7월 회식비" required aria-required="true"></div>
                        <div class="grid grid-cols-2 gap-2.5">
                            <div><label class="${label}" for="feeAmt">금액 (원)</label><input class="${input}" id="feeAmt" type="number" inputmode="numeric" value="15000" min="0" step="1"></div>
                            <div><label class="${label}" for="feeWhen">기준 시기</label><input class="${input}" id="feeWhen" type="month"></div>
                        </div>
                    </div>
                </jsp:body>
            </t:modal>
        </c:otherwise>
    </c:choose>
</t:layout>
