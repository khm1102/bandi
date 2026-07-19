<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<c:set var="textarea" value="min-h-24 w-full resize-y rounded-md border border-input bg-card px-3 py-2.5 text-sm leading-6 focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20"/>
<t:layout title="공연 콘텐츠" active="performance-content-management" role="${role}" scriptPath="performance/content-management">
    <t:pageHead title="공연 콘텐츠" description="관객에게 공개할 인물과 배역, 제작진, 사진을 순서대로 준비해요"/>

    <section class="mb-6 border-b pb-5" aria-label="공연 콘텐츠 범위">
        <div class="max-w-2xl">
            <label class="${label}" for="contentProjectSelect">현재 공연</label>
            <select id="contentProjectSelect" class="${input}"></select>
            <p data-content-project-state class="mt-2 text-sm text-muted-foreground">프로젝트를 불러오는 중이에요.</p>
        </div>
    </section>

    <section class="mb-6 rounded-xl border border-primary/20 bg-accent px-5 py-5" aria-labelledby="contentNextTitle">
        <div class="flex flex-col gap-4 sm:flex-row sm:items-center">
            <div class="min-w-0 flex-1">
                <p class="text-xs font-bold text-accent-foreground">다음에 할 일</p>
                <h2 id="contentNextTitle" data-content-next-title class="mt-1 text-xl font-extrabold text-foreground">공개 준비 상태를 확인하고 있어요</h2>
                <p data-content-next-description class="mt-2 text-sm leading-6 text-muted-foreground">준비가 필요한 항목을 곧 알려드릴게요.</p>
            </div>
            <button type="button" data-content-next-action
                    class="min-h-11 shrink-0 rounded-md bg-primary px-5 text-sm font-bold text-primary-foreground transition-colors hover:bg-primary-strong focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2">
                준비 상태 보기
            </button>
        </div>
    </section>

    <section class="mb-6 border-y py-4" aria-label="공개 콘텐츠 현황">
        <dl data-content-summary class="grid grid-cols-2 gap-x-5 gap-y-4 sm:grid-cols-4">
            <div><dt class="text-xs font-bold text-muted-foreground">게시 프로필</dt><dd class="mt-1 text-xl font-extrabold tabular-nums" data-summary-profiles>-</dd></div>
            <div><dt class="text-xs font-bold text-muted-foreground">등장인물</dt><dd class="mt-1 text-xl font-extrabold tabular-nums" data-summary-characters>-</dd></div>
            <div><dt class="text-xs font-bold text-muted-foreground">작품 캐스팅</dt><dd class="mt-1 text-xl font-extrabold tabular-nums" data-summary-casts>-</dd></div>
            <div><dt class="text-xs font-bold text-muted-foreground">게시 미디어</dt><dd class="mt-1 text-xl font-extrabold tabular-nums" data-summary-media>-</dd></div>
        </dl>
    </section>

    <nav class="mb-6 overflow-x-auto border-b" aria-label="공연 콘텐츠 관리 영역">
        <div class="flex min-w-max gap-6" role="tablist">
            <button id="contentTabProfiles" type="button" role="tab" aria-selected="true" aria-controls="contentPanelProfiles" data-content-tab="profiles" class="min-h-11 border-b-2 border-primary px-1 text-sm font-bold text-foreground">공개 프로필</button>
            <button id="contentTabCasting" type="button" role="tab" aria-selected="false" aria-controls="contentPanelCasting" data-content-tab="casting" class="min-h-11 border-b-2 border-transparent px-1 text-sm font-bold text-muted-foreground">배역·캐스팅</button>
            <button id="contentTabRounds" type="button" role="tab" aria-selected="false" aria-controls="contentPanelRounds" data-content-tab="rounds" class="min-h-11 border-b-2 border-transparent px-1 text-sm font-bold text-muted-foreground">회차별 캐스팅</button>
            <button id="contentTabCredits" type="button" role="tab" aria-selected="false" aria-controls="contentPanelCredits" data-content-tab="credits" class="min-h-11 border-b-2 border-transparent px-1 text-sm font-bold text-muted-foreground">제작진·미디어</button>
        </div>
    </nav>

    <p data-content-status class="sr-only" role="status" aria-live="polite"></p>

    <section id="contentPanelProfiles" data-content-panel="profiles" role="tabpanel" aria-labelledby="contentTabProfiles">
        <header class="mb-4 flex flex-col gap-3 sm:flex-row sm:items-end">
            <div class="min-w-0 flex-1"><h2 id="profileTitle" class="text-xl font-bold">공개 프로필</h2><p class="mt-1 text-sm leading-6 text-muted-foreground">내부 멤버 정보와 분리해 공개 이름과 사진, 소개의 동의를 관리해요.</p></div>
            <div class="flex flex-wrap gap-2"><t:button variant="outline" pageAction="profile-policy-open">동의 문서</t:button><t:button variant="outline" pageAction="profile-create-open">프로필 추가</t:button></div>
        </header>
        <div data-profile-list class="divide-y border-y"><p class="py-10 text-center text-sm text-muted-foreground">프로필을 불러오는 중이에요.</p></div>
    </section>

    <section id="contentPanelCasting" data-content-panel="casting" role="tabpanel" class="hidden" aria-labelledby="contentTabCasting">
        <div class="grid gap-8 xl:grid-cols-2">
            <section aria-labelledby="castingTitle">
                <header class="mb-4 flex items-end gap-3"><div class="min-w-0 flex-1"><h2 id="castingTitle" class="text-xl font-bold">등장인물</h2><p class="mt-1 text-sm text-muted-foreground">배역의 중요도와 공개 순서를 정해요.</p></div><t:button variant="outline" size="compact" pageAction="character-create-open">배역 추가</t:button></header>
                <div data-character-list class="divide-y border-y"></div>
            </section>
            <section aria-labelledby="projectCastTitle">
                <header class="mb-4 flex items-end gap-3"><div class="min-w-0 flex-1"><h2 id="projectCastTitle" class="text-xl font-bold">작품 캐스팅</h2><p class="mt-1 text-sm text-muted-foreground">공개 이름 동의를 마친 프로필만 배정할 수 있어요.</p></div><div class="flex gap-2"><t:button variant="outline" size="compact" pageAction="cast-history-open">이력</t:button><t:button variant="outline" size="compact" pageAction="cast-create-open">배정</t:button></div></header>
                <div data-cast-list class="divide-y border-y"></div>
            </section>
        </div>
    </section>

    <section id="contentPanelRounds" data-content-panel="rounds" role="tabpanel" class="hidden" aria-labelledby="contentTabRounds">
        <header class="mb-4 flex flex-col gap-4 lg:flex-row lg:items-end">
            <div class="min-w-0 flex-1"><h2 id="roundCastTitle" class="text-xl font-bold">회차별 실제 캐스팅</h2><p class="mt-1 text-sm text-muted-foreground">관객이 선택한 회차에서 실제로 만날 출연자를 확정해요.</p></div>
            <div class="w-full lg:w-80"><label class="${label}" for="roundCastRoundSelect">공연 회차</label><select id="roundCastRoundSelect" class="${input}"></select></div>
            <t:button variant="outline" pageAction="round-cast-create-open">회차 캐스팅 배정</t:button>
        </header>
        <div data-round-cast-list class="divide-y border-y"></div>
    </section>

    <section id="contentPanelCredits" data-content-panel="credits" role="tabpanel" class="hidden" aria-labelledby="contentTabCredits">
        <div class="grid gap-8 xl:grid-cols-2">
            <section aria-labelledby="creditTitle">
                <header class="mb-4 flex items-end gap-3"><div class="min-w-0 flex-1"><h2 id="creditTitle" class="text-xl font-bold">제작진 크레딧</h2><p class="mt-1 text-sm text-muted-foreground">프로그램북에 보일 순서로 담당 분야와 이름을 관리해요.</p></div><t:button variant="outline" size="compact" pageAction="credit-create-open">크레딧 추가</t:button></header>
                <div data-credit-list class="divide-y border-y"></div>
            </section>
            <section aria-labelledby="mediaTitleSection">
                <header class="mb-4 flex items-end gap-3"><div class="min-w-0 flex-1"><h2 id="mediaTitleSection" class="text-xl font-bold">사진·영상</h2><p class="mt-1 text-sm text-muted-foreground">MinIO 공개 파일과 대체 텍스트, 제작 크레딧을 관리해요.</p></div><t:button variant="outline" size="compact" pageAction="media-create-open">미디어 추가</t:button></header>
                <div data-media-list class="divide-y border-y"></div>
            </section>
        </div>
    </section>

    <t:sheet id="publicProfileSheet" title="공개 프로필" description="외부 참여자는 내부 멤버를 선택하지 않아도 돼요." presentation="workspace">
        <jsp:attribute name="footer"><t:button variant="outline" action="close-sheet">취소</t:button><t:button pageAction="profile-save">프로필 저장</t:button></jsp:attribute>
        <jsp:body><form data-profile-form class="grid gap-4"><div class="grid gap-4 md:grid-cols-2"><div><label class="${label}" for="profileMember">내부 멤버</label><select id="profileMember" class="${input}"><option value="">외부 참여자</option></select></div><div><label class="${label}" for="profileName">공개 이름 *</label><input id="profileName" class="${input}" required maxlength="100"></div></div><div><label class="${label}" for="profileBio">소개</label><textarea id="profileBio" class="${textarea}"></textarea></div><div class="grid gap-4 md:grid-cols-2"><div><label class="${label}" for="profileSocial">SNS 주소</label><input id="profileSocial" class="${input}" type="url" maxlength="500"></div><div><label class="${label}" for="profileImage">프로필 사진</label><input id="profileImage" type="file" accept="image/*" class="block min-h-11 w-full text-sm"><p class="mt-1 text-xs text-muted-foreground">새 파일을 선택하지 않으면 기존 사진을 유지해요.</p></div></div></form></jsp:body>
    </t:sheet>

    <t:sheet id="profileConsentSheet" title="프로필 공개 동의" description="이름·사진·소개·SNS를 항목별로 기록하고 철회해요." presentation="form">
        <jsp:attribute name="footer"><t:button variant="outline" action="close-sheet">닫기</t:button><t:button pageAction="profile-consent-save">동의 기록</t:button></jsp:attribute>
        <jsp:body><p data-consent-profile-name class="mb-4 rounded-md bg-secondary px-3 py-2 text-sm font-bold"></p><div data-consent-list class="mb-5 flex flex-col gap-2"></div><form data-consent-form class="grid gap-4"><div><label class="${label}" for="consentScope">동의 항목 *</label><select id="consentScope" class="${input}"><option value="NAME">공개 이름</option><option value="PHOTO">프로필 사진</option><option value="BIO">소개</option><option value="SOCIAL">SNS</option></select></div><div><label class="${label}" for="consentPolicyVersion">동의 문서 버전 *</label><select id="consentPolicyVersion" class="${input}" required></select></div></form></jsp:body>
    </t:sheet>

    <t:sheet id="profilePolicySheet" title="프로필 동의 문서" description="게시한 본문은 수정하지 않고 새 버전으로 발행해요." presentation="workspace">
        <jsp:attribute name="footer"><t:button variant="outline" action="close-sheet">취소</t:button><t:button pageAction="profile-policy-save">새 버전 발행</t:button></jsp:attribute>
        <jsp:body><form data-policy-form class="grid gap-4"><div><label class="${label}" for="policyDocument">정책 문서</label><select id="policyDocument" class="${input}"><option value="">새 개인정보 공개 동의 문서</option></select></div><div data-policy-title-field><label class="${label}" for="policyTitle">새 문서 제목 *</label><input id="policyTitle" class="${input}" maxlength="200" placeholder="공연 홍보용 공개 프로필 동의"></div><div><label class="${label}" for="policyBody">동의 본문 *</label><textarea id="policyBody" class="${textarea} min-h-48" required></textarea></div><div><label class="${label}" for="policyEffectiveFrom">적용 시작 *</label><input id="policyEffectiveFrom" class="${input}" type="datetime-local" required></div><label class="flex min-h-11 items-center gap-2 text-sm font-bold"><input id="policyRequired" type="checkbox" class="size-4" checked>필수 동의로 발행</label></form></jsp:body>
    </t:sheet>

    <t:sheet id="characterSheet" title="등장인물" presentation="form">
        <jsp:attribute name="footer"><t:button variant="outline" action="close-sheet">취소</t:button><t:button pageAction="character-save">배역 저장</t:button></jsp:attribute>
        <jsp:body><form data-character-form class="grid gap-4"><div><label class="${label}" for="characterName">배역명 *</label><input id="characterName" class="${input}" required maxlength="100"></div><div><label class="${label}" for="characterDescription">캐릭터 소개</label><textarea id="characterDescription" class="${textarea}"></textarea></div><div class="grid gap-3 sm:grid-cols-2"><div><label class="${label}" for="characterImportance">중요도 *</label><select id="characterImportance" class="${input}"><option value="LEAD">주연</option><option value="SUPPORT">조연</option><option value="ENSEMBLE">앙상블</option></select></div><div><label class="${label}" for="characterOrder">표시 순서 *</label><input id="characterOrder" class="${input}" type="number" min="0" value="0" required></div></div></form></jsp:body>
    </t:sheet>

    <t:sheet id="castSheet" title="작품 캐스팅" presentation="form">
        <jsp:attribute name="footer"><t:button variant="outline" action="close-sheet">취소</t:button><t:button pageAction="cast-save">캐스팅 저장</t:button></jsp:attribute>
        <jsp:body><form data-cast-form class="grid gap-4"><div><label class="${label}" for="castCharacter">등장인물 *</label><select id="castCharacter" class="${input}" required></select></div><div><label class="${label}" for="castProfile">공개 동의 프로필 *</label><select id="castProfile" class="${input}" required></select></div><div class="grid gap-3 sm:grid-cols-2"><div><label class="${label}" for="castType">배정 유형 *</label><select id="castType" class="${input}"><option value="PRIMARY">주 캐스팅</option><option value="ALTERNATE">대체</option><option value="UNDERSTUDY">언더스터디</option></select></div><div><label class="${label}" for="castOrder">표시 순서 *</label><input id="castOrder" class="${input}" type="number" min="0" value="0" required></div></div><div><label class="${label}" for="castReason">배정·변경 사유</label><textarea id="castReason" class="${textarea}" maxlength="500"></textarea></div></form></jsp:body>
    </t:sheet>

    <t:modal id="castHistoryModal" title="캐스팅 변경 이력" description="프로젝트와 회차별 배정·변경·해제 기록이에요."><jsp:attribute name="footer"><t:button variant="outline" action="close-modal">닫기</t:button></jsp:attribute><jsp:body><div data-cast-history-list class="max-h-[60vh] divide-y overflow-y-auto"></div></jsp:body></t:modal>

    <t:sheet id="roundCastSheet" title="회차별 캐스팅" presentation="form">
        <jsp:attribute name="footer"><t:button variant="outline" action="close-sheet">취소</t:button><t:button pageAction="round-cast-save">회차 캐스팅 저장</t:button></jsp:attribute>
        <jsp:body><form data-round-cast-form class="grid gap-4"><div><label class="${label}" for="roundCastCharacter">등장인물 *</label><select id="roundCastCharacter" class="${input}" required></select></div><div><label class="${label}" for="roundCastProfile">공개 동의 프로필 *</label><select id="roundCastProfile" class="${input}" required></select></div><div><label class="${label}" for="roundCastType">배정 유형 *</label><select id="roundCastType" class="${input}"><option value="PRIMARY">주 캐스팅</option><option value="ALTERNATE">대체</option><option value="UNDERSTUDY">언더스터디</option></select></div><div><label class="${label}" for="roundCastReason">배정·변경 사유</label><textarea id="roundCastReason" class="${textarea}" maxlength="500"></textarea></div></form></jsp:body>
    </t:sheet>

    <t:sheet id="creditSheet" title="제작진 크레딧" presentation="form">
        <jsp:attribute name="footer"><t:button variant="outline" action="close-sheet">취소</t:button><t:button pageAction="credit-save">크레딧 저장</t:button></jsp:attribute>
        <jsp:body><form data-credit-form class="grid gap-4"><div><label class="${label}" for="creditRole">담당 분야 *</label><input id="creditRole" class="${input}" required maxlength="100" placeholder="연출, 무대, 조명"></div><div><label class="${label}" for="creditName">공개 이름 *</label><input id="creditName" class="${input}" required maxlength="100"></div><div><label class="${label}" for="creditProfile">연결 프로필</label><select id="creditProfile" class="${input}"><option value="">연결하지 않음</option></select></div><div><label class="${label}" for="creditOrder">표시 순서 *</label><input id="creditOrder" class="${input}" type="number" min="0" value="0" required></div></form></jsp:body>
    </t:sheet>

    <t:sheet id="mediaSheet" title="공연 미디어" description="파일을 공개 저장소로 복제한 뒤 공연 페이지에 연결해요." presentation="workspace">
        <jsp:attribute name="footer"><t:button variant="outline" action="close-sheet">취소</t:button><t:button pageAction="media-save">미디어 저장</t:button></jsp:attribute>
        <jsp:body><form data-media-form class="grid gap-4"><div class="grid gap-4 md:grid-cols-2"><div><label class="${label}" for="mediaType">유형 *</label><select id="mediaType" class="${input}"><option value="POSTER">포스터·키아트</option><option value="PROFILE">프로필</option><option value="REHEARSAL">연습</option><option value="BEHIND">비하인드</option><option value="STAGE">무대</option><option value="VIDEO">영상</option></select></div><div><label class="${label}" for="mediaFile">파일 *</label><input id="mediaFile" type="file" class="block min-h-11 w-full text-sm"><p class="mt-1 text-xs text-muted-foreground">수정할 때 선택하지 않으면 기존 파일을 유지해요.</p></div></div><div class="grid gap-4 md:grid-cols-[minmax(0,1fr)_9rem]"><div><label class="${label}" for="mediaTitle">제목 *</label><input id="mediaTitle" class="${input}" required maxlength="200"></div><div><label class="${label}" for="mediaOrder">표시 순서 *</label><input id="mediaOrder" class="${input}" type="number" min="0" value="0" required></div></div><div class="grid gap-4 md:grid-cols-2"><div><label class="${label}" for="mediaDescription">설명 *</label><textarea id="mediaDescription" class="${textarea}" required></textarea></div><div><label class="${label}" for="mediaAlt">대체 텍스트 *</label><textarea id="mediaAlt" class="${textarea}" required maxlength="500"></textarea></div></div><div class="grid gap-4 md:grid-cols-2"><div><label class="${label}" for="mediaCredit">촬영·제작 크레딧 *</label><input id="mediaCredit" class="${input}" required maxlength="500"></div><div><label class="${label}" for="mediaExternalUrl">외부 영상 주소</label><input id="mediaExternalUrl" class="${input}" type="url" maxlength="1000"></div></div></form></jsp:body>
    </t:sheet>
</t:layout>
