package kr.ac.tukorea.bandi.domain.member.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class DuplicateCohortException extends BusinessException {

    public DuplicateCohortException() {
        super(ErrorCode.DUPLICATE_COHORT);
    }
}
