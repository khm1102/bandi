package kr.ac.tukorea.bandi.domain.performance.controller;

import kr.ac.tukorea.bandi.domain.performance.dto.request.PublicProfileConsentParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PublicProfileCreateParam;
import kr.ac.tukorea.bandi.domain.performance.model.ConsentScope;
import kr.ac.tukorea.bandi.domain.performance.service.PublicProfileService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicProfileApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ApiExceptionHandler.class, LoginMemberArgumentResolver.class,
        SecurityWebMvcConfig.class})
class PublicProfileApiControllerTest {
    private static final Long ACTOR_ID = 10L;
    private static final Long PROFILE_ID = 20L;

    private final MockMvc mockMvc;

    @MockitoBean
    private PublicProfileService publicProfileService;

    @Autowired
    PublicProfileApiControllerTest(MockMvc mockMvc) {
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
    void 공개_프로필을_등록한다() throws Exception {
        given(publicProfileService.create(any(), any())).willReturn(PROFILE_ID);

        mockMvc.perform(post("/api/public-profile-management")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberId\":11,\"publicName\":\"이서준\","
                                + "\"bio\":\"햄릿 역\",\"profileFileId\":30,"
                                + "\"socialUrl\":\"https://example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(PROFILE_ID));

        verify(publicProfileService).create(ACTOR_ID,
                new PublicProfileCreateParam(11L, "이서준", "햄릿 역",
                        30L, "https://example.com"));
    }

    @Test
    void 프로필_이름_공개에_동의한다() throws Exception {
        mockMvc.perform(post("/api/public-profile-management/{profileId}/consents",
                        PROFILE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"policyDocumentVersionId\":40,"
                                + "\"consentScope\":\"NAME\"}"))
                .andExpect(status().isNoContent());

        verify(publicProfileService).agree(ACTOR_ID,
                new PublicProfileConsentParam(PROFILE_ID, 40L,
                        ConsentScope.NAME));
    }

    @Test
    void 공개_프로필_이름이_없으면_C001을_반환한다() throws Exception {
        mockMvc.perform(post("/api/public-profile-management")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }
}
