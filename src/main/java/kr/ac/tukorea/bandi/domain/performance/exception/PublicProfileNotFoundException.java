package kr.ac.tukorea.bandi.domain.performance.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class PublicProfileNotFoundException extends BusinessException {

    public PublicProfileNotFoundException(Long publicProfileId) {
        super(ErrorCode.PUBLIC_PROFILE_NOT_FOUND,
                "publicProfileId=" + publicProfileId);
    }
}
