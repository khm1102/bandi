package kr.ac.tukorea.bandi.global.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityFileAddRequest;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityFileReplaceRequest;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityManageFilter;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityRecordCreateRequest;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityRecordUpdateRequest;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityRevisionRequest;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivitySubmitRequest;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityRecordCreatedResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityRecordManageDetailResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityRecordSummaryResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivitySubmissionResponse;
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

@RequestMapping("/api/activity-management")
@Tag(name = ApiTag.ACTIVITY, description = "활동 기록 작성·제출·검토 API")
@SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SCHEME)
public interface ActivityManagementApiDocs {

    @Operation(summary = "관리 가능한 활동 기록 목록 조회")
    @GetMapping
    ResponseEntity<List<ActivityRecordSummaryResponse>> search(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @RequestParam(required = false) Long teamId,
            @ParameterObject @ModelAttribute ActivityManageFilter filter,
            @RequestParam(required = false) Long createdByMemberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize);

    @Operation(summary = "관리용 활동 기록 상세·리비전·검토 이력 조회")
    @GetMapping("/{activityRecordId}")
    ResponseEntity<ActivityRecordManageDetailResponse> lookup(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long activityRecordId);

    @Operation(summary = "활동 기록 초안 등록")
    @PostMapping
    ResponseEntity<ActivityRecordCreatedResponse> create(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @Valid @RequestBody ActivityRecordCreateRequest request);

    @Operation(summary = "활동 기록 본문 수정")
    @PutMapping("/{activityRecordId}")
    ResponseEntity<Void> update(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long activityRecordId,
            @Valid @RequestBody ActivityRecordUpdateRequest request);

    @Operation(summary = "증빙 이미지 추가")
    @PostMapping("/{activityRecordId}/files")
    ResponseEntity<Void> addFile(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long activityRecordId,
            @Valid @RequestBody ActivityFileAddRequest request);

    @Operation(summary = "증빙 이미지 교체")
    @PutMapping("/files/{activityRecordFileId}")
    ResponseEntity<Void> replaceFile(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long activityRecordFileId,
            @Valid @RequestBody ActivityFileReplaceRequest request);

    @Operation(summary = "활동 기록 제출")
    @PostMapping("/{activityRecordId}/submit")
    ResponseEntity<ActivitySubmissionResponse> submit(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long activityRecordId,
            @Valid @RequestBody ActivitySubmitRequest request);

    @Operation(summary = "활동 기록 승인")
    @PostMapping("/{activityRecordId}/approve")
    ResponseEntity<Void> approve(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long activityRecordId);

    @Operation(summary = "활동 기록 보완 요청")
    @PostMapping("/{activityRecordId}/revision-request")
    ResponseEntity<Void> requestRevision(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long activityRecordId,
            @Valid @RequestBody ActivityRevisionRequest request);

    @Operation(summary = "활동 기록 보관")
    @PostMapping("/{activityRecordId}/archive")
    ResponseEntity<Void> archive(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long activityRecordId);
}
