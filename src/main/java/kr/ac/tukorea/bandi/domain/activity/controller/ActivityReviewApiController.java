package kr.ac.tukorea.bandi.domain.activity.controller;

import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityFinalApprovalRequest;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityRecordListSearchParam;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityRevisionRequest;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityRecordManageDetailResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityRecordSummaryResponse;
import kr.ac.tukorea.bandi.domain.activity.service.ActivityRecordService;
import kr.ac.tukorea.bandi.global.response.FileDownloadResponse;
import kr.ac.tukorea.bandi.global.response.PageResponse;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import kr.ac.tukorea.bandi.global.swagger.ActivityReviewApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
public class ActivityReviewApiController implements ActivityReviewApiDocs {

    private final ActivityRecordService activityRecordService;

    @Override
    public ResponseEntity<PageResponse<ActivityRecordSummaryResponse>> search(
            Long actorMemberId, ActivityRecordListSearchParam param) {
        return ResponseEntity.ok(activityRecordService.searchReview(actorMemberId, param));
    }

    @Override
    public ResponseEntity<ActivityRecordManageDetailResponse> lookup(
            Long actorMemberId, Long activityRecordId) {
        return ResponseEntity.ok(activityRecordService.lookupReviewable(
                actorMemberId, activityRecordId));
    }

    @Override
    public ResponseEntity<Resource> download(Long actorMemberId, Long activityRecordId,
                                             Long storedFileId) {
        FileDownloadResponse file = activityRecordService.openReviewDownload(
                actorMemberId, activityRecordId, storedFileId);
        ContentDisposition disposition = "application/hwp+zip".equals(file.contentType())
                ? ContentDisposition.attachment().filename(file.originalName(),
                StandardCharsets.UTF_8).build()
                : ContentDisposition.inline().filename(file.originalName(),
                StandardCharsets.UTF_8).build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .contentLength(file.sizeBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(file.resource());
    }

    @Override
    public ResponseEntity<Void> teamApprove(Long actorMemberId, Long activityRecordId) {
        activityRecordService.teamApprove(actorMemberId, activityRecordId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> finalApprove(Long actorMemberId, Long activityRecordId,
                                             ActivityFinalApprovalRequest request) {
        activityRecordService.finalApprove(actorMemberId, activityRecordId,
                request.emergencyReason());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> requestRevision(Long actorMemberId, Long activityRecordId,
                                                ActivityRevisionRequest request) {
        activityRecordService.requestRevision(actorMemberId, activityRecordId,
                request.comment());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> archive(Long actorMemberId, Long activityRecordId) {
        activityRecordService.archive(actorMemberId, activityRecordId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<byte[]> export(Long actorMemberId,
                                         ActivityRecordListSearchParam param) {
        byte[] content = activityRecordService.exportReviewCsv(actorMemberId, param);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("활동-기록-검수.csv",
                                StandardCharsets.UTF_8).build().toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(content);
    }
}
