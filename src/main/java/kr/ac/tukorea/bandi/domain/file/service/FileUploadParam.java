package kr.ac.tukorea.bandi.domain.file.service;

public record FileUploadParam(
        String domain,
        String originalName,
        long sizeBytes,
        FileContentSource contentSource,
        Long uploadedByMemberId
) {
}
