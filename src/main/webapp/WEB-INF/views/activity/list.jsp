<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<t:layout title="활동 기록" active="activity" role="${role}" scriptPath="activity/list">
    <t:pageHead title="활동 기록" description="팀별 활동을 네이비즘 인증 사진과 함께 제출하고 운영진 검수 상태를 확인합니다">
        <t:button openModal="activityModal">+ 활동 기록</t:button>
    </t:pageHead>

    <div class="flex flex-col gap-3.5" data-activity-list>
        <div class="rounded-lg border bg-card p-4 md:p-5">
            <div class="flex flex-col gap-3 md:flex-row md:items-start">
                <div class="flex h-28 w-full shrink-0 items-center justify-center rounded-md border bg-secondary text-xs font-bold text-muted-foreground md:w-40">네이비즘 인증 사진</div>
                <div class="min-w-0 flex-1"><div class="mb-2 flex flex-wrap items-center gap-2"><t:badge tone="neutral" dot="true">배우팀</t:badge><span class="text-xs text-muted-foreground">06/19 · 참여 9명</span><span class="md:ml-auto"><t:badge tone="success">승인</t:badge></span></div><p class="text-sm leading-relaxed">2막 전체 런스루 완료. 엇갈린 사랑 장면 블로킹 수정.</p><p class="mt-3 text-xs text-muted-foreground">인증 사진 1장 · 추가 사진 2장 · 이서준 검수</p></div>
            </div>
        </div>
        <div class="rounded-lg border border-warning bg-warning-soft p-4 md:p-5">
            <div class="flex flex-col gap-3 md:flex-row md:items-start">
                <div class="flex h-28 w-full shrink-0 items-center justify-center rounded-md border bg-card text-xs font-bold text-muted-foreground md:w-40">네이비즘 인증 사진</div>
                <div class="min-w-0 flex-1"><div class="mb-2 flex flex-wrap items-center gap-2"><t:badge tone="info" dot="true">무대팀</t:badge><span class="text-xs text-muted-foreground">06/18 · 참여 6명</span><span class="md:ml-auto"><t:badge tone="warning">보완 요청</t:badge></span></div><p class="text-sm leading-relaxed">숲속 세트 90% 완성, 전환용 이동 카트 제작.</p><p class="mt-3 text-xs font-bold text-warning">네이비즘 시각과 참여 인원이 한 화면에 보이도록 다시 제출해 주세요.</p></div>
            </div>
        </div>
        <div class="rounded-lg border bg-card p-4 md:p-5">
            <div class="flex flex-col gap-3 md:flex-row md:items-start">
                <div class="flex h-28 w-full shrink-0 items-center justify-center rounded-md border bg-secondary text-xs font-bold text-muted-foreground md:w-40">네이비즘 인증 사진</div>
                <div class="min-w-0 flex-1"><div class="mb-2 flex flex-wrap items-center gap-2"><t:badge tone="accent" dot="true">오퍼팀</t:badge><span class="text-xs text-muted-foreground">06/17 · 참여 4명</span><span class="md:ml-auto"><t:badge tone="info">검수 대기</t:badge></span></div><p class="text-sm leading-relaxed">조명 큐 60% 프로그래밍, 음향 큐시트 작성 중.</p><p class="mt-3 text-xs text-muted-foreground">인증 사진 1장 · 박서연 제출</p></div>
            </div>
        </div>
    </div>

    <t:modal id="activityModal" title="활동 기록 제출" description="소속 팀의 활동과 필수 인증 사진을 함께 제출합니다.">
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
                <div><label class="${label}" for="acProof">네이비즘 인증 사진 <span class="text-accent-foreground">*</span></label><input class="${input} h-auto py-2" id="acProof" type="file" accept="image/*" aria-describedby="acProofHelp"><p id="acProofHelp" class="mt-1.5 text-xs leading-5 text-muted-foreground">참여 인물과 네이비즘 시각 화면이 한 사진에 함께 보여야 합니다.</p></div>
            </div>
        </jsp:body>
    </t:modal>
</t:layout>
