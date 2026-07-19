package kr.ac.tukorea.bandi.domain.resource.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(Long resourceId) {
        super(ErrorCode.RESOURCE_NOT_FOUND);
    }
}
