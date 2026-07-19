package kr.ac.tukorea.bandi.domain.activity.dto.response;

import kr.ac.tukorea.bandi.domain.activity.model.ActivityFileRole;

import java.time.LocalDateTime;

public record ActivityFileLinkResponse(
        Long activityRecordFileId,
        Long storedFileId,
        ActivityFileRole fileRole,
        int displayOrder,
        Long uploadedByMemberId,
        String uploadedByName,
        LocalDateTime uploadedDttm
) {
}
