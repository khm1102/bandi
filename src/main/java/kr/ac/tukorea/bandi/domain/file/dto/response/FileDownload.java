package kr.ac.tukorea.bandi.domain.file.dto.response;

import kr.ac.tukorea.bandi.domain.file.exception.FileStorageUnavailableException;
import kr.ac.tukorea.bandi.domain.file.service.FileContentSource;

import java.io.IOException;
import java.io.InputStream;

public record FileDownload(
        String originalName,
        String contentType,
        long sizeBytes,
        FileContentSource contentSource
) {

    public InputStream openStream() {
        try {
            return contentSource.openStream();
        } catch (IOException exception) {
            throw new FileStorageUnavailableException("stream-open-failed");
        }
    }
}
