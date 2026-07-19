package kr.ac.tukorea.bandi.domain.reservation.service;

import kr.ac.tukorea.bandi.domain.performance.service.PerformanceRoundService;
import kr.ac.tukorea.bandi.domain.policy.service.PolicyService;
import kr.ac.tukorea.bandi.domain.reservation.dto.request.ReservationCreateParam;
import kr.ac.tukorea.bandi.domain.reservation.dto.response.ReservationCreatedResponse;
import kr.ac.tukorea.bandi.domain.reservation.dto.response.ReservationDetailResponse;
import kr.ac.tukorea.bandi.domain.reservation.dto.response.ReservationSeatResponse;
import kr.ac.tukorea.bandi.domain.reservation.exception.InvalidReservationException;
import kr.ac.tukorea.bandi.domain.reservation.exception.InvalidReservationStateException;
import kr.ac.tukorea.bandi.domain.reservation.exception.InvalidReservationTokenException;
import kr.ac.tukorea.bandi.domain.reservation.exception.ReservationNotFoundException;
import kr.ac.tukorea.bandi.domain.reservation.exception.RoundSeatNotFoundException;
import kr.ac.tukorea.bandi.domain.reservation.exception.SeatUnavailableException;
import kr.ac.tukorea.bandi.domain.reservation.mapper.ReservationMapper;
import kr.ac.tukorea.bandi.domain.reservation.model.ActiveSeatOccupancy;
import kr.ac.tukorea.bandi.domain.reservation.model.PerformanceRoundSeat;
import kr.ac.tukorea.bandi.domain.reservation.model.Reservation;
import kr.ac.tukorea.bandi.domain.reservation.model.ReservationSeat;
import kr.ac.tukorea.bandi.domain.reservation.model.ReservationStatus;
import kr.ac.tukorea.bandi.domain.reservation.model.ReservationStatusHistory;
import kr.ac.tukorea.bandi.global.config.ReservationSecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
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
public class ReservationService {

    private static final int MAX_ERASE_BATCH_SIZE = 1_000;

    private final ReservationMapper reservationMapper;
    private final PerformanceRoundService roundService;
    private final PolicyService policyService;
    private final ReservationDataProtector dataProtector;
    private final ReservationTokenGenerator tokenGenerator;
    private final ReservationSecurityProperties securityProperties;
    private final Clock clock;

    @Transactional
    public ReservationCreatedResponse create(
            String slug, ReservationCreateParam param) {
        List<Long> seatIds = validateSeatIds(
                param.performanceRoundSeatIds());
        if (!roundService.isPublicReservationOpen(
                slug, param.performanceRoundId(), now())) {
            throw new InvalidReservationStateException(
                    "reservationClosed");
        }
        List<PerformanceRoundSeat> seats = lockSeats(
                param.performanceRoundId(), seatIds);
        policyService.validateReservationPrivacyVersion(
                param.privacyPolicyVersionId());
        ProtectedApplicant applicant = dataProtector.protect(
                param.applicantName(), param.phone());
        ReservationCredentials credentials = tokenGenerator.generate();
        LocalDateTime createdDttm = now();
        Reservation reservation = Reservation.confirm(
                param.performanceRoundId(), credentials.reservationNo(),
                credentials.lookupTokenHash(),
                credentials.entryTokenHash(),
                applicant.applicantNameCiphertext(),
                applicant.phoneCiphertext(),
                applicant.phoneSearchHash(),
                applicant.encryptionKeyVersion(),
                param.privacyPolicyVersionId(), createdDttm);
        reservationMapper.insertReservation(reservation);
        occupySeats(reservation, seats, createdDttm);
        reservationMapper.insertReservationStatusHistory(
                ReservationStatusHistory.created(
                        reservation.getReservationId(), createdDttm));
        return new ReservationCreatedResponse(
                reservation.getReservationId(),
                reservation.getReservationNo(),
                credentials.lookupToken(), credentials.entryToken());
    }

    public ReservationDetailResponse lookup(String lookupToken) {
        Reservation reservation = reservationMapper
                .lookupReservationByLookupTokenHash(
                        tokenGenerator.hash(lookupToken))
                .orElseThrow(InvalidReservationTokenException::new);
        List<ReservationSeatResponse> seats = reservationMapper
                .searchReservationSeatResponses(
                        reservation.getReservationId());
        boolean cancellationOpen = roundService
                .isViewerCancellationOpen(
                        reservation.getPerformanceRoundId());
        return detail(reservation, seats,
                isCancelable(reservation, seats, cancellationOpen));
    }

    @Transactional
    public void cancel(String lookupToken, String reason) {
        Reservation current = reservationMapper
                .lookupReservationByLookupTokenHashForUpdate(
                        tokenGenerator.hash(lookupToken))
                .orElseThrow(InvalidReservationTokenException::new);
        List<ReservationSeat> seats = reservationMapper
                .searchReservationSeatsForUpdate(
                        current.getReservationId());
        validateViewerCancellation(current, seats,
                roundService.isViewerCancellationOpen(
                        current.getPerformanceRoundId()));
        cancelReservation(current, seats, reason, null);
    }

