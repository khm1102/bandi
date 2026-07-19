<%@ tag description="공개 셸 푸터 — 운영 주체, 관람 안내, 문의·개인정보 경로" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<footer class="border-t bg-card px-4 py-7 text-sm text-muted-foreground md:px-6">
    <div class="mx-auto grid max-w-5xl gap-6 md:grid-cols-3">
        <div>
            <p class="font-black text-foreground">한국공학대학교 연극동아리 반디</p>
            <p class="mt-2 text-xs leading-5">공연 제작과 관람 운영에 관한 공식 안내는 공시에서 제공합니다.</p>
        </div>
        <div>
            <p class="text-xs font-extrabold text-foreground">관람 안내</p>
            <div class="mt-2 flex flex-col gap-1.5 text-xs">
                <a href="<c:url value='/reserve'/>" class="w-fit underline-offset-4 hover:text-foreground hover:underline">관람 신청 조회·취소</a>
                <a href="<c:url value='/notices'/>" class="w-fit underline-offset-4 hover:text-foreground hover:underline">입장·취소·접근성 공시</a>
            </div>
        </div>
        <div>
            <p class="text-xs font-extrabold text-foreground">운영·개인정보 안내</p>
            <p class="mt-2 text-xs leading-5">대표 문의 수단과 개인정보 담당 연락처는 공개 전 확정하며, 최신 연락처는 공시에 고지합니다.</p>
            <a href="<c:url value='/notices'/>" class="mt-2 inline-flex min-h-11 items-center text-xs font-bold text-accent-foreground underline-offset-4 hover:underline">운영 공시 확인</a>
        </div>
    </div>
    <div class="mx-auto mt-6 max-w-5xl border-t pt-4 text-xs">&copy; bandi. 공연 사진과 포스터의 권리는 각 제작자에게 있습니다.</div>
</footer>
