package kr.ac.tukorea.bandi.domain.event.service;

import kr.ac.tukorea.bandi.domain.calendar.mapper.CalendarEventMapper;
import kr.ac.tukorea.bandi.domain.calendar.model.CalendarEvent;
import kr.ac.tukorea.bandi.domain.calendar.service.CalendarService;
import kr.ac.tukorea.bandi.domain.event.dto.request.AttendanceProcessParam;
import kr.ac.tukorea.bandi.domain.event.dto.request.EventTargetConfirmParam;
import kr.ac.tukorea.bandi.domain.event.mapper.ClubEventMapper;
import kr.ac.tukorea.bandi.domain.event.model.AttendanceStatus;
import kr.ac.tukorea.bandi.domain.event.model.ClubEvent;
import kr.ac.tukorea.bandi.domain.event.model.ClubEventStatus;
import kr.ac.tukorea.bandi.domain.event.model.EventAttendance;
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
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@SpringBootTest
@ActiveProfiles("test")
class ClubEventServiceTransactionTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 8, 1, 18, 0);
    private static final LocalDateTime END = START.plusHours(3);
    private static final LocalDateTime CHECK_IN_START = START.minusMinutes(30);
    private static final LocalDateTime CHECK_IN_END = START.plusMinutes(30);

    private final ClubEventService service;
    private final CalendarEventMapper calendarEventMapper;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private final MemberMapper memberMapper;
    private final JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private ClubEventMapper mapper;
    @MockitoBean
    private MemberService memberService;
    @MockitoBean
    private CalendarService calendarService;
    @MockitoBean
    private Clock clock;

    private Long teamId;
    private Long cohortId;
    private Long adminId;
    private Long memberId;
    private Long draftEventId;
    private Long openEventId;
    private Long attendanceId;

    @Autowired
    ClubEventServiceTransactionTest(ClubEventService service,
                                    CalendarEventMapper calendarEventMapper,
                                    TeamMapper teamMapper,
                                    CohortMapper cohortMapper,
                                    MemberMapper memberMapper,
                                    JdbcTemplate jdbcTemplate) {
        this.service = service;
        this.calendarEventMapper = calendarEventMapper;
        this.teamMapper = teamMapper;
        this.cohortMapper = cohortMapper;
        this.memberMapper = memberMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void setUp() {
        given(clock.instant()).willReturn(Instant.parse("2026-08-01T09:00:00Z"));
        given(clock.getZone()).willReturn(ZoneId.of("Asia/Seoul"));
        teamId = teamMapper.searchAll().stream()
                .filter(team -> team.getName().equals("무대팀"))
                .findFirst().orElseThrow().getTeamId();
        Cohort cohort = new Cohort(null, "행사트랜잭션기수", (short) 2994,
                CohortTerm.FIRST, true);
        cohortMapper.insert(cohort);
        cohortId = cohort.getCohortId();
        adminId = insertMember("2994000001", "행사운영진", ClubRole.ADMIN);
        memberId = insertMember("2994000002", "행사참석자", ClubRole.MEMBER);
        given(memberService.lookupAccessContext(adminId))
                .willReturn(new MemberAccessContext(
                        adminId, teamId, true, false, true));
        given(memberService.searchActiveMemberIds(null))
                .willReturn(List.of(adminId, memberId));

        ClubEvent draft = insertDraft("대상 확정 롤백");
        draftEventId = draft.getClubEventId();
        ClubEvent opened = insertDraft("출석 처리 롤백")
                .schedule(adminId)
                .openCheckIn(adminId, START);
        mapper.update(opened);
        openEventId = opened.getClubEventId();
        mapper.insertAttendances(List.of(
                EventAttendance.pending(openEventId, memberId)));
        attendanceId = jdbcTemplate.queryForObject("""
                SELECT event_attendance_id FROM event_attendance
                WHERE club_event_id = ? AND member_id = ?
                """, Long.class, openEventId, memberId);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM event_attendance_history");
        jdbcTemplate.update("DELETE FROM event_attendance");
        jdbcTemplate.update("DELETE FROM club_event");
        jdbcTemplate.update("DELETE FROM calendar_event");
        jdbcTemplate.update("DELETE FROM member WHERE member_id IN (?, ?)",
                adminId, memberId);
        jdbcTemplate.update("DELETE FROM cohort WHERE cohort_id = ?", cohortId);
    }

    @Test
    void 행사_상태_갱신이_실패하면_대상_스냅샷도_롤백한다() {
        willThrow(new IllegalStateException("행사 상태 갱신 실패"))
                .given(mapper).update(any());

        assertThatThrownBy(() -> service.confirmTargets(adminId,
                new EventTargetConfirmParam(draftEventId, List.of())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(mapper.lookupById(draftEventId)).isPresent().get()
                .extracting(ClubEvent::getStatus)
                .isEqualTo(ClubEventStatus.DRAFT);
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM event_attendance WHERE club_event_id = ?
                """, Integer.class, draftEventId);
        assertThat(count).isZero();
    }

    @Test
    void 이력_저장이_실패하면_출석_상태도_PENDING으로_롤백한다() {
        willThrow(new IllegalStateException("이력 저장 실패"))
                .given(mapper).insertAttendanceHistory(any());

        assertThatThrownBy(() -> service.processAttendance(adminId,
                new AttendanceProcessParam(openEventId, List.of(attendanceId),
                        AttendanceStatus.PRESENT, null)))
                .isInstanceOf(IllegalStateException.class);

        EventAttendance attendance = mapper.searchAttendancesByIdsForUpdate(
                openEventId, List.of(attendanceId)).get(0);
        assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.PENDING);
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM event_attendance_history
                WHERE event_attendance_id = ?
                """, Integer.class, attendanceId);
        assertThat(count).isZero();
    }

    private ClubEvent insertDraft(String title) {
        CalendarEvent calendar = CalendarEvent.create(null, title, title,
                START, END, false, "학생회관", adminId);
        calendarEventMapper.insert(calendar);
        ClubEvent event = ClubEvent.draft(calendar.getCalendarEventId(),
                EventTargetScope.ALL, null, title, null, "학생회관",
                START, END, CHECK_IN_START, CHECK_IN_END, adminId);
        mapper.insert(event);
        return event;
    }

    private Long insertMember(String studentNo, String name, ClubRole role) {
        Member member = new Member(null, studentNo, name, null, null, null,
                teamId, cohortId, role, MemberStatus.ACTIVE,
                SsoLinkStatus.LINKED, null, null, null);
        memberMapper.insert(member);
        return member.getMemberId();
    }
}
