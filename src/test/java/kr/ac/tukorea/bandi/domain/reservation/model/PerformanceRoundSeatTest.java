package kr.ac.tukorea.bandi.domain.reservation.model;

import kr.ac.tukorea.bandi.domain.reservation.exception.InvalidReservationException;
import kr.ac.tukorea.bandi.domain.reservation.exception.InvalidReservationStateException;
import kr.ac.tukorea.bandi.domain.reservation.exception.SeatUnavailableException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PerformanceRoundSeatTest {

    @Test
    void 회차_좌석을_생성한다() {
        PerformanceRoundSeat seat = PerformanceRoundSeat.available(
                11L, "A-1", "A", "1", "1", 0, 0, null);

        assertThat(seat.getPerformanceRoundId()).isEqualTo(11L);
        assertThat(seat.getSeatLabel()).isEqualTo("A-1");
        assertThat(seat.getStatus()).isEqualTo(RoundSeatStatus.AVAILABLE);
    }

    @Test
    void 좌석_라벨은_공백일_수_없다() {
        assertThatThrownBy(() -> PerformanceRoundSeat.available(
                11L, " ", null, null, null, null, null, null))
                .isInstanceOf(InvalidReservationException.class);
    }

    @Test
    void 표시_위치는_음수일_수_없다() {
        assertThatThrownBy(() -> PerformanceRoundSeat.available(
                11L, "A-1", null, null, null, -1, 0, null))
                .isInstanceOf(InvalidReservationException.class);
    }

    @Test
    void 좌석을_차단하고_다시_열수_있다() {
        PerformanceRoundSeat seat = seat();

        PerformanceRoundSeat blocked = seat.changeStatus(
                RoundSeatStatus.BLOCKED);
        PerformanceRoundSeat available = blocked.changeStatus(
                RoundSeatStatus.AVAILABLE);

        assertThat(blocked.getStatus()).isEqualTo(RoundSeatStatus.BLOCKED);
        assertThat(available.getStatus())
                .isEqualTo(RoundSeatStatus.AVAILABLE);
    }

    @Test
    void 같은_상태로는_변경할_수_없다() {
        assertThatThrownBy(() -> seat().changeStatus(
                RoundSeatStatus.AVAILABLE))
                .isInstanceOf(InvalidReservationStateException.class);
    }

    @Test
    void 차단된_좌석은_신청할_수_없다() {
        PerformanceRoundSeat blocked = seat().changeStatus(
                RoundSeatStatus.BLOCKED);

        assertThatThrownBy(blocked::validateReservable)
                .isInstanceOf(SeatUnavailableException.class);
    }

    @Test
    void 다른_회차의_좌석은_신청할_수_없다() {
        assertThatThrownBy(() -> seat().validateRound(12L))
                .isInstanceOf(InvalidReservationException.class);
    }

    private PerformanceRoundSeat seat() {
        return PerformanceRoundSeat.available(
                11L, "A-1", "A", "1", "1", 0, 0, null);
    }
}
