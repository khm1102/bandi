package kr.ac.tukorea.bandi.domain.reservation.model;

import kr.ac.tukorea.bandi.domain.reservation.exception.InvalidReservationException;
import kr.ac.tukorea.bandi.domain.reservation.exception.InvalidReservationStateException;
import kr.ac.tukorea.bandi.domain.reservation.exception.SeatUnavailableException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PerformanceRoundSeat {

    private Long performanceRoundSeatId;
    private final Long performanceRoundId;
    private final String seatLabel;
    private final String sectionCode;
    private final String rowLabel;
    private final String columnLabel;
    private final Integer displayRow;
    private final Integer displayColumn;
    private final RoundSeatStatus status;
    private final String accessibilityCode;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;

    public PerformanceRoundSeat(
            Long performanceRoundSeatId, Long performanceRoundId,
            String seatLabel, String sectionCode, String rowLabel,
            String columnLabel, Integer displayRow, Integer displayColumn,
            RoundSeatStatus status, String accessibilityCode,
            LocalDateTime createdDttm, LocalDateTime updatedDttm) {
        this.performanceRoundSeatId = performanceRoundSeatId;
        this.performanceRoundId = requireId(
                performanceRoundId, "performanceRoundId");
        this.seatLabel = requireText(seatLabel, "seatLabel", 30);
        this.sectionCode = optionalText(sectionCode, "sectionCode", 30);
        this.rowLabel = optionalText(rowLabel, "rowLabel", 30);
        this.columnLabel = optionalText(columnLabel, "columnLabel", 30);
        this.displayRow = requirePosition(displayRow, "displayRow");
        this.displayColumn = requirePosition(
                displayColumn, "displayColumn");
        this.status = requireStatus(status);
        this.accessibilityCode = optionalText(
                accessibilityCode, "accessibilityCode", 30);
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
    }

    public static PerformanceRoundSeat available(
            Long performanceRoundId, String seatLabel,
            String sectionCode, String rowLabel, String columnLabel,
            Integer displayRow, Integer displayColumn,
            String accessibilityCode) {
        return new PerformanceRoundSeat(null, performanceRoundId,
                seatLabel, sectionCode, rowLabel, columnLabel,
                displayRow, displayColumn, RoundSeatStatus.AVAILABLE,
                accessibilityCode, null, null);
    }

    public PerformanceRoundSeat changeStatus(RoundSeatStatus status) {
        if (status == null || this.status == status) {
            throw new InvalidReservationStateException("roundSeatStatus");
        }
        return copy(status);
    }

    public void validateReservable() {
        if (status != RoundSeatStatus.AVAILABLE) {
            throw new SeatUnavailableException(performanceRoundSeatId);
        }
    }

    public void validateRound(Long performanceRoundId) {
        if (!this.performanceRoundId.equals(performanceRoundId)) {
            throw new InvalidReservationException("performanceRoundId");
        }
    }

    private PerformanceRoundSeat copy(RoundSeatStatus status) {
        return new PerformanceRoundSeat(performanceRoundSeatId,
                performanceRoundId, seatLabel, sectionCode, rowLabel,
                columnLabel, displayRow, displayColumn, status,
                accessibilityCode, createdDttm, updatedDttm);
    }

    private static Long requireId(Long value, String field) {
        if (value == null || value < 1) {
            throw new InvalidReservationException(field);
        }
        return value;
    }

    private static String requireText(
            String value, String field, int maxLength) {
        if (value == null || value.isBlank()
                || value.trim().length() > maxLength) {
            throw new InvalidReservationException(field);
        }
        return value.trim();
    }

    private static String optionalText(
            String value, String field, int maxLength) {
        if (value == null) {
            return null;
        }
        return requireText(value, field, maxLength);
    }

    private static Integer requirePosition(Integer value, String field) {
        if (value != null && value < 0) {
            throw new InvalidReservationException(field);
        }
        return value;
    }

    private static RoundSeatStatus requireStatus(RoundSeatStatus value) {
        if (value == null) {
            throw new InvalidReservationException("status");
        }
        return value;
    }
}
