package kr.ac.tukorea.bandi.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
@ControllerAdvice
public class SsrExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ModelAndView handleBusiness(BusinessException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        log.warn("SSR 비즈니스 예외 - code={}", errorCode.getCode());

        if (errorCode.getStatus() == HttpStatus.NOT_FOUND) {
            return errorView("error/404", HttpStatus.NOT_FOUND);
        }
        if (errorCode.getStatus() == HttpStatus.FORBIDDEN) {
            return errorView("error/403", HttpStatus.FORBIDDEN);
        }
        return errorView("error/500", errorCode.getStatus());
    }

    @ExceptionHandler({NoHandlerFoundException.class,
            NoResourceFoundException.class})
    public ModelAndView handleNotFound(Exception exception) {
        log.debug("SSR 경로 없음 - type={}",
                exception.getClass().getSimpleName());
        return errorView("error/404", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleUnexpected(Exception exception) {
        log.error("예상하지 못한 SSR 예외", exception);
        return errorView("error/500", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ModelAndView errorView(String viewName, HttpStatus status) {
        ModelAndView modelAndView = new ModelAndView(viewName);
        modelAndView.setStatus(status);
        return modelAndView;
    }
}
