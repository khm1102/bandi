package kr.ac.tukorea.bandi.domain.reservation.dto.response;

import java.time.LocalDateTime;

public record PublicReservationContextResponse(
        Long performanceProjectId,
        String performanceTitle,
        String performanceSlug,
        String place,
        int roundNo,
        LocalDateTime startDttm,
        LocalDateTime entryStartDttm
) {
}
