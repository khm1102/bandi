package kr.ac.tukorea.bandi.domain.activity.controller;

import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityFileAddParam;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityFileReplaceParam;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityManageSearchParam;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityRecordSearchParam;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityRecordWriteParam;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityFileRole;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecordStatus;
import kr.ac.tukorea.bandi.domain.activity.service.ActivityRecordService;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ActivityRecordApiController.class,
        ActivityManagementApiController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import({ApiExceptionHandler.class, LoginMemberArgumentResolver.class,
        SecurityWebMvcConfig.class})
class ActivityApiControllerTest {

    private static final Long ACTOR_ID = 10L;
    private static final Long RECORD_ID = 20L;
    private static final Long TEAM_ID = 3L;
    private static final Long FILE_ID = 30L;
    private static final Long RECORD_FILE_ID = 40L;
    private static final LocalDateTime ACTIVITY_DTTM =
            LocalDateTime.of(2026, 8, 1, 19, 0);

    private final MockMvc mockMvc;

    @MockitoBean
    private ActivityRecordService activityRecordService;

    @Autowired
    ActivityApiControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @BeforeEach
    void setUpAuthentication() {
        LoginPrincipal principal = new LoginPrincipal(ACTOR_ID, "MEMBER");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(principal,
                        null, principal.authorities()));
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 승인된_활동_기록을_기간으로_검색한다() throws Exception {
        given(activityRecordService.searchApproved(any(), any()))
                .willReturn(List.of());

        mockMvc.perform(get("/api/activity-records")
                        .param("teamId", "3")
                        .param("dateFrom", "2026-08-01")
                        .param("dateTo", "2026-08-31"))
                .andExpect(status().isOk());

        verify(activityRecordService).searchApproved(ACTOR_ID,
                new ActivityRecordSearchParam(TEAM_ID,
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31), 0, 20));
    }

    @Test
    void 승인된_증빙은_MinIO_주소로_리다이렉트한다() throws Exception {
        given(activityRecordService.createApprovedDownloadUrl(ACTOR_ID,
                RECORD_ID, FILE_ID)).willReturn(
                "http://localhost:9000/bandi-private/evidence");

        mockMvc.perform(get("/api/activity-records/{recordId}/files/"
                        + "{fileId}/download", RECORD_ID, FILE_ID))
                .andExpect(status().isFound())
                .andExpect(header().string("Location",
                        "http://localhost:9000/bandi-private/evidence"));
    }

    @Test
    void 관리_가능한_현재_증빙은_MinIO_주소로_리다이렉트한다() throws Exception {
        given(activityRecordService.createManageableDownloadUrl(ACTOR_ID,
                RECORD_ID, FILE_ID)).willReturn(
                "http://localhost:9000/bandi-private/manage-evidence");

        mockMvc.perform(get("/api/activity-management/{recordId}/files/"
                        + "{fileId}/download", RECORD_ID, FILE_ID))
                .andExpect(status().isFound())
                .andExpect(header().string("Location",
                        "http://localhost:9000/bandi-private/manage-evidence"));
    }

    @Test
    void 멤버가_자기_팀_활동_초안을_등록한다() throws Exception {
        given(activityRecordService.createDraft(any(), any())).willReturn(RECORD_ID);

        mockMvc.perform(post("/api/activity-management")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.activityRecordId").value(RECORD_ID));

        verify(activityRecordService).createDraft(ACTOR_ID,
                new ActivityRecordWriteParam(TEAM_ID, ACTIVITY_DTTM,
                        "무대 제작", "세트 제작 활동", 8));
    }

    @Test
    void 증빙_이미지를_추가하고_교체한다() throws Exception {
        mockMvc.perform(post("/api/activity-management/{recordId}/files", RECORD_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"storedFileId\":30,\"fileRole\":\"EVIDENCE\"}"))
                .andExpect(status().isNoContent());
        mockMvc.perform(put("/api/activity-management/files/{fileId}",
                        RECORD_FILE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newStoredFileId\":31}"))
                .andExpect(status().isNoContent());

        verify(activityRecordService).addFile(ACTOR_ID,
                new ActivityFileAddParam(RECORD_ID, FILE_ID,
                        ActivityFileRole.EVIDENCE));
        verify(activityRecordService).replaceFile(ACTOR_ID,
                new ActivityFileReplaceParam(RECORD_FILE_ID, 31L));
    }

    @Test
    void 활동_기록을_제출하면_리비전_번호를_반환한다() throws Exception {
        given(activityRecordService.submit(ACTOR_ID, RECORD_ID, "사진 보완"))
                .willReturn(2);

        mockMvc.perform(post("/api/activity-management/{recordId}/submit",
                        RECORD_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"changeReason\":\"사진 보완\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revisionNo").value(2));
    }

    @Test
    void 팀장이_승인하거나_보완을_요청한다() throws Exception {
        mockMvc.perform(post("/api/activity-management/{recordId}/approve",
                        RECORD_ID))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/activity-management/{recordId}/revision-request",
                        RECORD_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comment\":\"네이비즘 시각이 보이게 올려주세요\"}"))
                .andExpect(status().isNoContent());

        verify(activityRecordService).approve(ACTOR_ID, RECORD_ID);
        verify(activityRecordService).requestRevision(ACTOR_ID, RECORD_ID,
                "네이비즘 시각이 보이게 올려주세요");
    }

    @Test
    void 관리_기록을_상태와_작성자로_검색한다() throws Exception {
        given(activityRecordService.searchManageable(any(), any()))
                .willReturn(List.of());

        mockMvc.perform(get("/api/activity-management")
                        .param("status", "SUBMITTED")
                        .param("createdByMemberId", "10"))
                .andExpect(status().isOk());

        verify(activityRecordService).searchManageable(ACTOR_ID,
                new ActivityManageSearchParam(null, ActivityRecordStatus.SUBMITTED,
                        ACTOR_ID, 0, 20));
    }

    @Test
    void 보완_요청_문구가_비어_있으면_C001을_반환한다() throws Exception {
        mockMvc.perform(post("/api/activity-management/{recordId}/revision-request",
                        RECORD_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comment\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    private String createBody() {
        return """
                {
                  "teamId": 3,
                  "activityDttm": "2026-08-01T19:00:00",
                  "title": "무대 제작",
                  "body": "세트 제작 활동",
                  "participantCount": 8
                }
                """;
    }
}
