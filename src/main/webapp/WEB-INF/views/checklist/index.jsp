<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ckOn" value="flex size-5 shrink-0 items-center justify-center rounded-md border border-success bg-success text-white"/>
<c:set var="ckOff" value="flex size-5 shrink-0 items-center justify-center rounded-md border bg-card text-white"/>
<c:set var="ckSvg" value="M20 6L9 17l-5-5"/>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<c:set var="canEdit" value="${role != 'member'}"/>
<t:layout title="체크리스트" active="checklist" role="${role}" scriptPath="checklist/index">
    <t:pageHead title="공연 체크리스트" description="공연 직전 팀별 준비 완료 여부를 확인합니다${canEdit ? ' · 팀장·운영진은 항목을 추가할 수 있어요' : ''}">
        <span data-checklist-summary><t:badge tone="warning">전체 준비 50%</t:badge></span>
        <c:if test="${canEdit}">
            <t:button openModal="checkModal">+ 항목 추가</t:button>
        </c:if>
    </t:pageHead>

    <c:if test="${canEdit}">
        <template data-checklist-delete-template><t:button variant="outline" size="compact" confirm="이 체크리스트 항목을 삭제할까요?" confirmAction="항목 삭제" cssClass="ml-auto text-destructive" action="checklist-delete">삭제</t:button></template>
    </c:if>

    <div class="mb-4 rounded-lg border bg-card p-5">
        <div class="mb-2 flex items-center"><b class="text-sm">전체 완료율</b><b class="ml-auto text-sm text-accent-foreground" data-checklist-count>4 / 8</b></div>
        <progress data-checklist-progress class="h-2 w-full overflow-hidden rounded-full accent-primary" value="4" max="8" aria-label="전체 체크리스트 완료율">50%</progress>
    </div>

    <div class="grid gap-3.5 md:grid-cols-2">
        <t:card title="배우연출팀" moreLabel="2/2" flush="true">
            <div data-checklist-list data-checklist-team="배우연출" class="flex flex-col gap-1 p-4">
                <div data-checklist-item data-complete="true" class="flex cursor-pointer items-center gap-3 rounded-md px-2 py-2">
                    <span class="${ckOn}"><svg class="size-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><path d="${ckSvg}"/></svg></span>
                    <span class="flex-1 text-sm font-semibold text-muted-foreground line-through">대본 최종본 숙지</span>
                </div>
                <div data-checklist-item data-complete="true" class="flex cursor-pointer items-center gap-3 rounded-md px-2 py-2">
                    <span class="${ckOn}"><svg class="size-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><path d="${ckSvg}"/></svg></span>
                    <span class="flex-1 text-sm font-semibold text-muted-foreground line-through">분장·의상 컨펌</span>
                </div>
                <c:if test="${canEdit}"><t:button variant="outline" size="compact" openModal="checkModal" cssClass="mt-1 justify-start border-dashed text-muted-foreground">+ 배우연출팀 항목 추가</t:button></c:if>
            </div>
        </t:card>
        <t:card title="무대팀" moreLabel="1/2" flush="true">
            <div data-checklist-list data-checklist-team="무대" class="flex flex-col gap-1 p-4">
                <div data-checklist-item data-complete="true" class="flex cursor-pointer items-center gap-3 rounded-md px-2 py-2">
                    <span class="${ckOn}"><svg class="size-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><path d="${ckSvg}"/></svg></span>
                    <span class="flex-1 text-sm font-semibold text-muted-foreground line-through">세트 설치 완료</span>
                </div>
                <div data-checklist-item data-complete="false" class="flex cursor-pointer items-center gap-3 rounded-md px-2 py-2">
                    <span class="${ckOff}"></span>
                    <span class="flex-1 text-sm font-semibold">전환 리허설</span>
                </div>
                <c:if test="${canEdit}"><t:button variant="outline" size="compact" openModal="checkModal" cssClass="mt-1 justify-start border-dashed text-muted-foreground">+ 무대팀 항목 추가</t:button></c:if>
            </div>
        </t:card>
        <t:card title="오퍼팀" moreLabel="0/2" flush="true">
            <div data-checklist-list data-checklist-team="오퍼" class="flex flex-col gap-1 p-4">
                <div data-checklist-item data-complete="false" class="flex cursor-pointer items-center gap-3 rounded-md px-2 py-2">
                    <span class="${ckOff}"></span>
                    <span class="flex-1 text-sm font-semibold">조명 큐 프로그래밍</span>
                </div>
                <div data-checklist-item data-complete="false" class="flex cursor-pointer items-center gap-3 rounded-md px-2 py-2">
                    <span class="${ckOff}"></span>
                    <span class="flex-1 text-sm font-semibold">음향 사운드체크</span>
                </div>
                <c:if test="${canEdit}"><t:button variant="outline" size="compact" openModal="checkModal" cssClass="mt-1 justify-start border-dashed text-muted-foreground">+ 오퍼팀 항목 추가</t:button></c:if>
            </div>
        </t:card>
        <t:card title="디자인팀" moreLabel="1/1" flush="true">
            <div data-checklist-list data-checklist-team="디자인" class="flex flex-col gap-1 p-4">
                <div data-checklist-item data-complete="true" class="flex cursor-pointer items-center gap-3 rounded-md px-2 py-2">
                    <span class="${ckOn}"><svg class="size-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><path d="${ckSvg}"/></svg></span>
                    <span class="flex-1 text-sm font-semibold text-muted-foreground line-through">팸플릿 인쇄</span>
                </div>
                <c:if test="${canEdit}"><t:button variant="outline" size="compact" openModal="checkModal" cssClass="mt-1 justify-start border-dashed text-muted-foreground">+ 디자인팀 항목 추가</t:button></c:if>
            </div>
        </t:card>
        <t:card title="영상팀" moreLabel="0/1" flush="true">
            <div data-checklist-list data-checklist-team="영상" class="flex flex-col gap-1 p-4">
                <div data-checklist-item data-complete="false" class="flex cursor-pointer items-center gap-3 rounded-md px-2 py-2">
                    <span class="${ckOff}"></span>
                    <span class="flex-1 text-sm font-semibold">기록 카메라 세팅</span>
                </div>
                <c:if test="${canEdit}"><t:button variant="outline" size="compact" openModal="checkModal" cssClass="mt-1 justify-start border-dashed text-muted-foreground">+ 영상팀 항목 추가</t:button></c:if>
            </div>
        </t:card>
    </div>

    <t:modal id="checkModal" title="체크리스트 항목 추가" description="팀장·운영진이 준비 항목을 추가합니다.">
        <jsp:attribute name="footer">
            <t:button variant="outline" action="close-modal">취소</t:button>
            <t:button pageAction="checklist-add">항목 추가</t:button>
        </jsp:attribute>
        <jsp:body>
            <div class="flex flex-col gap-3">
                <div><label class="${label}" for="ckTeam">담당팀</label><select class="${input}" id="ckTeam"><option>배우연출</option><option>무대</option><option>오퍼</option><option>디자인</option><option>영상</option></select></div>
                <div><label class="${label}" for="ckItem">체크 항목 <span class="text-accent-foreground">*</span></label><input class="${input}" id="ckItem" type="text" placeholder="예) 무대 전환 리허설 완료"></div>
            </div>
        </jsp:body>
    </t:modal>
</t:layout>
