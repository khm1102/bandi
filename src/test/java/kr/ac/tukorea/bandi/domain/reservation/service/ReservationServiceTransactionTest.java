package kr.ac.tukorea.bandi.domain.reservation.service;

import kr.ac.tukorea.bandi.domain.member.mapper.CohortMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.MemberMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.TeamMapper;
import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.Cohort;
import kr.ac.tukorea.bandi.domain.member.model.CohortTerm;
import kr.ac.tukorea.bandi.domain.member.model.Member;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.domain.member.model.SsoLinkStatus;
import kr.ac.tukorea.bandi.domain.performance.mapper.PerformanceProjectMapper;
import kr.ac.tukorea.bandi.domain.performance.mapper.PerformanceRoundMapper;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceProject;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceRound;
import kr.ac.tukorea.bandi.domain.performance.service.PerformanceRoundService;
import kr.ac.tukorea.bandi.domain.policy.service.PolicyService;
import kr.ac.tukorea.bandi.domain.reservation.dto.request.ReservationCreateParam;
import kr.ac.tukorea.bandi.domain.reservation.mapper.ReservationMapper;
import kr.ac.tukorea.bandi.domain.reservation.model.ActiveSeatOccupancy;
import kr.ac.tukorea.bandi.domain.reservation.model.PerformanceRoundSeat;
import kr.ac.tukorea.bandi.domain.reservation.model.Reservation;
import kr.ac.tukorea.bandi.domain.reservation.model.ReservationSeat;
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
import java.time.LocalDate;
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
class ReservationServiceTransactionTest {

    private static final LocalDateTime NOW =
            LocalDateTime.of(2997, 11, 10, 10, 0);
    private static final String LOOKUP_HASH = "a".repeat(64);
    private static final String ENTRY_HASH = "b".repeat(64);
    private static final String PHONE_HASH = "c".repeat(64);

    private final ReservationService service;
    private final EntryService entryService;
    private final PerformanceProjectMapper projectMapper;
    private final PerformanceRoundMapper roundMapper;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private final MemberMapper memberMapper;
    private final JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private ReservationMapper reservationMapper;
    @MockitoBean
    private PerformanceRoundService roundService;
    @MockitoBean
    private PolicyService policyService;
    @MockitoBean
    private ReservationDataProtector dataProtector;
    @MockitoBean
    private ReservationTokenGenerator tokenGenerator;
    @MockitoBean
    private Clock clock;

    private Long cohortId;
    private Long adminId;
    private Long policyVersionId;
    private Long projectId;
    private Long roundId;
    private Long firstSeatId;
    private Long secondSeatId;

