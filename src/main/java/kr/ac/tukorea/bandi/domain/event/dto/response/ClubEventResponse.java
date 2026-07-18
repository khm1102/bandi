package kr.ac.tukorea.bandi.domain.event.dto.response;

import kr.ac.tukorea.bandi.domain.event.model.ClubEventStatus;
import kr.ac.tukorea.bandi.domain.event.model.EventTargetScope;

import java.time.LocalDateTime;

public record ClubEventResponse(
        Long clubEventId,
        Long calendarEventId,
        EventTargetScope targetScope,
        Long teamId,
        String teamName,
        String title,
        String description,
        String place,
        LocalDateTime startDttm,
        LocalDateTime endDttm,
        LocalDateTime checkInStartDttm,
        LocalDateTime checkInEndDttm,
        ClubEventStatus status,
        int targetCount
) {
}
