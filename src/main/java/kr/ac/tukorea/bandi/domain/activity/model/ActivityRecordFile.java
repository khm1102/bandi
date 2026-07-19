package kr.ac.tukorea.bandi.domain.activity.model;

import kr.ac.tukorea.bandi.domain.activity.exception.InvalidActivityRecordException;
import kr.ac.tukorea.bandi.domain.activity.exception.InvalidActivityRecordStateException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ActivityRecordFile {

    private Long activityRecordFileId;
    private final Long activityRecordId;
    private final Long storedFileId;
    private final ActivityFileRole fileRole;
    private final int displayOrder;
    private final Long uploadedByMemberId;
    private final Long replacedByActivityRecordFileId;
    private final LocalDateTime replacedDttm;
    private final Long replacedByMemberId;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;

    public ActivityRecordFile(Long activityRecordFileId, Long activityRecordId,
                              Long storedFileId, ActivityFileRole fileRole,
                              int displayOrder, Long uploadedByMemberId,
                              Long replacedByActivityRecordFileId,
                              LocalDateTime replacedDttm, Long replacedByMemberId,
                              LocalDateTime createdDttm, LocalDateTime updatedDttm) {
        validate(activityRecordId, storedFileId, fileRole, displayOrder,
                uploadedByMemberId, replacedByActivityRecordFileId,
                replacedDttm, replacedByMemberId);
        this.activityRecordFileId = activityRecordFileId;
        this.activityRecordId = activityRecordId;
        this.storedFileId = storedFileId;
        this.fileRole = fileRole;
        this.displayOrder = displayOrder;
        this.uploadedByMemberId = uploadedByMemberId;
        this.replacedByActivityRecordFileId = replacedByActivityRecordFileId;
        this.replacedDttm = replacedDttm;
        this.replacedByMemberId = replacedByMemberId;
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
    }

    public static ActivityRecordFile create(Long activityRecordId,
                                            Long storedFileId,
                                            ActivityFileRole fileRole,
                                            int displayOrder,
                                            Long uploadedByMemberId) {
        return new ActivityRecordFile(null, activityRecordId, storedFileId,
                fileRole, displayOrder, uploadedByMemberId, null,
                null, null, null, null);
    }

    public ActivityRecordFile markReplaced(Long replacementFileId,
                                           Long actorMemberId,
                                           LocalDateTime currentDttm) {
        if (!isCurrent()) {
            throw new InvalidActivityRecordStateException("file-replaced");
        }
        if (replacementFileId == null || actorMemberId == null || currentDttm == null) {
            throw new InvalidActivityRecordException("replacement");
        }
        return new ActivityRecordFile(activityRecordFileId, activityRecordId,
                storedFileId, fileRole, displayOrder, uploadedByMemberId,
                replacementFileId, currentDttm, actorMemberId,
                createdDttm, updatedDttm);
    }

    public boolean isCurrent() {
        return replacedDttm == null;
    }

    private void validate(Long recordId, Long fileId, ActivityFileRole role,
                          int order, Long uploaderId, Long replacementId,
                          LocalDateTime replacementDttm, Long replacerId) {
        if (recordId == null || fileId == null || role == null
                || order < 0 || uploaderId == null) {
            throw new InvalidActivityRecordException("file");
        }
        boolean current = replacementId == null
                && replacementDttm == null && replacerId == null;
        boolean replaced = replacementId != null
                && replacementDttm != null && replacerId != null;
        if (!current && !replaced) {
            throw new InvalidActivityRecordException("replacement-state");
        }
    }
}
