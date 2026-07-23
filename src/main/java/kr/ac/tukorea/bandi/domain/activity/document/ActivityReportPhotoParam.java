package kr.ac.tukorea.bandi.domain.activity.document;

public record ActivityReportPhotoParam(
        byte[] bytes,
        String contentType
) {

    public ActivityReportPhotoParam {
        bytes = bytes == null ? null : bytes.clone();
    }

    @Override
    public byte[] bytes() {
        return bytes == null ? null : bytes.clone();
    }
}
