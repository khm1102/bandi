package kr.ac.tukorea.bandi.domain.performance.service;

import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformancePublicPageResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PublicProfileViewResponse;
import kr.ac.tukorea.bandi.domain.performance.exception.PerformanceAccessDeniedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicPerformanceFileService {

    private final PerformancePublicPageService publicPageService;
    private final PerformanceContentService contentService;
    private final PublicProfileService publicProfileService;
    private final FileService fileService;

    public String createPerformanceFileDownloadUrl(String slug,
                                                   Long storedFileId) {
        PerformancePublicPageResponse page = publicPageService.lookupPublic(slug);
        boolean pageFile = Objects.equals(page.heroFileId(), storedFileId)
                || Objects.equals(page.posterFileId(), storedFileId)
                || Objects.equals(page.ogImageFileId(), storedFileId);
        boolean mediaFile = pageFile || contentService.searchPublicMedia(slug)
                .stream()
                .anyMatch(media -> Objects.equals(
                        media.storedFileId(), storedFileId));
        if (!mediaFile) {
            throw new PerformanceAccessDeniedException();
        }
        return fileService.createPublicDownloadUrl(storedFileId);
    }

    public String createProfileFileDownloadUrl(Long publicProfileId,
                                               Long storedFileId) {
        PublicProfileViewResponse profile = publicProfileService
                .lookupPublic(publicProfileId);
        if (!Objects.equals(profile.profileFileId(), storedFileId)) {
            throw new PerformanceAccessDeniedException();
        }
        return fileService.createPublicDownloadUrl(storedFileId);
    }
}
