package kr.ac.tukorea.bandi.global.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceCastAssignRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceCastChangeRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceCharacterRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceMediaPublishedRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceMediaRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceRoundCastAssignRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceRoundCastChangeRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.request.ProductionCreditRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceCastHistoryResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceCastResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceCharacterResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceIdentifierResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceMediaResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceRoundCastResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.ProductionCreditResponse;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("/api/performance-content-management")
@Tag(name = ApiTag.PERFORMANCE,
        description = "공연 등장인물·캐스팅·제작진·미디어 관리 API")
@SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SCHEME)
public interface PerformanceContentApiDocs {

    @Operation(summary = "등장인물 목록 조회")
    @GetMapping("/projects/{projectId}/characters")
    ResponseEntity<List<PerformanceCharacterResponse>> searchCharacters(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long projectId);

    @Operation(summary = "등장인물 등록")
    @PostMapping("/characters")
    ResponseEntity<PerformanceIdentifierResponse> createCharacter(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @Valid @RequestBody PerformanceCharacterRequest request);

    @Operation(summary = "등장인물 수정")
    @PutMapping("/characters/{characterId}")
    ResponseEntity<Void> updateCharacter(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long characterId,
            @Valid @RequestBody PerformanceCharacterRequest request);

    @Operation(summary = "등장인물 삭제")
    @DeleteMapping("/characters/{characterId}")
    ResponseEntity<Void> removeCharacter(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long characterId);

    @Operation(summary = "작품 캐스팅 목록 조회")
    @GetMapping("/projects/{projectId}/casts")
    ResponseEntity<List<PerformanceCastResponse>> searchCasts(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long projectId);

    @Operation(summary = "캐스팅 변경 이력 조회")
    @GetMapping("/projects/{projectId}/cast-histories")
    ResponseEntity<List<PerformanceCastHistoryResponse>> searchCastHistories(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long projectId);

    @Operation(summary = "작품 캐스팅 배정")
    @PostMapping("/casts")
    ResponseEntity<PerformanceIdentifierResponse> assignCast(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @Valid @RequestBody PerformanceCastAssignRequest request);

    @Operation(summary = "작품 캐스팅 변경")
    @PutMapping("/casts/{castId}")
    ResponseEntity<Void> changeCast(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long castId,
            @Valid @RequestBody PerformanceCastChangeRequest request);

    @Operation(summary = "작품 캐스팅 해제")
    @DeleteMapping("/casts/{castId}")
    ResponseEntity<Void> removeCast(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long castId,
            @RequestParam(required = false) String reason);

    @Operation(summary = "제작진 크레딧 목록 조회")
    @GetMapping("/projects/{projectId}/credits")
    ResponseEntity<List<ProductionCreditResponse>> searchCredits(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long projectId);

    @Operation(summary = "제작진 크레딧 등록")
    @PostMapping("/credits")
    ResponseEntity<PerformanceIdentifierResponse> createCredit(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @Valid @RequestBody ProductionCreditRequest request);

    @Operation(summary = "제작진 크레딧 수정")
    @PutMapping("/credits/{creditId}")
    ResponseEntity<Void> updateCredit(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long creditId,
            @Valid @RequestBody ProductionCreditRequest request);

    @Operation(summary = "제작진 크레딧 삭제")
    @DeleteMapping("/credits/{creditId}")
    ResponseEntity<Void> removeCredit(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long creditId);

    @Operation(summary = "공연 미디어 목록 조회")
    @GetMapping("/projects/{projectId}/media")
    ResponseEntity<List<PerformanceMediaResponse>> searchMedia(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long projectId);

    @Operation(summary = "공연 미디어 등록")
    @PostMapping("/media")
    ResponseEntity<PerformanceIdentifierResponse> createMedia(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @Valid @RequestBody PerformanceMediaRequest request);

    @Operation(summary = "공연 미디어 수정")
    @PutMapping("/media/{mediaId}")
    ResponseEntity<Void> updateMedia(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long mediaId,
            @Valid @RequestBody PerformanceMediaRequest request);

    @Operation(summary = "공연 미디어 게시 상태 변경")
    @PatchMapping("/media/{mediaId}/published")
    ResponseEntity<Void> changeMediaPublished(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long mediaId,
            @Valid @RequestBody PerformanceMediaPublishedRequest request);

    @Operation(summary = "공연 미디어 삭제")
    @DeleteMapping("/media/{mediaId}")
    ResponseEntity<Void> removeMedia(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long mediaId);

    @Operation(summary = "회차별 캐스팅 목록 조회")
    @GetMapping("/rounds/{roundId}/casts")
    ResponseEntity<List<PerformanceRoundCastResponse>> searchRoundCasts(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long roundId);

    @Operation(summary = "회차별 캐스팅 배정")
    @PostMapping("/round-casts")
    ResponseEntity<PerformanceIdentifierResponse> assignRoundCast(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @Valid @RequestBody PerformanceRoundCastAssignRequest request);

    @Operation(summary = "회차별 캐스팅 변경")
    @PutMapping("/round-casts/{roundCastId}")
    ResponseEntity<Void> changeRoundCast(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long roundCastId,
            @Valid @RequestBody PerformanceRoundCastChangeRequest request);

    @Operation(summary = "회차별 캐스팅 해제")
    @DeleteMapping("/round-casts/{roundCastId}")
    ResponseEntity<Void> removeRoundCast(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long roundCastId,
            @RequestParam(required = false) String reason);
}
