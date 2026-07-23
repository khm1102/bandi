package kr.ac.tukorea.bandi.domain.activity.controller;

import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityReportDocumentRequest;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityReportDocumentDraftResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityReportDocumentSavedResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityReportParticipantCandidateResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivitySubmissionResponse;
import kr.ac.tukorea.bandi.domain.activity.document.ActivityReportPhotoUploadParam;
import kr.ac.tukorea.bandi.domain.activity.service.ActivityReportDocumentService;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import kr.ac.tukorea.bandi.global.swagger.ActivityReportDocumentApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ActivityReportDocumentApiController
        implements ActivityReportDocumentApiDocs {

    private static final MediaType HWPX_MEDIA_TYPE =
            MediaType.parseMediaType("application/hwp+zip");
    private final ActivityReportDocumentService activityReportDocumentService;

    @Override
    public ResponseEntity<byte[]> downloadBlank() {
        return download(activityReportDocumentService.createBlank(),
                "반디_동아리_활동_내역서_빈_양식.hwpx");
    }

    @Override
    public ResponseEntity<List<ActivityReportParticipantCandidateResponse>>
    searchParticipants(String q) {
        return ResponseEntity.ok(activityReportDocumentService.searchParticipants(q));
    }

    @Override
    public ResponseEntity<ActivityReportDocumentSavedResponse> create(
            @LoginMember Long actorMemberId,
            ActivityReportDocumentRequest request, MultipartFile photo) {
        ActivityReportDocumentSavedResponse saved =
                activityReportDocumentService.saveDraft(actorMemberId,
                        request.toModel(), new ActivityReportPhotoUploadParam(
                                photo.getSize(), photo.getContentType(),
                                photo::getInputStream));
        return ResponseEntity.created(URI.create(
                "/api/activity-report-documents/" + saved.activityRecordId()))
                .body(saved);
    }

    @Override
    public ResponseEntity<ActivityReportDocumentDraftResponse> lookup(
            @LoginMember Long actorMemberId, Long activityRecordId) {
        return ResponseEntity.ok(activityReportDocumentService.lookupDraft(
                actorMemberId, activityRecordId));
    }

    @Override
    public ResponseEntity<ActivityReportDocumentSavedResponse> update(
            @LoginMember Long actorMemberId, Long activityRecordId,
            ActivityReportDocumentRequest request, MultipartFile photo) {
        ActivityReportPhotoUploadParam upload = photo == null ? null
                : new ActivityReportPhotoUploadParam(photo.getSize(),
                        photo.getContentType(), photo::getInputStream);
        return ResponseEntity.ok(activityReportDocumentService.updateDraft(
                actorMemberId, activityRecordId, request.toModel(), upload));
    }

    @Override
    public ResponseEntity<ActivitySubmissionResponse> submit(
            @LoginMember Long actorMemberId, Long activityRecordId) {
        int revisionNo = activityReportDocumentService.submit(actorMemberId,
                activityRecordId);
        return ResponseEntity.ok(new ActivitySubmissionResponse(revisionNo));
    }

    private ResponseEntity<byte[]> download(byte[] body, String filename) {
        return ResponseEntity.ok()
                .contentType(HWPX_MEDIA_TYPE)
                .contentLength(body.length)
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(filename, StandardCharsets.UTF_8).build().toString())
                .body(body);
    }
}
