package kr.ac.tukorea.bandi.global.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.mock.web.MockHttpServletRequest;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginMemberArgumentResolverTest {

    private final LoginMemberArgumentResolver resolver =
            new LoginMemberArgumentResolver();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void LoginMember가_붙은_Long_파라미터를_지원한다() throws Exception {
        assertThat(resolver.supportsParameter(memberIdParameter())).isTrue();
        assertThat(resolver.supportsParameter(stringParameter())).isFalse();
    }

    @Test
    void 로그인_principal에서_memberId를_주입한다() throws Exception {
        LoginPrincipal principal = new LoginPrincipal(20L, "LEADER");
        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        principal, null, principal.authorities());
        SecurityContextHolder.setContext(new SecurityContextImpl(authentication));

        Object resolved = resolver.resolveArgument(memberIdParameter(), null,
                new ServletWebRequest(new MockHttpServletRequest()), null);

        assertThat(resolved).isEqualTo(20L);
    }

    @Test
    void 로그인_principal이_없으면_주입하지_않는다() throws Exception {
        assertThatThrownBy(() -> resolver.resolveArgument(
                memberIdParameter(), null,
                new ServletWebRequest(new MockHttpServletRequest()), null))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    private MethodParameter memberIdParameter() throws Exception {
        Method method = Handler.class.getDeclaredMethod("handle", Long.class);
        return new MethodParameter(method, 0);
    }

    private MethodParameter stringParameter() throws Exception {
        Method method = Handler.class.getDeclaredMethod("invalid", String.class);
        return new MethodParameter(method, 0);
    }

    private static class Handler {

        void handle(@LoginMember Long memberId) {
        }

        void invalid(@LoginMember String memberId) {
        }
    }
}
