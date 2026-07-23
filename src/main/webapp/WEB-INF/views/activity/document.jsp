<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="min-h-11 w-full rounded-md border border-input bg-card px-3 text-sm transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20"/>
<c:set var="label" value="mb-1.5 block text-sm font-bold text-foreground"/>
<c:url var="documentApiUrl" value="/api/activity-report-documents"/>
<t:layout title="활동 내역서 만들기" active="activity" role="${role}" scriptPath="activity/document">
    <jsp:attribute name="css">
        <link rel="stylesheet" href="<c:url value='/css/vendor/vanilla-calendar-pro/3.1.0/layout.css'/>">
        <link rel="stylesheet" href="<c:url value='/css/vendor/vanilla-calendar-pro/3.1.0/light.css'/>">
        <link rel="stylesheet" href="<c:url value='/css/vendor/vanilla-calendar-pro/3.1.0/bandi-adapter.css'/>">
    </jsp:attribute>
    <jsp:attribute name="script">
        <script src="<c:url value='/js/vendor/vanilla-calendar-pro/3.1.0/vanilla-calendar-pro.js'/>"></script>
    </jsp:attribute>
    <jsp:body>
        <t:pageHead title="활동 내역서 만들기" description="양식에 필요한 내용만 입력하면 제출용 HWPX 파일을 만듭니다.">
            <t:button variant="outline" pageAction="download-blank" cssClass="w-full md:w-auto">빈 양식 다운로드</t:button>
        </t:pageHead>

        <section class="mb-5 rounded-lg border bg-card px-4 py-4 md:px-5" aria-label="문서 생성 안내">
            <div class="flex items-start gap-3">
                <span class="mt-0.5 flex size-8 shrink-0 items-center justify-center rounded-full bg-secondary text-sm" aria-hidden="true">i</span>
                <div>
                    <p class="text-sm font-extrabold">입력 내용과 사진은 저장하지 않습니다.</p>
                    <p class="mt-1 text-xs leading-5 text-muted-foreground">파일을 만드는 동안에만 사용하며, 새로고침하거나 페이지를 나가면 복구할 수 없습니다.</p>
                    <c:choose>
                        <c:when test="${presidentConfigured}">
                            <p class="mt-2 text-xs font-bold">문서 확인: 반디 회장 <c:out value="${presidentName}"/></p>
                        </c:when>
                        <c:otherwise>
                            <p class="mt-2 text-xs font-bold text-destructive" role="alert">현재 회장이 등록되지 않아 문서를 만들 수 없습니다. 운영 DB에 실제 회장을 배정해 주세요.</p>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </section>

        <form:form id="activityReportForm" modelAttribute="activityReportForm"
                   method="post" action="${documentApiUrl}" enctype="multipart/form-data"
                   data-activity-report-form="true" data-president-configured="${presidentConfigured}">
            <section class="border-t py-6" aria-labelledby="activityReportBasicTitle">
                <h2 id="activityReportBasicTitle" class="text-lg font-black">기본 정보</h2>
                <div class="mt-4 grid grid-cols-1 gap-4 md:grid-cols-2">
                    <div>
                        <form:label path="representative" cssClass="${label}">대표자 <span class="text-destructive">*</span></form:label>
                        <form:input path="representative" cssClass="${input}" maxlength="20" autocomplete="name" aria-describedby="representativeHelp representativeError"/>
                        <p id="representativeHelp" class="mt-1.5 text-xs text-muted-foreground">활동을 대표해 내역서를 작성하는 사람을 입력해 주세요.</p>
                        <p id="representativeError" class="mt-1 hidden text-xs text-destructive" data-field-error="representative"></p>
                    </div>
                    <div>
                        <form:label path="location" cssClass="${label}">활동 장소 <span class="text-destructive">*</span></form:label>
                        <form:input path="location" cssClass="${input}" maxlength="50" aria-describedby="locationError"/>
                        <p id="locationError" class="mt-1 hidden text-xs text-destructive" data-field-error="location"></p>
                    </div>
                    <div class="md:col-span-2">
                        <t:dateTimeField id="activityAt" label="활동 일시" required="true" minuteStep="5"/>
                        <p id="activityAtError" class="mt-1 hidden text-xs text-destructive" data-field-error="activityAt"></p>
                    </div>
                </div>
            </section>

            <section class="border-t py-6" aria-labelledby="activityReportPhotoTitle">
                <h2 id="activityReportPhotoTitle" class="text-lg font-black">활동 사진</h2>
                <p class="mt-1 text-xs leading-5 text-muted-foreground">JPG 또는 PNG 한 장, 최대 10MiB까지 올릴 수 있습니다. 사진은 자르지 않고 양식 안에 맞춰 넣습니다.</p>
                <input class="sr-only" id="activityReportPhoto" type="file" accept="image/jpeg,image/png" aria-describedby="photoError">
                <button type="button" class="mt-4 flex min-h-40 w-full flex-col items-center justify-center rounded-lg border-2 border-dashed border-input bg-card px-5 py-6 text-center transition-colors focus-visible:ring-2 focus-visible:ring-ring" data-page-action="photo-select" data-photo-dropzone>
                    <span class="text-sm font-extrabold" data-photo-prompt>사진을 선택하거나 여기에 놓아 주세요</span>
                    <span class="mt-1 text-xs text-muted-foreground">JPG·PNG · 최대 10MiB</span>
                    <img class="mt-4 hidden max-h-64 max-w-full rounded-md object-contain" data-photo-preview alt="선택한 활동 사진 미리보기">
                </button>
                <div class="mt-3 hidden items-center gap-2" data-photo-actions>
                    <span class="min-w-0 flex-1 truncate text-xs font-bold" data-photo-name></span>
                    <t:button variant="outline" size="compact" pageAction="photo-remove">사진 제거</t:button>
                </div>
                <p id="photoError" class="mt-2 hidden text-xs text-destructive" data-field-error="photo"></p>
            </section>

            <section class="border-t py-6" aria-labelledby="activityReportContentTitle">
                <div class="flex items-end justify-between gap-3">
                    <h2 id="activityReportContentTitle" class="text-lg font-black">활동 내용</h2>
                    <span class="text-xs tabular-nums text-muted-foreground" data-content-count>0 / 300자</span>
                </div>
                <form:textarea path="content" cssClass="mt-4 min-h-44 w-full resize-y rounded-md border border-input bg-card px-3 py-3 text-sm leading-6 focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20" maxlength="300" aria-describedby="contentError"/>
                <p id="contentError" class="mt-1 hidden text-xs text-destructive" data-field-error="content"></p>
            </section>

            <section class="border-t py-6" aria-labelledby="activityReportParticipantsTitle">
                <div class="flex flex-wrap items-end justify-between gap-3">
                    <div>
                        <h2 id="activityReportParticipantsTitle" class="text-lg font-black">참여 인원</h2>
                        <p class="mt-1 text-xs text-muted-foreground">최대 14명까지 입력할 수 있으며 서명 칸은 문서에서 비워 둡니다.</p>
                    </div>
                    <span class="text-xs font-bold tabular-nums" data-participant-count>0 / 14명</span>
                </div>
                <div class="mt-4 flex flex-col gap-2 md:flex-row md:items-start">
                    <div class="relative min-w-0 flex-1">
                        <label class="sr-only" for="participantSearch">등록 멤버 검색</label>
                        <input class="${input}" id="participantSearch" type="search" autocomplete="off" maxlength="50" placeholder="이름 또는 학번 2자 이상 검색" role="combobox" aria-autocomplete="list" aria-expanded="false" aria-controls="participantSuggestions">
                        <div class="absolute z-20 mt-1 hidden max-h-64 w-full overflow-auto rounded-md border bg-card p-1 shadow-lg" id="participantSuggestions" role="listbox" data-participant-suggestions></div>
                        <p class="mt-1 hidden text-xs text-destructive" data-participant-search-error></p>
                    </div>
                    <t:button variant="outline" pageAction="participant-add-manual" cssClass="w-full md:w-auto">직접 입력 추가</t:button>
                </div>
                <div class="mt-4 flex flex-col gap-3" data-participant-list></div>
                <div class="mt-4 rounded-md border bg-card px-4 py-8 text-center" data-participant-empty>
                    <p class="text-sm font-bold">아직 참여자가 없습니다.</p>
                    <p class="mt-1 text-xs text-muted-foreground">등록 멤버를 검색하거나 직접 입력으로 추가해 주세요.</p>
                </div>
                <p class="mt-2 hidden text-xs text-destructive" data-field-error="participants"></p>
            </section>

            <div class="border-t py-6">
                <p class="hidden rounded-md border border-destructive bg-destructive-soft px-4 py-3 text-sm text-destructive" data-form-error role="alert"></p>
                <div class="mt-4 flex justify-end">
                    <t:button type="submit" cssClass="w-full md:w-auto" pageAction="generate" >HWPX 내역서 만들기</t:button>
                </div>
                <div class="mt-4 hidden rounded-lg border bg-card px-4 py-4" data-success-state role="status">
                    <p class="text-sm font-extrabold">활동 내역서 파일을 만들었어요.</p>
                    <p class="mt-1 text-xs text-muted-foreground">입력값은 현재 화면에만 남아 있습니다. 파일을 확인한 뒤 제출해 주세요.</p>
                    <div class="mt-3 flex flex-col gap-2 sm:flex-row">
                        <t:button variant="outline" pageAction="download-again">다시 다운로드</t:button>
                        <t:button variant="outline" pageAction="reset-form">새로 작성</t:button>
                    </div>
                </div>
            </div>
        </form:form>

        <template data-participant-template>
            <article class="rounded-lg border bg-card p-4" data-participant-row>
                <div class="grid grid-cols-1 gap-3 md:grid-cols-[1fr_1.4fr_1fr_1.4fr_auto] md:items-end">
                    <label class="text-xs font-bold">이름 <input class="mt-1 ${input}" data-participant-field="name" maxlength="20"></label>
                    <label class="text-xs font-bold">학과 <input class="mt-1 ${input}" data-participant-field="department" maxlength="30"></label>
                    <label class="text-xs font-bold">학번 <input class="mt-1 ${input}" data-participant-field="studentNo" maxlength="20" inputmode="numeric"></label>
                    <label class="text-xs font-bold">비고 <input class="mt-1 ${input}" data-participant-field="note" maxlength="40" placeholder="외부인은 여기에 표시"></label>
                    <t:button variant="outline" size="compact" pageAction="participant-remove" cssClass="text-destructive">삭제</t:button>
                </div>
            </article>
        </template>

        <p class="mt-4 text-xs leading-5 text-muted-foreground">HWPX는 한컴의 공개 문서 포맷 안내를 참고해 생성합니다. 최종 제출 전 한글에서 내용과 사진 배치를 확인해 주세요.</p>
    </jsp:body>
</t:layout>
