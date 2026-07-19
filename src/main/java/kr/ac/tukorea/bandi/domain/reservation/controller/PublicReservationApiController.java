package kr.ac.tukorea.bandi.domain.reservation.controller;

import kr.ac.tukorea.bandi.domain.reservation.dto.request.ReservationCancelRequest;
import kr.ac.tukorea.bandi.domain.reservation.dto.request.ReservationCreateRequest;
import kr.ac.tukorea.bandi.domain.reservation.dto.request.ReservationLookupRequest;
import kr.ac.tukorea.bandi.domain.reservation.dto.response.ReservationCreatedResponse;
import kr.ac.tukorea.bandi.domain.reservation.dto.response.ReservationDetailResponse;
import kr.ac.tukorea.bandi.domain.reservation.dto.response.RoundSeatResponse;
import kr.ac.tukorea.bandi.domain.reservation.service.ReservationService;
import kr.ac.tukorea.bandi.domain.reservation.service.RoundSeatService;
import kr.ac.tukorea.bandi.global.swagger.PublicReservationApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PublicReservationApiController implements PublicReservationApiDocs {

    private final ReservationService reservationService;
    private final RoundSeatService roundSeatService;

    @Override
    public ResponseEntity<List<RoundSeatResponse>> searchAvailableSeats(
            String slug, Long roundId) {
        return ResponseEntity.ok(roundSeatService.searchAvailable(
                slug, roundId));
    }

    @Override
    public ResponseEntity<ReservationCreatedResponse> create(
            String slug, ReservationCreateRequest request) {
        ReservationCreatedResponse response = reservationService.create(
                slug, request.toParam());
        URI location = URI.create("/reserve/lookup");
        return ResponseEntity.created(location).body(response);
    }

    @Override
    public ResponseEntity<ReservationDetailResponse> lookup(
            ReservationLookupRequest request) {
        return ResponseEntity.ok(reservationService.lookup(
                request.lookupToken()));
    }

    @Override
    public ResponseEntity<Void> cancel(ReservationCancelRequest request) {
        reservationService.cancel(request.lookupToken(), request.reason());
        return ResponseEntity.noContent().build();
    }
}
