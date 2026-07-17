<%@ tag description="공개 셸 상단 내비 (네이비)" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<header class="sticky top-0 z-20 bg-sidebar text-white">
    <nav class="mx-auto flex max-w-5xl items-center gap-2.5 px-4 py-3 md:px-6" aria-label="공개 메뉴">
        <a href="<c:url value='/reserve'/>" class="flex min-h-11 items-center gap-2.5 font-black">
            <span class="flex size-8 items-center justify-center rounded-md bg-primary text-sm font-black text-primary-foreground">B</span>
            반디
        </a>
        <a href="<c:url value='/reserve'/>" class="ml-auto inline-flex min-h-11 items-center rounded-md bg-sidebar-accent px-3 text-sm font-bold text-white" aria-current="page">관람 신청</a>
        <a href="<c:url value='/login'/>" class="inline-flex min-h-11 items-center rounded-md border border-sidebar-border px-3 text-xs font-bold text-sidebar-foreground transition-colors hover:bg-sidebar-accent hover:text-white">멤버 로그인</a>
    </nav>
</header>
