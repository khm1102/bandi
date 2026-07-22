<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout title="공지" active="notices" role="${role}" scriptPath="notice/list">
    <t:pageHead title="공지" description="전체 공지와 내 팀 공지를 확인하세요.">
        <c:if test="${role != 'member'}"><t:button href="/notices/write">공지 작성</t:button></c:if>
    </t:pageHead>
    <div class="mb-4 flex flex-col gap-3 md:flex-row">
        <input class="h-11 min-w-0 flex-1 rounded-md border border-input bg-card px-3 text-sm focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20" type="search" data-notice-search placeholder="제목과 내용으로 검색" aria-label="공지 검색">
        <div class="flex flex-wrap gap-2" aria-label="공지 필터">
            <t:filterChip group="notice-read" value="ALL" label="전체" active="true"/>
            <t:filterChip group="notice-read" value="UNREAD" label="미확인" dot="true"/>
            <t:filterChip group="notice-scope" value="ANY" label="전체 범위" active="true"/>
            <t:filterChip group="notice-scope" value="TEAM" label="팀 공지"/>
        </div>
    </div>
    <section class="rounded-lg border bg-card" aria-live="polite">
        <div class="divide-y" data-notice-list></div>
        <div class="px-5 py-11 text-center" data-notice-state>
            <b class="block text-sm font-extrabold" data-notice-state-title>공지를 불러오는 중입니다</b>
            <p class="mt-1 text-xs text-muted-foreground" data-notice-state-message>잠시만 기다려 주세요.</p>
            <button type="button" class="mt-4 hidden min-h-11 rounded-md border px-4 text-sm font-bold" data-notice-retry>다시 시도</button>
        </div>
    </section>
    <button type="button" class="mx-auto mt-5 hidden min-h-11 rounded-md border bg-card px-4 text-sm font-bold" data-notice-more>공지 더 보기</button>
    <template data-notice-row-template>
        <a class="block px-5 py-4 transition-colors hover:bg-secondary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-ring" data-notice-link>
            <div class="flex flex-wrap items-center gap-2" data-notice-badges></div>
            <b class="mt-2 block text-sm" data-notice-title></b>
            <span class="mt-1 block text-xs text-muted-foreground" data-notice-meta></span>
        </a>
    </template>
</t:layout>
