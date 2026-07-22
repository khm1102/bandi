<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layoutPublic title="공시" active="notices">
    <section class="border-b pb-6">
        <p class="text-xs font-black uppercase tracking-[0.18em] text-accent-foreground">Bandi official</p>
        <h1 class="mt-2 text-3xl font-black tracking-tight">공시</h1>
        <p class="mt-3 max-w-2xl text-sm leading-6 text-muted-foreground">동아리 운영과 모집에 관한 반디의 공식 안내입니다. 일반 글·댓글·좋아요 없이 운영진이 게시한 정보만 제공합니다.</p>
    </section>

    <section class="py-6" aria-labelledby="noticeListTitle">
        <div class="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
            <div>
                <h2 id="noticeListTitle" class="text-lg font-black"><c:choose><c:when test="${not empty keyword}">검색 결과</c:when><c:otherwise>전체 공시</c:otherwise></c:choose></h2>
                <p class="mt-1 text-xs text-muted-foreground">중요 공시 우선, 이후 최신순으로 표시합니다.</p>
            </div>
            <form method="get" action="<c:url value='/notices'/>" role="search" class="flex w-full gap-2 md:w-auto">
                <label class="min-w-0 flex-1 md:w-72">
                    <span class="sr-only">공시 검색</span>
                    <input name="keyword" value="<c:out value='${keyword}'/>" type="search" maxlength="200" class="h-11 w-full rounded-md border border-input bg-card px-3 text-base focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm" placeholder="제목·내용 검색">
                </label>
                <t:button type="submit">검색</t:button>
            </form>
        </div>

        <c:if test="${not empty keyword}">
            <div class="mt-4 flex flex-wrap items-center gap-2 rounded-md bg-secondary px-4 py-3 text-sm">
                <span class="font-bold">검색어</span>
                <strong><c:out value="${keyword}"/></strong>
                <a href="<c:url value='/notices'/>" class="ml-auto inline-flex min-h-11 items-center text-xs font-black text-accent-foreground underline-offset-4 hover:underline">검색 지우기</a>
            </div>
        </c:if>

        <c:choose>
            <c:when test="${empty notices}">
                <div class="mt-5 rounded-lg border bg-card px-5 py-14 text-center">
                    <h3 class="text-base font-black">표시할 공시가 없습니다</h3>
                    <p class="mt-2 text-sm text-muted-foreground"><c:choose><c:when test="${not empty keyword}">다른 검색어로 다시 확인해 주세요.</c:when><c:otherwise>새 공식 안내가 게시되면 이곳에 표시됩니다.</c:otherwise></c:choose></p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="mt-5 divide-y rounded-lg border bg-card">
                    <c:forEach var="notice" items="${notices}">
                        <c:url var="noticeUrl" value="/notices/${notice.publicNoticeId}"/>
                        <article class="p-5 transition-colors hover:bg-secondary/50 ${notice.pinned ? 'bg-accent/40' : ''}">
                            <div class="flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
                                <c:if test="${notice.pinned}"><t:badge tone="accent">중요</t:badge></c:if>
                                <t:badge tone="neutral"><c:choose><c:when test="${notice.categoryCode == 'RECRUITMENT'}">모집</c:when><c:otherwise>일반</c:otherwise></c:choose></t:badge>
                                <time datetime="<c:out value='${notice.publishStartDttm}'/>"><c:out value="${fn:replace(notice.publishStartDttm, 'T', ' ')}"/></time>
                            </div>
                            <h3 class="mt-3 text-base font-black"><a href="<c:out value='${noticeUrl}'/>" class="inline-flex min-h-11 items-center underline-offset-4 hover:text-accent-foreground hover:underline"><c:out value="${notice.title}"/></a></h3>
                            <p class="mt-1 text-xs text-muted-foreground">게시 <c:out value="${notice.createdByName}"/><c:if test="${notice.updatedDttm != notice.publishStartDttm}"> · 수정 <c:out value="${fn:replace(notice.updatedDttm, 'T', ' ')}"/></c:if></p>
                        </article>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>

        <c:if test="${hasPrevious || hasNext}">
            <c:url var="previousUrl" value="/notices"><c:param name="keyword" value="${keyword}"/><c:param name="page" value="${page - 1}"/></c:url>
            <c:url var="nextUrl" value="/notices"><c:param name="keyword" value="${keyword}"/><c:param name="page" value="${page + 1}"/></c:url>
            <nav class="mt-6 flex items-center justify-between" aria-label="공시 페이지">
                <c:choose><c:when test="${hasPrevious}"><a href="<c:out value='${previousUrl}'/>" class="inline-flex min-h-11 items-center rounded-md border bg-card px-4 text-sm font-bold hover:bg-secondary">이전</a></c:when><c:otherwise><span></span></c:otherwise></c:choose>
                <span class="text-xs font-bold text-muted-foreground"><c:out value="${page + 1}"/>페이지</span>
                <c:if test="${hasNext}"><a href="<c:out value='${nextUrl}'/>" class="inline-flex min-h-11 items-center rounded-md border bg-card px-4 text-sm font-bold hover:bg-secondary">다음</a></c:if>
            </nav>
        </c:if>
    </section>
</t:layoutPublic>
