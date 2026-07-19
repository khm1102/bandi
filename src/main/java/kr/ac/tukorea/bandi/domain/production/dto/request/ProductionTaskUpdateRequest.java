package kr.ac.tukorea.bandi.domain.production.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProductionTaskUpdateRequest(
        @NotBlank @Size(max = 200) String title,
        String description,
        @NotNull LocalDate startDate,
        @NotNull LocalDate dueDate
) {
    public ProductionTaskUpdateParam toParam(Long productionTaskId) {
        return new ProductionTaskUpdateParam(productionTaskId, title,
                description, startDate, dueDate);
    }
}
