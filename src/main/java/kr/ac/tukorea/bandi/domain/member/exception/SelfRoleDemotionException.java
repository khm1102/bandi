package kr.ac.tukorea.bandi.domain.member.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

/**
 * ADMIN이 본인의 권한을 스스로 낮추려는 요청.
 * 정본 5.4 — "본인의 ADMIN 권한 하향은 다른 ADMIN만 실행할 수 있다".
 */
public class SelfRoleDemotionException extends BusinessException {

    public SelfRoleDemotionException(Long memberId) {
        super(ErrorCode.SELF_ROLE_DEMOTION, "memberId=" + memberId);
    }
}
