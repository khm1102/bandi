package kr.ac.tukorea.bandi.domain.member.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

/**
 * 모집·운영이 종료된 기수에 멤버를 배정하려는 요청.
 */
public class InactiveCohortException extends BusinessException {

    public InactiveCohortException(Long cohortId) {
        super(ErrorCode.INACTIVE_COHORT, "cohortId=" + cohortId);
    }
}
