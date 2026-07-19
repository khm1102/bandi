package kr.ac.tukorea.bandi.domain.notice.service;

import kr.ac.tukorea.bandi.domain.file.dto.response.FileReferenceResponse;
import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.notice.dto.request.PublicNoticeAdminSearchCondition;
import kr.ac.tukorea.bandi.domain.notice.dto.request.PublicNoticeAdminSearchParam;
import kr.ac.tukorea.bandi.domain.notice.dto.request.PublicNoticePublishParam;
import kr.ac.tukorea.bandi.domain.notice.dto.request.PublicNoticeSearchCondition;
import kr.ac.tukorea.bandi.domain.notice.dto.request.PublicNoticeSearchParam;
import kr.ac.tukorea.bandi.domain.notice.dto.request.PublicNoticeUpdateParam;
import kr.ac.tukorea.bandi.domain.notice.dto.request.PublicNoticeWriteParam;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeAdminContentResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeAdminDetailResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeAdminSummaryResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeAttachmentResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeContentResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeDetailResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeSummaryResponse;
import kr.ac.tukorea.bandi.domain.notice.exception.InvalidPublicNoticeException;
import kr.ac.tukorea.bandi.domain.notice.exception.PublicNoticeAccessDeniedException;
import kr.ac.tukorea.bandi.domain.notice.exception.PublicNoticeNotFoundException;
import kr.ac.tukorea.bandi.domain.notice.mapper.PublicNoticeMapper;
import kr.ac.tukorea.bandi.domain.notice.model.PublicNotice;
import kr.ac.tukorea.bandi.domain.notice.model.PublicNoticeAttachment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicNoticeService {

    private final PublicNoticeMapper publicNoticeMapper;
    private final MemberService memberService;
    private final FileService fileService;
    private final Clock clock;

    public List<PublicNoticeSummaryResponse> searchPublic(PublicNoticeSearchParam param) {
        PublicNoticeSearchCondition condition = PublicNoticeSearchCondition.from(
                param, LocalDateTime.now(clock));
        return publicNoticeMapper.searchPublic(condition);
    }

    public PublicNoticeDetailResponse lookupPublic(Long publicNoticeId) {
        LocalDateTime currentDttm = LocalDateTime.now(clock);
        PublicNoticeContentResponse content = publicNoticeMapper.lookupPublicContent(
                        publicNoticeId, currentDttm)
                .orElseThrow(() -> new PublicNoticeNotFoundException(publicNoticeId));
        List<PublicNoticeAttachmentResponse> attachments = lookupAttachments(publicNoticeId);
        return PublicNoticeDetailResponse.of(content, attachments);
    }

    public List<PublicNoticeAdminSummaryResponse> searchAdmin(
            Long actorMemberId, PublicNoticeAdminSearchParam param) {
        validateAdmin(actorMemberId);
        return publicNoticeMapper.searchAdmin(PublicNoticeAdminSearchCondition.from(param));
    }

    public PublicNoticeAdminDetailResponse lookupAdmin(Long actorMemberId,
                                                       Long publicNoticeId) {
        validateAdmin(actorMemberId);
        PublicNoticeAdminContentResponse content = publicNoticeMapper
                .lookupAdminContent(publicNoticeId)
                .orElseThrow(() -> new PublicNoticeNotFoundException(publicNoticeId));
        List<PublicNoticeAttachmentResponse> attachments = lookupAttachments(publicNoticeId);
        return PublicNoticeAdminDetailResponse.of(content, attachments);
    }

    @Transactional
    public Long createDraft(Long actorMemberId, PublicNoticeWriteParam param) {
        validateAdmin(actorMemberId);
        validateAttachments(param.attachmentFileIds());
        PublicNotice notice = PublicNotice.draft(param.categoryCode(), param.title(),
                param.body(), param.pinned(), actorMemberId);
        publicNoticeMapper.insert(notice);
        attachFiles(notice.getPublicNoticeId(), param.attachmentFileIds());
        return notice.getPublicNoticeId();
    }

    @Transactional
    public void update(Long actorMemberId, PublicNoticeUpdateParam param) {
        validateAdmin(actorMemberId);
        validateAttachments(param.attachmentFileIds());
        PublicNotice original = lock(param.publicNoticeId());
        PublicNotice changed = original.edit(param.categoryCode(), param.title(),
                param.body(), param.pinned(), actorMemberId);
        publicNoticeMapper.update(changed);
        publicNoticeMapper.removeAttachments(param.publicNoticeId());
        attachFiles(param.publicNoticeId(), param.attachmentFileIds());
    }

    @Transactional
    public void publish(Long actorMemberId, PublicNoticePublishParam param) {
        validateAdmin(actorMemberId);
        PublicNotice changed = lock(param.publicNoticeId()).publish(
                param.publishStartDttm(), param.publishEndDttm(), actorMemberId,
                LocalDateTime.now(clock));
        publicNoticeMapper.update(changed);
    }

    @Transactional
    public void close(Long actorMemberId, Long publicNoticeId) {
        validateAdmin(actorMemberId);
        PublicNotice changed = lock(publicNoticeId).close(actorMemberId);
        publicNoticeMapper.update(changed);
    }

    @Transactional
    public void archive(Long actorMemberId, Long publicNoticeId) {
        validateAdmin(actorMemberId);
        PublicNotice changed = lock(publicNoticeId).archive(actorMemberId);
        publicNoticeMapper.update(changed);
    }

    public String createAttachmentDownloadUrl(Long publicNoticeId, Long storedFileId) {
        LocalDateTime currentDttm = LocalDateTime.now(clock);
        if (!publicNoticeMapper.existsPublicAttachment(
                publicNoticeId, storedFileId, currentDttm)) {
            throw new PublicNoticeAccessDeniedException();
        }
        return fileService.createPublicDownloadUrl(storedFileId);
    }

    private PublicNotice lock(Long publicNoticeId) {
        return publicNoticeMapper.lookupByIdForUpdate(publicNoticeId)
                .orElseThrow(() -> new PublicNoticeNotFoundException(publicNoticeId));
    }

    private void validateAdmin(Long actorMemberId) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        if (!access.canManageGlobal()) {
            throw new PublicNoticeAccessDeniedException();
        }
    }

    private void validateAttachments(List<Long> storedFileIds) {
        if (storedFileIds.stream().anyMatch(fileId -> fileId == null)
                || new HashSet<>(storedFileIds).size() != storedFileIds.size()) {
            throw new InvalidPublicNoticeException("attachments");
        }
        storedFileIds.forEach(fileService::lookupPublicReady);
    }

    private void attachFiles(Long publicNoticeId, List<Long> storedFileIds) {
        for (int index = 0; index < storedFileIds.size(); index++) {
            PublicNoticeAttachment attachment = PublicNoticeAttachment.create(
                    publicNoticeId, storedFileIds.get(index), index);
            publicNoticeMapper.insertAttachment(attachment);
        }
    }

    private PublicNoticeAttachmentResponse toAttachmentResponse(FileReferenceResponse file) {
        return new PublicNoticeAttachmentResponse(file.storedFileId(), file.originalName(),
                file.contentType(), file.sizeBytes());
    }

    private List<PublicNoticeAttachmentResponse> lookupAttachments(Long publicNoticeId) {
        return publicNoticeMapper.searchAttachmentFileIds(publicNoticeId).stream()
                .map(fileService::lookupPublicReady)
                .map(this::toAttachmentResponse)
                .toList();
    }
}
