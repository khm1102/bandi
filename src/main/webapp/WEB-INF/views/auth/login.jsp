<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:url var="loginUrl" value="/login"/>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<t:layoutAuth title="학교 계정 로그인" scriptPath="auth/login">
    <div class="rounded-lg border bg-secondary/60 p-4">
        <p class="text-sm font-extrabold">한국공학대학교 포털 계정으로 인증합니다</p>
        <p class="mt-1 text-xs leading-5 text-muted-foreground">현재 학교 시스템에서 재학생으로 확인되고, 운영진이 사전 등록한 멤버만 이용할 수 있습니다.</p>
    </div>

    <form:form id="schoolLoginForm" method="post" action="${loginUrl}" modelAttribute="schoolLoginForm"
               cssClass="mt-5 flex flex-col gap-3" aria-label="학교 계정 로그인">
        <div>
            <form:label cssClass="${label}" path="studentNo">학교 포털 아이디</form:label>
            <form:input cssClass="${input}" path="studentNo" type="text"
                        autocomplete="username" placeholder="학교 포털 아이디"
                        required="required" maxlength="50"
                        aria-describedby="loginTrust authError"/>
        </div>
        <div>
            <form:label cssClass="${label}" path="password">학교 포털 비밀번호</form:label>
            <form:password cssClass="${input}" path="password"
                           autocomplete="current-password" placeholder="학교 포털 비밀번호"
                           required="required" maxlength="100"
                           aria-describedby="loginTrust authError"/>
        </div>
        <c:choose>
            <c:when test="${not empty loginErrorTitle}">
                <div id="authError" class="rounded-md border border-destructive bg-destructive-soft px-3 py-2.5" data-auth-error role="alert" aria-live="polite">
                    <b class="block text-sm text-destructive"><c:out value="${loginErrorTitle}"/></b>
                    <p class="mt-1 text-xs leading-5 text-destructive"><c:out value="${loginErrorMessage}"/></p>
                </div>
            </c:when>
            <c:otherwise>
                <p id="authError" class="hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-sm text-destructive" data-auth-error role="alert" aria-live="polite"></p>
            </c:otherwise>
        </c:choose>
        <t:button type="submit" cssClass="mt-1 w-full">학교 계정으로 로그인</t:button>
    </form:form>

    <div id="loginTrust" class="mt-5 border-t pt-4 text-xs leading-5 text-muted-foreground">
        <p class="font-bold text-foreground">학교 비밀번호는 bandi에 저장하지 않습니다.</p>
        <p class="mt-1">학교 인증 결과에서 학번·이름·학적 상태만 확인하며, 로그인할 때마다 재학생 여부를 다시 확인합니다.</p>
        <p class="mt-2 rounded-md bg-accent/60 px-3 py-2 text-accent-foreground">인증이 끝나면 비밀번호는 즉시 폐기하고, 세션에는 멤버 식별자와 역할만 유지합니다.</p>
    </div>
</t:layoutAuth>