    @Transactional
    public void cancelByAdmin(
            Long actorMemberId, Long performanceProjectId,
            Long reservationId, String reason) {
        Reservation current = reservationMapper
                .lookupReservationByIdForUpdate(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(
                        reservationId));
        roundService.validateManage(actorMemberId,
                current.getPerformanceRoundId(), performanceProjectId);
        List<ReservationSeat> seats = reservationMapper
                .searchReservationSeatsForUpdate(reservationId);
        if (seats.stream().anyMatch(ReservationSeat::isCheckedIn)) {
            throw new InvalidReservationStateException(
                    "checkedInReservation");
        }
        cancelReservation(current, seats, reason, actorMemberId);
    }

    @Transactional
    public int eraseExpiredPersonalData(int limit) {
        if (limit < 1 || limit > MAX_ERASE_BATCH_SIZE) {
            throw new InvalidReservationException("eraseLimit");
        }
        LocalDateTime erasedDttm = now();
        LocalDateTime cutoffDttm = erasedDttm.minus(
                securityProperties.personalDataRetention());
        List<Reservation> targets = reservationMapper
                .searchPersonalDataEraseTargets(cutoffDttm, limit);
        for (Reservation target : targets) {
            reservationMapper.updateReservation(
                    target.erasePersonalData(erasedDttm));
        }
        return targets.size();
    }

    private List<Long> validateSeatIds(List<Long> values) {
        if (values == null || values.isEmpty()
                || values.stream().anyMatch(
                value -> value == null || value < 1)) {
            throw new InvalidReservationException("seatIds");
        }
        LinkedHashSet<Long> unique = new LinkedHashSet<>(values);
        if (unique.size() != values.size()) {
            throw new InvalidReservationException("duplicateSeatIds");
        }
        return List.copyOf(unique);
    }

    private List<PerformanceRoundSeat> lockSeats(
            Long performanceRoundId, List<Long> seatIds) {
        Map<Long, PerformanceRoundSeat> byId = reservationMapper
                .searchRoundSeatsForUpdate(seatIds).stream()
                .collect(Collectors.toMap(
                        PerformanceRoundSeat::getPerformanceRoundSeatId,
                        Function.identity()));
        if (byId.size() != seatIds.size()) {
            throw new RoundSeatNotFoundException(null);
        }
        return seatIds.stream().map(byId::get).peek(seat -> {
            seat.validateRound(performanceRoundId);
            seat.validateReservable();
        }).toList();
    }

    private void occupySeats(
            Reservation reservation, List<PerformanceRoundSeat> seats,
            LocalDateTime occupiedDttm) {
        for (PerformanceRoundSeat seat : seats) {
            ReservationSeat reservationSeat = ReservationSeat.confirmed(
                    reservation.getReservationId(),
                    seat.getPerformanceRoundSeatId());
            reservationMapper.insertReservationSeat(reservationSeat);
            try {
                reservationMapper.insertActiveSeatOccupancy(
                        ActiveSeatOccupancy.occupy(
                                seat.getPerformanceRoundSeatId(),
                                reservationSeat.getReservationSeatId(),
                                occupiedDttm));
            } catch (DuplicateKeyException exception) {
                throw new SeatUnavailableException(
                        seat.getPerformanceRoundSeatId());
            }
        }
    }

    private void validateViewerCancellation(
            Reservation reservation, List<ReservationSeat> seats,
            boolean cancellationOpen) {
        if (!isCancelable(reservation,
                seats.stream().map(this::seatResponse).toList(),
                cancellationOpen)) {
            throw new InvalidReservationStateException(
                    "viewerCancellation");
        }
    }

    private void cancelReservation(
            Reservation current, List<ReservationSeat> seats,
            String reason, Long actorMemberId) {
        LocalDateTime cancelledDttm = now();
        for (ReservationSeat seat : seats) {
            reservationMapper.updateReservationSeat(
                    seat.cancel(reason, cancelledDttm));
        }
        reservationMapper.removeActiveSeatOccupanciesByReservation(
                current.getReservationId());
        Reservation cancelled = current.cancel(reason, cancelledDttm);
        reservationMapper.updateReservation(cancelled);
        reservationMapper.insertReservationStatusHistory(
                ReservationStatusHistory.changed(
                        current.getReservationId(), current.getStatus(),
                        cancelled.getStatus(), reason, actorMemberId,
                        cancelledDttm));
    }

    private boolean isCancelable(
            Reservation reservation, List<ReservationSeatResponse> seats,
            boolean cancellationOpen) {
        return reservation.getStatus() != ReservationStatus.CANCELLED
                && cancellationOpen
                && seats.stream().noneMatch(
                ReservationSeatResponse::checkedIn);
    }

    private ReservationSeatResponse seatResponse(ReservationSeat seat) {
        return new ReservationSeatResponse(seat.getReservationSeatId(),
                seat.getPerformanceRoundSeatId(), "", null, null, null,
                seat.getStatus(), seat.getCheckedInDttm(),
                seat.getCheckedInByMemberId());
    }

    private ReservationDetailResponse detail(
            Reservation reservation, List<ReservationSeatResponse> seats,
            boolean cancelable) {
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
                reservation.getCancelReason(), cancelable, seats);
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
