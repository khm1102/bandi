package kr.ac.tukorea.bandi.global.config;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import kr.ac.tukorea.bandi.global.security.SecurityConfig;

@WebMvcTest(PublishingController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("dev")
class PublishingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest
    @ValueSource(strings = {"dashboard", "calendar", "schedule", "resources", "activity", "community",
            "props", "reservations", "showops", "checklist", "attendance", "dues", "members"})
    void 관리자_퍼블리싱_화면이_렌더링된다(String page) throws Exception {
        mockMvc.perform(get("/" + page).with(user("tester")))
                .andExpect(status().isOk())
                .andExpect(view().name("publishing/" + page))
                .andExpect(model().attribute("role", "admin"));
    }

    @Test
    void role_파라미터가_모델로_전달된다() throws Exception {
        mockMvc.perform(get("/dues").param("role", "member").with(user("tester")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("role", "member"));
    }

    @Test
    void 허용되지_않은_role_값은_admin으로_대체된다() throws Exception {
        mockMvc.perform(get("/dues").param("role", "hacker").with(user("tester")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("role", "admin"));
    }

    @ParameterizedTest
    @CsvSource({"reservations,member", "reservations,leader", "showops,member", "showops,leader",
            "members,member"})
    void 화면이_허용하지_않는_role은_대시보드로_리다이렉트된다(String page, String role) throws Exception {
        mockMvc.perform(get("/" + page).param("role", role).with(user("tester")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard?role=" + role));
    }

    @ParameterizedTest
    @CsvSource({"reservations,admin", "showops,admin", "members,leader", "members,admin",
            "dues,member"})
    void 화면이_허용하는_role은_그대로_렌더링된다(String page, String role) throws Exception {
        mockMvc.perform(get("/" + page).param("role", role).with(user("tester")))
                .andExpect(status().isOk())
                .andExpect(view().name("publishing/" + page))
                .andExpect(model().attribute("role", role));
    }

    @Test
    void allowedRoles가_화면별_허용_역할로_모델에_전달된다() throws Exception {
        mockMvc.perform(get("/reservations").with(user("tester")))
                .andExpect(model().attribute("allowedRoles", Set.of("admin")));
        mockMvc.perform(get("/members").with(user("tester")))
                .andExpect(model().attribute("allowedRoles", Set.of("leader", "admin")));
        mockMvc.perform(get("/dues").with(user("tester")))
                .andExpect(model().attribute("allowedRoles", Set.of("member", "leader", "admin")));
    }

    @Test
    void 로그인_화면이_렌더링되고_mode가_분기된다() throws Exception {
        mockMvc.perform(get("/login").with(user("tester")))
                .andExpect(status().isOk())
                .andExpect(view().name("publishing/login"))
                .andExpect(model().attribute("mode", "login"));
        mockMvc.perform(get("/login").param("mode", "signup").with(user("tester")))
                .andExpect(model().attribute("mode", "signup"));
    }

    @Test
    void 예매_화면이_렌더링된다() throws Exception {
        mockMvc.perform(get("/reserve").with(user("tester")))
                .andExpect(status().isOk())
                .andExpect(view().name("publishing/reserve"));
    }
}
