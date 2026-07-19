package kr.ac.tukorea.bandi.domain.reservation.model;

import kr.ac.tukorea.bandi.domain.reservation.exception.InvalidReservationException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ActiveSeatOccupancy {

    private final Long performanceRoundSeatId;
    private final Long reservationSeatId;
    private final LocalDateTime occupiedDttm;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;

    public ActiveSeatOccupancy(
            Long performanceRoundSeatId, Long reservationSeatId,
            LocalDateTime occupiedDttm, LocalDateTime createdDttm,
            LocalDateTime updatedDttm) {
        this.performanceRoundSeatId = requireId(
                performanceRoundSeatId, "performanceRoundSeatId");
        this.reservationSeatId = requireId(
                reservationSeatId, "reservationSeatId");
        this.occupiedDttm = requireTime(occupiedDttm);
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
    }

    public static ActiveSeatOccupancy occupy(
            Long performanceRoundSeatId, Long reservationSeatId,
            LocalDateTime occupiedDttm) {
        return new ActiveSeatOccupancy(performanceRoundSeatId,
                reservationSeatId, occupiedDttm, null, null);
    }

    private static Long requireId(Long value, String field) {
        if (value == null || value < 1) {
            throw new InvalidReservationException(field);
        }
        return value;
    }

    private static LocalDateTime requireTime(LocalDateTime value) {
        if (value == null) {
            throw new InvalidReservationException("occupiedDttm");
        }
        return value;
    }
}
