package kr.ac.tukorea.bandi.domain.calendar.controller;

import kr.ac.tukorea.bandi.domain.calendar.dto.request.CalendarEventCreateRequest;
import kr.ac.tukorea.bandi.domain.calendar.dto.request.CalendarEventSearchCondition;
import kr.ac.tukorea.bandi.domain.calendar.dto.request.CalendarEventUpdateRequest;
import kr.ac.tukorea.bandi.domain.calendar.dto.response.CalendarEventCreatedResponse;
import kr.ac.tukorea.bandi.domain.calendar.dto.response.CalendarEventResponse;
import kr.ac.tukorea.bandi.domain.calendar.service.CalendarService;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import kr.ac.tukorea.bandi.global.swagger.CalendarApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CalendarApiController implements CalendarApiDocs {

    private final CalendarService calendarService;

    @Override
    public ResponseEntity<List<CalendarEventResponse>> search(
            @LoginMember Long actorMemberId, LocalDateTime rangeStart,
            LocalDateTime rangeEnd, Long teamId) {
        return ResponseEntity.ok(calendarService.search(actorMemberId,
                new CalendarEventSearchCondition(rangeStart, rangeEnd,
                        teamId)));
    }

    @Override
    public ResponseEntity<CalendarEventCreatedResponse> create(
            @LoginMember Long actorMemberId,
            CalendarEventCreateRequest request) {
        Long id = calendarService.create(actorMemberId, request.toParam());
        return ResponseEntity.created(URI.create("/api/calendar-events/" + id))
                .body(new CalendarEventCreatedResponse(id));
    }

    @Override
    public ResponseEntity<Void> update(@LoginMember Long actorMemberId,
                                       Long calendarEventId,
                                       CalendarEventUpdateRequest request) {
        calendarService.update(actorMemberId,
                request.toParam(calendarEventId));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> delete(@LoginMember Long actorMemberId,
                                       Long calendarEventId) {
        calendarService.delete(actorMemberId, calendarEventId);
        return ResponseEntity.noContent().build();
    }
}
