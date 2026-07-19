package kr.ac.tukorea.bandi.domain.activity.dto.request;

public record ActivityFileReplaceParam(
        Long activityRecordFileId,
        Long newStoredFileId
) {
}
