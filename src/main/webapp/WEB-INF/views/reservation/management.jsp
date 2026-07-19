<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<t:layout title="신청 관리" active="reservations" role="${role}" scriptPath="reservation/management">
    <t:pageHead title="공연 신청 관리" description="회차별 관람 신청, 좌석과 취소 상태를 확인합니다">
        <t:button variant="outline" pageAction="reservation-export">명단 내보내기</t:button>
    </t:pageHead>

    <section class="mb-4 grid gap-4 rounded-lg border bg-card p-5 md:grid-cols-2" aria-label="신청 조회 기준">
        <div>
            <label class="${label}" for="reservationProject">공연 프로젝트</label>
            <select class="${input}" id="reservationProject"></select>
        </div>
        <div>
            <label class="${label}" for="reservationRound">공연 회차</label>
            <select class="${input}" id="reservationRound"></select>
        </div>
    </section>

    <section class="mb-4 grid grid-cols-2 gap-4 lg:grid-cols-4" aria-label="관람 신청 지표">
        <t:statCard label="신청 건수" value="0" unit="건" valueHook="reservation-count"/>
        <t:statCard label="신청 좌석" value="0" unit="석" valueHook="reserved-seat-count"/>
        <t:statCard label="입장 좌석" value="0" unit="석" tone="success" valueHook="checked-seat-count"/>
        <t:statCard label="입장률" value="0" unit="%" tone="success" valueHook="entry-rate"/>
    </section>

    <div class="mb-4 flex flex-wrap gap-2" aria-label="신청 상태 필터">
        <t:filterChip group="reservation-status" value="ALL" label="전체" active="true"/>
        <t:filterChip group="reservation-status" value="CONFIRMED" label="확정"/>
        <t:filterChip group="reservation-status" value="PARTIALLY_CANCELLED" label="일부 취소"/>
        <t:filterChip group="reservation-status" value="CANCELLED" label="취소"/>
    </div>

    <div class="rounded-lg border bg-card" data-reservation-region aria-busy="true">
        <t:dataTable caption="공연 관람 신청 명단">
            <thead>
            <tr>
                <th>신청번호</th>
                <th>관람객</th>
                <th>연락처</th>
                <th>좌석</th>
                <th>상태</th>
                <th><span class="sr-only">작업</span></th>
            </tr>
            </thead>
            <tbody data-reservation-list>
            <tr><td colspan="6" class="py-11 text-center text-muted-foreground">신청 명단을 불러오는 중입니다.</td></tr>
            </tbody>
        </t:dataTable>
    </div>

    <t:modal id="reservationCancelModal" title="관람 신청 취소" description="취소 사유는 운영 기록에 남습니다.">
        <jsp:attribute name="footer">
            <t:button variant="outline" action="close-modal">닫기</t:button>
            <t:button variant="danger" pageAction="reservation-cancel-save">신청 취소</t:button>
        </jsp:attribute>
        <jsp:body>
            <form data-cancel-form class="flex flex-col gap-4">
                <input id="cancelReservationId" type="hidden">
                <div>
                    <span class="${label}">취소 대상</span>
                    <strong data-cancel-reservation class="block text-sm"></strong>
                </div>
                <div>
                    <label class="${label}" for="cancelReason">취소 사유 *</label>
                    <textarea class="${input} min-h-24 py-3" id="cancelReason" required maxlength="500"></textarea>
                </div>
            </form>
        </jsp:body>
    </t:modal>
</t:layout>
