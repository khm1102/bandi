<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layoutPublic title="${shareTitle}" description="${shareDescription}" robots="noindex, noarchive" openGraphTitle="${shareTitle}" openGraphDescription="${shareDescription}">
    <jsp:body>
        <article class="mx-auto max-w-2xl rounded-xl border bg-card p-5 md:p-8">
            <p class="text-sm font-bold text-accent-foreground">반디 내부 게시글</p>
            <h1 class="mt-3 text-2xl font-black tracking-tight"><c:out value="${shareTitle}"/></h1>
            <p class="mt-4 text-sm text-muted-foreground">로그인 후 내용을 확인할 수 있어요.</p>
            <a href="<c:url value='${detailPath}'/>" class="mt-7 inline-flex min-h-11 items-center justify-center rounded-md bg-primary px-4 text-sm font-bold text-primary-foreground hover:bg-primary-strong hover:text-white">로그인하고 글 보기</a>
        </article>
    </jsp:body>
</t:layoutPublic>
