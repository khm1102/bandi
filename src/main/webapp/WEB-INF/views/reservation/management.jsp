<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="av" value="flex size-7 shrink-0 items-center justify-center rounded-full bg-info text-xs font-black text-white"/>
<t:layout title="신청 관리" active="reservations" role="${role}" scriptPath="reservation/management">
    <t:pageHead title="공연 신청 관리" description="회차별 관람 신청 명단을 관리합니다 (하루 1회 · 총 2회)">
        <t:button variant="outline" pageAction="reservation-export">명단 내보내기</t:button>
    </t:pageHead>

    <div class="mb-4 grid grid-cols-2 gap-4 lg:grid-cols-4">
        <t:statCard label="총 신청" value="5" unit="건"/>
        <t:statCard label="총 좌석" value="9" unit="석"/>
        <t:statCard label="6/21 (토)" value="8" unit="석" tone="success"/>
        <t:statCard label="6/22 (일)" value="1" unit="석" tone="success"/>
    </div>

    <div class="rounded-lg border bg-card">
        <t:dataTable caption="공연 관람 신청 명단">
            <thead><tr><th>관람객명</th><th>연락처</th><th>좌석</th><th>인원</th><th>관람일</th><th>회차</th></tr></thead>
            <tbody>
            <tr><td><span class="flex items-center gap-2"><span class="${av}">김</span><b data-export-value>김수민</b></span></td><td class="text-muted-foreground">010-2345-1122</td><td><t:badge tone="info">A3</t:badge> <t:badge tone="info">A4</t:badge></td><td>2명</td><td>6/21</td><td>17:00</td></tr>
            <tr><td><span class="flex items-center gap-2"><span class="${av}">이</span><b data-export-value>이정후</b></span></td><td class="text-muted-foreground">010-8811-3344</td><td><t:badge tone="info">C5</t:badge></td><td>1명</td><td>6/21</td><td>17:00</td></tr>
            <tr><td><span class="flex items-center gap-2"><span class="${av}">박</span><b data-export-value>박하은</b></span></td><td class="text-muted-foreground">010-5522-7788</td><td><t:badge tone="info">C6</t:badge> <t:badge tone="info">D1</t:badge> <t:badge tone="info">D2</t:badge></td><td>3명</td><td>6/21</td><td>17:00</td></tr>
            <tr><td><span class="flex items-center gap-2"><span class="${av}">최</span><b data-export-value>최유진</b></span></td><td class="text-muted-foreground">010-7788-9900</td><td><t:badge tone="info">E4</t:badge> <t:badge tone="info">E5</t:badge></td><td>2명</td><td>6/21</td><td>17:00</td></tr>
            <tr><td><span class="flex items-center gap-2"><span class="${av}">정</span><b data-export-value>정민석</b></span></td><td class="text-muted-foreground">010-3344-5566</td><td><t:badge tone="info">B2</t:badge></td><td>1명</td><td>6/22</td><td>17:00</td></tr>
            </tbody>
        </t:dataTable>
    </div>
</t:layout>
