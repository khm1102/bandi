package kr.ac.tukorea.bandi.domain.event.model;

import kr.ac.tukorea.bandi.domain.event.exception.InvalidClubEventException;
import kr.ac.tukorea.bandi.domain.event.exception.InvalidClubEventStateException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ClubEvent {

    private static final int MAX_TITLE_LENGTH = 150;
    private static final int MAX_PLACE_LENGTH = 150;

    private Long clubEventId;
    private final Long calendarEventId;
    private final EventTargetScope targetScope;
    private final Long teamId;
    private final String title;
    private final String description;
    private final String place;
    private final LocalDateTime startDttm;
    private final LocalDateTime endDttm;
    private final LocalDateTime checkInStartDttm;
    private final LocalDateTime checkInEndDttm;
    private final ClubEventStatus status;
    private final Long createdByMemberId;
    private final Long updatedByMemberId;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;
    private final LocalDateTime deletedDttm;

    public ClubEvent(Long clubEventId, Long calendarEventId,
                     EventTargetScope targetScope, Long teamId,
                     String title, String description, String place,
                     LocalDateTime startDttm, LocalDateTime endDttm,
                     LocalDateTime checkInStartDttm,
                     LocalDateTime checkInEndDttm, ClubEventStatus status,
                     Long createdByMemberId, Long updatedByMemberId,
                     LocalDateTime createdDttm, LocalDateTime updatedDttm,
                     LocalDateTime deletedDttm) {
        validate(targetScope, teamId, title, place,
                startDttm, endDttm, checkInStartDttm, checkInEndDttm,
                status, createdByMemberId, updatedByMemberId);
        this.clubEventId = clubEventId;
        this.calendarEventId = calendarEventId;
        this.targetScope = targetScope;
        this.teamId = teamId;
        this.title = title.strip();
        this.description = normalize(description);
        this.place = place.strip();
        this.startDttm = startDttm;
        this.endDttm = endDttm;
        this.checkInStartDttm = checkInStartDttm;
        this.checkInEndDttm = checkInEndDttm;
        this.status = status;
        this.createdByMemberId = createdByMemberId;
        this.updatedByMemberId = updatedByMemberId;
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
        this.deletedDttm = deletedDttm;
    }

    public static ClubEvent draft(Long calendarEventId,
                                  EventTargetScope targetScope, Long teamId,
                                  String title, String description, String place,
                                  LocalDateTime startDttm, LocalDateTime endDttm,
                                  LocalDateTime checkInStartDttm,
                                  LocalDateTime checkInEndDttm,
                                  Long actorMemberId) {
        return new ClubEvent(null, calendarEventId, targetScope, teamId,
                title, description, place, startDttm, endDttm,
                checkInStartDttm, checkInEndDttm, ClubEventStatus.DRAFT,
                actorMemberId, actorMemberId, null, null, null);
    }

    public ClubEvent edit(EventTargetScope newTargetScope, Long newTeamId,
                          String newTitle, String newDescription, String newPlace,
                          LocalDateTime newStartDttm, LocalDateTime newEndDttm,
                          LocalDateTime newCheckInStartDttm,
                          LocalDateTime newCheckInEndDttm, Long actorMemberId) {
        validateStatus(ClubEventStatus.DRAFT, "edit");
        return copy(newTargetScope, newTeamId, newTitle, newDescription,
                newPlace, newStartDttm, newEndDttm, newCheckInStartDttm,
                newCheckInEndDttm, status, actorMemberId);
    }

    public ClubEvent schedule(Long actorMemberId) {
        validateStatus(ClubEventStatus.DRAFT, "schedule");
        return copy(targetScope, teamId, title, description, place,
                startDttm, endDttm, checkInStartDttm, checkInEndDttm,
                ClubEventStatus.SCHEDULED, actorMemberId);
    }

    public ClubEvent openCheckIn(Long actorMemberId, LocalDateTime currentDttm) {
        if (status != ClubEventStatus.SCHEDULED && status != ClubEventStatus.CLOSED) {
            throw new InvalidClubEventStateException("check-in-open");
        }
        validateActor(actorMemberId);
        validateCheckInWindow(currentDttm);
        return copy(targetScope, teamId, title, description, place,
                startDttm, endDttm, checkInStartDttm, checkInEndDttm,
                ClubEventStatus.IN_PROGRESS, actorMemberId);
    }

    public ClubEvent closeCheckIn(Long actorMemberId) {
        validateStatus(ClubEventStatus.IN_PROGRESS, "check-in-close");
        return copy(targetScope, teamId, title, description, place,
                startDttm, endDttm, checkInStartDttm, checkInEndDttm,
                ClubEventStatus.CLOSED, actorMemberId);
    }

    public ClubEvent archive(Long actorMemberId) {
        if (status == ClubEventStatus.ARCHIVED) {
            throw new InvalidClubEventStateException("archive");
        }
        return copy(targetScope, teamId, title, description, place,
                startDttm, endDttm, checkInStartDttm, checkInEndDttm,
                ClubEventStatus.ARCHIVED, actorMemberId);
    }

    public void validateAttendanceProcessing(LocalDateTime currentDttm) {
        validateStatus(ClubEventStatus.IN_PROGRESS, "attendance-processing");
        validateCheckInWindow(currentDttm);
    }

    private ClubEvent copy(EventTargetScope newTargetScope, Long newTeamId,
                           String newTitle, String newDescription,
                           String newPlace, LocalDateTime newStartDttm,
                           LocalDateTime newEndDttm,
                           LocalDateTime newCheckInStartDttm,
                           LocalDateTime newCheckInEndDttm,
                           ClubEventStatus newStatus, Long actorMemberId) {
        return new ClubEvent(clubEventId, calendarEventId, newTargetScope,
                newTeamId, newTitle, newDescription, newPlace, newStartDttm,
                newEndDttm, newCheckInStartDttm, newCheckInEndDttm, newStatus,
                createdByMemberId, actorMemberId, createdDttm, updatedDttm,
                deletedDttm);
    }

    private void validate(EventTargetScope targetScope, Long targetTeamId,
                          String targetTitle, String targetPlace,
                          LocalDateTime targetStartDttm,
                          LocalDateTime targetEndDttm,
                          LocalDateTime targetCheckInStartDttm,
                          LocalDateTime targetCheckInEndDttm,
                          ClubEventStatus targetStatus,
                          Long creatorId, Long updaterId) {
        if (targetScope == null || targetStatus == null
                || creatorId == null || updaterId == null) {
            throw new InvalidClubEventException("required");
        }
        validateText(targetTitle, MAX_TITLE_LENGTH, "title");
        validateText(targetPlace, MAX_PLACE_LENGTH, "place");
        validateScope(targetScope, targetTeamId);
        validateTime(targetStartDttm, targetEndDttm,
                targetCheckInStartDttm, targetCheckInEndDttm);
    }

    private void validateScope(EventTargetScope targetScope, Long targetTeamId) {
        boolean valid = targetScope == EventTargetScope.TEAM
                ? targetTeamId != null : targetTeamId == null;
        if (!valid) {
            throw new InvalidClubEventException("target-scope");
        }
    }

    private void validateTime(LocalDateTime targetStartDttm,
                              LocalDateTime targetEndDttm,
                              LocalDateTime targetCheckInStartDttm,
                              LocalDateTime targetCheckInEndDttm) {
        if (targetStartDttm == null || targetEndDttm == null
                || targetCheckInStartDttm == null || targetCheckInEndDttm == null
                || !targetEndDttm.isAfter(targetStartDttm)
                || !targetCheckInEndDttm.isAfter(targetCheckInStartDttm)) {
            throw new InvalidClubEventException("time");
        }
    }

    private void validateStatus(ClubEventStatus expectedStatus, String operation) {
        if (status != expectedStatus) {
            throw new InvalidClubEventStateException(operation);
        }
    }

    private void validateCheckInWindow(LocalDateTime currentDttm) {
        if (currentDttm == null || currentDttm.isBefore(checkInStartDttm)
                || currentDttm.isAfter(checkInEndDttm)) {
            throw new InvalidClubEventStateException("check-in-window");
        }
    }

    private void validateActor(Long actorMemberId) {
        if (actorMemberId == null) {
            throw new InvalidClubEventException("actor");
        }
    }

    private void validateText(String value, int maxLength, String field) {
        if (value == null || value.isBlank() || value.length() > maxLength) {
            throw new InvalidClubEventException(field);
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
