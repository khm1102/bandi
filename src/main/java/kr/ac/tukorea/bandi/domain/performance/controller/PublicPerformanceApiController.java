package kr.ac.tukorea.bandi.domain.performance.controller;

import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformancePublicPageResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceViewingGuideResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PublicPerformanceRoundResponse;
import kr.ac.tukorea.bandi.domain.performance.service.PerformancePublicPageService;
import kr.ac.tukorea.bandi.domain.performance.service.PerformanceRoundService;
import kr.ac.tukorea.bandi.global.swagger.PublicPerformanceApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PublicPerformanceApiController implements PublicPerformanceApiDocs {
    private final PerformancePublicPageService publicPageService;
    private final PerformanceRoundService roundService;

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
}
