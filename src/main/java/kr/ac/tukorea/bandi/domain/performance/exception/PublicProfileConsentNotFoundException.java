package kr.ac.tukorea.bandi.domain.performance.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class PublicProfileConsentNotFoundException extends BusinessException {

    public PublicProfileConsentNotFoundException(Long consentId) {
        super(ErrorCode.PUBLIC_PROFILE_CONSENT_NOT_FOUND,
                "publicProfileConsentId=" + consentId);
    }
}
