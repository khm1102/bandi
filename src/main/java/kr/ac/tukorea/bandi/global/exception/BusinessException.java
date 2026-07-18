package kr.ac.tukorea.bandi.global.exception;

import lombok.Getter;

/**
 * 예상된 실패 흐름의 공통 부모 (컨벤션 9.4).
 * Service는 이 클래스를 직접 던지지 않고 feature별 커스텀 예외를 정의해 사용한다.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 사용자 응답은 {@link ErrorCode#getMessage()}를 쓰고, 진단용 식별자는 로그에만 남긴다.
     * detail에 개인정보를 넣지 않는다 (컨벤션 20.3).
     */
    protected BusinessException(ErrorCode errorCode, String detail) {
        super("%s [%s]".formatted(errorCode.getMessage(), detail));
        this.errorCode = errorCode;
    }
}
