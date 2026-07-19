package kr.ac.tukorea.bandi.global.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeDetailResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeSummaryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("/api/public-notices")
@Tag(name = ApiTag.PUBLIC_NOTICE, description = "외부 공개 공시 조회 API")
public interface PublicNoticeApiDocs {

    @Operation(summary = "공개 공시 목록 조회")
    @GetMapping
    ResponseEntity<List<PublicNoticeSummaryResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize);

    @Operation(summary = "공개 공시 상세 조회")
    @GetMapping("/{publicNoticeId}")
    ResponseEntity<PublicNoticeDetailResponse> lookup(
            @PathVariable Long publicNoticeId);

    @Operation(summary = "공개 공시 첨부파일 다운로드")
    @GetMapping("/{publicNoticeId}/attachments/{storedFileId}/download")
    ResponseEntity<Void> download(
            @PathVariable Long publicNoticeId,
            @PathVariable Long storedFileId);
}
