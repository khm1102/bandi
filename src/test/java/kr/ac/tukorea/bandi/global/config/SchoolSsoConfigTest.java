package kr.ac.tukorea.bandi.global.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class SchoolSsoConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(SchoolSsoConfig.class)
            .withPropertyValues(
                    "bandi.school-sso.login-page-url=https://sso.tukorea.ac.kr/login",
                    "bandi.school-sso.login-process-url=https://sso.tukorea.ac.kr/login/process",
                    "bandi.school-sso.connect-timeout=3s",
                    "bandi.school-sso.request-timeout=10s",
                    "bandi.school-sso.user-agent=bandi-test",
                    "bandi.school-login-attempt.max-failures=5",
                    "bandi.school-login-attempt.failure-window=15m",
                    "bandi.school-login-attempt.cooldown=15m",
                    "bandi.school-login-attempt.hash-secret=test-hmac-secret");

    @Test
    void 전역_설정에서_학교_SSO_프로퍼티를_바인딩한다() {
        contextRunner.run(context -> {
            SchoolSsoProperties properties = context.getBean(SchoolSsoProperties.class);

            assertThat(properties.loginPageUrl().toString())
                    .isEqualTo("https://sso.tukorea.ac.kr/login");
            assertThat(properties.loginProcessUrl().toString())
                    .isEqualTo("https://sso.tukorea.ac.kr/login/process");
            assertThat(properties.connectTimeout().getSeconds()).isEqualTo(3);
            assertThat(properties.requestTimeout().getSeconds()).isEqualTo(10);
            assertThat(properties.userAgent()).isEqualTo("bandi-test");
        });
    }

    @Test
    void 전역_설정에서_학교_로그인_제한_프로퍼티를_바인딩한다() {
        contextRunner.run(context -> {
            SchoolLoginAttemptProperties properties =
                    context.getBean(SchoolLoginAttemptProperties.class);

            assertThat(properties.maxFailures()).isEqualTo(5);
            assertThat(properties.failureWindow().getSeconds()).isEqualTo(900);
            assertThat(properties.cooldown().getSeconds()).isEqualTo(900);
        });
    }
}
