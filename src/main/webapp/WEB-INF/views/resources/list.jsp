<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<t:layout title="자료실" active="resources" role="${role}" scriptPath="resources/list">
    <t:pageHead title="자료실" description="대본·회의록·홍보물·영상을 카테고리별로 관리합니다">
        <input data-resource-search class="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:w-56 md:text-sm" type="search" name="resourceQuery" placeholder="파일명·업로더 검색" aria-label="자료 검색">
        <c:if test="${role != 'member'}">
            <t:button variant="outline" openModal="noticeModal">공지 작성</t:button>
            <t:button openModal="uploadModal">업로드</t:button>
        </c:if>
    </t:pageHead>

    <div class="mb-4 flex items-start gap-3 rounded-lg border bg-accent/50 px-4 py-3.5">
        <span class="flex size-8 shrink-0 items-center justify-center rounded-md bg-accent text-accent-foreground">
            <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M12 17v5M5 3h14l-2 6 2 6H5l2-6-2-6z"/></svg>
        </span>
        <div class="min-w-0 flex-1">
            <div class="flex items-center gap-2"><t:badge tone="accent">중요 공지</t:badge><b class="truncate text-sm">정기공연_최종대본_v4.pdf</b></div>
            <p class="mt-0.5 text-xs text-muted-foreground">정도윤 · 06/18 게시 · 전원 필독</p>
        </div>
        <t:button size="compact" pageAction="resource-open" cssClass="shrink-0">열기</t:button>
    </div>

    <div class="mb-4 flex flex-wrap gap-2">
        <t:filterChip group="resource" value="전체" label="전체" count="8" active="true"/>
        <t:filterChip group="resource" value="공지" label="공지" count="1" dot="true"/>
        <t:filterChip group="resource" value="대본" label="대본" count="2"/>
        <t:filterChip group="resource" value="회의록" label="회의록" count="1"/>
        <t:filterChip group="resource" value="홍보물" label="홍보물" count="2"/>
        <t:filterChip group="resource" value="공연영상" label="공연영상" count="1"/>
        <t:filterChip group="resource" value="연습영상" label="연습영상" count="1"/>
        <t:filterChip group="resource" value="기타" label="기타" count="1"/>
    </div>

    <div class="rounded-lg border bg-card">
        <t:dataTable caption="자료 목록">
            <thead><tr><th>파일명</th><th>카테고리</th><th>업로더</th><th>업로드일</th><th>원본 화질</th><th></th></tr></thead>
            <tbody data-resource-list>
            <tr data-resource-row data-category="공지 대본"><td class="font-bold">정기공연_최종대본_v4.pdf</td><td><t:badge tone="accent">공지</t:badge></td><td>정도윤</td><td class="text-muted-foreground">06/18</td><td class="text-muted-foreground">—</td><td class="text-right"><t:button variant="outline" size="compact" pageAction="resource-download">다운로드</t:button></td></tr>
            <tr data-resource-row data-category="회의록"><td class="font-bold">6월3주차_회의록.docx</td><td><t:badge tone="neutral">회의록</t:badge></td><td>이서준</td><td class="text-muted-foreground">06/17</td><td class="text-muted-foreground">—</td><td class="text-right"><t:button variant="outline" size="compact" pageAction="resource-download">다운로드</t:button></td></tr>
            <tr data-resource-row data-category="홍보물"><td class="font-bold">공연포스터_A2_최종.png</td><td><t:badge tone="neutral">홍보물</t:badge></td><td>한지우</td><td class="text-muted-foreground">06/15</td><td class="text-muted-foreground">—</td><td class="text-right"><t:button variant="outline" size="compact" pageAction="resource-download">다운로드</t:button></td></tr>
            <tr data-resource-row data-category="공연영상"><td class="font-bold">예고편_편집본.mp4</td><td><t:badge tone="neutral">공연영상</t:badge></td><td>박서연</td><td class="text-muted-foreground">06/14</td><td><t:badge tone="success">1080p</t:badge></td><td class="text-right"><t:button variant="outline" size="compact" pageAction="resource-download">다운로드</t:button></td></tr>
            <tr data-resource-row data-category="대본"><td class="font-bold">블로킹_노트_2막.pdf</td><td><t:badge tone="neutral">대본</t:badge></td><td>정도윤</td><td class="text-muted-foreground">06/13</td><td class="text-muted-foreground">—</td><td class="text-right"><t:button variant="outline" size="compact" pageAction="resource-download">다운로드</t:button></td></tr>
            <tr data-resource-row data-category="기타"><td class="font-bold">조명_큐시트_초안.xlsx</td><td><t:badge tone="neutral">기타</t:badge></td><td>박서연</td><td class="text-muted-foreground">06/12</td><td class="text-muted-foreground">—</td><td class="text-right"><t:button variant="outline" size="compact" pageAction="resource-download">다운로드</t:button></td></tr>
            <tr data-resource-row data-category="연습영상"><td class="font-bold">1막_연습_풀영상.mp4</td><td><t:badge tone="neutral">연습영상</t:badge></td><td>김하늘</td><td class="text-muted-foreground">06/10</td><td><t:badge tone="success">원본 4K</t:badge></td><td class="text-right"><t:button variant="outline" size="compact" pageAction="resource-download">다운로드</t:button></td></tr>
            <tr data-resource-row data-category="홍보물"><td class="font-bold">팸플릿_내지_시안.png</td><td><t:badge tone="neutral">홍보물</t:badge></td><td>한지우</td><td class="text-muted-foreground">06/09</td><td class="text-muted-foreground">—</td><td class="text-right"><t:button variant="outline" size="compact" pageAction="resource-download">다운로드</t:button></td></tr>
            </tbody>
        </t:dataTable>
        <div class="hidden border-t" data-resource-empty role="status" aria-live="polite">
            <t:emptyState title="조건에 맞는 자료가 없습니다" message="검색어를 바꾸거나 전체 카테고리에서 다시 확인해 보세요."/>
        </div>
    </div>

    <template data-resource-row-template>
        <tr data-resource-row>
            <td data-resource-name class="font-bold"></td>
            <td data-resource-category></td>
            <td data-resource-uploader></td>
            <td data-resource-date class="text-muted-foreground"></td>
            <td data-resource-quality class="text-muted-foreground"></td>
            <td class="text-right"><t:button variant="outline" size="compact" pageAction="resource-download">다운로드</t:button></td>
        </tr>
    </template>

    <t:modal id="uploadModal" title="파일 업로드" description="대용량 파일도 원본 화질 그대로 올라갑니다.">
        <jsp:attribute name="footer">
            <t:button variant="outline" action="close-modal">취소</t:button>
            <t:button pageAction="resource-upload">업로드</t:button>
        </jsp:attribute>
        <jsp:body>
            <div class="flex flex-col gap-3">
                <div><label class="${label}" for="upName">파일명 <span class="text-accent-foreground">*</span></label><input class="${input}" id="upName" type="text" placeholder="예) 2막_연습_영상.mp4"></div>
                <div><label class="${label}" for="upCat">카테고리</label><select class="${input}" id="upCat"><option>대본</option><option>회의록</option><option>홍보물</option><option>공연영상</option><option>연습영상</option><option>기타</option></select></div>
                <label class="flex cursor-pointer items-center gap-2 text-sm font-extrabold text-accent-foreground"><input id="upNotice" type="checkbox" class="size-4 accent-primary"> 중요 공지로 함께 올리기 (상단 고정)</label>
            </div>
        </jsp:body>
    </t:modal>

    <t:modal id="noticeModal" title="공지 자료 작성" description="중요 자료를 공지로 올리면 자료실 맨 위에 고정됩니다.">
        <jsp:attribute name="footer">
            <t:button variant="outline" action="close-modal">취소</t:button>
            <t:button pageAction="resource-notice">공지 등록</t:button>
        </jsp:attribute>
        <jsp:body>
            <div class="flex flex-col gap-3">
                <div><label class="${label}" for="ntName">공지 제목 <span class="text-accent-foreground">*</span></label><input class="${input}" id="ntName" type="text" placeholder="예) 정기공연 최종 대본 v5 · 전원 필독"></div>
                <div><label class="${label}" for="ntCat">카테고리</label><select class="${input}" id="ntCat"><option>대본</option><option>회의록</option><option>홍보물</option><option>기타</option></select></div>
            </div>
        </jsp:body>
    </t:modal>
</t:layout>
