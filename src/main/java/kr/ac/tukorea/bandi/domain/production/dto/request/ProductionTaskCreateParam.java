package kr.ac.tukorea.bandi.domain.production.dto.request;

import java.time.LocalDate;

public record ProductionTaskCreateParam(
        Long performanceProjectId,
        Long teamId,
        String title,
        String description,
        LocalDate startDate,
        LocalDate dueDate
) {
}
