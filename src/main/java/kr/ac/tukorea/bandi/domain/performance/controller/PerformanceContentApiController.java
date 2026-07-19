package kr.ac.tukorea.bandi.domain.performance.controller;

import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceCastAssignRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceCastChangeRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceCharacterRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceMediaPublishedRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceMediaRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceRoundCastAssignRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceRoundCastChangeRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.request.ProductionCreditRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceCastHistoryResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceCastResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceCharacterResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceIdentifierResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceMediaResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceRoundCastResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.ProductionCreditResponse;
import kr.ac.tukorea.bandi.domain.performance.service.PerformanceContentService;
import kr.ac.tukorea.bandi.domain.performance.service.PerformanceRoundCastService;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import kr.ac.tukorea.bandi.global.swagger.PerformanceContentApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PerformanceContentApiController implements PerformanceContentApiDocs {

    private final PerformanceContentService contentService;
    private final PerformanceRoundCastService roundCastService;

    @Override
    public ResponseEntity<List<PerformanceCharacterResponse>> searchCharacters(
            @LoginMember Long actorMemberId, Long projectId) {
        return ResponseEntity.ok(contentService.searchCharacters(
                actorMemberId, projectId));
    }

    @Override
    public ResponseEntity<PerformanceIdentifierResponse> createCharacter(
            @LoginMember Long actorMemberId,
            PerformanceCharacterRequest request) {
        Long id = contentService.createCharacter(actorMemberId,
                request.toParam(null));
        return created("characters", id);
    }

    @Override
    public ResponseEntity<Void> updateCharacter(
            @LoginMember Long actorMemberId, Long characterId,
            PerformanceCharacterRequest request) {
        contentService.updateCharacter(actorMemberId,
                request.toParam(characterId));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> removeCharacter(
            @LoginMember Long actorMemberId, Long characterId) {
        contentService.removeCharacter(actorMemberId, characterId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<PerformanceCastResponse>> searchCasts(
            @LoginMember Long actorMemberId, Long projectId) {
        return ResponseEntity.ok(contentService.searchCasts(
                actorMemberId, projectId));
    }

    @Override
    public ResponseEntity<List<PerformanceCastHistoryResponse>>
            searchCastHistories(@LoginMember Long actorMemberId,
                                Long projectId) {
        return ResponseEntity.ok(contentService.searchCastHistories(
                actorMemberId, projectId));
    }

    @Override
    public ResponseEntity<PerformanceIdentifierResponse> assignCast(
            @LoginMember Long actorMemberId,
            PerformanceCastAssignRequest request) {
        Long id = contentService.assignCast(actorMemberId, request.toParam());
        return created("casts", id);
    }

    @Override
    public ResponseEntity<Void> changeCast(@LoginMember Long actorMemberId,
                                           Long castId,
                                           PerformanceCastChangeRequest request) {
        contentService.changeCast(actorMemberId, request.toParam(castId));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> removeCast(@LoginMember Long actorMemberId,
                                           Long castId, String reason) {
        contentService.removeCast(actorMemberId, castId, reason);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<ProductionCreditResponse>> searchCredits(
            @LoginMember Long actorMemberId, Long projectId) {
        return ResponseEntity.ok(contentService.searchCredits(
                actorMemberId, projectId));
    }

    @Override
    public ResponseEntity<PerformanceIdentifierResponse> createCredit(
            @LoginMember Long actorMemberId, ProductionCreditRequest request) {
        Long id = contentService.createCredit(actorMemberId,
                request.toParam(null));
        return created("credits", id);
    }

    @Override
    public ResponseEntity<Void> updateCredit(@LoginMember Long actorMemberId,
                                             Long creditId,
                                             ProductionCreditRequest request) {
        contentService.updateCredit(actorMemberId,
                request.toParam(creditId));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> removeCredit(@LoginMember Long actorMemberId,
                                             Long creditId) {
        contentService.removeCredit(actorMemberId, creditId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<PerformanceMediaResponse>> searchMedia(
            @LoginMember Long actorMemberId, Long projectId) {
        return ResponseEntity.ok(contentService.searchMedia(
                actorMemberId, projectId));
    }

    @Override
    public ResponseEntity<PerformanceIdentifierResponse> createMedia(
            @LoginMember Long actorMemberId, PerformanceMediaRequest request) {
        Long id = contentService.createMedia(actorMemberId,
                request.toParam(null));
        return created("media", id);
    }

    @Override
    public ResponseEntity<Void> updateMedia(@LoginMember Long actorMemberId,
                                            Long mediaId,
                                            PerformanceMediaRequest request) {
        contentService.updateMedia(actorMemberId, request.toParam(mediaId));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> changeMediaPublished(
            @LoginMember Long actorMemberId, Long mediaId,
            PerformanceMediaPublishedRequest request) {
        contentService.changeMediaPublished(actorMemberId, mediaId,
                request.published());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> removeMedia(@LoginMember Long actorMemberId,
                                            Long mediaId) {
        contentService.removeMedia(actorMemberId, mediaId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<PerformanceRoundCastResponse>> searchRoundCasts(
            @LoginMember Long actorMemberId, Long roundId) {
        return ResponseEntity.ok(roundCastService.search(
                actorMemberId, roundId));
    }

    @Override
    public ResponseEntity<PerformanceIdentifierResponse> assignRoundCast(
            @LoginMember Long actorMemberId,
            PerformanceRoundCastAssignRequest request) {
        Long id = roundCastService.assign(actorMemberId, request.toParam());
        return created("round-casts", id);
    }

    @Override
    public ResponseEntity<Void> changeRoundCast(
            @LoginMember Long actorMemberId, Long roundCastId,
            PerformanceRoundCastChangeRequest request) {
        roundCastService.change(actorMemberId, request.toParam(roundCastId));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> removeRoundCast(
            @LoginMember Long actorMemberId, Long roundCastId, String reason) {
        roundCastService.remove(actorMemberId, roundCastId, reason);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<PerformanceIdentifierResponse> created(
            String resource, Long id) {
        URI location = URI.create("/api/performance-content-management/"
                + resource + "/" + id);
        return ResponseEntity.created(location)
                .body(new PerformanceIdentifierResponse(id));
    }
}
