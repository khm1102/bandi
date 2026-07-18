package kr.ac.tukorea.bandi.domain.fee.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class FeeAccessDeniedException extends BusinessException {

    public FeeAccessDeniedException() {
        super(ErrorCode.FORBIDDEN);
    }
}
