package kr.ac.tukorea.bandi.domain.fee.dto.response;

import kr.ac.tukorea.bandi.domain.fee.model.FeeChargeStatus;

import java.time.LocalDateTime;

public record FeeChargeHistoryResponse(
        Long feeChargeHistoryId,
        Long feeChargeId,
        FeeChargeStatus previousStatus,
        FeeChargeStatus newStatus,
        long amount,
        String reason,
        Long changedByMemberId,
        String changedByName,
        LocalDateTime changedDttm
) {
}
