package kr.ac.tukorea.bandi.domain.event.controller;

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
import kr.ac.tukorea.bandi.domain.event.service.ClubEventService;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import kr.ac.tukorea.bandi.global.swagger.EventManagementApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class EventManagementApiController implements EventManagementApiDocs {

    private final ClubEventService clubEventService;

    @Override
    public ResponseEntity<ClubEventCreatedResponse> create(
            @LoginMember Long actorMemberId, ClubEventWriteRequest request) {
        Long id = clubEventService.create(actorMemberId, request.toCreateParam());
        return ResponseEntity.created(URI.create("/api/event-management/" + id))
                .body(new ClubEventCreatedResponse(id));
    }

    @Override
    public ResponseEntity<Void> update(@LoginMember Long actorMemberId,
                                       Long clubEventId,
                                       ClubEventWriteRequest request) {
        clubEventService.update(actorMemberId, request.toUpdateParam(clubEventId));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<EventTargetConfirmedResponse> confirmTargets(
            @LoginMember Long actorMemberId, Long clubEventId,
            EventTargetConfirmRequest request) {
        int count = clubEventService.confirmTargets(actorMemberId,
                request.toParam(clubEventId));
        return ResponseEntity.ok(new EventTargetConfirmedResponse(count));
    }

    @Override
    public ResponseEntity<Void> openCheckIn(@LoginMember Long actorMemberId,
                                            Long clubEventId) {
        clubEventService.openCheckIn(actorMemberId, clubEventId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> closeCheckIn(@LoginMember Long actorMemberId,
                                             Long clubEventId) {
        clubEventService.closeCheckIn(actorMemberId, clubEventId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> archive(@LoginMember Long actorMemberId,
                                        Long clubEventId) {
        clubEventService.archive(actorMemberId, clubEventId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<AttendanceProcessedResponse> processAttendance(
            @LoginMember Long actorMemberId, Long clubEventId,
            AttendanceProcessRequest request) {
        int count = clubEventService.processAttendance(actorMemberId,
                request.toParam(clubEventId));
        return ResponseEntity.ok(new AttendanceProcessedResponse(count));
    }

    @Override
    public ResponseEntity<List<EventAttendanceResponse>> searchRoster(
            @LoginMember Long actorMemberId, Long clubEventId,
            AttendanceStatus status) {
        return ResponseEntity.ok(clubEventService.searchAttendanceRoster(
                actorMemberId, clubEventId, status));
    }

    @Override
    public ResponseEntity<List<AttendanceStatusCountResponse>> countStatuses(
            @LoginMember Long actorMemberId, Long clubEventId) {
        return ResponseEntity.ok(clubEventService.countAttendanceStatuses(
                actorMemberId, clubEventId));
    }

    @Override
    public ResponseEntity<List<AttendanceHistoryResponse>> searchHistories(
            @LoginMember Long actorMemberId, Long eventAttendanceId) {
        return ResponseEntity.ok(clubEventService.searchAttendanceHistories(
                actorMemberId, eventAttendanceId));
    }
}
