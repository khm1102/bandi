package kr.ac.tukorea.bandi.domain.activity.controller;

import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityReportDocumentDraftResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityReportParticipantResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityReportDocumentSavedResponse;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecordStatus;
import kr.ac.tukorea.bandi.domain.activity.service.ActivityReportDocumentService;
import kr.ac.tukorea.bandi.domain.member.exception.ClubPresidentUnavailableException;
import kr.ac.tukorea.bandi.global.exception.ApiExceptionHandler;
import kr.ac.tukorea.bandi.global.security.LoginPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ActivityReportDocumentApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.context.annotation.Import(ApiExceptionHandler.class)
class ActivityReportDocumentApiControllerTest {

    private static final long ACTOR_ID = 11L;

    private final MockMvc mockMvc;

    @MockitoBean
    private ActivityReportDocumentService service;

    @Autowired
    ActivityReportDocumentApiControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @BeforeEach
    void setUpAuthentication() {
        LoginPrincipal principal = new LoginPrincipal(ACTOR_ID, "MEMBER");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        principal, null, principal.authorities()));
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
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
    void JSON과_사진으로_완성본을_임시_저장한다() throws Exception {
        given(service.saveDraft(any(), any(), any())).willReturn(
                new ActivityReportDocumentSavedResponse(21L, 31L,
                        "2026-02-11_반디_동아리_활동_내역서.hwpx",
                        ActivityRecordStatus.DRAFT));
        MockMultipartFile request = new MockMultipartFile("request", "",
                MediaType.APPLICATION_JSON_VALUE, """
                {
                  "title": "대본 리딩",
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
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        "/api/activity-report-documents/21"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void 저장된_활동_내역서를_검수_요청한다() throws Exception {
        given(service.submit(ACTOR_ID, 21L)).willReturn(1);

        mockMvc.perform(post("/api/activity-report-documents/21/submit"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"revisionNo\":1}"));
    }

    @Test
    void 저장된_활동_내역서_입력값을_조회한다() throws Exception {
        given(service.lookupDraft(ACTOR_ID, 21L)).willReturn(
                new ActivityReportDocumentDraftResponse(21L, "대본 리딩", "김현민", "종합관",
                        LocalDateTime.of(2026, 2, 11, 16, 30), "활동 내용",
                        List.of(new ActivityReportParticipantResponse("김현민",
                                "컴퓨터공학부", "2025591010", "")),
                        ActivityRecordStatus.DRAFT, 41L, "activity-photo.png",
                        31L, "activity-report.hwpx"));

        mockMvc.perform(get("/api/activity-report-documents/21"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "activityRecordId": 21,
                          "title": "대본 리딩",
                          "representative": "김현민",
                          "status": "DRAFT",
                          "photoStoredFileId": 41,
                          "documentStoredFileId": 31
                        }
                        """));
    }

    @Test
    void 저장된_활동_내역서를_수정하면_HWPX를_다시_생성한다() throws Exception {
        given(service.updateDraft(any(), any(), any(), any())).willReturn(
                new ActivityReportDocumentSavedResponse(21L, 32L,
                        "2026-02-11_반디_동아리_활동_내역서.hwpx",
                        ActivityRecordStatus.DRAFT));
        MockMultipartFile request = new MockMultipartFile("request", "",
                MediaType.APPLICATION_JSON_VALUE, """
                {
                  "title": "수정한 대본 리딩",
                  "representative": "김현민",
                  "location": "종합관",
                  "activityAt": "2026-02-11T16:30:00",
                  "content": "수정한 활동 내용",
                  "participants": [{"name": "김현민", "department": "컴퓨터공학부",
                    "studentNo": "2025591010", "note": ""}]
                }
                """.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        mockMvc.perform(multipart(HttpMethod.PUT,
                        "/api/activity-report-documents/21").file(request))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "activityRecordId": 21,
                          "documentStoredFileId": 32,
                          "status": "DRAFT"
                        }
                        """));
    }

    @Test
    void 사진이_없으면_잘못된_요청으로_응답한다() throws Exception {
        MockMultipartFile request = new MockMultipartFile("request", "",
                MediaType.APPLICATION_JSON_VALUE, "{}".getBytes());

        mockMvc.perform(multipart("/api/activity-report-documents").file(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 활동_기록_제목이_없으면_잘못된_요청으로_응답한다() throws Exception {
        MockMultipartFile request = new MockMultipartFile("request", "",
                MediaType.APPLICATION_JSON_VALUE, """
                {
                  "representative": "김현민",
                  "location": "종합관",
                  "activityAt": "2026-02-11T16:30:00",
                  "content": "활동 내용",
                  "participants": [{"name": "김현민"}]
                }
                """.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        MockMultipartFile photo = new MockMultipartFile("photo", "activity.png",
                MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/activity-report-documents")
                        .file(request).file(photo))
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
