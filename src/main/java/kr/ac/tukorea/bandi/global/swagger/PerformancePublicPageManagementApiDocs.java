package kr.ac.tukorea.bandi.global.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformancePublicPageRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceViewingGuideRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PublicPageStatusRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceIdentifierResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformancePublicPageResponse;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/performance-page-management")
@Tag(name = ApiTag.PERFORMANCE, description = "외부 공연 페이지·관람 안내 관리 API")
@SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SCHEME)
public interface PerformancePublicPageManagementApiDocs {
    @Operation(summary = "공연 페이지 관리 목록 조회")
    @GetMapping
    ResponseEntity<List<PerformancePublicPageResponse>> search(
            @Parameter(hidden = true) @LoginMember Long actorMemberId);

    @Operation(summary = "공연 페이지 초안 등록")
    @PostMapping
    ResponseEntity<PerformanceIdentifierResponse> create(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @Valid @RequestBody PerformancePublicPageRequest request);

    @Operation(summary = "공연 페이지 수정")
    @PutMapping("/{pageId}")
    ResponseEntity<Void> update(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long pageId,
            @Valid @RequestBody PerformancePublicPageRequest request);

    @Operation(summary = "공연 페이지 게시 상태 변경")
    @PatchMapping("/{pageId}/status")
    ResponseEntity<Void> changeStatus(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long pageId,
            @Valid @RequestBody PublicPageStatusRequest request);

    @Operation(summary = "공연 관람 안내 저장")
    @PutMapping("/viewing-guide")
    ResponseEntity<Void> saveViewingGuide(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @Valid @RequestBody PerformanceViewingGuideRequest request);
}
