package kr.ac.tukorea.bandi.domain.reservation.model;

import kr.ac.tukorea.bandi.domain.reservation.exception.InvalidReservationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeatEntryHistoryTest {

    private static final LocalDateTime NOW =
            LocalDateTime.of(2026, 11, 21, 18, 30);

    @Test
    void 입장_처리에는_취소_사유가_있을_수_없다() {
        assertThatThrownBy(() -> new SeatEntryHistory(
                1L, 2L, SeatEntryAction.CHECK_IN, 3L,
                NOW, "잘못된 사유", NOW, NOW))
                .isInstanceOf(InvalidReservationException.class);
    }
}
