package kr.ac.tukorea.bandi.domain.notice.controller;

import kr.ac.tukorea.bandi.domain.notice.dto.request.InternalNoticeManageFilter;
import kr.ac.tukorea.bandi.domain.notice.dto.request.InternalNoticeManageSearchParam;
import kr.ac.tukorea.bandi.domain.notice.dto.request.InternalNoticeWriteRequest;
import kr.ac.tukorea.bandi.domain.notice.dto.request.NoticePublishRequest;
import kr.ac.tukorea.bandi.domain.notice.dto.request.MarkdownPreviewRequest;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeCreatedResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeManageDetailResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeManageSummaryResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeReadStatusResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.MarkdownPreviewResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.NoticeInlineImageResponse;
import kr.ac.tukorea.bandi.domain.file.dto.response.FileReferenceResponse;
import kr.ac.tukorea.bandi.domain.file.service.FileUploadParam;
import kr.ac.tukorea.bandi.global.response.FileDownloadResponse;
import kr.ac.tukorea.bandi.domain.notice.service.InternalNoticeService;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import kr.ac.tukorea.bandi.global.swagger.InternalNoticeManagementApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class InternalNoticeManagementApiController
        implements InternalNoticeManagementApiDocs {

    private final InternalNoticeService internalNoticeService;

    @Override
    public ResponseEntity<NoticeInlineImageResponse> uploadInlineImage(
            @LoginMember Long actorMemberId, MultipartFile file) {
        FileReferenceResponse uploaded = internalNoticeService.uploadInlineImage(actorMemberId,
                new FileUploadParam("notice", file.getOriginalFilename(), file.getSize(),
                        file::getInputStream, actorMemberId));
        return ResponseEntity.ok(NoticeInlineImageResponse.of(uploaded,
                "/api/internal-notice-management/images/" + uploaded.storedFileId()
                        + "/preview"));
    }

    @Override
    public ResponseEntity<Resource> previewInlineImage(@LoginMember Long actorMemberId,
                                                        Long storedFileId) {
        return inline(internalNoticeService.openTemporaryInlineImage(actorMemberId, storedFileId));
    }

    @Override
    public ResponseEntity<Resource> inlineAttachmentImage(@LoginMember Long actorMemberId,
                                                           Long internalNoticeId,
                                                           Long storedFileId) {
        return inline(internalNoticeService.openManageableAttachmentInline(actorMemberId,
                internalNoticeId, storedFileId));
    }

    @Override
    public ResponseEntity<Resource> downloadAttachment(@LoginMember Long actorMemberId,
                                                       Long internalNoticeId,
                                                       Long storedFileId) {
        return attachment(internalNoticeService.openManageableAttachmentDownload(actorMemberId,
                internalNoticeId, storedFileId));
    }

    @Override
    public ResponseEntity<MarkdownPreviewResponse> preview(@LoginMember Long actorMemberId,
                                                            MarkdownPreviewRequest request) {
        return ResponseEntity.ok(new MarkdownPreviewResponse(internalNoticeService.preview(
                actorMemberId, request.internalNoticeId(), request.bodyMarkdown(),
                request.attachmentFileIds())));
    }

    @Override
    public ResponseEntity<List<InternalNoticeManageSummaryResponse>> search(
            @LoginMember Long actorMemberId, String keyword,
            InternalNoticeManageFilter filter, Long teamId, int page,
            int pageSize) {
        return ResponseEntity.ok(internalNoticeService.searchManageable(actorMemberId,
                new InternalNoticeManageSearchParam(keyword, filter.status(),
                        filter.targetScope(), teamId, page, pageSize)));
    }

    @Override
    public ResponseEntity<InternalNoticeManageDetailResponse> lookup(
            @LoginMember Long actorMemberId, Long internalNoticeId) {
        return ResponseEntity.ok(internalNoticeService.lookupManageable(actorMemberId,
                internalNoticeId));
    }

    @Override
    public ResponseEntity<InternalNoticeCreatedResponse> create(
            @LoginMember Long actorMemberId, InternalNoticeWriteRequest request) {
        Long id = internalNoticeService.createDraft(actorMemberId, request.toParam());
        return ResponseEntity.created(URI.create(
                        "/api/internal-notice-management/" + id))
                .body(new InternalNoticeCreatedResponse(id));
    }

    @Override
    public ResponseEntity<Void> update(@LoginMember Long actorMemberId,
                                       Long internalNoticeId,
                                       InternalNoticeWriteRequest request) {
        internalNoticeService.update(actorMemberId,
                request.toUpdateParam(internalNoticeId));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> publish(@LoginMember Long actorMemberId,
                                        Long internalNoticeId,
                                        NoticePublishRequest request) {
        internalNoticeService.publish(actorMemberId,
                request.toInternalParam(internalNoticeId));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> close(@LoginMember Long actorMemberId,
                                      Long internalNoticeId) {
        internalNoticeService.close(actorMemberId, internalNoticeId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> archive(@LoginMember Long actorMemberId,
                                        Long internalNoticeId) {
        internalNoticeService.archive(actorMemberId, internalNoticeId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> returnToDraft(@LoginMember Long actorMemberId,
                                              Long internalNoticeId) {
        internalNoticeService.returnToDraft(actorMemberId, internalNoticeId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> deleteDraft(@LoginMember Long actorMemberId,
                                            Long internalNoticeId) {
        internalNoticeService.deleteDraft(actorMemberId, internalNoticeId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<InternalNoticeReadStatusResponse>> searchReadStatuses(
            @LoginMember Long actorMemberId, Long internalNoticeId) {
        return ResponseEntity.ok(internalNoticeService.searchReadStatuses(actorMemberId,
                internalNoticeId));
    }

    private ResponseEntity<Resource> inline(FileDownloadResponse file) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .contentLength(file.sizeBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(file.originalName(), StandardCharsets.UTF_8).build().toString())
                .body(file.resource());
    }

    private ResponseEntity<Resource> attachment(FileDownloadResponse file) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .contentLength(file.sizeBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(file.originalName(), StandardCharsets.UTF_8).build().toString())
                .body(file.resource());
    }
}
