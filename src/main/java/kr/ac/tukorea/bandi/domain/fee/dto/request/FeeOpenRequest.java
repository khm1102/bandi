package kr.ac.tukorea.bandi.domain.fee.dto.request;

import jakarta.validation.constraints.Positive;

import java.util.List;

public record FeeOpenRequest(
        List<@Positive Long> selectedMemberIds
) {

    public FeeOpenParam toParam(Long feeItemId) {
        return new FeeOpenParam(feeItemId, selectedMemberIds);
    }
}
