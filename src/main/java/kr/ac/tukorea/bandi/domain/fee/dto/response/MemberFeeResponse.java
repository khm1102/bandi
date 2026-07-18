package kr.ac.tukorea.bandi.domain.fee.dto.response;

import kr.ac.tukorea.bandi.domain.fee.model.FeeChargeStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MemberFeeResponse(
        Long feeChargeId,
        Long feeItemId,
        String itemName,
        short referenceYear,
        String referenceTermCode,
        long chargedAmount,
        FeeChargeStatus status,
        LocalDate dueDate,
        LocalDateTime paidDttm
) {
}
