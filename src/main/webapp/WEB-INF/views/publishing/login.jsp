<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-10 w-full rounded-md border border-input bg-card px-3 text-sm transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<t:layoutAuth title="${mode == 'signup' ? '회원가입' : '로그인'}">
    <div class="mb-4 grid grid-cols-2 gap-1 rounded-lg border bg-secondary p-1">
        <a href="<c:url value='/login'/>" class="rounded-md py-2 text-center text-sm font-extrabold transition-colors ${mode == 'login' ? 'border bg-card text-foreground' : 'text-muted-foreground'}">로그인</a>
        <a href="<c:url value='/login'/>?mode=signup" class="rounded-md py-2 text-center text-sm font-extrabold transition-colors ${mode == 'signup' ? 'border bg-card text-foreground' : 'text-muted-foreground'}">회원가입</a>
    </div>

    <c:choose>
        <c:when test="${mode == 'login'}">
            <div class="flex flex-col gap-3">
                <div>
                    <label class="${label}" for="loginId">아이디</label>
                    <input class="${input}" id="loginId" type="text" placeholder="아이디를 입력하세요" value="member">
                </div>
                <div>
                    <label class="${label}" for="loginPw">비밀번호</label>
                    <input class="${input}" id="loginPw" type="password" placeholder="비밀번호를 입력하세요" value="1234">
                </div>
                <div>
                    <label class="${label}" for="loginTeam">소속 팀</label>
                    <select class="${input}" id="loginTeam">
                        <option>배우연출</option><option>무대</option><option>오퍼</option><option>디자인</option><option>영상</option><option>운영진</option>
                    </select>
                </div>
                <a href="<c:url value='/dashboard'/>" class="mt-1 inline-flex h-12 w-full items-center justify-center rounded-md bg-primary text-base font-black text-primary-foreground transition-colors hover:bg-primary-strong hover:text-white">로그인</a>
                <p class="text-xs leading-relaxed text-muted-foreground">테스트 계정: member / 1234 · leader / 1234 · admin / 1234<br>운영진 계정은 팀 선택과 관계없이 로그인됩니다.</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="flex flex-col gap-3">
                <div class="grid grid-cols-2 gap-2.5">
                    <div>
                        <label class="${label}" for="joinName">이름 <span class="text-accent-foreground">*</span></label>
                        <input class="${input}" id="joinName" type="text" placeholder="이름">
                    </div>
                    <div>
                        <label class="${label}" for="joinTeam">소속 팀 <span class="text-accent-foreground">*</span></label>
                        <select class="${input}" id="joinTeam">
                            <option>배우연출</option><option>무대</option><option>오퍼</option><option>디자인</option><option>영상</option>
                        </select>
                    </div>
                </div>
                <div>
                    <label class="${label}" for="joinId">아이디 <span class="text-accent-foreground">*</span></label>
                    <input class="${input}" id="joinId" type="text" placeholder="사용할 아이디">
                </div>
                <div class="grid grid-cols-2 gap-2.5">
                    <div>
                        <label class="${label}" for="joinPw">비밀번호 <span class="text-accent-foreground">*</span></label>
                        <input class="${input}" id="joinPw" type="password" placeholder="비밀번호">
                    </div>
                    <div>
                        <label class="${label}" for="joinPw2">비밀번호 확인 <span class="text-accent-foreground">*</span></label>
                        <input class="${input}" id="joinPw2" type="password" placeholder="한 번 더 입력">
                    </div>
                </div>
                <div>
                    <label class="${label}" for="joinCode">운영진 초대코드 <span class="text-accent-foreground">*</span></label>
                    <input class="${input} font-mono uppercase tracking-widest" id="joinCode" type="text" placeholder="예: BANDI-262-M9Q4">
                    <p class="mt-1.5 text-xs text-muted-foreground">초대코드에 연결된 기수로 자동 가입됩니다. 예: 26-1기, 26-2기</p>
                </div>
                <button type="button" class="mt-1 inline-flex h-12 w-full items-center justify-center rounded-md bg-primary text-base font-black text-primary-foreground transition-colors hover:bg-primary-strong hover:text-white">회원가입</button>
            </div>
        </c:otherwise>
    </c:choose>

    <div class="my-4 flex items-center gap-2.5 text-xs font-bold text-muted-foreground/70 before:h-px before:flex-1 before:bg-border after:h-px after:flex-1 after:bg-border">또는</div>
    <a href="<c:url value='/reserve'/>" class="inline-flex h-10 w-full items-center justify-center rounded-md border bg-card text-sm font-bold transition-colors hover:bg-secondary">외부 관람객으로 공연 신청하기</a>
</t:layoutAuth>
