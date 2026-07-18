package kr.ac.tukorea.bandi.domain.fee.dto.response;

import kr.ac.tukorea.bandi.domain.fee.model.FeeItemStatus;

import java.time.LocalDate;

public record FeeItemResponse(
        Long feeItemId,
        String name,
        String description,
        short referenceYear,
        String referenceTermCode,
        long amount,
        LocalDate dueDate,
        FeeItemStatus status,
        int targetCount,
        int paidCount,
        int unpaidCount,
        long paidAmount
) {
}
