package kr.ac.tukorea.bandi.global.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ac.tukorea.bandi.domain.checklist.dto.request.ChecklistCompletionRequest;
import kr.ac.tukorea.bandi.domain.checklist.dto.request.ChecklistItemCreateRequest;
import kr.ac.tukorea.bandi.domain.checklist.dto.request.ChecklistItemUpdateRequest;
import kr.ac.tukorea.bandi.domain.checklist.dto.request.ChecklistSearchFilter;
import kr.ac.tukorea.bandi.domain.checklist.dto.response.ChecklistItemCreatedResponse;
import kr.ac.tukorea.bandi.domain.checklist.dto.response.ChecklistItemHistoryResponse;
import kr.ac.tukorea.bandi.domain.checklist.dto.response.ChecklistItemResponse;
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

@RequestMapping("/api/checklist-items")
@Tag(name = ApiTag.CHECKLIST, description = "공연 프로젝트·회차 체크리스트 API")
@SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SCHEME)
public interface ChecklistApiDocs {
    @Operation(summary = "체크리스트 검색")
    @GetMapping
    ResponseEntity<List<ChecklistItemResponse>> search(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @RequestParam Long performanceProjectId,
            @RequestParam(required = false) Long performanceRoundId,
            @RequestParam(required = false) Long teamId,
            @ParameterObject @ModelAttribute ChecklistSearchFilter filter);

    @Operation(summary = "체크리스트 항목 등록")
    @PostMapping
    ResponseEntity<ChecklistItemCreatedResponse> create(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @Valid @RequestBody ChecklistItemCreateRequest request);

    @Operation(summary = "체크리스트 항목 수정")
    @PutMapping("/{itemId}")
    ResponseEntity<Void> update(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long itemId,
            @Valid @RequestBody ChecklistItemUpdateRequest request);

    @Operation(summary = "체크리스트 완료 상태 변경")
    @PatchMapping("/{itemId}/completion")
    ResponseEntity<Void> changeCompletion(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long itemId,
            @Valid @RequestBody ChecklistCompletionRequest request);

    @Operation(summary = "체크리스트 항목 삭제")
    @DeleteMapping("/{itemId}")
    ResponseEntity<Void> delete(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long itemId);

    @Operation(summary = "체크리스트 완료 변경 이력 조회")
    @GetMapping("/{itemId}/histories")
    ResponseEntity<List<ChecklistItemHistoryResponse>> searchHistories(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long itemId);
}
