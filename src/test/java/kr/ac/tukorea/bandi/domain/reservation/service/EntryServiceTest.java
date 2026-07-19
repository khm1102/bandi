package kr.ac.tukorea.bandi.domain.reservation.service;

import kr.ac.tukorea.bandi.domain.performance.service.PerformanceRoundService;
import kr.ac.tukorea.bandi.domain.reservation.dto.response.ReservationMetricsResponse;
import kr.ac.tukorea.bandi.domain.reservation.dto.response.ReservationSeatResponse;
import kr.ac.tukorea.bandi.domain.reservation.exception.InvalidReservationStateException;
import kr.ac.tukorea.bandi.domain.reservation.exception.InvalidReservationTokenException;
import kr.ac.tukorea.bandi.domain.reservation.mapper.ReservationMapper;
import kr.ac.tukorea.bandi.domain.reservation.model.Reservation;
import kr.ac.tukorea.bandi.domain.reservation.model.ReservationSeat;
import kr.ac.tukorea.bandi.domain.reservation.model.ReservationSeatStatus;
import kr.ac.tukorea.bandi.domain.reservation.model.ReservationStatus;
import kr.ac.tukorea.bandi.domain.reservation.model.SeatEntryAction;
import kr.ac.tukorea.bandi.domain.reservation.model.SeatEntryHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EntryServiceTest {

    private static final Long ACTOR_ID = 1L;
    private static final Long PROJECT_ID = 10L;
    private static final Long ROUND_ID = 20L;
    private static final Long RESERVATION_ID = 30L;
    private static final Long FIRST_RESERVATION_SEAT_ID = 41L;
    private static final Long SECOND_RESERVATION_SEAT_ID = 42L;
    private static final String ENTRY_TOKEN = "entry-token";
    private static final String ENTRY_HASH = "b".repeat(64);
    private static final LocalDateTime NOW =
            LocalDateTime.of(2026, 11, 21, 18, 40);
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-11-21T09:40:00Z"),
            ZoneId.of("Asia/Seoul"));

    @Mock
    private ReservationMapper reservationMapper;
    @Mock
    private PerformanceRoundService roundService;
    @Mock
    private ReservationDataProtector dataProtector;
    @Mock
    private ReservationTokenGenerator tokenGenerator;

    private EntryService service;

    @BeforeEach
    void setUp() {
        service = new EntryService(reservationMapper, roundService,
                dataProtector, tokenGenerator, CLOCK);
    }

    @Test
    void 운영진이_QR로_신청과_좌석별_입장_상태를_조회한다() {
        givenEntryReservation(ReservationStatus.CONFIRMED);
        given(reservationMapper.searchReservationSeatResponses(
                RESERVATION_ID)).willReturn(List.of(seatResponse(false)));
        givenApplicantDecryption();

        var result = service.lookupByEntryToken(
                ACTOR_ID, ROUND_ID, ENTRY_TOKEN);

        assertThat(result.reservationId()).isEqualTo(RESERVATION_ID);
        assertThat(result.seats()).hasSize(1);
        assertThat(result.applicantName()).isEqualTo("홍길동");
    }

    @Test
    void 다른_회차의_QR은_입장_처리할_수_없다() {
        given(roundService.isEntryOpen(ACTOR_ID, ROUND_ID + 1))
                .willReturn(true);
        given(tokenGenerator.hash(ENTRY_TOKEN)).willReturn(ENTRY_HASH);
        given(reservationMapper.lookupReservationByEntryTokenHashForUpdate(
                ENTRY_HASH)).willReturn(Optional.of(
                        reservation(ReservationStatus.CONFIRMED)));

        assertThatThrownBy(() -> service.lookupByEntryToken(
                ACTOR_ID, ROUND_ID + 1, ENTRY_TOKEN))
                .isInstanceOf(InvalidReservationTokenException.class);
    }

    @Test
    void 취소된_신청은_입장_처리할_수_없다() {
        givenEntryReservation(ReservationStatus.CANCELLED);

        assertThatThrownBy(() -> service.lookupByEntryToken(
                ACTOR_ID, ROUND_ID, ENTRY_TOKEN))
                .isInstanceOf(InvalidReservationStateException.class);
    }

    @Test
    void 입장_오픈_상태가_아니면_QR을_처리할_수_없다() {
        given(roundService.isEntryOpen(ACTOR_ID, ROUND_ID))
                .willReturn(false);

        assertThatThrownBy(() -> service.lookupByEntryToken(
                ACTOR_ID, ROUND_ID, ENTRY_TOKEN))
                .isInstanceOf(InvalidReservationStateException.class);

        verify(reservationMapper, never())
                .lookupReservationByEntryTokenHashForUpdate(any());
    }

    @Test
    void 선택한_좌석만_입장_처리하고_기존_입장은_멱등하게_유지한다() {
        givenEntryReservation(ReservationStatus.CONFIRMED);
        given(reservationMapper.searchReservationSeatsForUpdate(
                RESERVATION_ID)).willReturn(List.of(
                        reservationSeat(FIRST_RESERVATION_SEAT_ID, false),
                        reservationSeat(SECOND_RESERVATION_SEAT_ID, true)));

        service.checkIn(ACTOR_ID, ROUND_ID, ENTRY_TOKEN,
                List.of(FIRST_RESERVATION_SEAT_ID,
                        SECOND_RESERVATION_SEAT_ID));

        verify(reservationMapper, times(1)).updateReservationSeat(any());
        ArgumentCaptor<SeatEntryHistory> captor =
                ArgumentCaptor.forClass(SeatEntryHistory.class);
        verify(reservationMapper).insertSeatEntryHistory(captor.capture());
        assertThat(captor.getValue().getAction())
                .isEqualTo(SeatEntryAction.CHECK_IN);
    }

    @Test
    void QR에_속하지_않는_좌석은_입장_처리할_수_없다() {
        givenEntryReservation(ReservationStatus.CONFIRMED);
        given(reservationMapper.searchReservationSeatsForUpdate(
                RESERVATION_ID)).willReturn(List.of(
                        reservationSeat(FIRST_RESERVATION_SEAT_ID, false)));

        assertThatThrownBy(() -> service.checkIn(
                ACTOR_ID, ROUND_ID, ENTRY_TOKEN,
                List.of(SECOND_RESERVATION_SEAT_ID)))
                .isInstanceOf(InvalidReservationTokenException.class);
    }

    @Test
    void 잘못_처리한_입장을_사유와_함께_취소한다() {
        ReservationSeat checkedIn = reservationSeat(
                FIRST_RESERVATION_SEAT_ID, true);
        given(roundService.isEntryOpen(ACTOR_ID, ROUND_ID))
                .willReturn(true);
        given(reservationMapper.lookupReservationSeatForUpdate(
                FIRST_RESERVATION_SEAT_ID))
                .willReturn(Optional.of(checkedIn));
        given(reservationMapper.lookupReservationByIdForUpdate(
                RESERVATION_ID)).willReturn(Optional.of(reservation(
                        ReservationStatus.CONFIRMED)));

        service.cancelCheckIn(ACTOR_ID, ROUND_ID,
                FIRST_RESERVATION_SEAT_ID, "좌석 선택 오류");

        verify(reservationMapper).updateReservationSeat(any());
        ArgumentCaptor<SeatEntryHistory> captor =
                ArgumentCaptor.forClass(SeatEntryHistory.class);
        verify(reservationMapper).insertSeatEntryHistory(captor.capture());
        assertThat(captor.getValue().getAction())
                .isEqualTo(SeatEntryAction.CANCEL_CHECK_IN);
        assertThat(captor.getValue().getReason())
                .isEqualTo("좌석 선택 오류");
    }

    @Test
    void 신청번호와_이름이_모두_맞아야_보조_조회된다() {
        given(roundService.isEntryOpen(ACTOR_ID, ROUND_ID))
                .willReturn(true);
        given(reservationMapper.lookupReservationByNo("R20261121ABC"))
                .willReturn(Optional.of(reservation(
                        ReservationStatus.CONFIRMED)));
        given(dataProtector.decryptName(any(byte[].class), any(Short.class)))
                .willReturn("다른 이름");

        assertThatThrownBy(() -> service.lookupByNumberAndName(
                ACTOR_ID, ROUND_ID, "R20261121ABC", "홍길동"))
                .isInstanceOf(InvalidReservationTokenException.class);
    }

    @Test
    void 신청번호와_이름으로_선택_좌석을_입장_처리한다() {
        givenManualReservation("홍길동");
        given(reservationMapper.searchReservationSeatsForUpdate(
                RESERVATION_ID)).willReturn(List.of(
                        reservationSeat(FIRST_RESERVATION_SEAT_ID, false)));

        service.checkInByNumberAndName(ACTOR_ID, ROUND_ID,
                "R20261121ABC", "홍길동",
                List.of(FIRST_RESERVATION_SEAT_ID));

        verify(reservationMapper).updateReservationSeat(any());
        verify(reservationMapper).insertSeatEntryHistory(any());
    }

    @Test
    void 보조_입장에서도_다른_신청의_좌석을_처리할_수_없다() {
        givenManualReservation("홍길동");
        given(reservationMapper.searchReservationSeatsForUpdate(
                RESERVATION_ID)).willReturn(List.of(
                        reservationSeat(FIRST_RESERVATION_SEAT_ID, false)));

        assertThatThrownBy(() -> service.checkInByNumberAndName(
                ACTOR_ID, ROUND_ID, "R20261121ABC", "홍길동",
                List.of(SECOND_RESERVATION_SEAT_ID)))
                .isInstanceOf(InvalidReservationTokenException.class);
    }

    @Test
    void 회차별_신청과_입장_지표를_조회한다() {
        ReservationMetricsResponse metrics =
                new ReservationMetricsResponse(3, 6, 4, 2, 1,
                        new BigDecimal("66.67"));
        given(reservationMapper.lookupReservationMetrics(ROUND_ID))
                .willReturn(metrics);

        ReservationMetricsResponse result = service.lookupMetrics(
                ACTOR_ID, PROJECT_ID, ROUND_ID);

        assertThat(result).isEqualTo(metrics);
        verify(roundService).validateManage(
                ACTOR_ID, ROUND_ID, PROJECT_ID);
    }

    private void givenEntryReservation(
            ReservationStatus reservationStatus) {
        given(roundService.isEntryOpen(ACTOR_ID, ROUND_ID))
                .willReturn(true);
        given(tokenGenerator.hash(ENTRY_TOKEN)).willReturn(ENTRY_HASH);
        given(reservationMapper.lookupReservationByEntryTokenHashForUpdate(
                ENTRY_HASH)).willReturn(Optional.of(
                        reservation(reservationStatus)));
    }

    private void givenApplicantDecryption() {
        given(dataProtector.decryptName(any(byte[].class), any(Short.class)))
                .willReturn("홍길동");
        given(dataProtector.decryptPhone(any(byte[].class), any(Short.class)))
                .willReturn("01012345678");
    }

    private void givenManualReservation(String applicantName) {
        given(roundService.isEntryOpen(ACTOR_ID, ROUND_ID))
                .willReturn(true);
        given(reservationMapper.lookupReservationByNo("R20261121ABC"))
                .willReturn(Optional.of(reservation(
                        ReservationStatus.CONFIRMED)));
        given(dataProtector.decryptName(any(byte[].class), any(Short.class)))
                .willReturn(applicantName);
    }

    private Reservation reservation(ReservationStatus status) {
        LocalDateTime cancelled = status == ReservationStatus.CANCELLED
                ? NOW.minusDays(1) : null;
        return new Reservation(RESERVATION_ID, ROUND_ID,
                "R20261121ABC", "a".repeat(64), ENTRY_HASH,
                new byte[]{1}, new byte[]{2}, "c".repeat(64),
                (short) 1, status, 50L, NOW.minusDays(10),
                cancelled, cancelled == null ? null : "취소",
                null, NOW.minusDays(10), NOW.minusDays(10));
    }

    private ReservationSeat reservationSeat(
            Long reservationSeatId, boolean checkedIn) {
        return new ReservationSeat(reservationSeatId, RESERVATION_ID,
                reservationSeatId + 100,
                ReservationSeatStatus.CONFIRMED, null, null,
                checkedIn ? NOW.minusMinutes(10) : null,
                checkedIn ? ACTOR_ID : null, null, null);
    }

    private ReservationSeatResponse seatResponse(boolean checkedIn) {
        return new ReservationSeatResponse(FIRST_RESERVATION_SEAT_ID,
                141L, "A-1", "A", "1", "1",
                ReservationSeatStatus.CONFIRMED,
                checkedIn ? NOW.minusMinutes(10) : null,
                checkedIn ? ACTOR_ID : null);
    }

}
