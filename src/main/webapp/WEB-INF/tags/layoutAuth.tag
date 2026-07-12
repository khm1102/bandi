<%@ tag description="인증 셸 — 로그인/가입 중앙 카드" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="script" fragment="true" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<t:head title="${title}"/>
</head>
<body>
<div class="flex min-h-screen items-center justify-center bg-linear-to-br from-accent via-background to-secondary p-6">
    <div class="w-full max-w-md rounded-xl border bg-card p-8 shadow-xl">
        <div class="mb-5 flex items-center gap-3">
            <span class="flex size-11 items-center justify-center rounded-lg bg-primary text-xl">🎭</span>
            <span>
                <b class="block text-lg font-black tracking-tight">bandi</b>
                <span class="block text-xs font-semibold text-muted-foreground">연극 동아리 통합 관리</span>
            </span>
        </div>
        <jsp:doBody/>
    </div>
</div>
<jsp:invoke fragment="script"/>
</body>
</html>
