package kr.ac.tukorea.bandi.domain.resource.dto.response;

import java.time.LocalDateTime;

public record ResourceSummaryResponse(
        Long resourceId,
        String title,
        String bodyMarkdown,
        String createdByName,
        LocalDateTime updatedDttm,
        int attachmentCount,
        Long coverStoredFileId,
        String coverImageSource,
        int linkPreviewCount
) {
}
