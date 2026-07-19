<%@ tag description="인증 셸 — 공연 맥락과 인증 폼을 분리한 백스테이지 레이아웃" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="scriptPath" %>
<%@ attribute name="script" fragment="true" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<t:head title="${title}"/>
</head>
<body>
<a href="#mainContent" class="fixed left-4 top-4 z-50 inline-flex min-h-11 -translate-y-24 items-center rounded-md bg-primary px-4 text-sm font-bold text-primary-foreground transition-transform focus:translate-y-0">본문으로 바로가기</a>
<main id="mainContent" class="flex min-h-screen items-stretch bg-secondary lg:items-center lg:p-8" tabindex="-1">
    <div class="mx-auto grid w-full max-w-6xl overflow-hidden bg-card lg:grid-cols-[0.92fr_1.08fr] lg:rounded-xl lg:border lg:shadow-xl">
        <section class="hidden bg-sidebar p-12 text-sidebar-foreground lg:flex lg:flex-col" aria-label="반디 소개">
            <div class="flex items-center gap-3">
                <span class="flex size-11 items-center justify-center rounded-lg bg-primary text-xl font-extrabold text-primary-foreground">B</span>
                <span>
                    <b class="block text-lg font-extrabold tracking-tight text-white">반디</b>
                    <span class="block text-xs font-semibold text-sidebar-muted">연극 동아리 통합관리시스템</span>
                </span>
            </div>

            <div class="my-auto max-w-sm py-16">
                <p class="mb-5 text-xs font-bold text-primary">반디 운영 포털</p>
                <p class="text-4xl font-extrabold leading-tight tracking-tight text-white">공연 준비의 다음 일을<br>놓치지 않도록.</p>
                <p class="mt-7 max-w-xs text-sm font-medium leading-7 text-sidebar-muted">일정, 자료, 활동 기록과 공연 운영을 역할에 맞게 이어서 처리해요.</p>
            </div>

            <div class="border-t border-sidebar-border pt-5">
                <p class="text-xs font-bold text-primary">학교 계정으로 안전하게</p>
                <p class="mt-2 text-xs font-semibold leading-6 text-sidebar-muted">학교에서 재학생 여부를 확인하고, 운영진이 등록한 멤버에게 필요한 메뉴만 보여줘요.</p>
            </div>
        </section>

        <section class="flex min-h-screen min-w-0 items-center px-4 py-10 md:px-8 lg:min-h-0 lg:px-16 lg:py-12" aria-label="${title} 영역">
            <div class="mx-auto min-w-0 w-full max-w-md">
                <div class="mb-9 flex items-center gap-3 lg:hidden">
                    <span class="flex size-11 items-center justify-center rounded-lg bg-primary text-xl font-extrabold text-primary-foreground">B</span>
                    <span>
                        <b class="block text-lg font-extrabold tracking-tight">반디</b>
                        <span class="block text-xs font-semibold text-muted-foreground">연극 동아리 통합관리시스템</span>
                    </span>
                </div>
                <div class="mb-6">
                    <h1 class="text-3xl font-extrabold tracking-tight text-foreground"><c:out value="${title}"/></h1>
                    <p class="mt-2 text-sm leading-6 text-muted-foreground">학교 포털 계정으로 본인과 재학생 여부를 확인해요.</p>
                </div>
                <jsp:doBody/>
            </div>
        </section>
    </div>
</main>
<c:if test="${not empty scriptPath}">
    <script type="module" src="<c:url value='/js/${scriptPath}.js'/>"></script>
</c:if>
<jsp:invoke fragment="script"/>
</body>
</html>
