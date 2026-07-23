<%@ tag description="날짜·시간 입력 — 모바일/JS 실패 시 datetime-local, 데스크톱 초기화 성공 시 날짜·시간 분리" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="id" required="true" %>
<%@ attribute name="label" required="true" %>
<%@ attribute name="required" type="java.lang.Boolean" %>
<%@ attribute name="minuteStep" type="java.lang.Integer" %>
<%@ attribute name="value" %>
<%@ attribute name="disabled" type="java.lang.Boolean" %>
<c:set var="resolvedMinuteStep" value="${empty minuteStep ? 5 : minuteStep}"/>
<fieldset class="min-w-0" data-date-time-field data-date-time-id="${id}"
          data-minute-step="${resolvedMinuteStep}" data-required="${required}"
          data-disabled="${disabled}">
    <legend class="mb-1.5 text-xs font-extrabold text-muted-foreground">
        <c:out value="${label}"/><c:if test="${required}"><span class="text-accent-foreground"> *</span></c:if>
    </legend>
    <c:choose>
        <c:when test="${required and disabled}">
            <input id="${id}" type="datetime-local" value="<c:out value='${value}'/>"
                   class="h-11 w-full rounded-md border border-input bg-card px-3 text-base focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"
                   data-date-time-native step="${resolvedMinuteStep * 60}" required disabled>
        </c:when>
        <c:when test="${required}">
            <input id="${id}" type="datetime-local" value="<c:out value='${value}'/>"
                   class="h-11 w-full rounded-md border border-input bg-card px-3 text-base focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"
                   data-date-time-native step="${resolvedMinuteStep * 60}" required>
        </c:when>
        <c:when test="${disabled}">
            <input id="${id}" type="datetime-local" value="<c:out value='${value}'/>"
                   class="h-11 w-full rounded-md border border-input bg-card px-3 text-base focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"
                   data-date-time-native step="${resolvedMinuteStep * 60}" disabled>
        </c:when>
        <c:otherwise>
            <input id="${id}" type="datetime-local" value="<c:out value='${value}'/>"
                   class="h-11 w-full rounded-md border border-input bg-card px-3 text-base focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"
                   data-date-time-native step="${resolvedMinuteStep * 60}">
        </c:otherwise>
    </c:choose>
    <div class="hidden grid-cols-1 gap-2 md:grid-cols-2" data-date-time-enhanced>
        <div>
            <label class="sr-only" for="${id}Date"><c:out value="${label}"/> 날짜</label>
            <input id="${id}Date" type="text" inputmode="numeric" autocomplete="off"
                   placeholder="연도. 월. 일."
                   class="h-11 w-full rounded-md border border-input bg-card px-3 text-base focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"
                   data-date-time-date>
        </div>
        <div data-date-time-time-wrap>
            <label class="sr-only" for="${id}Time"><c:out value="${label}"/> 시간</label>
            <input id="${id}Time" type="time" step="${resolvedMinuteStep * 60}"
                   class="h-11 w-full rounded-md border border-input bg-card px-3 text-base focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"
                   data-date-time-time>
        </div>
    </div>
</fieldset>
