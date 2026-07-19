package kr.ac.tukorea.bandi.domain.policy.controller;

import kr.ac.tukorea.bandi.domain.policy.dto.request.PolicyDocumentCreateParam;
import kr.ac.tukorea.bandi.domain.policy.dto.request.PolicyVersionPublishParam;
import kr.ac.tukorea.bandi.domain.policy.dto.response.PolicyVersionResponse;
import kr.ac.tukorea.bandi.domain.policy.model.PolicyAudience;
import kr.ac.tukorea.bandi.domain.policy.model.PolicyType;
import kr.ac.tukorea.bandi.domain.policy.service.PolicyService;
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

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({PolicyApiController.class, PublicPolicyApiController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import({ApiExceptionHandler.class, LoginMemberArgumentResolver.class,
        SecurityWebMvcConfig.class})
class PolicyApiControllerTest {

    private static final Long ACTOR_ID = 10L;
    private static final Long DOCUMENT_ID = 20L;
    private static final LocalDateTime EFFECTIVE_DTTM =
            LocalDateTime.of(2026, 8, 1, 0, 0);

    private final MockMvc mockMvc;

    @MockitoBean
    private PolicyService policyService;

    @Autowired
    PolicyApiControllerTest(MockMvc mockMvc) {
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
    void 운영진이_정책_문서를_생성한다() throws Exception {
        given(policyService.createDocument(any(), any()))
                .willReturn(DOCUMENT_ID);

        mockMvc.perform(post("/api/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"policyType\":\"RESERVATION_PRIVACY\","
                                + "\"title\":\"관람 신청 개인정보\","
                                + "\"audience\":\"VISITOR\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(DOCUMENT_ID));

        verify(policyService).createDocument(ACTOR_ID,
                new PolicyDocumentCreateParam(
                        PolicyType.RESERVATION_PRIVACY,
                        "관람 신청 개인정보", PolicyAudience.VISITOR));
    }

    @Test
    void 운영진이_정책의_새_버전을_게시한다() throws Exception {
        given(policyService.publishVersion(any(), any())).willReturn(30L);

        mockMvc.perform(post("/api/policies/{policyDocumentId}/versions",
                        DOCUMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\":\"수집 및 이용 동의\","
                                + "\"effectiveFromDttm\":"
                                + "\"2026-08-01T00:00:00\","
                                + "\"required\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(30));

        verify(policyService).publishVersion(ACTOR_ID,
                new PolicyVersionPublishParam(DOCUMENT_ID,
                        "수집 및 이용 동의", EFFECTIVE_DTTM, true));
    }

    @Test
    void 관람객이_현재_개인정보_동의문을_조회한다() throws Exception {
        given(policyService.lookupCurrentReservationPrivacy()).willReturn(
                new PolicyVersionResponse(30L, DOCUMENT_ID, 1,
                        "수집 및 이용 동의", EFFECTIVE_DTTM, ACTOR_ID,
                        EFFECTIVE_DTTM, true));

        mockMvc.perform(get("/api/public-policies/reservation-privacy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.policyDocumentVersionId").value(30))
                .andExpect(jsonPath("$.body").value("수집 및 이용 동의"));
    }

    @Test
    void 정책_필수값이_없으면_C001을_반환한다() throws Exception {
        mockMvc.perform(post("/api/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }
}
