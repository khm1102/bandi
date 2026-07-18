package kr.ac.tukorea.bandi.domain.calendar.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class InvalidCalendarEventException extends BusinessException {

    public InvalidCalendarEventException(String detail) {
        super(ErrorCode.INVALID_INPUT, detail);
    }
}
