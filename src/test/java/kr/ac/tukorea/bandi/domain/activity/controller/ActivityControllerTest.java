package kr.ac.tukorea.bandi.domain.activity.controller;

import kr.ac.tukorea.bandi.domain.activity.service.ActivityReportDocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ActivityController.class)
@AutoConfigureMockMvc(addFilters = false)
class ActivityControllerTest {

    private final MockMvc mockMvc;

    @MockitoBean
    private ActivityReportDocumentService service;

    @Autowired
    ActivityControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void 활동_내역서_생성_화면에_현재_회장_정보를_제공한다() throws Exception {
        given(service.lookupActivePresidentNameForPage())
                .willReturn(Optional.of("원동연"));

        mockMvc.perform(get("/activity-documents"))
                .andExpect(status().isOk())
                .andExpect(view().name("activity/document"))
                .andExpect(model().attribute("presidentName", "원동연"))
                .andExpect(model().attribute("presidentConfigured", true))
                .andExpect(model().attributeExists("activityReportForm"));
    }

    @Test
    void 회장_미설정이어도_화면은_안내와_함께_렌더링한다() throws Exception {
        given(service.lookupActivePresidentNameForPage()).willReturn(Optional.empty());

        mockMvc.perform(get("/activity-documents"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("presidentConfigured", false));
    }

    @Test
    void 활동_기록_내목록_검수_아카이브_화면을_분리해_렌더링한다() throws Exception {
        mockMvc.perform(get("/activity"))
                .andExpect(status().isOk())
                .andExpect(view().name("activity/list"));
        mockMvc.perform(get("/activity/review"))
                .andExpect(status().isOk())
                .andExpect(view().name("activity/review"));
        mockMvc.perform(get("/activity/review/{activityRecordId}", 12L))
                .andExpect(status().isOk())
                .andExpect(view().name("activity/review-detail"))
                .andExpect(model().attribute("activityRecordId", 12L));
        mockMvc.perform(get("/activity/archive"))
                .andExpect(status().isOk())
                .andExpect(view().name("activity/archive"));
        mockMvc.perform(get("/activity/archive/{activityRecordId}", 12L))
                .andExpect(status().isOk())
                .andExpect(view().name("activity/archive-detail"))
                .andExpect(model().attribute("activityRecordId", 12L));
    }
}
