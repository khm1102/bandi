package kr.ac.tukorea.bandi.domain.reservation.dto.request;

import java.util.List;

public record ReservationCreateParam(
        Long performanceRoundId,
        List<Long> performanceRoundSeatIds,
        String applicantName,
        String phone,
        Long privacyPolicyVersionId
) {
}
