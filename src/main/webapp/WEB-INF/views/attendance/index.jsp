<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<c:set var="canEdit" value="${role != 'member'}"/>
<t:layout title="출석" active="attendance" role="${role}" scriptPath="attendance/index">
    <t:pageHead title="행사 · 출석 관리" description="회식·MT 등 행사 참가 현황을 실시간으로 확인합니다${canEdit ? ' · 팀장·운영진이 행사를 만들 수 있어요' : ''}">
        <c:if test="${canEdit}">
            <t:button openModal="eventModal">+ 행사 생성</t:button>
        </c:if>
    </t:pageHead>

    <c:if test="${canEdit}">
        <template data-attendance-checkin-template><t:button size="compact" pageAction="attendance-checkin" cssClass="ml-auto">체크인</t:button></template>
    </c:if>

    <div data-event-card>
    <t:card title="정기공연 뒤풀이" moreLabel="06/22 · 홍대 회식장소">
        <div class="mb-3.5 flex items-center">
            <b class="text-sm" data-attendance-count>참가 3명</b>
            <c:choose>
                <c:when test="${role == 'member'}">
                    <t:button size="compact" pageAction="attendance-checkin" cssClass="ml-auto">체크인</t:button>
                </c:when>
                <c:otherwise>
                    <button type="button" data-checked-in="true" class="ml-auto inline-flex h-8 items-center gap-1.5 rounded-md border bg-card px-3 text-xs font-bold">체크인 완료</button>
                </c:otherwise>
            </c:choose>
        </div>
        <div class="flex flex-wrap gap-2" data-attendance-list>
            <span class="inline-flex items-center gap-1.5 rounded-full bg-secondary py-1 pl-1 pr-2.5 text-xs font-bold"><span class="flex size-5 items-center justify-center rounded-full bg-primary text-xs font-black text-primary-foreground">LS</span>이서준</span>
            <span class="inline-flex items-center gap-1.5 rounded-full bg-secondary py-1 pl-1 pr-2.5 text-xs font-bold"><span class="flex size-5 items-center justify-center rounded-full bg-info text-xs font-black text-white">JD</span>정도윤</span>
            <span class="inline-flex items-center gap-1.5 rounded-full bg-secondary py-1 pl-1 pr-2.5 text-xs font-bold"><span class="flex size-5 items-center justify-center rounded-full bg-accent-foreground text-xs font-black text-white">PS</span>박서연</span>
        </div>
    </t:card>
    </div>

    <t:modal id="eventModal" title="행사 생성" description="회식·MT 등 새 행사를 만듭니다.">
        <jsp:attribute name="footer">
            <t:button variant="outline" action="close-modal">취소</t:button>
            <t:button pageAction="event-add">행사 생성</t:button>
        </jsp:attribute>
        <jsp:body>
            <div class="flex flex-col gap-3">
                <div><label class="${label}" for="evName">행사명 <span class="text-accent-foreground">*</span></label><input class="${input}" id="evName" type="text" placeholder="예) 종강 MT"></div>
                <div class="grid grid-cols-2 gap-2.5">
                    <div><label class="${label}" for="evDate">날짜</label><input class="${input}" id="evDate" type="text" placeholder="07/05"></div>
                    <div><label class="${label}" for="evPlace">장소</label><input class="${input}" id="evPlace" type="text" placeholder="가평 펜션"></div>
                </div>
            </div>
        </jsp:body>
    </t:modal>
</t:layout>
