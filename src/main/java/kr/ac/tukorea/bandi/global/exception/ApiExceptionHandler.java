package kr.ac.tukorea.bandi.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class ApiExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(
            BusinessException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        log.warn("비즈니스 예외 - code={}", errorCode.getCode());
        return ResponseEntity.status(errorCode.getStatus())
                .body(ErrorResponse.from(errorCode));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException exception) {
        log.debug("API 입력 검증 실패 - fieldErrorCount={}",
                exception.getBindingResult().getFieldErrorCount());
        return ResponseEntity.badRequest()
                .body(ErrorResponse.from(ErrorCode.INVALID_INPUT,
                        exception.getBindingResult()));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class,
            HandlerMethodValidationException.class,
            IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleBinding(Exception exception) {
        log.debug("API 요청 바인딩 실패 - type={}",
                exception.getClass().getSimpleName());
        return ResponseEntity.badRequest()
                .body(ErrorResponse.from(ErrorCode.INVALID_INPUT));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception exception) {
        log.error("예상하지 못한 API 예외", exception);
        return ResponseEntity.internalServerError()
                .body(ErrorResponse.from(ErrorCode.INTERNAL_ERROR));
    }
}
