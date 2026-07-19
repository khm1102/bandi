package kr.ac.tukorea.bandi.domain.reservation.service;

import kr.ac.tukorea.bandi.domain.performance.service.PerformanceRoundService;
import kr.ac.tukorea.bandi.domain.policy.service.PolicyService;
import kr.ac.tukorea.bandi.domain.reservation.dto.request.ReservationCreateParam;
import kr.ac.tukorea.bandi.domain.reservation.exception.InvalidReservationException;
import kr.ac.tukorea.bandi.domain.reservation.exception.InvalidReservationStateException;
import kr.ac.tukorea.bandi.domain.reservation.exception.ReservationNotFoundException;
import kr.ac.tukorea.bandi.domain.reservation.exception.SeatUnavailableException;
import kr.ac.tukorea.bandi.domain.reservation.mapper.ReservationMapper;
import kr.ac.tukorea.bandi.domain.reservation.model.ActiveSeatOccupancy;
import kr.ac.tukorea.bandi.domain.reservation.model.PerformanceRoundSeat;
import kr.ac.tukorea.bandi.domain.reservation.model.Reservation;
import kr.ac.tukorea.bandi.domain.reservation.model.ReservationSeat;
import kr.ac.tukorea.bandi.domain.reservation.model.ReservationSeatStatus;
import kr.ac.tukorea.bandi.domain.reservation.model.ReservationStatus;
import kr.ac.tukorea.bandi.domain.reservation.model.ReservationStatusHistory;
import kr.ac.tukorea.bandi.domain.reservation.model.RoundSeatStatus;
import kr.ac.tukorea.bandi.global.config.ReservationSecurityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    private static final Long ACTOR_ID = 1L;
    private static final Long PROJECT_ID = 10L;
    private static final Long ROUND_ID = 20L;
    private static final Long RESERVATION_ID = 30L;
    private static final Long FIRST_SEAT_ID = 41L;
    private static final Long SECOND_SEAT_ID = 42L;
    private static final Long POLICY_VERSION_ID = 50L;
    private static final LocalDateTime NOW =
            LocalDateTime.of(2026, 11, 10, 10, 0);
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-11-10T01:00:00Z"),
            ZoneId.of("Asia/Seoul"));
    private static final String LOOKUP_TOKEN = "lookup-token";
    private static final String ENTRY_TOKEN = "entry-token";
    private static final String LOOKUP_HASH = "a".repeat(64);
    private static final String ENTRY_HASH = "b".repeat(64);
    private static final String PHONE_HASH = "c".repeat(64);

    @Mock
    private ReservationMapper reservationMapper;
    @Mock
    private PerformanceRoundService roundService;
    @Mock
    private PolicyService policyService;
    @Mock
    private ReservationDataProtector dataProtector;
    @Mock
    private ReservationTokenGenerator tokenGenerator;

    private ReservationService service;

    @BeforeEach
    void setUp() {
        service = new ReservationService(reservationMapper, roundService,
                policyService, dataProtector, tokenGenerator,
                properties(), CLOCK);
    }

    @Test
    void 공개_회차의_좌석을_신청하고_원본_토큰을_한번만_반환한다() {
        givenCreateDependencies(RoundSeatStatus.AVAILABLE);
        givenApplicantAndCredentials();
        willAnswer(invocation -> {
            assignId(invocation.getArgument(0), "reservationId",
                    RESERVATION_ID);
            return 1;
        }).given(reservationMapper).insertReservation(any());
        willAnswer(invocation -> {
            ReservationSeat seat = invocation.getArgument(0);
            assignId(seat, "reservationSeatId",
                    seat.getPerformanceRoundSeatId() + 100);
            return 1;
        }).given(reservationMapper).insertReservationSeat(any());

        var result = service.create("hamlet", createParam());

        assertThat(result.reservationId()).isEqualTo(RESERVATION_ID);
        assertThat(result.lookupToken()).isEqualTo(LOOKUP_TOKEN);
        assertThat(result.entryToken()).isEqualTo(ENTRY_TOKEN);
        verify(policyService).validateReservationPrivacyVersion(
                POLICY_VERSION_ID);
        verify(reservationMapper, times(2))
                .insertActiveSeatOccupancy(any());
        verify(reservationMapper).insertReservationStatusHistory(any());
    }

    @Test
    void 신청_가능_상태가_아닌_회차는_신청할_수_없다() {
        given(roundService.isPublicReservationOpen(
                "hamlet", ROUND_ID, NOW)).willReturn(false);

        assertThatThrownBy(() -> service.create("hamlet", createParam()))
                .isInstanceOf(InvalidReservationStateException.class);

        verify(reservationMapper, never()).insertReservation(any());
    }

    @Test
    void 신청_기간_밖에서는_신청할_수_없다() {
        given(roundService.isPublicReservationOpen(
                "hamlet", ROUND_ID, NOW)).willReturn(false);

        assertThatThrownBy(() -> service.create("hamlet", createParam()))
                .isInstanceOf(InvalidReservationStateException.class);
    }

    @Test
    void 같은_좌석을_두번_요청할_수_없다() {
        ReservationCreateParam duplicate = new ReservationCreateParam(
                ROUND_ID, List.of(FIRST_SEAT_ID, FIRST_SEAT_ID),
                "홍길동", "01012345678", POLICY_VERSION_ID);

        assertThatThrownBy(() -> service.create("hamlet", duplicate))
                .isInstanceOf(InvalidReservationException.class);
    }

    @Test
    void 차단된_좌석은_신청할_수_없다() {
        givenCreateDependencies(RoundSeatStatus.BLOCKED);

        assertThatThrownBy(() -> service.create("hamlet", createParam()))
                .isInstanceOf(SeatUnavailableException.class);
    }

    @Test
    void 다른_신청이_좌석을_먼저_점유하면_전체_신청을_실패시킨다() {
        givenCreateDependencies(RoundSeatStatus.AVAILABLE);
        givenApplicantAndCredentials();
        willAnswer(invocation -> {
            assignId(invocation.getArgument(0), "reservationId",
                    RESERVATION_ID);
            return 1;
        }).given(reservationMapper).insertReservation(any());
        willAnswer(invocation -> {
            ReservationSeat seat = invocation.getArgument(0);
            assignId(seat, "reservationSeatId",
                    seat.getPerformanceRoundSeatId() + 100);
            return 1;
        }).given(reservationMapper).insertReservationSeat(any());
        willThrow(new DuplicateKeyException("occupied"))
                .given(reservationMapper).insertActiveSeatOccupancy(any());

        assertThatThrownBy(() -> service.create("hamlet", createParam()))
                .isInstanceOf(SeatUnavailableException.class);
    }

    @Test
    void 조회_토큰으로_신청과_복호화한_신청자_정보를_조회한다() {
        given(tokenGenerator.hash(LOOKUP_TOKEN)).willReturn(LOOKUP_HASH);
        given(reservationMapper.lookupReservationByLookupTokenHash(
                LOOKUP_HASH)).willReturn(Optional.of(reservation()));
        given(reservationMapper.searchReservationSeatResponses(
                RESERVATION_ID)).willReturn(List.of());
        given(dataProtector.decryptName(any(byte[].class), any(Short.class)))
                .willReturn("홍길동");
        given(dataProtector.decryptPhone(any(byte[].class), any(Short.class)))
                .willReturn("01012345678");
        given(roundService.isViewerCancellationOpen(ROUND_ID))
                .willReturn(true);

        var result = service.lookup(LOOKUP_TOKEN);

        assertThat(result.applicantName()).isEqualTo("홍길동");
        assertThat(result.phone()).isEqualTo("01012345678");
        assertThat(result.cancelable()).isTrue();
    }

    @Test
    void 조회_토큰으로_신청을_전체_취소하고_좌석_점유를_반환한다() {
        given(tokenGenerator.hash(LOOKUP_TOKEN)).willReturn(LOOKUP_HASH);
        given(reservationMapper.lookupReservationByLookupTokenHashForUpdate(
                LOOKUP_HASH)).willReturn(Optional.of(reservation()));
        given(reservationMapper.searchReservationSeatsForUpdate(
                RESERVATION_ID)).willReturn(List.of(
                        reservationSeat(141L, FIRST_SEAT_ID, false),
                        reservationSeat(142L, SECOND_SEAT_ID, false)));
        given(roundService.isViewerCancellationOpen(ROUND_ID))
                .willReturn(true);

        service.cancel(LOOKUP_TOKEN, "일정 변경");

        verify(reservationMapper, times(2)).updateReservationSeat(any());
        verify(reservationMapper).removeActiveSeatOccupanciesByReservation(
                RESERVATION_ID);
        ArgumentCaptor<Reservation> captor =
                ArgumentCaptor.forClass(Reservation.class);
        verify(reservationMapper).updateReservation(captor.capture());
        assertThat(captor.getValue().getStatus())
                .isEqualTo(ReservationStatus.CANCELLED);
        verify(reservationMapper).insertReservationStatusHistory(any());
    }

    @Test
    void 입장_처리가_시작된_신청은_관람객이_취소할_수_없다() {
        given(tokenGenerator.hash(LOOKUP_TOKEN)).willReturn(LOOKUP_HASH);
        given(reservationMapper.lookupReservationByLookupTokenHashForUpdate(
                LOOKUP_HASH)).willReturn(Optional.of(reservation()));
        given(reservationMapper.searchReservationSeatsForUpdate(
                RESERVATION_ID)).willReturn(List.of(
                        reservationSeat(141L, FIRST_SEAT_ID, true)));
        given(roundService.isViewerCancellationOpen(ROUND_ID))
                .willReturn(true);

        assertThatThrownBy(() -> service.cancel(
                LOOKUP_TOKEN, "일정 변경"))
                .isInstanceOf(InvalidReservationStateException.class);
    }

    @Test
    void 입장_오픈_후에는_관람객이_직접_취소할_수_없다() {
        given(tokenGenerator.hash(LOOKUP_TOKEN)).willReturn(LOOKUP_HASH);
        given(reservationMapper.lookupReservationByLookupTokenHashForUpdate(
                LOOKUP_HASH)).willReturn(Optional.of(reservation()));
        given(roundService.isViewerCancellationOpen(ROUND_ID))
                .willReturn(false);

        assertThatThrownBy(() -> service.cancel(
                LOOKUP_TOKEN, "일정 변경"))
                .isInstanceOf(InvalidReservationStateException.class);
    }

    @Test
    void 운영진은_예외_상황에서_신청을_사유와_함께_취소한다() {
        given(reservationMapper.lookupReservationByIdForUpdate(
                RESERVATION_ID)).willReturn(Optional.of(reservation()));
        given(reservationMapper.searchReservationSeatsForUpdate(
                RESERVATION_ID)).willReturn(List.of(
                        reservationSeat(141L, FIRST_SEAT_ID, false),
                        reservationSeat(142L, SECOND_SEAT_ID, false)));

        service.cancelByAdmin(ACTOR_ID, PROJECT_ID,
                RESERVATION_ID, "현장 요청");

        verify(roundService).validateManage(
                ACTOR_ID, ROUND_ID, PROJECT_ID);
        verify(reservationMapper, times(2)).updateReservationSeat(any());
        verify(reservationMapper).removeActiveSeatOccupanciesByReservation(
                RESERVATION_ID);
        ArgumentCaptor<ReservationStatusHistory> captor =
                ArgumentCaptor.forClass(ReservationStatusHistory.class);
        verify(reservationMapper)
                .insertReservationStatusHistory(captor.capture());
        assertThat(captor.getValue().getChangedByMemberId())
                .isEqualTo(ACTOR_ID);
    }

    @Test
    void 운영진이_존재하지_않는_신청을_취소하면_찾을_수_없다() {
        given(reservationMapper.lookupReservationByIdForUpdate(
                RESERVATION_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancelByAdmin(
                ACTOR_ID, PROJECT_ID, RESERVATION_ID, "현장 요청"))
                .isInstanceOf(ReservationNotFoundException.class);

        verify(roundService, never()).validateManage(
                any(), any(), any());
    }

    @Test
    void 운영진이_회차별_신청자와_좌석을_조회한다() {
        given(reservationMapper.searchReservations(ROUND_ID,
                ReservationStatus.CONFIRMED, 0, 20))
                .willReturn(List.of(reservation()));
        given(reservationMapper.searchReservationSeatResponses(
                RESERVATION_ID)).willReturn(List.of());
        given(dataProtector.decryptName(any(byte[].class), any(Short.class)))
                .willReturn("홍길동");
        given(dataProtector.decryptPhone(any(byte[].class), any(Short.class)))
                .willReturn("01012345678");

        var result = service.search(ACTOR_ID, PROJECT_ID, ROUND_ID,
                ReservationStatus.CONFIRMED, 0, 20);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).applicantName()).isEqualTo("홍길동");
        assertThat(result.get(0).phone()).isEqualTo("01012345678");
        verify(roundService).validateManage(ACTOR_ID, ROUND_ID, PROJECT_ID);
    }

    @Test
    void 신청_목록의_페이지_범위가_잘못되면_조회하지_않는다() {
        assertThatThrownBy(() -> service.search(ACTOR_ID, PROJECT_ID,
                ROUND_ID, null, -1, 20))
                .isInstanceOf(InvalidReservationException.class);
        assertThatThrownBy(() -> service.search(ACTOR_ID, PROJECT_ID,
                ROUND_ID, null, 0, 51))
                .isInstanceOf(InvalidReservationException.class);

        verify(reservationMapper, never()).searchReservations(
                any(), any(), any(Integer.class), any(Integer.class));
    }

    @Test
    void 보존_기한이_지난_신청의_개인정보를_파기한다() {
        given(reservationMapper.searchPersonalDataEraseTargets(
                NOW.minusDays(90), 100))
                .willReturn(List.of(reservation()));

        int result = service.eraseExpiredPersonalData(100);

        assertThat(result).isEqualTo(1);
        ArgumentCaptor<Reservation> captor =
                ArgumentCaptor.forClass(Reservation.class);
        verify(reservationMapper).updateReservation(captor.capture());
        assertThat(captor.getValue().getPersonalDataErasedDttm())
                .isEqualTo(NOW);
        assertThat(captor.getValue().getLookupTokenHash()).isNull();
    }

    private void givenCreateDependencies(RoundSeatStatus status) {
        given(roundService.isPublicReservationOpen(
                "hamlet", ROUND_ID, NOW)).willReturn(true);
        given(reservationMapper.searchRoundSeatsForUpdate(
                List.of(FIRST_SEAT_ID, SECOND_SEAT_ID)))
                .willReturn(List.of(
                        roundSeat(FIRST_SEAT_ID, status),
                        roundSeat(SECOND_SEAT_ID, status)));
    }

    private void givenApplicantAndCredentials() {
        given(dataProtector.protect("홍길동", "01012345678"))
                .willReturn(new ProtectedApplicant(
                        new byte[]{1}, new byte[]{2}, PHONE_HASH,
                        (short) 1));
        given(tokenGenerator.generate()).willReturn(credentials());
    }

    private ReservationCreateParam createParam() {
        return new ReservationCreateParam(ROUND_ID,
                List.of(FIRST_SEAT_ID, SECOND_SEAT_ID),
                "홍길동", "01012345678", POLICY_VERSION_ID);
    }

    private ReservationCredentials credentials() {
        return new ReservationCredentials("R20261110ABC",
                LOOKUP_TOKEN, LOOKUP_HASH, ENTRY_TOKEN, ENTRY_HASH);
    }

    private Reservation reservation() {
        return new Reservation(RESERVATION_ID, ROUND_ID,
                "R20261110ABC", LOOKUP_HASH, ENTRY_HASH,
                new byte[]{1}, new byte[]{2}, PHONE_HASH, (short) 1,
                ReservationStatus.CONFIRMED, POLICY_VERSION_ID,
                NOW, null, null, null, NOW, NOW);
    }

    private PerformanceRoundSeat roundSeat(
            Long seatId, RoundSeatStatus status) {
        return new PerformanceRoundSeat(seatId, ROUND_ID,
                "A-" + seatId, "A", null, null, null, null,
                status, null, null, null);
    }

    private ReservationSeat reservationSeat(
            Long reservationSeatId, Long roundSeatId,
            boolean checkedIn) {
        return new ReservationSeat(reservationSeatId, RESERVATION_ID,
                roundSeatId, ReservationSeatStatus.CONFIRMED,
                null, null, checkedIn ? NOW.minusMinutes(10) : null,
                checkedIn ? ACTOR_ID : null, null, null);
    }

    private ReservationSecurityProperties properties() {
        return new ReservationSecurityProperties((short) 1,
                Map.of((short) 1,
                        "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE="),
                "YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY=",
                Duration.ofDays(90));
    }

    private void assignId(Object target, String fieldName, Long value)
            throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
