<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<c:set var="canAdmin" value="${role eq 'admin'}"/>
<t:layout title="소품·장비" active="props" role="${role}" scriptPath="props/list">
    <t:pageHead title="소품·장비" description="재고와 보관 위치, 변경 이력을 한곳에서 관리합니다">
        <c:if test="${canAdmin}">
            <t:button openModal="assetModal">품목 등록</t:button>
        </c:if>
    </t:pageHead>

    <section class="mb-4 rounded-lg border bg-card p-5" aria-label="소품·장비 검색">
        <label class="sr-only" for="assetSearch">품목명, 위치, 분류 검색</label>
        <input id="assetSearch" data-asset-search class="h-11 w-full rounded-md border-2 border-primary bg-card px-4 text-base transition-colors focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"
               type="search" placeholder="품목명, 위치, 분류 검색" autocomplete="off">
    </section>

    <section class="mb-4 grid grid-cols-2 gap-4 lg:grid-cols-3" aria-label="소품·장비 현황">
        <t:statCard label="전체 품목" value="0" unit="종" valueHook="asset-total"/>
        <t:statCard label="총 재고" value="0" unit="개" valueHook="asset-quantity"/>
        <t:statCard label="점검 필요" value="0" unit="종" tone="danger" valueHook="asset-attention"/>
    </section>

    <div class="mb-4 flex flex-wrap gap-2" aria-label="품목 상태 필터">
        <t:filterChip group="asset-status" value="ALL" label="전체" active="true"/>
        <t:filterChip group="asset-status" value="AVAILABLE" label="사용 가능"/>
        <t:filterChip group="asset-status" value="IN_USE" label="사용 중"/>
        <t:filterChip group="asset-status" value="REPAIR" label="수리 중"/>
        <t:filterChip group="asset-status" value="LOST" label="분실"/>
    </div>

    <div class="rounded-lg border bg-card" data-asset-region aria-busy="true">
        <t:dataTable caption="소품과 장비 재고 목록">
            <thead>
            <tr>
                <th>품목</th>
                <th>분류</th>
                <th>재고</th>
                <th>보관 위치</th>
                <th>상태</th>
                <th><span class="sr-only">작업</span></th>
            </tr>
            </thead>
            <tbody data-asset-list>
            <tr><td colspan="6" class="py-11 text-center text-muted-foreground">품목을 불러오는 중입니다.</td></tr>
            </tbody>
        </t:dataTable>
    </div>

    <template data-asset-actions-template>
        <div class="flex justify-end gap-2">
            <t:button variant="outline" size="compact" pageAction="asset-detail">상세</t:button>
            <c:if test="${canAdmin}">
                <t:button size="compact" pageAction="asset-edit">수정</t:button>
            </c:if>
        </div>
    </template>

    <c:if test="${canAdmin}">
        <t:modal id="assetModal" title="품목 등록" description="소유 구분과 재고 기준을 함께 기록합니다.">
            <jsp:attribute name="footer">
                <t:button variant="outline" action="close-modal">취소</t:button>
                <t:button pageAction="asset-save">등록</t:button>
            </jsp:attribute>
            <jsp:body>
                <form data-asset-form class="flex flex-col gap-4">
                    <input id="assetId" type="hidden">
                    <div>
                        <label class="${label}" for="assetName">품목명 *</label>
                        <input class="${input}" id="assetName" name="name" required maxlength="100" autocomplete="off">
                    </div>
                    <div class="grid gap-3 md:grid-cols-2">
                        <div>
                            <label class="${label}" for="assetCategory">분류 *</label>
                            <select class="${input}" id="assetCategory" name="categoryCode" required>
                                <option value="PROP">소품</option>
                                <option value="COSTUME">의상</option>
                                <option value="LIGHTING">조명 장비</option>
                                <option value="AUDIO">음향 장비</option>
                                <option value="VIDEO">영상 장비</option>
                                <option value="EQUIPMENT">기타 장비</option>
                            </select>
                        </div>
                        <div>
                            <label class="${label}" for="assetTrackingType">관리 방식 *</label>
                            <select class="${input}" id="assetTrackingType" name="trackingType" required>
                                <option value="QUANTITY">수량 단위</option>
                                <option value="INDIVIDUAL">개별 관리번호</option>
                            </select>
                        </div>
                    </div>
                    <div class="grid gap-3 md:grid-cols-2">
                        <div>
                            <label class="${label}" for="assetQuantity">총수량 *</label>
                            <input class="${input}" id="assetQuantity" name="totalQuantity" type="number" min="1" step="1" value="1" required>
                        </div>
                        <div>
                            <label class="${label}" for="assetLocation">보관 위치 *</label>
                            <input class="${input}" id="assetLocation" name="storageLocation" required maxlength="200" autocomplete="off">
                        </div>
                    </div>
                    <div class="grid gap-3 md:grid-cols-2">
                        <div>
                            <label class="${label}" for="assetOwnerType">소유 구분 *</label>
                            <select class="${input}" id="assetOwnerType" name="ownerType" required>
                                <option value="CLUB">동아리</option>
                                <option value="MEMBER">부원 개인</option>
                                <option value="EXTERNAL">외부 대여</option>
                            </select>
                        </div>
                        <div data-owner-member-field hidden>
                            <label class="${label}" for="assetOwnerMember">소유 부원 *</label>
                            <select class="${input}" id="assetOwnerMember" name="ownerMemberId"></select>
                        </div>
                        <div data-owner-external-field hidden>
                            <label class="${label}" for="assetExternalOwner">외부 소유자 *</label>
                            <input class="${input}" id="assetExternalOwner" name="externalOwnerName" maxlength="100" autocomplete="off">
                        </div>
                    </div>
                    <div>
                        <label class="${label}" for="assetPhoto">품목 사진</label>
                        <input class="block min-h-11 w-full rounded-md border border-input bg-card px-3 py-2 text-sm" id="assetPhoto" name="photo" type="file" accept="image/*">
                        <p class="mt-1 text-xs text-muted-foreground">새 파일을 선택한 경우에만 MinIO의 비공개 사진을 교체합니다.</p>
                    </div>
                    <div>
                        <label class="${label}" for="assetNote">메모</label>
                        <textarea class="${input} min-h-24 py-3" id="assetNote" name="note" maxlength="1000"></textarea>
                    </div>
                </form>
            </jsp:body>
        </t:modal>
    </c:if>

    <t:modal id="assetDetailModal" title="품목 상세" description="현재 재고와 변경 이력을 확인합니다.">
        <jsp:body>
            <div data-asset-detail class="flex flex-col gap-5" aria-live="polite"></div>
        </jsp:body>
    </t:modal>

    <c:if test="${canAdmin}">
        <t:modal id="assetUnitModal" title="개별 장비 등록" description="개별 관리 품목에 관리번호를 부여합니다.">
            <jsp:attribute name="footer">
                <t:button variant="outline" action="close-modal">취소</t:button>
                <t:button pageAction="asset-unit-save">장비 등록</t:button>
            </jsp:attribute>
            <jsp:body>
                <form data-unit-form class="flex flex-col gap-4">
                    <input id="unitItemId" type="hidden">
                    <input id="unitId" type="hidden">
                    <div>
                        <label class="${label}" for="unitManagementNo">관리번호 *</label>
                        <input class="${input}" id="unitManagementNo" required maxlength="50" autocomplete="off">
                    </div>
                    <div>
                        <label class="${label}" for="unitLocation">보관 위치 *</label>
                        <input class="${input}" id="unitLocation" required maxlength="200" autocomplete="off">
                    </div>
                    <div data-unit-status-field hidden>
                        <label class="${label}" for="unitStatus">상태 *</label>
                        <select class="${input}" id="unitStatus">
                            <option value="AVAILABLE">사용 가능</option>
                            <option value="REPAIR">수리 중</option>
                            <option value="LOST">분실</option>
                            <option value="DISPOSED">폐기</option>
                        </select>
                    </div>
                    <div data-unit-note-field hidden>
                        <label class="${label}" for="unitNote">변경 사유</label>
                        <textarea class="${input} min-h-20 py-3" id="unitNote" maxlength="1000"></textarea>
                    </div>
                </form>
            </jsp:body>
        </t:modal>
    </c:if>
</t:layout>
