package kr.ac.tukorea.bandi.global.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ac.tukorea.bandi.domain.reservation.dto.request.ReservationCancelRequest;
import kr.ac.tukorea.bandi.domain.reservation.dto.request.ReservationCreateRequest;
import kr.ac.tukorea.bandi.domain.reservation.dto.request.ReservationLookupRequest;
import kr.ac.tukorea.bandi.domain.reservation.dto.response.ReservationCreatedResponse;
import kr.ac.tukorea.bandi.domain.reservation.dto.response.ReservationDetailResponse;
import kr.ac.tukorea.bandi.domain.reservation.dto.response.RoundSeatResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/public-reservations")
@Tag(name = ApiTag.RESERVATION,
        description = "외부 관람객 좌석·신청 조회·취소 API")
public interface PublicReservationApiDocs {

    @Operation(summary = "공개 회차의 신청 가능한 좌석 조회")
    @GetMapping("/{slug}/rounds/{roundId}/seats")
    ResponseEntity<List<RoundSeatResponse>> searchAvailableSeats(
            @PathVariable String slug, @PathVariable Long roundId);

    @Operation(summary = "관람 신청")
    @PostMapping("/{slug}")
    ResponseEntity<ReservationCreatedResponse> create(
            @PathVariable String slug,
            @Valid @RequestBody ReservationCreateRequest request);

    @Operation(summary = "조회 토큰으로 관람 신청 조회",
            description = "조회 토큰이 URL과 서버 접근 로그에 남지 않도록 요청 본문으로 전달합니다.")
    @PostMapping("/lookup")
    ResponseEntity<ReservationDetailResponse> lookup(
            @Valid @RequestBody ReservationLookupRequest request);

    @Operation(summary = "조회 토큰으로 관람 신청 전체 취소")
    @PostMapping("/cancel")
    ResponseEntity<Void> cancel(
            @Valid @RequestBody ReservationCancelRequest request);
}
