package kr.ac.tukorea.bandi.domain.event.service;

import kr.ac.tukorea.bandi.domain.calendar.dto.request.CalendarEventCreateParam;
import kr.ac.tukorea.bandi.domain.calendar.dto.request.CalendarEventUpdateParam;
import kr.ac.tukorea.bandi.domain.calendar.service.CalendarService;
import kr.ac.tukorea.bandi.domain.event.dto.request.AttendanceProcessParam;
import kr.ac.tukorea.bandi.domain.event.dto.request.ClubEventCreateParam;
import kr.ac.tukorea.bandi.domain.event.dto.request.ClubEventSearchCondition;
import kr.ac.tukorea.bandi.domain.event.dto.request.ClubEventUpdateParam;
import kr.ac.tukorea.bandi.domain.event.dto.request.EventTargetConfirmParam;
import kr.ac.tukorea.bandi.domain.event.dto.response.ClubEventResponse;
import kr.ac.tukorea.bandi.domain.event.exception.ClubEventAccessDeniedException;
import kr.ac.tukorea.bandi.domain.event.exception.EventAttendanceNotFoundException;
import kr.ac.tukorea.bandi.domain.event.exception.InvalidClubEventException;
import kr.ac.tukorea.bandi.domain.event.exception.InvalidClubEventStateException;
import kr.ac.tukorea.bandi.domain.event.mapper.ClubEventMapper;
import kr.ac.tukorea.bandi.domain.event.model.AttendanceStatus;
import kr.ac.tukorea.bandi.domain.event.model.ClubEvent;
import kr.ac.tukorea.bandi.domain.event.model.ClubEventStatus;
import kr.ac.tukorea.bandi.domain.event.model.EventAttendance;
import kr.ac.tukorea.bandi.domain.event.model.EventAttendanceHistory;
import kr.ac.tukorea.bandi.domain.event.model.EventTargetScope;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ClubEventServiceTest {

    private static final Long ACTOR_ID = 1L;
    private static final Long EVENT_ID = 10L;
    private static final Long CALENDAR_ID = 20L;
    private static final Long TEAM_ID = 4L;
    private static final LocalDateTime START = LocalDateTime.of(2026, 8, 1, 18, 0);
    private static final LocalDateTime END = START.plusHours(3);
    private static final LocalDateTime CHECK_IN_START = START.minusMinutes(30);
    private static final LocalDateTime CHECK_IN_END = START.plusMinutes(30);
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-08-01T09:00:00Z"), ZoneId.of("Asia/Seoul"));

    @Mock
    private ClubEventMapper clubEventMapper;
    @Mock
    private MemberService memberService;
    @Mock
    private CalendarService calendarService;

    private ClubEventService service;

    @BeforeEach
    void setUp() {
        service = new ClubEventService(
                clubEventMapper, memberService, calendarService, CLOCK);
    }

    @Test
    void ADMIN은_캘린더와_연결된_행사_초안을_생성한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        given(calendarService.create(any(), any())).willReturn(CALENDAR_ID);
        willAnswer(invocation -> {
            assignId(invocation.getArgument(0), "clubEventId", EVENT_ID);
            return 1;
        }).given(clubEventMapper).insert(any());

        Long result = service.create(ACTOR_ID, createParam(EventTargetScope.TEAM, TEAM_ID));

        assertThat(result).isEqualTo(EVENT_ID);
        verify(memberService).validateActiveTeam(TEAM_ID);
        ArgumentCaptor<CalendarEventCreateParam> calendarCaptor =
                ArgumentCaptor.forClass(CalendarEventCreateParam.class);
        verify(calendarService).create(org.mockito.ArgumentMatchers.eq(ACTOR_ID),
                calendarCaptor.capture());
        assertThat(calendarCaptor.getValue().teamId()).isEqualTo(TEAM_ID);
    }

    @Test
    void ADMIN이_아니면_행사를_관리할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext());

        assertThatThrownBy(() -> service.create(ACTOR_ID,
                createParam(EventTargetScope.ALL, null)))
                .isInstanceOf(ClubEventAccessDeniedException.class);

        verify(calendarService, never()).create(any(), any());
    }

    @Test
    void 초안_수정은_연결_캘린더와_행사를_같이_갱신한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        given(clubEventMapper.lookupByIdForUpdate(EVENT_ID))
                .willReturn(Optional.of(event(ClubEventStatus.DRAFT,
                        EventTargetScope.ALL, null)));

        service.update(ACTOR_ID, updateParam(EventTargetScope.TEAM, TEAM_ID));

        verify(memberService).validateActiveTeam(TEAM_ID);
        verify(calendarService).update(org.mockito.ArgumentMatchers.eq(ACTOR_ID),
                any(CalendarEventUpdateParam.class));
        verify(clubEventMapper).update(any(ClubEvent.class));
    }

    @Test
    void ALL과_TEAM_대상은_확정_시점의_활성_멤버로_스냅샷을_생성한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        given(clubEventMapper.lookupByIdForUpdate(EVENT_ID))
                .willReturn(Optional.of(event(ClubEventStatus.DRAFT,
                        EventTargetScope.ALL, null)));
        given(memberService.searchActiveMemberIds(null)).willReturn(List.of(2L, 3L));

        int allCount = service.confirmTargets(ACTOR_ID,
                new EventTargetConfirmParam(EVENT_ID, List.of()));

        assertThat(allCount).isEqualTo(2);
        ArgumentCaptor<List<EventAttendance>> captor = ArgumentCaptor.captor();
        verify(clubEventMapper).insertAttendances(captor.capture());
        assertThat(captor.getValue()).extracting(EventAttendance::getMemberId)
                .containsExactly(2L, 3L);

        given(clubEventMapper.lookupByIdForUpdate(EVENT_ID))
                .willReturn(Optional.of(event(ClubEventStatus.DRAFT,
                        EventTargetScope.TEAM, TEAM_ID)));
        given(memberService.searchActiveMemberIds(TEAM_ID)).willReturn(List.of(4L));
        assertThat(service.confirmTargets(ACTOR_ID,
                new EventTargetConfirmParam(EVENT_ID, null))).isEqualTo(1);
    }

    @Test
    void SELECTED는_중복없이_모두_활성인_멤버만_확정한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        given(clubEventMapper.lookupByIdForUpdate(EVENT_ID))
                .willReturn(Optional.of(event(ClubEventStatus.DRAFT,
                        EventTargetScope.SELECTED, null)));
        given(memberService.searchActiveMemberIds(null)).willReturn(List.of(2L, 3L, 4L));

        assertThat(service.confirmTargets(ACTOR_ID,
                new EventTargetConfirmParam(EVENT_ID, List.of(4L, 2L))))
                .isEqualTo(2);

        assertThatThrownBy(() -> service.confirmTargets(ACTOR_ID,
                new EventTargetConfirmParam(EVENT_ID, List.of(2L, 2L))))
                .isInstanceOf(InvalidClubEventException.class);
        assertThatThrownBy(() -> service.confirmTargets(ACTOR_ID,
                new EventTargetConfirmParam(EVENT_ID, List.of(9L))))
                .isInstanceOf(InvalidClubEventException.class);
    }

    @Test
    void 이미_확정된_행사는_대상_행을_다시_삽입하지_않는다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        given(clubEventMapper.lookupByIdForUpdate(EVENT_ID))
                .willReturn(Optional.of(event(ClubEventStatus.SCHEDULED,
                        EventTargetScope.ALL, null)));

        assertThatThrownBy(() -> service.confirmTargets(ACTOR_ID,
                new EventTargetConfirmParam(EVENT_ID, List.of())))
                .isInstanceOf(InvalidClubEventStateException.class);

        verify(clubEventMapper, never()).insertAttendances(any());
    }

    @Test
    void 출석확인을_열고_닫고_설정시간_안에_다시_연다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        ClubEvent scheduled = event(ClubEventStatus.SCHEDULED,
                EventTargetScope.ALL, null);
        ClubEvent opened = event(ClubEventStatus.IN_PROGRESS,
                EventTargetScope.ALL, null);
        ClubEvent closed = event(ClubEventStatus.CLOSED,
                EventTargetScope.ALL, null);
        given(clubEventMapper.lookupByIdForUpdate(EVENT_ID))
                .willReturn(Optional.of(scheduled), Optional.of(opened),
                        Optional.of(closed));

        service.openCheckIn(ACTOR_ID, EVENT_ID);
        service.closeCheckIn(ACTOR_ID, EVENT_ID);
        service.openCheckIn(ACTOR_ID, EVENT_ID);

        verify(clubEventMapper, times(3)).update(any(ClubEvent.class));
    }

    @Test
    void 열린_행사의_대상을_개별_또는_일괄_처리하고_각각_이력을_남긴다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        given(clubEventMapper.lookupByIdForUpdate(EVENT_ID))
                .willReturn(Optional.of(event(ClubEventStatus.IN_PROGRESS,
                        EventTargetScope.ALL, null)));
        given(clubEventMapper.searchAttendancesByIdsForUpdate(
                EVENT_ID, List.of(101L, 102L))).willReturn(List.of(
                attendance(101L, 2L), attendance(102L, 3L)));

        int count = service.processAttendance(ACTOR_ID,
                new AttendanceProcessParam(EVENT_ID, List.of(101L, 102L),
                        AttendanceStatus.PRESENT, null));

        assertThat(count).isEqualTo(2);
        verify(clubEventMapper, times(2)).updateAttendance(any());
        ArgumentCaptor<EventAttendanceHistory> historyCaptor =
                ArgumentCaptor.forClass(EventAttendanceHistory.class);
        verify(clubEventMapper, times(2))
                .insertAttendanceHistory(historyCaptor.capture());
        assertThat(historyCaptor.getAllValues())
                .extracting(EventAttendanceHistory::getNewStatus)
                .containsOnly(AttendanceStatus.PRESENT);
    }

    @Test
    void 출석_ID가_중복되거나_다른_행사_대상이면_처리하지_않는다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        given(clubEventMapper.lookupByIdForUpdate(EVENT_ID))
                .willReturn(Optional.of(event(ClubEventStatus.IN_PROGRESS,
                        EventTargetScope.ALL, null)));

        assertThatThrownBy(() -> service.processAttendance(ACTOR_ID,
                new AttendanceProcessParam(EVENT_ID, List.of(101L, 101L),
                        AttendanceStatus.PRESENT, null)))
                .isInstanceOf(InvalidClubEventException.class);

        given(clubEventMapper.searchAttendancesByIdsForUpdate(
                EVENT_ID, List.of(101L, 102L)))
                .willReturn(List.of(attendance(101L, 2L)));
        assertThatThrownBy(() -> service.processAttendance(ACTOR_ID,
                new AttendanceProcessParam(EVENT_ID, List.of(101L, 102L),
                        AttendanceStatus.PRESENT, null)))
                .isInstanceOf(EventAttendanceNotFoundException.class);
    }

    @Test
    void 활성_멤버는_행사와_본인_출석을_조회하고_운영진만_명단을_조회한다() {
        ClubEventSearchCondition condition = new ClubEventSearchCondition(
                null, null, null, 0, 20);
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext());
        given(clubEventMapper.search(condition)).willReturn(List.of(eventResponse()));

        assertThat(service.search(ACTOR_ID, condition)).hasSize(1);
        assertThat(service.searchMyAttendances(ACTOR_ID)).isEmpty();
        assertThatThrownBy(() -> service.searchAttendanceRoster(
                ACTOR_ID, EVENT_ID, null))
                .isInstanceOf(ClubEventAccessDeniedException.class);

        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        assertThat(service.searchAttendanceRoster(ACTOR_ID, EVENT_ID, null)).isEmpty();
        assertThat(service.countAttendanceStatuses(ACTOR_ID, EVENT_ID)).isEmpty();
        assertThat(service.searchAttendanceHistories(ACTOR_ID, 101L)).isEmpty();
    }

    private ClubEventCreateParam createParam(EventTargetScope scope, Long teamId) {
        return new ClubEventCreateParam(scope, teamId, "여름 총회", "운영 공유",
                "학생회관", START, END, CHECK_IN_START, CHECK_IN_END);
    }

    private ClubEventUpdateParam updateParam(EventTargetScope scope, Long teamId) {
        return new ClubEventUpdateParam(EVENT_ID, scope, teamId,
                "수정 총회", "수정 안내", "대강당", START, END,
                CHECK_IN_START, CHECK_IN_END);
    }

    private ClubEvent event(ClubEventStatus status,
                            EventTargetScope scope, Long teamId) {
        return new ClubEvent(EVENT_ID, CALENDAR_ID, scope, teamId,
                "여름 총회", "운영 공유", "학생회관", START, END,
                CHECK_IN_START, CHECK_IN_END, status, ACTOR_ID, ACTOR_ID,
                null, null, null);
    }

    private EventAttendance attendance(Long attendanceId, Long memberId) {
        return new EventAttendance(attendanceId, EVENT_ID, memberId,
                AttendanceStatus.PENDING, null, null, null, null, null);
    }

    private ClubEventResponse eventResponse() {
        return new ClubEventResponse(EVENT_ID, CALENDAR_ID, EventTargetScope.ALL,
                null, null, "여름 총회", "운영 공유", "학생회관",
                START, END, CHECK_IN_START, CHECK_IN_END,
                ClubEventStatus.SCHEDULED, 10);
    }

    private MemberAccessContext adminContext() {
        return new MemberAccessContext(ACTOR_ID, TEAM_ID,
                true, false, true);
    }

    private MemberAccessContext memberContext() {
        return new MemberAccessContext(ACTOR_ID, TEAM_ID,
                false, false, true);
    }

    private void assignId(Object target, String fieldName, Long value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
