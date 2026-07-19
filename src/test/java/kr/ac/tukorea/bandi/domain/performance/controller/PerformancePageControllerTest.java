package kr.ac.tukorea.bandi.domain.performance.controller;

import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformancePublicPageResponse;
import kr.ac.tukorea.bandi.domain.performance.model.PublicPageStatus;
import kr.ac.tukorea.bandi.domain.performance.service.PerformanceContentService;
import kr.ac.tukorea.bandi.domain.performance.service.PerformancePublicPageService;
import kr.ac.tukorea.bandi.domain.performance.service.PerformanceRoundService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PerformancePageController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class PerformancePageControllerTest {

    private final MockMvc mockMvc;

    @MockitoBean
    private PerformancePublicPageService publicPageService;

    @MockitoBean
    private PerformanceRoundService roundService;

    @MockitoBean
    private PerformanceContentService contentService;

    @Autowired
    PerformancePageControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void 공개_공연_마이크로사이트가_SSR로_렌더링된다() throws Exception {
        PerformancePublicPageResponse page = new PerformancePublicPageResponse(
                1L, 2L, "소년 B가 사는 집",
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 2), "TIP 아트센터",
                "house-boy", PublicPageStatus.PUBLISHED,
                "작품 소개", "시놉시스", "연출 노트", "연극",
                "12세 이상", 90, null, 0, null, null,
                "#36C9B4", "반디", "contact", "반디",
                "공연", "공연 소개", null, null, null);
        when(publicPageService.lookupPublic("house-boy")).thenReturn(page);
        when(publicPageService.lookupPublicViewingGuide(2L))
                .thenReturn(Optional.empty());
        when(roundService.searchPublicRounds("house-boy"))
                .thenReturn(List.of());
        when(contentService.searchPublicCasts("house-boy"))
                .thenReturn(List.of());
        when(contentService.searchPublicCredits("house-boy"))
                .thenReturn(List.of());
        when(contentService.searchPublicMedia("house-boy"))
                .thenReturn(List.of());

        mockMvc.perform(get("/performances/house-boy"))
                .andExpect(status().isOk())
                .andExpect(view().name("performance/public-detail"))
                .andExpect(model().attribute("page", page))
                .andExpect(model().attribute("rounds", List.of()))
                .andExpect(model().attribute("reservationAvailable", false));
    }
}
