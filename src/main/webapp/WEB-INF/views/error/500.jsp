<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layoutPublic title="요청을 처리하지 못했습니다">
    <section class="mx-auto max-w-xl py-16 md:py-24" aria-labelledby="errorTitle">
        <p class="text-sm font-bold text-destructive">일시적인 오류 · 500</p>
        <h1 id="errorTitle" class="mt-3 text-3xl font-extrabold tracking-tight">요청을 마치지 못했어요</h1>
        <p class="mt-4 text-sm leading-7 text-muted-foreground">서버가 요청을 처리하는 중 문제가 생겼어요. 작성 중이던 내용은 저장되지 않았을 수 있으니 같은 작업을 다시 하기 전에 현재 상태를 확인해 주세요. 문제가 계속되면 운영 공시를 확인해 주세요.</p>
        <div class="mt-7 flex flex-col gap-2 sm:flex-row">
            <a href="<c:url value='/'/>" class="inline-flex min-h-11 items-center justify-center rounded-md bg-primary px-4 text-sm font-bold text-primary-foreground transition-colors hover:bg-primary-strong">홈에서 상태 확인</a>
            <a href="<c:url value='/notices'/>" class="inline-flex min-h-11 items-center justify-center rounded-md border bg-card px-4 text-sm font-bold hover:bg-secondary">운영 공시 보기</a>
        </div>
    </section>
</t:layoutPublic>
