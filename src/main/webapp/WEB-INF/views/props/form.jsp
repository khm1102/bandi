<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="canAdmin" value="${role eq 'admin'}"/>
<t:layout title="소품·장비" active="props" role="${role}" scriptPath="props/form">
    <div class="mx-auto max-w-5xl" data-asset-form-root data-can-admin="${canAdmin}">
        <t:pageHead title="소품·장비" description="이름과 사진부터 등록하고, 필요한 정보만 더 입력하세요.">
            <t:button variant="outline" href="/props">목록으로</t:button>
        </t:pageHead>

        <c:choose>
            <c:when test="${canAdmin}">
                <form class="space-y-7" data-asset-form novalidate>
                    <section class="space-y-5" aria-labelledby="assetBasicHeading">
                        <div class="flex items-baseline justify-between gap-3 border-b pb-4">
                            <div>
                                <h2 id="assetBasicHeading" class="text-lg font-extrabold">기본 정보</h2>
                                <p class="mt-1 text-sm text-muted-foreground">현장에서 바로 찾을 수 있는 정보부터 입력하세요.</p>
                            </div>
                        </div>
                        <div class="grid gap-5 md:grid-cols-2">
                            <div class="md:col-span-2">
                                <label class="mb-2 block text-sm font-extrabold" for="assetName">품목명</label>
                                <input id="assetName" data-asset-name required maxlength="100" placeholder="예: 유선 마이크 3번"
                                       class="h-12 w-full rounded-md border bg-card px-4 text-base focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm">
                            </div>
                            <div>
                                <label class="mb-2 block text-sm font-extrabold" for="assetCategory">분류</label>
                                <select id="assetCategory" data-asset-category required class="h-12 w-full rounded-md border bg-card px-3 text-base md:text-sm">
                                    <option value="PROP">소품</option>
                                    <option value="COSTUME">의상</option>
                                    <option value="LIGHTING">조명 장비</option>
                                    <option value="AUDIO">음향 장비</option>
                                    <option value="VIDEO">영상 장비</option>
                                    <option value="EQUIPMENT">기타 장비</option>
                                </select>
                            </div>
                            <div>
                                <label class="mb-2 block text-sm font-extrabold" for="assetStorageLocation">보관 위치</label>
                                <input id="assetStorageLocation" data-asset-storage-location required maxlength="200" placeholder="예: 무대팀 창고 A"
                                       class="h-12 w-full rounded-md border bg-card px-4 text-base focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm">
                            </div>
                        </div>
                    </section>

                    <section class="border-y py-6" aria-labelledby="assetPhotoHeading">
                        <div class="mb-4">
                            <h2 id="assetPhotoHeading" class="text-lg font-extrabold">품목 사진</h2>
                            <p class="mt-1 text-sm text-muted-foreground">사진 한 장으로 품목을 빠르게 구분할 수 있어요. 새 사진을 고르면 기존 사진이 교체됩니다.</p>
                        </div>
                        <label class="group flex min-h-56 cursor-pointer flex-col items-center justify-center overflow-hidden rounded-xl border-2 border-dashed border-input bg-card p-4 text-center transition-colors hover:border-primary focus-within:border-ring focus-within:ring-2 focus-within:ring-ring/20" data-asset-photo-dropzone>
                            <img class="hidden max-h-80 max-w-full rounded-lg object-contain" alt="선택한 품목 사진 미리보기" data-asset-photo-preview>
                            <span class="space-y-2" data-asset-photo-placeholder>
                                <span class="mx-auto flex size-12 items-center justify-center rounded-full bg-secondary text-xl text-muted-foreground" aria-hidden="true">▣</span>
                                <span class="block font-extrabold">사진을 찍거나 앨범에서 선택하세요</span>
                                <span class="block text-sm text-muted-foreground">JPG, PNG, WebP 이미지 한 장</span>
                            </span>
                            <input class="sr-only" type="file" accept="image/jpeg,image/png,image/webp" capture="environment" data-asset-photo-input>
                        </label>
                        <div class="mt-3 flex flex-wrap items-center justify-between gap-3">
                            <p class="text-sm text-muted-foreground" data-asset-photo-name>사진은 선택 사항이에요.</p>
                            <button type="button" class="min-h-11 rounded-md border bg-card px-3 text-sm font-bold hover:bg-secondary" data-asset-photo-select>사진 선택</button>
                        </div>
                        <p class="mt-3 hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-sm text-destructive" role="alert" data-asset-photo-error></p>
                    </section>

                    <details class="rounded-xl border bg-card" data-asset-advanced>
                        <summary class="min-h-14 cursor-pointer px-5 py-4 text-sm font-extrabold">추가 정보</summary>
                        <div class="grid gap-5 border-t p-5 md:grid-cols-2">
                            <div>
                                <label class="mb-2 block text-sm font-extrabold" for="assetTrackingType">관리 방식</label>
                                <select id="assetTrackingType" data-asset-tracking-type aria-describedby="assetTrackingTypeHelp" class="h-12 w-full rounded-md border bg-card px-3 text-base md:text-sm">
                                    <option value="QUANTITY">수량형</option>
                                    <option value="INDIVIDUAL">개별 관리</option>
                                </select>
                                <p id="assetTrackingTypeHelp" class="mt-2 hidden text-sm text-muted-foreground" data-asset-tracking-type-help>개별 장비와 변경 이력을 보존하기 위해 관리 방식은 등록 후 변경할 수 없어요.</p>
                            </div>
                            <div>
                                <label class="mb-2 block text-sm font-extrabold" for="assetTotalQuantity">수량</label>
                                <input id="assetTotalQuantity" data-asset-total-quantity type="number" min="1" value="1" inputmode="numeric"
                                       class="h-12 w-full rounded-md border bg-card px-4 text-base focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm">
                            </div>
                            <div>
                                <label class="mb-2 block text-sm font-extrabold" for="assetOwnerType">소유 구분</label>
                                <select id="assetOwnerType" data-asset-owner-type class="h-12 w-full rounded-md border bg-card px-3 text-base md:text-sm">
                                    <option value="CLUB">동아리 소유</option>
                                    <option value="MEMBER">부원 소유</option>
                                    <option value="EXTERNAL">외부 소유</option>
                                </select>
                            </div>
                            <div class="hidden" data-asset-external-owner-field>
                                <label class="mb-2 block text-sm font-extrabold" for="assetExternalOwnerName">외부 소유자</label>
                                <input id="assetExternalOwnerName" data-asset-external-owner-name maxlength="100" placeholder="외부 소유자 이름"
                                       class="h-12 w-full rounded-md border bg-card px-4 text-base focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm">
                            </div>
                            <div class="hidden md:col-span-2" data-asset-member-owner-field>
                                <label class="mb-2 block text-sm font-extrabold" for="assetOwnerMemberId">소유 멤버</label>
                                <select id="assetOwnerMemberId" data-asset-owner-member-id class="h-12 w-full rounded-md border bg-card px-3 text-base md:text-sm">
                                    <option value="">소유 멤버를 선택하세요</option>
                                </select>
                            </div>
                            <div class="md:col-span-2">
                                <label class="mb-2 block text-sm font-extrabold" for="assetNote">비고</label>
                                <textarea id="assetNote" data-asset-note rows="4" placeholder="사용 시 유의할 점이나 관리 메모를 적어 주세요."
                                          class="w-full rounded-md border bg-card px-4 py-3 text-base focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"></textarea>
                            </div>
                        </div>
                    </details>

                    <p class="hidden rounded-md border border-destructive bg-destructive-soft px-4 py-3 text-sm text-destructive" role="alert" data-asset-form-error></p>

                    <div class="sticky bottom-0 flex flex-wrap items-center justify-end gap-2 border-t bg-background/95 py-4 backdrop-blur">
                        <t:button variant="outline" href="/props">취소</t:button>
                        <t:button type="submit" pageAction="save-asset">품목 저장</t:button>
                    </div>
                </form>
            </c:when>
            <c:otherwise>
                <section class="rounded-xl border bg-card px-6 py-14 text-center">
                    <h2 class="text-lg font-extrabold">품목을 등록하거나 수정할 권한이 없습니다.</h2>
                    <p class="mt-2 text-sm text-muted-foreground">소품·장비 정보는 조회할 수 있어요.</p>
                    <a href="<c:url value='/props'/>" class="mt-5 inline-flex min-h-11 items-center justify-center rounded-md border bg-card px-4 text-sm font-bold hover:bg-secondary">목록으로</a>
                </section>
            </c:otherwise>
        </c:choose>
    </div>
</t:layout>
