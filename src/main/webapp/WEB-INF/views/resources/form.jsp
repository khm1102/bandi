<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout title="자료 작성" active="resources" role="${role}" scriptPath="resources/form">
    <jsp:attribute name="css">
        <link rel="stylesheet" href="<c:url value='/css/notice/markdown.css'/>">
    </jsp:attribute>
    <jsp:body>
    <div class="mx-auto max-w-5xl">
        <t:pageHead title="자료 작성" description="내용과 파일을 함께 정리해 동아리 멤버에게 공유하세요.">
            <t:button variant="outline" href="/resources">목록으로</t:button>
        </t:pageHead>
    </div>

    <form class="mx-auto max-w-5xl space-y-7" data-resource-form novalidate>
        <section class="space-y-5" aria-labelledby="resourceBodyHeading">
            <div>
                <label class="mb-2 block text-sm font-extrabold" for="resourceTitle">제목</label>
                <input id="resourceTitle" class="h-12 w-full rounded-md border border-input bg-card px-4 text-base placeholder:text-muted-foreground focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20" maxlength="200" required placeholder="자료 제목을 입력하세요" data-resource-title>
            </div>

            <div class="flex flex-wrap items-center justify-between gap-3 border-t pt-6">
                <h2 id="resourceBodyHeading" class="text-base font-extrabold">본문</h2>
                <div class="flex rounded-md border bg-card p-1" role="tablist" aria-label="본문 보기 방식">
                    <button type="button" class="min-h-11 rounded-sm bg-secondary px-3 text-sm font-bold" role="tab" aria-selected="true" aria-controls="resourceEditorPanel" id="resourceWriteTab" data-resource-tab="write">작성</button>
                    <button type="button" class="min-h-11 rounded-sm px-3 text-sm font-bold text-muted-foreground" role="tab" aria-selected="false" aria-controls="resourcePreviewPanel" id="resourcePreviewTab" data-resource-tab="preview">미리보기</button>
                </div>
            </div>

            <div id="resourceEditorPanel" class="mt-3" role="tabpanel" aria-labelledby="resourceWriteTab" data-resource-panel="write">
                <div class="overflow-x-auto rounded-t-md border border-b-0 bg-secondary/50">
                    <div class="flex min-w-max gap-1 p-2" aria-label="Markdown 서식 도구">
                        <button type="button" class="flex min-h-11 min-w-11 items-center justify-center rounded-sm px-2 text-sm font-extrabold hover:bg-card" data-resource-markdown-action="bold" aria-label="굵게" title="굵게">B</button>
                        <button type="button" class="flex min-h-11 min-w-11 items-center justify-center rounded-sm px-2 text-sm italic hover:bg-card" data-resource-markdown-action="italic" aria-label="기울임" title="기울임">I</button>
                        <span class="mx-1 h-7 self-center border-l" aria-hidden="true"></span>
                        <button type="button" class="min-h-11 min-w-11 rounded-sm px-2 text-xs font-bold hover:bg-card" data-resource-markdown-action="heading-one">H1</button>
                        <button type="button" class="min-h-11 min-w-11 rounded-sm px-2 text-xs font-bold hover:bg-card" data-resource-markdown-action="heading-two">H2</button>
                        <button type="button" class="min-h-11 min-w-11 rounded-sm px-2 text-xs font-bold hover:bg-card" data-resource-markdown-action="list">목록</button>
                        <button type="button" class="min-h-11 min-w-11 rounded-sm px-2 text-xs font-bold hover:bg-card" data-resource-markdown-action="quote">인용</button>
                        <button type="button" class="min-h-11 min-w-11 rounded-sm px-2 text-xs font-bold hover:bg-card" data-resource-markdown-action="code">코드</button>
                        <button type="button" class="min-h-11 min-w-11 rounded-sm px-2 text-xs font-bold hover:bg-card" data-resource-markdown-action="link">링크</button>
                        <button type="button" class="min-h-11 min-w-11 rounded-sm px-2 text-xs font-bold hover:bg-card" data-resource-markdown-action="table">표</button>
                        <button type="button" class="min-h-11 min-w-11 rounded-sm px-2 text-xs font-bold hover:bg-card" data-resource-markdown-action="image">이미지</button>
                    </div>
                </div>
                <textarea id="resourceBody" class="min-h-[30rem] w-full rounded-b-md border border-input bg-card px-4 py-4 font-mono text-base leading-6 placeholder:text-muted-foreground focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm" required placeholder="# 자료 제목\n\n자료 내용을 Markdown으로 작성하세요. 단독 HTTPS URL은 저장 후 링크 카드로 보여요." data-resource-body></textarea>
                <p class="mt-2 text-xs leading-5 text-muted-foreground">제목, 목록, 링크, 인용, 코드 블록, 표를 사용할 수 있어요. 이미지 버튼이나 파일 끌어놓기로 본문 이미지를 추가할 수 있고, 단독 HTTPS URL은 저장 후 링크 카드로 보여요. HTML은 지원하지 않아요.</p>
            </div>

            <div id="resourcePreviewPanel" class="mt-3 hidden rounded-md border bg-card p-5" role="tabpanel" aria-labelledby="resourcePreviewTab" data-resource-panel="preview" tabindex="-1">
                <div class="flex items-center justify-between gap-3">
                    <h3 class="text-sm font-extrabold">미리보기</h3>
                    <span class="text-xs text-muted-foreground" aria-live="polite" data-resource-preview-status></span>
                </div>
                <div class="markdown-content mt-5" data-resource-preview><p class="text-muted-foreground">내용을 입력하면 여기에 미리보기가 표시돼요.</p></div>
            </div>

            <div class="border-x border-b px-4 py-3 sm:flex sm:items-center sm:justify-between sm:gap-4">
                <div class="min-w-0">
                    <h3 id="resourceAttachmentsHeading" class="text-sm font-extrabold">첨부 파일</h3>
                    <p class="mt-1 text-xs text-muted-foreground">본문 이미지는 이미지 버튼으로 올리면 자동으로 함께 추가돼요.</p>
                </div>
                <label class="mt-3 inline-flex min-h-11 cursor-pointer items-center justify-center rounded-md border bg-card px-3 text-sm font-bold hover:bg-secondary sm:mt-0">
                    파일 첨부
                    <input class="sr-only" type="file" multiple data-resource-files>
                </label>
                <input class="sr-only" type="file" accept="image/jpeg,image/png,image/webp" data-resource-image-files>
            </div>
            <p class="hidden border-x border-b border-destructive bg-destructive-soft px-4 py-2.5 text-sm text-destructive" role="alert" data-resource-upload-error></p>
            <ul class="space-y-2 border-x border-b px-4 py-3" data-resource-file-list></ul>
        </section>

        <p class="hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-sm text-destructive" role="alert" data-resource-error></p>

        <div class="sticky bottom-0 flex flex-wrap items-center justify-end gap-2 border-t bg-background/95 py-4 backdrop-blur">
            <t:button variant="outline" href="/resources">취소</t:button>
            <t:button type="submit" pageAction="save-resource">자료 저장</t:button>
        </div>
    </form>
    </jsp:body>
</t:layout>
