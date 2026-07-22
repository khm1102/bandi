package kr.ac.tukorea.bandi.global.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ac.tukorea.bandi.domain.resource.dto.request.ResourceCreateRequest;
import kr.ac.tukorea.bandi.domain.resource.dto.request.ResourceManageFilter;
import kr.ac.tukorea.bandi.domain.resource.dto.request.ResourceRevisionRequest;
import kr.ac.tukorea.bandi.domain.resource.dto.request.ResourceUpdateRequest;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceCreatedResponse;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceManageDetailResponse;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceManageSummaryResponse;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceRevisionCreatedResponse;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("/api/resource-management")
@Tag(name = ApiTag.RESOURCE, description = "전체·팀 자료와 파일 리비전 관리 API")
@SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SCHEME)
public interface ResourceManagementApiDocs {

    @Operation(summary = "관리 가능한 자료 목록 조회")
    @GetMapping
    ResponseEntity<List<ResourceManageSummaryResponse>> search(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryCode,
            @ParameterObject @ModelAttribute ResourceManageFilter filter,
            @RequestParam(required = false) Long teamId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize);

    @Operation(summary = "관리용 자료 상세와 전체 리비전 조회")
    @GetMapping("/{resourceId}")
    ResponseEntity<ResourceManageDetailResponse> lookup(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long resourceId);

    @Operation(summary = "자료 초안 등록")
    @PostMapping
    ResponseEntity<ResourceCreatedResponse> create(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @Valid @RequestBody ResourceCreateRequest request);

    @Operation(summary = "자료 메타데이터 수정")
    @PutMapping("/{resourceId}")
    ResponseEntity<Void> update(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long resourceId,
            @Valid @RequestBody ResourceUpdateRequest request);

    @Operation(summary = "자료 파일 리비전 교체")
    @PostMapping("/{resourceId}/revisions")
    ResponseEntity<ResourceRevisionCreatedResponse> replaceFiles(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long resourceId,
            @Valid @RequestBody ResourceRevisionRequest request);

    @Operation(summary = "자료 게시")
    @PostMapping("/{resourceId}/publish")
    ResponseEntity<Void> publish(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long resourceId);

    @Operation(summary = "자료 보관")
    @PostMapping("/{resourceId}/archive")
    ResponseEntity<Void> archive(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long resourceId);
}
