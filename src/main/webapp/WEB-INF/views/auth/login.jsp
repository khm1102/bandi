<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:url var="loginUrl" value="/login"/>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<t:layoutAuth title="학교 계정 로그인" scriptPath="auth/login">
    <div class="border-y bg-secondary/60 px-1 py-4">
        <p class="text-sm font-bold">한국공학대학교 포털 계정으로 인증해요</p>
        <p class="mt-1 text-xs leading-5 text-muted-foreground">학교에서 재학생으로 확인되고 운영진이 미리 등록한 멤버만 이용할 수 있어요.</p>
    </div>

    <form:form id="schoolLoginForm" method="post" action="${loginUrl}" modelAttribute="schoolLoginForm"
               cssClass="mt-5 flex flex-col gap-3" aria-label="학교 계정 로그인">
        <div>
            <form:label cssClass="${label}" path="studentNo">학번</form:label>
            <form:input cssClass="${input}" path="studentNo" type="text"
                        inputmode="numeric" autocomplete="username" placeholder="학번"
                        required="required" maxlength="50"
                        aria-describedby="loginTrust authError"/>
        </div>
        <div>
            <form:label cssClass="${label}" path="password">학교 포털 비밀번호</form:label>
            <div class="relative">
                <form:password cssClass="${input} pr-20" path="password"
                               autocomplete="current-password" placeholder="학교 포털 비밀번호"
                               required="required" maxlength="100"
                               aria-describedby="loginTrust authError"/>
                <button type="button" data-password-toggle class="absolute inset-y-0 right-0 min-h-11 px-3 text-xs font-bold text-muted-foreground hover:text-foreground" aria-controls="password" aria-pressed="false">보기</button>
            </div>
        </div>
        <c:set var="authErrorVisibility" value="${empty loginErrorTitle ? 'hidden ' : ''}"/>
        <div id="authError" class="${authErrorVisibility}rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-destructive" data-auth-error role="alert" aria-live="polite">
            <c:if test="${not empty loginErrorTitle}">
                <b class="block text-sm"><c:out value="${loginErrorTitle}"/></b>
                <p class="mt-1 text-xs leading-5"><c:out value="${loginErrorMessage}"/></p>
            </c:if>
        </div>
        <t:button type="submit" cssClass="mt-1 w-full">학교 계정 확인하고 로그인</t:button>
    </form:form>

    <div id="loginTrust" class="mt-5 border-t pt-4 text-xs leading-5 text-muted-foreground">
        <p class="font-bold text-foreground">학교 비밀번호는 bandi에 저장하지 않아요.</p>
        <p class="mt-1">학교 인증 결과에서 학번·이름·학적 상태만 확인하며, 로그인할 때마다 재학생 여부를 다시 확인해요.</p>
        <p class="mt-2 border-l-2 border-primary pl-3 text-accent-foreground">인증이 끝나면 비밀번호는 즉시 폐기하고 세션에는 멤버 식별자와 역할만 유지해요.</p>
    </div>

    <div class="mt-4 flex flex-col gap-2 border-t pt-4 md:flex-row">
        <a href="<c:url value='/notices'/>" class="inline-flex min-h-11 flex-1 items-center justify-center rounded-md border bg-card px-3 text-xs font-bold transition-colors hover:bg-secondary">로그인 장애·운영 공시</a>
        <a href="<c:url value='/reserve'/>" class="inline-flex min-h-11 flex-1 items-center justify-center rounded-md border bg-card px-3 text-xs font-bold transition-colors hover:bg-secondary">관람객 공연 신청</a>
    </div>
</t:layoutAuth>
