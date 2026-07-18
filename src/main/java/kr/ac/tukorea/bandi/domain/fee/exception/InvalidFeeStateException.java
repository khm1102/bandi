package kr.ac.tukorea.bandi.domain.fee.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class InvalidFeeStateException extends BusinessException {

    public InvalidFeeStateException(String detail) {
        super(ErrorCode.INVALID_FEE_STATE, detail);
    }
}
