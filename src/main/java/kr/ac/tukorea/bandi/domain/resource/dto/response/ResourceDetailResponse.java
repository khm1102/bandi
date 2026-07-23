package kr.ac.tukorea.bandi.domain.resource.dto.response;

import kr.ac.tukorea.bandi.domain.notice.service.SafeMarkdownHtml;

import java.time.LocalDateTime;
import java.util.List;

public record ResourceDetailResponse(
        Long resourceId,
        String title,
        String createdByName,
        String updatedByName,
        LocalDateTime createdDttm,
        LocalDateTime updatedDttm,
        String bodyMarkdown,
        SafeMarkdownHtml bodyHtml,
        List<ResourceFileResponse> files,
        List<ResourceLinkPreviewResponse> linkPreviews,
        boolean canManage
) {
}
