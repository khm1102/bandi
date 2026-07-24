<%@ tag description="오류 화면용 공개 셸 푸터" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<footer class="border-t bg-card px-4 py-7 text-sm text-muted-foreground md:px-6">
    <div class="mx-auto flex max-w-5xl flex-col gap-4 md:flex-row md:items-center md:justify-between">
        <div>
            <p class="font-black text-foreground">한국공학대학교 연극동아리 반디</p>
            <p class="mt-2 text-xs leading-5">멤버 전용 동아리 운영 시스템입니다.</p>
        </div>
        <a href="<c:url value='/login'/>" class="inline-flex min-h-11 items-center text-xs font-bold text-accent-foreground underline-offset-4 hover:underline">멤버 로그인</a>
    </div>
    <div class="mx-auto mt-6 max-w-5xl border-t pt-4 text-xs">&copy; bandi.</div>
</footer>
