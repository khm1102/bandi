package kr.ac.tukorea.bandi.domain.notice.controller;

import kr.ac.tukorea.bandi.domain.notice.dto.request.PublicNoticeSearchParam;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeDetailResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeSummaryResponse;
import kr.ac.tukorea.bandi.domain.notice.service.PublicNoticeService;
import kr.ac.tukorea.bandi.domain.file.dto.response.FileDownload;
import kr.ac.tukorea.bandi.global.swagger.PublicNoticeApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PublicNoticeApiController implements PublicNoticeApiDocs {

    private final PublicNoticeService publicNoticeService;

    @Override
    public ResponseEntity<List<PublicNoticeSummaryResponse>> search(
            String keyword, int page, int pageSize) {
        return ResponseEntity.ok(publicNoticeService.searchPublic(
                new PublicNoticeSearchParam(keyword, page, pageSize)));
    }

    @Override
    public ResponseEntity<PublicNoticeDetailResponse> lookup(Long publicNoticeId) {
        return ResponseEntity.ok(publicNoticeService.lookupPublic(publicNoticeId));
    }

    @Override
    public ResponseEntity<Resource> download(Long publicNoticeId, Long storedFileId) {
        return attachment(publicNoticeService.openAttachmentDownload(publicNoticeId, storedFileId));
    }

    private ResponseEntity<Resource> attachment(FileDownload file) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .contentLength(file.sizeBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(file.originalName(), StandardCharsets.UTF_8).build().toString())
                .body(new InputStreamResource(file.openStream()));
    }
}
