package kr.ac.tukorea.bandi.domain.performance.dto.request;

import java.time.LocalDate;

public record PerformanceProjectCreateParam(
        short academicYear,
        String termCode,
        String title,
        LocalDate productionStartDate,
        LocalDate productionEndDate,
        String place
) {
}
