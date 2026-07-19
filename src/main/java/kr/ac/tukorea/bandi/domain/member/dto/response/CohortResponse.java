package kr.ac.tukorea.bandi.domain.member.dto.response;

import kr.ac.tukorea.bandi.domain.member.model.Cohort;
import kr.ac.tukorea.bandi.domain.member.model.CohortTerm;

public record CohortResponse(
        Long cohortId,
        String name,
        short admissionYear,
        CohortTerm termCode,
        boolean active
) {

    public static CohortResponse from(Cohort cohort) {
        return new CohortResponse(cohort.getCohortId(), cohort.getName(),
                cohort.getAdmissionYear(), cohort.getTermCode(),
                cohort.isActive());
    }
}
