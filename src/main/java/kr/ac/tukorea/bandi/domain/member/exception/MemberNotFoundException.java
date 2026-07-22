package kr.ac.tukorea.bandi.domain.member.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class MemberNotFoundException extends BusinessException {

    public MemberNotFoundException(Long memberId) {
        super(ErrorCode.MEMBER_NOT_FOUND, "memberId=" + memberId);
    }
}
