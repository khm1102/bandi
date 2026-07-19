package kr.ac.tukorea.bandi.global.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceProjectFilter;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceProjectRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceProjectStatusRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceRoundRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceRoundStatusRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.request.RoundAccessibilityRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceIdentifierResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceProjectResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceRoundAccessibilityResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceRoundResponse;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("/api/performance-management")
@Tag(name = ApiTag.PERFORMANCE, description = "공연 프로젝트·회차·접근성 관리 API")
@SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SCHEME)
public interface PerformanceCoreApiDocs {

    @Operation(summary = "공연 프로젝트 목록 조회")
    @GetMapping("/projects")
    ResponseEntity<List<PerformanceProjectResponse>> searchProjects(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @RequestParam(required = false) Short academicYear,
            @RequestParam(required = false) String termCode,
            @ParameterObject @ModelAttribute PerformanceProjectFilter filter,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit);

    @Operation(summary = "공연 프로젝트 등록")
    @PostMapping("/projects")
    ResponseEntity<PerformanceIdentifierResponse> createProject(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @Valid @RequestBody PerformanceProjectRequest request);

    @Operation(summary = "공연 프로젝트 수정")
    @PutMapping("/projects/{projectId}")
    ResponseEntity<Void> updateProject(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long projectId,
            @Valid @RequestBody PerformanceProjectRequest request);

    @Operation(summary = "공연 프로젝트 상태 변경")
    @PatchMapping("/projects/{projectId}/status")
    ResponseEntity<Void> changeProjectStatus(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long projectId,
            @Valid @RequestBody PerformanceProjectStatusRequest request);

    @Operation(summary = "공연 회차 목록 조회")
    @GetMapping("/projects/{projectId}/rounds")
    ResponseEntity<List<PerformanceRoundResponse>> searchRounds(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long projectId);

    @Operation(summary = "공연 회차 등록")
    @PostMapping("/rounds")
    ResponseEntity<PerformanceIdentifierResponse> createRound(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @Valid @RequestBody PerformanceRoundRequest request);

    @Operation(summary = "공연 회차 수정")
    @PutMapping("/rounds/{roundId}")
    ResponseEntity<Void> updateRound(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long roundId,
            @Valid @RequestBody PerformanceRoundRequest request);

    @Operation(summary = "공연 회차 상태 변경")
    @PatchMapping("/rounds/{roundId}/status")
    ResponseEntity<Void> changeRoundStatus(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long roundId,
            @Valid @RequestBody PerformanceRoundStatusRequest request);

    @Operation(summary = "회차 접근성 지원 목록 조회")
    @GetMapping("/rounds/{roundId}/accessibilities")
    ResponseEntity<List<PerformanceRoundAccessibilityResponse>> searchAccessibilities(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long roundId);

    @Operation(summary = "회차 접근성 지원 등록")
    @PostMapping("/rounds/{roundId}/accessibilities")
    ResponseEntity<PerformanceIdentifierResponse> createAccessibility(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long roundId,
            @Valid @RequestBody RoundAccessibilityRequest request);

    @Operation(summary = "회차 접근성 지원 수정")
    @PutMapping("/rounds/{roundId}/accessibilities/{accessibilityId}")
    ResponseEntity<Void> updateAccessibility(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long roundId,
            @PathVariable Long accessibilityId,
            @Valid @RequestBody RoundAccessibilityRequest request);

    @Operation(summary = "회차 접근성 지원 삭제")
    @DeleteMapping("/accessibilities/{accessibilityId}")
    ResponseEntity<Void> removeAccessibility(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long accessibilityId);
}
