package kr.ac.tukorea.bandi.global.security;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SchoolAuthenticationProvider implements AuthenticationProvider {

    private final SchoolLoginAuthenticator authenticator;

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        String studentNo = authentication.getName();
        String password = authentication.getCredentials() instanceof String value
                ? value : "";
        if (studentNo == null || studentNo.isBlank() || password.isBlank()) {
            throw new SchoolLoginAuthenticationException(
                    "bad-credentials",
                    new BadCredentialsException("blank school credentials"));
        }
        try {
            LoginPrincipal principal = authenticator.authenticate(
                    studentNo, password);
            return UsernamePasswordAuthenticationToken.authenticated(
                    principal, null, principal.authorities());
        } catch (BusinessException exception) {
            throw new SchoolLoginAuthenticationException(
                    loginErrorCode(exception.getErrorCode()), exception);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class
                .isAssignableFrom(authentication);
    }

    private String loginErrorCode(ErrorCode errorCode) {
        return switch (errorCode) {
            case SCHOOL_CREDENTIALS_INVALID -> "bad-credentials";
            case SCHOOL_MEMBER_NOT_REGISTERED -> "member-not-registered";
            case SCHOOL_IDENTITY_REVIEW_REQUIRED,
                 SCHOOL_IDENTITY_MISMATCH -> "link-pending";
            case SCHOOL_ACADEMIC_STATUS_DENIED -> "academic-restricted";
            case MEMBER_LOGIN_DENIED -> "member-restricted";
            case SCHOOL_SSO_UNAVAILABLE,
                 SCHOOL_SSO_RESPONSE_CHANGED -> "school-unavailable";
            default -> "school-unavailable";
        };
    }
}
