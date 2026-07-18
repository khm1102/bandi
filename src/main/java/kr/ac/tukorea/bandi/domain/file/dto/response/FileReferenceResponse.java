package kr.ac.tukorea.bandi.domain.file.dto.response;

import kr.ac.tukorea.bandi.domain.file.model.StoredFile;

public record FileReferenceResponse(
        Long storedFileId,
        String originalName,
        String contentType,
        long sizeBytes
) {

    public static FileReferenceResponse from(StoredFile file) {
        return new FileReferenceResponse(file.getStoredFileId(), file.getOriginalName(),
                file.getContentType(), file.getSizeBytes());
    }
}
