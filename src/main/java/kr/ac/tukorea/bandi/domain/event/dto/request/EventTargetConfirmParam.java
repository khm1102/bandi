package kr.ac.tukorea.bandi.domain.event.dto.request;

import java.util.List;

public record EventTargetConfirmParam(
        Long clubEventId,
        List<Long> selectedMemberIds
) {
}
