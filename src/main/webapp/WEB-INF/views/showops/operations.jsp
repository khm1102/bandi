<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<t:layout title="공연 운영" active="showops" role="${role}" scriptPath="showops/operations">
    <t:pageHead title="공연 운영" description="공연 정보와 관람객 입장 여부를 한 화면에서 관리합니다">
        <t:button pageAction="show-create">+ 공연 등록</t:button>
    </t:pageHead>

    <div class="mb-3">
        <h3 class="text-base font-extrabold">공연 정보</h3>
        <p class="text-xs text-muted-foreground">일정·장소·소개·이미지를 등록하고 수정할 수 있습니다</p>
    </div>
    <section data-show-card class="mb-7 overflow-hidden rounded-lg border bg-card">
        <div class="grid min-h-52 md:grid-cols-[220px_1fr]">
            <div class="flex min-h-52 items-center justify-center bg-linear-to-br from-sidebar to-sidebar-accent text-white">
                <div class="text-center text-sm font-extrabold">🎭<b class="mt-2 block">공연 이미지</b></div>
            </div>
            <div class="p-5">
                <div class="flex items-center gap-2">
                    <t:badge tone="success" dot="true"><span data-show-status>신청 진행 중</span></t:badge>
                    <div class="ml-auto flex gap-1.5">
                        <t:button variant="outline" size="compact" pageAction="show-edit">수정</t:button>
                        <t:button variant="outline" size="compact" pageAction="show-delete" confirm="이 공연과 화면에 표시된 운영 정보를 삭제할까요?" confirmAction="공연 삭제" cssClass="text-destructive">삭제</t:button>
                    </div>
                </div>
                <h2 data-show-title class="mt-3.5 text-xl font-black tracking-tight">소년 B가 사는 집</h2>
                <p data-show-description class="mb-3.5 mt-1 text-xs text-muted-foreground">지워지지 않는 기억과 남겨진 상처, 그리고 그럼에도 살아가려는 사람들의 이야기.</p>
                <div class="grid gap-2 md:grid-cols-2">
                    <div class="flex gap-3 border-b py-2 text-sm"><span class="w-16 shrink-0 font-bold text-muted-foreground">공연 일정</span><span data-show-period class="font-semibold">2025.06.21 - 06.22</span></div>
                    <div class="flex gap-3 border-b py-2 text-sm"><span class="w-16 shrink-0 font-bold text-muted-foreground">공연 시간</span><span data-show-time class="font-semibold">17:00</span></div>
                    <div class="flex gap-3 py-2 text-sm"><span class="w-16 shrink-0 font-bold text-muted-foreground">장소</span><span data-show-place class="font-semibold">한국공학대학교 TIP아트센터</span></div>
                    <div class="flex gap-3 py-2 text-sm"><span class="w-16 shrink-0 font-bold text-muted-foreground">관람 정보</span><span data-show-viewing class="font-semibold">만 12세 이상 · 90분</span></div>
                </div>
            </div>
        </div>
    </section>

    <div class="mb-3 flex items-center gap-2">
        <div>
            <h3 class="text-base font-extrabold">관람객 입장 현황</h3>
            <p class="text-xs text-muted-foreground">신청자별로 실제 입장 여부와 체크 시간을 확인합니다</p>
        </div>
        <span class="ml-auto"><t:badge tone="warning" dot="true"><span data-entry-pending-summary>미입장 2건</span></t:badge></span>
    </div>

    <div class="mb-4 grid grid-cols-2 gap-4 lg:grid-cols-4">
        <t:statCard label="신청 건수" value="5" unit="건"/>
        <t:statCard label="신청 좌석" value="9" unit="석"/>
        <t:statCard label="입장 완료" value="3" unit="건" delta="6석 입장" tone="success" valueHook="entry-completed" deltaHook="entry-completed-seats"/>
        <t:statCard label="미입장" value="2" unit="건" delta="입장률 60%" tone="danger" valueHook="entry-pending" deltaHook="entry-rate"/>
    </div>

    <div class="rounded-lg border bg-card">
        <t:dataTable caption="관람객 입장 현황">
            <thead><tr><th>관람객</th><th>연락처</th><th>좌석</th><th>공연 회차</th><th>입장 여부</th><th>체크 시간</th><th class="text-right">입장 처리</th></tr></thead>
            <tbody data-entry-list>
            <tr><td><span class="flex items-center gap-2"><span class="flex size-7 shrink-0 items-center justify-center rounded-full bg-success text-xs font-black text-white">김</span><span><b class="block">김수민</b><span class="text-xs text-muted-foreground">2명 신청</span></span></span></td><td class="text-muted-foreground">010-2345-1122</td><td><t:badge tone="info">A3</t:badge> <t:badge tone="info">A4</t:badge></td><td><b>6/21</b> <span class="text-muted-foreground">17:00</span></td><td><t:badge tone="success" dot="true">입장 완료</t:badge></td><td>16:42</td><td class="text-right"><t:button variant="outline" size="compact" pageAction="entry-cancel">입장 취소</t:button></td></tr>
            <tr><td><span class="flex items-center gap-2"><span class="flex size-7 shrink-0 items-center justify-center rounded-full bg-success text-xs font-black text-white">이</span><span><b class="block">이정후</b><span class="text-xs text-muted-foreground">1명 신청</span></span></span></td><td class="text-muted-foreground">010-8811-3344</td><td><t:badge tone="info">C5</t:badge></td><td><b>6/21</b> <span class="text-muted-foreground">17:00</span></td><td><t:badge tone="success" dot="true">입장 완료</t:badge></td><td>16:45</td><td class="text-right"><t:button variant="outline" size="compact" pageAction="entry-cancel">입장 취소</t:button></td></tr>
            <tr><td><span class="flex items-center gap-2"><span class="flex size-7 shrink-0 items-center justify-center rounded-full bg-success text-xs font-black text-white">박</span><span><b class="block">박하은</b><span class="text-xs text-muted-foreground">3명 신청</span></span></span></td><td class="text-muted-foreground">010-5522-7788</td><td><t:badge tone="info">C6</t:badge> <t:badge tone="info">D1</t:badge> <t:badge tone="info">D2</t:badge></td><td><b>6/21</b> <span class="text-muted-foreground">17:00</span></td><td><t:badge tone="success" dot="true">입장 완료</t:badge></td><td>16:51</td><td class="text-right"><t:button variant="outline" size="compact" pageAction="entry-cancel">입장 취소</t:button></td></tr>
            <tr><td><span class="flex items-center gap-2"><span class="flex size-7 shrink-0 items-center justify-center rounded-full bg-info text-xs font-black text-white">최</span><span><b class="block">최유진</b><span class="text-xs text-muted-foreground">2명 신청</span></span></span></td><td class="text-muted-foreground">010-7788-9900</td><td><t:badge tone="info">E4</t:badge> <t:badge tone="info">E5</t:badge></td><td><b>6/21</b> <span class="text-muted-foreground">17:00</span></td><td><t:badge tone="warning" dot="true">미입장</t:badge></td><td class="text-muted-foreground">—</td><td class="text-right"><t:button size="compact" pageAction="entry-checkin">입장 완료</t:button></td></tr>
            <tr><td><span class="flex items-center gap-2"><span class="flex size-7 shrink-0 items-center justify-center rounded-full bg-info text-xs font-black text-white">정</span><span><b class="block">정민석</b><span class="text-xs text-muted-foreground">1명 신청</span></span></span></td><td class="text-muted-foreground">010-3344-5566</td><td><t:badge tone="info">B2</t:badge></td><td><b>6/22</b> <span class="text-muted-foreground">17:00</span></td><td><t:badge tone="warning" dot="true">미입장</t:badge></td><td class="text-muted-foreground">—</td><td class="text-right"><t:button size="compact" pageAction="entry-checkin">입장 완료</t:button></td></tr>
            </tbody>
        </t:dataTable>
    </div>

    <t:modal id="showModal" title="공연 등록" description="공연 일정과 소개 이미지를 등록합니다.">
        <jsp:attribute name="footer">
            <t:button variant="outline" action="close-modal">취소</t:button>
            <t:button pageAction="show-save">등록</t:button>
        </jsp:attribute>
        <jsp:body>
            <div class="flex flex-col gap-3">
                <div><label class="${label}" for="shTitle">공연명 <span class="text-accent-foreground">*</span></label><input class="${input}" id="shTitle" type="text" placeholder="공연명"></div>
                <div class="grid grid-cols-2 gap-2.5">
                    <div><label class="${label}" for="shPeriod">공연 기간</label><input class="${input}" id="shPeriod" type="text" placeholder="2026.11.20 - 11.21"></div>
                    <div><label class="${label}" for="shTime">공연 시간</label><input class="${input}" id="shTime" type="text" placeholder="17:00"></div>
                </div>
                <div><label class="${label}" for="shPlace">장소</label><input class="${input}" id="shPlace" type="text" placeholder="공연 장소"></div>
                <div class="grid grid-cols-3 gap-2.5">
                    <div><label class="${label}" for="shAge">관람 연령</label><input class="${input}" id="shAge" type="text"></div>
                    <div><label class="${label}" for="shRuntime">러닝타임</label><input class="${input}" id="shRuntime" type="text"></div>
                    <div><label class="${label}" for="shStatus">상태</label><select class="${input}" id="shStatus"><option>신청 진행 중</option><option>준비 중</option><option>마감</option></select></div>
                </div>
                <div><label class="${label}" for="shDesc">공연 소개</label><textarea class="${input} h-auto resize-none py-2.5" id="shDesc" rows="3"></textarea></div>
                <div><label class="${label}" for="shImage">공연 이미지</label><input class="${input} py-2" id="shImage" type="file" accept="image/*"></div>
            </div>
        </jsp:body>
    </t:modal>
</t:layout>
