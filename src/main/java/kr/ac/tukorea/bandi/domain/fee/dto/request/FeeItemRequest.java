package kr.ac.tukorea.bandi.domain.fee.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record FeeItemRequest(
        @NotBlank @Size(max = 150) String name,
        String description,
        @Positive short referenceYear,
        @Size(max = 20) String referenceTermCode,
        @Positive long amount,
        @NotNull LocalDate dueDate
) {

    public FeeItemWriteParam toWriteParam() {
        return new FeeItemWriteParam(name, description, referenceYear,
                referenceTermCode, amount, dueDate);
    }

    public FeeItemUpdateParam toUpdateParam(Long feeItemId) {
        return new FeeItemUpdateParam(feeItemId, name, description,
                referenceYear, referenceTermCode, amount, dueDate);
    }
}
