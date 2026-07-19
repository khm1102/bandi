<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layoutPublic title="공시" active="notices">
    <section class="border-b pb-6">
        <p class="text-xs font-black text-accent-foreground">BANDI OFFICIAL</p>
        <h1 class="mt-2 text-3xl font-black tracking-tight">공시</h1>
        <p class="mt-3 max-w-2xl text-sm leading-6 text-muted-foreground">모집, 공연, 관람 운영에 관한 반디의 공식 안내입니다. 일반 글·댓글·좋아요 없이 운영진이 게시한 정보만 제공합니다.</p>
    </section>

    <section class="py-6" aria-labelledby="importantNoticeTitle">
        <h2 id="importantNoticeTitle" class="text-sm font-black">중요 공시</h2>
        <article class="mt-3 rounded-lg border border-primary/40 bg-accent/50 p-5">
            <div class="flex flex-wrap items-center gap-2">
                <t:badge tone="accent">중요</t:badge>
                <t:badge tone="neutral">공연</t:badge>
                <time class="ml-auto text-xs text-muted-foreground" datetime="2025-06-18">2025.06.18</time>
            </div>
            <h3 class="mt-3 text-lg font-black">2025 정기공연 관람 및 입장 안내</h3>
            <p class="mt-2 text-sm leading-6 text-muted-foreground">공연 회차, 입장 시작 시각, 지연 입장과 관람 유의사항을 안내합니다.</p>
            <a href="<c:url value='/reserve'/>" class="mt-4 inline-flex min-h-11 items-center text-sm font-bold text-accent-foreground underline-offset-4 hover:underline">관람 신청으로 이동</a>
        </article>
    </section>

    <section class="border-t py-6" aria-labelledby="noticeListTitle">
        <div class="flex flex-wrap items-end gap-3">
            <div>
                <h2 id="noticeListTitle" class="text-lg font-black">전체 공시</h2>
                <p class="mt-1 text-xs text-muted-foreground">중요 공시 우선, 이후 최신순으로 표시합니다.</p>
            </div>
            <label class="ml-auto w-full md:w-64">
                <span class="sr-only">공시 검색</span>
                <input type="search" class="h-11 w-full rounded-md border border-input bg-card px-3 text-base focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm" placeholder="제목·내용 검색">
            </label>
        </div>

        <div class="mt-4 divide-y rounded-lg border bg-card">
            <article class="p-4 md:p-5">
                <div class="flex flex-wrap items-center gap-2 text-xs text-muted-foreground"><t:badge tone="neutral">모집</t:badge><time datetime="2025-03-04">2025.03.04</time></div>
                <h3 class="mt-2 text-base font-black">2025-1학기 신입 부원 모집 결과 안내</h3>
                <p class="mt-1 text-sm leading-6 text-muted-foreground">신입 부원 등록 일정과 운영진 문의 방법을 안내합니다.</p>
            </article>
            <article class="p-4 md:p-5">
                <div class="flex flex-wrap items-center gap-2 text-xs text-muted-foreground"><t:badge tone="neutral">공연</t:badge><time datetime="2024-12-10">2024.12.10</time></div>
                <h3 class="mt-2 text-base font-black">동계 공연 기록 공개 안내</h3>
                <p class="mt-1 text-sm leading-6 text-muted-foreground">공연 기록 영상과 사진 공개 범위를 안내합니다.</p>
            </article>
        </div>
    </section>
</t:layoutPublic>
