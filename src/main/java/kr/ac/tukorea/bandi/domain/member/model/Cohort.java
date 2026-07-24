package kr.ac.tukorea.bandi.domain.member.model;

import kr.ac.tukorea.bandi.domain.member.exception.InactiveCohortException;
import lombok.Getter;

@Getter
public class Cohort {

    private Long cohortId;
    private final String name;
    private final boolean active;

    public Cohort(Long cohortId, String name, boolean active) {
        this.cohortId = cohortId;
        this.name = name;
        this.active = active;
    }

    public static Cohort create(String name) {
        return new Cohort(null, name.trim(), true);
    }

    public void validateAssignable() {
        if (!active) {
            throw new InactiveCohortException(cohortId);
        }
    }
}
