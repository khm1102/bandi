package kr.ac.tukorea.bandi.domain.performance.controller;

import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceCastAssignParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceCharacterWriteParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceMediaWriteParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceRoundCastAssignParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.ProductionCreditWriteParam;
import kr.ac.tukorea.bandi.domain.performance.model.CastType;
import kr.ac.tukorea.bandi.domain.performance.model.CharacterImportance;
import kr.ac.tukorea.bandi.domain.performance.service.PerformanceContentService;
import kr.ac.tukorea.bandi.domain.performance.service.PerformanceRoundCastService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PerformanceContentApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ApiExceptionHandler.class, LoginMemberArgumentResolver.class,
        SecurityWebMvcConfig.class})
class PerformanceContentApiControllerTest {

    private static final Long ACTOR_ID = 10L;
    private static final Long PROJECT_ID = 20L;
    private static final Long ROUND_ID = 30L;

    private final MockMvc mockMvc;

    @MockitoBean
    private PerformanceContentService contentService;

    @MockitoBean
    private PerformanceRoundCastService roundCastService;

    @Autowired
    PerformanceContentApiControllerTest(MockMvc mockMvc) {
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
    void 등장인물_목록을_조회한다() throws Exception {
        given(contentService.searchCharacters(ACTOR_ID, PROJECT_ID))
                .willReturn(List.of());

        mockMvc.perform(get("/api/performance-content-management/"
                        + "projects/{projectId}/characters", PROJECT_ID))
                .andExpect(status().isOk());

        verify(contentService).searchCharacters(ACTOR_ID, PROJECT_ID);
    }

    @Test
    void 등장인물을_등록한다() throws Exception {
        given(contentService.createCharacter(any(), any())).willReturn(40L);

        mockMvc.perform(post("/api/performance-content-management/characters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(characterBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(40));

        verify(contentService).createCharacter(ACTOR_ID,
                new PerformanceCharacterWriteParam(null, PROJECT_ID,
                        "햄릿", "덴마크의 왕자", CharacterImportance.LEAD, 0));
    }

    @Test
    void 작품_캐스팅을_배정한다() throws Exception {
        given(contentService.assignCast(any(), any())).willReturn(50L);

        mockMvc.perform(post("/api/performance-content-management/casts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(castBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(50));

        verify(contentService).assignCast(ACTOR_ID,
                new PerformanceCastAssignParam(PROJECT_ID, 40L, 70L,
                        CastType.PRIMARY, 0, "첫 배정"));
    }

    @Test
    void 제작진_크레딧을_등록한다() throws Exception {
        given(contentService.createCredit(any(), any())).willReturn(60L);

        mockMvc.perform(post("/api/performance-content-management/credits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(creditBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(60));

        verify(contentService).createCredit(ACTOR_ID,
                new ProductionCreditWriteParam(null, PROJECT_ID,
                        "연출", "김연출", null, 0));
    }

    @Test
    void 공연_미디어를_등록한다() throws Exception {
        given(contentService.createMedia(any(), any())).willReturn(80L);

        mockMvc.perform(post("/api/performance-content-management/media")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mediaBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(80));

        verify(contentService).createMedia(ACTOR_ID,
                new PerformanceMediaWriteParam(null, PROJECT_ID, 90L,
                        kr.ac.tukorea.bandi.domain.performance.model.MediaType.POSTER,
                        "메인 포스터", "공연 메인 포스터",
                        "햄릿 공연 포스터", "디자인팀", null, 0));
    }

    @Test
    void 회차별_캐스팅을_배정한다() throws Exception {
        given(roundCastService.assign(any(), any())).willReturn(100L);

        mockMvc.perform(post("/api/performance-content-management/round-casts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roundCastBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100));

        verify(roundCastService).assign(ACTOR_ID,
                new PerformanceRoundCastAssignParam(PROJECT_ID, ROUND_ID,
                        40L, 70L, CastType.PRIMARY, "1회차 배정"));
    }

    @Test
    void 캐스팅_해제_사유를_전달한다() throws Exception {
        mockMvc.perform(delete("/api/performance-content-management/"
                        + "casts/{castId}", 50L)
                        .param("reason", "배우 변경"))
                .andExpect(status().isNoContent());

        verify(contentService).removeCast(ACTOR_ID, 50L, "배우 변경");
    }

    @Test
    void 필수_콘텐츠값이_없으면_C001을_반환한다() throws Exception {
        mockMvc.perform(post("/api/performance-content-management/characters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    private String characterBody() {
        return """
                {
                  "performanceProjectId": 20,
                  "name": "햄릿",
                  "description": "덴마크의 왕자",
                  "importance": "LEAD",
                  "displayOrder": 0
                }
                """;
    }

    private String castBody() {
        return """
                {
                  "performanceProjectId": 20,
                  "performanceCharacterId": 40,
                  "publicProfileId": 70,
                  "castType": "PRIMARY",
                  "displayOrder": 0,
                  "reason": "첫 배정"
                }
                """;
    }

    private String creditBody() {
        return """
                {
                  "performanceProjectId": 20,
                  "creditRole": "연출",
                  "publicName": "김연출",
                  "displayOrder": 0
                }
                """;
    }

    private String mediaBody() {
        return """
                {
                  "performanceProjectId": 20,
                  "storedFileId": 90,
                  "mediaType": "POSTER",
                  "title": "메인 포스터",
                  "description": "공연 메인 포스터",
                  "altText": "햄릿 공연 포스터",
                  "creditText": "디자인팀",
                  "displayOrder": 0
                }
                """;
    }

    private String roundCastBody() {
        return """
                {
                  "performanceProjectId": 20,
                  "performanceRoundId": 30,
                  "performanceCharacterId": 40,
                  "publicProfileId": 70,
                  "castType": "PRIMARY",
                  "reason": "1회차 배정"
                }
                """;
    }
}
