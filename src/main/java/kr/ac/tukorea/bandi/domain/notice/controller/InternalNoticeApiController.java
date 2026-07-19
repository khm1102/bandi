package kr.ac.tukorea.bandi.domain.notice.controller;

import kr.ac.tukorea.bandi.domain.notice.dto.request.InternalNoticeSearchParam;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeDetailResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeSummaryResponse;
import kr.ac.tukorea.bandi.domain.notice.service.InternalNoticeService;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import kr.ac.tukorea.bandi.global.swagger.InternalNoticeApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
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
    public ResponseEntity<Void> download(@LoginMember Long memberId,
                                         Long internalNoticeId,
                                         Long storedFileId) {
        String url = internalNoticeService.createAttachmentDownloadUrl(memberId,
                internalNoticeId, storedFileId);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(url))
                .build();
    }
}
