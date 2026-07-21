package kr.ac.tukorea.bandi.domain.asset.controller;

import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetItemCreateRequest;
import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetSearchCondition;
import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetSearchFilter;
import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetUnitCreateRequest;
import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetItemUpdateRequest;
import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetStatusChangeRequest;
import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetUnitUpdateRequest;
import kr.ac.tukorea.bandi.domain.asset.dto.response.AssetIdentifierResponse;
import kr.ac.tukorea.bandi.domain.asset.dto.response.AssetItemResponse;
import kr.ac.tukorea.bandi.domain.asset.dto.response.AssetUnitResponse;
import kr.ac.tukorea.bandi.domain.asset.dto.response.AssetHistoryResponse;
import kr.ac.tukorea.bandi.domain.asset.service.AssetService;
import kr.ac.tukorea.bandi.global.response.FileDownloadResponse;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import kr.ac.tukorea.bandi.global.swagger.AssetApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.nio.charset.StandardCharsets;
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
    public ResponseEntity<List<AssetHistoryResponse>> searchHistories(
            @LoginMember Long actorMemberId, Long assetItemId) {
        return ResponseEntity.ok(assetService.searchHistories(actorMemberId,
                assetItemId));
    }

    @Override
    public ResponseEntity<Resource> downloadPhoto(
            @LoginMember Long actorMemberId, Long assetItemId) {
        return inline(assetService.openPhotoDownload(actorMemberId, assetItemId));
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
    public ResponseEntity<Void> updateItem(
            @LoginMember Long actorMemberId, Long assetItemId,
            AssetItemUpdateRequest request) {
        assetService.updateItem(actorMemberId, assetItemId,
                request.toParam());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> changeItemStatus(
            @LoginMember Long actorMemberId, Long assetItemId,
            AssetStatusChangeRequest request) {
        assetService.changeItemStatus(actorMemberId, assetItemId,
                request.status(), request.note());
        return ResponseEntity.noContent().build();
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
    public ResponseEntity<Void> updateUnit(
            @LoginMember Long actorMemberId, Long assetUnitId,
            AssetUnitUpdateRequest request) {
        assetService.updateUnit(actorMemberId,
                request.toParam(assetUnitId));
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<Resource> inline(FileDownloadResponse file) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .contentLength(file.sizeBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(file.originalName(), StandardCharsets.UTF_8).build().toString())
                .body(file.resource());
    }

}
