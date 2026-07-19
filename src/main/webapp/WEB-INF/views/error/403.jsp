<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layoutPublic title="접근 권한이 없습니다">
    <section class="mx-auto max-w-xl py-16 md:py-24" aria-labelledby="errorTitle">
        <p class="text-sm font-bold text-accent-foreground">접근 권한 안내 · 403</p>
        <h1 id="errorTitle" class="mt-3 text-3xl font-extrabold tracking-tight">이 메뉴를 사용할 권한이 없어요</h1>
        <p class="mt-4 text-sm leading-7 text-muted-foreground">현재 로그인한 역할로는 요청한 메뉴를 열 수 없어요. 이 화면 때문에 데이터가 변경되지는 않았어요. 권한이 필요하다면 동아리 운영진에게 멤버 역할을 확인해 달라고 요청해 주세요.</p>
        <div class="mt-7 flex flex-col gap-2 sm:flex-row">
            <a href="<c:url value='/'/>" class="inline-flex min-h-11 items-center justify-center rounded-md bg-primary px-4 text-sm font-bold text-primary-foreground transition-colors hover:bg-primary-strong">내가 사용할 수 있는 홈으로</a>
            <a href="<c:url value='/notices'/>" class="inline-flex min-h-11 items-center justify-center rounded-md border bg-card px-4 text-sm font-bold hover:bg-secondary">운영 공시 보기</a>
        </div>
    </section>
</t:layoutPublic>
