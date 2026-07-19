<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-bold text-muted-foreground"/>
<t:layout title="공지·자료실" active="resources" role="${role}" scriptPath="resources/list">
    <main class="w-full">
        <t:pageHead title="공지·자료실" description="먼저 확인할 공지를 읽고, 필요한 업무 자료를 찾아요"/>

        <nav class="mb-8 grid grid-cols-2 border-b" aria-label="공지·자료실 구분">
            <button type="button" class="min-h-11 border-b-2 border-primary px-3 text-sm font-bold text-foreground" data-info-tab="notices" aria-controls="internalNotices" aria-selected="true">공지</button>
            <button type="button" class="min-h-11 border-b-2 border-transparent px-3 text-sm font-bold text-muted-foreground" data-info-tab="resources" aria-controls="resourceLibrary" aria-selected="false">자료</button>
        </nav>

        <section id="internalNotices" data-info-panel="notices" aria-labelledby="internalNoticeTitle">
            <div class="flex flex-col gap-4 border-b pb-6 md:flex-row md:items-end">
                <div class="min-w-0 flex-1">
                    <h2 id="internalNoticeTitle" class="text-xl font-bold">읽어야 할 공지</h2>
                    <p class="mt-1 text-sm leading-6 text-muted-foreground">조회수 대신 내가 읽었는지 기록하고, 운영진은 대상별 확인 현황을 볼 수 있어요.</p>
                </div>
                <c:if test="${role != 'member'}">
                    <t:button pageAction="notice-create-open" cssClass="w-full md:w-auto">짧은 공지 작성</t:button>
                </c:if>
            </div>

            <section class="my-6 hidden border-l-4 border-primary bg-accent px-5 py-4" data-notice-priority aria-labelledby="noticePriorityTitle">
                <p class="text-xs font-bold text-accent-foreground">먼저 확인해 주세요</p>
                <h3 id="noticePriorityTitle" class="mt-1 text-lg font-bold" data-notice-priority-title></h3>
                <p class="mt-1 text-sm text-muted-foreground" data-notice-priority-meta></p>
                <t:button pageAction="notice-priority-open" cssClass="mt-4 w-full md:w-auto">중요 공지 읽기</t:button>
            </section>

            <div class="mb-5 flex gap-2 overflow-x-auto pb-1" aria-label="공지 필터">
                <t:filterChip group="notice" value="ALL" label="전체" active="true"/>
                <t:filterChip group="notice" value="UNREAD" label="내가 미확인" dot="true"/>
                <t:filterChip group="notice" value="ALL_SCOPE" label="전체 공지"/>
                <t:filterChip group="notice" value="TEAM_SCOPE" label="팀 공지"/>
            </div>

            <div class="border-y px-5 py-12 text-center" data-notice-state role="status" aria-live="polite">
                <b class="block text-sm font-bold" data-notice-state-title>공지를 불러오는 중입니다</b>
                <p class="mt-1 text-sm text-muted-foreground" data-notice-state-message>잠시만 기다려 주세요.</p>
                <button type="button" class="mx-auto mt-4 hidden min-h-11 rounded-md border bg-card px-4 text-sm font-bold" data-notice-retry>다시 시도</button>
            </div>
            <div class="hidden divide-y border-y" data-notice-list></div>
        </section>

        <section id="resourceLibrary" class="hidden" data-info-panel="resources" aria-labelledby="resourceLibraryTitle">
            <div class="flex flex-col gap-4 border-b pb-6 md:flex-row md:items-end">
                <div class="min-w-0 flex-1">
                    <h2 id="resourceLibraryTitle" class="text-xl font-bold">업무 자료 찾기</h2>
                    <p class="mt-1 text-sm leading-6 text-muted-foreground">제목과 분류로 찾고, 파일 교체 이력은 자료별로 확인해요.</p>
                </div>
                <c:if test="${role != 'member'}"><t:button pageAction="resource-create-open" cssClass="w-full md:w-auto">자료 업로드</t:button></c:if>
            </div>

            <div class="my-6 hidden items-start gap-3 border-l-4 border-primary bg-accent px-5 py-4" data-pinned-resource>
                <span class="mt-0.5 flex size-8 shrink-0 items-center justify-center rounded-md bg-primary text-primary-foreground">
                    <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M12 17v5M5 3h14l-2 6 2 6H5l2-6-2-6z"/></svg>
                </span>
                <div class="min-w-0 flex-1"><p class="text-xs font-bold text-accent-foreground">상단 고정 자료</p><b class="mt-1 block text-sm" data-pinned-resource-title></b><p class="mt-1 text-xs text-muted-foreground" data-pinned-resource-meta></p></div>
            </div>

            <div class="my-6 grid gap-3 md:grid-cols-[minmax(0,1fr)_auto] md:items-end">
                <div>
                    <label class="${label}" for="resourceQuery">자료 검색</label>
                    <input id="resourceQuery" data-resource-search class="${input}" type="search" name="resourceQuery" placeholder="자료 제목이나 수정자 입력">
                </div>
                <div class="flex gap-2 overflow-x-auto pb-1 md:max-w-xl" aria-label="자료 분류">
                    <t:filterChip group="resource" value="ALL" label="전체" active="true"/>
                    <t:filterChip group="resource" value="SCRIPT" label="대본"/>
                    <t:filterChip group="resource" value="MINUTES" label="회의록"/>
                    <t:filterChip group="resource" value="PROMOTION" label="홍보물"/>
                    <t:filterChip group="resource" value="VIDEO" label="영상"/>
                    <t:filterChip group="resource" value="OTHER" label="기타"/>
                </div>
            </div>

            <div class="border-y">
                <div data-resource-list></div>
                <div class="px-5 py-12 text-center" data-resource-state role="status" aria-live="polite">
                    <b class="block text-sm font-bold" data-resource-state-title>자료를 불러오는 중입니다</b>
                    <p class="mt-1 text-sm text-muted-foreground" data-resource-state-message>잠시만 기다려 주세요.</p>
                    <button type="button" class="mx-auto mt-4 hidden min-h-11 rounded-md border bg-card px-4 text-sm font-bold" data-resource-retry>다시 시도</button>
                </div>
            </div>
        </section>
    </main>

    <template data-resource-row-template>
        <article data-resource-row class="grid gap-3 border-b px-4 py-5 last:border-b-0 md:grid-cols-[minmax(0,1fr)_auto] md:items-center md:px-5">
            <div class="min-w-0">
                <div class="flex flex-wrap items-center gap-2"><span data-resource-category></span><span class="text-xs font-bold text-muted-foreground" data-resource-version></span></div>
                <h3 class="mt-2 break-words text-base font-bold" data-resource-name></h3>
                <p class="mt-1 text-xs text-muted-foreground"><span data-resource-uploader></span> · <span data-resource-date></span> 수정</p>
            </div>
            <div data-resource-actions class="grid grid-cols-2 gap-2 sm:flex sm:flex-wrap sm:justify-end"></div>
        </article>
    </template>
    <template data-notice-card-template>
        <article class="grid gap-3 px-4 py-5 md:grid-cols-[minmax(0,1fr)_auto] md:items-center md:px-5" data-notice-card>
            <div class="min-w-0">
                <div class="flex flex-wrap items-center gap-2" data-notice-badges></div>
                <h3 class="mt-2 break-words text-base font-bold" data-notice-title></h3>
                <p class="mt-1 text-xs text-muted-foreground" data-notice-meta></p>
            </div>
            <t:button variant="outline" pageAction="notice-open" cssClass="w-full md:w-auto">공지 읽기</t:button>
        </article>
    </template>

    <t:sheet id="uploadSheet" title="자료 업로드" description="파일은 MinIO 비공개 저장소에 보관해요." presentation="workspace">
        <jsp:attribute name="footer"><t:button variant="outline" action="close-sheet">취소</t:button><t:button pageAction="resource-upload"><span data-resource-submit-label>자료 업로드</span></t:button></jsp:attribute>
        <jsp:body>
            <div class="flex flex-col gap-4">
                <div><label class="${label}" for="upName">자료 제목 <span class="text-accent-foreground">*</span></label><input class="${input}" id="upName" type="text" maxlength="200" placeholder="예) 정기공연 최종 대본"></div>
                <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
                    <div><label class="${label}" for="upCat">분류</label><select class="${input}" id="upCat"><option value="SCRIPT">대본</option><option value="MINUTES">회의록</option><option value="PROMOTION">홍보물</option><option value="VIDEO">영상</option><option value="OTHER">기타</option></select></div>
                    <div><label class="${label}" for="upTarget">공개 대상</label><select class="${input}" id="upTarget"><c:if test="${role == 'admin'}"><option value="ALL">전체</option></c:if><option value="TEAM">내 팀</option></select></div>
                </div>
                <div class="hidden" data-resource-team-field><label class="${label}" for="upTeam">대상 팀</label><select class="${input}" id="upTeam"></select></div>
                <div><label class="${label}" for="upDescription">설명 <span class="text-accent-foreground">*</span></label><textarea class="min-h-24 w-full resize-y rounded-md border border-input bg-card px-3 py-2.5 text-sm focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20" id="upDescription" placeholder="자료 용도와 변경 사항을 입력해 주세요"></textarea></div>
                <div><label class="${label}" for="upFile">파일 <span class="text-accent-foreground">*</span></label><input class="block min-h-11 w-full rounded-md border border-input bg-card px-3 py-2 text-sm file:mr-3 file:rounded-md file:border-0 file:bg-secondary file:px-3 file:py-1.5 file:text-xs file:font-bold" id="upFile" type="file"></div>
                <label class="flex min-h-11 items-center gap-2 text-sm font-bold"><input id="upPinned" type="checkbox" class="size-4 rounded border-input"> 상단 고정 자료로 표시</label>
                <p class="hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-sm text-destructive" data-resource-form-error role="alert"></p>
            </div>
        </jsp:body>
    </t:sheet>

    <t:sheet id="noticeSheet" title="짧은 공지 작성" description="간단한 전달 사항을 바로 게시해요." presentation="form">
        <jsp:attribute name="footer"><t:button variant="outline" action="close-sheet">취소</t:button><t:button pageAction="notice-add"><span data-notice-submit-label>공지 게시</span></t:button></jsp:attribute>
        <jsp:body>
            <div class="flex flex-col gap-4">
                <div><label class="${label}" for="ntTarget">대상</label><select class="${input}" id="ntTarget"><c:if test="${role == 'admin'}"><option value="ALL">전체</option></c:if><option value="TEAM">내 팀</option></select></div>
                <div class="hidden" data-notice-team-field><label class="${label}" for="ntTeam">대상 팀</label><select class="${input}" id="ntTeam"></select></div>
                <div><label class="${label}" for="ntTitle">제목 <span class="text-accent-foreground">*</span></label><input class="${input}" id="ntTitle" type="text" maxlength="200" placeholder="공지 제목"></div>
                <div><label class="${label}" for="ntBody">내용 <span class="text-accent-foreground">*</span></label><textarea class="min-h-40 w-full resize-y rounded-md border border-input bg-card px-3 py-2.5 text-sm focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20" id="ntBody" placeholder="전달 내용을 입력해 주세요"></textarea></div>
                <label class="flex min-h-11 items-center gap-2 text-sm font-bold"><input id="ntImportant" type="checkbox" class="size-4 rounded border-input"> 중요 공지로 표시</label>
                <p class="hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-sm text-destructive" data-notice-form-error role="alert"></p>
            </div>
        </jsp:body>
    </t:sheet>

    <t:sheet id="noticeDetailSheet" title="공지 상세">
        <jsp:body>
            <div class="flex flex-wrap gap-2" data-notice-detail-badges></div>
            <h3 class="mt-4 text-xl font-bold" data-notice-detail-title></h3>
            <p class="mt-4 whitespace-pre-wrap text-sm leading-7 text-foreground" data-notice-detail-body></p>
            <p class="mt-6 border-t pt-4 text-xs text-muted-foreground" data-notice-detail-meta></p>
            <div class="mt-3 flex flex-col gap-2" data-notice-detail-files></div>
            <div class="mt-6 hidden grid grid-cols-2 gap-2 border-t pt-4" data-notice-management-actions>
                <t:button variant="outline" pageAction="notice-edit">공지 수정</t:button>
                <t:button variant="outline" pageAction="notice-read-statuses">확인 현황</t:button>
                <t:button variant="outline" pageAction="notice-close" confirm="공지 게시를 종료할까요?" confirmAction="게시 종료">게시 종료</t:button>
                <t:button variant="danger" pageAction="notice-archive" confirm="이 공지를 보관할까요?" confirmAction="공지 보관">공지 보관</t:button>
            </div>
        </jsp:body>
    </t:sheet>

    <t:sheet id="noticeReadSheet" title="공지 확인 현황" description="대상 멤버의 최초 확인 여부를 보여줘요."><jsp:body><div data-notice-read-list></div></jsp:body></t:sheet>
    <t:sheet id="resourceHistorySheet" title="자료 파일 이력"><jsp:body><h3 data-resource-history-title class="text-base font-bold"></h3><div data-resource-history-list class="mt-3"></div></jsp:body></t:sheet>
</t:layout>
