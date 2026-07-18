package kr.ac.tukorea.bandi.domain.calendar.mapper;

import kr.ac.tukorea.bandi.domain.calendar.dto.request.CalendarEventSearchCondition;
import kr.ac.tukorea.bandi.domain.calendar.model.CalendarEvent;
import kr.ac.tukorea.bandi.domain.member.mapper.CohortMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.MemberMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.TeamMapper;
import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.Cohort;
import kr.ac.tukorea.bandi.domain.member.model.CohortTerm;
import kr.ac.tukorea.bandi.domain.member.model.Member;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.domain.member.model.SsoLinkStatus;
import kr.ac.tukorea.bandi.domain.member.model.Team;
import kr.ac.tukorea.bandi.global.annotation.MapperTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@MapperTest
class CalendarEventMapperTest {

    private static final LocalDateTime JULY_START = LocalDateTime.of(2026, 7, 1, 0, 0);
    private static final LocalDateTime AUGUST_START = LocalDateTime.of(2026, 8, 1, 0, 0);

    private final CalendarEventMapper calendarEventMapper;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private final MemberMapper memberMapper;
    private final JdbcTemplate jdbcTemplate;

    private Long stageTeamId;
    private Long operatorTeamId;
    private Long actorMemberId;

    @Autowired
    CalendarEventMapperTest(CalendarEventMapper calendarEventMapper, TeamMapper teamMapper,
                            CohortMapper cohortMapper, MemberMapper memberMapper,
                            JdbcTemplate jdbcTemplate) {
        this.calendarEventMapper = calendarEventMapper;
        this.teamMapper = teamMapper;
        this.cohortMapper = cohortMapper;
        this.memberMapper = memberMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void setUp() {
        List<Team> teams = teamMapper.searchAll();
        stageTeamId = teams.stream().filter(team -> team.getName().equals("무대팀"))
                .findFirst().orElseThrow().getTeamId();
        operatorTeamId = teams.stream().filter(team -> team.getName().equals("오퍼팀"))
                .findFirst().orElseThrow().getTeamId();

        Cohort cohort = new Cohort(null, "26-2기", (short) 2026, CohortTerm.SECOND, true);
        cohortMapper.insert(cohort);
        Member actor = new Member(null, "2021184000", "김하늘", null, null, null,
                stageTeamId, cohort.getCohortId(), ClubRole.ADMIN, MemberStatus.ACTIVE,
                SsoLinkStatus.LINKED, null, null, null);
        memberMapper.insert(actor);
        actorMemberId = actor.getMemberId();
    }

    @Test
    void 일정을_저장하고_단건_조회한다() {
        CalendarEvent event = event(stageTeamId, "무대 연습", JULY_START.plusDays(1));

        calendarEventMapper.insert(event);
        CalendarEvent found = calendarEventMapper.lookupById(event.getCalendarEventId())
                .orElseThrow();

        assertThat(event.getCalendarEventId()).isNotNull();
        assertThat(found.getTeamId()).isEqualTo(stageTeamId);
        assertThat(found.getTitle()).isEqualTo("무대 연습");
        assertThat(found.getCreatedDttm()).isNotNull();
    }

    @Test
    void 조회_기간과_겹치는_월_경계_일정을_조회한다() {
        calendarEventMapper.insert(event(null, "월초 전체 일정", JULY_START.minusHours(1),
                JULY_START.plusHours(1)));
        calendarEventMapper.insert(event(stageTeamId, "월말 무대 일정",
                AUGUST_START.minusHours(1), AUGUST_START.plusHours(1)));
        calendarEventMapper.insert(event(stageTeamId, "다음 달 일정",
                AUGUST_START, AUGUST_START.plusHours(2)));

        List<CalendarEvent> found = calendarEventMapper.searchOverlapping(
                new CalendarEventSearchCondition(JULY_START, AUGUST_START, null));

        assertThat(found).extracting(CalendarEvent::getTitle)
                .containsExactly("월초 전체 일정", "월말 무대 일정");
    }

    @Test
    void 팀_필터는_전체_일정과_해당_팀_일정만_조회한다() {
        calendarEventMapper.insert(event(null, "전체 리허설", JULY_START.plusDays(1)));
        calendarEventMapper.insert(event(stageTeamId, "무대 연습", JULY_START.plusDays(2)));
        calendarEventMapper.insert(event(operatorTeamId, "오퍼 연습", JULY_START.plusDays(3)));

        List<CalendarEvent> found = calendarEventMapper.searchOverlapping(
                new CalendarEventSearchCondition(JULY_START, AUGUST_START, stageTeamId));

        assertThat(found).extracting(CalendarEvent::getTitle)
                .containsExactly("전체 리허설", "무대 연습");
    }

    @Test
    void 일정을_수정하면_내용과_최종_수정자가_갱신된다() {
        CalendarEvent original = event(stageTeamId, "무대 연습", JULY_START.plusDays(1));
        calendarEventMapper.insert(original);
        CalendarEvent changed = original.change(operatorTeamId, "오퍼 연습", "음향 큐 연습",
                JULY_START.plusDays(2), JULY_START.plusDays(2).plusHours(2),
                false, "대학극장", actorMemberId);

        int affected = calendarEventMapper.update(changed);

        assertThat(affected).isEqualTo(1);
        assertThat(calendarEventMapper.lookupById(original.getCalendarEventId()))
                .isPresent()
                .get()
                .satisfies(found -> {
                    assertThat(found.getTeamId()).isEqualTo(operatorTeamId);
                    assertThat(found.getTitle()).isEqualTo("오퍼 연습");
                    assertThat(found.getUpdatedByMemberId()).isEqualTo(actorMemberId);
                });
    }

    @Test
    void 삭제하면_논리_삭제되어_조회에서_제외된다() {
        CalendarEvent event = event(stageTeamId, "무대 연습", JULY_START.plusDays(1));
        calendarEventMapper.insert(event);

        int affected = calendarEventMapper.delete(event.getCalendarEventId(), actorMemberId,
                JULY_START.plusDays(5));

        assertThat(affected).isEqualTo(1);
        assertThat(calendarEventMapper.lookupById(event.getCalendarEventId())).isEmpty();
        assertThat(calendarEventMapper.searchOverlapping(
                new CalendarEventSearchCondition(JULY_START, AUGUST_START, null))).isEmpty();
    }

    @Test
    void 삭제된_일정은_다시_수정하거나_삭제할_수_없다() {
        CalendarEvent event = event(stageTeamId, "무대 연습", JULY_START.plusDays(1));
        calendarEventMapper.insert(event);
        calendarEventMapper.delete(event.getCalendarEventId(), actorMemberId,
                JULY_START.plusDays(5));

        assertThat(calendarEventMapper.update(event)).isZero();
        assertThat(calendarEventMapper.delete(event.getCalendarEventId(), actorMemberId,
                JULY_START.plusDays(6))).isZero();
    }

    @Test
    void DB도_종료가_시작보다_빠른_일정을_거부한다() {
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO calendar_event (
                    team_id, title, description, start_dttm, end_dttm, is_all_day, place,
                    created_by_member_id, updated_by_member_id
                ) VALUES (?, '잘못된 일정', '설명', ?, ?, 0, '장소', ?, ?)
                """, stageTeamId, JULY_START.plusHours(2), JULY_START,
                actorMemberId, actorMemberId))
                .isInstanceOf(DataAccessException.class);
    }

    private CalendarEvent event(Long teamId, String title, LocalDateTime start) {
        return event(teamId, title, start, start.plusHours(2));
    }

    private CalendarEvent event(Long teamId, String title, LocalDateTime start,
                                LocalDateTime end) {
        return CalendarEvent.create(teamId, title, "일정 설명", start, end,
                false, "학생회관 소극장", actorMemberId);
    }
}
