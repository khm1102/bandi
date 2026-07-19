package kr.ac.tukorea.bandi.domain.checklist.dto.request;

import kr.ac.tukorea.bandi.domain.checklist.model.ChecklistScope;

public record ChecklistItemCreateParam(
        Long performanceProjectId,
        Long performanceRoundId,
        Long teamId,
        ChecklistScope scope,
        String content,
        boolean required,
        int displayOrder
) {
}
