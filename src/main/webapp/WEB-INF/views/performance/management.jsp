<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-bold text-muted-foreground"/>
<c:set var="textarea" value="min-h-24 w-full resize-y rounded-md border border-input bg-card px-3 py-2.5 text-sm leading-6 focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20"/>
<c:set var="sectionTab" value="flex min-h-11 flex-1 items-center justify-center rounded-md px-2 text-sm font-bold text-muted-foreground transition-colors hover:text-foreground"/>
<t:layout title="공연 운영 설정" active="performance-management" role="${role}" scriptPath="performance/management">
    <div class="mx-auto max-w-4xl">
        <h1 class="text-2xl font-extrabold tracking-tight">공연 운영 설정</h1>
        <p class="mt-1 text-sm text-muted-foreground">준비 상태를 확인하고 다음 미완료 설정을 처리해요.</p>

        <%-- 프로젝트 컨텍스트 --%>
        <section class="mt-5" aria-label="공연 프로젝트 선택">
            <label class="${label}" for="performanceProjectSelect">공연 프로젝트</label>
            <div class="flex flex-col gap-2 md:flex-row md:items-center">
                <select id="performanceProjectSelect" class="${input} md:max-w-sm"></select>
                <div data-project-summary class="flex min-w-0 flex-wrap items-center gap-2 text-sm text-muted-foreground" aria-live="polite"></div>
            </div>
        </section>

        <%-- 로딩 placeholder --%>
        <div data-page-loading class="mt-6 flex flex-col gap-3" aria-hidden="true">
            <div class="h-24 rounded-lg border bg-card"></div>
            <div class="h-40 rounded-lg border bg-card"></div>
        </div>

        <%-- 전체 로드 실패 --%>
        <div data-page-error class="mt-6 hidden rounded-lg border border-destructive/30 bg-destructive-soft p-5" role="alert">
            <div class="flex items-start gap-3">
                <svg class="mt-0.5 size-5 shrink-0 text-destructive" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0zM12 9v4M12 17h.01"/></svg>
                <div>
                    <b class="block text-sm font-bold text-destructive">공연 운영 정보를 불러오지 못했어요</b>
                    <p data-page-error-message class="mt-1 text-sm text-destructive">네트워크 상태를 확인해 주세요. 화면의 데이터는 아직 반영되지 않았어요.</p>
                </div>
            </div>
            <div class="mt-3"><t:button variant="outline" pageAction="performance-reload">다시 불러오기</t:button></div>
        </div>

        <div data-page-body class="hidden">
            <%-- 프로젝트 없음 --%>
            <section data-project-empty class="mt-6 hidden rounded-lg border bg-card px-6 py-10 text-center">
                <b class="block text-base font-bold">아직 공연 프로젝트가 없어요</b>
                <p class="mx-auto mt-2 max-w-prose text-sm text-muted-foreground">학기당 하나의 공연 프로젝트를 만들면 회차, 외부 공연 페이지, 관람 안내를 이 화면에서 순서대로 준비할 수 있어요.</p>
                <div class="mt-5 flex justify-center"><t:button pageAction="performance-project-create-open">새 공연 프로젝트 만들기</t:button></div>
            </section>

            <div data-project-context class="hidden">
                <%-- 다음에 해야 할 작업 --%>
                <section class="mt-6 rounded-lg border border-primary/25 bg-accent p-5" aria-labelledby="nextActionTitle">
                    <h2 id="nextActionTitle" class="text-xs font-bold text-accent-foreground">다음에 할 일</h2>
                    <p data-next-action-message class="mt-1.5 text-base font-bold text-foreground"></p>
                    <div class="mt-4 flex flex-wrap items-center gap-2">
                        <button type="button" data-next-action-button class="inline-flex min-h-11 items-center justify-center gap-1.5 rounded-md bg-primary px-4 text-sm font-bold text-primary-foreground transition-colors hover:bg-primary-strong focus-visible:ring-2 focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50"></button>
                        <span data-next-action-done class="hidden text-sm font-bold text-success">모든 준비 단계를 마쳤어요.</span>
                    </div>
                </section>

                <%-- 섹션 전환 --%>
                <nav class="mt-6 flex rounded-lg border bg-secondary p-1" aria-label="설정 섹션">
                    <button type="button" data-section-tab="overview" class="${sectionTab}">개요</button>
                    <button type="button" data-section-tab="rounds" class="${sectionTab}">회차</button>
                    <button type="button" data-section-tab="public" class="${sectionTab}">외부 공개</button>
                    <button type="button" data-section-tab="guide" class="${sectionTab}">관람 안내</button>
                </nav>

                <%-- 개요 --%>
                <section data-section-panel="overview" class="hidden" aria-labelledby="overviewTitle">
                    <h2 id="overviewTitle" tabindex="-1" class="mt-6 text-lg font-bold">개요</h2>
                    <dl class="mt-3 flex flex-col gap-1.5 text-sm">
                        <div class="flex gap-2"><dt class="w-20 shrink-0 text-muted-foreground">제작 기간</dt><dd data-overview-period class="font-medium tabular-nums"></dd></div>
                        <div class="flex gap-2"><dt class="w-20 shrink-0 text-muted-foreground">공연 장소</dt><dd data-overview-place class="font-medium"></dd></div>
                    </dl>
                    <h3 class="mt-6 border-t pt-5 text-sm font-bold">준비 진행</h3>
                    <ol data-checklist class="mt-2 flex flex-col"></ol>
                    <div class="mt-6 flex flex-wrap gap-2 border-t pt-5">
                        <t:button variant="outline" pageAction="performance-project-edit-open">프로젝트 수정</t:button>
                        <t:button variant="outline" pageAction="performance-status-open">프로젝트 상태 변경</t:button>
                    </div>
                </section>

                <%-- 회차 --%>
                <section data-section-panel="rounds" class="hidden" aria-labelledby="roundsTitle">
                    <div class="mt-6 flex items-center gap-3">
                        <div>
                            <h2 id="roundsTitle" tabindex="-1" class="text-lg font-bold">회차</h2>
                            <p class="mt-1 text-sm text-muted-foreground">공연 시각, 신청 기간, 입장 시작과 접근성 지원을 회차 단위로 관리해요.</p>
                        </div>
                        <t:button cssClass="ml-auto shrink-0" variant="outline" pageAction="performance-round-create-open">회차 추가</t:button>
                    </div>
                    <div data-rounds-error class="mt-4 hidden rounded-md bg-destructive-soft px-4 py-3 text-sm text-destructive">회차 목록을 불러오지 못했어요. <button type="button" data-page-action="performance-reload-context" class="font-bold underline">다시 시도</button></div>
                    <div data-rounds-list class="mt-4 flex flex-col divide-y rounded-lg border bg-card"></div>
                    <div data-rounds-empty class="hidden rounded-lg border bg-card px-6 py-10 text-center">
                        <b class="block text-sm font-bold">아직 회차가 없어요</b>
                        <p class="mx-auto mt-1 max-w-prose text-sm text-muted-foreground">회차를 만들어야 관람 신청과 공연 당일 입장을 운영할 수 있어요.</p>
                        <div class="mt-4 flex justify-center"><t:button variant="outline" pageAction="performance-round-create-open">첫 회차 만들기</t:button></div>
                    </div>
                </section>

                <%-- 외부 공개 --%>
                <section data-section-panel="public" class="hidden" aria-labelledby="publicTitle">
                    <div class="mt-6 flex flex-wrap items-center gap-3">
                        <div class="min-w-0">
                            <h2 id="publicTitle" tabindex="-1" class="text-lg font-bold">외부 공개</h2>
                            <p class="mt-1 text-sm text-muted-foreground">외부 관람객에게 보여줄 공연 페이지를 저장하고 공개 상태를 관리해요.</p>
                        </div>
                        <div class="ml-auto flex flex-wrap items-center gap-2">
                            <span data-public-page-status><t:badge tone="neutral">페이지 없음</t:badge></span>
                            <a data-public-page-link class="hidden min-h-11 items-center rounded-md border bg-card px-4 text-sm font-bold hover:bg-secondary" target="_blank" rel="noopener noreferrer">공개 페이지 열기</a>
                        </div>
                    </div>
                    <form data-public-page-form class="mt-5 flex flex-col gap-8">
                        <fieldset>
                            <legend class="text-sm font-bold">공연 기본 소개</legend>
                            <div class="mt-3 grid gap-3 md:grid-cols-2">
                                <div><label class="${label}" for="publicGenre">장르 *</label><input id="publicGenre" class="${input}" required maxlength="100"></div>
                                <div><label class="${label}" for="publicAgeRating">관람 등급 *</label><input id="publicAgeRating" class="${input}" required maxlength="50"></div>
                                <div><label class="${label}" for="publicRuntime">러닝타임(분) *</label><input id="publicRuntime" class="${input}" type="number" min="1" required></div>
                                <div><label class="${label}" for="publicIntermission">인터미션(분)</label><input id="publicIntermission" class="${input}" type="number" min="0"></div>
                            </div>
                            <div class="mt-3"><label class="${label}" for="publicShortDescription">짧은 소개 *</label><textarea id="publicShortDescription" class="${textarea}" required maxlength="500"></textarea></div>
                            <div class="mt-3"><label class="${label}" for="publicSynopsis">시놉시스 *</label><textarea id="publicSynopsis" class="${textarea} min-h-48" required></textarea></div>
                            <div class="mt-3"><label class="${label}" for="publicDirectorNote">연출 노트</label><textarea id="publicDirectorNote" class="${textarea}"></textarea></div>
                        </fieldset>
                        <fieldset class="border-t pt-6">
                            <legend class="text-sm font-bold">포스터와 이미지</legend>
                            <div class="mt-3 grid gap-4 md:grid-cols-2">
                                <div>
                                    <label class="${label}" for="publicHero">히어로 이미지</label>
                                    <input id="publicHero" type="file" accept="image/*" data-image-input class="block w-full text-xs" aria-describedby="publicHeroPreview">
                                    <div id="publicHeroPreview" data-image-preview="publicHero" class="mt-2 hidden items-center gap-2"></div>
                                </div>
                                <div>
                                    <label class="${label}" for="publicPoster">포스터</label>
                                    <input id="publicPoster" type="file" accept="image/*" data-image-input class="block w-full text-xs" aria-describedby="publicPosterPreview">
                                    <div id="publicPosterPreview" data-image-preview="publicPoster" class="mt-2 hidden items-center gap-2"></div>
                                </div>
                            </div>
                            <div class="mt-3 md:max-w-xs"><label class="${label}" for="publicAccent">강조색</label><input id="publicAccent" class="${input}" maxlength="20" placeholder="#8E0015"><p class="mt-1 text-xs text-muted-foreground">비워 두면 기본 색을 사용해요.</p></div>
                        </fieldset>
                        <fieldset class="border-t pt-6">
                            <legend class="text-sm font-bold">관람료·주최·문의</legend>
                            <div class="mt-3 grid gap-3 md:grid-cols-2">
                                <div><label class="${label}" for="publicFee">관람료(원) *</label><input id="publicFee" class="${input}" type="number" min="0" value="0" required></div>
                                <div><label class="${label}" for="publicOrganizer">주최 *</label><input id="publicOrganizer" class="${input}" required maxlength="200"></div>
                                <div><label class="${label}" for="publicContactName">문의 이름 *</label><input id="publicContactName" class="${input}" required maxlength="100"></div>
                                <div><label class="${label}" for="publicContactChannel">문의 채널 *</label><input id="publicContactChannel" class="${input}" required maxlength="200"></div>
                            </div>
                        </fieldset>
                        <fieldset class="border-t pt-6">
                            <legend class="text-sm font-bold">공개 주소와 기간</legend>
                            <div class="mt-3 grid gap-3 md:grid-cols-2">
                                <div class="md:col-span-2"><label class="${label}" for="publicSlug">공개 주소 *</label><input id="publicSlug" class="${input}" required maxlength="100" pattern="[a-z0-9-]+" placeholder="hamlet-2026" aria-describedby="publicSlugHelp"><p id="publicSlugHelp" class="mt-1 text-xs text-muted-foreground">소문자·숫자·하이픈만 쓸 수 있어요. 예: /performances/hamlet-2026</p></div>
                                <div><label class="${label}" for="publicPublishStart">공개 시작</label><input id="publicPublishStart" class="${input}" type="datetime-local"></div>
                                <div><label class="${label}" for="publicPublishEnd">공개 종료</label><input id="publicPublishEnd" class="${input}" type="datetime-local"></div>
                            </div>
                        </fieldset>
                        <details class="border-t pt-6">
                            <summary class="flex min-h-11 cursor-pointer list-none items-center gap-2 text-sm font-bold [&::-webkit-details-marker]:hidden">
                                <svg class="size-4 transition-transform [details[open]_&]:rotate-90" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="m9 18 6-6-6-6"/></svg>
                                검색·공유 설정
                                <span class="text-xs font-medium text-muted-foreground">선택 항목이에요</span>
                            </summary>
                            <div class="mt-3 grid gap-3">
                                <div><label class="${label}" for="publicOgTitle">공유 제목</label><input id="publicOgTitle" class="${input}" maxlength="200"></div>
                                <div><label class="${label}" for="publicOgDescription">공유 설명</label><textarea id="publicOgDescription" class="${textarea}" maxlength="500"></textarea></div>
                                <div>
                                    <label class="${label}" for="publicOgImage">공유 이미지</label>
                                    <input id="publicOgImage" type="file" accept="image/*" data-image-input class="block w-full text-xs" aria-describedby="publicOgImagePreview">
                                    <div id="publicOgImagePreview" data-image-preview="publicOgImage" class="mt-2 hidden items-center gap-2"></div>
                                    <p class="mt-1 text-xs text-muted-foreground">선택하지 않으면 기존 이미지를 유지해요.</p>
                                </div>
                            </div>
                        </details>
                    </form>

                    <%-- 공연 관련 공시 --%>
                    <div class="mt-8 border-t pt-6">
                        <div class="flex items-center gap-3">
                            <div>
                                <h3 class="text-sm font-bold">공연 관련 공시</h3>
                                <p class="mt-1 text-xs text-muted-foreground">공개 공연 페이지에 함께 표시할 공시예요.</p>
                            </div>
                            <t:button cssClass="ml-auto shrink-0" variant="outline" size="compact" pageAction="performance-notice-open">공시 연결</t:button>
                        </div>
                        <div data-performance-notices class="mt-3 flex flex-col gap-2"></div>
                    </div>

                    <%-- sticky 저장 바 --%>
                    <div class="sticky bottom-0 z-10 mt-8 -mx-4 border-t bg-card/95 px-4 py-3 backdrop-blur md:mx-0 md:rounded-lg md:border">
                        <div class="flex flex-wrap items-center gap-3">
                            <p data-public-dirty class="min-w-0 flex-1 text-xs text-muted-foreground" aria-live="polite"></p>
                            <t:button variant="outline" pageAction="performance-public-status-open">공개 상태 변경</t:button>
                            <t:button pageAction="performance-public-save">페이지 저장</t:button>
                        </div>
                    </div>
                </section>

                <%-- 관람 안내 --%>
                <section data-section-panel="guide" class="hidden" aria-labelledby="guideTitle">
                    <h2 id="guideTitle" tabindex="-1" class="mt-6 text-lg font-bold">관람 안내</h2>
                    <p class="mt-1 text-sm text-muted-foreground">공연 전체에 적용되는 입장·취소·접근성 정책이에요. 항목을 하나씩 열어 작성해 주세요.</p>
                    <form data-viewing-guide-form class="mt-4 flex flex-col divide-y rounded-lg border bg-card">
                        <details open>
                            <summary class="flex min-h-11 cursor-pointer list-none items-center gap-2 px-4 text-sm font-bold [&::-webkit-details-marker]:hidden"><svg class="size-4 transition-transform [details[open]_&]:rotate-90" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="m9 18 6-6-6-6"/></svg>입장</summary>
                            <div class="grid gap-3 px-4 pb-4 md:grid-cols-2">
                                <div><label class="${label}" for="guideEntry">입장 안내 *</label><textarea id="guideEntry" class="${textarea}" required></textarea></div>
                                <div><label class="${label}" for="guideLate">지연 입장 *</label><textarea id="guideLate" class="${textarea}" required></textarea></div>
                            </div>
                        </details>
                        <details>
                            <summary class="flex min-h-11 cursor-pointer list-none items-center gap-2 px-4 text-sm font-bold [&::-webkit-details-marker]:hidden"><svg class="size-4 transition-transform [details[open]_&]:rotate-90" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="m9 18 6-6-6-6"/></svg>관람 규칙</summary>
                            <div class="grid gap-3 px-4 pb-4 md:grid-cols-2">
                                <div><label class="${label}" for="guideRecording">촬영·녹음 *</label><textarea id="guideRecording" class="${textarea}" required></textarea></div>
                                <div><label class="${label}" for="guideCancellation">신청 취소 *</label><textarea id="guideCancellation" class="${textarea}" required></textarea></div>
                            </div>
                        </details>
                        <details>
                            <summary class="flex min-h-11 cursor-pointer list-none items-center gap-2 px-4 text-sm font-bold [&::-webkit-details-marker]:hidden"><svg class="size-4 transition-transform [details[open]_&]:rotate-90" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="m9 18 6-6-6-6"/></svg>접근성</summary>
                            <div class="px-4 pb-4"><label class="${label}" for="guideAccessibility">접근성 안내 *</label><textarea id="guideAccessibility" class="${textarea}" required></textarea></div>
                        </details>
                        <details>
                            <summary class="flex min-h-11 cursor-pointer list-none items-center gap-2 px-4 text-sm font-bold [&::-webkit-details-marker]:hidden"><svg class="size-4 transition-transform [details[open]_&]:rotate-90" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="m9 18 6-6-6-6"/></svg>오시는 길·주차</summary>
                            <div class="grid gap-3 px-4 pb-4 md:grid-cols-2">
                                <div><label class="${label}" for="guideDirections">오시는 길 *</label><textarea id="guideDirections" class="${textarea}" required></textarea></div>
                                <div><label class="${label}" for="guideParking">주차 안내</label><textarea id="guideParking" class="${textarea}"></textarea></div>
                            </div>
                        </details>
                    </form>
                    <div class="sticky bottom-0 z-10 mt-6 -mx-4 border-t bg-card/95 px-4 py-3 backdrop-blur md:mx-0 md:rounded-lg md:border">
                        <div class="flex flex-wrap items-center gap-3">
                            <p data-guide-dirty class="min-w-0 flex-1 text-xs text-muted-foreground" aria-live="polite"></p>
                            <t:button pageAction="performance-guide-save">안내 저장</t:button>
                        </div>
                    </div>
                </section>
            </div>
        </div>
    </div>

    <%-- 프로젝트 sheet --%>
    <t:sheet id="performanceProjectSheet" title="공연 프로젝트" description="학기당 하나의 공연 프로젝트를 운영해요.">
        <jsp:attribute name="footer"><t:button variant="outline" action="close-sheet">닫기</t:button><t:button pageAction="performance-project-save">프로젝트 저장</t:button></jsp:attribute>
        <jsp:body>
            <form data-project-form class="grid gap-4">
                <div class="grid gap-3 md:grid-cols-2">
                    <div><label class="${label}" for="projectYear">학년도 *</label><input id="projectYear" class="${input}" type="number" min="2000" required></div>
                    <div><label class="${label}" for="projectTerm">학기 *</label><select id="projectTerm" class="${input}" required><option value="FIRST">1학기</option><option value="SECOND">2학기</option></select></div>
                </div>
                <div><label class="${label}" for="projectTitle">작품명 *</label><input id="projectTitle" class="${input}" required maxlength="200"></div>
                <div><label class="${label}" for="projectPlace">공연 장소 *</label><input id="projectPlace" class="${input}" required maxlength="200"></div>
                <div class="grid gap-3 md:grid-cols-2">
                    <div><label class="${label}" for="projectStart">제작 시작 *</label><input id="projectStart" class="${input}" type="date" required></div>
                    <div><label class="${label}" for="projectEnd">제작 종료 *</label><input id="projectEnd" class="${input}" type="date" required></div>
                </div>
                <p data-project-form-error class="hidden rounded-md bg-destructive-soft px-3 py-2.5 text-sm text-destructive" role="alert"></p>
            </form>
        </jsp:body>
    </t:sheet>

    <%-- 회차 sheet --%>
    <t:sheet id="performanceRoundSheet" title="공연 회차" description="신청과 입장 시각은 공연 시작보다 앞서야 해요.">
        <jsp:attribute name="footer"><t:button variant="outline" action="close-sheet">닫기</t:button><t:button pageAction="performance-round-save">회차 저장</t:button></jsp:attribute>
        <jsp:body>
            <form data-round-form class="grid gap-4">
                <div><label class="${label}" for="roundNo">회차 *</label><input id="roundNo" class="${input}" type="number" min="1" required></div>
                <div><label class="${label}" for="roundStart">공연 시작 *</label><input id="roundStart" class="${input}" type="datetime-local" required></div>
                <div><label class="${label}" for="roundEntry">입장 시작 *</label><input id="roundEntry" class="${input}" type="datetime-local" required></div>
                <div class="grid gap-3 md:grid-cols-2">
                    <div><label class="${label}" for="roundReservationOpen">신청 시작 *</label><input id="roundReservationOpen" class="${input}" type="datetime-local" required></div>
                    <div><label class="${label}" for="roundReservationClose">신청 마감 *</label><input id="roundReservationClose" class="${input}" type="datetime-local" required></div>
                </div>
                <p data-round-form-error class="hidden rounded-md bg-destructive-soft px-3 py-2.5 text-sm text-destructive" role="alert"></p>
            </form>
        </jsp:body>
    </t:sheet>

    <%-- 상태 변경 sheet (프로젝트/회차/공개 페이지 공용) --%>
    <t:sheet id="performanceStatusSheet" title="상태 변경">
        <jsp:attribute name="footer"><t:button variant="outline" action="close-sheet">닫기</t:button><t:button pageAction="performance-status-save">상태 변경</t:button></jsp:attribute>
        <jsp:body>
            <p data-status-summary class="rounded-md bg-secondary px-3 py-2.5 text-sm font-bold"></p>
            <fieldset class="mt-4">
                <legend class="${label}">변경할 상태</legend>
                <div data-status-options class="flex flex-col gap-2" role="radiogroup"></div>
            </fieldset>
            <p data-status-error class="mt-3 hidden rounded-md bg-destructive-soft px-3 py-2.5 text-sm text-destructive" role="alert"></p>
        </jsp:body>
    </t:sheet>

    <%-- 회차 접근성 sheet --%>
    <t:sheet id="performanceAccessibilitySheet" title="회차 접근성" description="자막·수어·음성 해설 등 실제 제공 정보를 입력해요.">
        <jsp:attribute name="footer"><t:button variant="outline" action="close-sheet">닫기</t:button><t:button pageAction="performance-accessibility-save">접근성 저장</t:button></jsp:attribute>
        <jsp:body>
            <div data-accessibility-list class="mb-4 flex flex-col gap-2"></div>
            <form data-accessibility-form class="grid gap-3 border-t pt-4">
                <div><label class="${label}" for="accessibilityType">지원 유형 *</label><select id="accessibilityType" class="${input}"><option value="CAPTION">자막</option><option value="SIGN_LANGUAGE">수어</option><option value="AUDIO_DESCRIPTION">음성 해설</option><option value="OTHER">기타</option></select></div>
                <div><label class="${label}" for="accessibilityTitle">표시명 *</label><input id="accessibilityTitle" class="${input}" required maxlength="100"></div>
                <div><label class="${label}" for="accessibilityDescription">설명</label><textarea id="accessibilityDescription" class="${textarea}"></textarea></div>
                <div><label class="${label}" for="accessibilityOrder">표시 순서 *</label><input id="accessibilityOrder" class="${input}" type="number" min="0" value="0" required></div>
            </form>
        </jsp:body>
    </t:sheet>

    <%-- 공시 연결 sheet --%>
    <t:sheet id="performanceNoticeSheet" title="공연 공시 연결" description="게시 중이거나 게시 예정인 공시를 선택해요.">
        <jsp:attribute name="footer"><t:button variant="outline" action="close-sheet">닫기</t:button><t:button pageAction="performance-notice-save">공시 연결</t:button></jsp:attribute>
        <jsp:body>
            <label class="${label}" for="performanceNoticeSelect">공시 *</label>
            <select id="performanceNoticeSelect" class="${input}"></select>
            <p class="mt-2 text-xs text-muted-foreground">목록은 최근 100건까지 보여요.</p>
        </jsp:body>
    </t:sheet>
</t:layout>
