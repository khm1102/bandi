package kr.ac.tukorea.bandi.domain.notice.service;

import kr.ac.tukorea.bandi.domain.file.dto.response.FileReferenceResponse;
import kr.ac.tukorea.bandi.domain.file.service.FileAccessDecision;
import kr.ac.tukorea.bandi.domain.file.service.FileService;
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
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeReadStatusResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeSummaryResponse;
import kr.ac.tukorea.bandi.domain.notice.exception.InternalNoticeAccessDeniedException;
import kr.ac.tukorea.bandi.domain.notice.exception.InternalNoticeNotFoundException;
import kr.ac.tukorea.bandi.domain.notice.exception.InvalidInternalNoticeException;
import kr.ac.tukorea.bandi.domain.notice.mapper.InternalNoticeMapper;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNotice;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeAttachment;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeTargetScope;
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
public class InternalNoticeService {

    private final InternalNoticeMapper internalNoticeMapper;
    private final MemberService memberService;
    private final FileService fileService;
    private final Clock clock;

    @Transactional
    public Long createDraft(Long actorMemberId, InternalNoticeWriteParam param) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        validateManagement(access, param.targetScope(), param.teamId());
        validateActiveTarget(param.targetScope(), param.teamId());
        validateAttachments(param.attachmentFileIds());
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
        validateAttachments(param.attachmentFileIds());
        InternalNotice changed = original.edit(param.targetScope(), param.teamId(),
                param.title(), param.body(), param.important(), actorMemberId);
        internalNoticeMapper.update(changed);
        internalNoticeMapper.removeAttachments(param.internalNoticeId());
        attachFiles(param.internalNoticeId(), param.attachmentFileIds());
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

    public List<InternalNoticeManageSummaryResponse> searchManageable(
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
        return internalNoticeMapper.searchManageable(condition);
    }

    public InternalNoticeManageDetailResponse lookupManageable(Long actorMemberId,
                                                               Long internalNoticeId) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        InternalNoticeManageContentResponse content = internalNoticeMapper
                .lookupManageContent(internalNoticeId)
                .orElseThrow(() -> new InternalNoticeNotFoundException(internalNoticeId));
        validateManagement(access, content.targetScope(), content.teamId());
        return InternalNoticeManageDetailResponse.of(
                content, lookupAttachments(internalNoticeId));
    }

    public List<InternalNoticeSummaryResponse> searchReadable(
            Long memberId, InternalNoticeSearchParam param) {
        MemberAccessContext access = readableAccess(memberId);
        InternalNoticeReadableSearchCondition condition =
                InternalNoticeReadableSearchCondition.from(param,
                        LocalDateTime.now(clock), memberId, access.teamId(),
                        access.canManageGlobal());
        return internalNoticeMapper.searchReadable(condition);
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
        return InternalNoticeDetailResponse.of(content, lookupAttachments(internalNoticeId));
    }

    public String createAttachmentDownloadUrl(Long memberId, Long internalNoticeId,
                                              Long storedFileId) {
        MemberAccessContext access = readableAccess(memberId);
        boolean readable = internalNoticeMapper.existsReadableAttachment(
                internalNoticeId, storedFileId, LocalDateTime.now(clock),
                access.teamId(), access.canManageGlobal());
        if (!readable) {
            throw new InternalNoticeNotFoundException(internalNoticeId);
        }
        return fileService.createPrivateDownloadUrl(
                storedFileId, FileAccessDecision.GRANTED);
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
        boolean allowed = scope == InternalNoticeTargetScope.ALL
                ? access.canManageGlobal()
                : access.canManageTeam(teamId);
        if (!allowed) {
            throw new InternalNoticeAccessDeniedException();
        }
    }

    private void validateActiveTarget(InternalNoticeTargetScope scope, Long teamId) {
        if (scope == InternalNoticeTargetScope.TEAM) {
            memberService.validateActiveTeam(teamId);
        }
    }

    private void validateAttachments(List<Long> storedFileIds) {
        if (storedFileIds.stream().anyMatch(fileId -> fileId == null)
                || new HashSet<>(storedFileIds).size() != storedFileIds.size()) {
            throw new InvalidInternalNoticeException("attachments");
        }
        storedFileIds.forEach(fileService::lookupPrivateReady);
    }

    private void attachFiles(Long internalNoticeId, List<Long> storedFileIds) {
        for (int index = 0; index < storedFileIds.size(); index++) {
            InternalNoticeAttachment attachment = InternalNoticeAttachment.create(
                    internalNoticeId, storedFileIds.get(index), index);
            internalNoticeMapper.insertAttachment(attachment);
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
}
