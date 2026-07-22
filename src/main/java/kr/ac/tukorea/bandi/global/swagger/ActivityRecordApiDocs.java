package kr.ac.tukorea.bandi.global.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityRecordDetailResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityRecordSummaryResponse;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@RequestMapping("/api/activity-records")
@Tag(name = ApiTag.ACTIVITY, description = "승인된 활동 기록 열람 API")
@SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SCHEME)
public interface ActivityRecordApiDocs {

    @Operation(summary = "승인된 활동 기록 목록 조회")
    @GetMapping
    ResponseEntity<List<ActivityRecordSummaryResponse>> search(
            @Parameter(hidden = true) @LoginMember Long memberId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize);

    @Operation(summary = "승인된 활동 기록 상세 조회")
    @GetMapping("/{activityRecordId}")
    ResponseEntity<ActivityRecordDetailResponse> lookup(
            @Parameter(hidden = true) @LoginMember Long memberId,
            @PathVariable Long activityRecordId);

    @Operation(summary = "승인된 증빙 이미지 다운로드")
    @GetMapping("/{activityRecordId}/files/{storedFileId}/download")
    ResponseEntity<Resource> download(
            @Parameter(hidden = true) @LoginMember Long memberId,
            @PathVariable Long activityRecordId,
            @PathVariable Long storedFileId);
}
