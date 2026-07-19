<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<c:set var="canEdit" value="${role != 'member'}"/>
<t:layout title="소품" active="props" role="${role}" scriptPath="props/list">
    <t:pageHead title="소품" description="모든 부원이 소품·장비의 재고와 보관 위치를 검색하고 조회할 수 있습니다">
        <c:if test="${canEdit}">
            <t:button openModal="propModal">+ 품목 등록</t:button>
        </c:if>
    </t:pageHead>

    <div class="mb-4 rounded-lg border bg-card p-5">
        <input data-prop-search class="h-11 w-full rounded-md border-2 border-primary bg-card px-4 text-sm transition-colors focus:outline-none focus:ring-2 focus:ring-ring/20" type="search" placeholder="품목명, 위치, 분류 검색">
    </div>

    <div class="mb-4 grid grid-cols-2 gap-4 lg:grid-cols-4">
        <t:statCard label="전체 품목" value="6" unit="종" valueHook="prop-total"/>
        <t:statCard label="사용중" value="24" tone="danger" valueHook="prop-used"/>
        <t:statCard label="보관중" value="15" tone="success" valueHook="prop-stored"/>
        <t:statCard label="반납 대기" value="3" unit="건" valueHook="borrow-pending"/>
    </div>

    <div class="mb-4 flex flex-wrap gap-2">
        <t:filterChip group="prop" value="전체" label="전체" active="true"/>
        <t:filterChip group="prop" value="소품" label="소품"/>
        <t:filterChip group="prop" value="의상" label="의상"/>
        <t:filterChip group="prop" value="조명장비" label="조명장비"/>
        <t:filterChip group="prop" value="음향장비" label="음향장비"/>
    </div>

    <c:if test="${canEdit}">
        <template data-prop-actions-template><t:button variant="outline" size="compact" pageAction="prop-edit">수정</t:button> <t:button variant="outline" size="compact" pageAction="prop-delete" confirm="이 품목을 목록에서 삭제할까요?" confirmAction="품목 삭제" cssClass="text-destructive">삭제</t:button></template>
        <template data-borrow-return-template><t:button size="compact" pageAction="borrow-return">반납 확인</t:button></template>
    </c:if>

    <div class="rounded-lg border bg-card">
        <t:dataTable caption="소품과 장비 재고 목록">
            <thead><tr><th>품목명</th><th>분류</th><th>총수량</th><th>사용중</th><th>보관 위치</th><th>상태</th><c:if test="${canEdit}"><th></th></c:if></tr></thead>
            <tbody data-prop-list>
            <tr data-prop-row data-category="소품"><td class="font-bold">앤티크 회중시계</td><td><t:badge tone="neutral">소품</t:badge></td><td>2</td><td class="font-bold text-warning">1</td><td>소품창고 A-3</td><td><t:badge tone="success" dot="true">정상</t:badge></td><c:if test="${canEdit}"><td class="text-right"><t:button variant="outline" size="compact" pageAction="prop-edit">수정</t:button> <t:button variant="outline" size="compact" pageAction="prop-delete" confirm="이 품목을 목록에서 삭제할까요?" confirmAction="품목 삭제" cssClass="text-destructive">삭제</t:button></td></c:if></tr>
            <tr data-prop-row data-category="음향장비"><td class="font-bold">무선 핀마이크</td><td><t:badge tone="neutral">음향장비</t:badge></td><td>8</td><td class="font-bold text-warning">6</td><td>조정실 캐비닛</td><td><t:badge tone="success" dot="true">정상</t:badge></td><c:if test="${canEdit}"><td class="text-right"><t:button variant="outline" size="compact" pageAction="prop-edit">수정</t:button> <t:button variant="outline" size="compact" pageAction="prop-delete" confirm="이 품목을 목록에서 삭제할까요?" confirmAction="품목 삭제" cssClass="text-destructive">삭제</t:button></td></c:if></tr>
            <tr data-prop-row data-category="조명장비"><td class="font-bold">LED 파라이트</td><td><t:badge tone="neutral">조명장비</t:badge></td><td>12</td><td class="font-bold text-warning">12</td><td>무대 상단 그리드</td><td><t:badge tone="warning" dot="true">사용중</t:badge></td><c:if test="${canEdit}"><td class="text-right"><t:button variant="outline" size="compact" pageAction="prop-edit">수정</t:button> <t:button variant="outline" size="compact" pageAction="prop-delete" confirm="이 품목을 목록에서 삭제할까요?" confirmAction="품목 삭제" cssClass="text-destructive">삭제</t:button></td></c:if></tr>
            <tr data-prop-row data-category="의상"><td class="font-bold">빅토리안 드레스</td><td><t:badge tone="neutral">의상</t:badge></td><td>3</td><td class="font-bold text-warning">0</td><td>의상실 행거 2</td><td><t:badge tone="danger" dot="true">수선중</t:badge></td><c:if test="${canEdit}"><td class="text-right"><t:button variant="outline" size="compact" pageAction="prop-edit">수정</t:button> <t:button variant="outline" size="compact" pageAction="prop-delete" confirm="이 품목을 목록에서 삭제할까요?" confirmAction="품목 삭제" cssClass="text-destructive">삭제</t:button></td></c:if></tr>
            <tr data-prop-row data-category="소품"><td class="font-bold">목재 의자 (세트용)</td><td><t:badge tone="neutral">소품</t:badge></td><td>10</td><td class="font-bold text-warning">4</td><td>소품창고 B-1</td><td><t:badge tone="success" dot="true">정상</t:badge></td><c:if test="${canEdit}"><td class="text-right"><t:button variant="outline" size="compact" pageAction="prop-edit">수정</t:button> <t:button variant="outline" size="compact" pageAction="prop-delete" confirm="이 품목을 목록에서 삭제할까요?" confirmAction="품목 삭제" cssClass="text-destructive">삭제</t:button></td></c:if></tr>
            <tr data-prop-row data-category="음향장비"><td class="font-bold">스탠드 마이크</td><td><t:badge tone="neutral">음향장비</t:badge></td><td>4</td><td class="font-bold text-warning">1</td><td>조정실 캐비닛</td><td><t:badge tone="success" dot="true">정상</t:badge></td><c:if test="${canEdit}"><td class="text-right"><t:button variant="outline" size="compact" pageAction="prop-edit">수정</t:button> <t:button variant="outline" size="compact" pageAction="prop-delete" confirm="이 품목을 목록에서 삭제할까요?" confirmAction="품목 삭제" cssClass="text-destructive">삭제</t:button></td></c:if></tr>
            </tbody>
        </t:dataTable>
    </div>

    <c:if test="${canEdit}">
        <div class="mb-2.5 mt-6 flex items-center gap-2.5">
            <h3 class="text-base font-extrabold">부원에게 빌린 물품</h3>
            <t:badge tone="warning">반납 대기 <span data-borrow-pending>3</span></t:badge>
            <t:button size="compact" openModal="borrowModal" cssClass="ml-auto">+ 빌린 물품 기록</t:button>
        </div>
        <div class="rounded-lg border bg-card">
            <t:dataTable caption="부원에게 빌린 물품 목록">
                <thead><tr><th>물품명</th><th>주인</th><th>소속팀</th><th>빌린 날짜</th><th>반납 예정</th><th>상태</th><th></th></tr></thead>
                <tbody data-borrow-list>
                <tr><td class="font-bold">빈티지 손목시계</td><td>김하늘</td><td>배우</td><td>06/14</td><td>공연 후</td><td data-borrow-status><t:badge tone="warning">대기</t:badge></td><td class="text-right"><t:button size="compact" pageAction="borrow-return">반납 확인</t:button></td></tr>
                <tr><td class="font-bold">체크무늬 담요</td><td>한지우</td><td>디자인팀</td><td>06/16</td><td>06/23</td><td data-borrow-status><t:badge tone="warning">대기</t:badge></td><td class="text-right"><t:button size="compact" pageAction="borrow-return">반납 확인</t:button></td></tr>
                <tr><td class="font-bold">블루투스 스피커</td><td>정도윤</td><td>무대팀</td><td>06/12</td><td>06/20</td><td data-borrow-status><t:badge tone="warning">대기</t:badge></td><td class="text-right"><t:button size="compact" pageAction="borrow-return">반납 확인</t:button></td></tr>
                <tr><td class="font-bold">빈티지 여행 가방</td><td>박서연</td><td>오퍼팀</td><td>06/09</td><td>06/15</td><td><t:badge tone="success">반납 완료</t:badge></td><td></td></tr>
                </tbody>
            </t:dataTable>
        </div>
    </c:if>

    <t:modal id="propModal" title="품목 등록" description="무대 소품·장비 재고를 추가합니다.">
        <jsp:attribute name="footer">
            <t:button variant="outline" action="close-modal">취소</t:button>
            <t:button pageAction="prop-add">등록</t:button>
        </jsp:attribute>
        <jsp:body>
            <div class="flex flex-col gap-3">
                <div><label class="${label}" for="ppName">품목명 <span class="text-accent-foreground">*</span></label><input class="${input}" id="ppName" type="text" placeholder="예) 앤티크 촛대"></div>
                <div class="grid grid-cols-2 gap-2.5">
                    <div><label class="${label}" for="ppTotal">총수량</label><input class="${input}" id="ppTotal" type="number" value="1" min="1" step="1"></div>
                    <div><label class="${label}" for="ppLoc">보관 위치</label><input class="${input}" id="ppLoc" type="text" placeholder="소품창고 A-4"></div>
                </div>
                <div><label class="${label}" for="ppCat">분류</label><select class="${input}" id="ppCat"><option>소품</option><option>의상</option><option>조명장비</option><option>음향장비</option></select></div>
            </div>
        </jsp:body>
    </t:modal>

    <c:if test="${canEdit}">
    <t:modal id="editPropModal" title="품목 수정" description="수량, 위치, 상태를 수정합니다.">
        <jsp:attribute name="footer">
            <t:button variant="outline" action="close-modal">취소</t:button>
            <t:button pageAction="prop-edit-save">수정 저장</t:button>
        </jsp:attribute>
        <jsp:body>
            <div class="flex flex-col gap-3">
                <div><label class="${label}" for="epName">품목명</label><input class="${input}" id="epName" type="text"></div>
                <div class="grid grid-cols-2 gap-2.5">
                    <div><label class="${label}" for="epTotal">총수량</label><input class="${input}" id="epTotal" type="number" min="0" step="1"></div>
                    <div><label class="${label}" for="epUse">사용중</label><input class="${input}" id="epUse" type="number" min="0" step="1"></div>
                </div>
                <div><label class="${label}" for="epLoc">보관 위치</label><input class="${input}" id="epLoc" type="text"></div>
                <div><label class="${label}" for="epStatus">상태</label><select class="${input}" id="epStatus"><option>정상</option><option>사용중</option><option>수선중</option></select></div>
            </div>
        </jsp:body>
    </t:modal>
    </c:if>

    <c:if test="${canEdit}">
    <t:modal id="borrowModal" title="빌린 물품 기록" description="부원에게 빌린 개인 물건을 기록해 분실을 막습니다.">
        <jsp:attribute name="footer">
            <t:button variant="outline" action="close-modal">취소</t:button>
            <t:button pageAction="borrow-add">기록 저장</t:button>
        </jsp:attribute>
        <jsp:body>
            <div class="flex flex-col gap-3">
                <div><label class="${label}" for="bwItem">물품명 <span class="text-accent-foreground">*</span></label><input class="${input}" id="bwItem" type="text" placeholder="예) 빈티지 카메라"></div>
                <div class="grid grid-cols-2 gap-2.5">
                    <div><label class="${label}" for="bwOwner">주인 (누구 것)</label><select class="${input}" id="bwOwner"><option>이서준 (연출)</option><option>정도윤 (무대팀)</option><option>김하늘 (배우)</option><option>박서연 (오퍼팀)</option><option>한지우 (디자인팀)</option><option>최민준 (영상팀)</option></select></div>
                    <div><label class="${label}" for="bwDue">반납 예정일</label><input class="${input}" id="bwDue" type="text" placeholder="06/23 또는 공연 후"></div>
                </div>
            </div>
        </jsp:body>
    </t:modal>
    </c:if>
</t:layout>
