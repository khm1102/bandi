package kr.ac.tukorea.bandi.global.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class SchoolLoginFailureHandlerTest {

    private final SchoolLoginFailureHandler failureHandler =
            new SchoolLoginFailureHandler();

    @Test
    void 로그인_실패_코드를_로그인_화면으로_전달한다() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/bandi");
        MockHttpServletResponse response = new MockHttpServletResponse();
        SchoolLoginAuthenticationException exception =
                new SchoolLoginAuthenticationException(
                        "academic-restricted", new RuntimeException());

        failureHandler.onAuthenticationFailure(request, response, exception);

        assertThat(response.getRedirectedUrl()).isEqualTo(
                "/bandi/login?error=academic-restricted");
    }

    @Test
    void 일반_인증_실패는_자격증명_오류로_처리한다() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        failureHandler.onAuthenticationFailure(request, response,
                new org.springframework.security.authentication.BadCredentialsException(
                        "school-password"));

        assertThat(response.getRedirectedUrl()).isEqualTo(
                "/login?error=bad-credentials");
    }
}
