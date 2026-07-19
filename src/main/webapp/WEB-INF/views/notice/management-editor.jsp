<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-bold text-muted-foreground"/>
<t:layout title="공시 작성" active="notice-management" role="${role}" scriptPath="notice/management-editor">
    <main class="w-full" data-notice-editor data-notice-id="<c:out value='${publicNoticeId}'/>">
        <t:pageHead title="${empty publicNoticeId ? '공시 작성' : '공시 수정'}" description="긴 공식 안내를 작성하고 초안 상태로 안전하게 저장해요"><t:button href="/notice-management" variant="outline" cssClass="" >목록으로</t:button><t:button pageAction="notice-draft-save">초안 저장</t:button></t:pageHead>

        <div class="mb-6 flex items-center gap-2 border-y py-3 text-sm"><span class="size-2 rounded-full bg-warning" data-notice-save-dot></span><b data-notice-save-state>저장되지 않은 변경이 없어요</b><span class="ml-auto text-xs text-muted-foreground" data-notice-save-time></span></div>

        <form data-notice-editor-form class="grid gap-8 lg:grid-cols-[minmax(0,1fr)_18rem]">
            <section class="border-b pb-6">
                <div><label class="${label}" for="noticeTitle">제목 *</label><input id="noticeTitle" class="${input}" maxlength="200" required autocomplete="off" placeholder="외부에서 바로 이해할 수 있는 제목"></div>
                <div class="mt-5"><label class="${label}" for="noticeBody">본문 *</label><textarea id="noticeBody" class="min-h-[30rem] w-full resize-y rounded-md border border-input bg-card px-4 py-4 text-base leading-8 focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20" required placeholder="안내 대상, 일정, 장소와 문의 방법을 구체적으로 작성해 주세요."></textarea><p class="mt-2 text-xs text-muted-foreground">한 문단에는 한 가지 내용만 적고, 일정·장소·신청 방법은 문단을 나눠 주세요.</p></div>
            </section>
            <aside class="flex flex-col gap-6">
                <section class="border-b pb-5"><label class="${label}" for="noticeCategory">공시 분류 *</label><select id="noticeCategory" class="${input}" required><option value="PERFORMANCE">공연</option><option value="RESERVATION">관람</option><option value="RECRUITMENT">모집</option><option value="GENERAL">일반</option></select><label class="mt-4 flex min-h-11 items-center gap-3 text-sm font-bold"><input id="noticePinned" type="checkbox" class="size-4 accent-primary">중요 공시로 표시</label></section>
                <section class="border-b pb-5"><label class="${label}" for="noticeFiles">첨부파일</label><input id="noticeFiles" type="file" multiple class="block min-h-11 w-full text-xs"><p class="mt-2 text-xs leading-5 text-muted-foreground">초안을 저장하면 MinIO 공개 버킷으로 복사돼요. 저장 실패 시 선택 내용은 이 화면에 남아요.</p><div class="mt-3 hidden border-l-4 border-info bg-info-soft px-3 py-2 text-xs" data-notice-selected-files></div><ul data-notice-attachments class="mt-3 flex flex-col gap-2"></ul></section>
                <p data-notice-editor-error class="hidden rounded-md bg-destructive-soft px-3 py-2 text-sm text-destructive" role="alert"></p>
            </aside>
        </form>

        <div class="sticky bottom-0 z-10 -mx-4 mt-8 border-t bg-card/95 px-4 py-3 backdrop-blur md:hidden"><t:button pageAction="notice-draft-save" cssClass="w-full">공시 초안 저장</t:button></div>
    </main>
</t:layout>
