package kr.ac.tukorea.bandi.global.security;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SecurityTestController.class)
@Import({SecurityConfig.class, ApiSecurityFailureHandler.class})
class SecurityConfigTest {

    private final MockMvc mockMvc;

    @MockitoBean
    private SchoolAuthenticationProvider authenticationProvider;

    @MockitoBean
    private SchoolLoginFailureHandler failureHandler;

    @Autowired
    SecurityConfigTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void 비로그인_사용자는_내부_화면에서_로그인으로_이동한다() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void 일반_멤버와_팀장은_관리자_화면에_접근할_수_없다() throws Exception {
        for (String role : new String[]{"MEMBER", "LEADER"}) {
            mockMvc.perform(get("/members/test")
                            .with(user("tester").roles(role)))
                    .andExpect(status().isForbidden());
            mockMvc.perform(get("/reservations/test")
                            .with(user("tester").roles(role)))
                    .andExpect(status().isForbidden());
            mockMvc.perform(get("/showops/test")
                            .with(user("tester").roles(role)))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void 운영진은_관리자_화면에_접근할_수_있다() throws Exception {
        for (String path : new String[]{
                "/members/test", "/reservations/test", "/showops/test"}) {
            mockMvc.perform(get(path).with(user("admin").roles("ADMIN")))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void 외부_공개_화면은_로그인하지_않아도_접근할_수_있다() throws Exception {
        for (String path : new String[]{
                "/notices/test", "/performances/show", "/reserve/test",
                "/swagger-ui/test"}) {
            mockMvc.perform(get(path)).andExpect(status().isOk());
        }
    }

    @Test
    void 공개_공시_조회_API는_로그인하지_않아도_접근할_수_있다()
            throws Exception {
        mockMvc.perform(get("/api/public-notices/test"))
                .andExpect(status().isOk());
    }

    @Test
    void 공개_공시_상태_변경_API는_로그인을_요구한다() throws Exception {
        mockMvc.perform(post("/api/public-notices/test").with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("C003"));
    }

    @Test
    void API_상태_변경은_로그인해도_CSRF가_필요하다() throws Exception {
        mockMvc.perform(post("/api/test")
                        .with(user("member").roles("MEMBER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/test")
                        .with(user("member").roles("MEMBER"))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void 미인증_API는_로그인_리다이렉트_대신_JSON_401을_반환한다()
            throws Exception {
        mockMvc.perform(get("/api/test"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("C003"))
                .andExpect(jsonPath("$.message").value("로그인이 필요합니다."));
    }

    @Test
    void 권한이_부족한_API는_JSON_403을_반환한다() throws Exception {
        mockMvc.perform(get("/api/members/test")
                        .with(user("member").roles("MEMBER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("C002"))
                .andExpect(jsonPath("$.message").value("권한이 없습니다."));
    }

    @Test
    void 로그아웃은_POST_CSRF로_세션을_무효화한다() throws Exception {
        HttpSession session = mockMvc.perform(get("/dashboard")
                        .with(user("member").roles("MEMBER")))
                .andReturn().getRequest().getSession();

        mockMvc.perform(post("/logout")
                        .session((org.springframework.mock.web.MockHttpSession) session)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));

        assertThat(((org.springframework.mock.web.MockHttpSession) session)
                .isInvalid()).isTrue();
    }
}

@RestController
class SecurityTestController {

    @GetMapping({"/dashboard", "/members/test", "/reservations/test",
            "/showops/test", "/notices/test", "/performances/show",
            "/reserve/test", "/swagger-ui/test", "/api/test",
            "/api/members/test", "/api/public-notices/test"})
    ResponseEntity<Void> page() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/test")
    ResponseEntity<Void> change() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/public-notices/test")
    ResponseEntity<Void> changePublicNotice() {
        return ResponseEntity.ok().build();
    }
}
