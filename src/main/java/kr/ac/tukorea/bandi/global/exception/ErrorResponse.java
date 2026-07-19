package kr.ac.tukorea.bandi.global.exception;

import org.springframework.validation.BindingResult;

import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        List<FieldError> fieldErrors
) {

    public ErrorResponse {
        fieldErrors = List.copyOf(fieldErrors);
    }

    public static ErrorResponse from(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(),
                List.of());
    }

    public static ErrorResponse from(ErrorCode errorCode,
                                     BindingResult bindingResult) {
        List<FieldError> errors = bindingResult.getFieldErrors().stream()
                .map(error -> new FieldError(error.getField(),
                        resolveReason(error.getDefaultMessage(), errorCode)))
                .toList();
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(),
                errors);
    }

    private static String resolveReason(String reason, ErrorCode errorCode) {
        return reason == null ? errorCode.getMessage() : reason;
    }

    public record FieldError(String field, String reason) {
    }
}