    @Autowired
    ReservationServiceTransactionTest(
            ReservationService service,
            EntryService entryService,
            PerformanceProjectMapper projectMapper,
            PerformanceRoundMapper roundMapper, TeamMapper teamMapper,
            CohortMapper cohortMapper, MemberMapper memberMapper,
            JdbcTemplate jdbcTemplate) {
        this.service = service;
        this.entryService = entryService;
        this.projectMapper = projectMapper;
        this.roundMapper = roundMapper;
        this.teamMapper = teamMapper;
        this.cohortMapper = cohortMapper;
        this.memberMapper = memberMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void setUp() {
        given(clock.instant()).willReturn(
                Instant.parse("2997-11-10T01:00:00Z"));
        given(clock.getZone()).willReturn(ZoneId.of("Asia/Seoul"));
        Long teamId = teamMapper.searchAll().get(0).getTeamId();
        Cohort cohort = new Cohort(null, "예약트랜잭션",
                (short) 2997, CohortTerm.FIRST, true);
        cohortMapper.insert(cohort);
        cohortId = cohort.getCohortId();
        Member admin = new Member(null, "2997000001", "예약운영진",
                null, null, null, teamId, cohortId, ClubRole.ADMIN,
                MemberStatus.ACTIVE, SsoLinkStatus.LINKED,
                null, null, null);
        memberMapper.insert(admin);
        adminId = admin.getMemberId();
        policyVersionId = insertPolicyVersion();
        PerformanceProject project = PerformanceProject.planning(
                (short) 2997, "FIRST", "예약 트랜잭션 공연",
                LocalDate.of(2997, 3, 1),
                LocalDate.of(2997, 12, 31), "소극장", adminId);
        projectMapper.insert(project);
        projectId = project.getPerformanceProjectId();
        PerformanceRound round = PerformanceRound.scheduled(
                projectId, 1, NOW.plusDays(11),
                NOW.plusDays(11).minusMinutes(30),
                NOW.minusDays(9), NOW.plusDays(10));
        roundMapper.insertRound(round);
        roundId = round.getPerformanceRoundId();
        firstSeatId = insertSeat("A-1", 0);
        secondSeatId = insertSeat("A-2", 1);
        given(roundService.isPublicReservationOpen(
                "transaction-show", roundId, NOW)).willReturn(true);
        given(dataProtector.protect("홍길동", "01012345678"))
                .willReturn(new ProtectedApplicant(
                        new byte[]{1}, new byte[]{2}, PHONE_HASH,
                        (short) 1));
        given(tokenGenerator.generate()).willReturn(
                new ReservationCredentials("R29971110ABC",
                        "lookup-token", LOOKUP_HASH,
                        "entry-token", ENTRY_HASH));
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM seat_entry_history");
        jdbcTemplate.update("DELETE FROM reservation_status_history");
        jdbcTemplate.update("DELETE FROM active_seat_occupancy");
        jdbcTemplate.update("DELETE FROM reservation_seat");
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM performance_round_seat");
        jdbcTemplate.update("DELETE FROM performance_round");
        jdbcTemplate.update("DELETE FROM performance_project");
        jdbcTemplate.update("DELETE FROM policy_document_version");
        jdbcTemplate.update("DELETE FROM policy_document");
        jdbcTemplate.update("DELETE FROM member WHERE member_id = ?", adminId);
        jdbcTemplate.update("DELETE FROM cohort WHERE cohort_id = ?", cohortId);
    }

    @Test
    void 신청_이력_저장이_실패하면_신청과_좌석_점유를_모두_롤백한다() {
        willThrow(new IllegalStateException("신청 이력 저장 실패"))
                .given(reservationMapper)
                .insertReservationStatusHistory(any());

        assertThatThrownBy(() -> service.create("transaction-show",
                new ReservationCreateParam(roundId,
                        List.of(firstSeatId, secondSeatId),
                        "홍길동", "01012345678", policyVersionId)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(count("reservation")).isZero();
        assertThat(count("reservation_seat")).isZero();
        assertThat(count("active_seat_occupancy")).isZero();
    }

    @Test
    void 취소_이력_저장이_실패하면_신청과_좌석_점유를_복원한다() {
        Reservation reservation = insertReservation();
        ReservationSeat reservationSeat = ReservationSeat.confirmed(
                reservation.getReservationId(), firstSeatId);
        reservationMapper.insertReservationSeat(reservationSeat);
        reservationMapper.insertActiveSeatOccupancy(
                ActiveSeatOccupancy.occupy(firstSeatId,
                        reservationSeat.getReservationSeatId(), NOW));
        given(roundService.isViewerCancellationOpen(roundId))
                .willReturn(true);
        given(tokenGenerator.hash("lookup-token"))
                .willReturn(LOOKUP_HASH);
        willThrow(new IllegalStateException("취소 이력 저장 실패"))
                .given(reservationMapper)
                .insertReservationStatusHistory(any());

        assertThatThrownBy(() -> service.cancel(
                "lookup-token", "일정 변경"))
                .isInstanceOf(IllegalStateException.class);

        assertThat(jdbcTemplate.queryForObject("""
                SELECT status_code FROM reservation
                WHERE reservation_id = ?
                """, String.class, reservation.getReservationId()))
                .isEqualTo("CONFIRMED");
        assertThat(jdbcTemplate.queryForObject("""
                SELECT status_code FROM reservation_seat
                WHERE reservation_seat_id = ?
                """, String.class,
                reservationSeat.getReservationSeatId()))
                .isEqualTo("CONFIRMED");
        assertThat(count("active_seat_occupancy")).isEqualTo(1);
    }

    @Test
    void 입장_이력_저장이_실패하면_좌석_입장_상태를_롤백한다() {
        Reservation reservation = insertReservation();
        ReservationSeat reservationSeat = ReservationSeat.confirmed(
                reservation.getReservationId(), firstSeatId);
        reservationMapper.insertReservationSeat(reservationSeat);
        given(roundService.isEntryOpen(adminId, roundId))
                .willReturn(true);
        given(tokenGenerator.hash("entry-token"))
                .willReturn(ENTRY_HASH);
        willThrow(new IllegalStateException("입장 이력 저장 실패"))
                .given(reservationMapper).insertSeatEntryHistory(any());

        assertThatThrownBy(() -> entryService.checkIn(
                adminId, roundId, "entry-token",
                List.of(reservationSeat.getReservationSeatId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(jdbcTemplate.queryForObject("""
                SELECT checked_in_dttm IS NULL
                FROM reservation_seat
                WHERE reservation_seat_id = ?
                """, Boolean.class,
                reservationSeat.getReservationSeatId())).isTrue();
    }

    private Long insertSeat(String label, int displayColumn) {
        PerformanceRoundSeat seat = PerformanceRoundSeat.available(
                roundId, label, "A", null, null,
                0, displayColumn, null);
        reservationMapper.insertRoundSeat(seat);
        return seat.getPerformanceRoundSeatId();
    }

    private Reservation insertReservation() {
        Reservation reservation = Reservation.confirm(roundId,
                "R29971110EXIST", LOOKUP_HASH, ENTRY_HASH,
                new byte[]{1}, new byte[]{2}, PHONE_HASH,
                (short) 1, policyVersionId, NOW);
        reservationMapper.insertReservation(reservation);
        return reservation;
    }

    private Long insertPolicyVersion() {
        jdbcTemplate.update("""
                INSERT INTO policy_document (
                    policy_type_code, title, audience_code, is_active
                ) VALUES ('RESERVATION_PRIVACY', '관람 신청 개인정보',
                          'VISITOR', 1)
                """);
        Long documentId = jdbcTemplate.queryForObject(
                "SELECT LAST_INSERT_ID()", Long.class);
        jdbcTemplate.update("""
                INSERT INTO policy_document_version (
                    policy_document_id, version_no, body,
                    published_dttm, published_by_member_id,
                    effective_from_dttm, is_required
                ) VALUES (?, 1, '수집 동의', ?, ?, ?, 1)
                """, documentId, NOW.minusDays(1), adminId,
                NOW.minusDays(1));
        return jdbcTemplate.queryForObject(
                "SELECT LAST_INSERT_ID()", Long.class);
    }

    private int count(String tableName) {
        String sql = switch (tableName) {
            case "reservation" -> "SELECT COUNT(*) FROM reservation";
            case "reservation_seat" ->
                    "SELECT COUNT(*) FROM reservation_seat";
            case "active_seat_occupancy" ->
                    "SELECT COUNT(*) FROM active_seat_occupancy";
            default -> throw new IllegalArgumentException(
                    "unsupported table");
        };
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }
}
