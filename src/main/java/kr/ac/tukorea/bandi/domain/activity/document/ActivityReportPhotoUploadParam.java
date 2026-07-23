package kr.ac.tukorea.bandi.domain.activity.document;

import org.springframework.core.io.InputStreamSource;

public record ActivityReportPhotoUploadParam(
        long size,
        String contentType,
        InputStreamSource source
) {
}
