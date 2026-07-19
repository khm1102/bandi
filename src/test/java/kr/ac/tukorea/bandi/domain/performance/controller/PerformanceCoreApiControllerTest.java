package kr.ac.tukorea.bandi.domain.performance.controller;

import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceProjectCreateParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceProjectSearchCondition;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceRoundAccessibilityWriteParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceRoundWriteParam;
import kr.ac.tukorea.bandi.domain.performance.model.AccessibilitySupportType;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceProjectStatus;
import kr.ac.tukorea.bandi.domain.performance.service.PerformanceProjectService;
import kr.ac.tukorea.bandi.domain.performance.service.PerformanceRoundService;
import kr.ac.tukorea.bandi.global.config.SecurityWebMvcConfig;
import kr.ac.tukorea.bandi.global.exception.ApiExceptionHandler;
import kr.ac.tukorea.bandi.global.security.LoginMemberArgumentResolver;
import kr.ac.tukorea.bandi.global.security.LoginPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PerformanceCoreApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ApiExceptionHandler.class, LoginMemberArgumentResolver.class,
        SecurityWebMvcConfig.class})
class PerformanceCoreApiControllerTest {

    private static final Long ACTOR_ID = 10L;
    private static final Long PROJECT_ID = 20L;
    private static final Long ROUND_ID = 30L;

    private final MockMvc mockMvc;

    @MockitoBean
    private PerformanceProjectService projectService;

    @MockitoBean
    private PerformanceRoundService roundService;

    @Autowired
    PerformanceCoreApiControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @BeforeEach
    void setUpAuthentication() {
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
    void 공연_프로젝트를_검색한다() throws Exception {
        given(projectService.search(any(), any())).willReturn(List.of());

        mockMvc.perform(get("/api/performance-management/projects")
                        .param("academicYear", "2026")
                        .param("termCode", "FIRST")
                        .param("status", "PLANNING"))
                .andExpect(status().isOk());

        verify(projectService).search(ACTOR_ID,
                new PerformanceProjectSearchCondition((short) 2026,
                        "FIRST", PerformanceProjectStatus.PLANNING, 0, 20));
    }

    @Test
    void 학기_공연_프로젝트를_등록한다() throws Exception {
        given(projectService.create(any(), any())).willReturn(PROJECT_ID);

        mockMvc.perform(post("/api/performance-management/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(PROJECT_ID));

        verify(projectService).create(ACTOR_ID,
                new PerformanceProjectCreateParam((short) 2026, "FIRST",
                        "햄릿", LocalDate.of(2026, 3, 1),
                        LocalDate.of(2026, 6, 30), "소극장"));
    }

    @Test
    void 공연_회차를_등록한다() throws Exception {
        given(roundService.createRound(any(), any())).willReturn(ROUND_ID);

        mockMvc.perform(post("/api/performance-management/rounds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roundBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ROUND_ID));

        verify(roundService).createRound(ACTOR_ID,
                new PerformanceRoundWriteParam(null, PROJECT_ID, 1,
                        LocalDateTime.of(2026, 6, 20, 19, 0),
                        LocalDateTime.of(2026, 6, 20, 18, 0),
                        LocalDateTime.of(2026, 5, 1, 0, 0),
                        LocalDateTime.of(2026, 6, 19, 23, 59)));
    }

    @Test
    void 회차별_접근성_지원을_등록한다() throws Exception {
        given(roundService.createAccessibility(any(), any())).willReturn(40L);

        mockMvc.perform(post("/api/performance-management/rounds/{roundId}/"
                        + "accessibilities", ROUND_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"supportType\":\"SIGN_LANGUAGE\","
                                + "\"title\":\"수어 통역\",\"displayOrder\":0}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(40));

        verify(roundService).createAccessibility(ACTOR_ID,
                new PerformanceRoundAccessibilityWriteParam(null, ROUND_ID,
                        AccessibilitySupportType.SIGN_LANGUAGE,
                        "수어 통역", null, 0));
    }

    @Test
    void 필수_공연값이_없으면_C001을_반환한다() throws Exception {
        mockMvc.perform(post("/api/performance-management/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    private String projectBody() {
        return """
                {
                  "academicYear": 2026,
                  "termCode": "FIRST",
                  "title": "햄릿",
                  "productionStartDate": "2026-03-01",
                  "productionEndDate": "2026-06-30",
                  "place": "소극장"
                }
                """;
    }

    private String roundBody() {
        return """
                {
                  "performanceProjectId": 20,
                  "roundNo": 1,
                  "startDttm": "2026-06-20T19:00:00",
                  "entryStartDttm": "2026-06-20T18:00:00",
                  "reservationOpenDttm": "2026-05-01T00:00:00",
                  "reservationCloseDttm": "2026-06-19T23:59:00"
                }
                """;
    }
}
