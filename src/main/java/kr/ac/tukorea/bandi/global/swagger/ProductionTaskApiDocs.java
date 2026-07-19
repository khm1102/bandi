package kr.ac.tukorea.bandi.global.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ac.tukorea.bandi.domain.production.dto.request.ProductionTaskCreateRequest;
import kr.ac.tukorea.bandi.domain.production.dto.request.ProductionTaskFilter;
import kr.ac.tukorea.bandi.domain.production.dto.request.ProductionTaskStatusRequest;
import kr.ac.tukorea.bandi.domain.production.dto.request.ProductionTaskUpdateRequest;
import kr.ac.tukorea.bandi.domain.production.dto.response.ProductionProgressResponse;
import kr.ac.tukorea.bandi.domain.production.dto.response.ProductionTaskCreatedResponse;
import kr.ac.tukorea.bandi.domain.production.dto.response.ProductionTaskHistoryResponse;
import kr.ac.tukorea.bandi.domain.production.dto.response.ProductionTaskResponse;
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

@RequestMapping("/api/production-tasks")
@Tag(name = ApiTag.PRODUCTION, description = "팀별 공연 제작 업무·진척 관리 API")
@SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SCHEME)
public interface ProductionTaskApiDocs {
    @Operation(summary = "제작 업무 검색")
    @GetMapping
    ResponseEntity<List<ProductionTaskResponse>> search(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @RequestParam Long performanceProjectId,
            @RequestParam(required = false) Long teamId,
            @ParameterObject @ModelAttribute ProductionTaskFilter filter,
            @RequestParam(defaultValue = "false") boolean overdueOnly,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit);

    @Operation(summary = "제작 업무 등록")
    @PostMapping
    ResponseEntity<ProductionTaskCreatedResponse> create(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @Valid @RequestBody ProductionTaskCreateRequest request);

    @Operation(summary = "제작 업무 수정")
    @PutMapping("/{taskId}")
    ResponseEntity<Void> update(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long taskId,
            @Valid @RequestBody ProductionTaskUpdateRequest request);

    @Operation(summary = "제작 업무 상태 변경")
    @PatchMapping("/{taskId}/status")
    ResponseEntity<Void> changeStatus(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long taskId,
            @Valid @RequestBody ProductionTaskStatusRequest request);

    @Operation(summary = "제작 업무 삭제")
    @DeleteMapping("/{taskId}")
    ResponseEntity<Void> delete(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long taskId);

    @Operation(summary = "프로젝트 전체 제작 진척 조회")
    @GetMapping("/projects/{projectId}/progress")
    ResponseEntity<ProductionProgressResponse> lookupProgress(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long projectId);

    @Operation(summary = "프로젝트 팀별 제작 진척 조회")
    @GetMapping("/projects/{projectId}/team-progress")
    ResponseEntity<List<ProductionProgressResponse>> searchTeamProgress(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long projectId);

    @Operation(summary = "제작 업무 상태 변경 이력 조회")
    @GetMapping("/{taskId}/histories")
    ResponseEntity<List<ProductionTaskHistoryResponse>> searchHistories(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long taskId);
}
