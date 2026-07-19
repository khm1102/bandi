package kr.ac.tukorea.bandi.domain.resource.exception;

import kr.ac.tukorea.bandi.domain.resource.model.ResourceStatus;
import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class InvalidResourceStateException extends BusinessException {

    public InvalidResourceStateException(ResourceStatus status) {
        super(ErrorCode.INVALID_RESOURCE_STATE);
    }
}
