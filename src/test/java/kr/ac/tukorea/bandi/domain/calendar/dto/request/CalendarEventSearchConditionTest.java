package kr.ac.tukorea.bandi.domain.calendar.dto.request;

import kr.ac.tukorea.bandi.domain.calendar.exception.InvalidCalendarEventException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CalendarEventSearchConditionTest {

    private static final LocalDateTime JULY_START = LocalDateTime.of(2026, 7, 1, 0, 0);
    private static final LocalDateTime AUGUST_START = LocalDateTime.of(2026, 8, 1, 0, 0);

    @Test
    void 조회_시작과_종료는_필수다() {
        assertThatThrownBy(() -> new CalendarEventSearchCondition(null, AUGUST_START, null))
                .isInstanceOf(InvalidCalendarEventException.class);
        assertThatThrownBy(() -> new CalendarEventSearchCondition(JULY_START, null, null))
                .isInstanceOf(InvalidCalendarEventException.class);
    }

    @Test
    void 조회_종료는_시작보다_늦어야_한다() {
        assertThatThrownBy(() -> new CalendarEventSearchCondition(
                JULY_START, JULY_START, null))
                .isInstanceOf(InvalidCalendarEventException.class);
        assertThatThrownBy(() -> new CalendarEventSearchCondition(
                AUGUST_START, JULY_START, null))
                .isInstanceOf(InvalidCalendarEventException.class);
    }
}
