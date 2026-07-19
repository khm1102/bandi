<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layoutPublic title="페이지를 찾을 수 없습니다">
    <section class="mx-auto max-w-xl py-16 md:py-24" aria-labelledby="errorTitle">
        <p class="text-sm font-bold text-accent-foreground">페이지 안내 · 404</p>
        <h1 id="errorTitle" class="mt-3 text-3xl font-extrabold tracking-tight">요청한 페이지를 찾을 수 없어요</h1>
        <p class="mt-4 text-sm leading-7 text-muted-foreground">주소가 바뀌었거나 공개 기간이 끝난 페이지일 수 있어요. 주소를 다시 확인하거나 최신 공연·관람 정보가 있는 공시로 이동해 주세요.</p>
        <div class="mt-7 flex flex-col gap-2 sm:flex-row">
            <a href="<c:url value='/notices'/>" class="inline-flex min-h-11 items-center justify-center rounded-md bg-primary px-4 text-sm font-bold text-primary-foreground transition-colors hover:bg-primary-strong">최신 공시 확인</a>
            <a href="<c:url value='/'/>" class="inline-flex min-h-11 items-center justify-center rounded-md border bg-card px-4 text-sm font-bold hover:bg-secondary">홈으로 이동</a>
        </div>
    </section>
</t:layoutPublic>
