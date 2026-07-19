package kr.ac.tukorea.bandi.domain.reservation.mapper;

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
import kr.ac.tukorea.bandi.domain.reservation.model.ActiveSeatOccupancy;
import kr.ac.tukorea.bandi.domain.reservation.model.PerformanceRoundSeat;
import kr.ac.tukorea.bandi.domain.reservation.model.Reservation;
import kr.ac.tukorea.bandi.domain.reservation.model.ReservationSeat;
import kr.ac.tukorea.bandi.domain.reservation.model.ReservationStatusHistory;
import kr.ac.tukorea.bandi.domain.reservation.model.RoundSeatStatus;
import kr.ac.tukorea.bandi.domain.reservation.model.SeatEntryHistory;
import kr.ac.tukorea.bandi.global.annotation.MapperTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@MapperTest
class ReservationMapperTest {

    private static final LocalDateTime NOW =
            LocalDateTime.of(2026, 11, 10, 10, 0);
    private static final String LOOKUP_HASH = "a".repeat(64);
    private static final String ENTRY_HASH = "b".repeat(64);
    private static final String PHONE_HASH = "c".repeat(64);

    private final ReservationMapper reservationMapper;
    private final PerformanceProjectMapper projectMapper;
    private final PerformanceRoundMapper roundMapper;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private final MemberMapper memberMapper;
    private final JdbcTemplate jdbcTemplate;

    private Long adminId;
    private Long policyVersionId;
    private PerformanceRound round;

