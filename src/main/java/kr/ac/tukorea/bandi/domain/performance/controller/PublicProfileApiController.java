package kr.ac.tukorea.bandi.domain.performance.controller;

import kr.ac.tukorea.bandi.domain.performance.dto.request.PublicProfileConsentParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PublicProfileConsentRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PublicProfileCreateRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PublicProfileSearchCondition;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PublicProfileUpdateRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PublicProfileVisibilityParam;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PublicProfileVisibilityRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceIdentifierResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PublicProfileConsentResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PublicProfileResponse;
import kr.ac.tukorea.bandi.domain.performance.model.PublicProfileVisibility;
import kr.ac.tukorea.bandi.domain.performance.service.PublicProfileService;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import kr.ac.tukorea.bandi.global.swagger.PublicProfileApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PublicProfileApiController implements PublicProfileApiDocs {
    private final PublicProfileService publicProfileService;

    @Override
    public ResponseEntity<List<PublicProfileResponse>> search(
            @LoginMember Long actorMemberId, Long memberId,
            PublicProfileVisibility visibilityStatus, int offset, int limit) {
        return ResponseEntity.ok(publicProfileService.search(actorMemberId,
                new PublicProfileSearchCondition(memberId, visibilityStatus,
                        offset, limit)));
    }

    @Override
    public ResponseEntity<PerformanceIdentifierResponse> create(
            @LoginMember Long actorMemberId, PublicProfileCreateRequest request) {
        Long id = publicProfileService.create(actorMemberId, request.toParam());
        return ResponseEntity.created(URI.create(
                        "/api/public-profile-management/" + id))
                .body(new PerformanceIdentifierResponse(id));
    }

    @Override
    public ResponseEntity<Void> update(@LoginMember Long actorMemberId,
                                       Long profileId,
                                       PublicProfileUpdateRequest request) {
        publicProfileService.update(actorMemberId, request.toParam(profileId));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> changeVisibility(
            @LoginMember Long actorMemberId, Long profileId,
            PublicProfileVisibilityRequest request) {
        publicProfileService.changeVisibility(actorMemberId,
                new PublicProfileVisibilityParam(profileId,
                        request.visibilityStatus()));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> agree(@LoginMember Long actorMemberId,
                                      Long profileId,
                                      PublicProfileConsentRequest request) {
        publicProfileService.agree(actorMemberId,
                new PublicProfileConsentParam(profileId,
                        request.policyDocumentVersionId(),
                        request.consentScope()));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> revoke(@LoginMember Long actorMemberId,
                                       Long consentId) {
        publicProfileService.revoke(actorMemberId, consentId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<PublicProfileConsentResponse>> searchConsents(
            @LoginMember Long actorMemberId, Long profileId) {
        return ResponseEntity.ok(publicProfileService.searchConsents(actorMemberId,
                profileId));
    }
}
