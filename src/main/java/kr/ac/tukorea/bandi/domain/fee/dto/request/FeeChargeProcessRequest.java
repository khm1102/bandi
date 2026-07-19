package kr.ac.tukorea.bandi.domain.fee.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import kr.ac.tukorea.bandi.domain.fee.model.FeeChargeStatus;

import java.util.List;

public record FeeChargeProcessRequest(
        @NotEmpty List<@Positive Long> feeChargeIds,
        @NotNull FeeChargeStatus status,
        @Size(max = 500) String reason
) {

    public FeeChargeProcessParam toParam(Long feeItemId) {
        return new FeeChargeProcessParam(feeItemId, feeChargeIds, status, reason);
    }
}
