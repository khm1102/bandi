<%@ tag description="폼 필드 — form:form 내부에서 사용. label + form:input/textarea/password + form:errors 조립, 바인딩 오류 시 오류 상태 클래스 자동 적용" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ attribute name="label" required="true" %>
<%@ attribute name="path" required="true" %>
<%@ attribute name="type" %>
<%@ attribute name="required" type="java.lang.Boolean" %>
<%@ attribute name="help" %>
<c:set var="baseClass" value="h-10 w-full rounded-md border bg-card px-3 text-sm transition-colors focus:outline-none focus:ring-2"/>
<c:set var="inputClass" value="${baseClass} border-input focus:border-ring focus:ring-ring/20"/>
<c:set var="errorClass" value="${baseClass} border-destructive focus:border-destructive focus:ring-destructive/20"/>
<div class="mb-3.5">
    <form:label path="${path}" cssClass="mb-1.5 block text-xs font-extrabold text-muted-foreground">
        <c:out value="${label}"/><c:if test="${required}"><span class="text-accent-foreground"> *</span></c:if>
    </form:label>
    <c:choose>
        <c:when test="${type == 'textarea'}">
            <form:textarea path="${path}" rows="4" cssClass="${inputClass} h-auto py-2.5 resize-none" cssErrorClass="${errorClass} h-auto py-2.5 resize-none"/>
        </c:when>
        <c:when test="${type == 'password'}">
            <form:password path="${path}" cssClass="${inputClass}" cssErrorClass="${errorClass}"/>
        </c:when>
        <c:otherwise>
            <form:input path="${path}" type="${empty type ? 'text' : type}" cssClass="${inputClass}" cssErrorClass="${errorClass}"/>
        </c:otherwise>
    </c:choose>
    <c:if test="${not empty help}">
        <p class="mt-1.5 text-xs text-muted-foreground"><c:out value="${help}"/></p>
    </c:if>
    <form:errors path="${path}" element="p" cssClass="mt-1.5 text-xs font-bold text-destructive"/>
</div>
