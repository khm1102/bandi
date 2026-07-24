package kr.ac.tukorea.bandi.global.security;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SchoolAuthenticationProviderTest {

    @Mock
    private SchoolLoginAuthenticator authenticator;

    private SchoolAuthenticationProvider provider;

    @BeforeEach
    void setUp() {
        provider = new SchoolAuthenticationProvider(authenticator);
    }

    @Test
    void 학교_인증에_성공하면_비밀번호를_지운_인증_객체를_반환한다() {
        given(authenticator.authenticate("2021184000", "school-password"))
                .willReturn(new LoginPrincipal(10L, "MEMBER"));

        Authentication result = provider.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        "2021184000", "school-password"));

        assertThat(result.isAuthenticated()).isTrue();
        assertThat(result.getPrincipal()).isEqualTo(
                new LoginPrincipal(10L, "MEMBER"));
        assertThat(result.getCredentials()).isNull();
        assertThat(result.getAuthorities()).extracting("authority")
                .containsExactly("ROLE_MEMBER");
        verify(authenticator).authenticate("2021184000", "school-password");
    }

    @ParameterizedTest
    @CsvSource({
            "SCHOOL_CREDENTIALS_INVALID,bad-credentials",
            "SCHOOL_LOGIN_RATE_LIMITED,login-rate-limited",
            "SCHOOL_MEMBER_NOT_REGISTERED,member-not-registered",
            "SCHOOL_IDENTITY_REVIEW_REQUIRED,link-pending",
            "SCHOOL_ACADEMIC_STATUS_DENIED,academic-restricted",
            "MEMBER_LOGIN_DENIED,member-restricted",
            "SCHOOL_SSO_UNAVAILABLE,school-unavailable",
            "SCHOOL_SSO_RESPONSE_CHANGED,school-unavailable"
    })
    void 비즈니스_인증_실패를_화면_오류_코드로_변환한다(
            ErrorCode errorCode, String loginErrorCode) {
        given(authenticator.authenticate("2021184000", "wrong"))
                .willThrow(new BusinessException(errorCode));

        assertThatThrownBy(() -> provider.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        "2021184000", "wrong")))
                .isInstanceOf(SchoolLoginAuthenticationException.class)
                .extracting("loginErrorCode")
                .isEqualTo(loginErrorCode);
    }

    @Test
    void 빈_자격증명은_학교에_요청하지_않고_거부한다() {
        assertThatThrownBy(() -> provider.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated("", "")))
                .isInstanceOf(SchoolLoginAuthenticationException.class)
                .extracting("loginErrorCode")
                .isEqualTo("bad-credentials");
        verifyNoInteractions(authenticator);
    }
}
