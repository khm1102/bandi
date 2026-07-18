package kr.ac.tukorea.bandi.domain.event.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class InvalidClubEventStateException extends BusinessException {

    public InvalidClubEventStateException(String detail) {
        super(ErrorCode.INVALID_CLUB_EVENT_STATE, detail);
    }
}
