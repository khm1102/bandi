<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layoutPublic title="공시" active="notices">
    <section class="border-b pb-6">
        <h1 class="text-3xl font-extrabold tracking-tight">공시</h1>
        <p class="mt-3 max-w-2xl text-sm leading-6 text-muted-foreground">모집, 공연, 관람 운영에 관한 반디의 공식 안내예요. 공연 시간이나 입장 방법이 바뀌면 이곳에서 가장 먼저 확인해 주세요.</p>
    </section>

    <section class="py-6" aria-labelledby="noticeListTitle">
        <div class="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
            <div>
                <h2 id="noticeListTitle" class="text-lg font-bold"><c:choose><c:when test="${not empty keyword}">검색 결과</c:when><c:otherwise>전체 공시</c:otherwise></c:choose></h2>
                <p class="mt-1 text-xs text-muted-foreground">중요 공시를 먼저, 나머지는 최신순으로 보여줘요.</p>
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
                <a href="<c:url value='/notices'/>" class="ml-auto inline-flex min-h-11 items-center text-xs font-bold text-accent-foreground underline-offset-4 hover:underline">검색 지우기</a>
            </div>
        </c:if>

        <c:choose>
            <c:when test="${empty notices}">
                <div class="mt-5 rounded-lg border bg-card px-5 py-14 text-center">
                    <h3 class="text-base font-bold">표시할 공시가 없어요</h3>
                    <p class="mt-2 text-sm text-muted-foreground"><c:choose><c:when test="${not empty keyword}">검색어를 줄이거나 다른 단어로 다시 확인해 주세요.</c:when><c:otherwise>새 공식 안내가 게시되면 이곳에 표시돼요.</c:otherwise></c:choose></p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="mt-5 divide-y border-y">
                    <c:forEach var="notice" items="${notices}">
                        <c:url var="noticeUrl" value="/notices/${notice.publicNoticeId}"/>
                        <article class="px-1 py-5 transition-colors hover:bg-secondary/50 ${notice.pinned ? 'bg-accent/40 sm:px-4' : ''}">
                            <div class="flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
                                <c:if test="${notice.pinned}"><t:badge tone="accent">중요</t:badge></c:if>
                                <t:badge tone="neutral"><c:choose><c:when test="${notice.categoryCode == 'PERFORMANCE'}">공연</c:when><c:when test="${notice.categoryCode == 'RECRUITMENT'}">모집</c:when><c:when test="${notice.categoryCode == 'RESERVATION'}">관람</c:when><c:otherwise><c:out value="${notice.categoryCode}"/></c:otherwise></c:choose></t:badge>
                                <time datetime="<c:out value='${notice.publishStartDttm}'/>"><c:out value="${fn:replace(notice.publishStartDttm, 'T', ' ')}"/></time>
                            </div>
                            <h3 class="mt-3 text-base font-bold"><a href="<c:out value='${noticeUrl}'/>" class="inline-flex min-h-11 items-center underline-offset-4 hover:text-accent-foreground hover:underline"><c:out value="${notice.title}"/></a></h3>
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
