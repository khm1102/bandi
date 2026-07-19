<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<c:set var="textarea" value="min-h-24 w-full resize-y rounded-md border border-input bg-card px-3 py-2.5 text-sm leading-6 focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20"/>
<t:layout title="공연 운영 설정" active="performance-management" role="${role}" scriptPath="performance/management">
    <t:pageHead title="공연 운영 설정" description="학기 공연 프로젝트부터 회차와 외부 공개 정보까지 순서대로 관리합니다">
        <t:button pageAction="performance-project-create-open">새 프로젝트</t:button>
    </t:pageHead>

    <section class="mb-5 rounded-lg border bg-card p-5" aria-label="공연 프로젝트 선택">
        <div class="grid gap-4 md:grid-cols-[1fr_auto] md:items-end">
            <div><label class="${label}" for="performanceProjectSelect">공연 프로젝트</label><select id="performanceProjectSelect" class="${input}"></select></div>
            <div data-performance-project-actions class="flex flex-wrap gap-2"></div>
        </div>
        <div data-performance-project-summary class="mt-4 rounded-md bg-secondary px-4 py-3 text-sm text-muted-foreground">프로젝트 정보를 불러오는 중입니다.</div>
    </section>

    <div class="grid gap-5 xl:grid-cols-[1.05fr_0.95fr]">
        <section class="rounded-lg border bg-card" aria-labelledby="performanceRoundsTitle">
            <header class="flex items-center gap-3 border-b px-5 py-4"><div><h2 id="performanceRoundsTitle" class="text-sm font-extrabold">공연 회차</h2><p class="mt-1 text-xs text-muted-foreground">신청과 입장 시간, 회차별 접근성을 관리합니다.</p></div><t:button cssClass="ml-auto" size="compact" pageAction="performance-round-create-open">회차 추가</t:button></header>
            <div data-performance-rounds class="divide-y" aria-live="polite"><p class="px-5 py-10 text-center text-sm text-muted-foreground">회차를 불러오는 중입니다.</p></div>
        </section>

        <section class="rounded-lg border bg-card p-5" aria-labelledby="performanceNoticesTitle">
            <div class="flex items-center gap-3"><div><h2 id="performanceNoticesTitle" class="text-sm font-extrabold">공연 관련 공시</h2><p class="mt-1 text-xs text-muted-foreground">공개 공연 페이지에 함께 표시할 공시입니다.</p></div><t:button cssClass="ml-auto" size="compact" pageAction="performance-notice-open">공시 연결</t:button></div>
            <div data-performance-notices class="mt-4 flex flex-col gap-2" aria-live="polite"></div>
        </section>
    </div>

    <section class="mt-5 rounded-lg border bg-card p-5" aria-labelledby="publicPageTitle">
        <div class="flex flex-col gap-3 border-b pb-4 md:flex-row md:items-end md:justify-between"><div><h2 id="publicPageTitle" class="text-lg font-extrabold">외부 공연 페이지</h2><p class="mt-1 text-xs text-muted-foreground">작품 소개와 관람 정보를 저장한 뒤 공개 상태를 전환합니다.</p></div><div class="flex flex-wrap items-center gap-2"><span data-public-page-status><t:badge tone="neutral">페이지 없음</t:badge></span><a data-public-page-link class="hidden min-h-11 items-center rounded-md border bg-card px-4 text-sm font-bold hover:bg-secondary" target="_blank" rel="noopener noreferrer">공개 페이지 열기</a><t:button pageAction="performance-public-save">페이지 저장</t:button><t:button variant="outline" pageAction="performance-public-status-open">공개 상태</t:button></div></div>
        <form data-public-page-form class="mt-5 grid gap-5 lg:grid-cols-[1fr_18rem]">
            <div class="flex flex-col gap-4">
                <div class="grid gap-3 md:grid-cols-2"><div><label class="${label}" for="publicSlug">공개 주소 *</label><input id="publicSlug" class="${input}" required maxlength="100" pattern="[a-z0-9-]+" placeholder="hamlet-2026"></div><div><label class="${label}" for="publicGenre">장르 *</label><input id="publicGenre" class="${input}" required maxlength="100"></div></div>
                <div><label class="${label}" for="publicShortDescription">짧은 소개 *</label><textarea id="publicShortDescription" class="${textarea}" required maxlength="500"></textarea></div>
                <div><label class="${label}" for="publicSynopsis">시놉시스 *</label><textarea id="publicSynopsis" class="${textarea} min-h-48" required></textarea></div>
                <div><label class="${label}" for="publicDirectorNote">연출 노트</label><textarea id="publicDirectorNote" class="${textarea}"></textarea></div>
                <div class="grid gap-3 md:grid-cols-3"><div><label class="${label}" for="publicAgeRating">관람 등급 *</label><input id="publicAgeRating" class="${input}" required maxlength="50"></div><div><label class="${label}" for="publicRuntime">러닝타임(분) *</label><input id="publicRuntime" class="${input}" type="number" min="1" required></div><div><label class="${label}" for="publicIntermission">인터미션(분)</label><input id="publicIntermission" class="${input}" type="number" min="0"></div></div>
                <div class="rounded-md border bg-secondary/40 p-4"><h3 class="text-sm font-extrabold">검색·공유 메타데이터</h3><p class="mt-1 text-xs text-muted-foreground">검색 결과와 메신저·SNS 링크 미리보기에 표시됩니다.</p><div class="mt-4 grid gap-3"><div><label class="${label}" for="publicOgTitle">공유 제목</label><input id="publicOgTitle" class="${input}" maxlength="200"></div><div><label class="${label}" for="publicOgDescription">공유 설명</label><textarea id="publicOgDescription" class="${textarea}" maxlength="500"></textarea></div><div><label class="${label}" for="publicOgImage">공유 이미지</label><input id="publicOgImage" type="file" accept="image/*" class="block w-full text-xs"><p class="mt-1 text-xs text-muted-foreground">선택하지 않으면 기존 이미지를 유지합니다.</p></div></div></div>
            </div>
            <aside class="flex flex-col gap-4">
                <div><label class="${label}" for="publicFee">관람료(원) *</label><input id="publicFee" class="${input}" type="number" min="0" value="0" required></div>
                <div><label class="${label}" for="publicOrganizer">주최 *</label><input id="publicOrganizer" class="${input}" required maxlength="200"></div>
                <div><label class="${label}" for="publicContactName">문의 이름 *</label><input id="publicContactName" class="${input}" required maxlength="100"></div>
                <div><label class="${label}" for="publicContactChannel">문의 채널 *</label><input id="publicContactChannel" class="${input}" required maxlength="200"></div>
                <div><label class="${label}" for="publicAccent">강조색</label><input id="publicAccent" class="${input}" maxlength="20" placeholder="#0F6F5D"></div>
                <div><label class="${label}" for="publicPublishStart">공개 시작</label><input id="publicPublishStart" class="${input}" type="datetime-local"></div>
                <div><label class="${label}" for="publicPublishEnd">공개 종료</label><input id="publicPublishEnd" class="${input}" type="datetime-local"></div>
                <div><label class="${label}" for="publicHero">히어로 이미지</label><input id="publicHero" type="file" accept="image/*" class="block w-full text-xs"></div>
                <div><label class="${label}" for="publicPoster">포스터</label><input id="publicPoster" type="file" accept="image/*" class="block w-full text-xs"></div>
            </aside>
        </form>
    </section>

    <section class="mt-5 rounded-lg border bg-card p-5" aria-labelledby="viewingGuideTitle">
        <div class="flex items-center gap-3 border-b pb-4"><div><h2 id="viewingGuideTitle" class="text-lg font-extrabold">관람 안내</h2><p class="mt-1 text-xs text-muted-foreground">공연 전체에 적용되는 입장·취소·접근성 정책입니다.</p></div><t:button cssClass="ml-auto" pageAction="performance-guide-save">안내 저장</t:button></div>
        <form data-viewing-guide-form class="mt-5 grid gap-4 md:grid-cols-2"><div><label class="${label}" for="guideEntry">입장 안내 *</label><textarea id="guideEntry" class="${textarea}" required></textarea></div><div><label class="${label}" for="guideLate">지연 입장 *</label><textarea id="guideLate" class="${textarea}" required></textarea></div><div><label class="${label}" for="guideRecording">촬영·녹음 *</label><textarea id="guideRecording" class="${textarea}" required></textarea></div><div><label class="${label}" for="guideCancellation">신청 취소 *</label><textarea id="guideCancellation" class="${textarea}" required></textarea></div><div><label class="${label}" for="guideAccessibility">접근성 *</label><textarea id="guideAccessibility" class="${textarea}" required></textarea></div><div><label class="${label}" for="guideDirections">오시는 길 *</label><textarea id="guideDirections" class="${textarea}" required></textarea></div><div class="md:col-span-2"><label class="${label}" for="guideParking">주차 안내</label><textarea id="guideParking" class="${textarea}"></textarea></div></form>
    </section>

    <t:modal id="performanceProjectModal" title="공연 프로젝트" description="학기당 하나의 공연 프로젝트를 운영합니다."><jsp:attribute name="footer"><t:button variant="outline" action="close-modal">취소</t:button><t:button pageAction="performance-project-save">저장</t:button></jsp:attribute><jsp:body><form data-project-form class="grid gap-4"><div class="grid gap-3 md:grid-cols-2"><div><label class="${label}" for="projectYear">학년도 *</label><input id="projectYear" class="${input}" type="number" min="2000" required></div><div><label class="${label}" for="projectTerm">학기 *</label><select id="projectTerm" class="${input}" required><option value="FIRST">1학기</option><option value="SECOND">2학기</option></select></div></div><div><label class="${label}" for="projectTitle">작품명 *</label><input id="projectTitle" class="${input}" required maxlength="200"></div><div><label class="${label}" for="projectPlace">공연 장소 *</label><input id="projectPlace" class="${input}" required maxlength="200"></div><div class="grid gap-3 md:grid-cols-2"><div><label class="${label}" for="projectStart">제작 시작 *</label><input id="projectStart" class="${input}" type="date" required></div><div><label class="${label}" for="projectEnd">제작 종료 *</label><input id="projectEnd" class="${input}" type="date" required></div></div></form></jsp:body></t:modal>

    <t:modal id="performanceRoundModal" title="공연 회차" description="신청과 입장 시각은 공연 시작보다 앞서야 합니다."><jsp:attribute name="footer"><t:button variant="outline" action="close-modal">취소</t:button><t:button pageAction="performance-round-save">저장</t:button></jsp:attribute><jsp:body><form data-round-form class="grid gap-4"><div><label class="${label}" for="roundNo">회차 *</label><input id="roundNo" class="${input}" type="number" min="1" required></div><div><label class="${label}" for="roundStart">공연 시작 *</label><input id="roundStart" class="${input}" type="datetime-local" required></div><div><label class="${label}" for="roundEntry">입장 시작 *</label><input id="roundEntry" class="${input}" type="datetime-local" required></div><div class="grid gap-3 md:grid-cols-2"><div><label class="${label}" for="roundReservationOpen">신청 시작 *</label><input id="roundReservationOpen" class="${input}" type="datetime-local" required></div><div><label class="${label}" for="roundReservationClose">신청 마감 *</label><input id="roundReservationClose" class="${input}" type="datetime-local" required></div></div></form></jsp:body></t:modal>

    <t:modal id="performanceAccessibilityModal" title="회차 접근성" description="자막·수어·음성 해설 등 실제 제공 정보를 입력합니다."><jsp:attribute name="footer"><t:button variant="outline" action="close-modal">닫기</t:button><t:button pageAction="performance-accessibility-save">추가</t:button></jsp:attribute><jsp:body><div data-accessibility-list class="mb-4 flex flex-col gap-2"></div><form data-accessibility-form class="grid gap-3"><div><label class="${label}" for="accessibilityType">지원 유형 *</label><select id="accessibilityType" class="${input}"><option value="CAPTION">자막</option><option value="SIGN_LANGUAGE">수어</option><option value="AUDIO_DESCRIPTION">음성 해설</option><option value="OTHER">기타</option></select></div><div><label class="${label}" for="accessibilityTitle">표시명 *</label><input id="accessibilityTitle" class="${input}" required maxlength="100"></div><div><label class="${label}" for="accessibilityDescription">설명</label><textarea id="accessibilityDescription" class="${textarea}"></textarea></div><div><label class="${label}" for="accessibilityOrder">표시 순서 *</label><input id="accessibilityOrder" class="${input}" type="number" min="0" value="0" required></div></form></jsp:body></t:modal>

    <t:modal id="performanceStatusModal" title="상태 변경"><jsp:attribute name="footer"><t:button variant="outline" action="close-modal">취소</t:button><t:button pageAction="performance-status-save">변경</t:button></jsp:attribute><jsp:body><p data-status-summary class="mb-4 rounded-md bg-secondary px-3 py-2.5 text-sm font-bold"></p><label class="${label}" for="performanceStatusValue">변경 상태 *</label><select id="performanceStatusValue" class="${input}"></select></jsp:body></t:modal>

    <t:modal id="performanceNoticeModal" title="공연 공시 연결" description="게시 중이거나 게시 예정인 공시를 선택합니다."><jsp:attribute name="footer"><t:button variant="outline" action="close-modal">취소</t:button><t:button pageAction="performance-notice-save">연결</t:button></jsp:attribute><jsp:body><label class="${label}" for="performanceNoticeSelect">공시 *</label><select id="performanceNoticeSelect" class="${input}"></select></jsp:body></t:modal>
</t:layout>
