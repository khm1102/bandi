package kr.ac.tukorea.bandi.domain.performance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductionCreditRequest(
        @NotNull @Positive Long performanceProjectId,
        @NotBlank @Size(max = 100) String creditRole,
        @NotBlank @Size(max = 100) String publicName,
        @Positive Long publicProfileId,
        @PositiveOrZero int displayOrder
) {

    public ProductionCreditWriteParam toParam(Long creditId) {
        return new ProductionCreditWriteParam(creditId,
                performanceProjectId, creditRole, publicName,
                publicProfileId, displayOrder);
    }
}
