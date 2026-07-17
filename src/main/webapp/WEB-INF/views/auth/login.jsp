<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<t:layoutAuth title="${mode == 'signup' ? '회원가입' : '로그인'}" scriptPath="auth/login">
    <div class="mb-4 grid grid-cols-2 gap-1 rounded-lg border bg-secondary p-1">
        <a href="<c:url value='/login'/>" class="inline-flex min-h-11 items-center justify-center rounded-md text-sm font-extrabold transition-colors ${mode == 'login' ? 'border bg-card text-foreground' : 'text-muted-foreground'}" aria-current="${mode == 'login' ? 'page' : 'false'}">로그인</a>
        <a href="<c:url value='/login'/>?mode=signup" class="inline-flex min-h-11 items-center justify-center rounded-md text-sm font-extrabold transition-colors ${mode == 'signup' ? 'border bg-card text-foreground' : 'text-muted-foreground'}" aria-current="${mode == 'signup' ? 'page' : 'false'}">회원가입</a>
    </div>

    <c:choose>
        <c:when test="${mode == 'login'}">
            <form class="flex flex-col gap-3" method="post" data-auth-form data-auth-mode="login">
                <div>
                    <label class="${label}" for="loginId">아이디</label>
                    <input class="${input}" id="loginId" name="username" type="text" autocomplete="username" placeholder="아이디를 입력하세요" required maxlength="50" aria-describedby="authError">
                </div>
                <div>
                    <label class="${label}" for="loginPw">비밀번호</label>
                    <input class="${input}" id="loginPw" name="password" type="password" autocomplete="current-password" placeholder="비밀번호를 입력하세요" required maxlength="100" aria-describedby="authError">
                </div>
                <div>
                    <label class="${label}" for="loginTeam">소속 팀</label>
                    <select class="${input}" id="loginTeam" name="team" required aria-describedby="authError">
                        <option>배우연출</option><option>무대</option><option>오퍼</option><option>디자인</option><option>영상</option><option>운영진</option>
                    </select>
                </div>
                <p id="authError" class="hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-sm text-destructive" data-auth-error role="alert" aria-live="polite"></p>
                <t:button type="submit" cssClass="mt-1 w-full">로그인</t:button>
            </form>
        </c:when>
        <c:otherwise>
            <form class="flex flex-col gap-3" method="post" data-auth-form data-auth-mode="signup">
                <div class="grid gap-2.5 md:grid-cols-2">
                    <div>
                        <label class="${label}" for="joinName">이름 <span aria-hidden="true">*</span></label>
                        <input class="${input}" id="joinName" name="name" type="text" autocomplete="name" placeholder="이름" required maxlength="50" aria-describedby="authError">
                    </div>
                    <div>
                        <label class="${label}" for="joinTeam">소속 팀 <span aria-hidden="true">*</span></label>
                        <select class="${input}" id="joinTeam" name="team" required>
                            <option>배우연출</option><option>무대</option><option>오퍼</option><option>디자인</option><option>영상</option>
                        </select>
                    </div>
                </div>
                <div>
                    <label class="${label}" for="joinId">아이디 <span aria-hidden="true">*</span></label>
                    <input class="${input}" id="joinId" name="username" type="text" autocomplete="username" placeholder="사용할 아이디" required maxlength="50" aria-describedby="authError">
                </div>
                <div class="grid gap-2.5 md:grid-cols-2">
                    <div>
                        <label class="${label}" for="joinPw">비밀번호 <span aria-hidden="true">*</span></label>
                        <input class="${input}" id="joinPw" name="newPassword" type="password" autocomplete="new-password" placeholder="비밀번호" required maxlength="100" aria-describedby="authError">
                    </div>
                    <div>
                        <label class="${label}" for="joinPw2">비밀번호 확인 <span aria-hidden="true">*</span></label>
                        <input class="${input}" id="joinPw2" name="newPasswordConfirm" type="password" autocomplete="new-password" placeholder="한 번 더 입력" required maxlength="100" aria-describedby="authError">
                    </div>
                </div>
                <div>
                    <label class="${label}" for="joinCode">운영진 초대코드 <span aria-hidden="true">*</span></label>
                    <input class="${input} font-mono uppercase tracking-widest" id="joinCode" name="inviteCode" type="text" autocapitalize="characters" placeholder="예: BANDI-262-M9Q4" required maxlength="40" aria-describedby="joinCodeHelp authError">
                    <p id="joinCodeHelp" class="mt-1.5 text-xs text-muted-foreground">초대코드에 연결된 기수로 자동 가입됩니다. 예: 26-1기, 26-2기</p>
                </div>
                <p id="authError" class="hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-sm text-destructive" data-auth-error role="alert" aria-live="polite"></p>
                <t:button type="submit" cssClass="mt-1 w-full">회원가입</t:button>
            </form>
        </c:otherwise>
    </c:choose>

    <div class="my-4 flex items-center gap-2.5 text-xs font-bold text-muted-foreground/70 before:h-px before:flex-1 before:bg-border after:h-px after:flex-1 after:bg-border">또는</div>
    <a href="<c:url value='/reserve'/>" class="inline-flex min-h-11 w-full items-center justify-center rounded-md border bg-card px-4 text-sm font-bold transition-colors hover:bg-secondary">외부 관람객으로 공연 신청하기</a>
</t:layoutAuth>
