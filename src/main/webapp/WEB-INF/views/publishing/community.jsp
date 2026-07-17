<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="btnPrimary" value="inline-flex h-9 items-center justify-center gap-1.5 rounded-md bg-primary px-4 text-sm font-bold text-primary-foreground transition-colors hover:bg-primary-strong hover:text-white"/>
<c:set var="btnOutline" value="inline-flex h-9 items-center justify-center gap-1.5 rounded-md border bg-card px-4 text-sm font-bold transition-colors hover:bg-secondary"/>
<c:set var="chip" value="inline-flex h-8 items-center gap-1.5 rounded-md border bg-card px-3 text-xs font-bold text-muted-foreground transition-colors hover:border-sidebar-muted"/>
<c:set var="chipOn" value="inline-flex h-8 items-center gap-1.5 rounded-md border border-sidebar bg-sidebar px-3 text-xs font-bold text-white"/>
<c:set var="input" value="h-10 w-full rounded-md border border-input bg-card px-3 text-sm transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<t:layout title="게시판" active="community" role="${role}">
    <t:pageHead title="커뮤니티 · 게시판" description="공지·자유 의견·질문을 나누는 공간입니다">
        <c:if test="${role != 'member'}">
            <button type="button" data-open-modal="noticePostModal" class="${btnOutline}">공지 작성</button>
        </c:if>
        <button type="button" data-open-modal="postModal" class="${btnPrimary}">글쓰기</button>
    </t:pageHead>

    <div class="mb-4 flex flex-wrap gap-2">
        <button type="button" class="${chipOn}">전체</button>
        <button type="button" class="${chip}">공지</button>
        <button type="button" class="${chip}">자유</button>
        <button type="button" class="${chip}">질문</button>
    </div>

    <div class="flex flex-col gap-3">
        <div class="rounded-lg border border-primary/40 bg-card p-5 ring-2 ring-ring/10">
            <div class="mb-2 flex items-center gap-2.5">
                <span class="flex size-7 items-center justify-center rounded-full bg-primary text-xs font-black text-primary-foreground">LS</span>
                <div class="min-w-0 flex-1"><b class="block text-sm">이서준</b><span class="text-xs text-muted-foreground">2시간 전</span></div>
                <t:badge tone="accent">📌 공지</t:badge>
            </div>
            <p class="text-sm font-extrabold">정기공연 최종 리허설 안내</p>
            <p class="mt-1 text-sm text-muted-foreground">6/20(금) 18시 전원 소집. 의상·소품 지참 바랍니다.</p>
            <div class="mt-3 flex items-center gap-4 text-xs font-bold text-muted-foreground"><span>♥ 12</span><span>💬 4</span></div>
        </div>
        <div class="rounded-lg border bg-card p-5">
            <div class="mb-2 flex items-center gap-2.5">
                <span class="flex size-7 items-center justify-center rounded-full bg-accent-foreground text-xs font-black text-white">PS</span>
                <div class="min-w-0 flex-1"><b class="block text-sm">박서연</b><span class="text-xs text-muted-foreground">5시간 전</span></div>
                <t:badge tone="neutral">자유</t:badge>
            </div>
            <p class="text-sm font-extrabold">2막 전환 타이밍 관련 아이디어</p>
            <p class="mt-1 text-sm text-muted-foreground">암전 대신 조명 페이드로 가면 어떨까요?</p>
            <div class="mt-3 flex items-center gap-4 text-xs font-bold text-muted-foreground"><span>♥ 7</span><span>💬 9</span></div>
        </div>
        <div class="rounded-lg border bg-card p-5">
            <div class="mb-2 flex items-center gap-2.5">
                <span class="flex size-7 items-center justify-center rounded-full bg-sidebar-accent text-xs font-black text-white">KH</span>
                <div class="min-w-0 flex-1"><b class="block text-sm">김하늘</b><span class="text-xs text-muted-foreground">어제</span></div>
                <t:badge tone="neutral">질문</t:badge>
            </div>
            <p class="text-sm font-extrabold">MT 회비 언제까지 내면 되나요?</p>
            <p class="mt-1 text-sm text-muted-foreground">이번 주까지인지 다음 주인지 헷갈려서요!</p>
            <div class="mt-3 flex items-center gap-4 text-xs font-bold text-muted-foreground"><span>♥ 2</span><span>💬 3</span></div>
        </div>
        <div class="rounded-lg border bg-card p-5">
            <div class="mb-2 flex items-center gap-2.5">
                <span class="flex size-7 items-center justify-center rounded-full bg-success text-xs font-black text-white">HJ</span>
                <div class="min-w-0 flex-1"><b class="block text-sm">한지우</b><span class="text-xs text-muted-foreground">2일 전</span></div>
                <t:badge tone="neutral">자유</t:badge>
            </div>
            <p class="text-sm font-extrabold">포스터 시안 투표해주세요 🎨</p>
            <p class="mt-1 text-sm text-muted-foreground">A안 vs B안 댓글로 투표 부탁!</p>
            <div class="mt-3 flex items-center gap-4 text-xs font-bold text-muted-foreground"><span>♥ 15</span><span>💬 22</span></div>
        </div>
    </div>

    <t:modal id="postModal" title="글쓰기" description="부원들과 자유롭게 의견을 나눠보세요.">
        <jsp:attribute name="footer">
            <button type="button" data-action="close-modal" class="${btnOutline}">취소</button>
            <button type="button" data-action="close-modal" class="${btnPrimary}">등록</button>
        </jsp:attribute>
        <jsp:body>
            <div class="flex flex-col gap-3">
                <div><label class="${label}" for="poCat">카테고리</label><select class="${input}" id="poCat"><option>자유</option><option>질문</option></select></div>
                <div><label class="${label}" for="poTitle">제목 <span class="text-accent-foreground">*</span></label><input class="${input}" id="poTitle" type="text" placeholder="제목을 입력하세요"></div>
                <div><label class="${label}" for="poBody">내용</label><textarea class="${input} h-auto resize-none py-2.5" id="poBody" rows="4" placeholder="내용을 입력하세요"></textarea></div>
            </div>
        </jsp:body>
    </t:modal>

    <t:modal id="noticePostModal" title="공지 작성" description="공지는 게시판 상단에 고정되고 전체 알림이 발송됩니다.">
        <jsp:attribute name="footer">
            <button type="button" data-action="close-modal" class="${btnOutline}">취소</button>
            <button type="button" data-action="close-modal" class="${btnPrimary}">공지 등록</button>
        </jsp:attribute>
        <jsp:body>
            <div class="flex flex-col gap-3">
                <div><label class="${label}" for="npTitle">공지 제목 <span class="text-accent-foreground">*</span></label><input class="${input}" id="npTitle" type="text" placeholder="예) 정기공연 최종 리허설 안내"></div>
                <div><label class="${label}" for="npBody">내용</label><textarea class="${input} h-auto resize-none py-2.5" id="npBody" rows="4" placeholder="공지 내용을 입력하세요"></textarea></div>
                <label class="flex cursor-pointer items-center gap-2 text-sm font-extrabold"><input type="checkbox" checked class="size-4 accent-primary"> 전체 부원에게 알림 발송</label>
            </div>
        </jsp:body>
    </t:modal>
</t:layout>
