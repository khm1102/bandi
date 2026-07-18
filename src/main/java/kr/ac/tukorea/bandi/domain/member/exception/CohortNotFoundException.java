package kr.ac.tukorea.bandi.domain.member.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class CohortNotFoundException extends BusinessException {

    public CohortNotFoundException(Long cohortId) {
        super(ErrorCode.COHORT_NOT_FOUND, "cohortId=" + cohortId);
    }
}
