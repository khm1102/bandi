package kr.ac.tukorea.bandi.global.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    private final OpenApiConfig config = new OpenApiConfig();

    @Test
    void API_기본_정보를_정의한다() {
        OpenAPI openApi = config.bandiOpenApi();

        assertThat(openApi.getInfo().getTitle()).isEqualTo("bandi API");
        assertThat(openApi.getInfo().getVersion()).isEqualTo("v1");
        assertThat(openApi.getInfo().getDescription()).isNotBlank();
    }

    @Test
    void Spring_Session_쿠키_인증_스키마를_정의한다() {
        OpenAPI openApi = config.bandiOpenApi();

        SecurityScheme scheme = openApi.getComponents().getSecuritySchemes()
                .get(OpenApiConfig.SESSION_COOKIE_SCHEME);
        assertThat(scheme.getType()).isEqualTo(SecurityScheme.Type.APIKEY);
        assertThat(scheme.getIn()).isEqualTo(SecurityScheme.In.COOKIE);
        assertThat(scheme.getName()).isEqualTo("SESSION");
    }

    @Test
    void 공개_API를_위해_전역_보안_요구사항은_강제하지_않는다() {
        OpenAPI openApi = config.bandiOpenApi();

        assertThat(openApi.getSecurity()).isNullOrEmpty();
    }
}
