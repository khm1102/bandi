package kr.ac.tukorea.bandi.domain.checklist.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import kr.ac.tukorea.bandi.domain.checklist.model.ChecklistScope;

public record ChecklistItemCreateRequest(
        @NotNull @Positive Long performanceProjectId,
        @Positive Long performanceRoundId,
        @NotNull @Positive Long teamId,
        @NotNull ChecklistScope scope,
        @NotBlank String content,
        boolean required,
        @PositiveOrZero int displayOrder
) {
    public ChecklistItemCreateParam toParam() {
        return new ChecklistItemCreateParam(performanceProjectId,
                performanceRoundId, teamId, scope, content, required,
                displayOrder);
    }
}
