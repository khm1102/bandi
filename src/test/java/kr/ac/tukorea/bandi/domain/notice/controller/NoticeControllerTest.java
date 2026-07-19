package kr.ac.tukorea.bandi.domain.notice.controller;

import kr.ac.tukorea.bandi.domain.notice.dto.request.PublicNoticeSearchParam;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeDetailResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeSummaryResponse;
import kr.ac.tukorea.bandi.domain.notice.service.PublicNoticeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(NoticeController.class)
@AutoConfigureMockMvc(addFilters = false)
class NoticeControllerTest {

    private final MockMvc mockMvc;

    @MockitoBean
    private PublicNoticeService publicNoticeService;

    @Autowired
    NoticeControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void 공개_공시_목록을_검색어와_페이지로_SSR_렌더링한다()
            throws Exception {
        PublicNoticeSummaryResponse notice =
                new PublicNoticeSummaryResponse(1L, "PERFORMANCE",
                        "공연 안내", true,
                        LocalDateTime.of(2026, 11, 1, 10, 0),
                        "운영진", LocalDateTime.of(2026, 11, 1, 10, 0));
        given(publicNoticeService.searchPublic(
                new PublicNoticeSearchParam("공연", 0, 20)))
                .willReturn(List.of(notice));

        mockMvc.perform(get("/notices").param("keyword", "공연"))
                .andExpect(status().isOk())
                .andExpect(view().name("notice/list"))
                .andExpect(model().attribute("notices", List.of(notice)))
                .andExpect(model().attribute("keyword", "공연"));
    }

    @Test
    void 공개_공시_상세를_SSR_렌더링한다() throws Exception {
        PublicNoticeDetailResponse notice = new PublicNoticeDetailResponse(
                1L, "PERFORMANCE", "공연 안내", "공시 본문", true,
                LocalDateTime.of(2026, 11, 1, 10, 0), null,
                "운영진", "운영진",
                LocalDateTime.of(2026, 11, 1, 10, 0), List.of());
        given(publicNoticeService.lookupPublic(1L)).willReturn(notice);

        mockMvc.perform(get("/notices/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("notice/detail"))
                .andExpect(model().attribute("notice", notice));
    }
}
