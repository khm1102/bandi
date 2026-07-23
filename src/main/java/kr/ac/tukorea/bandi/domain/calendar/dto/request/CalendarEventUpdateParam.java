package kr.ac.tukorea.bandi.domain.calendar.dto.request;

import kr.ac.tukorea.bandi.domain.calendar.model.CalendarEventColor;

import java.time.LocalDateTime;

public record CalendarEventUpdateParam(
        Long calendarEventId,
        Long teamId,
        String title,
        String description,
        LocalDateTime startDttm,
        LocalDateTime endDttm,
        boolean allDay,
        String place,
        CalendarEventColor colorCode
) {

    public CalendarEventUpdateParam(Long calendarEventId, Long teamId, String title,
                                    String description, LocalDateTime startDttm,
                                    LocalDateTime endDttm, boolean allDay, String place) {
        this(calendarEventId, teamId, title, description, startDttm, endDttm, allDay, place, null);
    }
}
