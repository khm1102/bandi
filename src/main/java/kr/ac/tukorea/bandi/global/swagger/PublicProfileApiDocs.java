package kr.ac.tukorea.bandi.global.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PublicProfileConsentRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PublicProfileCreateRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PublicProfileFilter;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PublicProfileUpdateRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PublicProfileVisibilityRequest;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformanceIdentifierResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PublicProfileConsentResponse;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PublicProfileResponse;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("/api/public-profile-management")
@Tag(name = ApiTag.PERFORMANCE, description = "공개 프로필과 항목별 동의 관리 API")
@SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SCHEME)
public interface PublicProfileApiDocs {
    @Operation(summary = "공개 프로필 관리 목록 조회")
    @GetMapping
    ResponseEntity<List<PublicProfileResponse>> search(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @RequestParam(required = false) Long memberId,
            @ParameterObject @ModelAttribute PublicProfileFilter filter,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit);

    @Operation(summary = "공개 프로필 등록")
    @PostMapping
    ResponseEntity<PerformanceIdentifierResponse> create(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @Valid @RequestBody PublicProfileCreateRequest request);

    @Operation(summary = "공개 프로필 수정")
    @PutMapping("/{profileId}")
    ResponseEntity<Void> update(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long profileId,
            @Valid @RequestBody PublicProfileUpdateRequest request);

    @Operation(summary = "공개 프로필 게시 상태 변경")
    @PatchMapping("/{profileId}/visibility")
    ResponseEntity<Void> changeVisibility(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long profileId,
            @Valid @RequestBody PublicProfileVisibilityRequest request);

    @Operation(summary = "프로필 항목 공개 동의")
    @PostMapping("/{profileId}/consents")
    ResponseEntity<Void> agree(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long profileId,
            @Valid @RequestBody PublicProfileConsentRequest request);

    @Operation(summary = "프로필 항목 공개 동의 철회")
    @PostMapping("/consents/{consentId}/revoke")
    ResponseEntity<Void> revoke(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long consentId);

    @Operation(summary = "프로필 동의 이력 조회")
    @GetMapping("/{profileId}/consents")
    ResponseEntity<List<PublicProfileConsentResponse>> searchConsents(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long profileId);

}
