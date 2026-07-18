package kr.ac.tukorea.bandi.domain.fee.dto.response;

import kr.ac.tukorea.bandi.domain.fee.model.FeeChargeStatus;

import java.time.LocalDateTime;

public record FeeChargeResponse(
        Long feeChargeId,
        Long memberId,
        String memberName,
        long chargedAmount,
        FeeChargeStatus status,
        LocalDateTime paidDttm,
        String processedByName,
        String processNote
) {
}
