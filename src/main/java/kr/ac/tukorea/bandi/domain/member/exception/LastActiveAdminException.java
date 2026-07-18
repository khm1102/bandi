package kr.ac.tukorea.bandi.domain.member.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

/**
 * 활성 운영진이 0명이 되는 변경 요청 (정본 5.4).
 * 운영진이 사라지면 아무도 권한을 복구할 수 없으므로 마지막 한 명은 보호한다.
 */
public class LastActiveAdminException extends BusinessException {

    public LastActiveAdminException(Long memberId) {
        super(ErrorCode.LAST_ACTIVE_ADMIN, "memberId=" + memberId);
    }
}
