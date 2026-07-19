package kr.ac.tukorea.bandi.domain.reservation.controller;

import kr.ac.tukorea.bandi.domain.reservation.dto.request.EntryCancelRequest;
import kr.ac.tukorea.bandi.domain.reservation.dto.request.EntryCheckInRequest;
import kr.ac.tukorea.bandi.domain.reservation.dto.request.EntrySearchRequest;
import kr.ac.tukorea.bandi.domain.reservation.dto.request.EntryTokenRequest;
import kr.ac.tukorea.bandi.domain.reservation.dto.request.ReservationManagementCancelRequest;
import kr.ac.tukorea.bandi.domain.reservation.dto.request.RoundSeatRequest;
import kr.ac.tukorea.bandi.domain.reservation.dto.request.RoundSeatStatusRequest;
import kr.ac.tukorea.bandi.domain.reservation.dto.response.ReservationDetailResponse;
import kr.ac.tukorea.bandi.domain.reservation.dto.response.ReservationIdentifierResponse;
import kr.ac.tukorea.bandi.domain.reservation.dto.response.ReservationMetricsResponse;
import kr.ac.tukorea.bandi.domain.reservation.dto.response.RoundSeatResponse;
import kr.ac.tukorea.bandi.domain.reservation.model.ReservationStatus;
import kr.ac.tukorea.bandi.domain.reservation.service.EntryService;
import kr.ac.tukorea.bandi.domain.reservation.service.ReservationService;
import kr.ac.tukorea.bandi.domain.reservation.service.RoundSeatService;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import kr.ac.tukorea.bandi.global.swagger.ReservationManagementApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReservationManagementApiController
        implements ReservationManagementApiDocs {

    private final ReservationService reservationService;
    private final RoundSeatService roundSeatService;
    private final EntryService entryService;

    @Override
    public ResponseEntity<List<RoundSeatResponse>> searchSeats(
            @LoginMember Long actorMemberId, Long roundId, Long projectId) {
        return ResponseEntity.ok(roundSeatService.search(
                actorMemberId, projectId, roundId));
    }

    @Override
    public ResponseEntity<ReservationIdentifierResponse> createSeat(
            @LoginMember Long actorMemberId, RoundSeatRequest request) {
        Long id = roundSeatService.create(actorMemberId, request.toParam());
        return ResponseEntity.created(URI.create(
                        "/api/reservation-management/seats/" + id))
                .body(new ReservationIdentifierResponse(id));
    }

    @Override
    public ResponseEntity<Void> changeSeatStatus(
            @LoginMember Long actorMemberId, Long seatId,
            RoundSeatStatusRequest request) {
        roundSeatService.changeStatus(actorMemberId,
                request.performanceProjectId(), seatId, request.status());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<ReservationDetailResponse>> searchReservations(
            @LoginMember Long actorMemberId, Long roundId, Long projectId,
            ReservationStatus status, int offset, int limit) {
        return ResponseEntity.ok(reservationService.search(actorMemberId,
                projectId, roundId, status, offset, limit));
    }

    @Override
    public ResponseEntity<Void> cancelReservation(
            @LoginMember Long actorMemberId, Long reservationId,
            ReservationManagementCancelRequest request) {
        reservationService.cancelByAdmin(actorMemberId,
                request.performanceProjectId(), reservationId,
                request.reason());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ReservationDetailResponse> lookupEntry(
            @LoginMember Long actorMemberId, Long roundId,
            EntryTokenRequest request) {
        return ResponseEntity.ok(entryService.lookupByEntryToken(
                actorMemberId, roundId, request.entryToken()));
    }

    @Override
    public ResponseEntity<Void> checkIn(@LoginMember Long actorMemberId,
                                        Long roundId,
                                        EntryCheckInRequest request) {
        entryService.checkIn(actorMemberId, roundId, request.entryToken(),
                request.reservationSeatIds());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ReservationDetailResponse> searchEntry(
            @LoginMember Long actorMemberId, Long roundId,
            EntrySearchRequest request) {
        return ResponseEntity.ok(entryService.lookupByNumberAndName(
                actorMemberId, roundId, request.reservationNo(),
                request.applicantName()));
    }

    @Override
    public ResponseEntity<Void> cancelCheckIn(
            @LoginMember Long actorMemberId, Long roundId,
            EntryCancelRequest request) {
        entryService.cancelCheckIn(actorMemberId, roundId,
                request.reservationSeatId(), request.reason());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ReservationMetricsResponse> lookupMetrics(
            @LoginMember Long actorMemberId, Long roundId, Long projectId) {
        return ResponseEntity.ok(entryService.lookupMetrics(
                actorMemberId, projectId, roundId));
    }
}
