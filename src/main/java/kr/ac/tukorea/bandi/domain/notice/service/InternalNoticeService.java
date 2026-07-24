package kr.ac.tukorea.bandi.domain.notice.service;

import kr.ac.tukorea.bandi.domain.file.dto.response.FileReferenceResponse;
import kr.ac.tukorea.bandi.global.response.FileDownloadResponse;
import kr.ac.tukorea.bandi.global.response.PageResponse;
import kr.ac.tukorea.bandi.domain.file.service.FileAccessDecision;
import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.domain.file.service.FileUploadParam;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.notice.dto.request.InternalNoticeManageSearchCondition;
import kr.ac.tukorea.bandi.domain.notice.dto.request.InternalNoticeManageSearchParam;
import kr.ac.tukorea.bandi.domain.notice.dto.request.InternalNoticePublishParam;
import kr.ac.tukorea.bandi.domain.notice.dto.request.InternalNoticeReadableSearchCondition;
import kr.ac.tukorea.bandi.domain.notice.dto.request.InternalNoticeSearchParam;
import kr.ac.tukorea.bandi.domain.notice.dto.request.InternalNoticeUpdateParam;
import kr.ac.tukorea.bandi.domain.notice.dto.request.InternalNoticeWriteParam;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeAttachmentResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeContentResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeDetailResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeManageContentResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeManageDetailResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeManageSummaryResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticePublicShareResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeReadStatusResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeSummaryResponse;
import kr.ac.tukorea.bandi.domain.notice.exception.InternalNoticeAccessDeniedException;
import kr.ac.tukorea.bandi.domain.notice.exception.InternalNoticeNotFoundException;
import kr.ac.tukorea.bandi.domain.notice.exception.InvalidInternalNoticeException;
import kr.ac.tukorea.bandi.domain.notice.mapper.InternalNoticeMapper;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNotice;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeAttachment;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeTargetScope;
import kr.ac.tukorea.bandi.domain.share.service.ShareTokenGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class InternalNoticeService {

    private static final long INLINE_IMAGE_MAX_BYTES = 10L * 1024 * 1024;
    private static final Set<String> INLINE_IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp");

    private final InternalNoticeMapper internalNoticeMapper;
    private final MemberService memberService;
    private final FileService fileService;
    private final MarkdownRenderer markdownRenderer;
    private final Clock clock;
    private final ShareTokenGenerator shareTokenGenerator;

    @Autowired
    public InternalNoticeService(InternalNoticeMapper internalNoticeMapper,
                                 MemberService memberService,
                                 FileService fileService,
                                 MarkdownRenderer markdownRenderer,
                                 Clock clock,
                                 ShareTokenGenerator shareTokenGenerator) {
        this.internalNoticeMapper = internalNoticeMapper;
        this.memberService = memberService;
        this.fileService = fileService;
        this.markdownRenderer = markdownRenderer;
        this.clock = clock;
        this.shareTokenGenerator = shareTokenGenerator;
    }

    InternalNoticeService(InternalNoticeMapper internalNoticeMapper,
                          MemberService memberService, FileService fileService,
                          MarkdownRenderer markdownRenderer, Clock clock) {
        this(internalNoticeMapper, memberService, fileService, markdownRenderer, clock,
                new ShareTokenGenerator());
    }

    @Transactional
    public Long createDraft(Long actorMemberId, InternalNoticeWriteParam param) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        validateManagement(access, param.targetScope(), param.teamId());
        validateActiveTarget(param.targetScope(), param.teamId());
        validateAttachments(param.attachmentFileIds(), actorMemberId);
        validateAttachedInlineImages(param.body(), param.attachmentFileIds());
        InternalNotice notice = InternalNotice.draft(param.targetScope(), param.teamId(),
                param.title(), param.body(), param.important(), actorMemberId);
        internalNoticeMapper.insert(notice);
        attachFiles(notice.getInternalNoticeId(), param.attachmentFileIds());
        return notice.getInternalNoticeId();
    }

    @Transactional
    public void update(Long actorMemberId, InternalNoticeUpdateParam param) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        InternalNotice original = lock(param.internalNoticeId());
        validateManagement(access, original.getTargetScope(), original.getTeamId());
        validateManagement(access, param.targetScope(), param.teamId());
        validateActiveTarget(param.targetScope(), param.teamId());
        List<Long> existingAttachmentFileIds = internalNoticeMapper
                .searchAttachmentFileIds(param.internalNoticeId());
        validateUpdatedAttachments(param.attachmentFileIds(), existingAttachmentFileIds,
                actorMemberId);
        validateAttachedInlineImages(param.body(), param.attachmentFileIds());
        InternalNotice changed = original.edit(param.targetScope(), param.teamId(),
                param.title(), param.body(), param.important(), actorMemberId);
        internalNoticeMapper.update(changed);
        internalNoticeMapper.removeAttachmentsExcept(param.internalNoticeId(),
                param.attachmentFileIds());
        attachNewFiles(param.internalNoticeId(), param.attachmentFileIds(),
                existingAttachmentFileIds);
    }

    @Transactional
    public void publish(Long actorMemberId, InternalNoticePublishParam param) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        InternalNotice original = lock(param.internalNoticeId());
        validateManagement(access, original.getTargetScope(), original.getTeamId());
        validateActiveTarget(original.getTargetScope(), original.getTeamId());
        InternalNotice changed = original.publish(param.publishStartDttm(),
                param.publishEndDttm(), actorMemberId, LocalDateTime.now(clock));
        internalNoticeMapper.update(changed);
    }

    @Transactional
    public void close(Long actorMemberId, Long internalNoticeId) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        InternalNotice original = lock(internalNoticeId);
        validateManagement(access, original.getTargetScope(), original.getTeamId());
        internalNoticeMapper.update(original.close(actorMemberId));
    }

    @Transactional
    public void archive(Long actorMemberId, Long internalNoticeId) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        InternalNotice original = lock(internalNoticeId);
        validateManagement(access, original.getTargetScope(), original.getTeamId());
        internalNoticeMapper.update(original.archive(actorMemberId));
    }

    @Transactional
    public void returnToDraft(Long actorMemberId, Long internalNoticeId) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        InternalNotice original = lock(internalNoticeId);
        validateManagement(access, original.getTargetScope(), original.getTeamId());
        internalNoticeMapper.update(original.returnToDraft(actorMemberId));
        internalNoticeMapper.removeReads(internalNoticeId);
    }

    @Transactional
    public void deleteDraft(Long actorMemberId, Long internalNoticeId) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        InternalNotice original = lock(internalNoticeId);
        validateManagement(access, original.getTargetScope(), original.getTeamId());
        original.validateDeletable();
        internalNoticeMapper.delete(internalNoticeId, actorMemberId, LocalDateTime.now(clock));
    }

    public PageResponse<InternalNoticeManageSummaryResponse> searchManageable(
            Long actorMemberId, InternalNoticeManageSearchParam param) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        InternalNoticeManageSearchCondition condition;
        if (access.canManageGlobal()) {
            condition = InternalNoticeManageSearchCondition.forAdmin(param);
        } else if (access.canManageTeam(access.teamId())) {
            condition = InternalNoticeManageSearchCondition.forLeader(param, access.teamId());
        } else {
            throw new InternalNoticeAccessDeniedException();
        }
        return PageResponse.of(internalNoticeMapper.searchManageable(condition),
                param.page(), param.pageSize(), internalNoticeMapper.countManageable(condition));
    }

    public InternalNoticeManageDetailResponse lookupManageable(Long actorMemberId,
                                                               Long internalNoticeId) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        InternalNoticeManageContentResponse content = internalNoticeMapper
                .lookupManageContent(internalNoticeId)
                .orElseThrow(() -> new InternalNoticeNotFoundException(internalNoticeId));
        validateManagement(access, content.targetScope(), content.teamId());
        List<InternalNoticeAttachmentResponse> attachments = lookupAttachments(internalNoticeId);
        return InternalNoticeManageDetailResponse.of(content,
                renderManageableMarkdown(content.body(), internalNoticeId, attachments), attachments);
    }

    public PageResponse<InternalNoticeSummaryResponse> searchReadable(
            Long memberId, InternalNoticeSearchParam param) {
        MemberAccessContext access = readableAccess(memberId);
        InternalNoticeReadableSearchCondition condition =
                InternalNoticeReadableSearchCondition.from(param,
                        LocalDateTime.now(clock), memberId, access.teamId(),
                        access.canManageGlobal());
        return PageResponse.of(internalNoticeMapper.searchReadable(condition),
                param.page(), param.pageSize(), internalNoticeMapper.countReadable(condition));
    }

    @Transactional
    public InternalNoticeDetailResponse lookupReadable(Long memberId,
                                                       Long internalNoticeId) {
        MemberAccessContext access = readableAccess(memberId);
        LocalDateTime currentDttm = LocalDateTime.now(clock);
        InternalNoticeContentResponse content = internalNoticeMapper.lookupReadableContent(
                        internalNoticeId, currentDttm, access.teamId(),
                        access.canManageGlobal())
                .orElseThrow(() -> new InternalNoticeNotFoundException(internalNoticeId));
        internalNoticeMapper.upsertRead(internalNoticeId, memberId, currentDttm);
        List<InternalNoticeAttachmentResponse> attachments = lookupAttachments(internalNoticeId);
        return InternalNoticeDetailResponse.of(content,
                renderReadableMarkdown(content.body(), internalNoticeId, attachments),
                canManage(access, content.targetScope(), content.teamId()),
                canIssuePublicShare(access, content),
                internalNoticeMapper.existsShareToken(internalNoticeId), attachments);
    }

    @Transactional
    public String issuePublicShare(Long actorMemberId, Long internalNoticeId) {
        MemberAccessContext access = readableAccess(actorMemberId);
        InternalNotice notice = lock(internalNoticeId);
        requirePublicShareIssuer(access, notice, actorMemberId);
        if (!notice.isPubliclyVisible(LocalDateTime.now(clock))) {
            throw new InvalidInternalNoticeException("share-status");
        }
        return internalNoticeMapper.lookupShareTokenForUpdate(internalNoticeId)
                .orElseGet(() -> createPublicShareToken(internalNoticeId));
    }

    @Transactional
    public void revokePublicShare(Long actorMemberId, Long internalNoticeId) {
        MemberAccessContext access = readableAccess(actorMemberId);
        InternalNotice notice = lock(internalNoticeId);
        requirePublicShareIssuer(access, notice, actorMemberId);
        internalNoticeMapper.updateShareToken(internalNoticeId, null);
    }

    public InternalNoticePublicShareResponse lookupPublicShare(String shareToken) {
        return internalNoticeMapper.lookupPublicShare(shareToken, LocalDateTime.now(clock))
                .orElseThrow(() -> new InternalNoticeNotFoundException(null));
    }

    public FileDownloadResponse openAttachmentDownload(Long memberId, Long internalNoticeId,
                                               Long storedFileId) {
        MemberAccessContext access = readableAccess(memberId);
        boolean readable = internalNoticeMapper.existsReadableAttachment(
                internalNoticeId, storedFileId, LocalDateTime.now(clock),
                access.teamId(), access.canManageGlobal());
        if (!readable) {
            throw new InternalNoticeNotFoundException(internalNoticeId);
        }
        return fileService.openPrivateDownload(
                storedFileId, FileAccessDecision.GRANTED);
    }

    public FileDownloadResponse openAttachmentInline(Long memberId, Long internalNoticeId,
                                                     Long storedFileId) {
        MemberAccessContext access = readableAccess(memberId);
        validateReadableAttachment(internalNoticeId, storedFileId, access);
        return fileService.openPrivateNoticeInlineImage(storedFileId, FileAccessDecision.GRANTED);
    }

    public FileReferenceResponse uploadInlineImage(Long actorMemberId, FileUploadParam param) {
        managementAccess(actorMemberId);
        return fileService.uploadNoticeInlineImage(new FileUploadParam("notice",
                param.originalName(), param.sizeBytes(), param.contentSource(), actorMemberId));
    }

    public FileDownloadResponse openTemporaryInlineImage(Long actorMemberId, Long storedFileId) {
        managementAccess(actorMemberId);
        return fileService.openPrivateNoticeInlineImageOwnedBy(storedFileId, actorMemberId);
    }

    public FileDownloadResponse openManageableAttachmentInline(Long actorMemberId,
                                                                Long internalNoticeId,
                                                                Long storedFileId) {
        validateManageableAttachment(actorMemberId, internalNoticeId, storedFileId);
        return fileService.openPrivateNoticeInlineImage(storedFileId, FileAccessDecision.GRANTED);
    }

    public FileDownloadResponse openManageableAttachmentDownload(Long actorMemberId,
                                                                  Long internalNoticeId,
                                                                  Long storedFileId) {
        validateManageableAttachment(actorMemberId, internalNoticeId, storedFileId);
        return fileService.openPrivateDownload(storedFileId, FileAccessDecision.GRANTED);
    }

    public SafeMarkdownHtml preview(Long actorMemberId, Long internalNoticeId,
                                    String bodyMarkdown, List<Long> attachmentFileIds) {
        MemberAccessContext access = managementAccess(actorMemberId);
        Set<Long> referencedImageIds = extractReferencedImageIds(bodyMarkdown);
        validatePreviewAttachmentReferences(referencedImageIds, attachmentFileIds);
        Map<Long, String> imageUrls = internalNoticeId == null
                ? temporaryImageUrls(actorMemberId, referencedImageIds)
                : previewImageUrls(actorMemberId, access, internalNoticeId,
                        referencedImageIds);
        return markdownRenderer.render(bodyMarkdown, imageUrls);
    }

    public List<InternalNoticeReadStatusResponse> searchReadStatuses(
            Long actorMemberId, Long internalNoticeId) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        InternalNotice notice = internalNoticeMapper.lookupById(internalNoticeId)
                .orElseThrow(() -> new InternalNoticeNotFoundException(internalNoticeId));
        validateReadStatusManagement(access, notice);
        return internalNoticeMapper.searchReadStatuses(internalNoticeId,
                notice.getTargetScope(), notice.getTeamId());
    }

    private InternalNotice lock(Long internalNoticeId) {
        return internalNoticeMapper.lookupByIdForUpdate(internalNoticeId)
                .orElseThrow(() -> new InternalNoticeNotFoundException(internalNoticeId));
    }

    private MemberAccessContext readableAccess(Long memberId) {
        MemberAccessContext access = memberService.lookupAccessContext(memberId);
        if (!access.canReadInternal()) {
            throw new InternalNoticeAccessDeniedException();
        }
        return access;
    }

    private MemberAccessContext managementAccess(Long memberId) {
        MemberAccessContext access = memberService.lookupAccessContext(memberId);
        if (!access.canManageGlobal() && !access.canManageTeam(access.teamId())) {
            throw new InternalNoticeAccessDeniedException();
        }
        return access;
    }

    private void validateManageableAttachment(Long actorMemberId, Long internalNoticeId,
                                              Long storedFileId) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        InternalNotice notice = internalNoticeMapper.lookupById(internalNoticeId)
                .orElseThrow(() -> new InternalNoticeNotFoundException(internalNoticeId));
        validateManagement(access, notice.getTargetScope(), notice.getTeamId());
        if (!internalNoticeMapper.searchAttachmentFileIds(internalNoticeId)
                .contains(storedFileId)) {
            throw new InternalNoticeNotFoundException(internalNoticeId);
        }
    }

    private void validateReadStatusManagement(MemberAccessContext access,
                                              InternalNotice notice) {
        boolean allowed = access.canManageGlobal()
                || (notice.getTargetScope() == InternalNoticeTargetScope.TEAM
                && access.canManageTeam(notice.getTeamId()));
        if (!allowed) {
            throw new InternalNoticeAccessDeniedException();
        }
    }

    private void validateManagement(MemberAccessContext access,
                                    InternalNoticeTargetScope scope, Long teamId) {
        if (!canManage(access, scope, teamId)) {
            throw new InternalNoticeAccessDeniedException();
        }
    }

    private boolean canManage(MemberAccessContext access,
                              InternalNoticeTargetScope scope, Long teamId) {
        return scope == InternalNoticeTargetScope.ALL
                ? access.canManageGlobal()
                : access.canManageTeam(teamId);
    }

    private boolean canIssuePublicShare(MemberAccessContext access,
                                        InternalNoticeContentResponse content) {
        return content.createdByMemberId().equals(access.memberId())
                || access.canManageGlobal()
                || canManage(access, content.targetScope(), content.teamId());
    }

    private void requirePublicShareIssuer(MemberAccessContext access,
                                          InternalNotice notice, Long actorMemberId) {
        if (notice.getCreatedByMemberId().equals(actorMemberId)
                || canManage(access, notice.getTargetScope(), notice.getTeamId())) {
            return;
        }
        throw new InternalNoticeAccessDeniedException();
    }

    private String createPublicShareToken(Long internalNoticeId) {
        String token = shareTokenGenerator.generate();
        internalNoticeMapper.updateShareToken(internalNoticeId, token);
        return token;
    }

    private void validateActiveTarget(InternalNoticeTargetScope scope, Long teamId) {
        if (scope == InternalNoticeTargetScope.TEAM) {
            memberService.validateActiveTeam(teamId);
        }
    }

    private void validateAttachments(List<Long> storedFileIds, Long actorMemberId) {
        if (storedFileIds.stream().anyMatch(fileId -> fileId == null)
                || new HashSet<>(storedFileIds).size() != storedFileIds.size()) {
            throw new InvalidInternalNoticeException("attachments");
        }
        storedFileIds.forEach(storedFileId ->
                fileService.validatePrivateReadyOwnedBy(storedFileId, actorMemberId));
    }

    private void validateAttachedInlineImages(String body, List<Long> attachmentFileIds) {
        Set<Long> referencedImageIds = extractReferencedImageIds(body);
        if (!new HashSet<>(attachmentFileIds).containsAll(referencedImageIds)) {
            throw new InvalidInternalNoticeException("body-image");
        }
        referencedImageIds.forEach(fileService::lookupPrivateNoticeInlineImage);
    }

    private void validateUpdatedAttachments(List<Long> storedFileIds,
                                            List<Long> existingAttachmentFileIds,
                                            Long actorMemberId) {
        if (storedFileIds.stream().anyMatch(fileId -> fileId == null)
                || new HashSet<>(storedFileIds).size() != storedFileIds.size()) {
            throw new InvalidInternalNoticeException("attachments");
        }
        storedFileIds.stream()
                .filter(storedFileId -> !existingAttachmentFileIds.contains(storedFileId))
                .forEach(storedFileId ->
                        fileService.validatePrivateReadyOwnedBy(storedFileId, actorMemberId));
    }

    private void attachFiles(Long internalNoticeId, List<Long> storedFileIds) {
        for (int index = 0; index < storedFileIds.size(); index++) {
            InternalNoticeAttachment attachment = InternalNoticeAttachment.create(
                    internalNoticeId, storedFileIds.get(index), index);
            internalNoticeMapper.insertAttachment(attachment);
        }
    }

    private void attachNewFiles(Long internalNoticeId, List<Long> storedFileIds,
                                List<Long> existingAttachmentFileIds) {
        for (int index = 0; index < storedFileIds.size(); index++) {
            Long storedFileId = storedFileIds.get(index);
            if (!existingAttachmentFileIds.contains(storedFileId)) {
                internalNoticeMapper.insertAttachment(InternalNoticeAttachment.create(
                        internalNoticeId, storedFileId, index));
            }
        }
    }

    private List<InternalNoticeAttachmentResponse> lookupAttachments(Long internalNoticeId) {
        return internalNoticeMapper.searchAttachmentFileIds(internalNoticeId).stream()
                .map(fileService::lookupPrivateReady)
                .map(this::toAttachmentResponse)
                .toList();
    }

    private InternalNoticeAttachmentResponse toAttachmentResponse(FileReferenceResponse file) {
        return new InternalNoticeAttachmentResponse(file.storedFileId(), file.originalName(),
                file.contentType(), file.sizeBytes());
    }

    private void validateReadableAttachment(Long internalNoticeId, Long storedFileId,
                                            MemberAccessContext access) {
        boolean readable = internalNoticeMapper.existsReadableAttachment(
                internalNoticeId, storedFileId, LocalDateTime.now(clock),
                access.teamId(), access.canManageGlobal());
        if (!readable) {
            throw new InternalNoticeNotFoundException(internalNoticeId);
        }
    }

    private SafeMarkdownHtml renderReadableMarkdown(String body, Long internalNoticeId,
                                                     List<InternalNoticeAttachmentResponse> attachments) {
        return markdownRenderer.render(body, attachedImageUrls(attachments,
                storedFileId -> "/api/internal-notices/" + internalNoticeId
                        + "/attachments/" + storedFileId + "/inline"));
    }

    private SafeMarkdownHtml renderManageableMarkdown(String body, Long internalNoticeId,
                                                       List<InternalNoticeAttachmentResponse> attachments) {
        return markdownRenderer.render(body, attachedImageUrls(attachments,
                storedFileId -> "/api/internal-notice-management/" + internalNoticeId
                        + "/attachments/" + storedFileId + "/inline"));
    }

    private Map<Long, String> attachedImageUrls(List<InternalNoticeAttachmentResponse> attachments,
                                                 java.util.function.Function<Long, String> urlFactory) {
        return attachments.stream()
                .filter(this::isInlineImage)
                .collect(Collectors.toMap(InternalNoticeAttachmentResponse::storedFileId,
                        attachment -> urlFactory.apply(attachment.storedFileId())));
    }

    private boolean isInlineImage(InternalNoticeAttachmentResponse attachment) {
        return attachment.sizeBytes() <= INLINE_IMAGE_MAX_BYTES
                && INLINE_IMAGE_CONTENT_TYPES.contains(attachment.contentType());
    }

    private Set<Long> extractReferencedImageIds(String bodyMarkdown) {
        return markdownRenderer.extractAttachmentImageReferences(bodyMarkdown).stream()
                .map(this::parseStoredFileId)
                .collect(Collectors.toUnmodifiableSet());
    }

    private Long parseStoredFileId(String reference) {
        String value = reference.substring("attachment://".length());
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            throw new InvalidInternalNoticeException("body-image");
        }
    }

    private void validatePreviewAttachmentReferences(Set<Long> referencedImageIds,
                                                     List<Long> attachmentFileIds) {
        if (attachmentFileIds == null
                || !new HashSet<>(attachmentFileIds).containsAll(referencedImageIds)) {
            throw new InvalidInternalNoticeException("body-image");
        }
    }

    private Map<Long, String> temporaryImageUrls(Long actorMemberId,
                                                  Set<Long> referencedImageIds) {
        referencedImageIds.forEach(storedFileId ->
                fileService.validatePrivateNoticeInlineImageOwnedBy(storedFileId, actorMemberId));
        return referencedImageIds.stream().collect(Collectors.toMap(storedFileId -> storedFileId,
                storedFileId -> "/api/internal-notice-management/images/" + storedFileId
                        + "/preview"));
    }

    private Map<Long, String> previewImageUrls(Long actorMemberId, MemberAccessContext access,
                                                Long internalNoticeId, Set<Long> referencedImageIds) {
        InternalNotice notice = internalNoticeMapper.lookupById(internalNoticeId)
                .orElseThrow(() -> new InternalNoticeNotFoundException(internalNoticeId));
        validateManagement(access, notice.getTargetScope(), notice.getTeamId());
        Set<Long> existingAttachmentIds = new HashSet<>(internalNoticeMapper
                .searchAttachmentFileIds(internalNoticeId));
        return referencedImageIds.stream().collect(Collectors.toMap(storedFileId -> storedFileId,
                storedFileId -> previewImageUrl(actorMemberId, internalNoticeId,
                        existingAttachmentIds, storedFileId)));
    }

    private String previewImageUrl(Long actorMemberId, Long internalNoticeId,
                                   Set<Long> existingAttachmentIds, Long storedFileId) {
        if (existingAttachmentIds.contains(storedFileId)) {
            fileService.lookupPrivateNoticeInlineImage(storedFileId);
            return "/api/internal-notice-management/" + internalNoticeId
                    + "/attachments/" + storedFileId + "/inline";
        }
        fileService.validatePrivateNoticeInlineImageOwnedBy(storedFileId, actorMemberId);
        return "/api/internal-notice-management/images/" + storedFileId + "/preview";
    }
}
