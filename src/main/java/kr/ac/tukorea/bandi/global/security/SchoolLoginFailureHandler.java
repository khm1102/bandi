package kr.ac.tukorea.bandi.global.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class SchoolLoginFailureHandler implements AuthenticationFailureHandler {

    private static final String DEFAULT_ERROR_CODE = "bad-credentials";
    private static final Set<String> ERROR_CODES = Set.of(
            DEFAULT_ERROR_CODE,
            "login-rate-limited",
            "school-unavailable",
            "member-not-registered",
            "link-pending",
            "academic-restricted",
            "member-restricted");

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {
        String errorCode = resolveErrorCode(exception);
        response.sendRedirect(request.getContextPath()
                + "/login?error=" + errorCode);
    }

    private String resolveErrorCode(AuthenticationException exception) {
        if (exception instanceof SchoolLoginAuthenticationException schoolException
                && ERROR_CODES.contains(schoolException.getLoginErrorCode())) {
            return schoolException.getLoginErrorCode();
        }
        return DEFAULT_ERROR_CODE;
    }
}
