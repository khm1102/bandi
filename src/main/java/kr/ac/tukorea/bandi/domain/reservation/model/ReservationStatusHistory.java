package kr.ac.tukorea.bandi.domain.reservation.model;

import kr.ac.tukorea.bandi.domain.reservation.exception.InvalidReservationException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReservationStatusHistory {

    private Long reservationStatusHistoryId;
    private final Long reservationId;
    private final ReservationStatus previousStatus;
    private final ReservationStatus newStatus;
    private final String reason;
    private final Long changedByMemberId;
    private final LocalDateTime changedDttm;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;

    public ReservationStatusHistory(
            Long reservationStatusHistoryId, Long reservationId,
            ReservationStatus previousStatus, ReservationStatus newStatus,
            String reason, Long changedByMemberId,
            LocalDateTime changedDttm, LocalDateTime createdDttm,
            LocalDateTime updatedDttm) {
        this.reservationStatusHistoryId = reservationStatusHistoryId;
        this.reservationId = requireId(reservationId);
        this.previousStatus = previousStatus;
        this.newStatus = requireStatus(newStatus);
        this.reason = requireReason(reason);
        this.changedByMemberId = changedByMemberId;
        this.changedDttm = requireTime(changedDttm);
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
    }

    public static ReservationStatusHistory created(
            Long reservationId, LocalDateTime changedDttm) {
        return new ReservationStatusHistory(null, reservationId, null,
                ReservationStatus.CONFIRMED, "신청 완료", null,
                changedDttm, null, null);
    }

    public static ReservationStatusHistory changed(
            Long reservationId, ReservationStatus previousStatus,
            ReservationStatus newStatus, String reason,
            Long changedByMemberId, LocalDateTime changedDttm) {
        return new ReservationStatusHistory(null, reservationId,
                previousStatus, newStatus, reason, changedByMemberId,
                changedDttm, null, null);
    }

    private static Long requireId(Long value) {
        if (value == null || value < 1) {
            throw new InvalidReservationException("reservationId");
        }
        return value;
    }

    private static ReservationStatus requireStatus(
            ReservationStatus value) {
        if (value == null) {
            throw new InvalidReservationException("newStatus");
        }
        return value;
    }

    private static String requireReason(String value) {
        if (value == null || value.isBlank()
                || value.trim().length() > 500) {
            throw new InvalidReservationException("reason");
        }
        return value.trim();
    }

    private static LocalDateTime requireTime(LocalDateTime value) {
        if (value == null) {
            throw new InvalidReservationException("changedDttm");
        }
        return value;
    }
}
