package kr.ac.tukorea.bandi.domain.notice.controller;

import kr.ac.tukorea.bandi.domain.notice.dto.request.PublicNoticePublishParam;
import kr.ac.tukorea.bandi.domain.notice.dto.request.PublicNoticeSearchParam;
import kr.ac.tukorea.bandi.domain.notice.dto.request.PublicNoticeWriteParam;
import kr.ac.tukorea.bandi.domain.notice.model.PublicNoticeStatus;
import kr.ac.tukorea.bandi.domain.notice.service.PublicNoticeService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({PublicNoticeApiController.class,
        PublicNoticeManagementApiController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import({ApiExceptionHandler.class, LoginMemberArgumentResolver.class,
        SecurityWebMvcConfig.class})
class PublicNoticeApiControllerTest {

    private static final Long ACTOR_ID = 10L;
    private static final Long NOTICE_ID = 20L;
    private static final Long FILE_ID = 30L;

    private final MockMvc mockMvc;

    @MockitoBean
    private PublicNoticeService publicNoticeService;

    @Autowired
    PublicNoticeApiControllerTest(MockMvc mockMvc) {
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
    void 공개_공시를_검색한다() throws Exception {
        given(publicNoticeService.searchPublic(any())).willReturn(List.of());

        mockMvc.perform(get("/api/public-notices")
                        .param("keyword", "공연")
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk());

        verify(publicNoticeService).searchPublic(
                new PublicNoticeSearchParam("공연", 1, 10));
    }

    @Test
    void 공개_첨부파일은_MinIO_주소로_리다이렉트한다() throws Exception {
        given(publicNoticeService.createAttachmentDownloadUrl(NOTICE_ID, FILE_ID))
                .willReturn("http://localhost:9000/bandi-private/file");

        mockMvc.perform(get("/api/public-notices/{noticeId}/attachments/"
                        + "{fileId}/download", NOTICE_ID, FILE_ID))
                .andExpect(status().isFound())
                .andExpect(header().string("Location",
                        "http://localhost:9000/bandi-private/file"));
    }

    @Test
    void 관리자가_공시_초안을_등록한다() throws Exception {
        given(publicNoticeService.createDraft(any(), any())).willReturn(NOTICE_ID);

        mockMvc.perform(post("/api/admin/public-notices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.publicNoticeId").value(NOTICE_ID));

        verify(publicNoticeService).createDraft(ACTOR_ID,
                new PublicNoticeWriteParam("PERFORMANCE", "공연 안내",
                        "상세 안내", true, List.of(FILE_ID)));
    }

    @Test
    void 관리자가_공시를_예약_게시한다() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 8, 1, 9, 0);

        mockMvc.perform(post("/api/admin/public-notices/{noticeId}/publish",
                        NOTICE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"publishStartDttm\":\"2026-08-01T09:00:00\"}"))
                .andExpect(status().isNoContent());

        verify(publicNoticeService).publish(ACTOR_ID,
                new PublicNoticePublishParam(NOTICE_ID, start, null));
    }

    @Test
    void 잘못된_관리_검색_상태는_C001을_반환한다() throws Exception {
        mockMvc.perform(get("/api/admin/public-notices")
                        .param("status", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    private String writeBody() {
        return """
                {
                  "categoryCode": "PERFORMANCE",
                  "title": "공연 안내",
                  "body": "상세 안내",
                  "pinned": true,
                  "attachmentFileIds": [30]
                }
                """;
    }
}
