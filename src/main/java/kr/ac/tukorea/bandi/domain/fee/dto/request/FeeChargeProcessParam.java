package kr.ac.tukorea.bandi.domain.fee.dto.request;

import kr.ac.tukorea.bandi.domain.fee.model.FeeChargeStatus;

import java.util.List;

public record FeeChargeProcessParam(
        Long feeItemId,
        List<Long> feeChargeIds,
        FeeChargeStatus status,
        String reason
) {
}
