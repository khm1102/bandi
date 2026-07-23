package kr.ac.tukorea.bandi.domain.member.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class ClubPresidentUnavailableException extends BusinessException {

    public ClubPresidentUnavailableException() {
        super(ErrorCode.CLUB_PRESIDENT_UNAVAILABLE);
    }
}
