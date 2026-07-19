package kr.ac.tukorea.bandi.domain.production.controller;

import kr.ac.tukorea.bandi.domain.checklist.controller.ChecklistApiController;
import kr.ac.tukorea.bandi.domain.checklist.dto.request.ChecklistCompletionParam;
import kr.ac.tukorea.bandi.domain.checklist.dto.request.ChecklistItemCreateParam;
import kr.ac.tukorea.bandi.domain.checklist.model.ChecklistScope;
import kr.ac.tukorea.bandi.domain.checklist.service.ChecklistService;
import kr.ac.tukorea.bandi.domain.production.dto.request.ProductionTaskCreateParam;
import kr.ac.tukorea.bandi.domain.production.dto.request.ProductionTaskStatusParam;
import kr.ac.tukorea.bandi.domain.production.model.ProductionTaskStatus;
import kr.ac.tukorea.bandi.domain.production.service.ProductionTaskService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ProductionTaskApiController.class, ChecklistApiController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import({ApiExceptionHandler.class, LoginMemberArgumentResolver.class,
        SecurityWebMvcConfig.class})
class ProductionChecklistApiControllerTest {
    private static final Long ACTOR_ID = 10L;
    private static final Long PROJECT_ID = 20L;
    private static final Long TEAM_ID = 3L;

    private final MockMvc mockMvc;

    @MockitoBean
    private ProductionTaskService productionTaskService;

    @MockitoBean
    private ChecklistService checklistService;

    @Autowired
    ProductionChecklistApiControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @BeforeEach
    void setUpAuthentication() {
        LoginPrincipal principal = new LoginPrincipal(ACTOR_ID, "LEADER");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(principal,
                        null, principal.authorities()));
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 팀_제작_업무를_등록한다() throws Exception {
        given(productionTaskService.create(any(), any())).willReturn(30L);

        mockMvc.perform(post("/api/production-tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"performanceProjectId\":20,\"teamId\":3,"
                                + "\"title\":\"무대 설계\",\"startDate\":\"2026-03-01\","
                                + "\"dueDate\":\"2026-04-01\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productionTaskId").value(30));

        verify(productionTaskService).create(ACTOR_ID,
                new ProductionTaskCreateParam(PROJECT_ID, TEAM_ID, "무대 설계",
                        null, LocalDate.of(2026, 3, 1),
                        LocalDate.of(2026, 4, 1)));
    }

    @Test
    void 제작_업무를_막힘_상태로_변경한다() throws Exception {
        mockMvc.perform(patch("/api/production-tasks/{taskId}/status", 30)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"BLOCKED\","
                                + "\"blockedReason\":\"자재 미입고\","
                                + "\"comment\":\"구매 대기\"}"))
                .andExpect(status().isNoContent());

        verify(productionTaskService).changeStatus(ACTOR_ID,
                new ProductionTaskStatusParam(30L, ProductionTaskStatus.BLOCKED,
                        "자재 미입고", "구매 대기"));
    }

    @Test
    void 회차_체크리스트를_등록하고_완료한다() throws Exception {
        given(checklistService.create(any(), any())).willReturn(40L);

        mockMvc.perform(post("/api/checklist-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"performanceProjectId\":20,"
                                + "\"performanceRoundId\":31,\"teamId\":3,"
                                + "\"scope\":\"ROUND\",\"content\":\"소품 배치 확인\","
                                + "\"required\":true,\"displayOrder\":0}"))
                .andExpect(status().isCreated());
        mockMvc.perform(patch("/api/checklist-items/{itemId}/completion", 40)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"completed\":true,\"reason\":\"현장 확인\"}"))
                .andExpect(status().isNoContent());

        verify(checklistService).create(ACTOR_ID,
                new ChecklistItemCreateParam(PROJECT_ID, 31L, TEAM_ID,
                        ChecklistScope.ROUND, "소품 배치 확인", true, 0));
        verify(checklistService).changeCompleted(ACTOR_ID,
                new ChecklistCompletionParam(40L, true, "현장 확인"));
    }

    @Test
    void 필수_제작값이_없으면_C001을_반환한다() throws Exception {
        mockMvc.perform(post("/api/production-tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }
}
