<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout title="${empty notice ? '공지 작성' : '공지 수정'}" active="notices" role="${role}" scriptPath="notice/form">
    <t:pageHead title="${empty notice ? '공지 작성' : '공지 수정'}" description="동아리 멤버에게 전달할 내용을 작성하세요.">
        <t:button variant="outline" href="/notices">목록으로</t:button>
    </t:pageHead>

    <form id="noticeForm" class="mx-auto max-w-5xl space-y-7" data-notice-form
          data-notice-id="<c:out value='${notice.internalNoticeId}'/>" novalidate>
        <section class="space-y-5" aria-labelledby="noticeBodyHeading">
            <div>
                <label class="mb-2 block text-sm font-extrabold" for="noticeTitle">제목</label>
                <input id="noticeTitle" class="h-12 w-full rounded-md border border-input bg-card px-4 text-base placeholder:text-muted-foreground focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20" maxlength="200" required placeholder="공지 제목을 입력하세요" data-notice-title>
            </div>
            <div class="flex flex-wrap items-center justify-between gap-3">
                <h2 id="noticeBodyHeading" class="text-base font-extrabold">본문</h2>
                <div class="flex rounded-md border bg-card p-1" role="tablist" aria-label="본문 보기 방식">
                    <button type="button" class="min-h-9 rounded-sm bg-secondary px-3 text-sm font-bold" role="tab" aria-selected="true" aria-controls="noticeEditorPanel" id="noticeWriteTab" data-notice-tab="write">작성</button>
                    <button type="button" class="min-h-9 rounded-sm px-3 text-sm font-bold text-muted-foreground" role="tab" aria-selected="false" aria-controls="noticePreviewPanel" id="noticePreviewTab" data-notice-tab="preview">미리보기</button>
                </div>
            </div>

            <div id="noticeEditorPanel" class="mt-3" role="tabpanel" aria-labelledby="noticeWriteTab" data-notice-panel="write">
                <div class="flex flex-wrap gap-1 rounded-t-md border border-b-0 bg-secondary/50 p-2" aria-label="Markdown 서식 도구">
                    <button type="button" class="flex min-h-9 min-w-9 items-center justify-center rounded-sm px-2 text-sm font-extrabold hover:bg-card" data-markdown-action="bold" aria-label="굵게" title="굵게">B</button>
                    <button type="button" class="flex min-h-9 min-w-9 items-center justify-center rounded-sm px-2 text-sm italic hover:bg-card" data-markdown-action="italic" aria-label="기울임" title="기울임">I</button>
                    <span class="mx-1 h-7 self-center border-l" aria-hidden="true"></span>
                    <button type="button" class="min-h-9 rounded-sm px-2 text-xs font-bold hover:bg-card" data-markdown-action="heading-one">H1</button>
                    <button type="button" class="min-h-9 rounded-sm px-2 text-xs font-bold hover:bg-card" data-markdown-action="heading-two">H2</button>
                    <button type="button" class="min-h-9 rounded-sm px-2 text-xs font-bold hover:bg-card" data-markdown-action="list">목록</button>
                    <button type="button" class="min-h-9 rounded-sm px-2 text-xs font-bold hover:bg-card" data-markdown-action="quote">인용</button>
                    <button type="button" class="min-h-9 rounded-sm px-2 text-xs font-bold hover:bg-card" data-markdown-action="code">코드</button>
                    <button type="button" class="min-h-9 rounded-sm px-2 text-xs font-bold hover:bg-card" data-markdown-action="link">링크</button>
                    <button type="button" class="min-h-9 rounded-sm px-2 text-xs font-bold hover:bg-card" data-markdown-action="table">표</button>
                    <button type="button" class="min-h-9 rounded-sm px-2 text-xs font-bold hover:bg-card" data-markdown-action="image">이미지</button>
                </div>
                <textarea id="noticeBody" class="min-h-[30rem] w-full rounded-b-md border border-input bg-card px-4 py-4 font-mono text-sm leading-6 placeholder:text-muted-foreground focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20" required placeholder="# 공지 제목\n\n공지 내용을 Markdown으로 작성하세요." data-notice-body></textarea>
                <p class="mt-2 text-xs leading-5 text-muted-foreground">제목, 목록, 링크, 인용, 코드 블록, 표를 사용할 수 있어요. 외부 이미지와 HTML은 지원하지 않아요.</p>
            </div>

            <div id="noticePreviewPanel" class="mt-3 hidden rounded-md border bg-card p-5" role="tabpanel" aria-labelledby="noticePreviewTab" data-notice-panel="preview" tabindex="-1">
                <div class="flex items-center justify-between gap-3">
                    <h3 class="text-sm font-extrabold">미리보기</h3>
                    <span class="text-xs text-muted-foreground" aria-live="polite" data-preview-status></span>
                </div>
                <div class="prose prose-slate mt-5 max-w-none text-sm leading-7" data-notice-preview><p class="text-muted-foreground">내용을 입력하면 여기에 미리보기가 표시돼요.</p></div>
            </div>

            <div class="border-x border-b px-4 py-3 sm:flex sm:items-center sm:justify-between sm:gap-4">
                <div class="min-w-0">
                    <h3 id="noticeAttachmentsHeading" class="text-sm font-extrabold">첨부 파일</h3>
                    <p class="mt-1 text-xs text-muted-foreground">본문 이미지는 이미지 버튼으로 올리면 자동으로 함께 추가돼요.</p>
                </div>
                <label class="mt-3 inline-flex min-h-10 cursor-pointer items-center justify-center rounded-md border bg-card px-3 text-sm font-bold hover:bg-secondary sm:mt-0">
                    파일 첨부
                    <input class="sr-only" type="file" multiple data-notice-files>
                </label>
                <input class="sr-only" type="file" accept="image/jpeg,image/png,image/webp" data-notice-image-files>
            </div>
            <p class="hidden border-x border-b border-destructive bg-destructive-soft px-4 py-2.5 text-sm text-destructive" role="alert" data-notice-upload-error></p>
            <ul class="space-y-2 border-x border-b px-4 py-3" data-notice-file-list></ul>
        </section>

        <section class="border-y py-5" aria-labelledby="noticePublishSettingsHeading">
            <div class="flex flex-wrap items-end gap-4">
                <div class="min-w-52">
                    <label id="noticePublishSettingsHeading" class="mb-2 block text-sm font-bold" for="noticeTarget">게시 대상</label>
                    <select id="noticeTarget" class="h-11 w-full rounded-md border border-input bg-card px-3 text-sm" data-notice-target>
                        <c:if test="${role == 'admin'}"><option value="ALL">전체 멤버</option></c:if>
                        <option value="TEAM">내 팀</option>
                    </select>
                </div>
                <div class="hidden min-w-52" data-notice-team-wrap>
                    <label class="mb-2 block text-sm font-bold" for="noticeTeam">대상 팀</label>
                    <select id="noticeTeam" class="h-11 w-full rounded-md border border-input bg-card px-3 text-sm" data-notice-team></select>
                </div>
                <details class="min-w-44" data-notice-options>
                    <summary class="flex min-h-11 cursor-pointer list-none items-center justify-between rounded-md border bg-card px-3 text-sm font-bold hover:bg-secondary">
                        게시 옵션
                        <span class="text-muted-foreground" aria-hidden="true">⌄</span>
                    </summary>
                    <div class="mt-3 space-y-4 rounded-md border bg-card p-4">
                        <label class="flex min-h-8 items-center gap-2 text-sm font-bold">
                            <input class="size-4 rounded" type="checkbox" data-notice-important>
                            중요 공지로 표시
                        </label>
                        <label class="flex min-h-8 items-center gap-2 text-sm font-bold">
                            <input class="size-4 rounded" type="checkbox" data-notice-schedule-enabled>
                            예약 게시
                        </label>
                        <div class="hidden" data-notice-schedule-wrap>
                            <label class="mb-2 block text-sm font-bold" for="noticePublishAt">게시 시각</label>
                            <input id="noticePublishAt" type="datetime-local" class="h-11 w-full rounded-md border border-input bg-card px-3 text-sm" data-notice-publish-at>
                            <p class="mt-1.5 text-xs text-muted-foreground">지정한 시각에 공지가 공개돼요.</p>
                        </div>
                    </div>
                </details>
            </div>
        </section>

        <p class="hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-sm text-destructive" role="alert" data-notice-error></p>

        <div class="sticky bottom-0 flex flex-wrap items-center justify-end gap-2 border-t bg-background/95 py-4 backdrop-blur">
            <t:button variant="outline" href="/notices">취소</t:button>
            <t:button type="submit" variant="outline" pageAction="save-draft">초안 저장</t:button>
            <t:button type="submit" pageAction="publish-notice">공지 게시</t:button>
        </div>
    </form>
</t:layout>
