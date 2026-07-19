<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<t:layout title="공지·자료실" active="resources" role="${role}" scriptPath="resources/list">
    <t:pageHead title="공지·자료실" description="내부 전달 사항과 업무 자료를 한 곳에서 찾되, 게시 책임과 파일 이력은 분리해 관리합니다"/>

    <nav class="mb-5 grid grid-cols-2 rounded-lg border bg-secondary p-1" aria-label="공지·자료실 구분">
        <button type="button" class="inline-flex min-h-11 items-center justify-center rounded-md border bg-card px-3 text-sm font-extrabold text-foreground" data-info-tab="notices" aria-controls="internalNotices" aria-selected="true">공지</button>
        <button type="button" class="inline-flex min-h-11 items-center justify-center rounded-md px-3 text-sm font-extrabold text-muted-foreground" data-info-tab="resources" aria-controls="resourceLibrary" aria-selected="false">자료</button>
    </nav>

    <section id="internalNotices" data-info-panel="notices" aria-labelledby="internalNoticeTitle">
        <div class="mb-4 flex flex-col gap-3 md:flex-row md:items-end">
            <div class="min-w-0 flex-1">
                <h2 id="internalNoticeTitle" class="text-lg font-black">내부 공지</h2>
                <p class="mt-1 text-xs leading-5 text-muted-foreground">전체 공지와 내 팀 공지를 표시하며, 조회수 대신 확인·미확인 멤버를 관리합니다.</p>
            </div>
            <c:if test="${role != 'member'}">
                <t:button openModal="noticeModal" cssClass="w-full md:w-auto">짧은 공지 작성</t:button>
            </c:if>
        </div>

        <div class="mb-4 flex flex-wrap gap-2">
            <t:filterChip group="notice" value="전체" label="전체" count="4" active="true"/>
            <t:filterChip group="notice" value="미확인" label="내가 미확인" count="1" dot="true"/>
            <t:filterChip group="notice" value="전체공지" label="전체 공지" count="2"/>
            <t:filterChip group="notice" value="팀공지" label="팀 공지" count="2"/>
        </div>

        <div class="flex flex-col gap-3" data-notice-list>
            <article class="rounded-lg border border-primary/40 bg-accent/40 p-4 md:p-5" data-notice-card data-category="미확인 전체공지">
                <div class="flex flex-col gap-3 md:flex-row md:items-start">
                    <div class="min-w-0 flex-1">
                        <div class="flex flex-wrap items-center gap-2"><t:badge tone="accent">중요</t:badge><t:badge tone="neutral">전체</t:badge><t:badge tone="warning">내가 미확인</t:badge></div>
                        <h3 class="mt-3 text-base font-black">정기공연 최종 리허설 및 전원 소집 안내</h3>
                        <p class="mt-1 text-sm leading-6 text-muted-foreground">6월 20일 18시까지 소극장으로 집합해 주세요. 개인 의상과 팀별 준비물을 확인합니다.</p>
                        <p class="mt-3 text-xs text-muted-foreground">이서준 · 06/18 게시 · 확인 38명 / 미확인 4명</p>
                    </div>
                    <t:button variant="outline" pageAction="notice-open" cssClass="w-full md:w-auto">상세 보기</t:button>
                </div>
            </article>
            <article class="rounded-lg border bg-card p-4 md:p-5" data-notice-card data-category="팀공지">
                <div class="flex flex-col gap-3 md:flex-row md:items-start">
                    <div class="min-w-0 flex-1">
                        <div class="flex flex-wrap items-center gap-2"><t:badge tone="info">무대팀</t:badge><t:badge tone="success">확인 완료</t:badge></div>
                        <h3 class="mt-3 text-base font-black">공연 당일 무대 전환 동선 확정</h3>
                        <p class="mt-1 text-sm leading-6 text-muted-foreground">전환별 담당 위치와 대기 동선을 최종 공유합니다.</p>
                        <p class="mt-3 text-xs text-muted-foreground">정도윤 · 06/17 게시 · 팀원 전원 확인</p>
                    </div>
                    <t:button variant="outline" pageAction="notice-open" cssClass="w-full md:w-auto">상세 보기</t:button>
                </div>
            </article>
            <article class="rounded-lg border bg-card p-4 md:p-5" data-notice-card data-category="전체공지">
                <div class="flex flex-col gap-3 md:flex-row md:items-start">
                    <div class="min-w-0 flex-1">
                        <div class="flex flex-wrap items-center gap-2"><t:badge tone="neutral">전체</t:badge><t:badge tone="success">확인 완료</t:badge></div>
                        <h3 class="mt-3 text-base font-black">공연 주간 동아리방 사용 안내</h3>
                        <p class="mt-1 text-sm leading-6 text-muted-foreground">공연 물품 적재 기간과 개인 물품 정리 기준을 안내합니다.</p>
                        <p class="mt-3 text-xs text-muted-foreground">운영진 · 06/16 게시 · 확인 42명 / 미확인 0명</p>
                    </div>
                    <t:button variant="outline" pageAction="notice-open" cssClass="w-full md:w-auto">상세 보기</t:button>
                </div>
            </article>
        </div>
    </section>

    <section id="resourceLibrary" class="hidden" data-info-panel="resources" aria-labelledby="resourceLibraryTitle">
        <div class="mb-4 flex flex-col gap-3 md:flex-row md:items-end">
            <div class="min-w-0 flex-1">
                <h2 id="resourceLibraryTitle" class="text-lg font-black">업무 자료</h2>
                <p class="mt-1 text-xs leading-5 text-muted-foreground">자료 제목, 분류, 버전과 파일 교체 이력을 기준으로 관리합니다.</p>
            </div>
            <div class="grid w-full gap-2 md:flex md:w-auto">
                <input data-resource-search class="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:w-56 md:text-sm" type="search" name="resourceQuery" placeholder="자료 제목·업로더 검색" aria-label="자료 검색">
                <c:if test="${role != 'member'}"><t:button openModal="uploadModal">자료 업로드</t:button></c:if>
            </div>
        </div>

        <div class="mb-4 flex items-start gap-3 rounded-lg border bg-secondary/60 px-4 py-3.5">
            <span class="flex size-8 shrink-0 items-center justify-center rounded-md bg-accent text-accent-foreground">
                <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M12 17v5M5 3h14l-2 6 2 6H5l2-6-2-6z"/></svg>
            </span>
            <div class="min-w-0 flex-1"><b class="block text-sm">최종 대본 v4</b><p class="mt-0.5 text-xs text-muted-foreground">대본 · 정도윤 · 06/18 교체</p></div>
        </div>

        <div class="mb-4 flex flex-wrap gap-2">
            <t:filterChip group="resource" value="전체" label="전체" count="6" active="true"/>
            <t:filterChip group="resource" value="대본" label="대본" count="2"/>
            <t:filterChip group="resource" value="회의록" label="회의록" count="1"/>
            <t:filterChip group="resource" value="홍보물" label="홍보물" count="1"/>
            <t:filterChip group="resource" value="영상" label="영상" count="2"/>
        </div>

        <div class="rounded-lg border bg-card">
            <t:dataTable caption="자료 목록">
                <thead><tr><th>자료 제목</th><th>분류</th><th>버전</th><th>업로더</th><th>교체일</th><th></th></tr></thead>
                <tbody data-resource-list>
                <tr data-resource-row data-category="대본"><td class="font-bold">정기공연 최종 대본</td><td><t:badge tone="neutral">대본</t:badge></td><td>v4</td><td>정도윤</td><td class="text-muted-foreground">06/18</td><td class="text-right"><t:button variant="outline" size="compact" pageAction="resource-download">다운로드</t:button></td></tr>
                <tr data-resource-row data-category="회의록"><td class="font-bold">6월 3주차 운영 회의록</td><td><t:badge tone="neutral">회의록</t:badge></td><td>v1</td><td>이서준</td><td class="text-muted-foreground">06/17</td><td class="text-right"><t:button variant="outline" size="compact" pageAction="resource-download">다운로드</t:button></td></tr>
                <tr data-resource-row data-category="홍보물"><td class="font-bold">정기공연 A2 포스터</td><td><t:badge tone="neutral">홍보물</t:badge></td><td>최종</td><td>한지우</td><td class="text-muted-foreground">06/15</td><td class="text-right"><t:button variant="outline" size="compact" pageAction="resource-download">다운로드</t:button></td></tr>
                <tr data-resource-row data-category="영상"><td class="font-bold">정기공연 예고편 편집본</td><td><t:badge tone="neutral">영상</t:badge></td><td>v2</td><td>박서연</td><td class="text-muted-foreground">06/14</td><td class="text-right"><t:button variant="outline" size="compact" pageAction="resource-download">다운로드</t:button></td></tr>
                <tr data-resource-row data-category="대본"><td class="font-bold">2막 블로킹 노트</td><td><t:badge tone="neutral">대본</t:badge></td><td>v3</td><td>정도윤</td><td class="text-muted-foreground">06/13</td><td class="text-right"><t:button variant="outline" size="compact" pageAction="resource-download">다운로드</t:button></td></tr>
                <tr data-resource-row data-category="영상"><td class="font-bold">1막 연습 전체 영상</td><td><t:badge tone="neutral">영상</t:badge></td><td>원본</td><td>김하늘</td><td class="text-muted-foreground">06/10</td><td class="text-right"><t:button variant="outline" size="compact" pageAction="resource-download">다운로드</t:button></td></tr>
                </tbody>
            </t:dataTable>
            <div class="hidden border-t" data-resource-empty role="status" aria-live="polite"><t:emptyState title="조건에 맞는 자료가 없습니다" message="검색어나 분류를 바꿔 다시 확인해 보세요."/></div>
        </div>
    </section>

    <template data-resource-row-template>
        <tr data-resource-row><td data-resource-name class="font-bold"></td><td data-resource-category></td><td data-resource-version></td><td data-resource-uploader></td><td data-resource-date class="text-muted-foreground"></td><td class="text-right"><t:button variant="outline" size="compact" pageAction="resource-download">다운로드</t:button></td></tr>
    </template>
    <template data-notice-card-template>
        <article class="rounded-lg border bg-card p-4 md:p-5" data-notice-card>
            <div class="flex flex-col gap-3 md:flex-row md:items-start"><div class="min-w-0 flex-1"><div class="flex flex-wrap items-center gap-2" data-notice-badges></div><h3 class="mt-3 text-base font-black" data-notice-title></h3><p class="mt-1 text-sm leading-6 text-muted-foreground" data-notice-body></p><p class="mt-3 text-xs text-muted-foreground" data-notice-meta></p></div><t:button variant="outline" pageAction="notice-open" cssClass="w-full md:w-auto">상세 보기</t:button></div>
        </article>
    </template>

    <t:modal id="uploadModal" title="자료 업로드" description="파일과 자료 메타데이터를 등록합니다. 파일 교체 이력은 자료 상세에서 관리합니다.">
        <jsp:attribute name="footer"><t:button variant="outline" action="close-modal">취소</t:button><t:button pageAction="resource-upload">업로드</t:button></jsp:attribute>
        <jsp:body><div class="flex flex-col gap-3"><div><label class="${label}" for="upName">자료 제목 <span class="text-accent-foreground">*</span></label><input class="${input}" id="upName" type="text" placeholder="예) 정기공연 최종 대본"></div><div><label class="${label}" for="upCat">분류</label><select class="${input}" id="upCat"><option>대본</option><option>회의록</option><option>홍보물</option><option>영상</option><option>기타</option></select></div></div></jsp:body>
    </t:modal>

    <t:modal id="noticeModal" title="짧은 공지 작성" description="간단한 전달 사항만 빠르게 게시합니다. 중요·장문 공지는 전용 작성 화면에서 관리합니다.">
        <jsp:attribute name="footer"><t:button variant="outline" action="close-modal">취소</t:button><t:button pageAction="notice-add">공지 게시</t:button></jsp:attribute>
        <jsp:body><div class="flex flex-col gap-3"><div><label class="${label}" for="ntTarget">대상</label><select class="${input}" id="ntTarget"><option>전체</option><option>내 팀</option></select></div><div><label class="${label}" for="ntTitle">제목 <span class="text-accent-foreground">*</span></label><input class="${input}" id="ntTitle" type="text" maxlength="100" placeholder="공지 제목"></div><div><label class="${label}" for="ntBody">내용</label><textarea class="${input} h-auto resize-none py-2.5" id="ntBody" rows="3" maxlength="500" placeholder="간단한 전달 내용을 입력하세요"></textarea></div></div></jsp:body>
    </t:modal>
</t:layout>
