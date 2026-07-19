package kr.ac.tukorea.bandi.domain.activity.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityFileRole;

public record ActivityFileAddRequest(
        @NotNull @Positive Long storedFileId,
        @NotNull ActivityFileRole fileRole
) {

    public ActivityFileAddParam toParam(Long activityRecordId) {
        return new ActivityFileAddParam(activityRecordId, storedFileId, fileRole);
    }
}
