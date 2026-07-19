package kr.ac.tukorea.bandi.domain.notice.controller;

import kr.ac.tukorea.bandi.domain.notice.dto.request.InternalNoticeManageFilter;
import kr.ac.tukorea.bandi.domain.notice.dto.request.InternalNoticeManageSearchParam;
import kr.ac.tukorea.bandi.domain.notice.dto.request.InternalNoticeWriteRequest;
import kr.ac.tukorea.bandi.domain.notice.dto.request.NoticePublishRequest;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeCreatedResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeManageDetailResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeManageSummaryResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeReadStatusResponse;
import kr.ac.tukorea.bandi.domain.notice.service.InternalNoticeService;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import kr.ac.tukorea.bandi.global.swagger.InternalNoticeManagementApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class InternalNoticeManagementApiController
        implements InternalNoticeManagementApiDocs {

    private final InternalNoticeService internalNoticeService;

    @Override
    public ResponseEntity<List<InternalNoticeManageSummaryResponse>> search(
            @LoginMember Long actorMemberId, String keyword,
            InternalNoticeManageFilter filter, Long teamId, int page,
            int pageSize) {
        return ResponseEntity.ok(internalNoticeService.searchManageable(actorMemberId,
                new InternalNoticeManageSearchParam(keyword, filter.status(),
                        filter.targetScope(), teamId, page, pageSize)));
    }

    @Override
    public ResponseEntity<InternalNoticeManageDetailResponse> lookup(
            @LoginMember Long actorMemberId, Long internalNoticeId) {
        return ResponseEntity.ok(internalNoticeService.lookupManageable(actorMemberId,
                internalNoticeId));
    }

    @Override
    public ResponseEntity<InternalNoticeCreatedResponse> create(
            @LoginMember Long actorMemberId, InternalNoticeWriteRequest request) {
        Long id = internalNoticeService.createDraft(actorMemberId, request.toParam());
        return ResponseEntity.created(URI.create(
                        "/api/internal-notice-management/" + id))
                .body(new InternalNoticeCreatedResponse(id));
    }

    @Override
    public ResponseEntity<Void> update(@LoginMember Long actorMemberId,
                                       Long internalNoticeId,
                                       InternalNoticeWriteRequest request) {
        internalNoticeService.update(actorMemberId,
                request.toUpdateParam(internalNoticeId));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> publish(@LoginMember Long actorMemberId,
                                        Long internalNoticeId,
                                        NoticePublishRequest request) {
        internalNoticeService.publish(actorMemberId,
                request.toInternalParam(internalNoticeId));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> close(@LoginMember Long actorMemberId,
                                      Long internalNoticeId) {
        internalNoticeService.close(actorMemberId, internalNoticeId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> archive(@LoginMember Long actorMemberId,
                                        Long internalNoticeId) {
        internalNoticeService.archive(actorMemberId, internalNoticeId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<InternalNoticeReadStatusResponse>> searchReadStatuses(
            @LoginMember Long actorMemberId, Long internalNoticeId) {
        return ResponseEntity.ok(internalNoticeService.searchReadStatuses(actorMemberId,
                internalNoticeId));
    }
}
