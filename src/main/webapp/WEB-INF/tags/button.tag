<%@ tag description="공통 버튼 — variant/size와 공통 data-action 규약을 한 곳에서 관리" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="type" %>
<%@ attribute name="href" %>
<%@ attribute name="variant" %>
<%@ attribute name="size" %>
<%@ attribute name="action" %>
<%@ attribute name="pageAction" %>
<%@ attribute name="openModal" %>
<%@ attribute name="confirm" %>
<%@ attribute name="confirmAction" %>
<%@ attribute name="cssClass" %>
<c:set var="buttonType" value="${empty type ? 'button' : type}"/>
<c:set var="buttonVariant" value="${empty variant ? 'primary' : variant}"/>
<c:set var="buttonSize" value="${empty size ? 'default' : size}"/>
<c:set var="baseClass" value="inline-flex items-center justify-center gap-1.5 rounded-md font-bold transition-colors focus-visible:ring-2 focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50"/>
<c:choose>
    <c:when test="${buttonSize == 'compact'}">
        <c:set var="sizeClass" value="min-h-11 px-3 text-xs md:min-h-9"/>
    </c:when>
    <c:otherwise>
        <c:set var="sizeClass" value="min-h-11 px-4 text-sm"/>
    </c:otherwise>
</c:choose>
<c:choose>
    <c:when test="${buttonVariant == 'outline'}">
        <c:set var="variantClass" value="border bg-card text-foreground hover:bg-secondary"/>
    </c:when>
    <c:when test="${buttonVariant == 'dark'}">
        <c:set var="variantClass" value="bg-sidebar text-white hover:bg-sidebar-accent"/>
    </c:when>
    <c:when test="${buttonVariant == 'danger'}">
        <c:set var="variantClass" value="bg-destructive text-destructive-foreground hover:bg-destructive/90"/>
    </c:when>
    <c:otherwise>
        <c:set var="variantClass" value="bg-primary text-primary-foreground hover:bg-primary-strong hover:text-white"/>
    </c:otherwise>
</c:choose>
<c:set var="buttonClass" value="${baseClass} ${sizeClass} ${variantClass} ${cssClass}"/>
<c:choose>
    <c:when test="${not empty href}">
        <a href="<c:url value='${href}'/>" class="<c:out value='${buttonClass}'/>"><jsp:doBody/></a>
    </c:when>
    <c:otherwise>
        <button type="<c:out value='${buttonType}'/>"
                class="<c:out value='${buttonClass}'/>"
                data-action="<c:out value='${action}'/>"
                data-page-action="<c:out value='${pageAction}'/>"
                data-open-modal="<c:out value='${openModal}'/>"
                data-confirm="<c:out value='${confirm}'/>"
                data-confirm-action="<c:out value='${confirmAction}'/>">
            <jsp:doBody/>
        </button>
    </c:otherwise>
</c:choose>
