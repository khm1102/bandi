package kr.ac.tukorea.bandi.global.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String SESSION_COOKIE_SCHEME = "sessionCookie";

    @Bean
    public OpenAPI bandiOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("bandi API")
                        .description("연극 동아리 제작과 운영을 위한 bandi API")
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes(SESSION_COOKIE_SCHEME,
                                sessionCookieScheme()));
    }

    private SecurityScheme sessionCookieScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)
                .name("SESSION")
                .description("학교 SSO 로그인 후 발급되는 Spring Session 쿠키");
    }
}
