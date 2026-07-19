package kr.ac.tukorea.bandi.domain.resource.dto.response;

import java.time.LocalDateTime;

public record ResourceFileLinkResponse(
        Long storedFileId,
        int revisionNo,
        int displayOrder,
        Long uploadedByMemberId,
        String uploadedByName,
        LocalDateTime uploadedDttm
) {
}
