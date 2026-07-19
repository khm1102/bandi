package kr.ac.tukorea.bandi.domain.performance.dto.response;

import kr.ac.tukorea.bandi.domain.performance.model.PerformanceProjectStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PerformanceProjectResponse(
        Long performanceProjectId,
        short academicYear,
        String termCode,
        String title,
        LocalDate productionStartDate,
        LocalDate productionEndDate,
        String place,
        PerformanceProjectStatus status,
        Long createdByMemberId,
        Long updatedByMemberId,
        LocalDateTime createdDttm,
        LocalDateTime updatedDttm
) {
}
