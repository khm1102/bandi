package kr.ac.tukorea.bandi.domain.calendar.dto.request;

import kr.ac.tukorea.bandi.domain.calendar.model.CalendarEventColor;

import java.time.LocalDateTime;

public record CalendarEventCreateParam(
        Long teamId,
        String title,
        String description,
        LocalDateTime startDttm,
        LocalDateTime endDttm,
        boolean allDay,
        String place,
        CalendarEventColor colorCode
) {

    public CalendarEventCreateParam(Long teamId, String title, String description,
                                    LocalDateTime startDttm, LocalDateTime endDttm,
                                    boolean allDay, String place) {
        this(teamId, title, description, startDttm, endDttm, allDay, place, null);
    }
}
