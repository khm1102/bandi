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
                <p class="mt-1 text-xs leading-5 text-muted-foreground">전체 공지와 내 팀 공지를 표시하며, 조회수 대신 내가 읽었는지 기록합니다.</p>
            </div>
            <c:if test="${role != 'member'}">
                <t:button openModal="noticeModal" cssClass="w-full md:w-auto">짧은 공지 작성</t:button>
            </c:if>
        </div>

        <div class="mb-4 flex flex-wrap gap-2">
            <t:filterChip group="notice" value="ALL" label="전체" active="true"/>
            <t:filterChip group="notice" value="UNREAD" label="내가 미확인" dot="true"/>
            <t:filterChip group="notice" value="ALL_SCOPE" label="전체 공지"/>
            <t:filterChip group="notice" value="TEAM_SCOPE" label="팀 공지"/>
        </div>

        <div class="rounded-lg border bg-card px-5 py-11 text-center" data-notice-state role="status" aria-live="polite">
            <b class="block text-sm font-extrabold" data-notice-state-title>공지를 불러오는 중입니다</b>
            <p class="mt-1 text-xs text-muted-foreground" data-notice-state-message>잠시만 기다려 주세요.</p>
            <button type="button" class="mx-auto mt-4 hidden min-h-11 rounded-md border bg-card px-4 text-xs font-bold" data-notice-retry>다시 시도</button>
        </div>
        <div class="hidden flex-col gap-3" data-notice-list></div>
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

        <div class="mb-4 hidden items-start gap-3 rounded-lg border bg-secondary/60 px-4 py-3.5" data-pinned-resource>
            <span class="flex size-8 shrink-0 items-center justify-center rounded-md bg-accent text-accent-foreground">
                <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M12 17v5M5 3h14l-2 6 2 6H5l2-6-2-6z"/></svg>
            </span>
            <div class="min-w-0 flex-1"><b class="block text-sm" data-pinned-resource-title></b><p class="mt-0.5 text-xs text-muted-foreground" data-pinned-resource-meta></p></div>
        </div>

        <div class="mb-4 flex flex-wrap gap-2">
            <t:filterChip group="resource" value="ALL" label="전체" active="true"/>
            <t:filterChip group="resource" value="SCRIPT" label="대본"/>
            <t:filterChip group="resource" value="MINUTES" label="회의록"/>
            <t:filterChip group="resource" value="PROMOTION" label="홍보물"/>
            <t:filterChip group="resource" value="VIDEO" label="영상"/>
            <t:filterChip group="resource" value="OTHER" label="기타"/>
        </div>

        <div class="rounded-lg border bg-card">
            <t:dataTable caption="자료 목록">
                <thead><tr><th>자료 제목</th><th>분류</th><th>버전</th><th>수정자</th><th>수정일</th><th></th></tr></thead>
                <tbody data-resource-list>
                    <tr data-resource-state><td colspan="6" class="px-5 py-11 text-center"><b class="block text-sm font-extrabold" data-resource-state-title>자료를 불러오는 중입니다</b><p class="mt-1 text-xs text-muted-foreground" data-resource-state-message>잠시만 기다려 주세요.</p><button type="button" class="mx-auto mt-4 hidden min-h-11 rounded-md border bg-card px-4 text-xs font-bold" data-resource-retry>다시 시도</button></td></tr>
                </tbody>
            </t:dataTable>
        </div>
    </section>

    <template data-resource-row-template>
        <tr data-resource-row><td data-resource-name class="font-bold"></td><td data-resource-category></td><td data-resource-version></td><td data-resource-uploader></td><td data-resource-date class="text-muted-foreground"></td><td class="text-right"><t:button variant="outline" size="compact" pageAction="resource-download">다운로드</t:button></td></tr>
    </template>
    <template data-notice-card-template>
        <article class="rounded-lg border bg-card p-4 md:p-5" data-notice-card>
            <div class="flex flex-col gap-3 md:flex-row md:items-start">
                <div class="min-w-0 flex-1"><div class="flex flex-wrap items-center gap-2" data-notice-badges></div><h3 class="mt-3 text-base font-black" data-notice-title></h3><p class="mt-3 text-xs text-muted-foreground" data-notice-meta></p></div>
                <t:button variant="outline" pageAction="notice-open" cssClass="w-full md:w-auto">상세 보기</t:button>
            </div>
        </article>
    </template>

    <t:modal id="uploadModal" title="자료 업로드" description="파일을 MinIO 비공개 저장소에 올린 뒤 자료 메타데이터와 함께 게시합니다.">
        <jsp:attribute name="footer"><t:button variant="outline" action="close-modal">취소</t:button><t:button pageAction="resource-upload">업로드</t:button></jsp:attribute>
        <jsp:body>
            <div class="flex flex-col gap-3">
                <div><label class="${label}" for="upName">자료 제목 <span class="text-accent-foreground">*</span></label><input class="${input}" id="upName" type="text" maxlength="200" placeholder="예) 정기공연 최종 대본"></div>
                <div class="grid grid-cols-1 gap-3 md:grid-cols-2">
                    <div><label class="${label}" for="upCat">분류</label><select class="${input}" id="upCat"><option value="SCRIPT">대본</option><option value="MINUTES">회의록</option><option value="PROMOTION">홍보물</option><option value="VIDEO">영상</option><option value="OTHER">기타</option></select></div>
                    <div><label class="${label}" for="upTarget">공개 대상</label><select class="${input}" id="upTarget"><option value="ALL">전체</option><option value="TEAM">내 팀</option></select></div>
                </div>
                <div><label class="${label}" for="upDescription">설명 <span class="text-accent-foreground">*</span></label><textarea class="min-h-24 w-full resize-y rounded-md border border-input bg-card px-3 py-2.5 text-sm" id="upDescription" placeholder="자료의 용도와 변경 사항을 입력하세요"></textarea></div>
                <div><label class="${label}" for="upFile">파일 <span class="text-accent-foreground">*</span></label><input class="block min-h-11 w-full rounded-md border border-input bg-card px-3 py-2 text-sm file:mr-3 file:rounded-md file:border-0 file:bg-secondary file:px-3 file:py-1.5 file:text-xs file:font-bold" id="upFile" type="file"></div>
                <label class="flex min-h-11 items-center gap-2 text-sm font-bold"><input id="upPinned" type="checkbox" class="size-4 rounded border-input"> 상단 고정 자료</label>
                <p class="hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-xs text-destructive" data-resource-form-error role="alert"></p>
            </div>
        </jsp:body>
    </t:modal>

    <t:modal id="noticeModal" title="짧은 공지 작성" description="간단한 전달 사항을 작성하고 즉시 게시합니다.">
        <jsp:attribute name="footer"><t:button variant="outline" action="close-modal">취소</t:button><t:button pageAction="notice-add">공지 게시</t:button></jsp:attribute>
        <jsp:body>
            <div class="flex flex-col gap-3">
                <div><label class="${label}" for="ntTarget">대상</label><select class="${input}" id="ntTarget"><option value="ALL">전체</option><option value="TEAM">내 팀</option></select></div>
                <div><label class="${label}" for="ntTitle">제목 <span class="text-accent-foreground">*</span></label><input class="${input}" id="ntTitle" type="text" maxlength="200" placeholder="공지 제목"></div>
                <div><label class="${label}" for="ntBody">내용 <span class="text-accent-foreground">*</span></label><textarea class="min-h-28 w-full resize-y rounded-md border border-input bg-card px-3 py-2.5 text-sm" id="ntBody" placeholder="전달 내용을 입력하세요"></textarea></div>
                <label class="flex min-h-11 items-center gap-2 text-sm font-bold"><input id="ntImportant" type="checkbox" class="size-4 rounded border-input"> 중요 공지로 표시</label>
                <p class="hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-xs text-destructive" data-notice-form-error role="alert"></p>
            </div>
        </jsp:body>
    </t:modal>

    <t:modal id="noticeDetailModal" title="공지 상세">
        <jsp:body>
            <div class="flex flex-wrap gap-2" data-notice-detail-badges></div>
            <h3 class="mt-4 text-lg font-black" data-notice-detail-title></h3>
            <p class="mt-2 whitespace-pre-wrap text-sm leading-6 text-muted-foreground" data-notice-detail-body></p>
            <p class="mt-4 border-t pt-3 text-xs text-muted-foreground" data-notice-detail-meta></p>
            <div class="mt-3 flex flex-col gap-2" data-notice-detail-files></div>
        </jsp:body>
    </t:modal>
</t:layout>
