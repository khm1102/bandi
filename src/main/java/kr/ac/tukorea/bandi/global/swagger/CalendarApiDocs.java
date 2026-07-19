package kr.ac.tukorea.bandi.global.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ac.tukorea.bandi.domain.calendar.dto.request.CalendarEventCreateRequest;
import kr.ac.tukorea.bandi.domain.calendar.dto.request.CalendarEventUpdateRequest;
import kr.ac.tukorea.bandi.domain.calendar.dto.response.CalendarEventCreatedResponse;
import kr.ac.tukorea.bandi.domain.calendar.dto.response.CalendarEventResponse;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@RequestMapping("/api/calendar-events")
@Tag(name = ApiTag.CALENDAR, description = "전체·팀 일정 통합 관리 API")
@SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SCHEME)
public interface CalendarApiDocs {

    @Operation(summary = "기간별 일정 조회")
    @GetMapping
    ResponseEntity<List<CalendarEventResponse>> search(
            @LoginMember Long actorMemberId,
            @RequestParam LocalDateTime rangeStart,
            @RequestParam LocalDateTime rangeEnd,
            @RequestParam(required = false) Long teamId);

    @Operation(summary = "일정 등록")
    @PostMapping
    ResponseEntity<CalendarEventCreatedResponse> create(
            @LoginMember Long actorMemberId,
            @Valid @RequestBody CalendarEventCreateRequest request);

    @Operation(summary = "일정 수정")
    @PutMapping("/{calendarEventId}")
    ResponseEntity<Void> update(
            @LoginMember Long actorMemberId,
            @PathVariable Long calendarEventId,
            @Valid @RequestBody CalendarEventUpdateRequest request);

    @Operation(summary = "일정 삭제")
    @DeleteMapping("/{calendarEventId}")
    ResponseEntity<Void> delete(
            @LoginMember Long actorMemberId,
            @PathVariable Long calendarEventId);
}
