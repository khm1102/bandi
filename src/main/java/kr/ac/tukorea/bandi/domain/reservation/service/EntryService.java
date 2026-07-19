package kr.ac.tukorea.bandi.domain.reservation.service;

import kr.ac.tukorea.bandi.domain.performance.service.PerformanceRoundService;
import kr.ac.tukorea.bandi.domain.reservation.dto.response.ReservationDetailResponse;
import kr.ac.tukorea.bandi.domain.reservation.dto.response.ReservationMetricsResponse;
import kr.ac.tukorea.bandi.domain.reservation.dto.response.ReservationSeatResponse;
import kr.ac.tukorea.bandi.domain.reservation.exception.InvalidReservationException;
import kr.ac.tukorea.bandi.domain.reservation.exception.InvalidReservationStateException;
import kr.ac.tukorea.bandi.domain.reservation.exception.InvalidReservationTokenException;
import kr.ac.tukorea.bandi.domain.reservation.exception.ReservationNotFoundException;
import kr.ac.tukorea.bandi.domain.reservation.exception.ReservationSeatNotFoundException;
import kr.ac.tukorea.bandi.domain.reservation.mapper.ReservationMapper;
import kr.ac.tukorea.bandi.domain.reservation.model.Reservation;
import kr.ac.tukorea.bandi.domain.reservation.model.ReservationSeat;
import kr.ac.tukorea.bandi.domain.reservation.model.ReservationStatus;
import kr.ac.tukorea.bandi.domain.reservation.model.SeatEntryHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EntryService {

    private final ReservationMapper reservationMapper;
    private final PerformanceRoundService roundService;
    private final ReservationDataProtector dataProtector;
    private final ReservationTokenGenerator tokenGenerator;
    private final Clock clock;

    @Transactional
    public ReservationDetailResponse lookupByEntryToken(
            Long actorMemberId, Long performanceRoundId,
            String entryToken) {
        validateEntryOpen(actorMemberId, performanceRoundId);
        Reservation reservation = lockByEntryToken(entryToken);
        validateEntryReservation(reservation, performanceRoundId);
        return detail(reservation);
    }

    @Transactional
    public void checkIn(
            Long actorMemberId, Long performanceRoundId,
            String entryToken, List<Long> reservationSeatIds) {
        validateEntryOpen(actorMemberId, performanceRoundId);
        Reservation reservation = lockByEntryToken(entryToken);
        validateEntryReservation(reservation, performanceRoundId);
        checkInReservation(actorMemberId, reservation,
                reservationSeatIds);
    }

    @Transactional
    public void checkInByNumberAndName(
            Long actorMemberId, Long performanceRoundId,
            String reservationNo, String applicantName,
            List<Long> reservationSeatIds) {
        validateEntryOpen(actorMemberId, performanceRoundId);
        Reservation reservation = lookupByNumberAndName(
                reservationNo, applicantName);
        validateEntryReservation(reservation, performanceRoundId);
        checkInReservation(actorMemberId, reservation,
                reservationSeatIds);
    }

    private void checkInReservation(Long actorMemberId,
                                    Reservation reservation,
                                    List<Long> reservationSeatIds) {
        List<Long> selectedIds = validateSeatIds(reservationSeatIds);
        Map<Long, ReservationSeat> byId = reservationMapper
                .searchReservationSeatsForUpdate(
                        reservation.getReservationId()).stream()
                .collect(Collectors.toMap(
                        ReservationSeat::getReservationSeatId,
                        Function.identity()));
        if (!byId.keySet().containsAll(selectedIds)) {
            throw new InvalidReservationTokenException();
        }
        LocalDateTime checkedInDttm = now();
        for (Long reservationSeatId : selectedIds) {
            ReservationSeat current = byId.get(reservationSeatId);
            if (current.isCheckedIn()) {
                continue;
            }
            ReservationSeat checkedIn = current.checkIn(
                    actorMemberId, checkedInDttm);
            reservationMapper.updateReservationSeat(checkedIn);
            reservationMapper.insertSeatEntryHistory(
                    SeatEntryHistory.checkIn(reservationSeatId,
                            actorMemberId, checkedInDttm));
        }
    }

    @Transactional
    public void cancelCheckIn(
            Long actorMemberId, Long performanceRoundId,
            Long reservationSeatId, String reason) {
        validateEntryOpen(actorMemberId, performanceRoundId);
        ReservationSeat current = reservationMapper
                .lookupReservationSeatForUpdate(reservationSeatId)
                .orElseThrow(() -> new ReservationSeatNotFoundException(
                        reservationSeatId));
        Reservation reservation = reservationMapper
                .lookupReservationByIdForUpdate(current.getReservationId())
                .orElseThrow(() -> new ReservationNotFoundException(
                        current.getReservationId()));
        validateEntryReservation(reservation, performanceRoundId);
        LocalDateTime processedDttm = now();
        reservationMapper.updateReservationSeat(current.cancelCheckIn());
        reservationMapper.insertSeatEntryHistory(
                SeatEntryHistory.cancelCheckIn(reservationSeatId,
                        actorMemberId, processedDttm, reason));
    }

    public ReservationDetailResponse lookupByNumberAndName(
            Long actorMemberId, Long performanceRoundId,
            String reservationNo, String applicantName) {
        validateEntryOpen(actorMemberId, performanceRoundId);
        Reservation reservation = reservationMapper
                .lookupReservationByNo(reservationNo)
                .orElseThrow(InvalidReservationTokenException::new);
        validateEntryReservation(reservation, performanceRoundId);
        validateApplicantName(reservation, applicantName);
        return detail(reservation);
    }

    private Reservation lookupByNumberAndName(String reservationNo,
                                               String applicantName) {
        Reservation reservation = reservationMapper
                .lookupReservationByNo(reservationNo)
                .orElseThrow(InvalidReservationTokenException::new);
        validateApplicantName(reservation, applicantName);
        return reservation;
    }

    private void validateApplicantName(Reservation reservation,
                                       String applicantName) {
        String storedName = dataProtector.decryptName(
                reservation.getApplicantNameCiphertext(),
                reservation.getEncryptionKeyVersion());
        if (applicantName == null
                || !storedName.equals(applicantName.trim())) {
            throw new InvalidReservationTokenException();
        }
    }

    public ReservationMetricsResponse lookupMetrics(
            Long actorMemberId, Long performanceProjectId,
            Long performanceRoundId) {
        roundService.validateManage(actorMemberId,
                performanceRoundId, performanceProjectId);
        return reservationMapper.lookupReservationMetrics(
                performanceRoundId);
    }

    private Reservation lockByEntryToken(String entryToken) {
        return reservationMapper.lookupReservationByEntryTokenHashForUpdate(
                        tokenGenerator.hash(entryToken))
                .orElseThrow(InvalidReservationTokenException::new);
    }

    private void validateEntryOpen(
            Long actorMemberId, Long performanceRoundId) {
        if (!roundService.isEntryOpen(
                actorMemberId, performanceRoundId)) {
            throw new InvalidReservationStateException("entryClosed");
        }
    }

    private void validateEntryReservation(
            Reservation reservation, Long performanceRoundId) {
        if (!reservation.getPerformanceRoundId().equals(
                performanceRoundId)) {
            throw new InvalidReservationTokenException();
        }
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new InvalidReservationStateException(
                    "cancelledReservation");
        }
    }

    private List<Long> validateSeatIds(List<Long> values) {
        if (values == null || values.isEmpty()
                || values.stream().anyMatch(
                value -> value == null || value < 1)) {
            throw new InvalidReservationException("reservationSeatIds");
        }
        LinkedHashSet<Long> unique = new LinkedHashSet<>(values);
        if (unique.size() != values.size()) {
            throw new InvalidReservationException(
                    "duplicateReservationSeatIds");
        }
        return List.copyOf(unique);
    }

    private ReservationDetailResponse detail(Reservation reservation) {
        List<ReservationSeatResponse> seats = reservationMapper
                .searchReservationSeatResponses(
                        reservation.getReservationId());
        return new ReservationDetailResponse(
                reservation.getReservationId(),
                reservation.getPerformanceRoundId(),
                reservation.getReservationNo(),
                dataProtector.decryptName(
                        reservation.getApplicantNameCiphertext(),
                        reservation.getEncryptionKeyVersion()),
                dataProtector.decryptPhone(
                        reservation.getPhoneCiphertext(),
                        reservation.getEncryptionKeyVersion()),
                reservation.getStatus(),
                reservation.getPrivacyPolicyVersionId(),
                reservation.getAgreedDttm(),
                reservation.getCancelledDttm(),
                reservation.getCancelReason(), false, seats);
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
