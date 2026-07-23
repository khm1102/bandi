package kr.ac.tukorea.bandi.global.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityReportDocumentRequest;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityReportDocumentDraftResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityReportDocumentSavedResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityReportParticipantCandidateResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivitySubmissionResponse;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/api/activity-report-documents")
@Tag(name = ApiTag.ACTIVITY, description = "동아리 활동 내역서 HWPX 생성 API")
@SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SCHEME)
public interface ActivityReportDocumentApiDocs {

    @Operation(summary = "빈 활동 내역서 HWPX 다운로드")
    @GetMapping("/blank")
    ResponseEntity<byte[]> downloadBlank();

    @Operation(summary = "활동 내역서 참여자 후보 검색")
    @GetMapping("/participants")
    ResponseEntity<List<ActivityReportParticipantCandidateResponse>> searchParticipants(
            @RequestParam String q);

    @Operation(summary = "활동 내역서 HWPX와 입력값을 임시 저장")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ActivityReportDocumentSavedResponse> create(
            @LoginMember Long actorMemberId,
            @Valid @RequestPart("request") ActivityReportDocumentRequest request,
            @RequestPart("photo") MultipartFile photo);

    @Operation(summary = "저장된 활동 내역서 입력값 조회")
    @GetMapping("/{activityRecordId}")
    ResponseEntity<ActivityReportDocumentDraftResponse> lookup(
            @LoginMember Long actorMemberId,
            @PathVariable Long activityRecordId);

    @Operation(summary = "저장된 활동 내역서와 HWPX 갱신")
    @PutMapping(value = "/{activityRecordId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ActivityReportDocumentSavedResponse> update(
            @LoginMember Long actorMemberId,
            @PathVariable Long activityRecordId,
            @Valid @RequestPart("request") ActivityReportDocumentRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo);

    @Operation(summary = "저장된 활동 내역서 검수 요청")
    @PostMapping("/{activityRecordId}/submit")
    ResponseEntity<ActivitySubmissionResponse> submit(
            @LoginMember Long actorMemberId,
            @PathVariable Long activityRecordId);
}
