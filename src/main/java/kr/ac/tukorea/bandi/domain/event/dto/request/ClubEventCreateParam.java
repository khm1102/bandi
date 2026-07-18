package kr.ac.tukorea.bandi.domain.event.dto.request;

import kr.ac.tukorea.bandi.domain.event.model.EventTargetScope;

import java.time.LocalDateTime;

public record ClubEventCreateParam(
        EventTargetScope targetScope,
        Long teamId,
        String title,
        String description,
        String place,
        LocalDateTime startDttm,
        LocalDateTime endDttm,
        LocalDateTime checkInStartDttm,
        LocalDateTime checkInEndDttm
) {
}
