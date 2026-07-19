package kr.ac.tukorea.bandi.global.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformancePublicPageResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformancePublicNoticeResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceRoundResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceViewingGuideResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceMediaResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PublicPerformanceCastResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PublicPerformanceRoundResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PublicPerformanceRoundCastResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PublicProductionCreditResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PublicProfileViewResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/public-performances")
@Tag(name = ApiTag.PERFORMANCE, description = "외부 공개 공연 기본 정보 API")
public interface PublicPerformanceApiDocs {
    @Operation(summary = "공개 공연 페이지 조회")
    @GetMapping("/{slug}")
    ResponseEntity<PerformancePublicPageResponse> lookup(@PathVariable String slug);

    @Operation(summary = "공개 공연 회차와 접근성 지원 조회")
    @GetMapping("/{slug}/rounds")
    ResponseEntity<List<PublicPerformanceRoundResponse>> searchRounds(
            @PathVariable String slug);

    @Operation(summary = "공개 공연 관람 안내 조회")
    @GetMapping("/{slug}/viewing-guide")
    ResponseEntity<PerformanceViewingGuideResponse> lookupViewingGuide(
            @PathVariable String slug);

    @Operation(summary = "동의 항목만 포함한 공개 프로필 조회")
    @GetMapping("/profiles/{profileId}")
    ResponseEntity<PublicProfileViewResponse> lookupProfile(
            @PathVariable Long profileId);

    @Operation(summary = "공개 작품 캐스팅 보드 조회")
    @GetMapping("/{slug}/casts")
    ResponseEntity<List<PublicPerformanceCastResponse>> searchCasts(
            @PathVariable String slug);

    @Operation(summary = "공개 회차별 캐스팅 조회")
    @GetMapping("/{slug}/rounds/{roundId}/casts")
    ResponseEntity<List<PublicPerformanceRoundCastResponse>> searchRoundCasts(
            @PathVariable String slug, @PathVariable Long roundId);

    @Operation(summary = "공개 제작진 크레딧 조회")
    @GetMapping("/{slug}/credits")
    ResponseEntity<List<PublicProductionCreditResponse>> searchCredits(
            @PathVariable String slug);

    @Operation(summary = "공개 공연 미디어 조회")
    @GetMapping("/{slug}/media")
    ResponseEntity<List<PerformanceMediaResponse>> searchMedia(
            @PathVariable String slug);

    @Operation(summary = "공개 공연 관련 공시 조회")
    @GetMapping("/{slug}/notices")
    ResponseEntity<List<PerformancePublicNoticeResponse>> searchNotices(
            @PathVariable String slug);

    @Operation(summary = "공개 공연 이미지·미디어 파일 조회")
    @GetMapping("/{slug}/files/{storedFileId}")
    ResponseEntity<Void> downloadPerformanceFile(
            @PathVariable String slug,
            @PathVariable Long storedFileId);

    @Operation(summary = "공개 프로필 사진 파일 조회")
    @GetMapping("/profiles/{profileId}/files/{storedFileId}")
    ResponseEntity<Void> downloadProfileFile(
            @PathVariable Long profileId,
            @PathVariable Long storedFileId);
}
