package kr.ac.tukorea.bandi.domain.calendar.dto.request;

import java.time.LocalDateTime;

public record CalendarEventCreateParam(
        Long teamId,
        String title,
        String description,
        LocalDateTime startDttm,
        LocalDateTime endDttm,
        boolean allDay,
        String place
) {
}
