package kr.ac.tukorea.bandi.domain.activity.service;

import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityFileAddParam;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityFileReplaceParam;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityManageSearchCondition;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityManageSearchParam;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityRecordSearchCondition;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityRecordSearchParam;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityRecordUpdateParam;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityRecordWriteParam;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityFileLinkResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityFileResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityRecordContentResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityRecordDetailResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityRecordManageContentResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityRecordManageDetailResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityRecordSummaryResponse;
import kr.ac.tukorea.bandi.domain.activity.exception.ActivityRecordAccessDeniedException;
import kr.ac.tukorea.bandi.domain.activity.exception.ActivityRecordFileNotFoundException;
import kr.ac.tukorea.bandi.domain.activity.exception.ActivityRecordNotFoundException;
import kr.ac.tukorea.bandi.domain.activity.exception.InvalidActivityRecordException;
import kr.ac.tukorea.bandi.domain.activity.mapper.ActivityRecordMapper;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityFileRole;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecord;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecordFile;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecordRevision;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecordStatus;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityReviewHistory;
import kr.ac.tukorea.bandi.domain.file.dto.response.FileReferenceResponse;
import kr.ac.tukorea.bandi.domain.file.service.FileAccessDecision;
import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.global.response.FileDownloadResponse;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityRecordService {

    private final ActivityRecordMapper activityRecordMapper;
    private final MemberService memberService;
    private final FileService fileService;
    private final Clock clock;

    @Transactional
    public Long createDraft(Long actorMemberId, ActivityRecordWriteParam param) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        validateCreate(access, param.teamId());
        memberService.validateActiveTeam(param.teamId());
        ActivityRecord record = ActivityRecord.draft(param.teamId(),
                param.activityDttm(), param.title(), param.body(),
                param.participantCount(), actorMemberId);
        activityRecordMapper.insert(record);
        return record.getActivityRecordId();
    }

    @Transactional
    public void update(Long actorMemberId, ActivityRecordUpdateParam param) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        ActivityRecord record = lock(param.activityRecordId());
        validateEdit(access, record, actorMemberId);
        activityRecordMapper.update(record.edit(param.activityDttm(), param.title(),
                param.body(), param.participantCount(), actorMemberId));
    }

    @Transactional
    public void addFile(Long actorMemberId, ActivityFileAddParam param) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        ActivityRecord record = lock(param.activityRecordId());
        validateEdit(access, record, actorMemberId);
        record.validateFileChange();
        validateOwnedImage(param.storedFileId(), actorMemberId);
        validateCurrentDuplicate(param.activityRecordId(), param.storedFileId());
        int displayOrder = activityRecordMapper.lookupNextDisplayOrder(
                param.activityRecordId(), param.fileRole());
        activityRecordMapper.insertFile(ActivityRecordFile.create(
                param.activityRecordId(), param.storedFileId(), param.fileRole(),
                displayOrder, actorMemberId));
    }

    @Transactional
    public void replaceFile(Long actorMemberId, ActivityFileReplaceParam param) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        ActivityRecordFile original = activityRecordMapper
                .lookupFileByIdForUpdate(param.activityRecordFileId())
                .orElseThrow(() -> new ActivityRecordFileNotFoundException(
                        param.activityRecordFileId()));
        ActivityRecord record = lock(original.getActivityRecordId());
        validateEdit(access, record, actorMemberId);
        record.validateFileChange();
        validateOwnedImage(param.newStoredFileId(), actorMemberId);
        validateCurrentDuplicate(record.getActivityRecordId(), param.newStoredFileId());
        ActivityRecordFile replacement = ActivityRecordFile.create(
                original.getActivityRecordId(), param.newStoredFileId(),
                original.getFileRole(), original.getDisplayOrder(), actorMemberId);
        activityRecordMapper.insertFile(replacement);
        activityRecordMapper.updateFile(original.markReplaced(
                replacement.getActivityRecordFileId(), actorMemberId,
                LocalDateTime.now(clock)));
    }

    @Transactional
    public int submit(Long actorMemberId, Long activityRecordId, String changeReason) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        ActivityRecord record = lock(activityRecordId);
        validateEdit(access, record, actorMemberId);
        if (activityRecordMapper.countCurrentFiles(
                activityRecordId, ActivityFileRole.EVIDENCE) < 1) {
            throw new InvalidActivityRecordException("evidence");
        }
        int maxRevisionNo = activityRecordMapper.lookupMaxRevisionNo(activityRecordId)
                .orElse(0);
        if (maxRevisionNo == Integer.MAX_VALUE) {
            throw new InvalidActivityRecordException("revision-limit");
        }
        int revisionNo = maxRevisionNo + 1;
        LocalDateTime currentDttm = LocalDateTime.now(clock);
        ActivityRecord changed = record.submit(actorMemberId, currentDttm);
        activityRecordMapper.insertRevision(ActivityRecordRevision.snapshot(
                record, revisionNo, actorMemberId, currentDttm, changeReason));
        activityRecordMapper.update(changed);
        activityRecordMapper.insertReviewHistory(ActivityReviewHistory.change(
                activityRecordId, record.getStatus(), changed.getStatus(), null,
                actorMemberId, currentDttm));
        return revisionNo;
    }

    @Transactional
    public void approve(Long actorMemberId, Long activityRecordId) {
        review(actorMemberId, activityRecordId, null, true);
    }

    @Transactional
    public void requestRevision(Long actorMemberId, Long activityRecordId,
                                String comment) {
        if (comment == null || comment.isBlank()) {
            throw new InvalidActivityRecordException("review-comment");
        }
        review(actorMemberId, activityRecordId, comment, false);
    }

    @Transactional
    public void archive(Long actorMemberId, Long activityRecordId) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        ActivityRecord record = lock(activityRecordId);
        validateReview(access, record);
        ActivityRecord changed = record.archive(actorMemberId);
        LocalDateTime currentDttm = LocalDateTime.now(clock);
        activityRecordMapper.update(changed);
        activityRecordMapper.insertReviewHistory(ActivityReviewHistory.change(
                activityRecordId, record.getStatus(), changed.getStatus(), null,
                actorMemberId, currentDttm));
    }

    public List<ActivityRecordSummaryResponse> searchApproved(
            Long memberId, ActivityRecordSearchParam param) {
        readableAccess(memberId);
        return activityRecordMapper.searchApproved(
                ActivityRecordSearchCondition.from(param));
    }

    public ActivityRecordDetailResponse lookupApproved(Long memberId,
                                                       Long activityRecordId) {
        readableAccess(memberId);
        ActivityRecordContentResponse content = activityRecordMapper
                .lookupApprovedContent(activityRecordId)
                .orElseThrow(() -> new ActivityRecordNotFoundException(activityRecordId));
        return ActivityRecordDetailResponse.of(content,
                toFiles(activityRecordMapper.searchCurrentFileLinks(activityRecordId)));
    }

    public FileDownloadResponse openApprovedDownload(Long memberId, Long activityRecordId,
                                             Long storedFileId) {
        readableAccess(memberId);
        if (!activityRecordMapper.existsApprovedCurrentFile(
                activityRecordId, storedFileId)) {
            throw new ActivityRecordNotFoundException(activityRecordId);
        }
        return fileService.openPrivateDownload(
                storedFileId, FileAccessDecision.GRANTED);
    }

    public List<ActivityRecordSummaryResponse> searchManageable(
            Long actorMemberId, ActivityManageSearchParam param) {
        MemberAccessContext access = readableAccess(actorMemberId);
        ActivityManageSearchCondition condition;
        if (access.canManageGlobal()) {
            condition = ActivityManageSearchCondition.forAdmin(param);
        } else if (access.canManageTeam(access.teamId())) {
            condition = ActivityManageSearchCondition.forLeader(param, access.teamId());
        } else {
            condition = ActivityManageSearchCondition.forMember(param, actorMemberId);
        }
        return activityRecordMapper.searchManageable(condition);
    }

    public ActivityRecordManageDetailResponse lookupManageable(
            Long actorMemberId, Long activityRecordId) {
        MemberAccessContext access = readableAccess(actorMemberId);
        ActivityRecordManageContentResponse content = activityRecordMapper
                .lookupManageContent(activityRecordId)
                .orElseThrow(() -> new ActivityRecordNotFoundException(activityRecordId));
        validateManageContent(access, content, actorMemberId);
        return ActivityRecordManageDetailResponse.of(content,
                toFiles(activityRecordMapper.searchCurrentFileLinks(activityRecordId)),
                activityRecordMapper.searchRevisions(activityRecordId),
                activityRecordMapper.searchReviewHistories(activityRecordId));
    }

    public FileDownloadResponse openManageableDownload(Long actorMemberId,
                                               Long activityRecordId,
                                               Long storedFileId) {
        MemberAccessContext access = readableAccess(actorMemberId);
        ActivityRecordManageContentResponse content = activityRecordMapper
                .lookupManageContent(activityRecordId)
                .orElseThrow(() -> new ActivityRecordNotFoundException(
                        activityRecordId));
        validateManageContent(access, content, actorMemberId);
        if (!activityRecordMapper.existsCurrentStoredFile(
                activityRecordId, storedFileId)) {
            throw new ActivityRecordFileNotFoundException(storedFileId);
        }
        return fileService.openPrivateDownload(
                storedFileId, FileAccessDecision.GRANTED);
    }

    private void review(Long actorMemberId, Long activityRecordId,
                        String comment, boolean approve) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        ActivityRecord record = lock(activityRecordId);
        validateReview(access, record);
        LocalDateTime currentDttm = LocalDateTime.now(clock);
        ActivityRecord changed = approve
                ? record.approve(actorMemberId, currentDttm)
                : record.requestRevision(actorMemberId, currentDttm);
        activityRecordMapper.update(changed);
        activityRecordMapper.insertReviewHistory(ActivityReviewHistory.change(
                activityRecordId, record.getStatus(), changed.getStatus(),
                comment, actorMemberId, currentDttm));
    }

    private ActivityRecord lock(Long activityRecordId) {
        return activityRecordMapper.lookupByIdForUpdate(activityRecordId)
                .orElseThrow(() -> new ActivityRecordNotFoundException(activityRecordId));
    }

    private MemberAccessContext readableAccess(Long memberId) {
        MemberAccessContext access = memberService.lookupAccessContext(memberId);
        if (!access.canReadInternal()) {
            throw new ActivityRecordAccessDeniedException();
        }
        return access;
    }

    private void validateCreate(MemberAccessContext access, Long teamId) {
        boolean allowed = access.canManageGlobal()
                || (access.canReadInternal() && Objects.equals(access.teamId(), teamId));
        if (!allowed) {
            throw new ActivityRecordAccessDeniedException();
        }
    }

    private void validateEdit(MemberAccessContext access, ActivityRecord record,
                              Long actorMemberId) {
        boolean author = access.canReadInternal()
                && Objects.equals(access.teamId(), record.getTeamId())
                && Objects.equals(record.getCreatedByMemberId(), actorMemberId);
        if (!access.canManageTeam(record.getTeamId()) && !author) {
            throw new ActivityRecordAccessDeniedException();
        }
    }

    private void validateReview(MemberAccessContext access, ActivityRecord record) {
        if (!access.canManageTeam(record.getTeamId())) {
            throw new ActivityRecordAccessDeniedException();
        }
    }

    private void validateManageContent(MemberAccessContext access,
                                       ActivityRecordManageContentResponse content,
                                       Long actorMemberId) {
        boolean author = Objects.equals(content.createdByMemberId(), actorMemberId)
                && Objects.equals(access.teamId(), content.teamId());
        if (!access.canManageTeam(content.teamId()) && !author) {
            throw new ActivityRecordAccessDeniedException();
        }
    }

    private void validateOwnedImage(Long storedFileId, Long actorMemberId) {
        FileReferenceResponse file = fileService.lookupPrivateReady(storedFileId);
        if (!Objects.equals(file.uploadedByMemberId(), actorMemberId)) {
            throw new ActivityRecordAccessDeniedException();
        }
        if (file.contentType() == null || !file.contentType().startsWith("image/")) {
            throw new InvalidActivityRecordException("image");
        }
    }

    private void validateCurrentDuplicate(Long activityRecordId, Long storedFileId) {
        if (activityRecordMapper.existsCurrentStoredFile(activityRecordId, storedFileId)) {
            throw new InvalidActivityRecordException("duplicate-current-file");
        }
    }

    private List<ActivityFileResponse> toFiles(List<ActivityFileLinkResponse> links) {
        return links.stream().map(this::toFile).toList();
    }

    private ActivityFileResponse toFile(ActivityFileLinkResponse link) {
        FileReferenceResponse file = fileService.lookupPrivateReady(link.storedFileId());
        return new ActivityFileResponse(link.activityRecordFileId(), file.storedFileId(),
                file.originalName(), file.contentType(), file.sizeBytes(), link.fileRole(),
                link.displayOrder(), link.uploadedByMemberId(),
                link.uploadedByName(), link.uploadedDttm());
    }
}
