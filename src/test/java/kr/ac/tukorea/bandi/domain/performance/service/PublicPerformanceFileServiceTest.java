package kr.ac.tukorea.bandi.domain.performance.service;

import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceMediaResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformancePublicPageResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PublicProfileViewResponse;
import kr.ac.tukorea.bandi.domain.performance.exception.PerformanceAccessDeniedException;
import kr.ac.tukorea.bandi.domain.performance.model.MediaType;
import kr.ac.tukorea.bandi.domain.performance.model.PublicPageStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PublicPerformanceFileServiceTest {

    private static final String SLUG = "hamlet";
    private static final Long FILE_ID = 11L;
    private static final Long PROFILE_ID = 21L;

    @Mock
    private PerformancePublicPageService publicPageService;
    @Mock
    private PerformanceContentService contentService;
    @Mock
    private PublicProfileService publicProfileService;
    @Mock
    private FileService fileService;

    private PublicPerformanceFileService service;

    @BeforeEach
    void setUp() {
        service = new PublicPerformanceFileService(publicPageService,
                contentService, publicProfileService, fileService);
    }

    @Test
    void 공개_공연_페이지_이미지는_다운로드_URL을_발급한다() {
        given(publicPageService.lookupPublic(SLUG)).willReturn(page(FILE_ID));
        given(fileService.createPublicDownloadUrl(FILE_ID))
                .willReturn("https://storage/page");

        String result = service.createPerformanceFileDownloadUrl(SLUG, FILE_ID);

        assertThat(result).isEqualTo("https://storage/page");
    }

    @Test
    void 게시된_공연_미디어는_다운로드_URL을_발급한다() {
        given(publicPageService.lookupPublic(SLUG)).willReturn(page(null));
        given(contentService.searchPublicMedia(SLUG))
                .willReturn(List.of(media(FILE_ID)));
        given(fileService.createPublicDownloadUrl(FILE_ID))
                .willReturn("https://storage/media");

        String result = service.createPerformanceFileDownloadUrl(SLUG, FILE_ID);

        assertThat(result).isEqualTo("https://storage/media");
    }

    @Test
    void 공개_동의된_프로필_사진은_다운로드_URL을_발급한다() {
        given(publicProfileService.lookupPublic(PROFILE_ID))
                .willReturn(new PublicProfileViewResponse(PROFILE_ID,
                        "배우", null, FILE_ID, null));
        given(fileService.createPublicDownloadUrl(FILE_ID))
                .willReturn("https://storage/profile");

        String result = service.createProfileFileDownloadUrl(PROFILE_ID, FILE_ID);

        assertThat(result).isEqualTo("https://storage/profile");
    }

    @Test
    void 공개_콘텐츠에_연결되지_않은_파일은_차단한다() {
        given(publicPageService.lookupPublic(SLUG)).willReturn(page(null));
        given(contentService.searchPublicMedia(SLUG)).willReturn(List.of());

        assertThatThrownBy(() ->
                service.createPerformanceFileDownloadUrl(SLUG, FILE_ID))
                .isInstanceOf(PerformanceAccessDeniedException.class);
        verify(fileService, never()).createPublicDownloadUrl(FILE_ID);
    }

    private PerformancePublicPageResponse page(Long heroFileId) {
        return new PerformancePublicPageResponse(1L, 2L, "햄릿",
                null, null, "소극장", SLUG, PublicPageStatus.PUBLISHED,
                "소개", "줄거리", null, "비극", "12세", 120,
                15, 0, heroFileId, null, "#000000", null, null,
                null, null, null, null, null, null);
    }

    private PerformanceMediaResponse media(Long storedFileId) {
        return new PerformanceMediaResponse(31L, 2L, storedFileId,
                MediaType.POSTER, "포스터", null, "포스터", null,
                null, 1, true);
    }
}
