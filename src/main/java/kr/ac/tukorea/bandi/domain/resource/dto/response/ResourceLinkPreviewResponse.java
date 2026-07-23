package kr.ac.tukorea.bandi.domain.resource.dto.response;

public record ResourceLinkPreviewResponse(
        String normalizedUrl,
        String domain,
        String title,
        String description,
        Long previewImageFileId
) {
}
