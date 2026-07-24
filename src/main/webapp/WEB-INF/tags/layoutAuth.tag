<%@ tag description="인증 셸 — 동아리 운영 공간과 인증 폼을 분리한 레이아웃" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
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
        <section class="relative hidden overflow-hidden bg-sidebar text-sidebar-foreground lg:flex" aria-label="반디 소개" data-auth-carousel>
            <div class="absolute inset-0" aria-hidden="true">
                <img src="<c:url value='/images/bandi/586859733_18065598020417765_2136804885828952170_n.jpg'/>" class="absolute inset-0 h-full w-full object-cover opacity-100 transition-opacity duration-300 motion-reduce:transition-none" alt="" data-auth-carousel-slide>
                <img src="<c:url value='/images/bandi/733606117_18088003127417765_3234727138997425177_n.jpg'/>" class="absolute inset-0 h-full w-full object-cover opacity-0 transition-opacity duration-300 motion-reduce:transition-none" alt="" data-auth-carousel-slide>
                <img src="<c:url value='/images/bandi/709326027_18084288173417765_3418235853654150586_n.jpg'/>" class="absolute inset-0 h-full w-full object-cover opacity-0 transition-opacity duration-300 motion-reduce:transition-none" alt="" data-auth-carousel-slide>
                <img src="<c:url value='/images/bandi/582081939_18064841897417765_8418638051277685375_n.jpg'/>" class="absolute inset-0 h-full w-full object-cover opacity-0 transition-opacity duration-300 motion-reduce:transition-none" alt="" data-auth-carousel-slide>
                <div class="absolute inset-0 bg-sidebar/85"></div>
            </div>

            <div class="relative z-10 flex w-full flex-col p-12">
                <div class="flex items-center gap-3">
                    <img src="<c:url value='/images/bandi-icon.png'/>" class="h-11 w-auto rounded-lg object-contain" alt="반디">
                    <span>
                        <b class="block text-lg font-black tracking-tight text-white">반디</b>
                        <span class="block text-xs font-semibold text-sidebar-muted">연극 동아리 통합관리시스템</span>
                    </span>
                </div>

                <div class="my-auto max-w-sm py-16">
                    <p class="mb-5 text-xs font-black text-primary">반디 운영 포털</p>
                    <p class="text-4xl font-black leading-tight tracking-tight text-white">동아리 운영을<br>함께 정리하는 곳.</p>
                    <p class="mt-7 max-w-xs text-sm font-medium leading-7 text-sidebar-muted">일정, 자료, 활동 기록과 소품 관리를 한곳에서 이어갑니다.</p>
                </div>

                <div class="mt-auto pt-5">
                    <div class="flex items-center gap-2" aria-label="소개 사진 선택">
                        <button type="button" class="inline-flex size-11 items-center justify-center rounded-md border border-sidebar-border text-white transition-colors hover:bg-white/10 focus-visible:ring-2 focus-visible:ring-ring" aria-label="이전 소개 사진" data-auth-carousel-prev>
                            <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                                <path d="m15 18-6-6 6-6"></path>
                            </svg>
                        </button>
                        <div class="flex items-center gap-2" role="group" aria-label="소개 사진">
                            <button type="button" class="size-3 rounded-full bg-white ring-2 ring-primary ring-offset-2 ring-offset-sidebar transition-colors focus-visible:ring-2 focus-visible:ring-ring" aria-label="1번 소개 사진 보기" aria-pressed="true" data-auth-carousel-dot="0"></button>
                            <button type="button" class="size-3 rounded-full bg-white/50 transition-colors hover:bg-white focus-visible:ring-2 focus-visible:ring-ring" aria-label="2번 소개 사진 보기" aria-pressed="false" data-auth-carousel-dot="1"></button>
                            <button type="button" class="size-3 rounded-full bg-white/50 transition-colors hover:bg-white focus-visible:ring-2 focus-visible:ring-ring" aria-label="3번 소개 사진 보기" aria-pressed="false" data-auth-carousel-dot="2"></button>
                            <button type="button" class="size-3 rounded-full bg-white/50 transition-colors hover:bg-white focus-visible:ring-2 focus-visible:ring-ring" aria-label="4번 소개 사진 보기" aria-pressed="false" data-auth-carousel-dot="3"></button>
                        </div>
                        <button type="button" class="inline-flex size-11 items-center justify-center rounded-md border border-sidebar-border text-white transition-colors hover:bg-white/10 focus-visible:ring-2 focus-visible:ring-ring" aria-label="다음 소개 사진" data-auth-carousel-next>
                            <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                                <path d="m9 18 6-6-6-6"></path>
                            </svg>
                        </button>
                    </div>
                </div>
            </div>
        </section>

        <section class="flex min-h-screen min-w-0 items-center px-4 py-10 md:px-8 lg:min-h-0 lg:px-16 lg:py-12" aria-label="${title} 영역">
            <div class="mx-auto min-w-0 w-full max-w-md">
                <div class="mb-9 flex items-center gap-3 lg:hidden">
                    <img src="<c:url value='/images/bandi-icon.png'/>" class="h-11 w-auto rounded-lg object-contain" alt="반디">
                    <span>
                        <b class="block text-lg font-black tracking-tight">반디</b>
                        <span class="block text-xs font-semibold text-muted-foreground">연극 동아리 통합관리시스템</span>
                    </span>
                </div>
                <div class="mb-6">
                    <h1 class="text-3xl font-black tracking-tight text-foreground"><c:out value="${title}"/></h1>
                    <p class="mt-2 text-sm leading-6 text-muted-foreground">학교 포털 인증으로 반디 운영 공간에 입장하세요.</p>
                </div>
                <jsp:doBody/>
            </div>
        </section>
    </div>
</main>
<script type="module" src="<c:url value='/js/auth/intro-carousel.js'/>"></script>
<c:if test="${not empty scriptPath}">
    <script type="module" src="<c:url value='/js/${scriptPath}.js'/>"></script>
</c:if>
<jsp:invoke fragment="script"/>
</body>
</html>
