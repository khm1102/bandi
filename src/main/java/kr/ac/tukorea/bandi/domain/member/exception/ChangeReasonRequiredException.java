package kr.ac.tukorea.bandi.domain.member.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

/**
 * 팀·권한 변경 사유 누락 (정본 5.4 — 사유는 필수이며 DB에서도 NOT NULL이다).
 */
public class ChangeReasonRequiredException extends BusinessException {

    public ChangeReasonRequiredException() {
        super(ErrorCode.CHANGE_REASON_REQUIRED);
    }
}
