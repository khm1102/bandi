package kr.ac.tukorea.bandi.global.security;

import org.springframework.security.core.AuthenticationException;

public class SchoolLoginAuthenticationException extends AuthenticationException {

    private final String loginErrorCode;

    public SchoolLoginAuthenticationException(String loginErrorCode,
                                              Throwable cause) {
        super("school login failed", cause);
        this.loginErrorCode = loginErrorCode;
    }

    public String getLoginErrorCode() {
        return loginErrorCode;
    }
}
