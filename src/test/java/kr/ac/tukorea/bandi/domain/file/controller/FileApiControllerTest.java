package kr.ac.tukorea.bandi.domain.file.controller;

import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.domain.file.service.FileUploadParam;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FileApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ApiExceptionHandler.class, LoginMemberArgumentResolver.class,
        SecurityWebMvcConfig.class})
class FileApiControllerTest {

    private static final Long ACTOR_ID = 10L;
    private static final byte[] CONTENT = new byte[]{1, 2, 3, 4};

    private final MockMvc mockMvc;

    @MockitoBean
    private FileService fileService;

    @Autowired
    FileApiControllerTest(MockMvc mockMvc) {
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
    void 로그인_멤버가_비공개_파일을_업로드한다() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "proof.png", "image/png", CONTENT);
        given(fileService.uploadPrivate(any())).willReturn(20L);

        mockMvc.perform(multipart("/api/files/private")
                        .file(file)
                        .param("domain", "activity"))
                .andExpect(status().isCreated())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.id").value(20));

        var captor = org.mockito.ArgumentCaptor
                .forClass(FileUploadParam.class);
        verify(fileService).uploadPrivate(captor.capture());
        FileUploadParam param = captor.getValue();
        assertThat(param.domain()).isEqualTo("activity");
        assertThat(param.originalName()).isEqualTo("proof.png");
        assertThat(param.sizeBytes()).isEqualTo(CONTENT.length);
        assertThat(param.uploadedByMemberId()).isEqualTo(ACTOR_ID);
        try (InputStream input = param.contentSource().openStream()) {
            assertThat(input.readAllBytes()).containsExactly(CONTENT);
        }
    }

    @Test
    void 운영진이_비공개_파일을_공개_객체로_승격한다() throws Exception {
        given(fileService.promoteToPublic(20L, "performance", ACTOR_ID))
                .willReturn(30L);

        mockMvc.perform(post("/api/files/{storedFileId}/public-promotions",
                        20L)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"domain\":\"performance\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(30));

        verify(fileService).promoteToPublic(
                20L, "performance", ACTOR_ID);
    }

    @Test
    void 업로드_파일이_없으면_400을_반환한다() throws Exception {
        mockMvc.perform(multipart("/api/files/private")
                        .param("domain", "activity"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 업로드_도메인_형식이_잘못되면_C001을_반환한다() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "proof.png", "image/png", CONTENT);

        mockMvc.perform(multipart("/api/files/private")
                        .file(file)
                        .param("domain", "../activity"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }
}
