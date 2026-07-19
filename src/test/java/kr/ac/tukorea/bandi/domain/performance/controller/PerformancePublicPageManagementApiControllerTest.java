package kr.ac.tukorea.bandi.domain.performance.controller;

import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceViewingGuideResponse;
import kr.ac.tukorea.bandi.domain.performance.service.PerformancePublicNoticeService;
import kr.ac.tukorea.bandi.domain.performance.service.PerformancePublicPageService;
import kr.ac.tukorea.bandi.global.config.SecurityWebMvcConfig;
import kr.ac.tukorea.bandi.global.security.LoginMemberArgumentResolver;
import kr.ac.tukorea.bandi.global.security.LoginPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PerformancePublicPageManagementApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({LoginMemberArgumentResolver.class, SecurityWebMvcConfig.class})
class PerformancePublicPageManagementApiControllerTest {

    private static final Long ACTOR_ID = 1L;
    private final MockMvc mockMvc;

    @MockitoBean
    private PerformancePublicPageService publicPageService;
    @MockitoBean
    private PerformancePublicNoticeService publicNoticeService;

    @Autowired
    PerformancePublicPageManagementApiControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @BeforeEach
    void authenticate() {
        LoginPrincipal principal = new LoginPrincipal(ACTOR_ID, "ADMIN");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(principal,
                        null, principal.authorities()));
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 비공개_프로젝트의_관람_안내를_관리용으로_조회한다()
            throws Exception {
        given(publicPageService.lookupViewingGuide(ACTOR_ID, 2L))
                .willReturn(Optional.of(new PerformanceViewingGuideResponse(
                        3L, 2L, "입장", "지연", "촬영", "취소",
                        "접근성", "길", null)));

        mockMvc.perform(get(
                        "/api/performance-page-management/projects/2/viewing-guide"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entryPolicy").value("입장"));
    }

    @Test
    void 관람_안내가_없으면_본문_없는_응답을_반환한다() throws Exception {
        given(publicPageService.lookupViewingGuide(ACTOR_ID, 2L))
                .willReturn(Optional.empty());

        mockMvc.perform(get(
                        "/api/performance-page-management/projects/2/viewing-guide"))
                .andExpect(status().isOk());
    }
}
