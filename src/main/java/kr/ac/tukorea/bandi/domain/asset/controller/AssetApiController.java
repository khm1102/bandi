package kr.ac.tukorea.bandi.domain.asset.controller;

import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetItemCreateRequest;
import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetSearchCondition;
import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetSearchFilter;
import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetUnitCreateRequest;
import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetUsageCreateRequest;
import kr.ac.tukorea.bandi.domain.asset.dto.response.AssetIdentifierResponse;
import kr.ac.tukorea.bandi.domain.asset.dto.response.AssetItemResponse;
import kr.ac.tukorea.bandi.domain.asset.dto.response.AssetUnitResponse;
import kr.ac.tukorea.bandi.domain.asset.dto.response.AssetUsageResponse;
import kr.ac.tukorea.bandi.domain.asset.service.AssetService;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import kr.ac.tukorea.bandi.global.swagger.AssetApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class AssetApiController implements AssetApiDocs {

    private final AssetService assetService;

    @Override
    public ResponseEntity<List<AssetItemResponse>> searchItems(
            @LoginMember Long actorMemberId, String keyword,
            String categoryCode, AssetSearchFilter filter) {
        return ResponseEntity.ok(assetService.searchItems(actorMemberId,
                new AssetSearchCondition(keyword, categoryCode,
                        filter.trackingType(), filter.status())));
    }

    @Override
    public ResponseEntity<List<AssetUnitResponse>> searchUnits(
            @LoginMember Long actorMemberId, Long assetItemId) {
        return ResponseEntity.ok(assetService.searchUnits(actorMemberId,
                assetItemId));
    }

    @Override
    public ResponseEntity<List<AssetUsageResponse>> searchUsages(
            @LoginMember Long actorMemberId, Long assetItemId) {
        return ResponseEntity.ok(assetService.searchUsages(actorMemberId,
                assetItemId));
    }

    @Override
    public ResponseEntity<AssetIdentifierResponse> registerItem(
            @LoginMember Long actorMemberId,
            AssetItemCreateRequest request) {
        Long id = assetService.registerItem(actorMemberId, request.toParam());
        return ResponseEntity.created(URI.create("/api/assets/" + id))
                .body(new AssetIdentifierResponse(id));
    }

    @Override
    public ResponseEntity<AssetIdentifierResponse> registerUnit(
            @LoginMember Long actorMemberId, Long assetItemId,
            AssetUnitCreateRequest request) {
        Long id = assetService.registerUnit(actorMemberId,
                request.toParam(assetItemId));
        return ResponseEntity.created(URI.create("/api/assets/" + assetItemId
                        + "/units/" + id))
                .body(new AssetIdentifierResponse(id));
    }

    @Override
    public ResponseEntity<AssetIdentifierResponse> reserve(
            @LoginMember Long actorMemberId,
            AssetUsageCreateRequest request) {
        Long id = assetService.reserve(actorMemberId, request.toParam());
        return ResponseEntity.created(URI.create("/api/assets/usages/" + id))
                .body(new AssetIdentifierResponse(id));
    }

    @Override
    public ResponseEntity<Void> returnUsage(
            @LoginMember Long actorMemberId, Long assetUsageId) {
        assetService.returnUsage(actorMemberId, assetUsageId);
        return ResponseEntity.noContent().build();
    }
}
