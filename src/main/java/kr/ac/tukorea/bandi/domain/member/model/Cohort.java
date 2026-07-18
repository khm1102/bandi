package kr.ac.tukorea.bandi.domain.member.model;

import kr.ac.tukorea.bandi.domain.member.exception.InactiveCohortException;
import lombok.Getter;

@Getter
public class Cohort {

    private Long cohortId;
    private final String name;
    private final short admissionYear;
    private final String termCode;
    private final boolean active;

    public Cohort(Long cohortId, String name, short admissionYear, String termCode, boolean active) {
        this.cohortId = cohortId;
        this.name = name;
        this.admissionYear = admissionYear;
        this.termCode = termCode;
        this.active = active;
    }

    public void validateAssignable() {
        if (!active) {
            throw new InactiveCohortException(cohortId);
        }
    }
}
