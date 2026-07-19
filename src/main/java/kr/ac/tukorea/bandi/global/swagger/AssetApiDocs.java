package kr.ac.tukorea.bandi.global.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetItemCreateRequest;
import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetSearchFilter;
import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetUsageCreateRequest;
import kr.ac.tukorea.bandi.domain.asset.dto.request.AssetUnitCreateRequest;
import kr.ac.tukorea.bandi.domain.asset.dto.response.AssetIdentifierResponse;
import kr.ac.tukorea.bandi.domain.asset.dto.response.AssetItemResponse;
import kr.ac.tukorea.bandi.domain.asset.dto.response.AssetUnitResponse;
import kr.ac.tukorea.bandi.domain.asset.dto.response.AssetUsageResponse;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("/api/assets")
@Tag(name = ApiTag.ASSET, description = "소품·장비 품목과 공연 사용 관리 API")
@SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SCHEME)
public interface AssetApiDocs {

    @Operation(summary = "소품·장비 품목 검색")
    @GetMapping
    ResponseEntity<List<AssetItemResponse>> searchItems(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryCode,
            @ParameterObject @ModelAttribute AssetSearchFilter filter);

    @Operation(summary = "개별 장비 목록 조회")
    @GetMapping("/{assetItemId}/units")
    ResponseEntity<List<AssetUnitResponse>> searchUnits(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long assetItemId);

    @Operation(summary = "품목 사용 이력 조회")
    @GetMapping("/{assetItemId}/usages")
    ResponseEntity<List<AssetUsageResponse>> searchUsages(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long assetItemId);

    @Operation(summary = "소품·장비 품목 등록")
    @PostMapping
    ResponseEntity<AssetIdentifierResponse> registerItem(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @Valid @RequestBody AssetItemCreateRequest request);

    @Operation(summary = "개별 장비 등록")
    @PostMapping("/{assetItemId}/units")
    ResponseEntity<AssetIdentifierResponse> registerUnit(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long assetItemId,
            @Valid @RequestBody AssetUnitCreateRequest request);

    @Operation(summary = "소품·장비 사용 예약")
    @PostMapping("/usages")
    ResponseEntity<AssetIdentifierResponse> reserve(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @Valid @RequestBody AssetUsageCreateRequest request);

    @Operation(summary = "소품·장비 반납")
    @PostMapping("/usages/{assetUsageId}/return")
    ResponseEntity<Void> returnUsage(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long assetUsageId);
}
