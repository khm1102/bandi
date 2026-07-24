<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout title="개인정보 안내" role="${role}" crumb="개인정보 안내">
    <div class="mx-auto w-full max-w-3xl">
        <t:pageHead title="개인정보 안내" description="반디가 동아리 운영을 위해 확인하고 사용하는 정보를 안내합니다."/>

        <div class="divide-y rounded-xl border bg-card">
            <section class="p-5 md:p-7" aria-labelledby="privacyInfoTitle">
                <h2 id="privacyInfoTitle" class="text-lg font-extrabold">확인하는 정보</h2>
                <p class="mt-3 text-sm leading-7 text-muted-foreground">학교 SSO 로그인 과정에서 학번, 이름, 학과, 학적 상태와 휴대폰 번호를 확인할 수 있어요. 동아리 안에서는 소속 팀, 기수, 역할 같은 운영 정보를 함께 관리합니다.</p>
            </section>
            <section class="p-5 md:p-7" aria-labelledby="privacyPurposeTitle">
                <h2 id="privacyPurposeTitle" class="text-lg font-extrabold">사용하는 목적</h2>
                <ul class="mt-3 list-disc space-y-2 pl-5 text-sm leading-7 text-muted-foreground">
                    <li>학교 계정 인증과 로그인 멤버 확인</li>
                    <li>팀·역할에 따른 메뉴와 운영 기능 접근 제어</li>
                    <li>공지, 자료, 활동 기록과 소품·장비의 내부 운영</li>
                </ul>
            </section>
            <section class="p-5 md:p-7" aria-labelledby="privacyStorageTitle">
                <h2 id="privacyStorageTitle" class="text-lg font-extrabold">파일과 게시글</h2>
                <p class="mt-3 text-sm leading-7 text-muted-foreground">업로드 파일은 서버의 영구 저장 경로에 보관하고, 데이터베이스에는 파일 이름과 저장 위치 등 관리 정보만 분리해 저장합니다. 학교 계정 비밀번호는 반디에 저장하지 않습니다.</p>
                <p class="mt-3 text-sm leading-7 text-muted-foreground">공유 링크를 발급한 게시글은 외부 미리보기에 제목만 표시될 수 있으며, 본문과 첨부 파일은 로그인한 멤버만 볼 수 있어요.</p>
            </section>
            <section class="p-5 md:p-7" aria-labelledby="privacyHelpTitle">
                <h2 id="privacyHelpTitle" class="text-lg font-extrabold">정보 확인과 문의</h2>
                <p class="mt-3 text-sm leading-7 text-muted-foreground">내 정보가 다르거나 처리 방법이 궁금하면 운영 문의에서 필요한 정보를 확인해 주세요. 소속 팀과 기수 변경처럼 운영 확인이 필요한 항목은 사유와 함께 요청하면 됩니다.</p>
                <a href="<c:url value='/support'/>" class="mt-5 inline-flex min-h-11 items-center justify-center rounded-md border bg-card px-4 text-sm font-bold transition-colors hover:bg-secondary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2">운영 문의 보기</a>
            </section>
        </div>
    </div>
</t:layout>
