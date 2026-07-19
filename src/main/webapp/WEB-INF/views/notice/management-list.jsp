<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 rounded-md border border-input bg-card px-3 text-base focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<t:layout title="공시 관리" active="notice-management" role="${role}" scriptPath="notice/management-list">
    <t:pageHead title="공시 관리" description="외부에 공개되는 공식 안내를 작성하고 게시 기간을 관리합니다">
        <t:button href="/notice-management/write">공시 작성</t:button>
    </t:pageHead>
    <section class="mb-4 flex flex-col gap-3 rounded-lg border bg-card p-4 md:flex-row" aria-label="공시 검색">
        <label class="min-w-0 flex-1"><span class="sr-only">검색어</span><input id="noticeManageKeyword" class="${input} w-full" type="search" maxlength="200" placeholder="제목·본문 검색"></label>
        <label><span class="sr-only">게시 상태</span><select id="noticeManageStatus" class="${input} w-full md:w-40"><option value="">전체 상태</option><option value="DRAFT">초안</option><option value="SCHEDULED">게시 예정</option><option value="PUBLISHED">게시 중</option><option value="CLOSED">게시 종료</option><option value="ARCHIVED">보관</option></select></label>
        <t:button pageAction="notice-search">검색</t:button>
    </section>
    <t:card flush="true">
        <t:dataTable caption="공시 관리 목록">
            <thead><tr><th>공시</th><th>상태</th><th>게시 기간</th><th>수정 정보</th><th class="text-right">관리</th></tr></thead>
            <tbody data-notice-manage-list><tr><td colspan="5" class="px-5 py-12 text-center text-sm text-muted-foreground">공시를 불러오는 중입니다.</td></tr></tbody>
        </t:dataTable>
    </t:card>
    <t:modal id="noticePublishModal" title="공시 게시" description="시작 시각이 미래이면 예약 게시됩니다.">
        <jsp:attribute name="footer"><t:button variant="outline" action="close-modal">취소</t:button><t:button pageAction="notice-publish-save">게시 설정 저장</t:button></jsp:attribute>
        <jsp:body><form data-notice-publish-form class="grid gap-4"><p data-notice-publish-title class="rounded-md bg-secondary px-3 py-2.5 text-sm font-bold"></p><label class="text-xs font-extrabold text-muted-foreground">게시 시작 *<input id="noticePublishStart" class="${input} mt-1.5 w-full" type="datetime-local" required></label><label class="text-xs font-extrabold text-muted-foreground">게시 종료<input id="noticePublishEnd" class="${input} mt-1.5 w-full" type="datetime-local"></label></form></jsp:body>
    </t:modal>
</t:layout>
