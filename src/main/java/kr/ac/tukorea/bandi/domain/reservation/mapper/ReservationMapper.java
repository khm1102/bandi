package kr.ac.tukorea.bandi.domain.reservation.mapper;

import kr.ac.tukorea.bandi.domain.reservation.model.PerformanceRoundSeat;
import kr.ac.tukorea.bandi.domain.reservation.dto.response.ReservationSeatResponse;
import kr.ac.tukorea.bandi.domain.reservation.dto.response.ReservationMetricsResponse;
import kr.ac.tukorea.bandi.domain.reservation.model.ActiveSeatOccupancy;
import kr.ac.tukorea.bandi.domain.reservation.model.Reservation;
import kr.ac.tukorea.bandi.domain.reservation.model.ReservationSeat;
import kr.ac.tukorea.bandi.domain.reservation.model.ReservationStatusHistory;
import kr.ac.tukorea.bandi.domain.reservation.model.ReservationStatus;
import kr.ac.tukorea.bandi.domain.reservation.model.SeatEntryHistory;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationMapper {

    Optional<PerformanceRoundSeat> lookupRoundSeatForUpdate(
            Long performanceRoundSeatId);

    List<PerformanceRoundSeat> searchRoundSeats(Long performanceRoundId);

    List<PerformanceRoundSeat> searchAvailableRoundSeats(
            Long performanceRoundId);

    List<PerformanceRoundSeat> searchRoundSeatsForUpdate(
            @Param("performanceRoundSeatIds")
            List<Long> performanceRoundSeatIds);

    Optional<Reservation> lookupReservationByLookupTokenHash(
            String lookupTokenHash);

    Optional<Reservation> lookupReservationByLookupTokenHashForUpdate(
            String lookupTokenHash);

    Optional<Reservation> lookupReservationByEntryTokenHashForUpdate(
            String entryTokenHash);

    Optional<Reservation> lookupReservationByIdForUpdate(Long reservationId);

    Optional<Reservation> lookupReservationByNo(String reservationNo);

    Optional<ReservationSeat> lookupReservationSeatForUpdate(
            Long reservationSeatId);

    List<ReservationSeat> searchReservationSeatsForUpdate(
            Long reservationId);

    List<ReservationSeatResponse> searchReservationSeatResponses(
            Long reservationId);

    List<Reservation> searchReservations(
            @Param("performanceRoundId") Long performanceRoundId,
            @Param("status") ReservationStatus status,
            @Param("offset") int offset,
            @Param("limit") int limit);

    ReservationMetricsResponse lookupReservationMetrics(
            Long performanceRoundId);

    List<Reservation> searchPersonalDataEraseTargets(
            @Param("cutoffDttm") LocalDateTime cutoffDttm,
            @Param("limit") int limit);

    int insertRoundSeat(PerformanceRoundSeat seat);

    int updateRoundSeatStatus(PerformanceRoundSeat seat);

    int insertReservation(Reservation reservation);

    int updateReservation(Reservation reservation);

    int insertReservationSeat(ReservationSeat reservationSeat);

    int updateReservationSeat(ReservationSeat reservationSeat);

    int insertActiveSeatOccupancy(ActiveSeatOccupancy occupancy);

    int removeActiveSeatOccupanciesByReservation(Long reservationId);

    int insertReservationStatusHistory(
            ReservationStatusHistory history);

    int insertSeatEntryHistory(SeatEntryHistory history);
}
