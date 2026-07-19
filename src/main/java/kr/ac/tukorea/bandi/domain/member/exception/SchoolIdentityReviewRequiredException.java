package kr.ac.tukorea.bandi.domain.member.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class SchoolIdentityReviewRequiredException extends BusinessException {

    public SchoolIdentityReviewRequiredException() {
        super(ErrorCode.SCHOOL_IDENTITY_REVIEW_REQUIRED);
    }
}
