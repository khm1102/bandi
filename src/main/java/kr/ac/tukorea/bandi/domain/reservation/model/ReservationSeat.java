package kr.ac.tukorea.bandi.domain.reservation.model;

import kr.ac.tukorea.bandi.domain.reservation.exception.InvalidReservationException;
import kr.ac.tukorea.bandi.domain.reservation.exception.InvalidReservationStateException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReservationSeat {

    private Long reservationSeatId;
    private final Long reservationId;
    private final Long performanceRoundSeatId;
    private final ReservationSeatStatus status;
    private final LocalDateTime cancelledDttm;
    private final String cancelReason;
    private final LocalDateTime checkedInDttm;
    private final Long checkedInByMemberId;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;

    public ReservationSeat(
            Long reservationSeatId, Long reservationId,
            Long performanceRoundSeatId, ReservationSeatStatus status,
            LocalDateTime cancelledDttm, String cancelReason,
            LocalDateTime checkedInDttm, Long checkedInByMemberId,
            LocalDateTime createdDttm, LocalDateTime updatedDttm) {
        this.reservationSeatId = reservationSeatId;
        this.reservationId = requireId(reservationId, "reservationId");
        this.performanceRoundSeatId = requireId(
                performanceRoundSeatId, "performanceRoundSeatId");
        this.status = requireStatus(status);
        this.cancelledDttm = cancelledDttm;
        this.cancelReason = cancelReason;
        this.checkedInDttm = checkedInDttm;
        this.checkedInByMemberId = checkedInByMemberId;
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
        validateState();
    }

    public static ReservationSeat confirmed(
            Long reservationId, Long performanceRoundSeatId) {
        return new ReservationSeat(null, reservationId,
                performanceRoundSeatId, ReservationSeatStatus.CONFIRMED,
                null, null, null, null, null, null);
    }

    public ReservationSeat checkIn(
            Long memberId, LocalDateTime checkedInDttm) {
        if (status == ReservationSeatStatus.CANCELLED) {
            throw new InvalidReservationStateException("cancelledSeat");
        }
        if (isCheckedIn()) {
            return this;
        }
        return copy(status, cancelledDttm, cancelReason,
                requireTime(checkedInDttm, "checkedInDttm"),
                requireId(memberId, "checkedInByMemberId"));
    }

    public ReservationSeat cancelCheckIn() {
        if (!isCheckedIn()) {
            throw new InvalidReservationStateException("notCheckedIn");
        }
        return copy(status, cancelledDttm, cancelReason, null, null);
    }

    public ReservationSeat cancel(
            String reason, LocalDateTime cancelledDttm) {
        if (status == ReservationSeatStatus.CANCELLED || isCheckedIn()) {
            throw new InvalidReservationStateException("seatCancellation");
        }
        return copy(ReservationSeatStatus.CANCELLED,
                requireTime(cancelledDttm, "cancelledDttm"),
                requireText(reason, "cancelReason", 500), null, null);
    }

    public boolean isCheckedIn() {
        return checkedInDttm != null;
    }

    private ReservationSeat copy(
            ReservationSeatStatus status, LocalDateTime cancelledDttm,
            String cancelReason, LocalDateTime checkedInDttm,
            Long checkedInByMemberId) {
        return new ReservationSeat(reservationSeatId, reservationId,
                performanceRoundSeatId, status, cancelledDttm,
                cancelReason, checkedInDttm, checkedInByMemberId,
                createdDttm, updatedDttm);
    }

    private void validateState() {
        if ((checkedInDttm == null) != (checkedInByMemberId == null)) {
            throw new InvalidReservationException("checkInState");
        }
        if (status == ReservationSeatStatus.CANCELLED
                && (cancelledDttm == null || cancelReason == null
                || cancelReason.isBlank()
                || cancelReason.trim().length() > 500)) {
            throw new InvalidReservationException("cancellation");
        }
        if (status == ReservationSeatStatus.CONFIRMED
                && (cancelledDttm != null || cancelReason != null)) {
            throw new InvalidReservationException("cancellation");
        }
        if (status == ReservationSeatStatus.CANCELLED && isCheckedIn()) {
            throw new InvalidReservationException("cancelledCheckedInSeat");
        }
    }

    private static Long requireId(Long value, String field) {
        if (value == null || value < 1) {
            throw new InvalidReservationException(field);
        }
        return value;
    }

    private static ReservationSeatStatus requireStatus(
            ReservationSeatStatus value) {
        if (value == null) {
            throw new InvalidReservationException("status");
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

    private static LocalDateTime requireTime(
            LocalDateTime value, String field) {
        if (value == null) {
            throw new InvalidReservationException(field);
        }
        return value;
    }
}
