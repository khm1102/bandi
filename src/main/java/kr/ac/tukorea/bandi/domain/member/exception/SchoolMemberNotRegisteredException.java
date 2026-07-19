package kr.ac.tukorea.bandi.domain.member.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class SchoolMemberNotRegisteredException extends BusinessException {

    public SchoolMemberNotRegisteredException() {
        super(ErrorCode.SCHOOL_MEMBER_NOT_REGISTERED);
    }
}
