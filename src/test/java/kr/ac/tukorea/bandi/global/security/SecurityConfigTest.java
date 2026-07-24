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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    void 온보딩_안내는_모든_로그인_멤버에게_열리고_비로그인은_로그인으로_이동한다() throws Exception {
        mockMvc.perform(get("/onboarding"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
        for (String role : new String[]{"MEMBER", "LEADER", "ADMIN"}) {
            mockMvc.perform(get("/onboarding").with(user("member").roles(role)))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void 개인정보_안내와_운영_문의는_모든_로그인_멤버에게_열리고_비로그인은_로그인으로_이동한다()
            throws Exception {
        for (String path : new String[]{"/privacy", "/support"}) {
            mockMvc.perform(get(path))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("http://localhost/login"));
            for (String role : new String[]{"MEMBER", "LEADER", "ADMIN"}) {
                mockMvc.perform(get(path).with(user("member").roles(role)))
                        .andExpect(status().isOk());
            }
        }
    }

    @Test
    void 공개_공유_페이지는_비로그인도_열고_공유_API는_인증과_CSRF를_요구한다()
            throws Exception {
        mockMvc.perform(get("/share/test"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/internal-notices/1/share-link").with(csrf()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/internal-notices/1/share-link")
                        .with(user("member").roles("MEMBER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/internal-notices/1/share-link")
                        .with(user("member").roles("MEMBER")).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void 일반_멤버와_팀장은_관리자_화면에_접근할_수_없다() throws Exception {
        for (String role : new String[]{"MEMBER", "LEADER"}) {
            mockMvc.perform(get("/members/test").with(user("tester").roles(role)))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void 운영진은_남은_관리자_화면에_접근할_수_있다() throws Exception {
        for (String path : new String[]{"/members/test"}) {
            mockMvc.perform(get(path).with(user("admin").roles("ADMIN")))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void 폐기된_공개_경로는_로그인으로_보내지_않는다() throws Exception {
        for (String path : new String[]{"/performances/show", "/reserve/test",
                "/api/public-performances/test", "/api/public-policies/test",
                "/api/public-reservations/test",
                "/api/public-notices/test"}) {
            mockMvc.perform(get(path)).andExpect(status().isNotFound());
        }
        mockMvc.perform(post("/api/public-notices/test").with(csrf()))
                .andExpect(status().isNotFound());
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
    void 활동_내역서_화면과_생성_API는_모든_로그인_멤버가_사용한다()
            throws Exception {
        mockMvc.perform(get("/activity-documents"))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/api/activity-report-documents/blank"))
                .andExpect(status().isUnauthorized());
        for (String role : new String[]{"MEMBER", "LEADER", "ADMIN"}) {
            mockMvc.perform(get("/activity-documents")
                            .with(user("tester").roles(role)))
                    .andExpect(status().isOk());
            mockMvc.perform(get("/api/activity-report-documents/blank")
                            .with(user("tester").roles(role)))
                    .andExpect(status().isOk());
            mockMvc.perform(post("/api/activity-report-documents")
                            .with(user("tester").roles(role)))
                    .andExpect(status().isForbidden());
            mockMvc.perform(post("/api/activity-report-documents")
                            .with(user("tester").roles(role)).with(csrf()))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void 프로필은_로그인_멤버가_보고_수정하며_팀_멤버_관리는_팀장만_한다()
            throws Exception {
        mockMvc.perform(get("/profile").with(user("member").roles("MEMBER")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/members/me/profile").with(user("member").roles("MEMBER")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/members/1/profile-photo").with(user("member").roles("MEMBER")))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/members/me/profile-photo").with(csrf()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/members/me/profile-photo")
                        .with(user("member").roles("MEMBER")).with(csrf()))
                .andExpect(status().isOk());
        mockMvc.perform(get("/team-members").with(user("member").roles("MEMBER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/team-members").with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/members/team-members")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/members/team-members").with(user("member").roles("LEADER")))
                .andExpect(status().isOk());
    }

    @Test
    void 기수_추가는_관리자만_가능하고_기수_변경은_팀장과_관리자가_요청할_수_있다()
            throws Exception {
        mockMvc.perform(post("/api/members/cohorts")
                        .with(user("leader").roles("LEADER")).with(csrf()))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/members/cohorts")
                        .with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/members/1/cohort")
                        .with(user("member").roles("MEMBER")).with(csrf()))
                .andExpect(status().isForbidden());
        for (String role : new String[]{"LEADER", "ADMIN"}) {
            mockMvc.perform(patch("/api/members/1/cohort")
                            .with(user("operator").roles(role)).with(csrf()))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void 일반_멤버는_활동_승인_기록과_검수_화면에_접근할_수_없다() throws Exception {
        for (String path : new String[]{"/activity/archive", "/activity/review"}) {
            mockMvc.perform(get(path).with(user("member").roles("MEMBER")))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void 팀장과_운영진은_활동_승인_기록과_검수_화면에_접근할_수_있다() throws Exception {
        for (String role : new String[]{"LEADER", "ADMIN"}) {
            for (String path : new String[]{"/activity/archive", "/activity/review"}) {
                mockMvc.perform(get(path).with(user("operator").roles(role)))
                        .andExpect(status().isOk());
            }
        }
    }

    @Test
    void 공지_관리_API는_팀장과_운영진만_접근한다() throws Exception {
        mockMvc.perform(get("/api/internal-notice-management/test")
                        .with(user("member").roles("MEMBER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/internal-notice-management/test")
                        .with(user("leader").roles("LEADER")))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/internal-notice-management/test")
                        .with(user("leader").roles("LEADER")).with(csrf()))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/internal-notice-management/test")
                        .with(user("leader").roles("LEADER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/internal-notice-management/test")
                        .with(user("leader").roles("LEADER")).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void 공지_관리_화면은_팀장과_운영진만_접근한다() throws Exception {
        for (String path : new String[]{"/notices/manage", "/notices/manage/1"}) {
            mockMvc.perform(get(path).with(user("member").roles("MEMBER")))
                    .andExpect(status().isForbidden());
            mockMvc.perform(get(path).with(user("leader").roles("LEADER")))
                    .andExpect(status().isOk());
            mockMvc.perform(get(path).with(user("admin").roles("ADMIN")))
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

    @GetMapping({"/dashboard", "/members/test",
            "/dispatch-target", "/api/test", "/api/members/me",
            "/api/members/reference/teams",
            "/api/members/reference/cohorts",
            "/profile", "/team-members", "/api/members/me/profile",
            "/api/members/1/profile-photo", "/api/members/team-members",
            "/onboarding", "/privacy", "/support",
            "/api/internal-notice-management/test", "/notices/manage",
            "/notices/manage/1", "/activity-documents",
            "/api/activity-report-documents/blank", "/activity/archive",
            "/activity/review", "/share/test"})
    ResponseEntity<Void> page() {
        return ResponseEntity.ok().build();
    }

    @PostMapping({"/api/test", "/api/internal-notices/1/share-link",
            "/api/members/cohorts"})
    ResponseEntity<Void> change() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/activity-report-documents")
    ResponseEntity<Void> createActivityReport() {
        return ResponseEntity.ok().build();
    }

    @PostMapping({"/api/files/private", "/api/files/1/public-promotions"})
    ResponseEntity<Void> fileChange() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/internal-notice-management/test")
    ResponseEntity<Void> manageNotice() {
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/internal-notice-management/test")
    ResponseEntity<Void> deleteNotice() {
        return ResponseEntity.ok().build();
    }

    @PutMapping("/api/members/me/profile-photo")
    ResponseEntity<Void> updateProfilePhoto() {
        return ResponseEntity.ok().build();
    }

    @org.springframework.web.bind.annotation.PatchMapping("/api/members/1/cohort")
    ResponseEntity<Void> updateCohort() {
        return ResponseEntity.ok().build();
    }
}
