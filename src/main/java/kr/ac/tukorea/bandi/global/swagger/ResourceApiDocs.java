package kr.ac.tukorea.bandi.global.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceDetailResponse;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceSummaryResponse;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("/api/resources")
@Tag(name = ApiTag.RESOURCE, description = "전체·팀 자료실 열람 API")
@SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SCHEME)
public interface ResourceApiDocs {

    @Operation(summary = "읽을 수 있는 자료 목록 조회")
    @GetMapping
    ResponseEntity<List<ResourceSummaryResponse>> search(
            @Parameter(hidden = true) @LoginMember Long memberId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize);

    @Operation(summary = "자료 상세와 현재 파일 조회")
    @GetMapping("/{resourceId}")
    ResponseEntity<ResourceDetailResponse> lookup(
            @Parameter(hidden = true) @LoginMember Long memberId,
            @PathVariable Long resourceId);

    @Operation(summary = "자료 파일 다운로드")
    @GetMapping("/{resourceId}/files/{storedFileId}/download")
    ResponseEntity<Resource> download(
            @Parameter(hidden = true) @LoginMember Long memberId,
            @PathVariable Long resourceId,
            @PathVariable Long storedFileId);
}
