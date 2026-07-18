package kr.ac.tukorea.bandi.domain.member.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

/**
 * 변경 전후 값이 같은 팀·권한 변경 요청.
 * 의미 없는 이력 행이 쌓이는 것을 막는다 (docs/database-schema.md 5.4).
 */
public class NoChangeException extends BusinessException {

    public NoChangeException(String field) {
        super(ErrorCode.NO_CHANGE, field);
    }
}
