package kr.ac.tukorea.bandi.global.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeDetailResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeSummaryResponse;
import kr.ac.tukorea.bandi.global.response.PageResponse;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("/api/internal-notices")
@Tag(name = ApiTag.INTERNAL_NOTICE, description = "동아리 내부 공지 열람 API")
@SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SCHEME)
public interface InternalNoticeApiDocs {

    @Operation(summary = "읽을 수 있는 공지 목록 조회")
    @GetMapping
    ResponseEntity<PageResponse<InternalNoticeSummaryResponse>> search(
            @Parameter(hidden = true) @LoginMember Long memberId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "ALL") String readFilter,
            @RequestParam(required = false) String targetScope,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize);

    @Operation(summary = "공지 상세 조회 및 읽음 기록")
    @GetMapping("/{internalNoticeId}")
    ResponseEntity<InternalNoticeDetailResponse> lookup(
            @Parameter(hidden = true) @LoginMember Long memberId,
            @PathVariable Long internalNoticeId);

    @Operation(summary = "공지 첨부파일 다운로드")
    @GetMapping("/{internalNoticeId}/attachments/{storedFileId}/download")
    ResponseEntity<Resource> download(
            @Parameter(hidden = true) @LoginMember Long memberId,
            @PathVariable Long internalNoticeId,
            @PathVariable Long storedFileId);

    @Operation(summary = "공지 본문 내부 이미지 조회")
    @GetMapping("/{internalNoticeId}/attachments/{storedFileId}/inline")
    ResponseEntity<Resource> inline(
            @Parameter(hidden = true) @LoginMember Long memberId,
            @PathVariable Long internalNoticeId,
            @PathVariable Long storedFileId);
}
