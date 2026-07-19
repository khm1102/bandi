package kr.ac.tukorea.bandi.domain.reservation.dto.request;

public record RoundSeatCreateParam(
        Long performanceProjectId,
        Long performanceRoundId,
        String seatLabel,
        String sectionCode,
        String rowLabel,
        String columnLabel,
        Integer displayRow,
        Integer displayColumn,
        String accessibilityCode
) {
}
