<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="btnPrimary" value="inline-flex h-9 items-center justify-center gap-1.5 rounded-md bg-primary px-4 text-sm font-bold text-primary-foreground transition-colors hover:bg-primary-strong hover:text-white"/>
<c:set var="btnOutline" value="inline-flex h-9 items-center justify-center gap-1.5 rounded-md border bg-card px-4 text-sm font-bold transition-colors hover:bg-secondary"/>
<c:set var="btnSm" value="inline-flex h-8 items-center rounded-md border bg-card px-3 text-xs font-bold transition-colors hover:bg-secondary"/>
<c:set var="btnSmPrimary" value="inline-flex h-8 items-center rounded-md bg-primary px-3 text-xs font-bold text-primary-foreground transition-colors hover:bg-primary-strong hover:text-white"/>
<c:set var="th" value="whitespace-nowrap bg-secondary px-4 py-3 text-left text-xs font-extrabold text-muted-foreground"/>
<c:set var="td" value="px-4 py-3 text-sm"/>
<c:set var="input" value="h-10 w-full rounded-md border border-input bg-card px-3 text-sm transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<t:layout title="회비" active="dues" role="${role}">
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
            <div class="overflow-x-auto rounded-lg border bg-card">
                <table class="w-full border-collapse text-left">
                    <thead><tr><th class="${th}">회비 항목</th><th class="${th}">기준 시기</th><th class="${th}">금액</th><th class="${th}">납부 여부</th><th class="${th}">납부일</th></tr></thead>
                    <tbody class="divide-y">
                    <tr class="transition-colors hover:bg-secondary/50"><td class="${td} font-bold">동아리 가입 회비</td><td class="${td}">2025-1학기</td><td class="${td}">30,000원</td><td class="${td}"><t:badge tone="success">납부 완료</t:badge></td><td class="${td}">${role == 'leader' ? '05/03' : '05/05'}</td></tr>
                    <c:choose>
                        <c:when test="${role == 'leader'}">
                            <tr class="transition-colors hover:bg-secondary/50"><td class="${td} font-bold">6월 정기 회식비</td><td class="${td}">2025-06</td><td class="${td}">15,000원</td><td class="${td}"><t:badge tone="success">납부 완료</t:badge></td><td class="${td}">06/11</td></tr>
                        </c:when>
                        <c:otherwise>
                            <tr class="transition-colors hover:bg-secondary/50"><td class="${td} font-bold">6월 정기 회식비</td><td class="${td}">2025-06</td><td class="${td}">15,000원</td><td class="${td}"><t:badge tone="warning">미납</t:badge></td><td class="${td} text-muted-foreground">—</td></tr>
                        </c:otherwise>
                    </c:choose>
                    <tr class="transition-colors hover:bg-secondary/50"><td class="${td} font-bold">공연 추가 납부</td><td class="${td}">2025-06</td><td class="${td}">10,000원</td><td class="${td}"><t:badge tone="warning">미납</t:badge></td><td class="${td} text-muted-foreground">—</td></tr>
                    </tbody>
                </table>
            </div>
        </c:when>

        <%-- ───── 운영진 뷰: 회비 관리 ───── --%>
        <c:otherwise>
            <t:pageHead title="회비 관리" description="부원을 선택하여 납부·미납 처리를 한 번에 적용할 수 있습니다">
                <button type="button" data-confirm="현재 회비 항목을 삭제할까요? 납부 기록도 함께 사라집니다." class="${btnOutline} text-destructive">현재 항목 삭제</button>
                <button type="button" data-open-modal="feeModal" class="${btnPrimary}">+ 회비 항목 추가</button>
            </t:pageHead>

            <div class="mb-4 inline-flex flex-wrap rounded-lg border bg-secondary p-0.5">
                <button type="button" class="rounded-md border bg-card px-3 py-1.5 text-xs font-bold text-foreground">동아리 가입 회비</button>
                <button type="button" class="rounded-md px-3 py-1.5 text-xs font-bold text-muted-foreground transition-colors">6월 정기 회식비</button>
                <button type="button" class="rounded-md px-3 py-1.5 text-xs font-bold text-muted-foreground transition-colors">공연 추가 납부</button>
            </div>

            <div class="mb-4 grid grid-cols-2 gap-4 lg:grid-cols-4">
                <t:statCard label="항목 금액" value="30,000" unit="원"/>
                <t:statCard label="납부 완료" value="4" tone="success"/>
                <t:statCard label="미납" value="2" tone="danger"/>
                <t:statCard label="수납액" value="120,000" unit="원"/>
            </div>

            <div class="mb-3 rounded-lg border bg-card px-4 py-3">
                <div class="flex flex-wrap items-center gap-3">
                    <label class="flex cursor-pointer items-center gap-2 text-xs font-extrabold"><input type="checkbox" class="size-4 accent-primary"> 전체 선택</label>
                    <span class="text-xs text-muted-foreground">체크한 부원에게 일괄 적용</span>
                    <div class="ml-auto flex gap-1.5">
                        <button type="button" class="${btnSmPrimary}">선택 납부 처리</button>
                        <button type="button" class="${btnSm}">선택 미납 처리</button>
                    </div>
                </div>
            </div>

            <div class="overflow-x-auto rounded-lg border bg-card">
                <table class="w-full border-collapse text-left">
                    <thead><tr><th class="${th} w-11">선택</th><th class="${th}">부원</th><th class="${th}">기수</th><th class="${th}">소속팀</th><th class="${th}">납부 상태</th><th class="${th}">납부일</th></tr></thead>
                    <tbody class="divide-y">
                    <tr class="transition-colors hover:bg-secondary/50"><td class="${td}"><input type="checkbox" class="size-4 accent-primary"></td><td class="${td}"><span class="flex items-center gap-2"><span class="flex size-7 items-center justify-center rounded-full bg-primary text-xs font-black text-primary-foreground">LS</span><b>이서준</b></span></td><td class="${td}">26-1기</td><td class="${td}">운영진</td><td class="${td}"><t:badge tone="success">납부 완료</t:badge></td><td class="${td}">05/02</td></tr>
                    <tr class="transition-colors hover:bg-secondary/50"><td class="${td}"><input type="checkbox" class="size-4 accent-primary"></td><td class="${td}"><span class="flex items-center gap-2"><span class="flex size-7 items-center justify-center rounded-full bg-info text-xs font-black text-white">JD</span><b>정도윤</b></span></td><td class="${td}">26-1기</td><td class="${td}">무대</td><td class="${td}"><t:badge tone="success">납부 완료</t:badge></td><td class="${td}">05/03</td></tr>
                    <tr class="transition-colors hover:bg-secondary/50"><td class="${td}"><input type="checkbox" class="size-4 accent-primary"></td><td class="${td}"><span class="flex items-center gap-2"><span class="flex size-7 items-center justify-center rounded-full bg-sidebar-accent text-xs font-black text-white">KH</span><b>김하늘</b></span></td><td class="${td}">26-2기</td><td class="${td}">배우연출</td><td class="${td}"><t:badge tone="success">납부 완료</t:badge></td><td class="${td}">05/05</td></tr>
                    <tr class="transition-colors hover:bg-secondary/50"><td class="${td}"><input type="checkbox" class="size-4 accent-primary"></td><td class="${td}"><span class="flex items-center gap-2"><span class="flex size-7 items-center justify-center rounded-full bg-accent-foreground text-xs font-black text-white">PS</span><b>박서연</b></span></td><td class="${td}">26-2기</td><td class="${td}">오퍼</td><td class="${td}"><t:badge tone="warning">미납</t:badge></td><td class="${td} text-muted-foreground">—</td></tr>
                    <tr class="transition-colors hover:bg-secondary/50"><td class="${td}"><input type="checkbox" class="size-4 accent-primary"></td><td class="${td}"><span class="flex items-center gap-2"><span class="flex size-7 items-center justify-center rounded-full bg-success text-xs font-black text-white">HJ</span><b>한지우</b></span></td><td class="${td}">26-2기</td><td class="${td}">디자인</td><td class="${td}"><t:badge tone="warning">미납</t:badge></td><td class="${td} text-muted-foreground">—</td></tr>
                    <tr class="transition-colors hover:bg-secondary/50"><td class="${td}"><input type="checkbox" class="size-4 accent-primary"></td><td class="${td}"><span class="flex items-center gap-2"><span class="flex size-7 items-center justify-center rounded-full bg-warning text-xs font-black text-white">CM</span><b>최민준</b></span></td><td class="${td}">26-1기</td><td class="${td}">영상</td><td class="${td}"><t:badge tone="success">납부 완료</t:badge></td><td class="${td}">05/06</td></tr>
                    </tbody>
                </table>
            </div>

            <t:modal id="feeModal" title="회비 항목 추가" description="가입비·회식비·추가 납부 등 새 항목을 만듭니다.">
                <jsp:attribute name="footer">
                    <button type="button" data-action="close-modal" class="${btnOutline}">취소</button>
                    <button type="button" data-action="close-modal" class="${btnPrimary}">항목 추가</button>
                </jsp:attribute>
                <jsp:body>
                    <div class="flex flex-col gap-3">
                        <div><label class="${label}" for="feeName">항목명 <span class="text-accent-foreground">*</span></label><input class="${input}" id="feeName" type="text" placeholder="예) 7월 회식비"></div>
                        <div class="grid grid-cols-2 gap-2.5">
                            <div><label class="${label}" for="feeAmt">금액 (원)</label><input class="${input}" id="feeAmt" type="number" value="15000" min="0"></div>
                            <div><label class="${label}" for="feeWhen">기준 시기</label><input class="${input}" id="feeWhen" type="text" placeholder="예) 2025-07"></div>
                        </div>
                    </div>
                </jsp:body>
            </t:modal>
        </c:otherwise>
    </c:choose>
</t:layout>
