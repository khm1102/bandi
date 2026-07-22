<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<t:layout title="공시 작성" active="notice-management" role="${role}" scriptPath="notice/management-editor">
    <div data-notice-editor data-notice-id="<c:out value='${publicNoticeId}'/>">
        <t:pageHead title="${empty publicNoticeId ? '공시 작성' : '공시 수정'}" description="외부 공개 전 제목, 본문, 첨부와 게시 범위를 확인합니다">
            <t:button href="/notice-management" variant="outline">목록으로</t:button><t:button pageAction="notice-draft-save">초안 저장</t:button>
        </t:pageHead>
        <form data-notice-editor-form class="grid gap-5 lg:grid-cols-[1fr_18rem]">
            <section class="rounded-lg border bg-card p-5">
                <div><label class="${label}" for="noticeTitle">제목 *</label><input id="noticeTitle" class="${input}" maxlength="200" required autocomplete="off"></div>
                <div class="mt-4"><label class="${label}" for="noticeBody">본문 *</label><textarea id="noticeBody" class="min-h-[28rem] w-full resize-y rounded-md border border-input bg-card px-3 py-3 text-sm leading-7 focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20" required></textarea></div>
            </section>
            <aside class="flex flex-col gap-4">
                <section class="rounded-lg border bg-card p-4"><label class="${label}" for="noticeCategory">분류 *</label><select id="noticeCategory" class="${input}" required><option value="RECRUITMENT">모집</option><option value="GENERAL">일반</option></select><label class="mt-4 flex min-h-11 items-center gap-3 text-sm font-bold"><input id="noticePinned" type="checkbox" class="size-4 accent-primary">중요 공시로 표시</label></section>
                <section class="rounded-lg border bg-card p-4"><label class="${label}" for="noticeFiles">첨부파일</label><input id="noticeFiles" type="file" multiple class="block w-full text-xs"><p class="mt-2 text-xs leading-5 text-muted-foreground">저장할 때 공개용 파일이 별도로 생성됩니다.</p><ul data-notice-attachments class="mt-3 flex flex-col gap-2"></ul></section>
                <p data-notice-editor-error class="hidden rounded-md bg-destructive-soft px-3 py-2 text-xs text-destructive" role="alert"></p>
            </aside>
        </form>
    </div>
</t:layout>
