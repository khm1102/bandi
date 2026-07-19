package kr.ac.tukorea.bandi.domain.event.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class InvalidEventAttendanceException extends BusinessException {

    public InvalidEventAttendanceException(String detail) {
        super(ErrorCode.INVALID_INPUT, detail);
    }
}
