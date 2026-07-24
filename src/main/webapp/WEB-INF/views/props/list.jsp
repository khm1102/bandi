<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="canAdmin" value="${role eq 'admin'}"/>
<t:layout title="소품·장비" active="props" role="${role}" scriptPath="props/list">
    <t:pageHead title="소품·장비" description="사진으로 확인하고, 필요한 소품과 장비를 빠르게 찾으세요.">
        <c:if test="${canAdmin}">
            <t:button href="/props/new">품목 등록</t:button>
        </c:if>
    </t:pageHead>

    <section class="mb-5 rounded-xl border bg-card p-4 sm:p-5" aria-label="소품·장비 검색과 필터"
             data-asset-list-root data-can-admin="${canAdmin}">
        <div class="grid gap-3 md:grid-cols-2 lg:grid-cols-5">
            <div class="md:col-span-2">
                <label class="sr-only" for="assetSearch">품목명 또는 보관 위치 검색</label>
                <input id="assetSearch" data-asset-search type="search" autocomplete="off"
                       class="h-11 w-full rounded-md border bg-card px-3 text-base outline-none transition-colors focus:border-ring focus:ring-2 focus:ring-ring/20 md:text-sm"
                       placeholder="품목명 또는 보관 위치 검색">
            </div>
            <div>
                <label class="sr-only" for="assetCategory">분류</label>
                <select id="assetCategory" data-asset-category class="h-11 w-full rounded-md border bg-card px-3 text-base md:text-sm">
                    <option value="">전체 분류</option>
                    <option value="PROP">소품</option>
                    <option value="COSTUME">의상</option>
                    <option value="LIGHTING">조명 장비</option>
                    <option value="AUDIO">음향 장비</option>
                    <option value="VIDEO">영상 장비</option>
                    <option value="EQUIPMENT">기타 장비</option>
                </select>
            </div>
            <div>
                <label class="sr-only" for="assetTrackingType">관리 방식</label>
                <select id="assetTrackingType" data-asset-tracking-type class="h-11 w-full rounded-md border bg-card px-3 text-base md:text-sm">
                    <option value="">전체 관리 방식</option>
                    <option value="QUANTITY">수량형</option>
                    <option value="INDIVIDUAL">개별 관리</option>
                </select>
            </div>
            <div>
                <label class="sr-only" for="assetStatus">상태</label>
                <select id="assetStatus" data-asset-status class="h-11 w-full rounded-md border bg-card px-3 text-base md:text-sm">
                    <option value="">전체 상태</option>
                    <option value="AVAILABLE">사용 가능</option>
                    <option value="IN_USE">사용 중</option>
                    <option value="LOANED">대여 중</option>
                    <option value="REPAIR">수리 중</option>
                    <option value="LOST">분실</option>
                    <option value="DISPOSED">폐기</option>
                </select>
            </div>
        </div>
        <c:if test="${canAdmin}">
            <label class="mt-4 inline-flex min-h-11 items-center gap-2 text-sm font-semibold">
                <input type="checkbox" data-asset-deleted class="size-5 rounded border-input text-primary focus:ring-ring">
                삭제된 품목 보기
            </label>
        </c:if>
    </section>

    <section data-asset-region aria-labelledby="assetListHeading" aria-busy="true">
        <div class="mb-3 flex items-baseline justify-between gap-3">
            <h2 id="assetListHeading" class="text-lg font-extrabold">품목 목록</h2>
            <p class="text-sm text-muted-foreground" data-asset-summary></p>
        </div>

        <div class="hidden overflow-hidden rounded-xl border bg-card md:block">
            <table class="w-full table-fixed text-left">
                <caption class="sr-only">소품과 장비 목록</caption>
                <thead class="border-b bg-secondary/50 text-xs text-muted-foreground">
                <tr>
                    <th class="w-24 px-4 py-3 font-bold">사진</th>
                    <th class="px-4 py-3 font-bold">품목명</th>
                    <th class="w-32 px-4 py-3 font-bold">분류</th>
                    <th class="w-32 px-4 py-3 font-bold">관리</th>
                    <th class="w-44 px-4 py-3 font-bold">보관 위치</th>
                    <th class="w-28 px-4 py-3 font-bold">상태</th>
                    <th class="w-36 px-4 py-3"><span class="sr-only">작업</span></th>
                </tr>
                </thead>
                <tbody data-asset-table-list></tbody>
            </table>
        </div>

        <div class="grid gap-3 md:hidden" data-asset-card-list></div>
        <div class="hidden rounded-xl border bg-card px-5 py-14 text-center" data-asset-empty>
            <p class="font-bold" data-asset-empty-title></p>
            <p class="mt-1 text-sm text-muted-foreground" data-asset-empty-description></p>
            <button type="button" class="mt-4 min-h-11 rounded-md border px-4 text-sm font-bold" data-asset-reset-filter hidden>필터 초기화</button>
        </div>
        <div class="hidden rounded-xl border border-destructive/30 bg-destructive-soft px-5 py-8 text-center" data-asset-error>
            <p class="font-bold text-destructive">품목을 불러오지 못했습니다.</p>
            <button type="button" class="mt-3 min-h-11 rounded-md border border-destructive/30 bg-card px-4 text-sm font-bold" data-asset-retry>다시 시도</button>
        </div>
        <t:pagination id="assetPagination" label="소품·장비 목록 페이지"/>
    </section>
</t:layout>
