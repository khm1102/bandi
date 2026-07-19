package kr.ac.tukorea.bandi.domain.checklist.dto.request;

public record ChecklistCompletionParam(
        Long checklistItemId,
        boolean completed,
        String reason
) {
}
