package kr.ac.tukorea.bandi.domain.activity.dto.response;

import kr.ac.tukorea.bandi.domain.activity.model.ActivityFileRole;

import java.time.LocalDateTime;

public record ActivityFileResponse(
        Long activityRecordFileId,
        Long storedFileId,
        String originalName,
        String contentType,
        long sizeBytes,
        ActivityFileRole fileRole,
        int displayOrder,
        Long uploadedByMemberId,
        String uploadedByName,
        LocalDateTime uploadedDttm
) {
}
