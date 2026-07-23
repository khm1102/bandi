package kr.ac.tukorea.bandi.domain.resource.dto.response;

import java.time.LocalDateTime;

public record ResourceFileResponse(
        Long storedFileId,
        String originalName,
        String contentType,
        long sizeBytes,
        int displayOrder,
        Long uploadedByMemberId,
        String uploadedByName,
        LocalDateTime uploadedDttm
) {
}
