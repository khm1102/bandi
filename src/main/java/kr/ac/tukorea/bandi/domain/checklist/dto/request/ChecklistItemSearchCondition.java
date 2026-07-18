package kr.ac.tukorea.bandi.domain.checklist.dto.request;

import kr.ac.tukorea.bandi.domain.checklist.exception.InvalidChecklistItemException;
import kr.ac.tukorea.bandi.domain.checklist.model.ChecklistScope;

public record ChecklistItemSearchCondition(
        Long performanceProjectId,
        Long performanceRoundId,
        Long teamId,
        ChecklistScope scope
) {

    public ChecklistItemSearchCondition {
        if (performanceProjectId == null || performanceProjectId < 1
                || (performanceRoundId != null && performanceRoundId < 1)
                || (teamId != null && teamId < 1)
                || (scope == ChecklistScope.PROJECT
                && performanceRoundId != null)) {
            throw new InvalidChecklistItemException("searchCondition");
        }
    }
}
