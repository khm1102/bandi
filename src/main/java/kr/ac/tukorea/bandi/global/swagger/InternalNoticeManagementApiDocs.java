package kr.ac.tukorea.bandi.global.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ac.tukorea.bandi.domain.notice.dto.request.InternalNoticeManageFilter;
import kr.ac.tukorea.bandi.domain.notice.dto.request.InternalNoticeWriteRequest;
import kr.ac.tukorea.bandi.domain.notice.dto.request.NoticePublishRequest;
import kr.ac.tukorea.bandi.domain.notice.dto.request.MarkdownPreviewRequest;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeCreatedResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeManageDetailResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeManageSummaryResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeReadStatusResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.MarkdownPreviewResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.NoticeInlineImageResponse;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/api/internal-notice-management")
@Tag(name = ApiTag.INTERNAL_NOTICE, description = "전체·팀 공지 관리 API")
@SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SCHEME)
public interface InternalNoticeManagementApiDocs {

    @Operation(summary = "공지 본문 이미지 업로드")
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<NoticeInlineImageResponse> uploadInlineImage(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @RequestPart("file") MultipartFile file);

    @Operation(summary = "작성 중 공지 본문 이미지 미리보기")
    @GetMapping("/images/{storedFileId}/preview")
    ResponseEntity<Resource> previewInlineImage(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long storedFileId);

    @Operation(summary = "관리용 공지 본문 이미지 조회")
    @GetMapping("/{internalNoticeId}/attachments/{storedFileId}/inline")
    ResponseEntity<Resource> inlineAttachmentImage(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long internalNoticeId,
            @PathVariable Long storedFileId);

    @Operation(summary = "관리용 공지 첨부파일 다운로드")
    @GetMapping("/{internalNoticeId}/attachments/{storedFileId}/download")
    ResponseEntity<Resource> downloadAttachment(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long internalNoticeId,
            @PathVariable Long storedFileId);

    @Operation(summary = "관리용 공지 Markdown 미리보기")
    @PostMapping("/markdown-preview")
    ResponseEntity<MarkdownPreviewResponse> preview(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @Valid @RequestBody MarkdownPreviewRequest request);

    @Operation(summary = "관리 가능한 공지 목록 조회")
    @GetMapping
    ResponseEntity<List<InternalNoticeManageSummaryResponse>> search(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @RequestParam(required = false) String keyword,
            @ParameterObject @ModelAttribute InternalNoticeManageFilter filter,
            @RequestParam(required = false) Long teamId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize);

    @Operation(summary = "관리용 공지 상세 조회")
    @GetMapping("/{internalNoticeId}")
    ResponseEntity<InternalNoticeManageDetailResponse> lookup(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long internalNoticeId);

    @Operation(summary = "공지 초안 등록")
    @PostMapping
    ResponseEntity<InternalNoticeCreatedResponse> create(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @Valid @RequestBody InternalNoticeWriteRequest request);

    @Operation(summary = "공지 수정")
    @PutMapping("/{internalNoticeId}")
    ResponseEntity<Void> update(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long internalNoticeId,
            @Valid @RequestBody InternalNoticeWriteRequest request);

    @Operation(summary = "공지 게시 또는 예약 게시")
    @PostMapping("/{internalNoticeId}/publish")
    ResponseEntity<Void> publish(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long internalNoticeId,
            @RequestBody NoticePublishRequest request);

    @Operation(summary = "공지 게시 종료")
    @PostMapping("/{internalNoticeId}/close")
    ResponseEntity<Void> close(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long internalNoticeId);

    @Operation(summary = "공지 보관")
    @PostMapping("/{internalNoticeId}/archive")
    ResponseEntity<Void> archive(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long internalNoticeId);

    @Operation(summary = "공지 초안 복귀")
    @PostMapping("/{internalNoticeId}/draft")
    ResponseEntity<Void> returnToDraft(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long internalNoticeId);

    @Operation(summary = "공지 초안 삭제")
    @DeleteMapping("/{internalNoticeId}")
    ResponseEntity<Void> deleteDraft(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long internalNoticeId);

    @Operation(summary = "공지 대상별 읽음 현황 조회")
    @GetMapping("/{internalNoticeId}/read-statuses")
    ResponseEntity<List<InternalNoticeReadStatusResponse>> searchReadStatuses(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long internalNoticeId);
}
