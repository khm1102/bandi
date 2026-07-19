package kr.ac.tukorea.bandi.domain.notice.controller;

import kr.ac.tukorea.bandi.domain.notice.dto.request.PublicNoticeSearchParam;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeDetailResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeSummaryResponse;
import kr.ac.tukorea.bandi.domain.notice.service.PublicNoticeService;
import kr.ac.tukorea.bandi.global.swagger.PublicNoticeApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
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
    public ResponseEntity<Void> download(Long publicNoticeId, Long storedFileId) {
        String url = publicNoticeService.createAttachmentDownloadUrl(
                publicNoticeId, storedFileId);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(url))
                .build();
    }
}
