package kr.ac.tukorea.bandi.domain.portal.controller;

import kr.ac.tukorea.bandi.domain.activity.controller.ActivityController;
import kr.ac.tukorea.bandi.domain.asset.controller.AssetController;
import kr.ac.tukorea.bandi.domain.calendar.controller.CalendarController;
import kr.ac.tukorea.bandi.domain.dashboard.controller.DashboardController;
import kr.ac.tukorea.bandi.domain.member.controller.MemberController;
import kr.ac.tukorea.bandi.domain.resource.controller.ResourceController;
import kr.ac.tukorea.bandi.global.security.LoginPrincipal;
import kr.ac.tukorea.bandi.global.security.LoginViewModelAdvice;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest({DashboardController.class, CalendarController.class,
        ResourceController.class, ActivityController.class, AssetController.class,
        MemberController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import(LoginViewModelAdvice.class)
@ActiveProfiles("test")
class SsrControllerRoutingTest {

    private static final Map<String, String> PAGE_VIEWS = Map.of(
            "dashboard", "dashboard/index",
            "calendar", "schedule/calendar",
            "resources", "resources/list",
            "activity", "activity/list",
            "props", "props/list",
            "members", "members/list");

    private final MockMvc mockMvc;

    @Autowired
    SsrControllerRoutingTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest
    @ValueSource(strings = {"dashboard", "calendar", "resources", "activity",
            "props", "members"})
    void 유지되는_내부_화면이_렌더링된다(String page) throws Exception {
        LoginPrincipal principal = new LoginPrincipal(1L, "ADMIN");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(principal,
                        null, principal.authorities()));

        mockMvc.perform(get("/" + page))
                .andExpect(status().isOk())
                .andExpect(view().name(PAGE_VIEWS.get(page)))
                .andExpect(model().attribute("role", "admin"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"schedule", "community", "dues", "attendance",
            "production", "checklist", "performance-management",
            "performance-content-management", "reservations", "showops",
            "reserve", "reserve/lookup", "performances/house-boy", "style-guide",
            "notices", "notices/1", "notice-management", "notice-management/write"})
    void 폐기된_화면은_라우팅하지_않는다(String page) throws Exception {
        mockMvc.perform(get("/" + page))
                .andExpect(status().isNotFound());
    }
}
