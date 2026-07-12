<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout title="스타일 가이드" crumb="스타일 가이드">
    <jsp:attribute name="script">
        <script type="module" src="<c:url value='/js/styleguide/index.js'/>"></script>
    </jsp:attribute>
    <jsp:body>
        <t:pageHead title="스타일 가이드" description="bandi 디자인 시스템의 살아있는 명세 — 토큰·타이포·컴포넌트를 여기서 복사해 쓴다 (docs/design-guide.md)">
            <button type="button" data-action="show-demo-toast"
                    class="inline-flex h-9 items-center justify-center gap-1.5 rounded-md border bg-card px-4 text-sm font-bold transition-colors hover:bg-secondary">토스트 데모</button>
            <button type="button" data-action="open-demo-modal"
                    class="inline-flex h-9 items-center justify-center gap-1.5 rounded-md bg-primary px-4 text-sm font-bold text-primary-foreground transition-colors hover:bg-primary-strong hover:text-white">모달 데모</button>
        </t:pageHead>

        <div class="grid gap-4">
            <t:card title="색상 토큰">
                <div class="grid grid-cols-2 gap-3 md:grid-cols-4 lg:grid-cols-6">
                    <div><div class="h-14 rounded-md bg-primary"></div><p class="mt-1.5 text-xs font-bold">primary</p></div>
                    <div><div class="h-14 rounded-md bg-primary-strong"></div><p class="mt-1.5 text-xs font-bold">primary-strong</p></div>
                    <div><div class="h-14 rounded-md bg-accent"></div><p class="mt-1.5 text-xs font-bold">accent</p></div>
                    <div><div class="h-14 rounded-md bg-sidebar"></div><p class="mt-1.5 text-xs font-bold">sidebar</p></div>
                    <div><div class="h-14 rounded-md bg-sidebar-accent"></div><p class="mt-1.5 text-xs font-bold">sidebar-accent</p></div>
                    <div><div class="h-14 rounded-md border bg-background"></div><p class="mt-1.5 text-xs font-bold">background</p></div>
                    <div><div class="h-14 rounded-md bg-success"></div><p class="mt-1.5 text-xs font-bold">success</p></div>
                    <div><div class="h-14 rounded-md bg-warning"></div><p class="mt-1.5 text-xs font-bold">warning</p></div>
                    <div><div class="h-14 rounded-md bg-destructive"></div><p class="mt-1.5 text-xs font-bold">destructive</p></div>
                    <div><div class="h-14 rounded-md bg-info"></div><p class="mt-1.5 text-xs font-bold">info</p></div>
                    <div><div class="h-14 rounded-md border bg-secondary"></div><p class="mt-1.5 text-xs font-bold">secondary</p></div>
                    <div><div class="h-14 rounded-md border bg-card"></div><p class="mt-1.5 text-xs font-bold">card</p></div>
                </div>
            </t:card>

            <t:card title="타이포그래피 — Noto Sans KR">
                <p class="text-2xl font-black tracking-tight">페이지 제목 text-2xl font-black</p>
                <p class="mt-2 text-lg font-extrabold">섹션 제목 text-lg font-extrabold</p>
                <p class="mt-2 text-sm font-extrabold">카드 제목 text-sm font-extrabold</p>
                <p class="mt-2 text-sm">본문 text-sm — 공연과 단원, 회비와 일정을 한 곳에서 관리한다.</p>
                <p class="mt-2 text-xs text-muted-foreground">보조 텍스트 text-xs text-muted-foreground</p>
            </t:card>

            <t:card title="버튼">
                <div class="flex flex-wrap items-center gap-2">
                    <button type="button" class="inline-flex h-9 items-center justify-center rounded-md bg-primary px-4 text-sm font-bold text-primary-foreground transition-colors hover:bg-primary-strong hover:text-white focus-visible:ring-2 focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50">기본(primary)</button>
                    <button type="button" class="inline-flex h-9 items-center justify-center rounded-md border bg-card px-4 text-sm font-bold transition-colors hover:bg-secondary">보조(outline)</button>
                    <button type="button" class="inline-flex h-9 items-center justify-center rounded-md bg-sidebar px-4 text-sm font-bold text-white transition-colors hover:bg-sidebar-accent">네이비(dark)</button>
                    <button type="button" class="inline-flex h-9 items-center justify-center rounded-md bg-destructive px-4 text-sm font-bold text-destructive-foreground transition-colors hover:bg-destructive/90">삭제(danger)</button>
                    <button type="button" class="inline-flex h-8 items-center justify-center rounded-md border bg-card px-3 text-xs font-bold transition-colors hover:bg-secondary">작게(sm)</button>
                    <button type="button" disabled class="inline-flex h-9 items-center justify-center rounded-md bg-primary px-4 text-sm font-bold text-primary-foreground disabled:pointer-events-none disabled:opacity-50">비활성</button>
                </div>
            </t:card>

            <t:card title="배지 / 칩">
                <div class="flex flex-wrap items-center gap-2">
                    <t:badge tone="accent" dot="true">모집 중</t:badge>
                    <t:badge tone="success" dot="true">납부 완료</t:badge>
                    <t:badge tone="warning" dot="true">확인 필요</t:badge>
                    <t:badge tone="danger" dot="true">미납</t:badge>
                    <t:badge tone="info">공지</t:badge>
                    <t:badge tone="neutral">보관됨</t:badge>
                </div>
                <div class="mt-4 flex flex-wrap gap-2">
                    <button type="button" class="inline-flex h-8 items-center gap-1.5 rounded-md border border-sidebar bg-sidebar px-3 text-xs font-bold text-white">전체 <span class="opacity-70">24</span></button>
                    <button type="button" class="inline-flex h-8 items-center gap-1.5 rounded-md border bg-card px-3 text-xs font-bold text-muted-foreground transition-colors hover:border-sidebar-muted">기수별 <span class="opacity-70">3</span></button>
                    <button type="button" class="inline-flex h-8 items-center gap-1.5 rounded-md border bg-card px-3 text-xs font-bold text-muted-foreground transition-colors hover:border-sidebar-muted">팀별</button>
                </div>
            </t:card>

            <div class="grid grid-cols-2 gap-4 lg:grid-cols-4">
                <t:statCard label="전체 단원" value="42" unit="명"/>
                <t:statCard label="이번 달 납부율" value="87%" delta="▲ 지난달 대비 4%p" tone="success"/>
                <t:statCard label="미납 인원" value="5" unit="명" delta="▼ 독촉 필요" tone="danger"/>
                <t:statCard label="다가오는 일정" value="3" unit="건" delta="이번 주" tone="default"/>
            </div>

            <t:card title="테이블" flush="true">
                <div class="overflow-x-auto">
                    <table class="w-full border-collapse text-left">
                        <thead>
                        <tr>
                            <th class="whitespace-nowrap border-b bg-secondary px-4 py-3 text-xs font-extrabold text-muted-foreground">이름</th>
                            <th class="whitespace-nowrap border-b bg-secondary px-4 py-3 text-xs font-extrabold text-muted-foreground">기수</th>
                            <th class="whitespace-nowrap border-b bg-secondary px-4 py-3 text-xs font-extrabold text-muted-foreground">상태</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr class="transition-colors hover:bg-secondary/50">
                            <td class="border-b px-4 py-3 text-sm font-bold">김반디</td>
                            <td class="border-b px-4 py-3 text-sm text-muted-foreground">12기</td>
                            <td class="border-b px-4 py-3"><t:badge tone="success" dot="true">활동</t:badge></td>
                        </tr>
                        <tr class="transition-colors hover:bg-secondary/50">
                            <td class="px-4 py-3 text-sm font-bold">이무대</td>
                            <td class="px-4 py-3 text-sm text-muted-foreground">13기</td>
                            <td class="px-4 py-3"><t:badge tone="neutral">휴단</t:badge></td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </t:card>

            <t:card title="공지 배너 / 빈 상태">
                <div class="flex items-start gap-3 rounded-lg border border-l-4 border-l-primary bg-accent/50 px-4 py-3.5">
                    <div class="min-w-0">
                        <b class="text-sm">정기 공연 티켓 오픈</b>
                        <p class="mt-0.5 text-xs text-muted-foreground">6월 정기 공연 예매가 시작되었습니다.</p>
                    </div>
                </div>
                <div class="mt-4 rounded-lg border border-dashed">
                    <t:emptyState title="아직 등록된 일정이 없습니다" message="첫 일정을 만들어 단원들과 공유해 보세요.">
                        <button type="button" class="inline-flex h-8 items-center justify-center rounded-md bg-primary px-3 text-xs font-bold text-primary-foreground transition-colors hover:bg-primary-strong hover:text-white">일정 만들기</button>
                    </t:emptyState>
                </div>
            </t:card>

            <t:card title="리스트 행 / 아바타 / 진행 바" flush="true">
                <div class="flex items-center gap-3 border-b px-5 py-3">
                    <span class="min-w-11 text-sm font-extrabold text-accent-foreground">19:00</span>
                    <span class="flex size-8 shrink-0 items-center justify-center rounded-md bg-accent text-accent-foreground text-xs font-black">연</span>
                    <div class="min-w-0 flex-1">
                        <p class="text-sm font-bold">전체 연습 — 2막 런스루</p>
                        <p class="mt-0.5 text-xs text-muted-foreground">소극장 · 전원 참석</p>
                    </div>
                    <t:badge tone="accent">오늘</t:badge>
                </div>
                <div class="flex items-center gap-3 px-5 py-3">
                    <span class="flex size-7 items-center justify-center rounded-full bg-sidebar-accent text-xs font-black text-white">김</span>
                    <div class="min-w-0 flex-1">
                        <p class="text-sm font-bold">김반디 — 회비 납부 진행률</p>
                        <div class="mt-2 h-2 overflow-hidden rounded-full bg-secondary">
                            <span class="block h-full rounded-full bg-primary transition-all" style="width:63%"></span>
                        </div>
                    </div>
                    <span class="text-xs font-bold text-muted-foreground">63%</span>
                </div>
            </t:card>

            <t:card title="컨트롤 — 세그먼트 / 체크박스 / 스테퍼">
                <div class="flex flex-wrap items-center gap-6">
                    <div class="inline-flex rounded-lg border bg-secondary p-0.5">
                        <button type="button" class="rounded-md border bg-card px-3 py-1.5 text-xs font-bold text-foreground">주간</button>
                        <button type="button" class="rounded-md px-3 py-1.5 text-xs font-bold text-muted-foreground transition-colors">월간</button>
                        <button type="button" class="rounded-md px-3 py-1.5 text-xs font-bold text-muted-foreground transition-colors">전체</button>
                    </div>
                    <div class="flex items-center gap-2">
                        <button type="button" class="flex size-5 items-center justify-center rounded-md border border-success bg-success text-white" aria-pressed="true">
                            <svg class="size-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><path d="M20 6L9 17l-5-5"/></svg>
                        </button>
                        <button type="button" class="flex size-5 items-center justify-center rounded-md border bg-card text-white" aria-pressed="false"></button>
                        <span class="text-xs font-bold text-muted-foreground">체크박스</span>
                    </div>
                    <div class="flex w-36 items-center justify-between rounded-lg border p-1.5">
                        <button type="button" class="flex size-8 items-center justify-center rounded-md border bg-card text-lg font-extrabold">−</button>
                        <span class="text-sm font-black">2</span>
                        <button type="button" class="flex size-8 items-center justify-center rounded-md border bg-card text-lg font-extrabold">+</button>
                    </div>
                </div>
            </t:card>

            <t:card title="폼 — formField 태그 (검증 오류·flash 토스트·중복 제출 방지·confirm 실동작 데모)">
                <c:url var="styleGuideUrl" value="/style-guide"/>
                <form:form modelAttribute="styleGuideRequest" action="${styleGuideUrl}" method="post" cssClass="max-w-md" data-guard="true">
                    <t:formField label="이름" path="name" required="true"/>
                    <t:formField label="이메일" path="email" type="email" help="비워두면 성공, 형식이 틀리면 오류 상태를 볼 수 있습니다."/>
                    <t:formField label="소개" path="bio" type="textarea"/>
                    <div class="flex gap-2">
                        <button type="submit" class="inline-flex h-9 items-center justify-center rounded-md bg-primary px-4 text-sm font-bold text-primary-foreground transition-colors hover:bg-primary-strong hover:text-white">등록</button>
                        <button type="submit" data-confirm="정말 삭제할까요? 이 동작은 되돌릴 수 없습니다."
                                class="inline-flex h-9 items-center justify-center rounded-md bg-destructive px-4 text-sm font-bold text-destructive-foreground transition-colors hover:bg-destructive/90">삭제 (confirm 데모)</button>
                    </div>
                </form:form>
            </t:card>
        </div>

        <t:modal id="demoModal" title="일정 추가" description="모달 컴포넌트 데모 — 폼이나 확인 메시지를 담는다.">
            <jsp:attribute name="footer">
                <button type="button" data-action="close-modal" class="inline-flex h-9 items-center justify-center rounded-md border bg-card px-4 text-sm font-bold transition-colors hover:bg-secondary">취소</button>
                <button type="button" data-action="close-modal" class="inline-flex h-9 items-center justify-center rounded-md bg-primary px-4 text-sm font-bold text-primary-foreground transition-colors hover:bg-primary-strong hover:text-white">저장</button>
            </jsp:attribute>
            <jsp:body>
                <p class="text-sm text-muted-foreground">본문 영역입니다. 닫기는 우상단 ×, 하단 취소, 바깥 클릭 세 경로 모두 동작합니다.</p>
            </jsp:body>
        </t:modal>
    </jsp:body>
</t:layout>
