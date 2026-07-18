package kr.ac.tukorea.bandi.domain.calendar.dto.request;

import kr.ac.tukorea.bandi.domain.calendar.exception.InvalidCalendarEventException;

import java.time.LocalDateTime;

public record CalendarEventSearchCondition(
        LocalDateTime rangeStart,
        LocalDateTime rangeEnd,
        Long teamId
) {

    public CalendarEventSearchCondition {
        if (rangeStart == null || rangeEnd == null || !rangeStart.isBefore(rangeEnd)) {
            throw new InvalidCalendarEventException("search-period");
        }
    }
}
