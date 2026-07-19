package kr.ac.tukorea.bandi.domain.event.model;

import kr.ac.tukorea.bandi.domain.event.exception.InvalidEventAttendanceException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventAttendanceHistoryTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 1, 18, 0);

    @Test
    void 출석_상태_변경_이력을_생성한다() {
        EventAttendanceHistory history = EventAttendanceHistory.change(
                1L, AttendanceStatus.PENDING, AttendanceStatus.PRESENT,
                null, 2L, NOW);

        assertThat(history.getPreviousStatus()).isEqualTo(AttendanceStatus.PENDING);
        assertThat(history.getNewStatus()).isEqualTo(AttendanceStatus.PRESENT);
    }

    @Test
    void 같은_상태와_사유없는_인정결석_이력은_거부한다() {
        assertThatThrownBy(() -> EventAttendanceHistory.change(
                1L, AttendanceStatus.PRESENT, AttendanceStatus.PRESENT,
                null, 2L, NOW))
                .isInstanceOf(InvalidEventAttendanceException.class);
        assertThatThrownBy(() -> EventAttendanceHistory.change(
                1L, AttendanceStatus.PRESENT, AttendanceStatus.PENDING,
                null, 2L, NOW))
                .isInstanceOf(InvalidEventAttendanceException.class);
        assertThatThrownBy(() -> EventAttendanceHistory.change(
                1L, AttendanceStatus.PENDING, AttendanceStatus.EXCUSED,
                " ", 2L, NOW))
                .isInstanceOf(InvalidEventAttendanceException.class);
        assertThatThrownBy(() -> EventAttendanceHistory.change(
                1L, AttendanceStatus.PENDING, AttendanceStatus.EXCUSED,
                "가".repeat(501), 2L, NOW))
                .isInstanceOf(InvalidEventAttendanceException.class);
    }
}
