<%@ tag description="공개 셸 상단 내비 (네이비)" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<header class="sticky top-0 z-20 bg-sidebar text-white">
    <nav class="mx-auto flex max-w-5xl items-center gap-2.5 px-6 py-4">
        <a href="<c:url value='/reserve'/>" class="flex items-center gap-2.5 font-black">
            <span class="flex size-8 items-center justify-center rounded-md bg-primary text-base">🎭</span>
            반디
        </a>
        <div class="ml-auto flex items-center gap-1 text-sm font-bold">
            <span class="hidden rounded-md px-3 py-1.5 text-sidebar-muted md:block">공연 소개</span>
            <span class="hidden rounded-md px-3 py-1.5 text-sidebar-muted md:block">일정</span>
            <a href="<c:url value='/reserve'/>" class="rounded-md bg-sidebar-accent px-3 py-1.5 text-white">관람 신청</a>
            <span class="hidden rounded-md px-3 py-1.5 text-sidebar-muted md:block">문의</span>
        </div>
        <a href="<c:url value='/login'/>" class="rounded-md border border-sidebar-border px-3 py-1.5 text-xs font-bold text-sidebar-foreground transition-colors hover:bg-sidebar-accent hover:text-white">← 나가기</a>
    </nav>
</header>
