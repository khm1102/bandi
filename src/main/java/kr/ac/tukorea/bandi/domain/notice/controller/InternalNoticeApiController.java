package kr.ac.tukorea.bandi.domain.notice.controller;

import kr.ac.tukorea.bandi.domain.notice.dto.request.InternalNoticeSearchParam;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeDetailResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeSummaryResponse;
import kr.ac.tukorea.bandi.domain.notice.service.InternalNoticeService;
import kr.ac.tukorea.bandi.domain.file.dto.response.FileDownload;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import kr.ac.tukorea.bandi.global.swagger.InternalNoticeApiDocs;
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
public class InternalNoticeApiController implements InternalNoticeApiDocs {

    private final InternalNoticeService internalNoticeService;

    @Override
    public ResponseEntity<List<InternalNoticeSummaryResponse>> search(
            @LoginMember Long memberId, String keyword, int page, int pageSize) {
        return ResponseEntity.ok(internalNoticeService.searchReadable(memberId,
                new InternalNoticeSearchParam(keyword, page, pageSize)));
    }

    @Override
    public ResponseEntity<InternalNoticeDetailResponse> lookup(
            @LoginMember Long memberId, Long internalNoticeId) {
        return ResponseEntity.ok(internalNoticeService.lookupReadable(memberId,
                internalNoticeId));
    }

    @Override
    public ResponseEntity<Resource> download(@LoginMember Long memberId,
                                             Long internalNoticeId,
                                             Long storedFileId) {
        return attachment(internalNoticeService.openAttachmentDownload(memberId,
                internalNoticeId, storedFileId));
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
