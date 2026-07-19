package kr.ac.tukorea.bandi.domain.portal.controller;

import java.util.Map;
import kr.ac.tukorea.bandi.domain.activity.controller.ActivityController;
import kr.ac.tukorea.bandi.domain.asset.controller.AssetController;
import kr.ac.tukorea.bandi.domain.calendar.controller.CalendarController;
import kr.ac.tukorea.bandi.domain.checklist.controller.ChecklistController;
import kr.ac.tukorea.bandi.domain.dashboard.controller.DashboardController;
import kr.ac.tukorea.bandi.domain.event.controller.ClubEventController;
import kr.ac.tukorea.bandi.domain.fee.controller.FeeController;
import kr.ac.tukorea.bandi.domain.member.controller.MemberController;
import kr.ac.tukorea.bandi.domain.notice.controller.NoticeController;
import kr.ac.tukorea.bandi.domain.reservation.controller.ReservationController;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest({DashboardController.class, CalendarController.class,
        ResourceController.class, ActivityController.class,
        AssetController.class, ReservationController.class,
        ChecklistController.class, ClubEventController.class,
        FeeController.class, MemberController.class, NoticeController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import(LoginViewModelAdvice.class)
@ActiveProfiles("test")
class SsrControllerRoutingTest {

    private static final Map<String, String> PAGE_VIEWS = Map.ofEntries(
            Map.entry("dashboard", "dashboard/index"),
            Map.entry("calendar", "schedule/calendar"),
            Map.entry("resources", "resources/list"),
            Map.entry("activity", "activity/list"),
            Map.entry("props", "props/list"),
            Map.entry("reservations", "reservation/management"),
            Map.entry("showops", "showops/operations"),
            Map.entry("checklist", "checklist/index"),
            Map.entry("attendance", "attendance/index"),
            Map.entry("dues", "dues/list"),
            Map.entry("members", "members/list"));

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
    @ValueSource(strings = {"dashboard", "calendar", "resources",
            "activity", "props", "reservations", "showops", "checklist",
            "attendance", "dues", "members"})
    void 운영_프로파일에서_내부_화면이_렌더링된다(String page)
            throws Exception {
        LoginPrincipal principal = new LoginPrincipal(1L, "ADMIN");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(principal,
                        null, principal.authorities()));

        mockMvc.perform(get("/" + page))
                .andExpect(status().isOk())
                .andExpect(view().name(PAGE_VIEWS.get(page)))
                .andExpect(model().attribute("role", "admin"));
    }

    @Test
    void 공개_공시와_관람_신청_화면이_렌더링된다() throws Exception {
        mockMvc.perform(get("/notices"))
                .andExpect(status().isOk())
                .andExpect(view().name("notice/list"));
        mockMvc.perform(get("/reserve"))
                .andExpect(status().isOk())
                .andExpect(view().name("reservation/form"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"schedule", "community"})
    void 폐기된_화면은_라우팅하지_않는다(String page) throws Exception {
        mockMvc.perform(get("/" + page))
                .andExpect(status().isNotFound());
    }
}
