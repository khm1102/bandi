<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<c:set var="canReview" value="${role == 'admin' || role == 'leader'}"/>
<t:layout title="활동 기록" active="activity" role="${role}" scriptPath="activity/list">
    <t:pageHead title="활동 기록" description="팀 활동을 네이비즘 인증 사진과 함께 제출하고 검수 이력을 관리합니다">
        <t:button pageAction="activity-create-open">+ 활동 기록</t:button>
    </t:pageHead>

    <nav class="mb-5 grid grid-cols-2 rounded-lg border bg-secondary p-1" aria-label="활동 기록 구분">
        <button type="button" class="inline-flex min-h-11 items-center justify-center rounded-md border bg-card px-3 text-sm font-extrabold text-foreground" data-activity-view="manageable" aria-selected="true">작성·검수</button>
        <button type="button" class="inline-flex min-h-11 items-center justify-center rounded-md px-3 text-sm font-extrabold text-muted-foreground" data-activity-view="approved" aria-selected="false">승인된 전체 기록</button>
    </nav>

    <div class="mb-4 flex flex-col gap-3 md:flex-row md:items-end">
        <div class="min-w-0 flex-1">
            <h2 class="text-lg font-black" data-activity-section-title>작성·검수 기록</h2>
            <p class="mt-1 text-xs leading-5 text-muted-foreground" data-activity-section-description>내가 작성했거나 현재 권한으로 관리할 수 있는 기록입니다.</p>
        </div>
        <div class="w-full md:w-56">
            <label class="${label}" for="activityTeamFilter">팀 필터</label>
            <select class="${input}" id="activityTeamFilter"><option value="">전체 팀</option></select>
        </div>
    </div>

    <div class="mb-4 flex flex-wrap gap-2" data-activity-status-filters>
        <t:filterChip group="activity-status" value="ALL" label="전체" active="true"/>
        <t:filterChip group="activity-status" value="DRAFT" label="작성 중"/>
        <t:filterChip group="activity-status" value="SUBMITTED" label="검수 대기" dot="true"/>
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

    <t:modal id="activityModal" title="활동 기록 작성" description="초안을 저장한 뒤 증빙 사진과 함께 제출합니다.">
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
                    <div><label class="${label}" for="activityDttm">활동 일시 <span class="text-accent-foreground">*</span></label><input class="${input}" id="activityDttm" type="datetime-local"></div>
                    <div><label class="${label}" for="activityParticipantCount">참여 인원 <span class="text-accent-foreground">*</span></label><input class="${input}" id="activityParticipantCount" type="number" min="1" step="1" inputmode="numeric"></div>
                </div>
                <div>
                    <label class="${label}" for="activityBody">활동 내용 <span class="text-accent-foreground">*</span></label>
                    <textarea class="min-h-28 w-full resize-y rounded-md border border-input bg-card px-3 py-2.5 text-sm" id="activityBody" placeholder="진행 내용과 결과를 구체적으로 작성해 주세요."></textarea>
                </div>
                <div>
                    <label class="${label}" for="activityEvidence">네이비즘 인증 사진 <span class="text-accent-foreground" data-evidence-required>(제출 시 필수)</span></label>
                    <input class="block min-h-11 w-full rounded-md border border-input bg-card px-3 py-2 text-sm file:mr-3 file:rounded-md file:border-0 file:bg-secondary file:px-3 file:py-1.5 file:text-xs file:font-bold" id="activityEvidence" type="file" accept="image/*" aria-describedby="activityEvidenceHelp">
                    <p id="activityEvidenceHelp" class="mt-1.5 text-xs leading-5 text-muted-foreground">참여 인물과 네이비즘 시각 화면이 한 사진에 함께 보여야 합니다. 수정 시 새 파일을 선택하면 추가 증빙으로 등록합니다.</p>
                </div>
                <div>
                    <label class="${label}" for="activityAdditional">추가 활동 사진</label>
                    <input class="block min-h-11 w-full rounded-md border border-input bg-card px-3 py-2 text-sm file:mr-3 file:rounded-md file:border-0 file:bg-secondary file:px-3 file:py-1.5 file:text-xs file:font-bold" id="activityAdditional" type="file" accept="image/*" multiple>
                </div>
                <div class="hidden" data-activity-change-reason-field>
                    <label class="${label}" for="activityChangeReason">재제출 사유</label>
                    <input class="${input}" id="activityChangeReason" type="text" maxlength="500" placeholder="보완한 내용을 간단히 적어 주세요.">
                </div>
                <p class="rounded-md bg-secondary px-3 py-2.5 text-xs leading-5 text-muted-foreground">사진 내용은 자동 판정하지 않습니다. 팀장 또는 운영진이 참여자와 네이비즘 시각을 직접 확인합니다.</p>
                <p class="hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-xs text-destructive" data-activity-form-error role="alert"></p>
            </div>
        </jsp:body>
    </t:modal>

    <t:modal id="activityDetailModal" title="활동 기록 상세">
        <jsp:attribute name="footer">
            <t:button variant="outline" action="close-modal">닫기</t:button>
            <span class="hidden" data-detail-action="edit"><t:button variant="outline" pageAction="activity-edit-open">수정·사진 추가</t:button></span>
            <span class="hidden" data-detail-action="submit"><t:button pageAction="activity-submit" confirm="이 활동 기록을 검수 대상으로 제출할까요?" confirmAction="검수 제출">제출</t:button></span>
            <c:if test="${canReview}">
                <span class="hidden" data-detail-action="revision"><t:button variant="outline" pageAction="activity-revision-open">보완 요청</t:button></span>
                <span class="hidden" data-detail-action="approve"><t:button pageAction="activity-approve" confirm="증빙 사진과 내용을 확인하고 승인할까요?" confirmAction="활동 승인">승인</t:button></span>
                <span class="hidden" data-detail-action="archive"><t:button variant="outline" pageAction="activity-archive" confirm="이 활동 기록을 보관할까요?" confirmAction="활동 보관" cssClass="text-destructive">보관</t:button></span>
            </c:if>
        </jsp:attribute>
        <jsp:body>
            <div class="flex flex-wrap gap-2" data-activity-detail-badges></div>
            <h3 class="mt-4 text-xl font-black" data-activity-detail-title></h3>
            <p class="mt-2 text-xs text-muted-foreground" data-activity-detail-meta></p>
            <p class="mt-4 whitespace-pre-wrap text-sm leading-6" data-activity-detail-body></p>

            <section class="mt-5 border-t pt-4">
                <div class="flex items-center gap-2"><h4 class="text-sm font-extrabold">증빙 사진</h4><span class="text-xs text-muted-foreground" data-activity-file-count></span></div>
                <div class="mt-3 grid grid-cols-1 gap-3 sm:grid-cols-2" data-activity-detail-files></div>
                <p class="mt-3 hidden text-xs text-muted-foreground" data-activity-file-empty>등록된 현재 사진이 없습니다.</p>
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

    <t:modal id="activityRevisionModal" title="활동 기록 보완 요청" description="작성자가 무엇을 다시 확인해야 하는지 구체적으로 남겨 주세요.">
        <jsp:attribute name="footer"><t:button variant="outline" action="close-modal">취소</t:button><t:button pageAction="activity-revision">보완 요청</t:button></jsp:attribute>
        <jsp:body>
            <label class="${label}" for="activityRevisionComment">보완 의견 <span class="text-accent-foreground">*</span></label>
            <textarea class="min-h-28 w-full resize-y rounded-md border border-input bg-card px-3 py-2.5 text-sm" id="activityRevisionComment" maxlength="1000" placeholder="예) 네이비즘 시각과 참여 인원이 한 화면에 보이도록 다시 촬영해 주세요."></textarea>
            <p class="mt-3 hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-xs text-destructive" data-activity-revision-error role="alert"></p>
        </jsp:body>
    </t:modal>
</t:layout>
