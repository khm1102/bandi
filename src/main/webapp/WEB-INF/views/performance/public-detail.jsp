<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="pageTitle" value="${not empty page.ogTitle ? page.ogTitle : page.projectTitle}"/>
<c:set var="heroFileId" value="${not empty page.heroFileId ? page.heroFileId : page.posterFileId}"/>
<c:url var="reservationUrl" value="/reserve/${page.slug}"/>
<c:if test="${not empty heroFileId}">
    <c:url var="heroUrl" value="/api/public-performances/${page.slug}/files/${heroFileId}"/>
</c:if>
<t:layoutPublic title="${pageTitle}" active="performance" scriptPath="performance/public-detail">
    <div data-performance-page data-performance-slug="<c:out value='${page.slug}'/>" class="flex flex-col gap-14 pb-8 md:gap-20">
        <section class="relative -mx-4 -mt-6 min-h-[34rem] overflow-hidden bg-sidebar text-white md:-mx-6 md:-mt-7 md:min-h-[38rem]" aria-labelledby="performanceTitle">
            <c:choose>
                <c:when test="${not empty heroFileId}">
                    <img src="<c:out value='${heroUrl}'/>" alt="<c:out value='${page.projectTitle}'/> 대표 이미지" width="1400" height="900" class="absolute inset-0 size-full object-cover" fetchpriority="high">
                    <div class="absolute inset-0 bg-sidebar/60" aria-hidden="true"></div>
                    <div class="absolute inset-x-0 bottom-0 h-56 bg-linear-to-t from-sidebar to-transparent" aria-hidden="true"></div>
                </c:when>
                <c:otherwise>
                    <div class="absolute inset-0 bg-sidebar" aria-hidden="true"></div>
                    <div class="absolute -right-20 top-24 size-80 rounded-full border border-sidebar-border" aria-hidden="true"></div>
                    <div class="absolute -right-4 top-40 size-52 rounded-full border border-primary/40" aria-hidden="true"></div>
                </c:otherwise>
            </c:choose>
            <div class="relative mx-auto flex min-h-[34rem] max-w-5xl flex-col justify-end px-4 py-10 md:min-h-[38rem] md:px-6 md:py-14">
                <div class="mb-5 flex flex-wrap gap-2">
                    <c:choose>
                        <c:when test="${page.status == 'CANCELLED'}"><t:badge tone="danger">공연 취소</t:badge></c:when>
                        <c:when test="${page.status == 'ENDED' || page.status == 'ARCHIVED'}"><t:badge tone="neutral">공연 기록</t:badge></c:when>
                        <c:when test="${reservationAvailable}"><t:badge tone="success">관람 신청 중</t:badge></c:when>
                        <c:otherwise><t:badge tone="warning">공연 안내</t:badge></c:otherwise>
                    </c:choose>
                    <t:badge tone="neutral"><c:out value="${page.organizerName}"/></t:badge>
                </div>
                <p class="text-xs font-black uppercase tracking-[0.24em] text-primary"><c:out value="${page.genre}"/></p>
                <h1 id="performanceTitle" class="mt-3 max-w-4xl text-4xl font-black leading-tight tracking-[-0.04em] md:text-6xl"><c:out value="${page.projectTitle}"/></h1>
                <p class="mt-4 max-w-2xl text-sm leading-7 text-sidebar-foreground md:text-base"><c:out value="${page.shortDescription}"/></p>
                <div class="mt-7 flex flex-col gap-3 sm:flex-row sm:items-center">
                    <c:if test="${reservationAvailable}">
                        <a href="<c:out value='${reservationUrl}'/>" class="inline-flex min-h-12 items-center justify-center rounded-md bg-primary px-5 text-sm font-black text-primary-foreground transition-colors hover:bg-white hover:text-sidebar">관람 신청하기</a>
                    </c:if>
                    <a href="#performanceSchedule" class="inline-flex min-h-12 items-center justify-center rounded-md border border-sidebar-border px-5 text-sm font-bold text-white transition-colors hover:bg-sidebar-accent">공연 일정 확인</a>
                </div>
            </div>
        </section>

        <section aria-labelledby="performanceFactsTitle">
            <div class="mb-5 flex items-end justify-between gap-4">
                <div>
                    <p class="text-xs font-black uppercase tracking-[0.18em] text-accent-foreground">At a glance</p>
                    <h2 id="performanceFactsTitle" class="mt-2 text-2xl font-black tracking-tight">공연 정보</h2>
                </div>
                <c:if test="${not empty page.contactChannel}"><span class="hidden text-xs text-muted-foreground md:block">문의 · <c:out value="${page.contactName}"/></span></c:if>
            </div>
            <dl class="grid border-y sm:grid-cols-2 lg:grid-cols-4">
                <div class="border-b py-5 sm:border-r lg:border-b-0"><dt class="text-xs font-bold text-muted-foreground">공연 기간</dt><dd class="mt-2 text-sm font-black"><time datetime="<c:out value='${page.productionStartDate}'/>"><c:out value="${page.productionStartDate}"/></time><br><span class="text-muted-foreground">—</span> <time datetime="<c:out value='${page.productionEndDate}'/>"><c:out value="${page.productionEndDate}"/></time></dd></div>
                <div class="border-b py-5 sm:pl-5 lg:border-b-0 lg:border-r"><dt class="text-xs font-bold text-muted-foreground">장소</dt><dd class="mt-2 text-sm font-black"><c:out value="${page.place}"/></dd></div>
                <div class="border-b py-5 sm:border-b-0 lg:border-r lg:pl-5"><dt class="text-xs font-bold text-muted-foreground">관람 정보</dt><dd class="mt-2 text-sm font-black"><c:out value="${page.ageRating}"/> · <c:out value="${page.runtimeMinutes}"/>분<c:if test="${not empty page.intermissionMinutes}"><span class="block text-xs font-bold text-muted-foreground">인터미션 <c:out value="${page.intermissionMinutes}"/>분</span></c:if></dd></div>
                <div class="py-5 sm:pl-5"><dt class="text-xs font-bold text-muted-foreground">관람료</dt><dd class="mt-2 text-sm font-black"><c:choose><c:when test="${page.admissionFee == 0}">무료</c:when><c:otherwise><fmt:formatNumber value="${page.admissionFee}"/>원</c:otherwise></c:choose></dd></div>
            </dl>
        </section>

        <section class="grid gap-8 lg:grid-cols-[0.75fr_1.25fr]" aria-labelledby="synopsisTitle">
            <div>
                <p class="text-xs font-black uppercase tracking-[0.18em] text-accent-foreground">The story</p>
                <h2 id="synopsisTitle" class="mt-2 text-3xl font-black tracking-tight">작품 소개</h2>
                <p class="mt-4 text-sm leading-7 text-muted-foreground"><c:out value="${page.shortDescription}"/></p>
            </div>
            <div class="border-l-2 border-primary pl-6 md:pl-8">
                <h3 class="text-sm font-black">시놉시스</h3>
                <p class="mt-3 whitespace-pre-line text-sm leading-8 text-foreground/85"><c:out value="${page.synopsis}"/></p>
                <c:if test="${not empty page.directorNote}">
                    <div class="mt-8 border-t pt-6">
                        <h3 class="text-sm font-black">연출 노트</h3>
                        <p class="mt-3 whitespace-pre-line text-sm leading-8 text-muted-foreground"><c:out value="${page.directorNote}"/></p>
                    </div>
                </c:if>
            </div>
        </section>

        <c:if test="${not empty casts}">
            <section aria-labelledby="castBoardTitle">
                <p class="text-xs font-black uppercase tracking-[0.18em] text-accent-foreground">Cast board</p>
                <h2 id="castBoardTitle" class="mt-2 text-3xl font-black tracking-tight">등장인물과 배우</h2>
                <p class="mt-3 max-w-2xl text-sm leading-6 text-muted-foreground">작품 전체 캐스팅입니다. 실제 출연자는 아래 회차별 캐스팅에서 다시 확인할 수 있습니다.</p>
                <div class="mt-7 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                    <c:forEach var="cast" items="${casts}">
                        <article class="overflow-hidden rounded-lg border bg-card ${cast.characterImportance == 'LEAD' ? 'sm:col-span-2 lg:col-span-1' : ''}">
                            <c:choose>
                                <c:when test="${not empty cast.profile.profileFileId}">
                                    <c:url var="profileImageUrl" value="/api/public-performances/profiles/${cast.profile.publicProfileId}/files/${cast.profile.profileFileId}"/>
                                    <img src="<c:out value='${profileImageUrl}'/>" alt="<c:out value='${cast.profile.publicName}'/> 프로필" width="720" height="900" loading="lazy" class="aspect-[4/3] w-full object-cover ${cast.characterImportance == 'LEAD' ? 'sm:aspect-[16/9] lg:aspect-[4/3]' : ''}">
                                </c:when>
                                <c:otherwise>
                                    <div class="flex aspect-[4/3] items-end bg-sidebar p-5 text-4xl font-black text-primary" aria-hidden="true"><c:out value="${fn:substring(cast.profile.publicName, 0, 1)}"/></div>
                                </c:otherwise>
                            </c:choose>
                            <div class="p-5">
                                <div class="flex flex-wrap items-center gap-2">
                                    <span class="text-xs font-black uppercase tracking-wider text-accent-foreground"><c:out value="${cast.characterName}"/></span>
                                    <c:if test="${cast.castType != 'PRIMARY'}"><t:badge tone="neutral"><c:out value="${cast.castType == 'ALTERNATE' ? '얼터네이트' : '언더스터디'}"/></t:badge></c:if>
                                </div>
                                <h3 class="mt-2 text-xl font-black"><c:out value="${cast.profile.publicName}"/></h3>
                                <c:if test="${not empty cast.characterDescription}"><p class="mt-2 text-sm leading-6 text-muted-foreground"><c:out value="${cast.characterDescription}"/></p></c:if>
                                <c:if test="${not empty cast.profile.bio}"><p class="mt-3 border-t pt-3 text-xs leading-5 text-muted-foreground"><c:out value="${cast.profile.bio}"/></p></c:if>
                            </div>
                        </article>
                    </c:forEach>
                </div>
            </section>
        </c:if>

        <section id="performanceSchedule" class="scroll-mt-20" aria-labelledby="performanceScheduleTitle">
            <p class="text-xs font-black uppercase tracking-[0.18em] text-accent-foreground">Schedule</p>
            <h2 id="performanceScheduleTitle" class="mt-2 text-3xl font-black tracking-tight">공연 일정과 오늘의 캐스팅</h2>
            <c:choose>
                <c:when test="${empty rounds}">
                    <p class="mt-6 rounded-lg border bg-card p-6 text-sm text-muted-foreground">공개된 공연 회차가 없습니다.</p>
                </c:when>
                <c:otherwise>
                    <div class="mt-6 grid gap-6 lg:grid-cols-[0.85fr_1.15fr]">
                        <div class="flex flex-col gap-2" role="tablist" aria-label="공연 회차 선택">
                            <c:forEach var="round" items="${rounds}" varStatus="status">
                                <button type="button" role="tab" data-round-select data-round-id="<c:out value='${round.performanceRoundId}'/>" aria-selected="${status.first ? 'true' : 'false'}" class="min-h-20 rounded-lg border p-4 text-left transition-colors hover:border-primary ${status.first ? 'border-primary bg-accent' : 'bg-card'}">
                                    <span class="flex flex-wrap items-center gap-2">
                                        <strong class="text-sm font-black"><c:out value="${round.roundNo}"/>회차 · <time datetime="<c:out value='${round.startDttm}'/>"><c:out value="${fn:replace(round.startDttm, 'T', ' ')}"/></time></strong>
                                        <span class="ml-auto text-xs font-bold text-muted-foreground"><c:choose><c:when test="${round.status == 'RESERVATION_OPEN'}">신청 가능</c:when><c:when test="${round.status == 'CANCELLED'}">취소</c:when><c:when test="${round.status == 'ENDED'}">종료</c:when><c:otherwise>신청 마감</c:otherwise></c:choose></span>
                                    </span>
                                    <c:if test="${not empty round.accessibilities}">
                                        <span class="mt-2 flex flex-wrap gap-1.5">
                                            <c:forEach var="accessibility" items="${round.accessibilities}"><t:badge tone="info"><c:out value="${accessibility.title}"/></t:badge></c:forEach>
                                        </span>
                                    </c:if>
                                </button>
                            </c:forEach>
                        </div>
                        <div class="rounded-lg bg-sidebar p-5 text-white md:p-6" role="tabpanel" aria-live="polite" aria-busy="true" data-round-cast-panel>
                            <p class="text-xs font-black uppercase tracking-[0.18em] text-primary">Today&apos;s cast</p>
                            <h3 class="mt-2 text-xl font-black">선택 회차 출연진</h3>
                            <div data-round-cast-list class="mt-5 grid gap-3 sm:grid-cols-2">
                                <p class="text-sm text-sidebar-foreground">회차별 캐스팅을 불러오는 중입니다.</p>
                            </div>
                            <c:if test="${reservationAvailable}">
                                <a data-round-reservation-link href="<c:out value='${reservationUrl}'/>" class="mt-6 inline-flex min-h-11 items-center justify-center rounded-md bg-primary px-4 text-sm font-black text-primary-foreground transition-colors hover:bg-white hover:text-sidebar">관람 신청하기</a>
                            </c:if>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

        <c:if test="${not empty credits}">
            <section class="grid gap-7 lg:grid-cols-[0.65fr_1.35fr]" aria-labelledby="creditsTitle">
                <div>
                    <p class="text-xs font-black uppercase tracking-[0.18em] text-accent-foreground">Creative team</p>
                    <h2 id="creditsTitle" class="mt-2 text-3xl font-black tracking-tight">만든 사람들</h2>
                </div>
                <dl class="divide-y border-y">
                    <c:forEach var="credit" items="${credits}">
                        <div class="grid gap-1 py-3 sm:grid-cols-[10rem_1fr]"><dt class="text-xs font-bold text-muted-foreground"><c:out value="${credit.creditRole}"/></dt><dd class="text-sm font-black"><c:out value="${not empty credit.profile.publicName ? credit.profile.publicName : credit.publicName}"/></dd></div>
                    </c:forEach>
                </dl>
            </section>
        </c:if>

        <c:if test="${not empty media}">
            <section aria-labelledby="mediaTitle">
                <p class="text-xs font-black uppercase tracking-[0.18em] text-accent-foreground">Scenes</p>
                <h2 id="mediaTitle" class="mt-2 text-3xl font-black tracking-tight">공연과 연습실</h2>
                <div class="mt-7 grid gap-4 md:grid-cols-2">
                    <c:forEach var="item" items="${media}">
                        <c:url var="mediaFileUrl" value="/api/public-performances/${page.slug}/files/${item.storedFileId}"/>
                        <figure class="overflow-hidden rounded-lg border bg-card">
                            <img src="<c:out value='${mediaFileUrl}'/>" alt="<c:out value='${item.altText}'/>" width="960" height="640" loading="lazy" class="aspect-[3/2] w-full object-cover">
                            <figcaption class="p-4"><strong class="text-sm font-black"><c:out value="${item.title}"/></strong><c:if test="${not empty item.description}"><p class="mt-1 text-xs leading-5 text-muted-foreground"><c:out value="${item.description}"/></p></c:if><c:if test="${not empty item.creditText}"><span class="mt-2 block text-xs text-muted-foreground">제작 · <c:out value="${item.creditText}"/></span></c:if><c:if test="${item.mediaType == 'VIDEO' && not empty item.externalUrl}"><c:url var="externalMediaUrl" value="${item.externalUrl}"/><a href="<c:out value='${externalMediaUrl}'/>" target="_blank" rel="noopener noreferrer" class="mt-3 inline-flex min-h-11 items-center text-xs font-black text-accent-foreground underline-offset-4 hover:underline">자막이 있는 영상 보기</a></c:if></figcaption>
                        </figure>
                    </c:forEach>
                </div>
            </section>
        </c:if>

        <section class="grid gap-7 border-t pt-10 lg:grid-cols-[0.7fr_1.3fr]" aria-labelledby="viewingGuideTitle">
            <div>
                <p class="text-xs font-black uppercase tracking-[0.18em] text-accent-foreground">Before you arrive</p>
                <h2 id="viewingGuideTitle" class="mt-2 text-3xl font-black tracking-tight">관람 안내</h2>
                <p class="mt-3 text-sm leading-6 text-muted-foreground"><c:out value="${page.place}"/> · <c:out value="${page.contactName}"/></p>
                <c:if test="${not empty page.contactChannel}"><p class="mt-1 text-sm font-bold"><c:out value="${page.contactChannel}"/></p></c:if>
            </div>
            <c:choose>
                <c:when test="${not empty viewingGuide}">
                    <dl class="grid gap-4 sm:grid-cols-2">
                        <c:if test="${not empty viewingGuide.entryPolicy}"><div class="rounded-lg border bg-card p-4"><dt class="text-xs font-black">입장 안내</dt><dd class="mt-2 whitespace-pre-line text-sm leading-6 text-muted-foreground"><c:out value="${viewingGuide.entryPolicy}"/></dd></div></c:if>
                        <c:if test="${not empty viewingGuide.lateEntryPolicy}"><div class="rounded-lg border bg-card p-4"><dt class="text-xs font-black">지연 입장</dt><dd class="mt-2 whitespace-pre-line text-sm leading-6 text-muted-foreground"><c:out value="${viewingGuide.lateEntryPolicy}"/></dd></div></c:if>
                        <c:if test="${not empty viewingGuide.recordingPolicy}"><div class="rounded-lg border bg-card p-4"><dt class="text-xs font-black">촬영·녹음</dt><dd class="mt-2 whitespace-pre-line text-sm leading-6 text-muted-foreground"><c:out value="${viewingGuide.recordingPolicy}"/></dd></div></c:if>
                        <c:if test="${not empty viewingGuide.cancellationPolicy}"><div class="rounded-lg border bg-card p-4"><dt class="text-xs font-black">신청 취소</dt><dd class="mt-2 whitespace-pre-line text-sm leading-6 text-muted-foreground"><c:out value="${viewingGuide.cancellationPolicy}"/></dd></div></c:if>
                        <c:if test="${not empty viewingGuide.accessibilityPolicy}"><div class="rounded-lg border bg-card p-4"><dt class="text-xs font-black">접근성</dt><dd class="mt-2 whitespace-pre-line text-sm leading-6 text-muted-foreground"><c:out value="${viewingGuide.accessibilityPolicy}"/></dd></div></c:if>
                        <c:if test="${not empty viewingGuide.directions}"><div class="rounded-lg border bg-card p-4"><dt class="text-xs font-black">오시는 길</dt><dd class="mt-2 whitespace-pre-line text-sm leading-6 text-muted-foreground"><c:out value="${viewingGuide.directions}"/></dd></div></c:if>
                        <c:if test="${not empty viewingGuide.parkingInformation}"><div class="rounded-lg border bg-card p-4"><dt class="text-xs font-black">주차</dt><dd class="mt-2 whitespace-pre-line text-sm leading-6 text-muted-foreground"><c:out value="${viewingGuide.parkingInformation}"/></dd></div></c:if>
                    </dl>
                </c:when>
                <c:otherwise><p class="rounded-lg border bg-card p-5 text-sm text-muted-foreground">상세 관람 안내는 공시에서 확인해 주세요.</p></c:otherwise>
            </c:choose>
        </section>

        <section class="rounded-xl bg-sidebar px-5 py-8 text-white md:px-8 md:py-10">
            <div class="flex flex-col gap-5 md:flex-row md:items-center md:justify-between">
                <div><p class="text-xs font-black uppercase tracking-[0.18em] text-primary">Official notice</p><h2 class="mt-2 text-2xl font-black">변경 사항은 공시에서 확인하세요</h2><p class="mt-2 text-sm text-sidebar-foreground">캐스팅, 공연 시간, 입장 방법과 취소 관련 변경을 공식 공시로 안내합니다.</p></div>
                <a href="<c:url value='/notices'/>" class="inline-flex min-h-11 shrink-0 items-center justify-center rounded-md border border-sidebar-border px-4 text-sm font-black text-white transition-colors hover:bg-sidebar-accent">공시 확인</a>
            </div>
        </section>
    </div>
</t:layoutPublic>
