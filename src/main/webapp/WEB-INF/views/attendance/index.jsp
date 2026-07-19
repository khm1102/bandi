<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-bold text-muted-foreground"/>
<c:set var="canManage" value="${role == 'admin'}"/>
<t:layout title="출석" active="attendance" role="${role}" scriptPath="attendance/index">
    <main class="w-full">
    <t:pageHead title="행사 · 출석 관리" description="진행 중인 행사를 먼저 확인하고, 운영진이 현장에서 출석을 기록해요">
        <c:if test="${canManage}">
            <t:button pageAction="event-create-open">새 행사 만들기</t:button>
        </c:if>
    </t:pageHead>

    <section class="mb-8" aria-labelledby="eventNextTitle">
        <p class="text-sm font-bold text-accent-foreground">지금 확인할 행사</p>
        <div class="mt-2 grid gap-4 border-l-4 border-primary bg-accent px-5 py-5 md:grid-cols-[minmax(0,1fr)_auto] md:items-center">
            <div>
                <h2 id="eventNextTitle" class="text-lg font-bold" data-event-next-title>행사를 불러오고 있어요</h2>
                <p class="mt-1 text-sm leading-6 text-muted-foreground" data-event-next-message>잠시만 기다려 주세요.</p>
            </div>
            <c:if test="${canManage}"><span class="hidden" data-event-next-action><t:button pageAction="event-roster-open" cssClass="w-full md:w-auto">출석 명단 열기</t:button></span></c:if>
        </div>
    </section>

    <dl class="mb-6 grid grid-cols-2 divide-x border-y py-4 text-center sm:grid-cols-4" aria-label="행사 요약">
        <div class="px-2"><dt class="text-xs text-muted-foreground">전체 행사</dt><dd class="mt-1 text-lg font-bold tabular-nums" data-stat-value="event-total">—</dd></div>
        <div class="px-2"><dt class="text-xs text-muted-foreground">예정</dt><dd class="mt-1 text-lg font-bold tabular-nums" data-stat-value="event-scheduled">—</dd></div>
        <div class="px-2"><dt class="text-xs text-muted-foreground">확인 중</dt><dd class="mt-1 text-lg font-bold tabular-nums" data-stat-value="event-progress">—</dd></div>
        <div class="px-2"><dt class="text-xs text-muted-foreground">내 미처리</dt><dd class="mt-1 text-lg font-bold tabular-nums text-destructive" data-stat-value="attendance-pending">—</dd></div>
    </dl>

    <div class="mb-5 flex gap-2 overflow-x-auto pb-1" aria-label="행사 상태 필터">
        <t:filterChip group="event" value="ALL" label="전체" active="true"/>
        <c:if test="${canManage}"><t:filterChip group="event" value="DRAFT" label="초안"/></c:if>
        <t:filterChip group="event" value="SCHEDULED" label="예정"/>
        <t:filterChip group="event" value="IN_PROGRESS" label="출석 확인 중" dot="true"/>
        <t:filterChip group="event" value="CLOSED" label="종료"/>
        <t:filterChip group="event" value="ARCHIVED" label="보관"/>
    </div>

    <div class="border-y px-5 py-12 text-center" data-event-state>
        <b class="block text-sm font-bold" data-event-state-title>행사를 불러오는 중입니다</b>
        <p class="mt-1 text-sm text-muted-foreground" data-event-state-message>잠시만 기다려 주세요.</p>
        <button type="button" class="mx-auto mt-4 hidden min-h-11 rounded-md border bg-card px-4 text-sm font-bold" data-page-action="event-retry" data-event-retry>다시 시도</button>
    </div>
    <div class="hidden divide-y border-y" data-event-list></div>

    <template data-event-card-template>
        <article class="px-4 py-5 md:px-5 lg:grid lg:grid-cols-[minmax(16rem,0.8fr)_minmax(24rem,1.2fr)] lg:items-start lg:gap-8" data-event-card>
            <header>
                <div class="flex flex-wrap items-center gap-2">
                    <h2 class="min-w-0 flex-1 text-base font-extrabold" data-event-title></h2>
                    <span data-event-status></span>
                </div>
                <p class="mt-1.5 text-xs font-bold text-muted-foreground" data-event-schedule></p>
            </header>
            <div class="pt-4 lg:pt-0">
                <p class="hidden whitespace-pre-wrap text-sm leading-6 text-muted-foreground" data-event-description></p>
                <div class="mt-4 grid grid-cols-1 gap-3 border-y py-3 text-xs sm:grid-cols-2">
                    <div><span class="block font-bold text-muted-foreground">참석 대상</span><b class="mt-1 block" data-event-target></b></div>
                    <div><span class="block font-bold text-muted-foreground">출석 확인 시간</span><b class="mt-1 block" data-event-checkin-window></b></div>
                </div>
                <div class="mt-4 hidden border-l-4 border-primary bg-accent px-4 py-3" data-my-attendance>
                    <div class="flex flex-wrap items-center gap-2">
                        <b class="text-xs">내 출석 상태</b>
                        <span data-my-attendance-status></span>
                        <span class="ml-auto text-xs text-muted-foreground" data-my-attendance-time></span>
                    </div>
                    <p class="mt-1 hidden text-xs text-muted-foreground" data-my-attendance-reason></p>
                </div>
                <div class="mt-4 hidden flex-wrap gap-2 border-t pt-4" data-event-admin-actions>
                    <span class="hidden" data-role="edit"><t:button variant="outline" size="compact" pageAction="event-edit">초안 수정</t:button></span>
                    <span class="hidden" data-role="target"><t:button size="compact" pageAction="event-target-open">대상 확정</t:button></span>
                    <span class="hidden" data-role="open"><t:button size="compact" pageAction="event-checkin-open">출석 확인 시작</t:button></span>
                    <span class="hidden" data-role="roster"><t:button variant="outline" size="compact" pageAction="event-roster-open">출석 명단</t:button></span>
                    <span class="hidden" data-role="close"><t:button variant="outline" size="compact" pageAction="event-checkin-close">출석 확인 종료</t:button></span>
                    <span class="hidden" data-role="archive"><t:button variant="outline" size="compact" pageAction="event-archive" cssClass="text-destructive">보관</t:button></span>
                </div>
            </div>
        </article>
    </template>
    </main>

    <c:if test="${canManage}">
        <t:sheet id="eventSheet" title="행사 생성" description="행사를 저장한 뒤 참석 대상을 확정해요.">
            <jsp:attribute name="footer">
                <t:button variant="outline" action="close-sheet">취소</t:button>
                <t:button pageAction="event-save"><span data-event-save-label>행사 저장</span></t:button>
            </jsp:attribute>
            <jsp:body>
                <div class="flex flex-col gap-3">
                    <div class="grid grid-cols-1 gap-3 md:grid-cols-2">
                        <div>
                            <label class="${label}" for="eventTargetScope">참석 대상 <span class="text-accent-foreground">*</span></label>
                            <select class="${input}" id="eventTargetScope">
                                <option value="ALL">전체 활성 멤버</option>
                                <option value="TEAM">특정 팀</option>
                                <option value="SELECTED">선택한 멤버</option>
                            </select>
                        </div>
                        <div class="hidden" data-event-team-field>
                            <label class="${label}" for="eventTeam">대상 팀 <span class="text-accent-foreground">*</span></label>
                            <select class="${input}" id="eventTeam"><option value="">팀을 선택해 주세요</option></select>
                        </div>
                    </div>
                    <p class="rounded-md bg-secondary px-3 py-2.5 text-xs text-muted-foreground" data-event-target-help>저장 후 전체 활성 멤버를 대상으로 확정합니다.</p>
                    <div>
                        <label class="${label}" for="eventTitle">행사명 <span class="text-accent-foreground">*</span></label>
                        <input class="${input}" id="eventTitle" type="text" maxlength="150" placeholder="예) 정기공연 뒤풀이">
                    </div>
                    <div>
                        <label class="${label}" for="eventDescription">설명</label>
                        <textarea class="min-h-20 w-full resize-y rounded-md border border-input bg-card px-3 py-2.5 text-sm" id="eventDescription" maxlength="1000" placeholder="준비물이나 집합 안내를 입력해 주세요."></textarea>
                    </div>
                    <div>
                        <label class="${label}" for="eventPlace">장소 <span class="text-accent-foreground">*</span></label>
                        <input class="${input}" id="eventPlace" type="text" maxlength="150" placeholder="예) 교내 소극장">
                    </div>
                    <div class="grid grid-cols-1 gap-3 md:grid-cols-2">
                        <div><label class="${label}" for="eventStart">행사 시작 <span class="text-accent-foreground">*</span></label><input class="${input}" id="eventStart" type="datetime-local"></div>
                        <div><label class="${label}" for="eventEnd">행사 종료 <span class="text-accent-foreground">*</span></label><input class="${input}" id="eventEnd" type="datetime-local"></div>
                    </div>
                    <div class="grid grid-cols-1 gap-3 md:grid-cols-2">
                        <div><label class="${label}" for="checkInStart">출석 확인 시작 <span class="text-accent-foreground">*</span></label><input class="${input}" id="checkInStart" type="datetime-local"></div>
                        <div><label class="${label}" for="checkInEnd">출석 확인 종료 <span class="text-accent-foreground">*</span></label><input class="${input}" id="checkInEnd" type="datetime-local"></div>
                    </div>
                    <p class="hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-xs text-destructive" data-event-form-error role="alert"></p>
                </div>
            </jsp:body>
        </t:sheet>

        <t:sheet id="targetSheet" title="참석 대상 확정" description="확정하면 출석 명단이 생성되며 대상은 다시 바꿀 수 없어요.">
            <jsp:attribute name="footer">
                <t:button variant="outline" action="close-sheet">취소</t:button>
                <t:button pageAction="event-target-confirm">대상 확정</t:button>
            </jsp:attribute>
            <jsp:body>
                <div class="rounded-md bg-secondary px-3 py-2.5 text-sm">
                    <span class="text-xs font-bold text-muted-foreground">확정할 행사</span>
                    <b class="mt-1 block" data-target-event-title></b>
                    <p class="mt-1 text-xs text-muted-foreground" data-target-summary></p>
                </div>
                <div class="mt-4 hidden" data-selected-member-section>
                    <div class="mb-2 flex items-center gap-2">
                        <b class="text-xs">활성 멤버 선택</b>
                        <label class="ml-auto flex min-h-11 cursor-pointer items-center gap-2 text-xs font-bold md:min-h-9"><input type="checkbox" class="size-4 accent-primary" data-target-all> 전체 선택</label>
                    </div>
                    <div class="max-h-72 overflow-y-auto rounded-md border" data-target-member-list></div>
                    <p class="py-6 text-center text-xs text-muted-foreground" data-target-member-empty>선택할 수 있는 활성 멤버가 없습니다.</p>
                </div>
                <p class="mt-3 hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-xs text-destructive" data-target-error role="alert"></p>
            </jsp:body>
        </t:sheet>

        <template data-target-member-template>
            <label class="flex min-h-11 cursor-pointer items-center gap-3 border-b px-3 py-2 last:border-b-0">
                <input type="checkbox" class="size-4 accent-primary" data-target-member>
                <span class="flex size-7 shrink-0 items-center justify-center rounded-full bg-primary text-xs font-extrabold text-primary-foreground" data-target-member-avatar></span>
                <span class="min-w-0"><b class="block truncate text-sm" data-target-member-name></b><small class="block truncate text-xs text-muted-foreground" data-target-member-meta></small></span>
            </label>
        </template>

        <t:modal id="rosterModal" title="출석 명단" description="현장에서 확인한 멤버의 출석 상태를 일괄 처리합니다.">
            <jsp:attribute name="footer">
                <t:button variant="outline" action="close-modal">닫기</t:button>
                <span class="hidden" data-roster-process-button><t:button pageAction="attendance-process">선택 상태 반영</t:button></span>
            </jsp:attribute>
            <jsp:body>
                <div class="rounded-md bg-secondary px-3 py-2.5">
                    <b class="text-sm" data-roster-event-title></b>
                    <div class="mt-2 flex flex-wrap gap-2" data-roster-counts></div>
                </div>
                <div class="mt-4 hidden rounded-md border bg-card p-3" data-roster-controls>
                    <div class="grid grid-cols-1 gap-3 md:grid-cols-2">
                        <div><label class="${label}" for="attendanceStatus">처리 상태</label><select class="${input}" id="attendanceStatus"><option value="PRESENT">출석</option><option value="LATE">지각</option><option value="ABSENT">결석</option><option value="EXCUSED">공결</option></select></div>
                        <div><label class="${label}" for="attendanceReason">처리 사유</label><input class="${input}" id="attendanceReason" type="text" maxlength="500" placeholder="공결은 사유가 필수입니다."></div>
                    </div>
                </div>
                <p class="mt-3 hidden rounded-md bg-warning-soft px-3 py-2.5 text-xs text-warning" data-roster-readonly>출석 확인 중인 행사에서만 상태를 처리할 수 있습니다.</p>
                <div class="mt-4 border-y">
                    <label class="flex min-h-11 items-center gap-3 border-b px-4 text-sm font-bold"><input type="checkbox" class="size-4 accent-primary" data-roster-all> 현재 명단 전체 선택</label>
                    <div data-roster-list></div>
                    <div data-roster-state class="px-5 py-12 text-center"><b data-roster-state-title>명단을 불러오는 중입니다</b><p class="mt-1 text-sm text-muted-foreground" data-roster-state-message>잠시만 기다려 주세요.</p></div>
                </div>
                <p class="mt-3 hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-xs text-destructive" data-roster-error role="alert"></p>
            </jsp:body>
        </t:modal>

        <template data-roster-row-template>
            <article data-roster-row class="grid grid-cols-[auto_minmax(0,1fr)] gap-x-3 gap-y-2 border-b px-4 py-4 last:border-b-0">
                <input type="checkbox" class="mt-1 size-4 accent-primary" data-roster-member>
                <div class="min-w-0">
                    <div class="flex flex-wrap items-center gap-2"><span class="flex size-7 shrink-0 items-center justify-center rounded-full bg-primary text-xs font-extrabold text-primary-foreground" data-roster-avatar></span><b data-roster-name></b><span data-roster-status></span></div>
                    <p class="mt-1 text-xs text-muted-foreground"><span data-roster-team></span> · <span data-roster-processor></span></p>
                    <small class="mt-1 block text-xs text-muted-foreground" data-roster-reason></small>
                    <t:button variant="outline" size="compact" pageAction="attendance-history-open" cssClass="mt-3 w-full sm:w-auto">변경 이력</t:button>
                </div>
            </article>
        </template>

        <t:sheet id="attendanceHistorySheet" title="출석 상태 변경 이력" description="처리 상태와 담당자, 사유를 시간순으로 확인해요.">
            <jsp:body><p class="mb-3 text-sm font-extrabold" data-attendance-history-member></p><div class="flex flex-col gap-2" data-attendance-history></div></jsp:body>
        </t:sheet>
    </c:if>
</t:layout>
