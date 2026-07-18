package kr.ac.tukorea.bandi.domain.calendar.model;

import kr.ac.tukorea.bandi.domain.calendar.exception.InvalidCalendarEventException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CalendarEvent {

    private static final int MAX_TITLE_LENGTH = 150;
    private static final int MAX_PLACE_LENGTH = 200;

    private Long calendarEventId;
    private final Long teamId;
    private final String title;
    private final String description;
    private final LocalDateTime startDttm;
    private final LocalDateTime endDttm;
    private final boolean allDay;
    private final String place;
    private final Long createdByMemberId;
    private final Long updatedByMemberId;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;
    private final LocalDateTime deletedDttm;

    public CalendarEvent(Long calendarEventId, Long teamId, String title, String description,
                         LocalDateTime startDttm, LocalDateTime endDttm, boolean allDay,
                         String place, Long createdByMemberId, Long updatedByMemberId,
                         LocalDateTime createdDttm, LocalDateTime updatedDttm,
                         LocalDateTime deletedDttm) {
        validate(title, description, startDttm, endDttm, place, createdByMemberId,
                updatedByMemberId);
        this.calendarEventId = calendarEventId;
        this.teamId = teamId;
        this.title = title;
        this.description = description;
        this.startDttm = startDttm;
        this.endDttm = endDttm;
        this.allDay = allDay;
        this.place = place;
        this.createdByMemberId = createdByMemberId;
        this.updatedByMemberId = updatedByMemberId;
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
        this.deletedDttm = deletedDttm;
    }

    public static CalendarEvent create(Long teamId, String title, String description,
                                       LocalDateTime startDttm, LocalDateTime endDttm,
                                       boolean allDay, String place, Long actorMemberId) {
        return new CalendarEvent(null, teamId, title, description, startDttm, endDttm,
                allDay, place, actorMemberId, actorMemberId, null, null, null);
    }

    public CalendarEvent change(Long newTeamId, String newTitle, String newDescription,
                                LocalDateTime newStartDttm, LocalDateTime newEndDttm,
                                boolean newAllDay, String newPlace, Long actorMemberId) {
        return new CalendarEvent(calendarEventId, newTeamId, newTitle, newDescription,
                newStartDttm, newEndDttm, newAllDay, newPlace, createdByMemberId,
                actorMemberId, createdDttm, updatedDttm, deletedDttm);
    }

    private void validate(String titleValue, String descriptionValue,
                          LocalDateTime startValue, LocalDateTime endValue,
                          String placeValue, Long creatorId, Long updaterId) {
        validateText(titleValue, MAX_TITLE_LENGTH, "title");
        validateText(descriptionValue, Integer.MAX_VALUE, "description");
        validateText(placeValue, MAX_PLACE_LENGTH, "place");
        if (startValue == null || endValue == null || endValue.isBefore(startValue)) {
            throw new InvalidCalendarEventException("period");
        }
        if (creatorId == null || updaterId == null) {
            throw new InvalidCalendarEventException("actor");
        }
    }

    private void validateText(String value, int maxLength, String field) {
        if (value == null || value.isBlank() || value.length() > maxLength) {
            throw new InvalidCalendarEventException(field);
        }
    }
}
