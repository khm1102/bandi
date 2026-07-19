package kr.ac.tukorea.bandi.domain.fee.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class InvalidFeeException extends BusinessException {

    public InvalidFeeException(String detail) {
        super(ErrorCode.INVALID_INPUT, detail);
    }
}
