package kr.ac.tukorea.bandi.global.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import kr.ac.tukorea.bandi.global.security.LoginMember;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("/api/reservation-management")
@Tag(name = ApiTag.RESERVATION,
        description = "관람 신청·좌석·공연 당일 입장 관리 API")
@SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SCHEME)
public interface ReservationManagementApiDocs {

    @Operation(summary = "회차 좌석 목록 조회")
    @GetMapping("/rounds/{roundId}/seats")
    ResponseEntity<List<RoundSeatResponse>> searchSeats(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long roundId,
            @RequestParam Long projectId);

    @Operation(summary = "회차 좌석 등록")
    @PostMapping("/seats")
    ResponseEntity<ReservationIdentifierResponse> createSeat(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @Valid @RequestBody RoundSeatRequest request);

    @Operation(summary = "회차 좌석 상태 변경")
    @PatchMapping("/seats/{seatId}/status")
    ResponseEntity<Void> changeSeatStatus(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long seatId,
            @Valid @RequestBody RoundSeatStatusRequest request);

    @Operation(summary = "회차별 관람 신청 목록 조회")
    @GetMapping("/rounds/{roundId}/reservations")
    ResponseEntity<List<ReservationDetailResponse>> searchReservations(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long roundId,
            @RequestParam Long projectId,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit);

    @Operation(summary = "운영진 관람 신청 취소")
    @PatchMapping("/reservations/{reservationId}/cancel")
    ResponseEntity<Void> cancelReservation(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long reservationId,
            @Valid @RequestBody ReservationManagementCancelRequest request);

    @Operation(summary = "QR 입장 토큰으로 신청 조회")
    @PostMapping("/rounds/{roundId}/entry/lookup")
    ResponseEntity<ReservationDetailResponse> lookupEntry(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long roundId,
            @Valid @RequestBody EntryTokenRequest request);

    @Operation(summary = "선택 좌석 입장 처리")
    @PostMapping("/rounds/{roundId}/entry/check-ins")
    ResponseEntity<Void> checkIn(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long roundId,
            @Valid @RequestBody EntryCheckInRequest request);

    @Operation(summary = "신청번호와 이름으로 입장 신청 보조 조회")
    @PostMapping("/rounds/{roundId}/entry/search")
    ResponseEntity<ReservationDetailResponse> searchEntry(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long roundId,
            @Valid @RequestBody EntrySearchRequest request);

    @Operation(summary = "잘못 처리한 좌석 입장 취소")
    @PostMapping("/rounds/{roundId}/entry/check-in-cancellations")
    ResponseEntity<Void> cancelCheckIn(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long roundId,
            @Valid @RequestBody EntryCancelRequest request);

    @Operation(summary = "회차별 신청·입장 지표 조회")
    @GetMapping("/rounds/{roundId}/metrics")
    ResponseEntity<ReservationMetricsResponse> lookupMetrics(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long roundId,
            @RequestParam Long projectId);
}
