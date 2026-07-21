package kr.ac.tukorea.bandi.global.security;

import jakarta.servlet.DispatcherType;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
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
            mockMvc.perform(get("/members/test").with(user("tester").roles(role)))
                    .andExpect(status().isForbidden());
            mockMvc.perform(get("/notice-management/test")
                            .with(user("tester").roles(role)))
                    .andExpect(status().isForbidden());
            mockMvc.perform(get("/api/admin/public-notices/test")
                            .with(user("tester").roles(role)))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void 운영진은_남은_관리자_화면에_접근할_수_있다() throws Exception {
        for (String path : new String[]{"/members/test", "/notice-management/test",
                "/api/admin/public-notices/test"}) {
            mockMvc.perform(get(path).with(user("admin").roles("ADMIN")))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void 폐기된_공개_공연과_관람_경로는_로그인으로_보내지_않는다() throws Exception {
        for (String path : new String[]{"/performances/show", "/reserve/test",
                "/api/public-performances/test", "/api/public-policies/test",
                "/api/public-reservations/test"}) {
            mockMvc.perform(get(path)).andExpect(status().isNotFound());
        }
    }

    @Test
    void JSP_포워드와_오류_디스패치는_추가_인증을_요구하지_않는다()
            throws Exception {
        mockMvc.perform(get("/dispatch-target").with(request -> {
                    request.setDispatcherType(DispatcherType.FORWARD);
                    return request;
                }))
                .andExpect(status().isOk());
        mockMvc.perform(get("/dispatch-target").with(request -> {
                    request.setDispatcherType(DispatcherType.ERROR);
                    return request;
                }))
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
        mockMvc.perform(post("/api/test").with(user("member").roles("MEMBER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/test").with(user("member").roles("MEMBER"))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void 로그인_멤버는_파일을_업로드하고_공개_승격은_운영진만_한다()
            throws Exception {
        mockMvc.perform(post("/api/files/private").with(csrf()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/files/private")
                        .with(user("member").roles("MEMBER")).with(csrf()))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/files/1/public-promotions")
                        .with(user("member").roles("MEMBER")).with(csrf()))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/files/1/public-promotions")
                        .with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void 미인증_API는_로그인_리다이렉트_대신_JSON_401을_반환한다()
            throws Exception {
        mockMvc.perform(get("/api/test"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("C003"));
    }

    @Test
    void 로그인_멤버는_본인_정보와_팀_기준정보를_조회할_수_있다()
            throws Exception {
        for (String role : new String[]{"MEMBER", "LEADER", "ADMIN"}) {
            mockMvc.perform(get("/api/members/me").with(user("tester").roles(role)))
                    .andExpect(status().isOk());
            mockMvc.perform(get("/api/members/reference/teams")
                            .with(user("tester").roles(role)))
                    .andExpect(status().isOk());
        }
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

    @GetMapping({"/dashboard", "/members/test", "/notice-management/test",
            "/dispatch-target", "/api/test", "/api/members/me",
            "/api/members/reference/teams", "/api/admin/public-notices/test"})
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

    @PostMapping({"/api/files/private", "/api/files/1/public-promotions"})
    ResponseEntity<Void> fileChange() {
        return ResponseEntity.ok().build();
    }
}
