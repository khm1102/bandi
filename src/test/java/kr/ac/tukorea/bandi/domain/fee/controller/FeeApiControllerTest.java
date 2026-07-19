package kr.ac.tukorea.bandi.domain.fee.controller;

import kr.ac.tukorea.bandi.domain.fee.dto.request.FeeChargeProcessParam;
import kr.ac.tukorea.bandi.domain.fee.dto.request.FeeItemWriteParam;
import kr.ac.tukorea.bandi.domain.fee.dto.request.FeeOpenParam;
import kr.ac.tukorea.bandi.domain.fee.model.FeeChargeStatus;
import kr.ac.tukorea.bandi.domain.fee.service.FeeService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({FeeApiController.class, FeeManagementApiController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import({ApiExceptionHandler.class, LoginMemberArgumentResolver.class,
        SecurityWebMvcConfig.class})
class FeeApiControllerTest {

    private static final Long ACTOR_ID = 10L;
    private static final Long FEE_ITEM_ID = 20L;

    private final MockMvc mockMvc;

    @MockitoBean
    private FeeService feeService;

    @Autowired
    FeeApiControllerTest(MockMvc mockMvc) {
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
    void 내_회비_목록과_요약을_조회한다() throws Exception {
        given(feeService.searchMyFees(ACTOR_ID)).willReturn(List.of());

        mockMvc.perform(get("/api/fees/mine")).andExpect(status().isOk());
        mockMvc.perform(get("/api/fees/mine/summary")).andExpect(status().isOk());

        verify(feeService).searchMyFees(ACTOR_ID);
        verify(feeService).lookupMySummary(ACTOR_ID);
    }

    @Test
    void 관리자가_회비_항목을_등록한다() throws Exception {
        given(feeService.create(any(), any())).willReturn(FEE_ITEM_ID);

        mockMvc.perform(post("/api/fee-management")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.feeItemId").value(FEE_ITEM_ID));

        verify(feeService).create(ACTOR_ID,
                new FeeItemWriteParam("2026년 1학기 회비", "정기 회비",
                        (short) 2026, "FIRST", 30000L,
                        LocalDate.of(2026, 3, 31)));
    }

    @Test
    void 선택한_멤버에게_회비를_부과한다() throws Exception {
        given(feeService.open(any(), any())).willReturn(2);

        mockMvc.perform(post("/api/fee-management/{feeItemId}/open", FEE_ITEM_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"selectedMemberIds\":[11,12]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetCount").value(2));

        verify(feeService).open(ACTOR_ID,
                new FeeOpenParam(FEE_ITEM_ID, List.of(11L, 12L)));
    }

    @Test
    void 수납_상태를_일괄_처리한다() throws Exception {
        given(feeService.processCharges(any(), any())).willReturn(2);

        mockMvc.perform(post("/api/fee-management/{feeItemId}/charges/process",
                        FEE_ITEM_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"feeChargeIds\":[31,32],\"status\":\"PAID\","
                                + "\"reason\":\"계좌 입금 확인\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processedCount").value(2));

        verify(feeService).processCharges(ACTOR_ID,
                new FeeChargeProcessParam(FEE_ITEM_ID, List.of(31L, 32L),
                        FeeChargeStatus.PAID, "계좌 입금 확인"));
    }

    @Test
    void 회비를_마감하고_사유와_함께_취소한다() throws Exception {
        mockMvc.perform(post("/api/fee-management/{feeItemId}/close", FEE_ITEM_ID))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/fee-management/{feeItemId}/cancel", FEE_ITEM_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"행사 취소\"}"))
                .andExpect(status().isNoContent());

        verify(feeService).close(ACTOR_ID, FEE_ITEM_ID);
        verify(feeService).cancel(ACTOR_ID, FEE_ITEM_ID, "행사 취소");
    }

    @Test
    void 수납_대상이_비어_있으면_C001을_반환한다() throws Exception {
        mockMvc.perform(post("/api/fee-management/{feeItemId}/charges/process",
                        FEE_ITEM_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"feeChargeIds\":[],\"status\":\"PAID\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    private String itemBody() {
        return """
                {
                  "name": "2026년 1학기 회비",
                  "description": "정기 회비",
                  "referenceYear": 2026,
                  "referenceTermCode": "FIRST",
                  "amount": 30000,
                  "dueDate": "2026-03-31"
                }
                """;
    }
}
