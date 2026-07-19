package kr.ac.tukorea.bandi.domain.calendar.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class CalendarEventNotFoundException extends BusinessException {

    public CalendarEventNotFoundException(Long calendarEventId) {
        super(ErrorCode.CALENDAR_EVENT_NOT_FOUND, String.valueOf(calendarEventId));
    }
}
