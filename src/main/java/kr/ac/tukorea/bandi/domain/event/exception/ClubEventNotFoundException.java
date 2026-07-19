package kr.ac.tukorea.bandi.domain.event.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class ClubEventNotFoundException extends BusinessException {

    public ClubEventNotFoundException(Long clubEventId) {
        super(ErrorCode.CLUB_EVENT_NOT_FOUND, String.valueOf(clubEventId));
    }
}
