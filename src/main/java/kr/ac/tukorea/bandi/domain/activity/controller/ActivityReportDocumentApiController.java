package kr.ac.tukorea.bandi.domain.activity.controller;

import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityReportDocumentRequest;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityReportParticipantCandidateResponse;
import kr.ac.tukorea.bandi.domain.activity.document.ActivityReportPhotoUploadParam;
import kr.ac.tukorea.bandi.domain.activity.service.ActivityReportDocumentService;
import kr.ac.tukorea.bandi.global.swagger.ActivityReportDocumentApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ActivityReportDocumentApiController
        implements ActivityReportDocumentApiDocs {

    private static final MediaType HWPX_MEDIA_TYPE =
            MediaType.parseMediaType("application/hwp+zip");
    private static final DateTimeFormatter FILE_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
    public ResponseEntity<byte[]> create(ActivityReportDocumentRequest request,
                                         MultipartFile photo) {
        String filename = request.activityAt().format(FILE_DATE_FORMATTER)
                + "_반디_동아리_활동_내역서.hwpx";
        return download(activityReportDocumentService.create(request.toModel(),
                        new ActivityReportPhotoUploadParam(photo.getSize(),
                                photo.getContentType(), photo::getInputStream)),
                filename);
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
