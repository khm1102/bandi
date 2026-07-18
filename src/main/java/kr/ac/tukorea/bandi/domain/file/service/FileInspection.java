package kr.ac.tukorea.bandi.domain.file.service;

public record FileInspection(
        String contentType,
        long sizeBytes,
        String sha256Hash
) {
}
