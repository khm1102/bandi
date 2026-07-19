package kr.ac.tukorea.bandi.domain.reservation.model;

import kr.ac.tukorea.bandi.domain.reservation.exception.InvalidReservationException;
import kr.ac.tukorea.bandi.domain.reservation.exception.InvalidReservationStateException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationSeatTest {

    private static final LocalDateTime NOW =
            LocalDateTime.of(2026, 11, 21, 18, 30);

    @Test
    void 신청_좌석을_확정한다() {
        ReservationSeat seat = ReservationSeat.confirmed(1L, 2L);

        assertThat(seat.getReservationId()).isEqualTo(1L);
        assertThat(seat.getPerformanceRoundSeatId()).isEqualTo(2L);
        assertThat(seat.getStatus())
                .isEqualTo(ReservationSeatStatus.CONFIRMED);
        assertThat(seat.isCheckedIn()).isFalse();
    }

    @Test
    void 좌석을_입장_처리한다() {
        ReservationSeat result = seat().checkIn(9L, NOW);

        assertThat(result.isCheckedIn()).isTrue();
        assertThat(result.getCheckedInByMemberId()).isEqualTo(9L);
        assertThat(result.getCheckedInDttm()).isEqualTo(NOW);
    }

    @Test
    void 이미_입장한_좌석은_동일_결과를_반환한다() {
        ReservationSeat checkedIn = seat().checkIn(9L, NOW);

        assertThat(checkedIn.checkIn(10L, NOW.plusMinutes(1)))
                .isSameAs(checkedIn);
    }

    @Test
    void 취소된_좌석은_입장_처리할_수_없다() {
        ReservationSeat cancelled = seat().cancel("전체 취소", NOW);

        assertThatThrownBy(() -> cancelled.checkIn(9L, NOW))
                .isInstanceOf(InvalidReservationStateException.class);
    }

    @Test
    void 입장_처리를_취소한다() {
        ReservationSeat checkedIn = seat().checkIn(9L, NOW);

        ReservationSeat result = checkedIn.cancelCheckIn();

        assertThat(result.isCheckedIn()).isFalse();
        assertThat(result.getCheckedInDttm()).isNull();
        assertThat(result.getCheckedInByMemberId()).isNull();
    }

    @Test
    void 입장하지_않은_좌석은_입장_취소할_수_없다() {
        assertThatThrownBy(() -> seat().cancelCheckIn())
                .isInstanceOf(InvalidReservationStateException.class);
    }

    @Test
    void 신청_좌석_취소_사유는_필수다() {
        assertThatThrownBy(() -> seat().cancel("", NOW))
                .isInstanceOf(InvalidReservationException.class);
    }

    @Test
    void 확정_좌석에는_취소_정보가_있을_수_없다() {
        assertThatThrownBy(() -> new ReservationSeat(
                3L, 1L, 2L, ReservationSeatStatus.CONFIRMED,
                NOW, "잘못된 취소", null, null, NOW, NOW))
                .isInstanceOf(InvalidReservationException.class);
    }

    private ReservationSeat seat() {
        return ReservationSeat.confirmed(1L, 2L);
    }
}
