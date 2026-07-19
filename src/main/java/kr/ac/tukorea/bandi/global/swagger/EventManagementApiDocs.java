package kr.ac.tukorea.bandi.global.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ac.tukorea.bandi.domain.event.dto.request.AttendanceProcessRequest;
import kr.ac.tukorea.bandi.domain.event.dto.request.ClubEventWriteRequest;
import kr.ac.tukorea.bandi.domain.event.dto.request.EventTargetConfirmRequest;
import kr.ac.tukorea.bandi.domain.event.dto.response.AttendanceHistoryResponse;
import kr.ac.tukorea.bandi.domain.event.dto.response.AttendanceProcessedResponse;
import kr.ac.tukorea.bandi.domain.event.dto.response.AttendanceStatusCountResponse;
import kr.ac.tukorea.bandi.domain.event.dto.response.ClubEventCreatedResponse;
import kr.ac.tukorea.bandi.domain.event.dto.response.EventAttendanceResponse;
import kr.ac.tukorea.bandi.domain.event.dto.response.EventTargetConfirmedResponse;
import kr.ac.tukorea.bandi.domain.event.model.AttendanceStatus;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("/api/event-management")
@Tag(name = ApiTag.EVENT, description = "행사 대상과 출석 처리 관리 API")
@SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SCHEME)
public interface EventManagementApiDocs {

    @Operation(summary = "행사 등록")
    @PostMapping
    ResponseEntity<ClubEventCreatedResponse> create(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @Valid @RequestBody ClubEventWriteRequest request);

    @Operation(summary = "행사 수정")
    @PutMapping("/{clubEventId}")
    ResponseEntity<Void> update(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long clubEventId,
            @Valid @RequestBody ClubEventWriteRequest request);

    @Operation(summary = "행사 대상 확정")
    @PostMapping("/{clubEventId}/targets")
    ResponseEntity<EventTargetConfirmedResponse> confirmTargets(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long clubEventId,
            @Valid @RequestBody EventTargetConfirmRequest request);

    @Operation(summary = "출석 확인 시작")
    @PostMapping("/{clubEventId}/check-in/open")
    ResponseEntity<Void> openCheckIn(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long clubEventId);

    @Operation(summary = "출석 확인 종료")
    @PostMapping("/{clubEventId}/check-in/close")
    ResponseEntity<Void> closeCheckIn(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long clubEventId);

    @Operation(summary = "행사 보관")
    @PostMapping("/{clubEventId}/archive")
    ResponseEntity<Void> archive(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long clubEventId);

    @Operation(summary = "출석 상태 일괄 처리")
    @PostMapping("/{clubEventId}/attendances/process")
    ResponseEntity<AttendanceProcessedResponse> processAttendance(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long clubEventId,
            @Valid @RequestBody AttendanceProcessRequest request);

    @Operation(summary = "행사 출석 명단 조회")
    @GetMapping("/{clubEventId}/attendances")
    ResponseEntity<List<EventAttendanceResponse>> searchRoster(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long clubEventId,
            @RequestParam(required = false) AttendanceStatus status);

    @Operation(summary = "행사 출석 상태 집계")
    @GetMapping("/{clubEventId}/attendance-counts")
    ResponseEntity<List<AttendanceStatusCountResponse>> countStatuses(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long clubEventId);

    @Operation(summary = "개별 출석 변경 이력 조회")
    @GetMapping("/attendances/{eventAttendanceId}/histories")
    ResponseEntity<List<AttendanceHistoryResponse>> searchHistories(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long eventAttendanceId);
}
