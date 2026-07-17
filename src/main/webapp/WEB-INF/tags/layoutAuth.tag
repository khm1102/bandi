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
<a href="#mainContent" class="fixed left-4 top-4 z-50 -translate-y-24 rounded-md bg-primary px-4 py-2 text-sm font-bold text-primary-foreground transition-transform focus:translate-y-0">본문으로 바로가기</a>
<main id="mainContent" class="flex min-h-screen items-stretch bg-secondary lg:items-center lg:p-8" tabindex="-1">
    <div class="mx-auto grid w-full max-w-6xl overflow-hidden bg-card lg:grid-cols-[0.92fr_1.08fr] lg:rounded-xl lg:border lg:shadow-xl">
        <section class="relative hidden overflow-hidden bg-sidebar p-12 text-sidebar-foreground lg:flex lg:flex-col" aria-label="반디 소개">
            <img src="<c:url value='/images/performance/show-house-boy.webp'/>" alt="" width="960" height="1200" class="absolute inset-0 size-full object-cover opacity-30" aria-hidden="true">
            <div class="absolute inset-0 bg-sidebar/80" aria-hidden="true"></div>
            <div class="relative flex items-center gap-3">
                <span class="flex size-11 items-center justify-center rounded-lg bg-primary text-xl font-black text-primary-foreground">B</span>
                <span>
                    <b class="block text-lg font-black tracking-tight text-white">반디</b>
                    <span class="block text-xs font-semibold text-sidebar-muted">연극 동아리 통합관리시스템</span>
                </span>
            </div>

            <div class="relative my-auto max-w-sm py-16">
                <p class="mb-5 text-xs font-black text-primary">반디 운영 포털</p>
                <p class="text-4xl font-black leading-tight tracking-tight text-white">한 번의 공연을<br>함께 준비하는 곳.</p>
                <p class="mt-7 max-w-xs text-sm font-medium leading-7 text-sidebar-muted">일정부터 소품, 출석, 회비까지. 무대 뒤의 복잡한 일을 한 팀처럼 연결합니다.</p>
            </div>

            <div class="relative border-t border-sidebar-border pt-5">
                <p class="text-xs font-bold text-primary">2025 정기공연</p>
                <p class="mt-1 text-lg font-black text-white">소년 B가 사는 집</p>
                <p class="mt-2 text-xs font-semibold text-sidebar-muted">6월 21–22일 · 한국공학대학교 TIP아트센터</p>
            </div>
        </section>

        <section class="flex min-h-screen items-center px-4 py-10 md:px-8 lg:min-h-0 lg:px-16 lg:py-12" aria-label="${title} 영역">
            <div class="mx-auto w-full max-w-md">
                <div class="mb-9 flex items-center gap-3 lg:hidden">
                    <span class="flex size-11 items-center justify-center rounded-lg bg-primary text-xl font-black text-primary-foreground">B</span>
                    <span>
                        <b class="block text-lg font-black tracking-tight">반디</b>
                        <span class="block text-xs font-semibold text-muted-foreground">연극 동아리 통합관리시스템</span>
                    </span>
                </div>
                <div class="mb-6">
                    <h1 class="text-3xl font-black tracking-tight text-foreground"><c:out value="${title}"/></h1>
                    <p class="mt-2 text-sm leading-6 text-muted-foreground">반디 구성원 계정으로 백스테이지에 입장하세요.</p>
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
