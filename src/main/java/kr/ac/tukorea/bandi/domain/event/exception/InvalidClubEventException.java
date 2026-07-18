package kr.ac.tukorea.bandi.domain.event.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class InvalidClubEventException extends BusinessException {

    public InvalidClubEventException(String detail) {
        super(ErrorCode.INVALID_INPUT, detail);
    }
}
