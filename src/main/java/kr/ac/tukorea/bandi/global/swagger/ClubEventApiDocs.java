package kr.ac.tukorea.bandi.global.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.tukorea.bandi.domain.event.dto.response.ClubEventResponse;
import kr.ac.tukorea.bandi.domain.event.dto.response.MemberAttendanceResponse;
import kr.ac.tukorea.bandi.domain.event.model.ClubEventStatus;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@RequestMapping("/api/events")
@Tag(name = ApiTag.EVENT, description = "행사와 내 참석 대상 조회 API")
@SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SCHEME)
public interface ClubEventApiDocs {

    @Operation(summary = "행사 목록 조회")
    @GetMapping
    ResponseEntity<List<ClubEventResponse>> search(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @RequestParam(required = false) ClubEventStatus status,
            @RequestParam(required = false) LocalDateTime rangeStart,
            @RequestParam(required = false) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit);

    @Operation(summary = "내 행사 참석 대상과 상태 조회")
    @GetMapping("/my-attendances")
    ResponseEntity<List<MemberAttendanceResponse>> searchMyAttendances(
            @Parameter(hidden = true) @LoginMember Long actorMemberId);
}
