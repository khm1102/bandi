package kr.ac.tukorea.bandi.domain.checklist.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record ChecklistItemUpdateRequest(
        @NotBlank String content,
        boolean required,
        @PositiveOrZero int displayOrder
) {
    public ChecklistItemUpdateParam toParam(Long checklistItemId) {
        return new ChecklistItemUpdateParam(checklistItemId, content,
                required, displayOrder);
    }
}
