package kr.ac.tukorea.bandi.domain.calendar.dto.request;

import java.time.LocalDateTime;

public record CalendarEventUpdateParam(
        Long calendarEventId,
        Long teamId,
        String title,
        String description,
        LocalDateTime startDttm,
        LocalDateTime endDttm,
        boolean allDay,
        String place
) {
}
