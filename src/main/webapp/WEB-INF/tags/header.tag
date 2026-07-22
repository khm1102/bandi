<%@ tag description="오류 화면용 공개 셸 상단 내비 (네이비)" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="active" %>
<header class="sticky top-0 z-20 bg-sidebar text-white">
    <nav class="mx-auto flex max-w-5xl items-center gap-1 px-3 py-3 md:gap-2.5 md:px-6" aria-label="공개 화면 메뉴">
        <a href="<c:url value='/login'/>" class="flex min-h-11 shrink-0 items-center gap-2 font-black">
            <span class="flex size-8 items-center justify-center rounded-md bg-primary text-sm font-black text-primary-foreground">B</span>
            <span class="hidden md:inline">반디</span>
        </a>
        <a href="<c:url value='/login'/>" class="ml-auto inline-flex min-h-11 items-center rounded-md border border-sidebar-border px-2.5 text-xs font-bold text-sidebar-foreground transition-colors hover:bg-sidebar-accent hover:text-white md:px-3">멤버 로그인</a>
    </nav>
</header>
