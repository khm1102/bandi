package kr.ac.tukorea.bandi.domain.member.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class TeamNotFoundException extends BusinessException {

    public TeamNotFoundException(Long teamId) {
        super(ErrorCode.TEAM_NOT_FOUND, "teamId=" + teamId);
    }
}
