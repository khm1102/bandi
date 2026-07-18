package kr.ac.tukorea.bandi.domain.event.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class ClubEventAccessDeniedException extends BusinessException {

    public ClubEventAccessDeniedException() {
        super(ErrorCode.FORBIDDEN);
    }
}
