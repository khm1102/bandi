package kr.ac.tukorea.bandi.domain.resource.controller;

import kr.ac.tukorea.bandi.domain.resource.dto.request.ResourceManageSearchParam;
import kr.ac.tukorea.bandi.domain.resource.dto.request.ResourceRevisionParam;
import kr.ac.tukorea.bandi.domain.resource.dto.request.ResourceSearchParam;
import kr.ac.tukorea.bandi.domain.resource.dto.request.ResourceWriteParam;
import kr.ac.tukorea.bandi.domain.resource.model.ResourceStatus;
import kr.ac.tukorea.bandi.domain.resource.model.ResourceTargetScope;
import kr.ac.tukorea.bandi.domain.resource.service.ResourceService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ResourceApiController.class, ResourceManagementApiController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import({ApiExceptionHandler.class, LoginMemberArgumentResolver.class,
        SecurityWebMvcConfig.class})
class ResourceApiControllerTest {

    private static final Long ACTOR_ID = 10L;
    private static final Long RESOURCE_ID = 20L;
    private static final Long TEAM_ID = 3L;
    private static final Long FILE_ID = 30L;

    private final MockMvc mockMvc;

    @MockitoBean
    private ResourceService resourceService;

    @Autowired
    ResourceApiControllerTest(MockMvc mockMvc) {
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
    void 읽을_수_있는_자료를_검색한다() throws Exception {
        given(resourceService.searchReadable(any(), any())).willReturn(List.of());

        mockMvc.perform(get("/api/resources")
                        .param("keyword", "대본")
                        .param("categoryCode", "SCRIPT"))
                .andExpect(status().isOk());

        verify(resourceService).searchReadable(ACTOR_ID,
                new ResourceSearchParam("대본", "SCRIPT", 0, 20));
    }

    @Test
    void 자료_파일은_MinIO_주소로_리다이렉트한다() throws Exception {
        given(resourceService.createDownloadUrl(ACTOR_ID, RESOURCE_ID, FILE_ID))
                .willReturn("http://localhost:9000/bandi-private/file");

        mockMvc.perform(get("/api/resources/{resourceId}/files/{fileId}/download",
                        RESOURCE_ID, FILE_ID))
                .andExpect(status().isFound())
                .andExpect(header().string("Location",
                        "http://localhost:9000/bandi-private/file"));
    }

    @Test
    void 팀장이_팀_자료_초안을_등록한다() throws Exception {
        given(resourceService.createDraft(any(), any())).willReturn(RESOURCE_ID);

        mockMvc.perform(post("/api/resource-management")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resourceId").value(RESOURCE_ID));

        verify(resourceService).createDraft(ACTOR_ID,
                new ResourceWriteParam(ResourceTargetScope.TEAM, TEAM_ID,
                        "SCRIPT", "최종 대본", "3차 수정본", true,
                        List.of(FILE_ID)));
    }

    @Test
    void 새_파일_리비전을_생성한다() throws Exception {
        given(resourceService.replaceFiles(any(), any())).willReturn(2);

        mockMvc.perform(post("/api/resource-management/{resourceId}/revisions",
                        RESOURCE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"storedFileIds\":[30]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.revisionNo").value(2));

        verify(resourceService).replaceFiles(ACTOR_ID,
                new ResourceRevisionParam(RESOURCE_ID, List.of(FILE_ID)));
    }

    @Test
    void 관리_자료를_범위와_상태로_검색한다() throws Exception {
        given(resourceService.searchManageable(any(), any())).willReturn(List.of());

        mockMvc.perform(get("/api/resource-management")
                        .param("status", "PUBLISHED")
                        .param("targetScope", "TEAM")
                        .param("teamId", "3"))
                .andExpect(status().isOk());

        verify(resourceService).searchManageable(ACTOR_ID,
                new ResourceManageSearchParam(null, null,
                        ResourceStatus.PUBLISHED, ResourceTargetScope.TEAM,
                        TEAM_ID, 0, 20));
    }

    @Test
    void 자료를_게시하고_보관한다() throws Exception {
        mockMvc.perform(post("/api/resource-management/{resourceId}/publish",
                        RESOURCE_ID))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/resource-management/{resourceId}/archive",
                        RESOURCE_ID))
                .andExpect(status().isNoContent());

        verify(resourceService).publish(ACTOR_ID, RESOURCE_ID);
        verify(resourceService).archive(ACTOR_ID, RESOURCE_ID);
    }

    @Test
    void 파일_없는_리비전_요청은_C001을_반환한다() throws Exception {
        mockMvc.perform(post("/api/resource-management/{resourceId}/revisions",
                        RESOURCE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"storedFileIds\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    private String createBody() {
        return """
                {
                  "targetScope": "TEAM",
                  "teamId": 3,
                  "categoryCode": "SCRIPT",
                  "title": "최종 대본",
                  "description": "3차 수정본",
                  "pinned": true,
                  "storedFileIds": [30]
                }
                """;
    }
}
