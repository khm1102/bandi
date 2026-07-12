<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="rq" value="?role=${role}"/>
<c:set var="btnPrimary" value="inline-flex h-9 items-center justify-center gap-1.5 rounded-md bg-primary px-4 text-sm font-bold text-primary-foreground transition-colors hover:bg-primary-strong hover:text-white"/>
<c:set var="chip" value="inline-flex h-8 items-center gap-1.5 rounded-md border bg-card px-3 text-xs font-bold text-muted-foreground transition-colors hover:border-sidebar-muted"/>
<c:set var="chipOn" value="inline-flex h-8 items-center gap-1.5 rounded-md border border-sidebar bg-sidebar px-3 text-xs font-bold text-white"/>
<c:set var="cell" value="min-h-20 rounded-md border bg-card p-1.5"/>
<c:set var="dn" value="text-xs font-extrabold"/>
<c:set var="ev" value="mt-1 block truncate rounded-sm bg-secondary px-1 py-0.5 text-xs font-bold text-muted-foreground"/>
<c:set var="evHot" value="mt-1 block truncate rounded-sm bg-accent px-1 py-0.5 text-xs font-bold text-accent-foreground"/>
<c:set var="input" value="h-10 w-full rounded-md border border-input bg-card px-3 text-sm transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<t:layout title="통합 캘린더" active="calendar" role="${role}">
    <t:pageHead title="통합 캘린더" description="팀별로 필터해서 어떤 일정이 있는지 한눈에 봅니다">
        <c:if test="${role != 'member'}">
            <button type="button" data-open-modal="calEventModal" class="${btnPrimary}">+ 일정 등록</button>
        </c:if>
    </t:pageHead>

    <div class="mb-4 flex flex-wrap gap-2">
        <button type="button" class="${chipOn}">전체 <span class="opacity-70">10</span></button>
        <button type="button" class="${chip}">배우연출 <span class="opacity-70">3</span></button>
        <button type="button" class="${chip}">무대 <span class="opacity-70">2</span></button>
        <button type="button" class="${chip}">오퍼 <span class="opacity-70">2</span></button>
        <button type="button" class="${chip}">디자인 <span class="opacity-70">2</span></button>
        <button type="button" class="${chip}">영상 <span class="opacity-70">1</span></button>
    </div>

    <t:card>
        <div class="mb-4 flex items-center gap-3">
            <button type="button" class="inline-flex h-8 items-center justify-center rounded-md border bg-card px-3 text-xs font-bold transition-colors hover:bg-secondary">‹</button>
            <b class="text-base font-black">2025년 6월</b>
            <button type="button" class="inline-flex h-8 items-center justify-center rounded-md border bg-card px-3 text-xs font-bold transition-colors hover:bg-secondary">›</button>
            <span class="ml-auto text-xs text-muted-foreground">전체 팀 일정 표시 중</span>
        </div>
        <div class="grid grid-cols-7 gap-1.5">
            <div class="py-1 text-center text-xs font-extrabold text-muted-foreground">일</div>
            <div class="py-1 text-center text-xs font-extrabold text-muted-foreground">월</div>
            <div class="py-1 text-center text-xs font-extrabold text-muted-foreground">화</div>
            <div class="py-1 text-center text-xs font-extrabold text-muted-foreground">수</div>
            <div class="py-1 text-center text-xs font-extrabold text-muted-foreground">목</div>
            <div class="py-1 text-center text-xs font-extrabold text-muted-foreground">금</div>
            <div class="py-1 text-center text-xs font-extrabold text-muted-foreground">토</div>
            <div class="${cell}"><span class="${dn}">1</span></div>
            <div class="${cell}"><span class="${dn}">2</span></div>
            <div class="${cell}"><span class="${dn}">3</span></div>
            <div class="${cell}"><span class="${dn}">4</span></div>
            <div class="${cell}"><span class="${dn}">5</span></div>
            <div class="${cell}"><span class="${dn}">6</span></div>
            <div class="${cell}"><span class="${dn}">7</span></div>
            <div class="${cell}"><span class="${dn}">8</span></div>
            <div class="${cell}"><span class="${dn}">9</span></div>
            <div class="${cell}"><span class="${dn}">10</span></div>
            <div class="${cell}"><span class="${dn}">11</span></div>
            <div class="${cell}"><span class="${dn}">12</span></div>
            <div class="${cell}"><span class="${dn}">13</span></div>
            <div class="${cell}"><span class="${dn}">14</span></div>
            <div class="${cell}"><span class="${dn}">15</span></div>
            <div class="${cell}"><span class="${dn}">16</span><span class="${ev}" title="TIP아트센터">조명 세팅</span></div>
            <div class="${cell}"><span class="${dn}">17</span><span class="${ev}" title="동아리방">대본 리딩</span></div>
            <div class="${cell}"><span class="${dn}">18</span><span class="${ev}" title="동아리방">주간 운영회의</span></div>
            <div class="${cell}"><span class="${dn}">19</span><span class="${ev}" title="의상실">의상 피팅</span></div>
            <div class="min-h-20 rounded-md border border-primary bg-card p-1.5 ring-2 ring-ring/20"><span class="${dn} text-accent-foreground">20</span><span class="${ev}" title="TIP아트센터">최종 리허설</span></div>
            <div class="${cell}"><span class="${dn}">21</span><span class="${evHot}" title="TIP아트센터">정기공연 1일차</span></div>
            <div class="${cell}"><span class="${dn}">22</span><span class="${evHot}" title="TIP아트센터">정기공연 2일차</span></div>
            <div class="${cell}"><span class="${dn}">23</span><span class="${ev}" title="TIP아트센터">기록 촬영</span></div>
            <div class="${cell}"><span class="${dn}">24</span><span class="${ev}" title="소극장">정기연습</span></div>
            <div class="${cell}"><span class="${dn}">25</span></div>
            <div class="${cell}"><span class="${dn}">26</span><span class="${ev}" title="가평">MT</span></div>
            <div class="${cell}"><span class="${dn}">27</span></div>
            <div class="${cell}"><span class="${dn}">28</span></div>
            <div class="${cell}"><span class="${dn}">29</span></div>
            <div class="${cell}"><span class="${dn}">30</span></div>
        </div>
    </t:card>

    <t:modal id="calEventModal" title="일정 등록" description="캘린더에 새 일정을 추가합니다. 팀과 장소를 지정하면 팀별 필터로 볼 수 있어요.">
        <jsp:attribute name="footer">
            <button type="button" data-action="close-modal" class="inline-flex h-9 items-center justify-center rounded-md border bg-card px-4 text-sm font-bold transition-colors hover:bg-secondary">취소</button>
            <button type="button" data-action="close-modal" class="${btnPrimary}">등록</button>
        </jsp:attribute>
        <jsp:body>
            <div class="flex flex-col gap-3">
                <div><label class="${label}" for="ceTitle">일정명 <span class="text-accent-foreground">*</span></label><input class="${input}" id="ceTitle" type="text" placeholder="예) 전체 연습"></div>
                <div class="grid grid-cols-2 gap-2.5">
                    <div><label class="${label}" for="ceDay">날짜 (일)</label><input class="${input}" id="ceDay" type="number" value="25" min="1" max="30"></div>
                    <div><label class="${label}" for="ceTeam">담당팀</label><select class="${input}" id="ceTeam"><option>배우연출</option><option>무대</option><option>오퍼</option><option>디자인</option><option>영상</option></select></div>
                </div>
                <div><label class="${label}" for="ceLoc">장소</label><input class="${input}" id="ceLoc" type="text" placeholder="예) TIP아트센터"></div>
            </div>
        </jsp:body>
    </t:modal>
</t:layout>
