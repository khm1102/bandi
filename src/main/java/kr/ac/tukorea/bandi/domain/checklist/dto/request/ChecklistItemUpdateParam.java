package kr.ac.tukorea.bandi.domain.checklist.dto.request;

public record ChecklistItemUpdateParam(
        Long checklistItemId,
        String content,
        boolean required,
        int displayOrder
) {
}
