package kr.ac.tukorea.bandi.domain.notice.dto.response;

import kr.ac.tukorea.bandi.domain.file.dto.response.FileReferenceResponse;

public record NoticeInlineImageResponse(
        Long storedFileId,
        String originalName,
        String contentType,
        long sizeBytes,
        String previewUrl
) {

    public static NoticeInlineImageResponse of(FileReferenceResponse file, String previewUrl) {
        return new NoticeInlineImageResponse(file.storedFileId(), file.originalName(),
                file.contentType(), file.sizeBytes(), previewUrl);
    }
}
