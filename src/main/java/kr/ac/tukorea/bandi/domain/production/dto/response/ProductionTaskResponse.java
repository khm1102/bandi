package kr.ac.tukorea.bandi.domain.production.dto.response;

import kr.ac.tukorea.bandi.domain.production.model.ProductionTaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProductionTaskResponse(
        Long productionTaskId,
        Long performanceProjectId,
        Long teamId,
        String teamName,
        String title,
        String description,
        LocalDate startDate,
        LocalDate dueDate,
        ProductionTaskStatus status,
        String blockedReason,
        Long createdByMemberId,
        Long updatedByMemberId,
        LocalDateTime createdDttm,
        LocalDateTime updatedDttm
) {
}
