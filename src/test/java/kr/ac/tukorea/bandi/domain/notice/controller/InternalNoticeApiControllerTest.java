package kr.ac.tukorea.bandi.domain.notice.controller;

import kr.ac.tukorea.bandi.domain.notice.dto.request.InternalNoticeManageSearchParam;
import kr.ac.tukorea.bandi.domain.notice.dto.request.InternalNoticeSearchParam;
import kr.ac.tukorea.bandi.domain.notice.dto.request.InternalNoticeWriteParam;
import kr.ac.tukorea.bandi.domain.file.dto.response.FileReferenceResponse;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeStatus;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeReadFilter;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeTargetScope;
import kr.ac.tukorea.bandi.domain.notice.service.InternalNoticeService;
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
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({InternalNoticeApiController.class,
        InternalNoticeManagementApiController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import({ApiExceptionHandler.class, LoginMemberArgumentResolver.class,
        SecurityWebMvcConfig.class})
class InternalNoticeApiControllerTest {

    private static final Long ACTOR_ID = 10L;
    private static final Long NOTICE_ID = 20L;
    private static final Long TEAM_ID = 3L;
    private static final Long FILE_ID = 30L;

    private final MockMvc mockMvc;

    @MockitoBean
    private InternalNoticeService internalNoticeService;

    @Autowired
    InternalNoticeApiControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @BeforeEach
    void setUpAuthentication() {
        LoginPrincipal principal = new LoginPrincipal(ACTOR_ID, "LEADER");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(principal,
                        null, principal.authorities()));
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 읽을_수_있는_공지를_검색한다() throws Exception {
        given(internalNoticeService.searchReadable(any(), any()))
                .willReturn(kr.ac.tukorea.bandi.global.response.PageResponse.of(
                        List.of(), 0, 20, 0));

        mockMvc.perform(get("/api/internal-notices")
                        .param("keyword", "연습"))
                .andExpect(status().isOk());

        verify(internalNoticeService).searchReadable(ACTOR_ID,
                new InternalNoticeSearchParam("연습", InternalNoticeReadFilter.ALL,
                        null, 0, 20));
    }

    @Test
    void 내부_공지_첨부파일은_애플리케이션이_직접_전송한다() throws Exception {
        given(internalNoticeService.openAttachmentDownload(ACTOR_ID,
                NOTICE_ID, FILE_ID)).willReturn(
                new kr.ac.tukorea.bandi.global.response.FileDownloadResponse(
                        "notice.pdf", "application/pdf", 4,
                        new org.springframework.core.io.InputStreamResource(
                                new java.io.ByteArrayInputStream(new byte[]{1, 2, 3, 4}))));

        mockMvc.perform(get("/api/internal-notices/{noticeId}/attachments/"
                        + "{fileId}/download", NOTICE_ID, FILE_ID))
                .andExpect(status().isOk());
    }

    @Test
    void 공지_본문_이미지는_전용_API로_업로드하고_inline으로_전송한다() throws Exception {
        given(internalNoticeService.uploadInlineImage(any(), any()))
                .willReturn(new FileReferenceResponse(FILE_ID, "poster.png", "image/png", 4));
        given(internalNoticeService.openAttachmentInline(ACTOR_ID, NOTICE_ID, FILE_ID))
                .willReturn(new kr.ac.tukorea.bandi.global.response.FileDownloadResponse(
                        "poster.png", "image/png", 4,
                        new org.springframework.core.io.InputStreamResource(
                                new java.io.ByteArrayInputStream(new byte[]{1, 2, 3, 4}))));

        mockMvc.perform(multipart("/api/internal-notice-management/images")
                        .file(new MockMultipartFile("file", "poster.png", "image/png",
                                new byte[]{1, 2, 3, 4})))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storedFileId").value(FILE_ID))
                .andExpect(jsonPath("$.previewUrl").value(
                        "/api/internal-notice-management/images/30/preview"));
        mockMvc.perform(get("/api/internal-notices/{noticeId}/attachments/{fileId}/inline",
                        NOTICE_ID, FILE_ID))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("inline")));
    }

    @Test
    void 팀장이_팀_공지_초안을_등록한다() throws Exception {
        given(internalNoticeService.createDraft(any(), any())).willReturn(NOTICE_ID);

        mockMvc.perform(post("/api/internal-notice-management")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.internalNoticeId").value(NOTICE_ID));

        verify(internalNoticeService).createDraft(ACTOR_ID,
                new InternalNoticeWriteParam(InternalNoticeTargetScope.TEAM,
                        TEAM_ID, "무대팀 공지", "연습 일정", true,
                        List.of(FILE_ID)));
    }

    @Test
    void 관리_목록은_대상과_상태로_검색한다() throws Exception {
        given(internalNoticeService.searchManageable(any(), any()))
                .willReturn(kr.ac.tukorea.bandi.global.response.PageResponse.of(
                        List.of(), 0, 20, 0));

        mockMvc.perform(get("/api/internal-notice-management")
                        .param("status", "PUBLISHED")
                        .param("targetScope", "TEAM")
                        .param("teamId", "3"))
                .andExpect(status().isOk());

        verify(internalNoticeService).searchManageable(ACTOR_ID,
                new InternalNoticeManageSearchParam(null,
                        InternalNoticeStatus.PUBLISHED,
                        InternalNoticeTargetScope.TEAM, TEAM_ID, 0, 20));
    }

    @Test
    void 관리자는_공지_상태를_변경하고_초안을_삭제한다() throws Exception {
        mockMvc.perform(post("/api/internal-notice-management/{noticeId}/draft", NOTICE_ID))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/internal-notice-management/{noticeId}/close", NOTICE_ID))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/internal-notice-management/{noticeId}/archive", NOTICE_ID))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/internal-notice-management/{noticeId}", NOTICE_ID))
                .andExpect(status().isNoContent());

        verify(internalNoticeService).returnToDraft(ACTOR_ID, NOTICE_ID);
        verify(internalNoticeService).close(ACTOR_ID, NOTICE_ID);
        verify(internalNoticeService).archive(ACTOR_ID, NOTICE_ID);
        verify(internalNoticeService).deleteDraft(ACTOR_ID, NOTICE_ID);
    }

    @Test
    void 공지_작성자_또는_관리자는_제목_공개_공유_링크를_발급하고_중단한다()
            throws Exception {
        given(internalNoticeService.issuePublicShare(ACTOR_ID, NOTICE_ID))
                .willReturn("share-token");

        mockMvc.perform(post("/api/internal-notices/{noticeId}/share-link", NOTICE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shareUrl").value(
                        "http://localhost/share/notices/share-token"));
        mockMvc.perform(delete("/api/internal-notices/{noticeId}/share-link", NOTICE_ID))
                .andExpect(status().isNoContent());

        verify(internalNoticeService).revokePublicShare(ACTOR_ID, NOTICE_ID);
    }

    @Test
    void 관리_상세의_첨부파일을_직접_전송한다() throws Exception {
        given(internalNoticeService.openManageableAttachmentDownload(
                ACTOR_ID, NOTICE_ID, FILE_ID)).willReturn(
                new kr.ac.tukorea.bandi.global.response.FileDownloadResponse(
                        "notice.pdf", "application/pdf", 4,
                        new org.springframework.core.io.InputStreamResource(
                                new java.io.ByteArrayInputStream(new byte[]{1, 2, 3, 4}))));

        mockMvc.perform(get("/api/internal-notice-management/{noticeId}/attachments/"
                        + "{fileId}/download", NOTICE_ID, FILE_ID))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("attachment")));
    }

    @Test
    void 공지_제목이_비어_있으면_C001을_반환한다() throws Exception {
        mockMvc.perform(post("/api/internal-notice-management")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetScope\":\"ALL\",\"body\":\"내용\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    @Test
    void Markdown_미리보기는_관리_API에서_서버_정화_HTML만_반환한다() throws Exception {
        mockMvc.perform(post("/api/internal-notice-management/markdown-preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bodyMarkdown\":\"# 안내\",\"attachmentFileIds\":[]}"))
                .andExpect(status().isOk());

        verify(internalNoticeService).preview(ACTOR_ID, null, "# 안내", List.of());
    }

    private String writeBody() {
        return """
                {
                  "targetScope": "TEAM",
                  "teamId": 3,
                  "title": "무대팀 공지",
                  "body": "연습 일정",
                  "important": true,
                  "attachmentFileIds": [30]
                }
                """;
    }
}
