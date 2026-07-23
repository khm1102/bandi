package kr.ac.tukorea.bandi.domain.portal.controller;

import kr.ac.tukorea.bandi.domain.activity.controller.ActivityController;
import kr.ac.tukorea.bandi.domain.asset.controller.AssetController;
import kr.ac.tukorea.bandi.domain.calendar.controller.CalendarController;
import kr.ac.tukorea.bandi.domain.dashboard.controller.DashboardController;
import kr.ac.tukorea.bandi.domain.member.controller.MemberController;
import kr.ac.tukorea.bandi.domain.notice.controller.NoticeController;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeDetailResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeDetailViewResponse;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeTargetScope;
import kr.ac.tukorea.bandi.domain.notice.service.InternalNoticeService;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.hamcrest.Matchers.instanceOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest({DashboardController.class, CalendarController.class,
        ResourceController.class, ActivityController.class, AssetController.class,
        MemberController.class, NoticeController.class})
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
            "members", "members/list",
            "profile", "members/profile",
            "team-members", "members/team-members",
            "notices", "notice/list",
            "notices/write", "notice/form");

    @MockitoBean
    private InternalNoticeService internalNoticeService;

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
            "props", "members", "profile", "team-members", "notices", "notices/write"})
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

    @Test
    void 공지_상세_화면이_읽음_기록과_함께_렌더링된다() throws Exception {
        authenticate();
        given(internalNoticeService.lookupReadable(1L, 10L)).willReturn(noticeDetail());

        mockMvc.perform(get("/notices/10"))
                .andExpect(status().isOk())
                .andExpect(view().name("notice/detail"))
                .andExpect(model().attribute("notice", instanceOf(
                        InternalNoticeDetailViewResponse.class)));
    }

    @Test
    void 공지_수정_화면은_JSP에_record를_전달하지_않는다() throws Exception {
        authenticate();

        mockMvc.perform(get("/notices/10/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("notice/form"))
                .andExpect(model().attribute("noticeId", 10L))
                .andExpect(model().attributeDoesNotExist("notice"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"schedule", "community", "dues", "attendance",
            "production", "checklist", "performance-management",
            "performance-content-management", "reservations", "showops",
            "reserve", "reserve/lookup", "performances/house-boy", "style-guide",
            "notice-management", "notice-management/write"})
    void 폐기된_화면은_라우팅하지_않는다(String page) throws Exception {
        mockMvc.perform(get("/" + page))
                .andExpect(status().isNotFound());
    }

    private void authenticate() {
        LoginPrincipal principal = new LoginPrincipal(1L, "ADMIN");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(principal,
                        null, principal.authorities()));
    }

    private InternalNoticeDetailResponse noticeDetail() {
        return new InternalNoticeDetailResponse(10L, InternalNoticeTargetScope.ALL,
                null, null, "공지", null, false,
                LocalDateTime.of(2026, 7, 23, 16, 30), null, "관리자",
                LocalDateTime.of(2026, 7, 23, 17, 10), List.of());
    }
}
