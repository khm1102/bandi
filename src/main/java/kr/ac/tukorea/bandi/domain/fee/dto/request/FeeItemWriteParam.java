package kr.ac.tukorea.bandi.domain.fee.dto.request;

import java.time.LocalDate;

public record FeeItemWriteParam(
        String name,
        String description,
        short referenceYear,
        String referenceTermCode,
        long amount,
        LocalDate dueDate
) {
}
