package kr.ac.tukorea.bandi.domain.calendar.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class CalendarAccessDeniedException extends BusinessException {

    public CalendarAccessDeniedException() {
        super(ErrorCode.FORBIDDEN);
    }
}
