package kr.ac.tukorea.bandi.domain.activity.controller;

import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityRecordSearchParam;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityRecordDetailResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityRecordSummaryResponse;
import kr.ac.tukorea.bandi.domain.activity.service.ActivityRecordService;
import kr.ac.tukorea.bandi.global.response.FileDownloadResponse;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import kr.ac.tukorea.bandi.global.swagger.ActivityRecordApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ActivityRecordApiController implements ActivityRecordApiDocs {

    private final ActivityRecordService activityRecordService;

    @Override
    public ResponseEntity<List<ActivityRecordSummaryResponse>> search(
            @LoginMember Long memberId, Long teamId, LocalDate dateFrom,
            LocalDate dateTo, int page, int pageSize) {
        return ResponseEntity.ok(activityRecordService.searchApproved(memberId,
                new ActivityRecordSearchParam(teamId, dateFrom, dateTo,
                        page, pageSize)));
    }

    @Override
    public ResponseEntity<ActivityRecordDetailResponse> lookup(
            @LoginMember Long memberId, Long activityRecordId) {
        return ResponseEntity.ok(activityRecordService.lookupApproved(memberId,
                activityRecordId));
    }

    @Override
    public ResponseEntity<Resource> download(@LoginMember Long memberId,
                                             Long activityRecordId,
                                             Long storedFileId) {
        return inline(activityRecordService.openApprovedDownload(memberId,
                activityRecordId, storedFileId));
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
