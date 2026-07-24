<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="canAdmin" value="${role eq 'admin'}"/>
<t:layout title="소품·장비 상세" active="props" role="${role}" scriptPath="props/detail">
    <section data-asset-detail-root data-can-admin="${canAdmin}" aria-busy="true">
        <div class="mb-6 flex flex-wrap items-center justify-between gap-3">
            <a href="<c:url value='/props'/>" class="inline-flex min-h-11 items-center justify-center rounded-md border bg-card px-4 text-sm font-bold hover:bg-secondary">목록으로</a>
            <div class="hidden flex-wrap gap-2" data-asset-detail-admin-actions>
                <a class="inline-flex min-h-11 items-center justify-center rounded-md border bg-card px-4 text-sm font-bold hover:bg-secondary" data-asset-edit-link>품목 수정</a>
                <button type="button" class="inline-flex min-h-11 items-center justify-center rounded-md bg-destructive px-4 text-sm font-bold text-destructive-foreground hover:bg-destructive/90" data-asset-delete data-confirm="" data-confirm-action="삭제">품목 삭제</button>
            </div>
        </div>

        <div class="hidden space-y-7" data-asset-detail-loaded>
            <article class="overflow-hidden rounded-xl border bg-card">
                <div class="grid lg:grid-cols-[minmax(18rem,0.8fr)_minmax(0,1.2fr)]">
                    <div class="flex min-h-72 items-center justify-center bg-secondary p-5" data-asset-photo-area></div>
                    <div class="p-6 sm:p-8">
                        <div class="flex flex-wrap items-center gap-2" data-asset-badges></div>
                        <h1 class="mt-4 text-2xl font-black tracking-tight sm:text-3xl" data-asset-name></h1>
                        <p class="mt-2 text-sm text-muted-foreground" data-asset-category></p>
                        <dl class="mt-7 grid gap-5 sm:grid-cols-2" data-asset-facts></dl>
                    </div>
                </div>
            </article>

            <section class="rounded-xl border bg-card p-5 sm:p-6" aria-labelledby="assetUnitsHeading" data-asset-units-section>
                <div class="flex flex-wrap items-center justify-between gap-3">
                    <div>
                        <h2 id="assetUnitsHeading" class="text-lg font-extrabold">개별 장비</h2>
                        <p class="mt-1 text-sm text-muted-foreground">개별 관리 품목의 관리번호와 현재 상태를 확인해요.</p>
                    </div>
                    <button type="button" class="hidden min-h-11 rounded-md bg-primary px-4 text-sm font-bold text-primary-foreground hover:bg-primary-strong" data-open-modal="assetUnitCreateModal" data-asset-unit-create>개별 장비 추가</button>
                </div>
                <div class="mt-5 hidden overflow-x-auto md:block">
                    <table class="min-w-[42rem] w-full text-left">
                        <thead class="border-b text-xs text-muted-foreground"><tr><th class="px-3 py-3">관리번호</th><th class="px-3 py-3">상태</th><th class="px-3 py-3">보관 위치</th><th class="px-3 py-3"><span class="sr-only">수정</span></th></tr></thead>
                        <tbody data-asset-unit-list></tbody>
                    </table>
                </div>
                <div class="mt-5 grid gap-3 md:hidden" data-asset-unit-card-list></div>
                <p class="hidden mt-5 rounded-md bg-secondary px-4 py-5 text-center text-sm text-muted-foreground" data-asset-unit-empty>등록된 개별 장비가 없습니다.</p>
            </section>

            <section class="rounded-xl border bg-card p-5 sm:p-6" aria-labelledby="assetHistoryHeading">
                <h2 id="assetHistoryHeading" class="text-lg font-extrabold">변경 이력</h2>
                <p class="mt-1 text-sm text-muted-foreground">삭제와 복구를 포함한 품목 변경 이력이 남아요.</p>
                <ol class="mt-5 space-y-3" data-asset-history-list></ol>
                <p class="hidden mt-5 rounded-md bg-secondary px-4 py-5 text-center text-sm text-muted-foreground" data-asset-history-empty>아직 변경 이력이 없습니다.</p>
            </section>
        </div>

        <div class="rounded-xl border bg-card px-6 py-14 text-center" data-asset-detail-loading>
            <p class="font-bold">품목 정보를 불러오는 중이에요.</p>
        </div>
        <div class="hidden rounded-xl border border-destructive/30 bg-destructive-soft px-6 py-14 text-center" data-asset-detail-error>
            <p class="font-bold text-destructive">품목을 불러오지 못했습니다.</p>
            <p class="mt-2 text-sm text-destructive" data-asset-detail-error-message></p>
            <button type="button" class="mt-4 min-h-11 rounded-md border border-destructive/30 bg-card px-4 text-sm font-bold" data-asset-detail-retry>다시 시도</button>
        </div>
    </section>

    <c:if test="${canAdmin}">
        <t:modal id="assetUnitCreateModal" title="개별 장비 추가" description="관리번호와 현재 보관 위치를 입력하세요." size="md" mobileFullscreen="true">
            <jsp:attribute name="footer"><t:button variant="outline" action="close-modal">취소</t:button><t:button action="save-asset-unit">추가</t:button></jsp:attribute>
            <jsp:body>
                <div class="space-y-4">
                    <div><label class="mb-2 block text-sm font-extrabold" for="assetUnitManagementNo">관리번호</label><input id="assetUnitManagementNo" data-asset-unit-management-no maxlength="50" class="h-12 w-full rounded-md border bg-card px-3" placeholder="예: MIC-001"></div>
                    <div><label class="mb-2 block text-sm font-extrabold" for="assetUnitStorageLocation">보관 위치</label><input id="assetUnitStorageLocation" data-asset-unit-storage-location maxlength="200" class="h-12 w-full rounded-md border bg-card px-3" placeholder="예: 음향팀 캐비닛"></div>
                    <p class="hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2 text-sm text-destructive" data-asset-unit-create-error></p>
                </div>
            </jsp:body>
        </t:modal>
        <t:modal id="assetUnitEditModal" title="개별 장비 수정" description="상태와 보관 위치를 변경하면 이력에 남아요." size="md" mobileFullscreen="true">
            <jsp:attribute name="footer"><t:button variant="outline" action="close-modal">취소</t:button><t:button action="update-asset-unit">저장</t:button></jsp:attribute>
            <jsp:body>
                <div class="space-y-4">
                    <div><label class="mb-2 block text-sm font-extrabold" for="assetUnitStatus">상태</label><select id="assetUnitStatus" data-asset-unit-status class="h-12 w-full rounded-md border bg-card px-3"><option value="AVAILABLE">사용 가능</option><option value="IN_USE">사용 중</option><option value="LOANED">대여 중</option><option value="REPAIR">수리 중</option><option value="LOST">분실</option><option value="DISPOSED">폐기</option></select></div>
                    <div><label class="mb-2 block text-sm font-extrabold" for="assetUnitEditStorageLocation">보관 위치</label><input id="assetUnitEditStorageLocation" data-asset-unit-edit-storage-location maxlength="200" class="h-12 w-full rounded-md border bg-card px-3"></div>
                    <div><label class="mb-2 block text-sm font-extrabold" for="assetUnitNote">변경 사유</label><textarea id="assetUnitNote" data-asset-unit-note rows="3" class="w-full rounded-md border bg-card px-3 py-2" placeholder="예: 배터리 교체"></textarea></div>
                    <p class="hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2 text-sm text-destructive" data-asset-unit-edit-error></p>
                </div>
            </jsp:body>
        </t:modal>
    </c:if>
</t:layout>
