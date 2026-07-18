package kr.ac.tukorea.bandi.domain.calendar.service;

import kr.ac.tukorea.bandi.domain.calendar.dto.request.CalendarEventCreateParam;
import kr.ac.tukorea.bandi.domain.calendar.dto.request.CalendarEventSearchCondition;
import kr.ac.tukorea.bandi.domain.calendar.dto.request.CalendarEventUpdateParam;
import kr.ac.tukorea.bandi.domain.calendar.dto.response.CalendarEventResponse;
import kr.ac.tukorea.bandi.domain.calendar.exception.CalendarAccessDeniedException;
import kr.ac.tukorea.bandi.domain.calendar.exception.CalendarEventNotFoundException;
import kr.ac.tukorea.bandi.domain.calendar.mapper.CalendarEventMapper;
import kr.ac.tukorea.bandi.domain.calendar.model.CalendarEvent;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CalendarServiceTest {

    private static final Long ACTOR_ID = 1L;
    private static final Long STAGE_TEAM_ID = 4L;
    private static final Long OPERATOR_TEAM_ID = 5L;
    private static final Instant FIXED_INSTANT = Instant.parse("2026-07-20T03:00:00Z");
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final LocalDateTime START = LocalDateTime.of(2026, 7, 20, 18, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 7, 20, 21, 0);

    @Mock
    private CalendarEventMapper calendarEventMapper;
    @Mock
    private MemberService memberService;

    private CalendarService calendarService;

    @BeforeEach
    void setUp() {
        calendarService = new CalendarService(calendarEventMapper, memberService,
                Clock.fixed(FIXED_INSTANT, SEOUL));
    }

    @Test
    void 활성_MEMBER는_전체_캘린더를_조회할_수_있다() {
        CalendarEventSearchCondition condition = condition();
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext(false, false, true));
        given(calendarEventMapper.searchOverlapping(condition))
                .willReturn(List.of(event(null, "전체 리허설"), event(STAGE_TEAM_ID, "무대 연습")));

        List<CalendarEventResponse> result = calendarService.search(ACTOR_ID, condition);

        assertThat(result).extracting(CalendarEventResponse::title)
                .containsExactly("전체 리허설", "무대 연습");
    }

    @Test
    void 비활성_멤버는_캘린더를_조회할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext(true, false, false));

        assertThatThrownBy(() -> calendarService.search(ACTOR_ID, condition()))
                .isInstanceOf(CalendarAccessDeniedException.class);
        verify(calendarEventMapper, never()).searchOverlapping(any());
    }

    @Test
    void LEADER는_본인_팀_일정을_생성할_수_있다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext(false, true, true));

        calendarService.create(ACTOR_ID, createParam(STAGE_TEAM_ID));

        verify(memberService).validateActiveTeam(STAGE_TEAM_ID);
        ArgumentCaptor<CalendarEvent> captor = ArgumentCaptor.forClass(CalendarEvent.class);
        verify(calendarEventMapper).insert(captor.capture());
        assertThat(captor.getValue().getTeamId()).isEqualTo(STAGE_TEAM_ID);
        assertThat(captor.getValue().getCreatedByMemberId()).isEqualTo(ACTOR_ID);
    }

    @Test
    void LEADER는_다른_팀이나_전체_일정을_생성할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext(false, true, true));

        assertThatThrownBy(() -> calendarService.create(ACTOR_ID, createParam(OPERATOR_TEAM_ID)))
                .isInstanceOf(CalendarAccessDeniedException.class);
        assertThatThrownBy(() -> calendarService.create(ACTOR_ID, createParam(null)))
                .isInstanceOf(CalendarAccessDeniedException.class);
        verify(calendarEventMapper, never()).insert(any());
    }

    @Test
    void MEMBER는_일정을_생성할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext(false, false, true));

        assertThatThrownBy(() -> calendarService.create(ACTOR_ID, createParam(STAGE_TEAM_ID)))
                .isInstanceOf(CalendarAccessDeniedException.class);
        verify(calendarEventMapper, never()).insert(any());
    }

    @Test
    void ADMIN은_전체_일정을_생성할_수_있다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext(true, false, true));

        calendarService.create(ACTOR_ID, createParam(null));

        verify(memberService, never()).validateActiveTeam(any());
        verify(calendarEventMapper).insert(any());
    }

    @Test
    void ADMIN도_팀_일정을_생성할_때는_활성_팀인지_검증한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext(true, false, true));

        calendarService.create(ACTOR_ID, createParam(OPERATOR_TEAM_ID));

        verify(memberService).validateActiveTeam(OPERATOR_TEAM_ID);
        verify(calendarEventMapper).insert(any());
    }

    @Test
    void LEADER는_본인_팀_일정을_수정할_수_있다() {
        CalendarEvent original = event(STAGE_TEAM_ID, "무대 연습");
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext(false, true, true));
        given(calendarEventMapper.lookupByIdForUpdate(10L)).willReturn(Optional.of(original));

        calendarService.update(ACTOR_ID, updateParam(10L, STAGE_TEAM_ID));

        verify(memberService).validateActiveTeam(STAGE_TEAM_ID);
        ArgumentCaptor<CalendarEvent> captor = ArgumentCaptor.forClass(CalendarEvent.class);
        verify(calendarEventMapper).update(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("수정된 일정");
        assertThat(captor.getValue().getUpdatedByMemberId()).isEqualTo(ACTOR_ID);
    }

    @Test
    void LEADER는_다른_팀_일정을_본인_팀으로_바꾸는_우회를_할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext(false, true, true));
        given(calendarEventMapper.lookupByIdForUpdate(10L))
                .willReturn(Optional.of(event(OPERATOR_TEAM_ID, "오퍼 연습")));

        assertThatThrownBy(() -> calendarService.update(
                ACTOR_ID, updateParam(10L, STAGE_TEAM_ID)))
                .isInstanceOf(CalendarAccessDeniedException.class);
        verify(calendarEventMapper, never()).update(any());
    }

    @Test
    void LEADER는_본인_팀_일정을_전체_일정으로_바꿀_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext(false, true, true));
        given(calendarEventMapper.lookupByIdForUpdate(10L))
                .willReturn(Optional.of(event(STAGE_TEAM_ID, "무대 연습")));

        assertThatThrownBy(() -> calendarService.update(ACTOR_ID, updateParam(10L, null)))
                .isInstanceOf(CalendarAccessDeniedException.class);
        verify(calendarEventMapper, never()).update(any());
    }

    @Test
    void 존재하지_않는_일정은_수정할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext(true, false, true));
        given(calendarEventMapper.lookupByIdForUpdate(10L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> calendarService.update(
                ACTOR_ID, updateParam(10L, STAGE_TEAM_ID)))
                .isInstanceOf(CalendarEventNotFoundException.class);
    }

    @Test
    void LEADER는_본인_팀_일정을_삭제할_수_있다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext(false, true, true));
        given(calendarEventMapper.lookupByIdForUpdate(10L))
                .willReturn(Optional.of(event(STAGE_TEAM_ID, "무대 연습")));

        calendarService.delete(ACTOR_ID, 10L);

        verify(calendarEventMapper).delete(10L, ACTOR_ID,
                LocalDateTime.ofInstant(FIXED_INSTANT, SEOUL));
    }

    @Test
    void LEADER는_다른_팀_일정을_삭제할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext(false, true, true));
        given(calendarEventMapper.lookupByIdForUpdate(10L))
                .willReturn(Optional.of(event(OPERATOR_TEAM_ID, "오퍼 연습")));

        assertThatThrownBy(() -> calendarService.delete(ACTOR_ID, 10L))
                .isInstanceOf(CalendarAccessDeniedException.class);
        verify(calendarEventMapper, never()).delete(any(), any(), any());
    }

    private MemberAccessContext memberContext(boolean admin, boolean leader, boolean active) {
        return new MemberAccessContext(ACTOR_ID, STAGE_TEAM_ID, admin, leader, active);
    }

    private CalendarEventCreateParam createParam(Long teamId) {
        return new CalendarEventCreateParam(teamId, "무대 연습", "전체 장면 연습",
                START, END, false, "학생회관 소극장");
    }

    private CalendarEventUpdateParam updateParam(Long calendarEventId, Long teamId) {
        return new CalendarEventUpdateParam(calendarEventId, teamId, "수정된 일정",
                "수정된 설명", START.plusDays(1), END.plusDays(1), false, "대학극장");
    }

    private CalendarEventSearchCondition condition() {
        return new CalendarEventSearchCondition(START.minusMonths(1), END.plusMonths(1), null);
    }

    private CalendarEvent event(Long teamId, String title) {
        return new CalendarEvent(10L, teamId, title, "일정 설명", START, END,
                false, "학생회관 소극장", ACTOR_ID, ACTOR_ID,
                START.minusDays(1), START.minusDays(1), null);
    }
}
