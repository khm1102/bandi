package kr.ac.tukorea.bandi.domain.reservation.model;

import kr.ac.tukorea.bandi.domain.reservation.exception.InvalidReservationException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SeatEntryHistory {

    private Long seatEntryHistoryId;
    private final Long reservationSeatId;
    private final SeatEntryAction action;
    private final Long processedByMemberId;
    private final LocalDateTime processedDttm;
    private final String reason;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;

    public SeatEntryHistory(
            Long seatEntryHistoryId, Long reservationSeatId,
            SeatEntryAction action, Long processedByMemberId,
            LocalDateTime processedDttm, String reason,
            LocalDateTime createdDttm, LocalDateTime updatedDttm) {
        this.seatEntryHistoryId = seatEntryHistoryId;
        this.reservationSeatId = requireId(
                reservationSeatId, "reservationSeatId");
        this.action = requireAction(action);
        this.processedByMemberId = requireId(
                processedByMemberId, "processedByMemberId");
        this.processedDttm = requireTime(processedDttm);
        this.reason = validateReason(action, reason);
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
    }

    public static SeatEntryHistory checkIn(
            Long reservationSeatId, Long processedByMemberId,
            LocalDateTime processedDttm) {
        return new SeatEntryHistory(null, reservationSeatId,
                SeatEntryAction.CHECK_IN, processedByMemberId,
                processedDttm, null, null, null);
    }

    public static SeatEntryHistory cancelCheckIn(
            Long reservationSeatId, Long processedByMemberId,
            LocalDateTime processedDttm, String reason) {
        return new SeatEntryHistory(null, reservationSeatId,
                SeatEntryAction.CANCEL_CHECK_IN, processedByMemberId,
                processedDttm, reason, null, null);
    }

    private static Long requireId(Long value, String field) {
        if (value == null || value < 1) {
            throw new InvalidReservationException(field);
        }
        return value;
    }

    private static SeatEntryAction requireAction(SeatEntryAction value) {
        if (value == null) {
            throw new InvalidReservationException("action");
        }
        return value;
    }

    private static LocalDateTime requireTime(LocalDateTime value) {
        if (value == null) {
            throw new InvalidReservationException("processedDttm");
        }
        return value;
    }

    private static String validateReason(
            SeatEntryAction action, String value) {
        if (action == SeatEntryAction.CHECK_IN) {
            if (value != null) {
                throw new InvalidReservationException("reason");
            }
            return value;
        }
        if (value == null || value.isBlank()
                || value.trim().length() > 500) {
            throw new InvalidReservationException("reason");
        }
        return value.trim();
    }
}
