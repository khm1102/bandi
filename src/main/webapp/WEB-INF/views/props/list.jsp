<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-bold text-muted-foreground"/>
<c:set var="canAdmin" value="${role eq 'admin'}"/>
<c:set var="canReserve" value="${role eq 'admin' or role eq 'leader'}"/>
<t:layout title="소품·장비" active="props" role="${role}" scriptPath="props/list">
    <main class="mx-auto w-full max-w-5xl">
    <t:pageHead title="소품·장비" description="점검이 필요하거나 사용 중인 품목부터 확인해요">
        <c:if test="${canAdmin}">
            <t:button pageAction="asset-create">새 품목 등록</t:button>
        </c:if>
    </t:pageHead>

    <section class="mb-8" aria-labelledby="assetNextTitle"><p class="text-sm font-bold text-accent-foreground">다음에 확인할 품목</p><div class="mt-2 grid gap-4 border-l-4 border-primary bg-accent px-5 py-5 md:grid-cols-[minmax(0,1fr)_auto] md:items-center"><div><h2 id="assetNextTitle" class="text-lg font-bold" data-asset-next-title>품목 상태를 확인하고 있어요</h2><p class="mt-1 text-sm leading-6 text-muted-foreground" data-asset-next-message>잠시만 기다려 주세요.</p></div><span class="hidden" data-asset-next-action><t:button variant="outline" pageAction="asset-detail" cssClass="w-full md:w-auto">품목 상세 보기</t:button></span></div></section>

    <section class="mb-6" aria-label="소품·장비 검색">
        <label class="sr-only" for="assetSearch">품목명, 위치, 분류 검색</label>
        <input id="assetSearch" data-asset-search class="h-11 w-full rounded-md border border-input bg-card px-4 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"
               type="search" placeholder="품목명, 위치, 분류 검색" autocomplete="off">
    </section>

    <dl class="mb-6 grid grid-cols-2 divide-x border-y py-4 text-center sm:grid-cols-4" aria-label="소품·장비 현황"><div class="px-2"><dt class="text-xs text-muted-foreground">전체 품목</dt><dd class="mt-1 text-lg font-bold tabular-nums"><span data-stat-value="asset-total">0</span>종</dd></div><div class="px-2"><dt class="text-xs text-muted-foreground">총 재고</dt><dd class="mt-1 text-lg font-bold tabular-nums"><span data-stat-value="asset-quantity">0</span>개</dd></div><div class="px-2"><dt class="text-xs text-muted-foreground">사용 중</dt><dd class="mt-1 text-lg font-bold tabular-nums text-warning"><span data-stat-value="asset-used">0</span>개</dd></div><div class="px-2"><dt class="text-xs text-muted-foreground">점검 필요</dt><dd class="mt-1 text-lg font-bold tabular-nums text-destructive"><span data-stat-value="asset-attention">0</span>종</dd></div></dl>

    <div class="mb-5 flex gap-2 overflow-x-auto pb-1" aria-label="품목 상태 필터">
        <t:filterChip group="asset-status" value="ALL" label="전체" active="true"/>
        <t:filterChip group="asset-status" value="AVAILABLE" label="사용 가능"/>
        <t:filterChip group="asset-status" value="IN_USE" label="사용 중"/>
        <t:filterChip group="asset-status" value="REPAIR" label="수리 중"/>
        <t:filterChip group="asset-status" value="LOST" label="분실"/>
    </div>

    <div class="border-y" data-asset-region aria-busy="true">
        <div data-asset-list><p class="px-5 py-12 text-center text-sm text-muted-foreground">품목을 불러오는 중입니다.</p></div>
    </div>
    </main>

    <template data-asset-actions-template>
        <div class="grid grid-cols-2 gap-2 sm:flex sm:justify-end">
            <t:button variant="outline" size="compact" pageAction="asset-detail">상세</t:button>
            <c:if test="${canReserve}">
                <t:button variant="outline" size="compact" pageAction="asset-reserve">사용 등록</t:button>
            </c:if>
            <c:if test="${canAdmin}">
                <t:button size="compact" pageAction="asset-edit">수정</t:button>
            </c:if>
        </div>
    </template>

    <c:if test="${canAdmin}">
        <t:sheet id="assetSheet" title="품목 등록" description="소유 구분과 재고 기준을 함께 기록해요.">
            <jsp:attribute name="footer">
                <t:button variant="outline" action="close-sheet">취소</t:button>
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
        </t:sheet>
    </c:if>

    <t:modal id="assetDetailModal" title="품목 상세" description="현재 재고와 변경·사용 이력을 확인합니다.">
        <jsp:body>
            <p class="sr-only" data-asset-detail-status role="status" aria-live="polite"></p>
            <div data-asset-detail class="flex flex-col gap-5"></div>
        </jsp:body>
    </t:modal>

    <c:if test="${canReserve}">
        <t:modal id="assetReserveModal" title="사용 등록" description="공연 제작을 위해 사용할 수량과 반납 예정 시각을 기록합니다.">
            <jsp:attribute name="footer">
                <t:button variant="outline" action="close-modal">취소</t:button>
                <t:button pageAction="asset-reserve-save">사용 등록</t:button>
            </jsp:attribute>
            <jsp:body>
                <form data-reserve-form class="flex flex-col gap-4">
                    <input id="reserveItemId" type="hidden">
                    <div>
                        <span class="${label}">선택 품목</span>
                        <strong data-reserve-item-name class="block text-sm"></strong>
                    </div>
                    <div>
                        <label class="${label}" for="reserveProject">공연 프로젝트 *</label>
                        <select class="${input}" id="reserveProject" required></select>
                    </div>
                    <c:if test="${canAdmin}">
                        <div>
                            <label class="${label}" for="reserveTeam">사용 팀 *</label>
                            <select class="${input}" id="reserveTeam" required></select>
                        </div>
                    </c:if>
                    <div data-reserve-unit-field hidden>
                        <label class="${label}" for="reserveUnit">개별 장비 *</label>
                        <select class="${input}" id="reserveUnit"></select>
                    </div>
                    <div class="grid gap-3 md:grid-cols-2">
                        <div>
                            <label class="${label}" for="reserveQuantity">수량 *</label>
                            <input class="${input}" id="reserveQuantity" type="number" min="1" step="1" value="1" required>
                        </div>
                        <div>
                            <label class="${label}" for="reserveReturn">반납 예정 *</label>
                            <input class="${input}" id="reserveReturn" type="datetime-local" required>
                        </div>
                    </div>
                    <div>
                        <label class="${label}" for="reserveNote">사용 목적</label>
                        <textarea class="${input} min-h-20 py-3" id="reserveNote" maxlength="1000"></textarea>
                    </div>
                </form>
            </jsp:body>
        </t:modal>
    </c:if>

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
