package kr.ac.tukorea.bandi.domain.checklist.dto.response;

import kr.ac.tukorea.bandi.domain.checklist.model.ChecklistScope;

import java.time.LocalDateTime;

public record ChecklistItemResponse(
        Long checklistItemId,
        Long performanceProjectId,
        Long performanceRoundId,
        Long teamId,
        String teamName,
        ChecklistScope scope,
        String content,
        boolean required,
        int displayOrder,
        boolean completed,
        Long completedByMemberId,
        String completedByMemberName,
        LocalDateTime completedDttm
) {
}
