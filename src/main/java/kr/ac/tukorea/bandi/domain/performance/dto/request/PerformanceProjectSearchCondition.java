package kr.ac.tukorea.bandi.domain.performance.dto.request;

import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPerformanceProjectException;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceProjectStatus;

public record PerformanceProjectSearchCondition(
        Short academicYear,
        String termCode,
        PerformanceProjectStatus status,
        int offset,
        int limit
) {

    public PerformanceProjectSearchCondition {
        String normalizedTermCode = termCode == null || termCode.isBlank()
                ? null : termCode.strip();
        if ((academicYear != null && academicYear < 1)
                || (normalizedTermCode != null
                && normalizedTermCode.length() > 20)
                || offset < 0 || limit < 1 || limit > 100) {
            throw new InvalidPerformanceProjectException("search-condition");
        }
        termCode = normalizedTermCode;
    }
}
