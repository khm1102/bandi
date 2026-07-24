<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout title="처음 시작하기" active="onboarding" role="${role}" scriptPath="onboarding/index">
    <div class="mx-auto w-full max-w-4xl" data-onboarding>
        <header class="flex flex-wrap items-start justify-between gap-4" aria-labelledby="onboardingPageTitle">
            <div>
                <p class="text-sm font-extrabold text-accent-foreground">처음 시작하기</p>
                <h1 id="onboardingPageTitle" class="mt-1 text-2xl font-black tracking-tight">반디를 5단계로 둘러봐요.</h1>
                <p class="mt-2 max-w-2xl text-sm leading-6 text-muted-foreground">필수 절차를 완료하는 온보딩이 아니라, 동아리 생활과 사이트 사용을 빠르게 익히는 안내예요.</p>
            </div>
            <a href="<c:url value='/dashboard'/>" class="inline-flex min-h-11 items-center justify-center rounded-md border bg-card px-4 text-sm font-bold text-foreground transition-colors hover:bg-secondary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring">바로 홈으로</a>
        </header>

        <section class="mt-6 rounded-xl border bg-card p-4 md:p-6" aria-label="반디 사용 안내">
            <div class="flex flex-wrap items-center justify-between gap-3">
                <p class="text-sm font-bold">지금 볼 내용</p>
                <p class="text-sm text-muted-foreground" data-onboarding-status aria-live="polite">1 / 5 · 반디를 먼저 살펴봐요</p>
            </div>
            <div class="mt-3 h-1 overflow-hidden rounded-full bg-secondary" aria-hidden="true">
                <div class="h-full w-full origin-left scale-x-20 bg-primary transition-transform duration-200 motion-reduce:transition-none" data-onboarding-progress></div>
            </div>
            <ol class="mt-5 grid grid-cols-5 gap-2" aria-label="안내 단계">
                <li>
                    <button type="button" class="flex min-h-11 w-full items-center justify-center gap-2 rounded-md border border-primary bg-primary px-2 py-2 text-xs font-bold text-primary-foreground transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 motion-reduce:transition-none md:px-3" data-onboarding-step data-onboarding-index="0" aria-controls="onboardingSlides" aria-current="step" aria-label="1단계, 반디 살펴보기">
                        <span>1</span><span class="hidden lg:inline">시작</span>
                    </button>
                </li>
                <li>
                    <button type="button" class="flex min-h-11 w-full items-center justify-center gap-2 rounded-md border bg-card px-2 py-2 text-xs font-bold text-muted-foreground transition-colors hover:bg-secondary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 motion-reduce:transition-none md:px-3" data-onboarding-step data-onboarding-index="1" aria-controls="onboardingSlides" aria-label="2단계, 내 정보 확인">
                        <span>2</span><span class="hidden lg:inline">내 정보</span>
                    </button>
                </li>
                <li>
                    <button type="button" class="flex min-h-11 w-full items-center justify-center gap-2 rounded-md border bg-card px-2 py-2 text-xs font-bold text-muted-foreground transition-colors hover:bg-secondary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 motion-reduce:transition-none md:px-3" data-onboarding-step data-onboarding-index="2" aria-controls="onboardingSlides" aria-label="3단계, 일정과 공지 확인">
                        <span>3</span><span class="hidden lg:inline">일정·공지</span>
                    </button>
                </li>
                <li>
                    <button type="button" class="flex min-h-11 w-full items-center justify-center gap-2 rounded-md border bg-card px-2 py-2 text-xs font-bold text-muted-foreground transition-colors hover:bg-secondary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 motion-reduce:transition-none md:px-3" data-onboarding-step data-onboarding-index="3" aria-controls="onboardingSlides" aria-label="4단계, 자료와 활동 기록">
                        <span>4</span><span class="hidden lg:inline">기록</span>
                    </button>
                </li>
                <li>
                    <button type="button" class="flex min-h-11 w-full items-center justify-center gap-2 rounded-md border bg-card px-2 py-2 text-xs font-bold text-muted-foreground transition-colors hover:bg-secondary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 motion-reduce:transition-none md:px-3" data-onboarding-step data-onboarding-index="4" aria-controls="onboardingSlides" aria-label="5단계, 소품과 장비 확인">
                        <span>5</span><span class="hidden lg:inline">소품·장비</span>
                    </button>
                </li>
            </ol>
        </section>

        <div id="onboardingSlides" class="mt-6" data-onboarding-slides>
            <section class="rounded-xl border bg-card p-5 md:p-8" data-onboarding-slide aria-labelledby="onboardingSlide1Title" tabindex="-1">
                <p class="text-sm font-extrabold text-accent-foreground">1단계 · 시작</p>
                <h2 id="onboardingSlide1Title" class="mt-2 text-2xl font-black tracking-tight">반디는 동아리 일을 한곳에 남기는 공간이에요.</h2>
                <p class="mt-3 max-w-2xl text-sm leading-6 text-muted-foreground">일정, 공지, 자료, 활동 기록과 소품 정보를 모아 두고 필요한 글은 링크로 공유할 수 있어요. 실제 일정과 팀별 안내는 항상 최신 공지를 기준으로 확인해 주세요.</p>
                <dl class="mt-7 grid divide-y border-y text-sm md:grid-cols-3 md:divide-x md:divide-y-0">
                    <div class="py-4 md:px-5 md:first:pl-0"><dt class="font-extrabold">확인할 정보</dt><dd class="mt-2 leading-6 text-muted-foreground">오늘 일정과 읽지 않은 공지</dd></div>
                    <div class="py-4 md:px-5"><dt class="font-extrabold">남길 정보</dt><dd class="mt-2 leading-6 text-muted-foreground">활동 기록과 공용 자료</dd></div>
                    <div class="py-4 md:px-5 md:pr-0"><dt class="font-extrabold">찾을 정보</dt><dd class="mt-2 leading-6 text-muted-foreground">동아리 소품과 장비 상태</dd></div>
                </dl>
            </section>

            <section class="hidden rounded-xl border bg-card p-5 md:p-8" data-onboarding-slide aria-hidden="true" aria-labelledby="onboardingSlide2Title" tabindex="-1">
                <p class="text-sm font-extrabold text-accent-foreground">2단계 · 내 정보</p>
                <h2 id="onboardingSlide2Title" class="mt-2 text-2xl font-black tracking-tight">프로필에서 내 소속을 먼저 확인해요.</h2>
                <p class="mt-3 max-w-2xl text-sm leading-6 text-muted-foreground">이름, 학번, 기수, 소속 팀과 역할은 권한과 팀별 안내 범위를 결정하는 정보예요. 학교 SSO에서 확인한 정보는 다음 로그인 때 최신 값으로 갱신됩니다.</p>
                <div class="mt-7 border-l-2 border-primary pl-4">
                    <p class="text-sm font-extrabold">정보가 다르게 보이나요?</p>
                    <p class="mt-1 text-sm leading-6 text-muted-foreground">소속 팀이나 기수 변경이 필요하면 운영진에게 변경 사유와 함께 알려 주세요.</p>
                </div>
                <a href="<c:url value='/profile'/>" class="mt-7 inline-flex min-h-11 items-center justify-center rounded-md bg-primary px-4 text-sm font-bold text-primary-foreground transition-colors hover:bg-primary-strong focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2">내 프로필 확인하기</a>
            </section>

            <section class="hidden rounded-xl border bg-card p-5 md:p-8" data-onboarding-slide aria-hidden="true" aria-labelledby="onboardingSlide3Title" tabindex="-1">
                <p class="text-sm font-extrabold text-accent-foreground">3단계 · 일정과 공지</p>
                <h2 id="onboardingSlide3Title" class="mt-2 text-2xl font-black tracking-tight">이번 주에 해야 할 일은 공지와 캘린더에서 확인해요.</h2>
                <p class="mt-3 max-w-2xl text-sm leading-6 text-muted-foreground">통합 캘린더에는 동아리 전체 일정과 팀 일정이 함께 표시돼요. 공지는 전체 공지와 내 소속 팀 대상 공지가 다를 수 있으니, 새 공지 표시를 함께 확인해 주세요.</p>
                <div class="mt-7 flex flex-wrap gap-3">
                    <a href="<c:url value='/calendar'/>" class="inline-flex min-h-11 items-center justify-center rounded-md border bg-card px-4 text-sm font-bold transition-colors hover:bg-secondary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2">통합 캘린더 보기</a>
                    <a href="<c:url value='/notices'/>" class="inline-flex min-h-11 items-center justify-center rounded-md border bg-card px-4 text-sm font-bold transition-colors hover:bg-secondary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2">공지 확인하기</a>
                </div>
            </section>

            <section class="hidden rounded-xl border bg-card p-5 md:p-8" data-onboarding-slide aria-hidden="true" aria-labelledby="onboardingSlide4Title" tabindex="-1">
                <p class="text-sm font-extrabold text-accent-foreground">4단계 · 기록</p>
                <h2 id="onboardingSlide4Title" class="mt-2 text-2xl font-black tracking-tight">필요한 자료를 찾고, 내 활동을 기록으로 남겨요.</h2>
                <p class="mt-3 max-w-2xl text-sm leading-6 text-muted-foreground">자료실에는 공용 문서, 파일, 링크가 쌓여요. 활동 기록은 내가 작성한 내용을 검수 흐름에 따라 확인하는 공간이에요. 역할에 따라 보이는 메뉴와 가능한 작업은 달라질 수 있어요.</p>
                <div class="mt-7 grid gap-3 sm:grid-cols-2">
                    <a href="<c:url value='/resources'/>" class="group rounded-lg border p-4 transition-colors hover:bg-secondary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2">
                        <b class="block text-sm">자료실</b><span class="mt-1 block text-sm leading-6 text-muted-foreground">파일과 링크로 공유된 자료를 확인해요.</span>
                    </a>
                    <a href="<c:url value='/activity'/>" class="group rounded-lg border p-4 transition-colors hover:bg-secondary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2">
                        <b class="block text-sm">내 활동 기록</b><span class="mt-1 block text-sm leading-6 text-muted-foreground">작성한 기록과 현재 검수 단계를 확인해요.</span>
                    </a>
                </div>
            </section>

            <section class="hidden rounded-xl border bg-card p-5 md:p-8" data-onboarding-slide aria-hidden="true" aria-labelledby="onboardingSlide5Title" tabindex="-1">
                <p class="text-sm font-extrabold text-accent-foreground">5단계 · 소품과 장비</p>
                <h2 id="onboardingSlide5Title" class="mt-2 text-2xl font-black tracking-tight">필요한 소품과 장비 상태를 찾아볼 수 있어요.</h2>
                <p class="mt-3 max-w-2xl text-sm leading-6 text-muted-foreground">소품·장비에서 품목의 사진, 보관 위치와 현재 상태를 확인해요. 운영 관리 기능은 역할에 따라 표시됩니다.</p>
                <div class="mt-7 border-t pt-6">
                    <p class="text-sm font-extrabold">준비가 끝났어요.</p>
                    <p class="mt-1 text-sm leading-6 text-muted-foreground">이제 홈에서 오늘 일정과 새 공지를 확인하며 시작해 보세요.</p>
                    <a href="<c:url value='/props'/>" class="mt-5 inline-flex min-h-11 items-center justify-center rounded-md border bg-card px-4 text-sm font-bold transition-colors hover:bg-secondary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2">소품·장비 보기</a>
                </div>
            </section>
        </div>

        <nav class="mt-6 flex items-center justify-between gap-3 border-t pt-5" aria-label="안내 이동">
            <button type="button" class="inline-flex min-h-11 items-center justify-center rounded-md border bg-card px-4 text-sm font-bold transition-colors hover:bg-secondary disabled:cursor-not-allowed disabled:opacity-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2" data-onboarding-action="previous" disabled>이전</button>
            <div class="flex items-center gap-3">
                <button type="button" class="inline-flex min-h-11 items-center justify-center rounded-md bg-primary px-4 text-sm font-bold text-primary-foreground transition-colors hover:bg-primary-strong focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2" data-onboarding-action="next">다음 안내</button>
                <a href="<c:url value='/dashboard'/>" class="hidden min-h-11 items-center justify-center rounded-md bg-primary px-4 text-sm font-bold text-primary-foreground transition-colors hover:bg-primary-strong focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2" data-onboarding-complete>홈에서 시작하기</a>
            </div>
        </nav>
    </div>
</t:layout>
