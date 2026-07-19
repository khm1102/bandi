package kr.ac.tukorea.bandi.domain.performance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PerformanceProjectRequest(
        @Positive short academicYear,
        @NotBlank @Size(max = 20) String termCode,
        @NotBlank @Size(max = 200) String title,
        @NotNull LocalDate productionStartDate,
        @NotNull LocalDate productionEndDate,
        @NotBlank @Size(max = 200) String place
) {

    public PerformanceProjectCreateParam toCreateParam() {
        return new PerformanceProjectCreateParam(academicYear, termCode, title,
                productionStartDate, productionEndDate, place);
    }

    public PerformanceProjectUpdateParam toUpdateParam(Long projectId) {
        return new PerformanceProjectUpdateParam(projectId, academicYear,
                termCode, title, productionStartDate, productionEndDate, place);
    }
}
