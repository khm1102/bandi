package kr.ac.tukorea.bandi.global.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.tukorea.bandi.domain.resource.dto.request.ResourceWriteRequest;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceDetailResponse;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceIdentifierResponse;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceSummaryResponse;
import kr.ac.tukorea.bandi.global.response.PageResponse;
import kr.ac.tukorea.bandi.global.response.ShareLinkResponse;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@RequestMapping("/api/resources")
@Tag(name = ApiTag.RESOURCE, description = "공용 Markdown 자료실 API")
@SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SCHEME)
public interface ResourceApiDocs {

    @Operation(summary = "자료 목록 조회")
    @GetMapping
    ResponseEntity<PageResponse<ResourceSummaryResponse>> search(
            @Parameter(hidden = true) @LoginMember Long memberId,
            @RequestParam(name = "q", required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize);

    @Operation(summary = "자료 상세 조회")
    @GetMapping("/{resourceId}")
    ResponseEntity<ResourceDetailResponse> lookup(
            @Parameter(hidden = true) @LoginMember Long memberId,
            @PathVariable Long resourceId);

    @Operation(summary = "자료 제목 공개 공유 링크 발급")
    @PostMapping("/{resourceId}/share-link")
    ResponseEntity<ShareLinkResponse> issueShareLink(
            @Parameter(hidden = true) @LoginMember Long memberId,
            @PathVariable Long resourceId);

    @Operation(summary = "자료 제목 공개 공유 링크 중단")
    @DeleteMapping("/{resourceId}/share-link")
    ResponseEntity<Void> revokeShareLink(
            @Parameter(hidden = true) @LoginMember Long memberId,
            @PathVariable Long resourceId);

    @Operation(summary = "Markdown 미리보기")
    @PostMapping("/markdown-preview")
    ResponseEntity<Map<String, String>> preview(
            @Parameter(hidden = true) @LoginMember Long memberId,
            @RequestBody Map<String, String> request);

    @Operation(summary = "자료 작성")
    @PostMapping
    ResponseEntity<ResourceIdentifierResponse> create(
            @Parameter(hidden = true) @LoginMember Long memberId,
            @RequestBody ResourceWriteRequest request);

    @Operation(summary = "자료 수정")
    @PutMapping("/{resourceId}")
    ResponseEntity<Void> update(
            @Parameter(hidden = true) @LoginMember Long memberId,
            @PathVariable Long resourceId,
            @RequestBody ResourceWriteRequest request);

    @Operation(summary = "자료 삭제")
    @DeleteMapping("/{resourceId}")
    ResponseEntity<Void> delete(
            @Parameter(hidden = true) @LoginMember Long memberId,
            @PathVariable Long resourceId);

    @Operation(summary = "자료 파일 다운로드")
    @GetMapping("/{resourceId}/files/{storedFileId}/download")
    ResponseEntity<Resource> download(
            @Parameter(hidden = true) @LoginMember Long memberId,
            @PathVariable Long resourceId,
            @PathVariable Long storedFileId);

    @Operation(summary = "자료 내부 이미지 열람")
    @GetMapping("/{resourceId}/files/{storedFileId}/inline")
    ResponseEntity<Resource> inline(
            @Parameter(hidden = true) @LoginMember Long memberId,
            @PathVariable Long resourceId,
            @PathVariable Long storedFileId);

    @Operation(summary = "링크 카드 대표 이미지 열람")
    @GetMapping("/{resourceId}/link-previews/{storedFileId}/inline")
    ResponseEntity<Resource> inlineLinkPreview(
            @Parameter(hidden = true) @LoginMember Long memberId,
            @PathVariable Long resourceId,
            @PathVariable Long storedFileId);
}
