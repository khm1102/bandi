package kr.ac.tukorea.bandi.domain.notice.dto.response;

public record PublicNoticeAttachmentResponse(
        Long storedFileId,
        String originalName,
        String contentType,
        long sizeBytes
) {
}
