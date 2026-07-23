package kr.ac.tukorea.bandi.domain.calendar.model;

import kr.ac.tukorea.bandi.domain.calendar.exception.InvalidCalendarEventException;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.LocalTime;

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
    private final CalendarEventColor colorCode;
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
        this(calendarEventId, teamId, title, description, startDttm, endDttm, allDay, place,
                CalendarEventColor.NAVY, createdByMemberId, updatedByMemberId, createdDttm,
                updatedDttm, deletedDttm);
    }

    public CalendarEvent(Long calendarEventId, Long teamId, String title, String description,
                         LocalDateTime startDttm, LocalDateTime endDttm, boolean allDay,
                         String place, CalendarEventColor colorCode, Long createdByMemberId,
                         Long updatedByMemberId, LocalDateTime createdDttm,
                         LocalDateTime updatedDttm, LocalDateTime deletedDttm) {
        validate(title, description, startDttm, endDttm, allDay, place, colorCode,
                createdByMemberId, updatedByMemberId);
        this.calendarEventId = calendarEventId;
        this.teamId = teamId;
        this.title = title;
        this.description = description;
        this.startDttm = startDttm;
        this.endDttm = endDttm;
        this.allDay = allDay;
        this.place = place;
        this.colorCode = colorCode;
        this.createdByMemberId = createdByMemberId;
        this.updatedByMemberId = updatedByMemberId;
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
        this.deletedDttm = deletedDttm;
    }

    public static CalendarEvent create(Long teamId, String title, String description,
                                       LocalDateTime startDttm, LocalDateTime endDttm,
                                       boolean allDay, String place, Long actorMemberId) {
        return create(teamId, title, description, startDttm, endDttm, allDay, place,
                CalendarEventColor.NAVY, actorMemberId);
    }

    public static CalendarEvent create(Long teamId, String title, String description,
                                       LocalDateTime startDttm, LocalDateTime endDttm,
                                       boolean allDay, String place, CalendarEventColor colorCode,
                                       Long actorMemberId) {
        return new CalendarEvent(null, teamId, title, description, startDttm, endDttm,
                allDay, place, colorCode, actorMemberId, actorMemberId, null, null, null);
    }

    public CalendarEvent change(Long newTeamId, String newTitle, String newDescription,
                                LocalDateTime newStartDttm, LocalDateTime newEndDttm,
                                boolean newAllDay, String newPlace, Long actorMemberId) {
        return change(newTeamId, newTitle, newDescription, newStartDttm, newEndDttm,
                newAllDay, newPlace, colorCode, actorMemberId);
    }

    public CalendarEvent change(Long newTeamId, String newTitle, String newDescription,
                                LocalDateTime newStartDttm, LocalDateTime newEndDttm,
                                boolean newAllDay, String newPlace, CalendarEventColor newColorCode,
                                Long actorMemberId) {
        return new CalendarEvent(calendarEventId, newTeamId, newTitle, newDescription,
                newStartDttm, newEndDttm, newAllDay, newPlace, newColorCode, createdByMemberId,
                actorMemberId, createdDttm, updatedDttm, deletedDttm);
    }

    private void validate(String titleValue, String descriptionValue,
                          LocalDateTime startValue, LocalDateTime endValue,
                          boolean allDayValue, String placeValue, CalendarEventColor colorValue,
                          Long creatorId,
                          Long updaterId) {
        validateText(titleValue, MAX_TITLE_LENGTH, "title");
        validateOptionalText(descriptionValue, Integer.MAX_VALUE, "description");
        validateOptionalText(placeValue, MAX_PLACE_LENGTH, "place");
        if (colorValue == null) {
            throw new InvalidCalendarEventException("colorCode");
        }
        if (startValue == null || endValue == null || !endValue.isAfter(startValue)) {
            throw new InvalidCalendarEventException("period");
        }
        if (allDayValue && (!startValue.toLocalTime().equals(LocalTime.MIDNIGHT)
                || !endValue.toLocalTime().equals(LocalTime.MIDNIGHT))) {
            throw new InvalidCalendarEventException("allDayPeriod");
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

    private void validateOptionalText(String value, int maxLength, String field) {
        if (value != null && value.length() > maxLength) {
            throw new InvalidCalendarEventException(field);
        }
    }
}
