<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<t:layout title="내 활동 기록" active="activity" role="${role}" scriptPath="activity/list">
    <jsp:attribute name="css">
        <link rel="stylesheet" href="<c:url value='/css/vendor/vanilla-calendar-pro/3.1.0/layout.css'/>">
        <link rel="stylesheet" href="<c:url value='/css/vendor/vanilla-calendar-pro/3.1.0/light.css'/>">
        <link rel="stylesheet" href="<c:url value='/css/vendor/vanilla-calendar-pro/3.1.0/bandi-adapter.css'/>">
    </jsp:attribute>
    <jsp:attribute name="script">
        <script src="<c:url value='/js/vendor/vanilla-calendar-pro/3.1.0/vanilla-calendar-pro.js'/>"></script>
    </jsp:attribute>
    <jsp:body>
    <t:pageHead title="내 활동 기록" description="작성한 활동 기록의 검수 단계와 보완 요청을 확인합니다">
        <t:button openModal="activityChoiceModal">+ 활동 기록</t:button>
    </t:pageHead>

    <div class="mb-4">
        <h2 class="text-lg font-black">내가 작성한 기록</h2>
        <p class="mt-1 text-xs leading-5 text-muted-foreground">초안부터 최종 승인까지 현재 처리 단계를 확인할 수 있어요.</p>
    </div>

    <div class="mb-4 flex flex-wrap gap-2" data-activity-status-filters>
        <t:filterChip group="activity-status" value="ALL" label="전체" active="true"/>
        <t:filterChip group="activity-status" value="DRAFT" label="작성 중"/>
        <t:filterChip group="activity-status" value="SUBMITTED" label="검수 대기" dot="true"/>
        <t:filterChip group="activity-status" value="TEAM_APPROVED" label="팀장 승인"/>
        <t:filterChip group="activity-status" value="REVISION_REQUESTED" label="보완 요청"/>
        <t:filterChip group="activity-status" value="APPROVED" label="승인"/>
        <t:filterChip group="activity-status" value="ARCHIVED" label="보관"/>
    </div>
    <div class="rounded-lg border bg-card px-5 py-11 text-center" data-activity-state role="status" aria-live="polite">
        <b class="block text-sm font-extrabold" data-activity-state-title>활동 기록을 불러오는 중입니다</b>
        <p class="mt-1 text-xs text-muted-foreground" data-activity-state-message>잠시만 기다려 주세요.</p>
        <button type="button" class="mx-auto mt-4 hidden min-h-11 rounded-md border bg-card px-4 text-xs font-bold" data-page-action="activity-retry" data-activity-retry>다시 시도</button>
    </div>
    <div class="hidden grid-cols-1 gap-4 lg:grid-cols-2" data-activity-list></div>
    <t:pagination id="activityPagination" label="내 활동 기록 페이지"/>

    <template data-activity-card-template>
        <article class="overflow-hidden rounded-lg border bg-card" data-activity-card>
            <div class="flex flex-col sm:flex-row">
                <div class="relative flex h-44 w-full shrink-0 items-center justify-center overflow-hidden border-b bg-secondary sm:h-auto sm:w-48 sm:border-b-0 sm:border-r">
                    <img class="hidden size-full object-cover" data-activity-image alt="">
                    <span class="px-4 text-center text-xs font-bold text-muted-foreground" data-activity-image-fallback>인증 사진 없음</span>
                </div>
                <div class="min-w-0 flex-1 p-5">
                    <div class="flex flex-wrap items-center gap-2" data-activity-badges></div>
                    <h3 class="mt-3 text-base font-black" data-activity-title></h3>
                    <p class="mt-2 text-xs leading-5 text-muted-foreground" data-activity-meta></p>
                    <p class="mt-3 text-xs text-muted-foreground" data-activity-author></p>
                    <t:button variant="outline" size="compact" pageAction="activity-detail-open" cssClass="mt-4 w-full sm:w-auto">상세 보기</t:button>
                </div>
            </div>
        </article>
    </template>

    <t:modal id="activityChoiceModal" title="활동 기록 작성" description="작성 방식에 따라 필요한 정보와 검수 기준이 달라요.">
        <jsp:body>
            <div class="grid gap-3 sm:grid-cols-2">
                <button type="button" class="rounded-lg border bg-card p-4 text-left transition-colors hover:bg-secondary focus-visible:ring-2 focus-visible:ring-ring" data-page-action="activity-create-open">
                    <b class="block text-sm">간단 활동 기록</b>
                    <span class="mt-1 block text-xs leading-5 text-muted-foreground">제목·일시·참여 인원·내용과 선택 첨부를 기록해요.</span>
                </button>
                <a href="<c:url value='/activity-documents'/>" class="rounded-lg border bg-card p-4 text-left transition-colors hover:bg-secondary focus-visible:ring-2 focus-visible:ring-ring">
                    <b class="block text-sm">한글 내역서 만들기</b>
                    <span class="mt-1 block text-xs leading-5 text-muted-foreground">네이비즘 사진을 포함한 HWPX 내역서를 만들어요.</span>
                </a>
            </div>
        </jsp:body>
    </t:modal>

    <t:modal id="activityModal" title="간단 활동 기록" description="사진은 선택 사항이며, 제출 후 팀장과 관리자가 순서대로 검수합니다.">
        <jsp:attribute name="footer">
            <t:button variant="outline" action="close-modal">취소</t:button>
            <t:button variant="outline" pageAction="activity-save">초안 저장</t:button>
            <t:button pageAction="activity-save-submit">저장·제출</t:button>
        </jsp:attribute>
        <jsp:body>
            <div class="flex flex-col gap-3">
                <div data-activity-team-field>
                    <label class="${label}" for="activityTeam">담당 팀 <span class="text-accent-foreground">*</span></label>
                    <select class="${input}" id="activityTeam"><option value="">팀을 선택해 주세요</option></select>
                </div>
                <div>
                    <label class="${label}" for="activityTitle">활동 제목 <span class="text-accent-foreground">*</span></label>
                    <input class="${input}" id="activityTitle" type="text" maxlength="150" placeholder="예) 2막 전체 런스루">
                </div>
                <div class="grid grid-cols-1 gap-3 md:grid-cols-2">
                    <t:dateTimeField id="activityDttm" label="활동 일시" required="true" minuteStep="5"/>
                    <div><label class="${label}" for="activityParticipantCount">참여 인원 <span class="text-accent-foreground">*</span></label><input class="${input}" id="activityParticipantCount" type="number" min="1" step="1" inputmode="numeric"></div>
                </div>
                <div>
                    <label class="${label}" for="activityBody">활동 내용 <span class="text-accent-foreground">*</span></label>
                    <textarea class="min-h-28 w-full resize-y rounded-md border border-input bg-card px-3 py-2.5 text-sm" id="activityBody" placeholder="진행 내용과 결과를 구체적으로 작성해 주세요."></textarea>
                </div>
                <div>
                    <label class="${label}" for="activityEvidence">활동 사진 <span class="text-muted-foreground">(선택)</span></label>
                    <input class="block min-h-11 w-full rounded-md border border-input bg-card px-3 py-2 text-sm file:mr-3 file:rounded-md file:border-0 file:bg-secondary file:px-3 file:py-1.5 file:text-xs file:font-bold" id="activityEvidence" type="file" accept="image/*" aria-describedby="activityEvidenceHelp">
                    <p id="activityEvidenceHelp" class="mt-1.5 text-xs leading-5 text-muted-foreground">사진이 있으면 기록 카드와 검수 화면에서 함께 확인할 수 있어요.</p>
                </div>
                <div>
                    <label class="${label}" for="activityAdditional">추가 활동 사진</label>
                    <input class="block min-h-11 w-full rounded-md border border-input bg-card px-3 py-2 text-sm file:mr-3 file:rounded-md file:border-0 file:bg-secondary file:px-3 file:py-1.5 file:text-xs file:font-bold" id="activityAdditional" type="file" accept="image/*" multiple>
                </div>
                <div class="hidden" data-activity-change-reason-field>
                    <label class="${label}" for="activityChangeReason">재제출 사유</label>
                    <input class="${input}" id="activityChangeReason" type="text" maxlength="500" placeholder="보완한 내용을 간단히 적어 주세요.">
                </div>
                <p class="rounded-md bg-secondary px-3 py-2.5 text-xs leading-5 text-muted-foreground">간단 기록은 사진 없이도 제출할 수 있어요. 한글 내역서는 별도 작성 화면에서 네이비즘 인증 사진이 필요합니다.</p>
                <p class="hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-xs text-destructive" data-activity-form-error role="alert"></p>
            </div>
        </jsp:body>
    </t:modal>

    <t:modal id="activityDetailModal" title="활동 기록 상세">
        <jsp:attribute name="footer">
            <t:button variant="outline" action="close-modal">닫기</t:button>
            <span class="hidden" data-detail-action="edit"><t:button variant="outline" pageAction="activity-edit-open">수정·사진 추가</t:button></span>
            <span class="hidden" data-detail-action="submit"><t:button pageAction="activity-submit" confirm="이 활동 기록을 검수 대상으로 제출할까요?" confirmAction="검수 제출">제출</t:button></span>
        </jsp:attribute>
        <jsp:body>
            <div class="flex flex-wrap gap-2" data-activity-detail-badges></div>
            <h3 class="mt-4 text-xl font-black" data-activity-detail-title></h3>
            <p class="mt-2 text-xs text-muted-foreground" data-activity-detail-meta></p>
            <p class="mt-4 whitespace-pre-wrap text-sm leading-6" data-activity-detail-body></p>

            <section class="mt-5 border-t pt-4">
                <div class="flex items-center gap-2"><h4 class="text-sm font-extrabold">증빙 및 문서</h4><span class="text-xs text-muted-foreground" data-activity-file-count></span></div>
                <div class="mt-3 grid grid-cols-1 gap-3 sm:grid-cols-2" data-activity-detail-files></div>
                <p class="mt-3 hidden text-xs text-muted-foreground" data-activity-file-empty>등록된 증빙이나 문서가 없습니다.</p>
            </section>

            <section class="mt-5 hidden border-t pt-4" data-activity-review-section>
                <h4 class="text-sm font-extrabold">검수 이력</h4>
                <div class="mt-3 flex flex-col gap-2" data-activity-review-history></div>
            </section>
            <section class="mt-5 hidden border-t pt-4" data-activity-revision-section>
                <h4 class="text-sm font-extrabold">제출 리비전</h4>
                <div class="mt-3 flex flex-col gap-2" data-activity-revisions></div>
            </section>
            <p class="mt-3 hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-xs text-destructive" data-activity-detail-error role="alert"></p>
        </jsp:body>
    </t:modal>

    <template data-activity-file-template>
        <figure class="overflow-hidden rounded-md border bg-card" data-activity-file>
            <div class="relative flex h-40 items-center justify-center overflow-hidden bg-secondary">
                <img class="size-full object-cover" data-activity-file-image alt="">
            </div>
            <figcaption class="p-3">
                <div class="flex flex-wrap items-center gap-2"><span data-activity-file-role></span><b class="min-w-0 flex-1 truncate text-xs" data-activity-file-name></b></div>
                <p class="mt-1 text-xs text-muted-foreground" data-activity-file-meta></p>
                <span class="mt-2 hidden" data-file-replace-action><t:button variant="outline" size="compact" pageAction="activity-file-replace">사진 교체</t:button></span>
            </figcaption>
        </figure>
    </template>
    <input type="file" class="hidden" id="activityReplacementFile" accept="image/*">

    </jsp:body>
</t:layout>
