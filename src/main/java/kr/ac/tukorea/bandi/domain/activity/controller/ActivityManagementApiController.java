package kr.ac.tukorea.bandi.domain.activity.controller;

import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityFileAddRequest;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityFileReplaceRequest;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityManageFilter;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityManageSearchParam;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityRecordCreateRequest;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityRecordUpdateRequest;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityRevisionRequest;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivitySubmitRequest;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityRecordCreatedResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityRecordManageDetailResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityRecordSummaryResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivitySubmissionResponse;
import kr.ac.tukorea.bandi.domain.activity.service.ActivityRecordService;
import kr.ac.tukorea.bandi.global.response.FileDownloadResponse;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import kr.ac.tukorea.bandi.global.swagger.ActivityManagementApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ActivityManagementApiController implements ActivityManagementApiDocs {

    private final ActivityRecordService activityRecordService;

    @Override
    public ResponseEntity<List<ActivityRecordSummaryResponse>> search(
            @LoginMember Long actorMemberId, Long teamId,
            ActivityManageFilter filter, Long createdByMemberId,
            int page, int pageSize) {
        return ResponseEntity.ok(activityRecordService.searchManageable(actorMemberId,
                new ActivityManageSearchParam(teamId, filter.status(), createdByMemberId,
                        page, pageSize)));
    }

    @Override
    public ResponseEntity<ActivityRecordManageDetailResponse> lookup(
            @LoginMember Long actorMemberId, Long activityRecordId) {
        return ResponseEntity.ok(activityRecordService.lookupManageable(actorMemberId,
                activityRecordId));
    }

    @Override
    public ResponseEntity<Resource> download(@LoginMember Long actorMemberId,
                                             Long activityRecordId,
                                             Long storedFileId) {
        return inline(activityRecordService.openManageableDownload(
                actorMemberId, activityRecordId, storedFileId));
    }

    @Override
    public ResponseEntity<ActivityRecordCreatedResponse> create(
            @LoginMember Long actorMemberId, ActivityRecordCreateRequest request) {
        Long id = activityRecordService.createDraft(actorMemberId, request.toParam());
        return ResponseEntity.created(URI.create("/api/activity-management/" + id))
                .body(new ActivityRecordCreatedResponse(id));
    }

    @Override
    public ResponseEntity<Void> update(@LoginMember Long actorMemberId,
                                       Long activityRecordId,
                                       ActivityRecordUpdateRequest request) {
        activityRecordService.update(actorMemberId,
                request.toParam(activityRecordId));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> addFile(@LoginMember Long actorMemberId,
                                        Long activityRecordId,
                                        ActivityFileAddRequest request) {
        activityRecordService.addFile(actorMemberId,
                request.toParam(activityRecordId));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> replaceFile(@LoginMember Long actorMemberId,
                                            Long activityRecordFileId,
                                            ActivityFileReplaceRequest request) {
        activityRecordService.replaceFile(actorMemberId,
                request.toParam(activityRecordFileId));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ActivitySubmissionResponse> submit(
            @LoginMember Long actorMemberId, Long activityRecordId,
            ActivitySubmitRequest request) {
        int revisionNo = activityRecordService.submit(actorMemberId,
                activityRecordId, request.changeReason());
        return ResponseEntity.ok(new ActivitySubmissionResponse(revisionNo));
    }

    @Override
    public ResponseEntity<Void> approve(@LoginMember Long actorMemberId,
                                        Long activityRecordId) {
        activityRecordService.approve(actorMemberId, activityRecordId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> requestRevision(@LoginMember Long actorMemberId,
                                                Long activityRecordId,
                                                ActivityRevisionRequest request) {
        activityRecordService.requestRevision(actorMemberId, activityRecordId,
                request.comment());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> archive(@LoginMember Long actorMemberId,
                                        Long activityRecordId) {
        activityRecordService.archive(actorMemberId, activityRecordId);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<Resource> inline(FileDownloadResponse file) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .contentLength(file.sizeBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(file.originalName(), StandardCharsets.UTF_8).build().toString())
                .body(file.resource());
    }
}
