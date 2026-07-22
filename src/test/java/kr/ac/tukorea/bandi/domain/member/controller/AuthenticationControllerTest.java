package kr.ac.tukorea.bandi.domain.member.controller;

import kr.ac.tukorea.bandi.domain.member.dto.request.SchoolLoginForm;
import kr.ac.tukorea.bandi.global.security.ApiSecurityFailureHandler;
import kr.ac.tukorea.bandi.global.security.LoginPrincipal;
import kr.ac.tukorea.bandi.global.security.SchoolAuthenticationProvider;
import kr.ac.tukorea.bandi.global.security.SchoolLoginFailureHandler;
import kr.ac.tukorea.bandi.global.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AuthenticationController.class)
@Import({SecurityConfig.class, ApiSecurityFailureHandler.class})
class AuthenticationControllerTest {

    private final MockMvc mockMvc;

    @MockitoBean
    private SchoolAuthenticationProvider authenticationProvider;

    @MockitoBean
    private SchoolLoginFailureHandler failureHandler;

    @Autowired
    AuthenticationControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void 로그인_화면에_학교_로그인_폼을_제공한다() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attribute("schoolLoginForm",
                        instanceOf(SchoolLoginForm.class)));
    }

    @ParameterizedTest
    @CsvSource({
            "school-unavailable, 학교 로그인 서비스 장애",
            "bad-credentials, 학교 계정 확인 필요",
            "member-not-registered, 멤버 사전 등록 필요",
            "link-pending, 학교 계정 연결 대기",
            "academic-restricted, 학적 상태 확인 필요",
            "member-restricted, 멤버 이용 상태 확인 필요"
    })
    void 로그인_오류_상태를_구분한다(String error, String title) throws Exception {
        mockMvc.perform(get("/login").param("error", error))
                .andExpect(status().isOk())
                .andExpect(model().attribute("loginErrorTitle", title))
                .andExpect(model().attributeExists("loginErrorMessage"));
    }

    @Test
    void 알_수_없는_오류_코드는_화면에_노출하지_않는다() throws Exception {
        mockMvc.perform(get("/login").param("error", "untrusted"))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist(
                        "loginErrorTitle", "loginErrorMessage"));
    }

    @Test
    void 학교_로그인에_성공하면_대시보드로_이동한다() throws Exception {
        LoginPrincipal principal = new LoginPrincipal(10L, "MEMBER");
        Authentication authenticatedToken =
                UsernamePasswordAuthenticationToken.authenticated(
                        principal, null, principal.authorities());
        given(authenticationProvider.supports(
                UsernamePasswordAuthenticationToken.class)).willReturn(true);
        given(authenticationProvider.authenticate(any(Authentication.class)))
                .willReturn(authenticatedToken);

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("studentNo", "2021184000")
                        .param("password", "school-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(authenticated().withAuthenticationPrincipal(principal));
    }

    @Test
    void 로그인_POST도_CSRF가_없으면_거부한다() throws Exception {
        mockMvc.perform(post("/login")
                        .param("studentNo", "2021184000")
                        .param("password", "school-password"))
                .andExpect(status().isForbidden());
    }
}
