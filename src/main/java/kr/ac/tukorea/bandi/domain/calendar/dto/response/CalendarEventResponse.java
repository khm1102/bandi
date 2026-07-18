package kr.ac.tukorea.bandi.domain.calendar.dto.response;

import kr.ac.tukorea.bandi.domain.calendar.model.CalendarEvent;

import java.time.LocalDateTime;

public record CalendarEventResponse(
        Long calendarEventId,
        Long teamId,
        String title,
        String description,
        LocalDateTime startDttm,
        LocalDateTime endDttm,
        boolean allDay,
        String place,
        Long createdByMemberId,
        Long updatedByMemberId,
        LocalDateTime updatedDttm
) {

    public static CalendarEventResponse from(CalendarEvent event) {
        return new CalendarEventResponse(event.getCalendarEventId(), event.getTeamId(),
                event.getTitle(), event.getDescription(), event.getStartDttm(),
                event.getEndDttm(), event.isAllDay(), event.getPlace(),
                event.getCreatedByMemberId(), event.getUpdatedByMemberId(),
                event.getUpdatedDttm());
    }
}
