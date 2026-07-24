<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout title="내 프로필" active="profile" role="${role}" scriptPath="members/profile">
    <t:pageHead title="내 프로필" description="학교 SSO에서 확인한 정보와 동아리 소속을 확인합니다."/>

    <div class="flex flex-col gap-6" data-profile-root aria-busy="true">
        <section class="border-b pb-6" aria-labelledby="profilePhotoTitle">
            <div class="flex flex-col gap-5 sm:flex-row sm:items-center">
                <div class="relative flex size-24 shrink-0 items-center justify-center overflow-hidden rounded-full bg-primary text-3xl font-black text-primary-foreground">
                    <img class="hidden size-full object-cover" data-profile-photo alt="">
                    <span data-profile-initial>·</span>
                </div>
                <div class="min-w-0 flex-1">
                    <h2 id="profilePhotoTitle" class="text-lg font-extrabold">프로필 사진</h2>
                    <p class="mt-1 text-sm leading-6 text-muted-foreground">사진은 로그인한 동아리 멤버에게만 표시돼요. 외부 공개 화면에는 사용하지 않아요.</p>
                    <div class="mt-4 flex flex-wrap gap-2">
                        <label class="inline-flex min-h-11 cursor-pointer items-center justify-center rounded-md bg-primary px-4 text-sm font-bold text-primary-foreground transition-colors hover:bg-primary-strong">
                            사진 선택<input class="sr-only" type="file" accept="image/jpeg,image/png,image/webp" data-profile-photo-input>
                        </label>
                        <button type="button" class="hidden min-h-11 rounded-md border bg-card px-4 text-sm font-bold transition-colors hover:bg-secondary" data-profile-photo-delete>기본 아바타로 바꾸기</button>
                    </div>
                    <p class="mt-2 text-xs text-muted-foreground" data-profile-photo-message>JPEG, PNG, WebP 파일을 5MB까지 올릴 수 있어요.</p>
                </div>
            </div>
        </section>

        <section class="grid gap-5 lg:grid-cols-[minmax(0,1fr)_minmax(18rem,0.8fr)]" aria-label="기본 프로필 정보">
            <div>
                <h2 class="text-lg font-extrabold">기본 정보</h2>
                <dl class="mt-4 divide-y border-y">
                    <div class="grid grid-cols-[7rem_1fr] gap-3 py-3 text-sm"><dt class="font-bold text-muted-foreground">이름</dt><dd class="font-semibold" data-profile-name>불러오는 중</dd></div>
                    <div class="grid grid-cols-[7rem_1fr] gap-3 py-3 text-sm"><dt class="font-bold text-muted-foreground">학번</dt><dd class="font-semibold tabular-nums" data-profile-student-no>—</dd></div>
                    <div class="grid grid-cols-[7rem_1fr] gap-3 py-3 text-sm"><dt class="font-bold text-muted-foreground">기수</dt><dd data-profile-cohort>—</dd></div>
                    <div class="grid grid-cols-[7rem_1fr] gap-3 py-3 text-sm"><dt class="font-bold text-muted-foreground">현재 팀</dt><dd data-profile-team>—</dd></div>
                    <div class="grid grid-cols-[7rem_1fr] gap-3 py-3 text-sm"><dt class="font-bold text-muted-foreground">역할</dt><dd data-profile-role>—</dd></div>
                </dl>
            </div>
            <form class="border-t pt-5 lg:border-l lg:border-t-0 lg:pl-6 lg:pt-0" data-profile-team-form novalidate>
                <h2 class="text-lg font-extrabold">소속 팀 변경</h2>
                <p class="mt-1 text-sm leading-6 text-muted-foreground">변경 즉시 반영되며 사유와 변경 이력이 남아요.</p>
                <label class="mt-4 block text-sm font-bold" for="profileTeam">새 소속 팀</label>
                <select id="profileTeam" class="mt-2 min-h-11 w-full rounded-md border border-input bg-card px-3 text-base focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm" data-profile-team-select required></select>
                <label class="mt-4 block text-sm font-bold" for="profileTeamReason">변경 사유</label>
                <textarea id="profileTeamReason" class="mt-2 min-h-28 w-full resize-y rounded-md border border-input bg-card px-3 py-2.5 text-base focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm" maxlength="500" data-profile-team-reason required placeholder="예) 이번 학기 팀 이동"></textarea>
                <p class="mt-2 hidden text-sm text-destructive" data-profile-team-error role="alert"></p>
                <button type="submit" class="mt-4 inline-flex min-h-11 items-center justify-center rounded-md bg-primary px-4 text-sm font-bold text-primary-foreground transition-colors hover:bg-primary-strong" data-profile-team-submit>소속 팀 변경</button>
            </form>
        </section>

        <section class="border-t pt-6" aria-labelledby="profileSsoTitle">
            <h2 id="profileSsoTitle" class="text-lg font-extrabold">학교 SSO 정보</h2>
            <p class="mt-1 text-sm leading-6 text-muted-foreground">학교에서 확인한 정보예요. 직접 수정할 수 없고 다음 로그인 때 갱신돼요.</p>
            <dl class="mt-4 grid gap-x-8 border-y sm:grid-cols-2">
                <div class="grid grid-cols-[7rem_1fr] gap-3 py-3 text-sm"><dt class="font-bold text-muted-foreground">학과</dt><dd data-profile-department>—</dd></div>
                <div class="grid grid-cols-[7rem_1fr] gap-3 py-3 text-sm"><dt class="font-bold text-muted-foreground">휴대폰</dt><dd class="tabular-nums" data-profile-phone-number>—</dd></div>
                <div class="grid grid-cols-[7rem_1fr] gap-3 py-3 text-sm"><dt class="font-bold text-muted-foreground">학적</dt><dd data-profile-academic-status>—</dd></div>
                <div class="grid grid-cols-[7rem_1fr] gap-3 py-3 text-sm"><dt class="font-bold text-muted-foreground">SSO 연결</dt><dd data-profile-sso-status>—</dd></div>
                <div class="grid grid-cols-[7rem_1fr] gap-3 py-3 text-sm"><dt class="font-bold text-muted-foreground">마지막 확인</dt><dd class="tabular-nums" data-profile-verified-at>—</dd></div>
                <div class="grid grid-cols-[7rem_1fr] gap-3 py-3 text-sm"><dt class="font-bold text-muted-foreground">마지막 로그인</dt><dd class="tabular-nums" data-profile-login-at>—</dd></div>
            </dl>
        </section>
    </div>
</t:layout>
