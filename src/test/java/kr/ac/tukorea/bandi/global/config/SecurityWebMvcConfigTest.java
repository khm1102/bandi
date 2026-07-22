package kr.ac.tukorea.bandi.global.config;

import kr.ac.tukorea.bandi.global.security.LoginMemberArgumentResolver;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityWebMvcConfigTest {

    @Test
    void LoginMember_리졸버를_MVC에_등록한다() {
        LoginMemberArgumentResolver resolver =
                new LoginMemberArgumentResolver();
        SecurityWebMvcConfig config = new SecurityWebMvcConfig(resolver);
        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();

        config.addArgumentResolvers(resolvers);

        assertThat(resolvers).containsExactly(resolver);
    }
}
