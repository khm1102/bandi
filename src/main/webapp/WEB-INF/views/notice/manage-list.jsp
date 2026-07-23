<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout title="공지 관리" active="notices" role="${role}" scriptPath="notice/manage-list">
    <div data-notice-manage-root data-role="<c:out value='${role}'/>">
        <t:pageHead title="공지 관리" description="작성 중인 공지와 게시 상태를 확인하고 관리하세요.">
            <t:button href="/notices/write">새 공지 작성</t:button>
        </t:pageHead>
        <div class="mb-5 flex flex-col gap-3 md:flex-row">
            <input type="search" class="h-11 min-w-0 flex-1 rounded-md border border-input bg-card px-3 text-sm focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20" data-manage-search placeholder="제목과 내용으로 검색" aria-label="관리 공지 검색">
            <select class="h-11 rounded-md border border-input bg-card px-3 text-sm md:w-44" data-manage-status aria-label="공지 상태">
                <option value="">전체 상태</option>
                <option value="DRAFT">초안</option>
                <option value="SCHEDULED">예약</option>
                <option value="PUBLISHED">게시 중</option>
                <option value="CLOSED">게시 종료</option>
                <option value="ARCHIVED">보관</option>
            </select>
            <c:if test="${role == 'admin'}">
                <select class="h-11 rounded-md border border-input bg-card px-3 text-sm md:w-44" data-manage-scope aria-label="공지 대상">
                    <option value="">전체 대상</option>
                    <option value="ALL">전체 공지</option>
                    <option value="TEAM">팀 공지</option>
                </select>
                <select class="hidden h-11 rounded-md border border-input bg-card px-3 text-sm md:w-44" data-manage-team aria-label="대상 팀">
                    <option value="">모든 팀</option>
                </select>
            </c:if>
        </div>
        <section class="overflow-hidden rounded-xl border bg-card" aria-labelledby="manageListTitle">
            <h2 id="manageListTitle" class="sr-only">관리할 공지 목록</h2>
            <div class="divide-y" data-manage-list></div>
            <div class="px-5 py-12 text-center" data-manage-state>
                <b class="block text-sm font-extrabold" data-manage-state-title>공지를 불러오는 중입니다</b>
                <p class="mt-1 text-xs text-muted-foreground" data-manage-state-message>잠시만 기다려 주세요.</p>
                <button type="button" class="mt-4 hidden min-h-11 rounded-md border bg-card px-4 text-sm font-bold" data-manage-reset>필터 초기화</button>
                <button type="button" class="mt-4 hidden min-h-11 rounded-md border bg-card px-4 text-sm font-bold" data-manage-retry>다시 시도</button>
            </div>
        </section>
        <t:pagination id="noticeManagePagination" label="공지 관리 목록 페이지"/>
        <template data-manage-row-template>
            <a class="block px-5 py-5 transition-colors hover:bg-secondary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-ring" data-manage-link>
                <div class="flex flex-wrap items-center gap-2" data-manage-badges></div>
                <div class="mt-3 flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
                    <div class="min-w-0">
                        <b class="block truncate text-base" data-manage-title></b>
                        <p class="mt-1 text-xs text-muted-foreground" data-manage-created></p>
                        <p class="mt-1 text-xs text-muted-foreground" data-manage-updated></p>
                    </div>
                    <span class="text-sm font-bold text-primary" data-manage-action-label></span>
                </div>
            </a>
        </template>
    </div>
</t:layout>
