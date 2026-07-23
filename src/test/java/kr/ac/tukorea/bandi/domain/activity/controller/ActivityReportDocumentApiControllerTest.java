package kr.ac.tukorea.bandi.domain.activity.controller;

import kr.ac.tukorea.bandi.domain.activity.service.ActivityReportDocumentService;
import kr.ac.tukorea.bandi.domain.member.exception.ClubPresidentUnavailableException;
import kr.ac.tukorea.bandi.global.exception.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ActivityReportDocumentApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.context.annotation.Import(ApiExceptionHandler.class)
class ActivityReportDocumentApiControllerTest {

    private final MockMvc mockMvc;

    @MockitoBean
    private ActivityReportDocumentService service;

    @Autowired
    ActivityReportDocumentApiControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void 빈_양식을_HWPX와_no_store로_전송한다() throws Exception {
        given(service.createBlank()).willReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/api/activity-report-documents/blank"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hwp+zip"))
                .andExpect(header().string("Cache-Control",
                        org.hamcrest.Matchers.containsString("no-store")))
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("attachment")));
    }

    @Test
    void JSON과_사진으로_완성본을_생성한다() throws Exception {
        given(service.create(any(), any())).willReturn(new byte[]{1, 2, 3});
        MockMultipartFile request = new MockMultipartFile("request", "",
                MediaType.APPLICATION_JSON_VALUE, """
                {
                  "representative": "김현민",
                  "location": "종합관",
                  "activityAt": "2026-02-11T16:30:00",
                  "content": "활동 내용",
                  "participants": [{"name": "김현민", "department": "컴퓨터공학부",
                    "studentNo": "2025591010", "note": ""}]
                }
                """.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        MockMultipartFile photo = new MockMultipartFile("photo", "activity.png",
                MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/activity-report-documents")
                        .file(request).file(photo))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hwp+zip"))
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("2026-02-11")));
    }

    @Test
    void 사진이_없으면_잘못된_요청으로_응답한다() throws Exception {
        MockMultipartFile request = new MockMultipartFile("request", "",
                MediaType.APPLICATION_JSON_VALUE, "{}".getBytes());

        mockMvc.perform(multipart("/api/activity-report-documents").file(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 현재_활성_회장이_없으면_빈_양식도_충돌로_응답한다() throws Exception {
        given(service.createBlank()).willThrow(new ClubPresidentUnavailableException());

        mockMvc.perform(get("/api/activity-report-documents/blank"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
