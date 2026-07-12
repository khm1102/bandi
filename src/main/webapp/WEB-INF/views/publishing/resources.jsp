<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="btnPrimary" value="inline-flex h-9 items-center justify-center gap-1.5 rounded-md bg-primary px-4 text-sm font-bold text-primary-foreground transition-colors hover:bg-primary-strong hover:text-white"/>
<c:set var="btnOutline" value="inline-flex h-9 items-center justify-center gap-1.5 rounded-md border bg-card px-4 text-sm font-bold transition-colors hover:bg-secondary"/>
<c:set var="btnSm" value="inline-flex h-8 items-center rounded-md border bg-card px-3 text-xs font-bold transition-colors hover:bg-secondary"/>
<c:set var="chip" value="inline-flex h-8 items-center gap-1.5 rounded-md border bg-card px-3 text-xs font-bold text-muted-foreground transition-colors hover:border-sidebar-muted"/>
<c:set var="chipOn" value="inline-flex h-8 items-center gap-1.5 rounded-md border border-sidebar bg-sidebar px-3 text-xs font-bold text-white"/>
<c:set var="th" value="whitespace-nowrap bg-secondary px-4 py-3 text-left text-xs font-extrabold text-muted-foreground"/>
<c:set var="td" value="px-4 py-3 text-sm"/>
<c:set var="input" value="h-10 w-full rounded-md border border-input bg-card px-3 text-sm transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<t:layout title="자료실" active="resources" role="${role}">
    <t:pageHead title="자료실" description="대본·회의록·홍보물·영상을 카테고리별로 관리합니다">
        <input class="h-9 w-48 rounded-md border border-input bg-card px-3 text-sm transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20" type="search" placeholder="파일 검색">
        <c:if test="${role != 'member'}">
            <button type="button" data-open-modal="noticeModal" class="${btnOutline}">공지 작성</button>
            <button type="button" data-open-modal="uploadModal" class="${btnPrimary}">업로드</button>
        </c:if>
    </t:pageHead>

    <div class="mb-4 flex items-start gap-3 rounded-lg border border-l-4 border-l-primary bg-accent/50 px-4 py-3.5">
        <div class="min-w-0 flex-1">
            <div class="flex items-center gap-2"><t:badge tone="accent">📌 중요 공지</t:badge><b class="truncate text-sm">정기공연_최종대본_v4.pdf</b></div>
            <p class="mt-0.5 text-xs text-muted-foreground">정도윤 · 06/18 게시 · 전원 필독</p>
        </div>
        <button type="button" class="inline-flex h-8 shrink-0 items-center rounded-md bg-primary px-3 text-xs font-bold text-primary-foreground transition-colors hover:bg-primary-strong hover:text-white">열기</button>
    </div>

    <div class="mb-4 flex flex-wrap gap-2">
        <button type="button" class="${chipOn}">전체 <span class="opacity-70">8</span></button>
        <button type="button" class="${chip}"><i class="size-2 rounded-full bg-primary"></i>공지 <span class="opacity-70">1</span></button>
        <button type="button" class="${chip}">대본 <span class="opacity-70">2</span></button>
        <button type="button" class="${chip}">회의록 <span class="opacity-70">1</span></button>
        <button type="button" class="${chip}">홍보물 <span class="opacity-70">2</span></button>
        <button type="button" class="${chip}">공연영상 <span class="opacity-70">1</span></button>
        <button type="button" class="${chip}">연습영상 <span class="opacity-70">1</span></button>
        <button type="button" class="${chip}">기타 <span class="opacity-70">1</span></button>
    </div>

    <div class="overflow-x-auto rounded-lg border bg-card">
        <table class="w-full border-collapse text-left">
            <thead><tr><th class="${th}">파일명</th><th class="${th}">카테고리</th><th class="${th}">업로더</th><th class="${th}">업로드일</th><th class="${th}">원본 화질</th><th class="${th}"></th></tr></thead>
            <tbody class="divide-y">
            <tr class="transition-colors hover:bg-secondary/50"><td class="${td} font-bold">정기공연_최종대본_v4.pdf</td><td class="${td}"><t:badge tone="accent">📌 공지</t:badge></td><td class="${td}">정도윤</td><td class="${td} text-muted-foreground">06/18</td><td class="${td} text-muted-foreground">—</td><td class="${td} text-right"><button type="button" class="${btnSm}">다운로드</button></td></tr>
            <tr class="transition-colors hover:bg-secondary/50"><td class="${td} font-bold">6월3주차_회의록.docx</td><td class="${td}"><t:badge tone="neutral">회의록</t:badge></td><td class="${td}">이서준</td><td class="${td} text-muted-foreground">06/17</td><td class="${td} text-muted-foreground">—</td><td class="${td} text-right"><button type="button" class="${btnSm}">다운로드</button></td></tr>
            <tr class="transition-colors hover:bg-secondary/50"><td class="${td} font-bold">공연포스터_A2_최종.png</td><td class="${td}"><t:badge tone="neutral">홍보물</t:badge></td><td class="${td}">한지우</td><td class="${td} text-muted-foreground">06/15</td><td class="${td} text-muted-foreground">—</td><td class="${td} text-right"><button type="button" class="${btnSm}">다운로드</button></td></tr>
            <tr class="transition-colors hover:bg-secondary/50"><td class="${td} font-bold">예고편_편집본.mp4</td><td class="${td}"><t:badge tone="neutral">공연영상</t:badge></td><td class="${td}">박서연</td><td class="${td} text-muted-foreground">06/14</td><td class="${td}"><t:badge tone="success">1080p</t:badge></td><td class="${td} text-right"><button type="button" class="${btnSm}">다운로드</button></td></tr>
            <tr class="transition-colors hover:bg-secondary/50"><td class="${td} font-bold">블로킹_노트_2막.pdf</td><td class="${td}"><t:badge tone="neutral">대본</t:badge></td><td class="${td}">정도윤</td><td class="${td} text-muted-foreground">06/13</td><td class="${td} text-muted-foreground">—</td><td class="${td} text-right"><button type="button" class="${btnSm}">다운로드</button></td></tr>
            <tr class="transition-colors hover:bg-secondary/50"><td class="${td} font-bold">조명_큐시트_초안.xlsx</td><td class="${td}"><t:badge tone="neutral">기타</t:badge></td><td class="${td}">박서연</td><td class="${td} text-muted-foreground">06/12</td><td class="${td} text-muted-foreground">—</td><td class="${td} text-right"><button type="button" class="${btnSm}">다운로드</button></td></tr>
            <tr class="transition-colors hover:bg-secondary/50"><td class="${td} font-bold">1막_연습_풀영상.mp4</td><td class="${td}"><t:badge tone="neutral">연습영상</t:badge></td><td class="${td}">김하늘</td><td class="${td} text-muted-foreground">06/10</td><td class="${td}"><t:badge tone="success">원본 4K</t:badge></td><td class="${td} text-right"><button type="button" class="${btnSm}">다운로드</button></td></tr>
            <tr class="transition-colors hover:bg-secondary/50"><td class="${td} font-bold">팸플릿_내지_시안.png</td><td class="${td}"><t:badge tone="neutral">홍보물</t:badge></td><td class="${td}">한지우</td><td class="${td} text-muted-foreground">06/09</td><td class="${td} text-muted-foreground">—</td><td class="${td} text-right"><button type="button" class="${btnSm}">다운로드</button></td></tr>
            </tbody>
        </table>
    </div>

    <t:modal id="uploadModal" title="파일 업로드" description="대용량 파일도 원본 화질 그대로 올라갑니다.">
        <jsp:attribute name="footer">
            <button type="button" data-action="close-modal" class="${btnOutline}">취소</button>
            <button type="button" data-action="close-modal" class="${btnPrimary}">업로드</button>
        </jsp:attribute>
        <jsp:body>
            <div class="flex flex-col gap-3">
                <div><label class="${label}" for="upName">파일명 <span class="text-accent-foreground">*</span></label><input class="${input}" id="upName" type="text" placeholder="예) 2막_연습_영상.mp4"></div>
                <div><label class="${label}" for="upCat">카테고리</label><select class="${input}" id="upCat"><option>대본</option><option>회의록</option><option>홍보물</option><option>공연영상</option><option>연습영상</option><option>기타</option></select></div>
                <label class="flex cursor-pointer items-center gap-2 text-sm font-extrabold text-accent-foreground"><input type="checkbox" class="size-4 accent-primary"> 중요 공지로 함께 올리기 (상단 고정)</label>
            </div>
        </jsp:body>
    </t:modal>

    <t:modal id="noticeModal" title="공지 자료 작성" description="중요 자료를 공지로 올리면 자료실 맨 위에 고정됩니다.">
        <jsp:attribute name="footer">
            <button type="button" data-action="close-modal" class="${btnOutline}">취소</button>
            <button type="button" data-action="close-modal" class="${btnPrimary}">공지 등록</button>
        </jsp:attribute>
        <jsp:body>
            <div class="flex flex-col gap-3">
                <div><label class="${label}" for="ntName">공지 제목 <span class="text-accent-foreground">*</span></label><input class="${input}" id="ntName" type="text" placeholder="예) 정기공연 최종 대본 v5 · 전원 필독"></div>
                <div><label class="${label}" for="ntCat">카테고리</label><select class="${input}" id="ntCat"><option>대본</option><option>회의록</option><option>홍보물</option><option>기타</option></select></div>
            </div>
        </jsp:body>
    </t:modal>
</t:layout>