    @Autowired
    ReservationMapperTest(
            ReservationMapper reservationMapper,
            PerformanceProjectMapper projectMapper,
            PerformanceRoundMapper roundMapper, TeamMapper teamMapper,
            CohortMapper cohortMapper, MemberMapper memberMapper,
            JdbcTemplate jdbcTemplate) {
        this.reservationMapper = reservationMapper;
        this.projectMapper = projectMapper;
        this.roundMapper = roundMapper;
        this.teamMapper = teamMapper;
        this.cohortMapper = cohortMapper;
        this.memberMapper = memberMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void setUp() {
        adminId = insertAdmin();
        policyVersionId = insertPolicyVersion();
        PerformanceProject project = PerformanceProject.planning(
                (short) 2032, "FIRST", "햄릿",
                LocalDate.of(2032, 3, 1),
                LocalDate.of(2032, 12, 31), "소극장", adminId);
        projectMapper.insert(project);
        round = PerformanceRound.scheduled(
                project.getPerformanceProjectId(), 1,
                NOW.plusDays(11), NOW.plusDays(11).minusMinutes(30),
                NOW.minusDays(9), NOW.plusDays(10));
        roundMapper.insertRound(round);
    }

    @Test
    void 회차_좌석을_저장하고_상태와_표시_순서로_조회한다() {
        PerformanceRoundSeat second = insertSeat("A-2", 0, 1);
        PerformanceRoundSeat first = insertSeat("A-1", 0, 0);

        reservationMapper.updateRoundSeatStatus(
                second.changeStatus(RoundSeatStatus.BLOCKED));

        assertThat(reservationMapper.searchRoundSeats(
                round.getPerformanceRoundId()))
                .extracting(PerformanceRoundSeat::getSeatLabel)
                .containsExactly("A-1", "A-2");
        assertThat(reservationMapper.lookupRoundSeatForUpdate(
                second.getPerformanceRoundSeatId())).isPresent().get()
                .extracting(PerformanceRoundSeat::getStatus)
                .isEqualTo(RoundSeatStatus.BLOCKED);
        assertThat(reservationMapper.searchAvailableRoundSeats(
                round.getPerformanceRoundId()))
                .extracting(PerformanceRoundSeat::getPerformanceRoundSeatId)
                .containsExactly(first.getPerformanceRoundSeatId());
    }

    @Test
    void 같은_회차의_좌석_라벨은_중복할_수_없다() {
        insertSeat("A-1", 0, 0);

        assertThatThrownBy(() -> insertSeat("A-1", 1, 0))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void 신청과_암호문_토큰_해시를_저장하고_각_키로_조회한다() {
        Reservation reservation = insertReservation(
                "R20321110AAA", LOOKUP_HASH, ENTRY_HASH);

        assertThat(reservationMapper.lookupReservationByLookupTokenHash(
                LOOKUP_HASH)).isPresent().get()
                .satisfies(found -> {
                    assertThat(found.getReservationId())
                            .isEqualTo(reservation.getReservationId());
                    assertThat(found.getApplicantNameCiphertext())
                            .containsExactly(1, 2, 3);
                    assertThat(found.getPhoneCiphertext())
                            .containsExactly(4, 5, 6);
                });
        assertThat(reservationMapper
                .lookupReservationByEntryTokenHashForUpdate(ENTRY_HASH))
                .isPresent();
        assertThat(reservationMapper.lookupReservationByNo(
                "R20321110AAA")).isPresent();
    }

    @Test
    void 활성_좌석_점유는_DB에서_한_건만_허용한다() {
        PerformanceRoundSeat seat = insertSeat("A-1", 0, 0);
        Reservation first = insertReservation(
                "R20321110AAA", LOOKUP_HASH, ENTRY_HASH);
        ReservationSeat firstSeat = insertReservationSeat(first, seat);
        reservationMapper.insertActiveSeatOccupancy(
                ActiveSeatOccupancy.occupy(
                        seat.getPerformanceRoundSeatId(),
                        firstSeat.getReservationSeatId(), NOW));
        Reservation second = insertReservation(
                "R20321110BBB", "d".repeat(64), "e".repeat(64));
        ReservationSeat secondSeat = insertReservationSeat(second, seat);

        assertThatThrownBy(() -> reservationMapper
                .insertActiveSeatOccupancy(
                        ActiveSeatOccupancy.occupy(
                                seat.getPerformanceRoundSeatId(),
                                secondSeat.getReservationSeatId(), NOW)))
                .isInstanceOf(DataAccessException.class);
        assertThat(reservationMapper.searchAvailableRoundSeats(
                round.getPerformanceRoundId())).isEmpty();
    }

    @Test
    void 신청_취소_후_활성_점유를_삭제하면_좌석이_다시_노출된다() {
        PerformanceRoundSeat seat = insertSeat("A-1", 0, 0);
        Reservation reservation = insertReservation(
                "R20321110AAA", LOOKUP_HASH, ENTRY_HASH);
        ReservationSeat reservationSeat =
                insertReservationSeat(reservation, seat);
        reservationMapper.insertActiveSeatOccupancy(
                ActiveSeatOccupancy.occupy(
                        seat.getPerformanceRoundSeatId(),
                        reservationSeat.getReservationSeatId(), NOW));

        reservationMapper.removeActiveSeatOccupanciesByReservation(
                reservation.getReservationId());

        assertThat(reservationMapper.searchAvailableRoundSeats(
                round.getPerformanceRoundId()))
                .extracting(PerformanceRoundSeat::getPerformanceRoundSeatId)
                .containsExactly(seat.getPerformanceRoundSeatId());
    }

    @Test
    void 좌석별_입장과_취소_이력을_저장하고_운영_지표를_집계한다() {
        PerformanceRoundSeat first = insertSeat("A-1", 0, 0);
        PerformanceRoundSeat second = insertSeat("A-2", 0, 1);
        Reservation reservation = insertReservation(
                "R20321110AAA", LOOKUP_HASH, ENTRY_HASH);
        ReservationSeat firstSeat = insertReservationSeat(
                reservation, first);
        insertReservationSeat(reservation, second);
        ReservationSeat checkedIn = firstSeat.checkIn(adminId, NOW);
        reservationMapper.updateReservationSeat(checkedIn);
        reservationMapper.insertSeatEntryHistory(
                SeatEntryHistory.checkIn(
                        firstSeat.getReservationSeatId(), adminId, NOW));

        var metrics = reservationMapper.lookupReservationMetrics(
                round.getPerformanceRoundId());

        assertThat(metrics.reservationCount()).isEqualTo(1);
        assertThat(metrics.reservedSeatCount()).isEqualTo(2);
        assertThat(metrics.checkedInSeatCount()).isEqualTo(1);
        assertThat(metrics.notCheckedInSeatCount()).isEqualTo(1);
        assertThat(metrics.partiallyCheckedInReservationCount())
                .isEqualTo(1);
        assertThat(metrics.entryRate()).isEqualByComparingTo("50.00");
        assertThat(reservationMapper.searchReservationSeatResponses(
                reservation.getReservationId()))
                .extracting("checkedIn")
                .containsExactly(true, false);
    }

    @Test
    void 상태_이력과_입장_취소_이력을_저장한다() {
        PerformanceRoundSeat seat = insertSeat("A-1", 0, 0);
        Reservation reservation = insertReservation(
                "R20321110AAA", LOOKUP_HASH, ENTRY_HASH);
        ReservationSeat reservationSeat =
                insertReservationSeat(reservation, seat);
        ReservationSeat checkedIn = reservationSeat.checkIn(adminId, NOW);
        reservationMapper.updateReservationSeat(checkedIn);

        reservationMapper.insertReservationStatusHistory(
                ReservationStatusHistory.created(
                        reservation.getReservationId(), NOW));
        reservationMapper.insertSeatEntryHistory(
                SeatEntryHistory.cancelCheckIn(
                        reservationSeat.getReservationSeatId(), adminId,
                        NOW.plusMinutes(1), "처리 오류"));

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation_status_history",
                Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM seat_entry_history",
                Integer.class)).isEqualTo(1);
    }

