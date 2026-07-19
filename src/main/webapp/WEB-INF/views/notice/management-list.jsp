<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 rounded-md border border-input bg-card px-3 text-base focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<t:layout title="공시 관리" active="notice-management" role="${role}" scriptPath="notice/management-list">
    <main class="mx-auto w-full max-w-5xl">
        <t:pageHead title="공시 관리" description="외부에 공개할 공식 안내의 게시 상태와 기간을 관리해요"><t:button href="/notice-management/write">새 공시 작성</t:button></t:pageHead>

        <section class="mb-8" aria-labelledby="noticeManageNextTitle"><p class="text-sm font-bold text-accent-foreground">다음에 할 일</p><div class="mt-2 grid gap-4 border-l-4 border-primary bg-accent px-5 py-5 md:grid-cols-[minmax(0,1fr)_auto] md:items-center"><div><h2 id="noticeManageNextTitle" class="text-lg font-bold" data-notice-next-title>공시 상태를 확인하고 있어요</h2><p class="mt-1 text-sm leading-6 text-muted-foreground" data-notice-next-message>잠시만 기다려 주세요.</p></div><span class="hidden" data-notice-next-action><t:button pageAction="notice-next-action" cssClass="w-full md:w-auto">공시 이어서 작성</t:button></span></div></section>

        <section class="mb-6 grid gap-3 border-y py-5 md:grid-cols-[minmax(0,1fr)_12rem_auto] md:items-end" aria-label="공시 검색">
            <label><span class="mb-1.5 block text-xs font-bold text-muted-foreground">공시 검색</span><input id="noticeManageKeyword" class="${input} w-full" type="search" maxlength="200" placeholder="제목·본문 검색"></label>
            <label><span class="mb-1.5 block text-xs font-bold text-muted-foreground">게시 상태</span><select id="noticeManageStatus" class="${input} w-full"><option value="">전체 상태</option><option value="DRAFT">초안</option><option value="SCHEDULED">게시 예정</option><option value="PUBLISHED">게시 중</option><option value="CLOSED">게시 종료</option><option value="ARCHIVED">보관</option></select></label>
            <t:button pageAction="notice-search" cssClass="w-full md:w-auto">공시 찾기</t:button>
        </section>

        <div class="border-y" data-notice-manage-list><p class="px-5 py-12 text-center text-sm text-muted-foreground">공시를 불러오는 중입니다.</p></div>
    </main>

    <t:sheet id="noticePublishSheet" title="공시 게시 설정" description="시작 시각이 미래이면 예약 게시돼요.">
        <jsp:attribute name="footer"><t:button variant="outline" action="close-sheet">취소</t:button><t:button pageAction="notice-publish-save">게시 설정 저장</t:button></jsp:attribute>
        <jsp:body><form data-notice-publish-form class="grid gap-4"><p data-notice-publish-title class="border-l-4 border-primary bg-accent px-3 py-2.5 text-sm font-bold"></p><label class="text-xs font-bold text-muted-foreground">게시 시작 *<input id="noticePublishStart" class="${input} mt-1.5 w-full" type="datetime-local" required></label><label class="text-xs font-bold text-muted-foreground">게시 종료<input id="noticePublishEnd" class="${input} mt-1.5 w-full" type="datetime-local"></label></form></jsp:body>
    </t:sheet>
</t:layout>
