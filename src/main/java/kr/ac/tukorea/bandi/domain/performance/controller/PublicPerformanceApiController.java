package kr.ac.tukorea.bandi.domain.performance.controller;

import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformancePublicPageResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceMediaResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceViewingGuideResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PublicPerformanceCastResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PublicPerformanceRoundResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PublicPerformanceRoundCastResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PublicProductionCreditResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PublicProfileViewResponse;
import kr.ac.tukorea.bandi.domain.performance.service.PerformancePublicPageService;
import kr.ac.tukorea.bandi.domain.performance.service.PerformanceContentService;
import kr.ac.tukorea.bandi.domain.performance.service.PerformanceRoundCastService;
import kr.ac.tukorea.bandi.domain.performance.service.PerformanceRoundService;
import kr.ac.tukorea.bandi.domain.performance.service.PublicPerformanceFileService;
import kr.ac.tukorea.bandi.domain.performance.service.PublicProfileService;
import kr.ac.tukorea.bandi.global.swagger.PublicPerformanceApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PublicPerformanceApiController implements PublicPerformanceApiDocs {
    private final PerformancePublicPageService publicPageService;
    private final PerformanceRoundService roundService;
    private final PublicProfileService publicProfileService;
    private final PerformanceContentService contentService;
    private final PerformanceRoundCastService roundCastService;
    private final PublicPerformanceFileService publicPerformanceFileService;

    @Override
    public ResponseEntity<PerformancePublicPageResponse> lookup(String slug) {
        return ResponseEntity.ok(publicPageService.lookupPublic(slug));
    }

    @Override
    public ResponseEntity<List<PublicPerformanceRoundResponse>> searchRounds(
            String slug) {
        return ResponseEntity.ok(roundService.searchPublicRounds(slug));
    }

    @Override
    public ResponseEntity<PerformanceViewingGuideResponse> lookupViewingGuide(
            String slug) {
        PerformancePublicPageResponse page = publicPageService.lookupPublic(slug);
        return ResponseEntity.of(publicPageService.lookupPublicViewingGuide(
                page.performanceProjectId()));
    }

    @Override
    public ResponseEntity<PublicProfileViewResponse> lookupProfile(Long profileId) {
        return ResponseEntity.ok(publicProfileService.lookupPublic(profileId));
    }

    @Override
    public ResponseEntity<List<PublicPerformanceCastResponse>> searchCasts(
            String slug) {
        return ResponseEntity.ok(contentService.searchPublicCasts(slug));
    }

    @Override
    public ResponseEntity<List<PublicPerformanceRoundCastResponse>>
            searchRoundCasts(String slug, Long roundId) {
        return ResponseEntity.ok(roundCastService.searchPublic(slug, roundId));
    }

    @Override
    public ResponseEntity<List<PublicProductionCreditResponse>> searchCredits(
            String slug) {
        return ResponseEntity.ok(contentService.searchPublicCredits(slug));
    }

    @Override
    public ResponseEntity<List<PerformanceMediaResponse>> searchMedia(String slug) {
        return ResponseEntity.ok(contentService.searchPublicMedia(slug));
    }

    @Override
    public ResponseEntity<Void> downloadPerformanceFile(String slug,
                                                        Long storedFileId) {
        String url = publicPerformanceFileService
                .createPerformanceFileDownloadUrl(slug, storedFileId);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(url))
                .build();
    }

    @Override
    public ResponseEntity<Void> downloadProfileFile(Long profileId,
                                                    Long storedFileId) {
        String url = publicPerformanceFileService
                .createProfileFileDownloadUrl(profileId, storedFileId);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(url))
                .build();
    }
}
