package kr.ac.tukorea.bandi.domain.performance.controller;

import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformancePublicPageRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformancePublicPageStatusParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceViewingGuideRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PublicPageStatusRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceIdentifierResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformancePublicPageResponse;
import kr.ac.tukorea.bandi.domain.performance.service.PerformancePublicPageService;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import kr.ac.tukorea.bandi.global.swagger.PerformancePublicPageManagementApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PerformancePublicPageManagementApiController
        implements PerformancePublicPageManagementApiDocs {
    private final PerformancePublicPageService publicPageService;

    @Override
    public ResponseEntity<List<PerformancePublicPageResponse>> search(
            @LoginMember Long actorMemberId) {
        return ResponseEntity.ok(publicPageService.search(actorMemberId));
    }

    @Override
    public ResponseEntity<PerformanceIdentifierResponse> create(
            @LoginMember Long actorMemberId,
            PerformancePublicPageRequest request) {
        Long id = publicPageService.create(actorMemberId, request.toParam(null));
        return ResponseEntity.created(URI.create(
                        "/api/performance-page-management/" + id))
                .body(new PerformanceIdentifierResponse(id));
    }

    @Override
    public ResponseEntity<Void> update(@LoginMember Long actorMemberId,
                                       Long pageId,
                                       PerformancePublicPageRequest request) {
        publicPageService.update(actorMemberId, request.toParam(pageId));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> changeStatus(@LoginMember Long actorMemberId,
                                             Long pageId,
                                             PublicPageStatusRequest request) {
        publicPageService.changeStatus(actorMemberId,
                new PerformancePublicPageStatusParam(pageId, request.status()));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> saveViewingGuide(
            @LoginMember Long actorMemberId,
            PerformanceViewingGuideRequest request) {
        publicPageService.saveViewingGuide(actorMemberId, request.toParam());
        return ResponseEntity.noContent().build();
    }
}
