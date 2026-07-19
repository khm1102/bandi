package kr.ac.tukorea.bandi.domain.notice.controller;

import kr.ac.tukorea.bandi.domain.notice.dto.request.NoticePublishRequest;
import kr.ac.tukorea.bandi.domain.notice.dto.request.PublicNoticeAdminSearchParam;
import kr.ac.tukorea.bandi.domain.notice.dto.request.PublicNoticeWriteRequest;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeAdminDetailResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeAdminSummaryResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeCreatedResponse;
import kr.ac.tukorea.bandi.domain.notice.model.PublicNoticeStatus;
import kr.ac.tukorea.bandi.domain.notice.service.PublicNoticeService;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import kr.ac.tukorea.bandi.global.swagger.PublicNoticeManagementApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PublicNoticeManagementApiController
        implements PublicNoticeManagementApiDocs {

    private final PublicNoticeService publicNoticeService;

    @Override
    public ResponseEntity<List<PublicNoticeAdminSummaryResponse>> search(
            @LoginMember Long actorMemberId, String keyword,
            PublicNoticeStatus status, int page, int pageSize) {
        return ResponseEntity.ok(publicNoticeService.searchAdmin(actorMemberId,
                new PublicNoticeAdminSearchParam(keyword, status, page, pageSize)));
    }

    @Override
    public ResponseEntity<PublicNoticeAdminDetailResponse> lookup(
            @LoginMember Long actorMemberId, Long publicNoticeId) {
        return ResponseEntity.ok(publicNoticeService.lookupAdmin(actorMemberId,
                publicNoticeId));
    }

    @Override
    public ResponseEntity<PublicNoticeCreatedResponse> create(
            @LoginMember Long actorMemberId, PublicNoticeWriteRequest request) {
        Long id = publicNoticeService.createDraft(actorMemberId, request.toParam());
        return ResponseEntity.created(URI.create("/api/admin/public-notices/" + id))
                .body(new PublicNoticeCreatedResponse(id));
    }

    @Override
    public ResponseEntity<Void> update(@LoginMember Long actorMemberId,
                                       Long publicNoticeId,
                                       PublicNoticeWriteRequest request) {
        publicNoticeService.update(actorMemberId,
                request.toUpdateParam(publicNoticeId));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> publish(@LoginMember Long actorMemberId,
                                        Long publicNoticeId,
                                        NoticePublishRequest request) {
        publicNoticeService.publish(actorMemberId,
                request.toPublicParam(publicNoticeId));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> close(@LoginMember Long actorMemberId,
                                      Long publicNoticeId) {
        publicNoticeService.close(actorMemberId, publicNoticeId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> archive(@LoginMember Long actorMemberId,
                                        Long publicNoticeId) {
        publicNoticeService.archive(actorMemberId, publicNoticeId);
        return ResponseEntity.noContent().build();
    }
}
