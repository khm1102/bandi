package kr.ac.tukorea.bandi.domain.fee.dto.request;

import java.time.LocalDate;

public record FeeItemUpdateParam(
        Long feeItemId,
        String name,
        String description,
        short referenceYear,
        String referenceTermCode,
        long amount,
        LocalDate dueDate
) {
}
