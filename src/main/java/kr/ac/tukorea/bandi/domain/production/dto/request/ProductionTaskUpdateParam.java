package kr.ac.tukorea.bandi.domain.production.dto.request;

import java.time.LocalDate;

public record ProductionTaskUpdateParam(
        Long productionTaskId,
        String title,
        String description,
        LocalDate startDate,
        LocalDate dueDate
) {
}
