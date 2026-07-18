package kr.ac.tukorea.bandi.domain.member.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

/**
 * 활성 운영진이 아닌 멤버가 멤버 관리 기능을 실행하려는 요청.
 */
public class MemberManagementForbiddenException extends BusinessException {

    public MemberManagementForbiddenException(Long actorMemberId) {
        super(ErrorCode.FORBIDDEN, "actorMemberId=" + actorMemberId);
    }
}
