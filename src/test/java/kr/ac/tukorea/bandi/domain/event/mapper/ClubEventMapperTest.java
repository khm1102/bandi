package kr.ac.tukorea.bandi.domain.event.mapper;

import kr.ac.tukorea.bandi.domain.calendar.mapper.CalendarEventMapper;
import kr.ac.tukorea.bandi.domain.calendar.model.CalendarEvent;
import kr.ac.tukorea.bandi.domain.event.dto.request.ClubEventSearchCondition;
import kr.ac.tukorea.bandi.domain.event.dto.response.AttendanceStatusCountResponse;
import kr.ac.tukorea.bandi.domain.event.dto.response.EventAttendanceResponse;
import kr.ac.tukorea.bandi.domain.event.model.AttendanceStatus;
import kr.ac.tukorea.bandi.domain.event.model.ClubEvent;
import kr.ac.tukorea.bandi.domain.event.model.ClubEventStatus;
import kr.ac.tukorea.bandi.domain.event.model.EventAttendance;
import kr.ac.tukorea.bandi.domain.event.model.EventAttendanceHistory;
import kr.ac.tukorea.bandi.domain.event.model.EventTargetScope;
import kr.ac.tukorea.bandi.domain.member.mapper.CohortMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.MemberMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.TeamMapper;
import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.Cohort;
import kr.ac.tukorea.bandi.domain.member.model.CohortTerm;
import kr.ac.tukorea.bandi.domain.member.model.Member;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.domain.member.model.SsoLinkStatus;
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
class ClubEventMapperTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 8, 1, 18, 0);
    private static final LocalDateTime END = START.plusHours(3);
    private static final LocalDateTime CHECK_IN_START = START.minusMinutes(30);
    private static final LocalDateTime CHECK_IN_END = START.plusMinutes(30);

    private final ClubEventMapper mapper;
    private final CalendarEventMapper calendarEventMapper;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private final MemberMapper memberMapper;
    private final JdbcTemplate jdbcTemplate;

    private Long teamId;
    private Long adminId;
    private Long memberId;

    @Autowired
    ClubEventMapperTest(ClubEventMapper mapper,
                        CalendarEventMapper calendarEventMapper,
                        TeamMapper teamMapper, CohortMapper cohortMapper,
                        MemberMapper memberMapper, JdbcTemplate jdbcTemplate) {
        this.mapper = mapper;
        this.calendarEventMapper = calendarEventMapper;
        this.teamMapper = teamMapper;
        this.cohortMapper = cohortMapper;
        this.memberMapper = memberMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void setUp() {
        teamId = teamMapper.searchAll().stream()
                .filter(team -> team.getName().equals("무대팀"))
                .findFirst().orElseThrow().getTeamId();
        Cohort cohort = new Cohort(null, "26-행사", (short) 2026,
                CohortTerm.SECOND, true);
        cohortMapper.insert(cohort);
        adminId = insertMember("2026000301", "운영진", cohort.getCohortId(),
                ClubRole.ADMIN);
        memberId = insertMember("2026000302", "참석자", cohort.getCohortId(),
                ClubRole.MEMBER);
    }

    @Test
    void 캘린더와_연결된_행사를_저장하고_목록으로_조회한다() {
        ClubEvent event = insertDraft(EventTargetScope.TEAM, teamId);

        assertThat(mapper.lookupById(event.getClubEventId()))
                .isPresent().get()
                .extracting(ClubEvent::getCalendarEventId)
                .isEqualTo(event.getCalendarEventId());
        assertThat(mapper.search(new ClubEventSearchCondition(
                ClubEventStatus.DRAFT, START.minusDays(1),
                END.plusDays(1), 0, 20)))
                .singleElement().satisfies(response -> {
                    assertThat(response.teamName()).isEqualTo("무대팀");
                    assertThat(response.targetCount()).isZero();
                });
    }

    @Test
    void DB는_대상범위와_시간과_캘린더_중복을_제약한다() {
        Long calendarEventId = insertCalendar();
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO club_event (
                    calendar_event_id, target_scope_code, team_id, title,
                    place, start_dttm, end_dttm, check_in_start_dttm,
                    check_in_end_dttm, status_code,
                    created_by_member_id, updated_by_member_id
                ) VALUES (?, 'TEAM', NULL, '오류', '장소', ?, ?, ?, ?,
                          'DRAFT', ?, ?)
                """, calendarEventId, START, END, CHECK_IN_START,
                CHECK_IN_END, adminId, adminId))
                .isInstanceOf(DataAccessException.class);
        ClubEvent first = event(calendarEventId, EventTargetScope.ALL, null);
        mapper.insert(first);
        assertThatThrownBy(() -> mapper.insert(
                event(calendarEventId, EventTargetScope.ALL, null)))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void 행사_대상_스냅샷은_멤버별로_한_행만_저장한다() {
        ClubEvent event = insertDraft(EventTargetScope.ALL, null);
        mapper.insertAttendances(List.of(
                EventAttendance.pending(event.getClubEventId(), adminId),
                EventAttendance.pending(event.getClubEventId(), memberId)));

        assertThat(mapper.searchAttendanceRoster(event.getClubEventId(), null))
                .extracting(EventAttendanceResponse::memberName)
                .containsExactly("운영진", "참석자");
        assertThatThrownBy(() -> mapper.insertAttendances(List.of(
                EventAttendance.pending(event.getClubEventId(), memberId))))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void 출석_처리와_이력을_저장하고_상태별_집계와_멤버_이력을_조회한다() {
        ClubEvent event = insertDraft(EventTargetScope.ALL, null);
        mapper.insertAttendances(List.of(
                EventAttendance.pending(event.getClubEventId(), memberId)));
        Long attendanceId = jdbcTemplate.queryForObject("""
                SELECT event_attendance_id
                FROM event_attendance
                WHERE club_event_id = ? AND member_id = ?
                """, Long.class, event.getClubEventId(), memberId);
        EventAttendance pending = mapper.searchAttendancesByIdsForUpdate(
                event.getClubEventId(), List.of(attendanceId)).get(0);
        EventAttendance present = pending.changeStatus(
                AttendanceStatus.PRESENT, adminId, START, null);
        mapper.updateAttendance(present);
        mapper.insertAttendanceHistory(EventAttendanceHistory.change(
                attendanceId, AttendanceStatus.PENDING,
                AttendanceStatus.PRESENT, null, adminId, START));

        assertThat(mapper.searchAttendanceRoster(event.getClubEventId(),
                AttendanceStatus.PRESENT)).singleElement()
                .extracting(EventAttendanceResponse::processedByName)
                .isEqualTo("운영진");
        assertThat(mapper.countAttendanceStatuses(event.getClubEventId()))
                .extracting(AttendanceStatusCountResponse::status,
                        AttendanceStatusCountResponse::count)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(
                        AttendanceStatus.PRESENT, 1));
        assertThat(mapper.searchAttendanceHistories(attendanceId))
                .singleElement().satisfies(history ->
                        assertThat(history.changedByName()).isEqualTo("운영진"));
        assertThat(mapper.searchMemberAttendances(memberId))
                .singleElement().satisfies(history ->
                        assertThat(history.status()).isEqualTo(
                                AttendanceStatus.PRESENT));
    }

    @Test
    void 논리_삭제된_행사는_모든_행사_조회에서_제외한다() {
        ClubEvent event = insertDraft(EventTargetScope.ALL, null);
        mapper.insertAttendances(List.of(
                EventAttendance.pending(event.getClubEventId(), memberId)));
        Long attendanceId = jdbcTemplate.queryForObject("""
                SELECT event_attendance_id FROM event_attendance
                WHERE club_event_id = ? AND member_id = ?
                """, Long.class, event.getClubEventId(), memberId);
        mapper.insertAttendanceHistory(EventAttendanceHistory.change(
                attendanceId, AttendanceStatus.PENDING,
                AttendanceStatus.PRESENT, null, adminId, START));
        jdbcTemplate.update("""
                UPDATE club_event SET deleted_dttm = NOW(6)
                WHERE club_event_id = ?
                """, event.getClubEventId());

        assertThat(mapper.lookupById(event.getClubEventId())).isEmpty();
        assertThat(mapper.search(new ClubEventSearchCondition(
                null, null, null, 0, 20))).isEmpty();
        assertThat(mapper.searchAttendanceRoster(
                event.getClubEventId(), null)).isEmpty();
        assertThat(mapper.countAttendanceStatuses(
                event.getClubEventId())).isEmpty();
        assertThat(mapper.searchAttendanceHistories(attendanceId)).isEmpty();
        assertThat(mapper.searchMemberAttendances(memberId)).isEmpty();
    }

    private ClubEvent insertDraft(EventTargetScope scope, Long targetTeamId) {
        ClubEvent event = event(insertCalendar(), scope, targetTeamId);
        mapper.insert(event);
        return event;
    }

    private ClubEvent event(Long calendarEventId, EventTargetScope scope,
                            Long targetTeamId) {
        return ClubEvent.draft(calendarEventId, scope, targetTeamId,
                "여름 총회", "운영 공유", "학생회관", START, END,
                CHECK_IN_START, CHECK_IN_END, adminId);
    }

    private Long insertCalendar() {
        CalendarEvent calendarEvent = CalendarEvent.create(null, "여름 총회",
                "운영 공유", START, END, false, "학생회관", adminId);
        calendarEventMapper.insert(calendarEvent);
        return calendarEvent.getCalendarEventId();
    }

    private Long insertMember(String studentNo, String name, Long cohortId,
                              ClubRole role) {
        Member member = new Member(null, studentNo, name, null, null, null,
                teamId, cohortId, role, MemberStatus.ACTIVE,
                SsoLinkStatus.LINKED, null, null, null);
        memberMapper.insert(member);
        return member.getMemberId();
    }
}
