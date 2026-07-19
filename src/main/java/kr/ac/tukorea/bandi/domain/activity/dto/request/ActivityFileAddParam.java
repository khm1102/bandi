package kr.ac.tukorea.bandi.domain.activity.dto.request;

import kr.ac.tukorea.bandi.domain.activity.model.ActivityFileRole;

public record ActivityFileAddParam(
        Long activityRecordId,
        Long storedFileId,
        ActivityFileRole fileRole
) {
}
