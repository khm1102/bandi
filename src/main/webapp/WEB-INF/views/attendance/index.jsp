<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="input" value="h-11 w-full rounded-md border border-input bg-card px-3 text-base transition-colors focus:border-ring focus:outline-none focus:ring-2 focus:ring-ring/20 md:text-sm"/>
<c:set var="label" value="mb-1.5 block text-xs font-extrabold text-muted-foreground"/>
<c:set var="canManage" value="${role == 'admin'}"/>
<t:layout title="출석" active="attendance" role="${role}" scriptPath="attendance/index">
    <t:pageHead title="행사 · 출석 관리" description="행사 일정과 내 출석 상태를 확인합니다. 출석은 운영진이 현장에서 처리합니다.">
        <c:if test="${canManage}">
            <t:button pageAction="event-create-open">+ 행사 생성</t:button>
        </c:if>
    </t:pageHead>

    <div class="mb-4 grid grid-cols-2 gap-2.5 lg:grid-cols-4 lg:gap-4">
        <t:statCard label="전체 행사" value="—" valueHook="event-total" icon="calendar"/>
        <t:statCard label="예정" value="—" valueHook="event-scheduled" tone="success"/>
        <t:statCard label="출석 확인 중" value="—" valueHook="event-progress" featured="true"/>
        <t:statCard label="내 미처리 출석" value="—" valueHook="attendance-pending" tone="danger"/>
    </div>

    <div class="mb-4 flex flex-wrap gap-2" aria-label="행사 상태 필터">
        <t:filterChip group="event" value="ALL" label="전체" active="true"/>
        <c:if test="${canManage}"><t:filterChip group="event" value="DRAFT" label="초안"/></c:if>
        <t:filterChip group="event" value="SCHEDULED" label="예정"/>
        <t:filterChip group="event" value="IN_PROGRESS" label="출석 확인 중" dot="true"/>
        <t:filterChip group="event" value="CLOSED" label="종료"/>
        <t:filterChip group="event" value="ARCHIVED" label="보관"/>
    </div>

    <div class="rounded-lg border bg-card px-5 py-11 text-center" data-event-state>
        <b class="block text-sm font-extrabold" data-event-state-title>행사를 불러오는 중입니다</b>
        <p class="mt-1 text-xs text-muted-foreground" data-event-state-message>잠시만 기다려 주세요.</p>
        <button type="button" class="mx-auto mt-4 hidden min-h-11 rounded-md border bg-card px-4 text-xs font-bold" data-page-action="event-retry" data-event-retry>다시 시도</button>
    </div>
    <div class="hidden grid-cols-1 gap-4 lg:grid-cols-2" data-event-list></div>

    <template data-event-card-template>
        <article class="overflow-hidden rounded-lg border bg-card" data-event-card>
            <header class="border-b px-5 py-4">
                <div class="flex flex-wrap items-center gap-2">
                    <h2 class="min-w-0 flex-1 text-base font-extrabold" data-event-title></h2>
                    <span data-event-status></span>
                </div>
                <p class="mt-1.5 text-xs font-bold text-muted-foreground" data-event-schedule></p>
            </header>
            <div class="p-5">
                <p class="hidden whitespace-pre-wrap text-sm leading-6 text-muted-foreground" data-event-description></p>
                <div class="mt-4 grid grid-cols-2 gap-2 rounded-md bg-secondary p-3 text-xs">
                    <div><span class="block font-bold text-muted-foreground">참석 대상</span><b class="mt-1 block" data-event-target></b></div>
                    <div><span class="block font-bold text-muted-foreground">출석 확인 시간</span><b class="mt-1 block" data-event-checkin-window></b></div>
                </div>
                <div class="mt-4 hidden rounded-md border border-primary/40 bg-accent/60 px-3 py-2.5" data-my-attendance>
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

    <c:if test="${canManage}">
        <t:modal id="eventModal" title="행사 생성" description="행사를 저장한 뒤 참석 대상을 확정합니다.">
            <jsp:attribute name="footer">
                <t:button variant="outline" action="close-modal">취소</t:button>
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
        </t:modal>

        <t:modal id="targetModal" title="참석 대상 확정" description="확정하면 출석 명단이 생성되며 대상은 다시 바꿀 수 없습니다.">
            <jsp:attribute name="footer">
                <t:button variant="outline" action="close-modal">취소</t:button>
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
        </t:modal>

        <template data-target-member-template>
            <label class="flex min-h-11 cursor-pointer items-center gap-3 border-b px-3 py-2 last:border-b-0">
                <input type="checkbox" class="size-4 accent-primary" data-target-member>
                <span class="flex size-7 shrink-0 items-center justify-center rounded-full bg-primary text-xs font-black text-primary-foreground" data-target-member-avatar></span>
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
                <div class="mt-4 overflow-hidden rounded-md border">
                    <t:dataTable caption="행사 출석 명단">
                        <thead><tr><th class="w-11"><input type="checkbox" class="size-4 accent-primary" data-roster-all aria-label="전체 선택"></th><th>멤버</th><th>팀</th><th>상태</th><th>처리 정보</th></tr></thead>
                        <tbody data-roster-list><tr data-roster-state><td colspan="5" class="px-5 py-11 text-center"><b data-roster-state-title>명단을 불러오는 중입니다</b><p class="mt-1 text-xs text-muted-foreground" data-roster-state-message>잠시만 기다려 주세요.</p></td></tr></tbody>
                    </t:dataTable>
                </div>
                <p class="mt-3 hidden rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-xs text-destructive" data-roster-error role="alert"></p>
            </jsp:body>
        </t:modal>

        <template data-roster-row-template>
            <tr data-roster-row><td><input type="checkbox" class="size-4 accent-primary" data-roster-member></td><td><span class="flex items-center gap-2"><span class="flex size-7 shrink-0 items-center justify-center rounded-full bg-primary text-xs font-black text-primary-foreground" data-roster-avatar></span><b data-roster-name></b></span></td><td data-roster-team></td><td data-roster-status></td><td><span class="block text-xs" data-roster-processor></span><small class="block text-xs text-muted-foreground" data-roster-reason></small></td></tr>
        </template>
    </c:if>
</t:layout>
