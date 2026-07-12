<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layoutPublic title="페이지를 찾을 수 없습니다">
    <section class="flex flex-col items-center justify-center py-24 text-center">
        <h1 class="text-6xl font-black tracking-tight">404</h1>
        <p class="mt-4 text-sm text-muted-foreground">요청하신 페이지를 찾을 수 없습니다.</p>
        <a href="<c:url value='/'/>" class="mt-8 inline-flex h-9 items-center rounded-md bg-primary px-4 text-sm font-bold text-primary-foreground transition-colors hover:bg-primary-strong hover:text-white">홈으로</a>
    </section>
</t:layoutPublic>
