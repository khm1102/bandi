<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="btnPrimary" value="inline-flex h-9 items-center justify-center gap-1.5 rounded-md bg-primary px-4 text-sm font-bold text-primary-foreground transition-colors hover:bg-primary-strong hover:text-white"/>
<c:set var="btnOutline" value="inline-flex h-9 items-center justify-center gap-1.5 rounded-md border bg-card px-4 text-sm font-bold transition-colors hover:bg-secondary"/>
<c:set var="btnSm" value="inline-flex h-8 items-center rounded-md border bg-card px-3 text-xs font-bold transition-colors hover:bg-secondary"/>
<c:set var="chip" value="inline-flex h-8 items-center gap-1.5 rounded-md border bg-card px-3 text-xs font-bold text-muted-foreground transition-colors hover:border-sidebar-muted"/>
<c:set var="chipOn" value="inline-flex h-8 items-center gap-1.5 rounded-md border border-sidebar bg-sidebar px-3 text-xs font-bold text-white"/>
<c:set var="th" value="whitespace-nowrap bg-secondary px-4 py-3 text-left text-xs font-extrabold text-muted-foreground"/>
<c:set var="td" value="px-4 py-3 text-sm"/>
<c:set var="input" value="h-10 w-full rounded-md border border-input bg-card px-3 text-sm transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<c:set var="canEdit" value="${role != 'member'}"/>
<t:layout title="소품" active="props" role="${role}">
    <t:pageHead title="소품" description="모든 부원이 소품·장비의 재고와 보관 위치를 검색하고 조회할 수 있습니다">
        <c:if test="${canEdit}">
            <button type="button" data-open-modal="propModal" class="${btnPrimary}">+ 품목 등록</button>
        </c:if>
    </t:pageHead>

    <div class="mb-4 rounded-lg border bg-card p-5">
        <input class="h-11 w-full rounded-md border-2 border-primary bg-card px-4 text-sm transition-colors focus:outline-none focus:ring-2 focus:ring-ring/20" type="search" placeholder="품목명, 위치, 분류 검색">
    </div>

    <div class="mb-4 grid grid-cols-2 gap-4 lg:grid-cols-4">
        <t:statCard label="전체 품목" value="6" unit="종"/>
        <t:statCard label="사용중" value="24" tone="danger"/>
        <t:statCard label="보관중" value="15" tone="success"/>
        <t:statCard label="반납 대기" value="3" unit="건"/>
    </div>

    <div class="mb-4 flex flex-wrap gap-2">
        <button type="button" class="${chipOn}">전체</button>
        <button type="button" class="${chip}">소품</button>
        <button type="button" class="${chip}">의상</button>
        <button type="button" class="${chip}">조명장비</button>
        <button type="button" class="${chip}">음향장비</button>
    </div>

    <div class="overflow-x-auto rounded-lg border bg-card">
        <table class="w-full border-collapse text-left">
            <thead><tr><th class="${th}">품목명</th><th class="${th}">분류</th><th class="${th}">총수량</th><th class="${th}">사용중</th><th class="${th}">보관 위치</th><th class="${th}">상태</th><c:if test="${canEdit}"><th class="${th}"></th></c:if></tr></thead>
            <tbody class="divide-y">
            <tr class="transition-colors hover:bg-secondary/50"><td class="${td} font-bold">앤티크 회중시계</td><td class="${td}"><t:badge tone="neutral">소품</t:badge></td><td class="${td}">2</td><td class="${td} font-bold text-warning">1</td><td class="${td}">소품창고 A-3</td><td class="${td}"><t:badge tone="success" dot="true">정상</t:badge></td><c:if test="${canEdit}"><td class="${td} text-right"><button type="button" class="${btnSm}">수정</button> <button type="button" data-confirm="이 품목을 삭제할까요?" class="${btnSm} text-destructive">삭제</button></td></c:if></tr>
            <tr class="transition-colors hover:bg-secondary/50"><td class="${td} font-bold">무선 핀마이크</td><td class="${td}"><t:badge tone="neutral">음향장비</t:badge></td><td class="${td}">8</td><td class="${td} font-bold text-warning">6</td><td class="${td}">조정실 캐비닛</td><td class="${td}"><t:badge tone="success" dot="true">정상</t:badge></td><c:if test="${canEdit}"><td class="${td} text-right"><button type="button" class="${btnSm}">수정</button> <button type="button" data-confirm="이 품목을 삭제할까요?" class="${btnSm} text-destructive">삭제</button></td></c:if></tr>
            <tr class="transition-colors hover:bg-secondary/50"><td class="${td} font-bold">LED 파라이트</td><td class="${td}"><t:badge tone="neutral">조명장비</t:badge></td><td class="${td}">12</td><td class="${td} font-bold text-warning">12</td><td class="${td}">무대 상단 그리드</td><td class="${td}"><t:badge tone="warning" dot="true">사용중</t:badge></td><c:if test="${canEdit}"><td class="${td} text-right"><button type="button" class="${btnSm}">수정</button> <button type="button" data-confirm="이 품목을 삭제할까요?" class="${btnSm} text-destructive">삭제</button></td></c:if></tr>
            <tr class="transition-colors hover:bg-secondary/50"><td class="${td} font-bold">빅토리안 드레스</td><td class="${td}"><t:badge tone="neutral">의상</t:badge></td><td class="${td}">3</td><td class="${td} font-bold text-warning">0</td><td class="${td}">의상실 행거 2</td><td class="${td}"><t:badge tone="danger" dot="true">수선중</t:badge></td><c:if test="${canEdit}"><td class="${td} text-right"><button type="button" class="${btnSm}">수정</button> <button type="button" data-confirm="이 품목을 삭제할까요?" class="${btnSm} text-destructive">삭제</button></td></c:if></tr>
            <tr class="transition-colors hover:bg-secondary/50"><td class="${td} font-bold">목재 의자 (세트용)</td><td class="${td}"><t:badge tone="neutral">소품</t:badge></td><td class="${td}">10</td><td class="${td} font-bold text-warning">4</td><td class="${td}">소품창고 B-1</td><td class="${td}"><t:badge tone="success" dot="true">정상</t:badge></td><c:if test="${canEdit}"><td class="${td} text-right"><button type="button" class="${btnSm}">수정</button> <button type="button" data-confirm="이 품목을 삭제할까요?" class="${btnSm} text-destructive">삭제</button></td></c:if></tr>
            <tr class="transition-colors hover:bg-secondary/50"><td class="${td} font-bold">스탠드 마이크</td><td class="${td}"><t:badge tone="neutral">음향장비</t:badge></td><td class="${td}">4</td><td class="${td} font-bold text-warning">1</td><td class="${td}">조정실 캐비닛</td><td class="${td}"><t:badge tone="success" dot="true">정상</t:badge></td><c:if test="${canEdit}"><td class="${td} text-right"><button type="button" class="${btnSm}">수정</button> <button type="button" data-confirm="이 품목을 삭제할까요?" class="${btnSm} text-destructive">삭제</button></td></c:if></tr>
            </tbody>
        </table>
    </div>

    <c:if test="${canEdit}">
        <div class="mb-2.5 mt-6 flex items-center gap-2.5">
            <h3 class="text-base font-extrabold">부원에게 빌린 물품</h3>
            <t:badge tone="warning">반납 대기 3</t:badge>
            <button type="button" data-open-modal="borrowModal" class="ml-auto inline-flex h-8 items-center rounded-md bg-primary px-3 text-xs font-bold text-primary-foreground transition-colors hover:bg-primary-strong hover:text-white">+ 빌린 물품 기록</button>
        </div>
        <div class="overflow-x-auto rounded-lg border bg-card">
            <table class="w-full border-collapse text-left">
                <thead><tr><th class="${th}">물품명</th><th class="${th}">주인</th><th class="${th}">소속팀</th><th class="${th}">빌린 날짜</th><th class="${th}">반납 예정</th><th class="${th}">상태</th><th class="${th}"></th></tr></thead>
                <tbody class="divide-y">
                <tr class="transition-colors hover:bg-secondary/50"><td class="${td} font-bold">빈티지 손목시계</td><td class="${td}">김하늘</td><td class="${td}">배우연출팀</td><td class="${td}">06/14</td><td class="${td}">공연 후</td><td class="${td}"><t:badge tone="warning">대기</t:badge></td><td class="${td} text-right"><button type="button" class="inline-flex h-8 items-center rounded-md bg-primary px-3 text-xs font-bold text-primary-foreground transition-colors hover:bg-primary-strong hover:text-white">반납 확인</button></td></tr>
                <tr class="transition-colors hover:bg-secondary/50"><td class="${td} font-bold">체크무늬 담요</td><td class="${td}">한지우</td><td class="${td}">디자인팀</td><td class="${td}">06/16</td><td class="${td}">06/23</td><td class="${td}"><t:badge tone="warning">대기</t:badge></td><td class="${td} text-right"><button type="button" class="inline-flex h-8 items-center rounded-md bg-primary px-3 text-xs font-bold text-primary-foreground transition-colors hover:bg-primary-strong hover:text-white">반납 확인</button></td></tr>
                <tr class="transition-colors hover:bg-secondary/50"><td class="${td} font-bold">블루투스 스피커</td><td class="${td}">정도윤</td><td class="${td}">무대팀</td><td class="${td}">06/12</td><td class="${td}">06/20</td><td class="${td}"><t:badge tone="warning">대기</t:badge></td><td class="${td} text-right"><button type="button" class="inline-flex h-8 items-center rounded-md bg-primary px-3 text-xs font-bold text-primary-foreground transition-colors hover:bg-primary-strong hover:text-white">반납 확인</button></td></tr>
                <tr class="transition-colors hover:bg-secondary/50"><td class="${td} font-bold">빈티지 여행 가방</td><td class="${td}">박서연</td><td class="${td}">오퍼팀</td><td class="${td}">06/09</td><td class="${td}">06/15</td><td class="${td}"><t:badge tone="success">반납 완료</t:badge></td><td class="${td}"></td></tr>
                </tbody>
            </table>
        </div>
    </c:if>

    <t:modal id="propModal" title="품목 등록" description="무대 소품·장비 재고를 추가합니다.">
        <jsp:attribute name="footer">
            <button type="button" data-action="close-modal" class="${btnOutline}">취소</button>
            <button type="button" data-action="close-modal" class="${btnPrimary}">등록</button>
        </jsp:attribute>
        <jsp:body>
            <div class="flex flex-col gap-3">
                <div><label class="${label}" for="ppName">품목명 <span class="text-accent-foreground">*</span></label><input class="${input}" id="ppName" type="text" placeholder="예) 앤티크 촛대"></div>
                <div class="grid grid-cols-2 gap-2.5">
                    <div><label class="${label}" for="ppTotal">총수량</label><input class="${input}" id="ppTotal" type="number" value="1" min="1"></div>
                    <div><label class="${label}" for="ppLoc">보관 위치</label><input class="${input}" id="ppLoc" type="text" placeholder="소품창고 A-4"></div>
                </div>
                <div><label class="${label}" for="ppCat">분류</label><select class="${input}" id="ppCat"><option>소품</option><option>의상</option><option>조명장비</option><option>음향장비</option></select></div>
            </div>
        </jsp:body>
    </t:modal>

    <c:if test="${canEdit}">
    <t:modal id="borrowModal" title="빌린 물품 기록" description="부원에게 빌린 개인 물건을 기록해 분실을 막습니다.">
        <jsp:attribute name="footer">
            <button type="button" data-action="close-modal" class="${btnOutline}">취소</button>
            <button type="button" data-action="close-modal" class="${btnPrimary}">기록 저장</button>
        </jsp:attribute>
        <jsp:body>
            <div class="flex flex-col gap-3">
                <div><label class="${label}" for="bwItem">물품명 <span class="text-accent-foreground">*</span></label><input class="${input}" id="bwItem" type="text" placeholder="예) 빈티지 카메라"></div>
                <div class="grid grid-cols-2 gap-2.5">
                    <div><label class="${label}" for="bwOwner">주인 (누구 것)</label><select class="${input}" id="bwOwner"><option>이서준 (운영진팀)</option><option>정도윤 (무대팀)</option><option>김하늘 (배우연출팀)</option><option>박서연 (오퍼팀)</option><option>한지우 (디자인팀)</option><option>최민준 (영상팀)</option></select></div>
                    <div><label class="${label}" for="bwDue">반납 예정일</label><input class="${input}" id="bwDue" type="text" placeholder="06/23 또는 공연 후"></div>
                </div>
            </div>
        </jsp:body>
    </t:modal>
    </c:if>
</t:layout>
