package kr.ac.tukorea.bandi.domain.performance.controller;

import kr.ac.tukorea.bandi.domain.performance.service.PerformancePublicPageService;
import kr.ac.tukorea.bandi.domain.performance.service.PerformancePublicNoticeService;
import kr.ac.tukorea.bandi.domain.performance.service.PerformanceContentService;
import kr.ac.tukorea.bandi.domain.performance.service.PerformanceRoundCastService;
import kr.ac.tukorea.bandi.domain.performance.service.PerformanceRoundService;
import kr.ac.tukorea.bandi.domain.performance.service.PublicPerformanceFileService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({PublicPerformanceApiController.class,
        PerformancePublicPageManagementApiController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import({ApiExceptionHandler.class, LoginMemberArgumentResolver.class,
        SecurityWebMvcConfig.class})
class PublicPerformanceApiControllerTest {
    private static final Long ACTOR_ID = 10L;

    private final MockMvc mockMvc;

    @MockitoBean
    private PerformancePublicPageService publicPageService;

    @MockitoBean
    private PerformanceRoundService roundService;

    @MockitoBean
    private PublicProfileService publicProfileService;

    @MockitoBean
    private PerformanceContentService contentService;

    @MockitoBean
    private PerformanceRoundCastService roundCastService;

    @MockitoBean
    private PublicPerformanceFileService publicPerformanceFileService;

    @MockitoBean
    private PerformancePublicNoticeService publicNoticeService;

    @Autowired
    PublicPerformanceApiControllerTest(MockMvc mockMvc) {
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
    void slug로_공개_공연과_회차를_조회한다() throws Exception {
        given(roundService.searchPublicRounds("hamlet")).willReturn(List.of());

        mockMvc.perform(get("/api/public-performances/{slug}", "hamlet"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/public-performances/{slug}/rounds", "hamlet"))
                .andExpect(status().isOk());

        verify(publicPageService).lookupPublic("hamlet");
        verify(roundService).searchPublicRounds("hamlet");
    }

    @Test
    void 관리자가_공연_페이지_초안을_등록한다() throws Exception {
        given(publicPageService.create(any(), any())).willReturn(40L);

        mockMvc.perform(post("/api/performance-page-management")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pageBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(40));

        verify(publicPageService).create(any(), any());
    }

    @Test
    void 공개_캐스팅과_제작진과_미디어를_조회한다() throws Exception {
        given(contentService.searchPublicCasts("hamlet")).willReturn(List.of());
        given(roundCastService.searchPublic("hamlet", 30L))
                .willReturn(List.of());
        given(contentService.searchPublicCredits("hamlet"))
                .willReturn(List.of());
        given(contentService.searchPublicMedia("hamlet")).willReturn(List.of());

        mockMvc.perform(get("/api/public-performances/{slug}/casts", "hamlet"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/public-performances/{slug}/rounds/"
                        + "{roundId}/casts", "hamlet", 30L))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/public-performances/{slug}/credits", "hamlet"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/public-performances/{slug}/media", "hamlet"))
                .andExpect(status().isOk());

        verify(contentService).searchPublicCasts("hamlet");
        verify(roundCastService).searchPublic("hamlet", 30L);
        verify(contentService).searchPublicCredits("hamlet");
        verify(contentService).searchPublicMedia("hamlet");
    }

    @Test
    void 공개_공연의_연결_공시를_조회한다() throws Exception {
        given(publicNoticeService.searchPublic("hamlet"))
                .willReturn(List.of());

        mockMvc.perform(get("/api/public-performances/{slug}/notices",
                        "hamlet"))
                .andExpect(status().isOk());

        verify(publicNoticeService).searchPublic("hamlet");
    }

    @Test
    void 운영진은_공연_공시를_연결하고_해제한다() throws Exception {
        mockMvc.perform(post("/api/performance-page-management/projects/"
                        + "{projectId}/notices", 20L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"publicNoticeId\":30}"))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/performance-page-management/projects/"
                        + "{projectId}/notices/{publicNoticeId}", 20L, 30L))
                .andExpect(status().isNoContent());

        verify(publicNoticeService).link(ACTOR_ID, 20L, 30L);
        verify(publicNoticeService).unlink(ACTOR_ID, 20L, 30L);
    }

    @Test
    void 공개_공연과_프로필_파일은_검증된_URL로_이동한다() throws Exception {
        given(publicPerformanceFileService.createPerformanceFileDownloadUrl(
                "hamlet", 11L)).willReturn("https://storage/performance");
        given(publicPerformanceFileService.createProfileFileDownloadUrl(
                21L, 12L)).willReturn("https://storage/profile");

        mockMvc.perform(get("/api/public-performances/{slug}/files/{storedFileId}",
                        "hamlet", 11L))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("https://storage/performance"));
        mockMvc.perform(get("/api/public-performances/profiles/{profileId}/files/"
                        + "{storedFileId}", 21L, 12L))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("https://storage/profile"));
    }

    @Test
    void 필수_공개_페이지값이_없으면_C001을_반환한다() throws Exception {
        mockMvc.perform(post("/api/performance-page-management")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    private String pageBody() {
        return """
                {
                  "performanceProjectId": 20,
                  "slug": "hamlet",
                  "shortDescription": "셰익스피어의 비극",
                  "synopsis": "덴마크 왕자의 복수극",
                  "genre": "비극",
                  "ageRating": "12세 이상",
                  "runtimeMinutes": 120,
                  "intermissionMinutes": 15,
                  "admissionFee": 0,
                  "contactName": "반디극회",
                  "contactChannel": "@bandi",
                  "organizerName": "반디극회"
                }
                """;
    }
}
