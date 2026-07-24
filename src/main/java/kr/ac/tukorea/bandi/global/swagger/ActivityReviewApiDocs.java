package kr.ac.tukorea.bandi.global.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityFinalApprovalRequest;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityRecordListSearchParam;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityRevisionRequest;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityRecordManageDetailResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityRecordSummaryResponse;
import kr.ac.tukorea.bandi.global.response.PageResponse;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/activity-reviews")
@Tag(name = ApiTag.ACTIVITY, description = "활동 기록 팀 검수·최종 승인 API")
@SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SCHEME)
public interface ActivityReviewApiDocs {

    @Operation(summary = "검수 대상 활동 기록 목록")
    @GetMapping
    ResponseEntity<PageResponse<ActivityRecordSummaryResponse>> search(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @ParameterObject ActivityRecordListSearchParam param);

    @Operation(summary = "검수용 활동 기록 상세")
    @GetMapping("/{activityRecordId}")
    ResponseEntity<ActivityRecordManageDetailResponse> lookup(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long activityRecordId);

    @Operation(summary = "검수용 첨부 파일 다운로드")
    @GetMapping("/{activityRecordId}/files/{storedFileId}/download")
    ResponseEntity<Resource> download(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long activityRecordId,
            @PathVariable Long storedFileId);

    @Operation(summary = "팀장 1차 승인")
    @PostMapping("/{activityRecordId}/team-approve")
    ResponseEntity<Void> teamApprove(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long activityRecordId);

    @Operation(summary = "관리자 최종 승인 또는 긴급 승인")
    @PostMapping("/{activityRecordId}/final-approve")
    ResponseEntity<Void> finalApprove(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long activityRecordId,
            @Valid @RequestBody ActivityFinalApprovalRequest request);

    @Operation(summary = "검수 보완 요청")
    @PostMapping("/{activityRecordId}/revision-request")
    ResponseEntity<Void> requestRevision(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long activityRecordId,
            @Valid @RequestBody ActivityRevisionRequest request);

    @Operation(summary = "최종 승인 활동 기록 보관")
    @PostMapping("/{activityRecordId}/archive")
    ResponseEntity<Void> archive(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long activityRecordId);

    @Operation(summary = "관리자 검수 목록 CSV 내보내기")
    @GetMapping("/export")
    ResponseEntity<byte[]> export(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @ParameterObject ActivityRecordListSearchParam param);
}
