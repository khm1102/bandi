<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="btnOutline" value="inline-flex h-9 items-center justify-center gap-1.5 rounded-md border bg-card px-4 text-sm font-bold transition-colors hover:bg-secondary"/>
<c:set var="th" value="whitespace-nowrap bg-secondary px-4 py-3 text-left text-xs font-extrabold text-muted-foreground"/>
<c:set var="td" value="px-4 py-3 text-sm"/>
<c:set var="av" value="flex size-7 shrink-0 items-center justify-center rounded-full bg-info text-xs font-black text-white"/>
<t:layout title="신청 관리" active="reservations" role="${role}">
    <t:pageHead title="공연 신청 관리" description="회차별 관람 신청 명단을 관리합니다 (하루 1회 · 총 2회)">
        <button type="button" class="${btnOutline}">명단 내보내기</button>
    </t:pageHead>

    <div class="mb-4 grid grid-cols-2 gap-4 lg:grid-cols-4">
        <t:statCard label="총 신청" value="5" unit="건"/>
        <t:statCard label="총 좌석" value="9" unit="석"/>
        <t:statCard label="6/21 (토)" value="8" unit="석" tone="success"/>
        <t:statCard label="6/22 (일)" value="1" unit="석" tone="success"/>
    </div>

    <div class="overflow-x-auto rounded-lg border bg-card">
        <table class="w-full border-collapse text-left">
            <thead><tr><th class="${th}">관람객명</th><th class="${th}">연락처</th><th class="${th}">좌석</th><th class="${th}">인원</th><th class="${th}">관람일</th><th class="${th}">회차</th></tr></thead>
            <tbody class="divide-y">
            <tr class="transition-colors hover:bg-secondary/50"><td class="${td}"><span class="flex items-center gap-2"><span class="${av}">김</span><b>김수민</b></span></td><td class="${td} text-muted-foreground">010-2345-1122</td><td class="${td}"><t:badge tone="info">A3</t:badge> <t:badge tone="info">A4</t:badge></td><td class="${td}">2명</td><td class="${td}">6/21</td><td class="${td}">17:00</td></tr>
            <tr class="transition-colors hover:bg-secondary/50"><td class="${td}"><span class="flex items-center gap-2"><span class="${av}">이</span><b>이정후</b></span></td><td class="${td} text-muted-foreground">010-8811-3344</td><td class="${td}"><t:badge tone="info">C5</t:badge></td><td class="${td}">1명</td><td class="${td}">6/21</td><td class="${td}">17:00</td></tr>
            <tr class="transition-colors hover:bg-secondary/50"><td class="${td}"><span class="flex items-center gap-2"><span class="${av}">박</span><b>박하은</b></span></td><td class="${td} text-muted-foreground">010-5522-7788</td><td class="${td}"><t:badge tone="info">C6</t:badge> <t:badge tone="info">D1</t:badge> <t:badge tone="info">D2</t:badge></td><td class="${td}">3명</td><td class="${td}">6/21</td><td class="${td}">17:00</td></tr>
            <tr class="transition-colors hover:bg-secondary/50"><td class="${td}"><span class="flex items-center gap-2"><span class="${av}">최</span><b>최유진</b></span></td><td class="${td} text-muted-foreground">010-7788-9900</td><td class="${td}"><t:badge tone="info">E4</t:badge> <t:badge tone="info">E5</t:badge></td><td class="${td}">2명</td><td class="${td}">6/21</td><td class="${td}">17:00</td></tr>
            <tr class="transition-colors hover:bg-secondary/50"><td class="${td}"><span class="flex items-center gap-2"><span class="${av}">정</span><b>정민석</b></span></td><td class="${td} text-muted-foreground">010-3344-5566</td><td class="${td}"><t:badge tone="info">B2</t:badge></td><td class="${td}">1명</td><td class="${td}">6/22</td><td class="${td}">17:00</td></tr>
            </tbody>
        </table>
    </div>
</t:layout>
