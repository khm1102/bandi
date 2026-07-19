package kr.ac.tukorea.bandi.domain.event.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class EventAttendanceNotFoundException extends BusinessException {

    public EventAttendanceNotFoundException(Long eventAttendanceId) {
        super(ErrorCode.EVENT_ATTENDANCE_NOT_FOUND,
                String.valueOf(eventAttendanceId));
    }
}
