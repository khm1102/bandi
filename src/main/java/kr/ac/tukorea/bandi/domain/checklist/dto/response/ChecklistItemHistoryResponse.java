package kr.ac.tukorea.bandi.domain.checklist.dto.response;

import java.time.LocalDateTime;

public record ChecklistItemHistoryResponse(
        Long checklistItemHistoryId,
        Long checklistItemId,
        boolean previousCompleted,
        boolean newCompleted,
        Long changedByMemberId,
        String changedByMemberName,
        LocalDateTime changedDttm,
        String reason
) {
}
