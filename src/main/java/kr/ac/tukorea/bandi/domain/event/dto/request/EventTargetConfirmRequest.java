package kr.ac.tukorea.bandi.domain.event.dto.request;

import jakarta.validation.constraints.Positive;

import java.util.List;

public record EventTargetConfirmRequest(
        List<@Positive Long> selectedMemberIds
) {

    public EventTargetConfirmParam toParam(Long clubEventId) {
        return new EventTargetConfirmParam(clubEventId, selectedMemberIds);
    }
}
