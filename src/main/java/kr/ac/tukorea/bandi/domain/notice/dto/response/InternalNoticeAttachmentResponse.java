package kr.ac.tukorea.bandi.domain.notice.dto.response;

public record InternalNoticeAttachmentResponse(
        Long storedFileId,
        String originalName,
        String contentType,
        long sizeBytes
) {
}
