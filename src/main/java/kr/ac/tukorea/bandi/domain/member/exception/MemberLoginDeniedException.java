package kr.ac.tukorea.bandi.domain.member.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class MemberLoginDeniedException extends BusinessException {

    public MemberLoginDeniedException() {
        super(ErrorCode.MEMBER_LOGIN_DENIED);
    }
}
