package kr.ac.tukorea.bandi.domain.production.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProductionTaskCreateRequest(
        @NotNull @Positive Long performanceProjectId,
        @NotNull @Positive Long teamId,
        @NotBlank @Size(max = 200) String title,
        String description,
        @NotNull LocalDate startDate,
        @NotNull LocalDate dueDate
) {
    public ProductionTaskCreateParam toParam() {
        return new ProductionTaskCreateParam(performanceProjectId, teamId,
                title, description, startDate, dueDate);
    }
}
