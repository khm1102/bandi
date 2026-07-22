package kr.ac.tukorea.bandi.domain.member.exception;

import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

/**
 * 관리자 명령으로 허용되지 않은 멤버 상태 전이.
 */
public class InvalidMemberStatusTransitionException extends BusinessException {

    public InvalidMemberStatusTransitionException(MemberStatus previousStatus, MemberStatus newStatus) {
        super(ErrorCode.INVALID_MEMBER_STATUS_TRANSITION,
                "previousStatus=" + previousStatus + ", newStatus=" + newStatus);
    }
}
