<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<t:layout title="활동 기록" active="activity" role="${role}" scriptPath="activity/list">
    <t:pageHead title="활동 기록" description="팀별 활동을 기록해 불참한 부원도 흐름을 파악합니다">
        <c:if test="${role != 'member'}">
            <t:button openModal="activityModal">+ 활동 기록</t:button>
        </c:if>
    </t:pageHead>

    <div class="flex flex-col gap-3.5" data-activity-list>
        <div class="rounded-lg border bg-card p-5">
            <div class="mb-2 flex items-center gap-2"><t:badge tone="neutral" dot="true">배우연출팀</t:badge><span class="text-xs text-muted-foreground">06/19 · 참여 9명</span><span class="ml-auto"><t:badge tone="info">사진 3</t:badge></span></div>
            <p class="text-sm leading-relaxed">2막 전체 런스루 완료. 엇갈린 사랑 장면 블로킹 수정.</p>
        </div>
        <div class="rounded-lg border bg-card p-5">
            <div class="mb-2 flex items-center gap-2"><t:badge tone="info" dot="true">무대팀</t:badge><span class="text-xs text-muted-foreground">06/18 · 참여 6명</span><span class="ml-auto"><t:badge tone="info">사진 2</t:badge></span></div>
            <p class="text-sm leading-relaxed">숲속 세트 90% 완성, 전환용 이동 카트 제작.</p>
        </div>
        <div class="rounded-lg border bg-card p-5">
            <div class="mb-2 flex items-center gap-2"><t:badge tone="accent" dot="true">오퍼팀</t:badge><span class="text-xs text-muted-foreground">06/17 · 참여 4명</span></div>
            <p class="text-sm leading-relaxed">조명 큐 60% 프로그래밍, 음향 큐시트 작성 중.</p>
        </div>
        <div class="rounded-lg border bg-card p-5">
            <div class="mb-2 flex items-center gap-2"><t:badge tone="warning" dot="true">디자인팀</t:badge><span class="text-xs text-muted-foreground">06/16 · 참여 3명</span><span class="ml-auto"><t:badge tone="info">사진 4</t:badge></span></div>
            <p class="text-sm leading-relaxed">포스터 최종 확정, 팸플릿 내지 인쇄 대기.</p>
        </div>
        <div class="rounded-lg border bg-card p-5">
            <div class="mb-2 flex items-center gap-2"><t:badge tone="neutral" dot="true">영상팀</t:badge><span class="text-xs text-muted-foreground">06/15 · 참여 2명</span></div>
            <p class="text-sm leading-relaxed">예고편 1차 편집, 기록영상 촬영 스케줄 확정.</p>
        </div>
    </div>

    <t:modal id="activityModal" title="활동 기록 작성" description="우리 팀의 오늘 활동을 기록합니다.">
        <jsp:attribute name="footer">
            <t:button variant="outline" action="close-modal">취소</t:button>
            <t:button pageAction="activity-add">기록 저장</t:button>
        </jsp:attribute>
        <jsp:body>
            <div class="flex flex-col gap-3">
                <div><label class="${label}" for="acBody">활동 내용 <span class="text-accent-foreground">*</span></label><textarea class="${input} h-auto resize-none py-2.5" id="acBody" rows="4" placeholder="예) 2막 런스루 진행, 블로킹 수정"></textarea></div>
                <div class="grid grid-cols-2 gap-2.5">
                    <div><label class="${label}" for="acDate">날짜</label><input class="${input}" id="acDate" type="text" placeholder="06/20"></div>
                    <div><label class="${label}" for="acPpl">참여 인원</label><input class="${input}" id="acPpl" type="number" value="6"></div>
                </div>
            </div>
        </jsp:body>
    </t:modal>
</t:layout>
