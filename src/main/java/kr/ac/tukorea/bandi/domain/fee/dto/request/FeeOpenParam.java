package kr.ac.tukorea.bandi.domain.fee.dto.request;

import java.util.List;

public record FeeOpenParam(
        Long feeItemId,
        List<Long> selectedMemberIds
) {
}