    @Test
    void 공연_종료_후_보존_기한이_지난_개인정보를_파기한다() {
        Reservation reservation = insertReservation(
                "R20321110AAA", LOOKUP_HASH, ENTRY_HASH);

        List<Reservation> targets = reservationMapper
                .searchPersonalDataEraseTargets(
                        NOW.plusYears(1), 100);
        reservationMapper.updateReservation(
                targets.get(0).erasePersonalData(NOW.plusYears(1)));

        assertThat(reservationMapper.lookupReservationByIdForUpdate(
                reservation.getReservationId())).isPresent().get()
                .satisfies(found -> {
                    assertThat(found.getLookupTokenHash()).isNull();
                    assertThat(found.getApplicantNameCiphertext()).isNull();
                    assertThat(found.getPersonalDataErasedDttm())
                            .isEqualTo(NOW.plusYears(1));
                });
    }

    @Test
    void DB는_좌석과_신청과_입장_상태_코드를_제약한다() {
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO performance_round_seat (
                    performance_round_id, seat_label, status_code
                ) VALUES (?, 'A-1', 'HELD')
                """, round.getPerformanceRoundId()))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO reservation (
                    performance_round_id, reservation_no,
                    lookup_token_hash, entry_token_hash,
                    applicant_name_ciphertext, phone_ciphertext,
                    phone_search_hash, encryption_key_version,
                    status_code, privacy_policy_version_id, agreed_dttm
                ) VALUES (?, 'BAD', ?, ?, X'01', X'02', ?, 1,
                          'PENDING', ?, ?)
                """, round.getPerformanceRoundId(), LOOKUP_HASH,
                ENTRY_HASH, PHONE_HASH, policyVersionId, NOW))
                .isInstanceOf(DataAccessException.class);
    }

    private PerformanceRoundSeat insertSeat(
            String label, int displayRow, int displayColumn) {
        PerformanceRoundSeat seat = PerformanceRoundSeat.available(
                round.getPerformanceRoundId(), label, "A", null, null,
                displayRow, displayColumn, null);
        reservationMapper.insertRoundSeat(seat);
        return seat;
    }

    private Reservation insertReservation(
            String reservationNo, String lookupHash, String entryHash) {
        Reservation reservation = Reservation.confirm(
                round.getPerformanceRoundId(), reservationNo,
                lookupHash, entryHash, new byte[]{1, 2, 3},
                new byte[]{4, 5, 6}, PHONE_HASH,
                (short) 1, policyVersionId, NOW);
        reservationMapper.insertReservation(reservation);
        return reservation;
    }

    private ReservationSeat insertReservationSeat(
            Reservation reservation, PerformanceRoundSeat seat) {
        ReservationSeat reservationSeat = ReservationSeat.confirmed(
                reservation.getReservationId(),
                seat.getPerformanceRoundSeatId());
        reservationMapper.insertReservationSeat(reservationSeat);
        return reservationSeat;
    }

    private Long insertAdmin() {
        Long teamId = teamMapper.searchAll().stream()
                .filter(team -> team.getName().equals("연출"))
                .findFirst().orElseThrow().getTeamId();
        Cohort cohort = new Cohort(null, "32-예약", (short) 2032,
                CohortTerm.FIRST, true);
        cohortMapper.insert(cohort);
        Member admin = new Member(null, "2032000991", "예약 운영진",
                null, null, null, teamId, cohort.getCohortId(),
                ClubRole.ADMIN, MemberStatus.ACTIVE,
                SsoLinkStatus.LINKED, null, null, null);
        memberMapper.insert(admin);
        return admin.getMemberId();
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
}
