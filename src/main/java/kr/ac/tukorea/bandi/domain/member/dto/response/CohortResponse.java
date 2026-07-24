package kr.ac.tukorea.bandi.domain.member.dto.response;

import kr.ac.tukorea.bandi.domain.member.model.Cohort;

public record CohortResponse(
        Long cohortId,
        String name,
        boolean active
) {

    public static CohortResponse from(Cohort cohort) {
        return new CohortResponse(cohort.getCohortId(), cohort.getName(), cohort.isActive());
    }
}
