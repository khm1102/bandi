<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout title="운영 문의" role="${role}" crumb="운영 문의">
    <div class="mx-auto w-full max-w-3xl">
        <t:pageHead title="운영 문의" description="어떤 도움을 받을 수 있는지 먼저 확인해 주세요."/>

        <div class="divide-y rounded-xl border bg-card">
            <section class="p-5 md:p-7" aria-labelledby="supportAccountTitle">
                <h2 id="supportAccountTitle" class="text-lg font-extrabold">로그인과 내 정보</h2>
                <p class="mt-3 text-sm leading-7 text-muted-foreground">학교 SSO 로그인, 이름·학과·학적 정보, 소속 팀·기수 정보가 다르게 보이면 운영진에게 알려 주세요. 비밀번호는 어떤 경우에도 전달하지 마세요.</p>
                <a href="<c:url value='/profile'/>" class="mt-5 inline-flex min-h-11 items-center justify-center rounded-md border bg-card px-4 text-sm font-bold transition-colors hover:bg-secondary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2">내 프로필 확인하기</a>
            </section>
            <section class="p-5 md:p-7" aria-labelledby="supportFeatureTitle">
                <h2 id="supportFeatureTitle" class="text-lg font-extrabold">사이트 이용 중 문제가 생겼다면</h2>
                <p class="mt-3 text-sm leading-7 text-muted-foreground">오류가 난 화면의 주소, 발생한 시각, 어떤 동작을 했는지와 화면 캡처를 함께 보내면 더 빠르게 확인할 수 있어요. 개인정보나 학교 계정 비밀번호는 캡처에 포함하지 않아야 합니다.</p>
            </section>
            <section class="p-5 md:p-7" aria-labelledby="supportContentTitle">
                <h2 id="supportContentTitle" class="text-lg font-extrabold">공지·자료·활동 기록</h2>
                <p class="mt-3 text-sm leading-7 text-muted-foreground">공지나 자료의 내용 정정, 활동 기록 검수와 소품·장비 정보 관련 문의는 해당 팀 운영진에게 먼저 알려 주세요. 권한 또는 처리 상태 확인이 필요하면 운영진이 이어서 확인합니다.</p>
                <div class="mt-5 flex flex-wrap gap-3">
                    <a href="<c:url value='/notices'/>" class="inline-flex min-h-11 items-center justify-center rounded-md border bg-card px-4 text-sm font-bold transition-colors hover:bg-secondary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2">공지 보기</a>
                    <a href="<c:url value='/resources'/>" class="inline-flex min-h-11 items-center justify-center rounded-md border bg-card px-4 text-sm font-bold transition-colors hover:bg-secondary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2">자료실 보기</a>
                </div>
            </section>
        </div>
    </div>
</t:layout>
