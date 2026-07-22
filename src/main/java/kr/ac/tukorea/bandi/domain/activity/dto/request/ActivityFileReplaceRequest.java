package kr.ac.tukorea.bandi.domain.activity.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ActivityFileReplaceRequest(
        @NotNull @Positive Long newStoredFileId
) {

    public ActivityFileReplaceParam toParam(Long activityRecordFileId) {
        return new ActivityFileReplaceParam(activityRecordFileId, newStoredFileId);
    }
}
