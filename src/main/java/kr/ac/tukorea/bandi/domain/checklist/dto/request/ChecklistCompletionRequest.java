package kr.ac.tukorea.bandi.domain.checklist.dto.request;

import jakarta.validation.constraints.Size;

public record ChecklistCompletionRequest(
        boolean completed,
        @Size(max = 500) String reason
) {
}
