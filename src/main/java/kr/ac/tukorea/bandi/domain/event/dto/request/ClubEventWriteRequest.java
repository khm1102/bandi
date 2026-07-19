package kr.ac.tukorea.bandi.domain.event.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import kr.ac.tukorea.bandi.domain.event.model.EventTargetScope;

import java.time.LocalDateTime;

public record ClubEventWriteRequest(
        @NotNull EventTargetScope targetScope,
        @Positive Long teamId,
        @NotBlank @Size(max = 150) String title,
        String description,
        @NotBlank @Size(max = 150) String place,
        @NotNull LocalDateTime startDttm,
        @NotNull LocalDateTime endDttm,
        @NotNull LocalDateTime checkInStartDttm,
        @NotNull LocalDateTime checkInEndDttm
) {

    public ClubEventCreateParam toCreateParam() {
        return new ClubEventCreateParam(targetScope, teamId, title, description,
                place, startDttm, endDttm, checkInStartDttm, checkInEndDttm);
    }

    public ClubEventUpdateParam toUpdateParam(Long clubEventId) {
        return new ClubEventUpdateParam(clubEventId, targetScope, teamId, title,
                description, place, startDttm, endDttm,
                checkInStartDttm, checkInEndDttm);
    }
}
