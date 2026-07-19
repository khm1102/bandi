package kr.ac.tukorea.bandi.domain.resource.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class ResourceAccessDeniedException extends BusinessException {

    public ResourceAccessDeniedException() {
        super(ErrorCode.FORBIDDEN);
    }
}
