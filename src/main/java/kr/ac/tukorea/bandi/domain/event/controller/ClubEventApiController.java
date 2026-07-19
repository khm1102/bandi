package kr.ac.tukorea.bandi.domain.event.controller;

import kr.ac.tukorea.bandi.domain.event.dto.request.ClubEventSearchCondition;
import kr.ac.tukorea.bandi.domain.event.dto.request.ClubEventSearchFilter;
import kr.ac.tukorea.bandi.domain.event.dto.response.ClubEventResponse;
import kr.ac.tukorea.bandi.domain.event.dto.response.MemberAttendanceResponse;
import kr.ac.tukorea.bandi.domain.event.service.ClubEventService;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import kr.ac.tukorea.bandi.global.swagger.ClubEventApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ClubEventApiController implements ClubEventApiDocs {

    private final ClubEventService clubEventService;

    @Override
    public ResponseEntity<List<ClubEventResponse>> search(
            @LoginMember Long actorMemberId, ClubEventSearchFilter filter,
            LocalDateTime rangeStart, LocalDateTime rangeEnd,
            int offset, int limit) {
        return ResponseEntity.ok(clubEventService.search(actorMemberId,
                new ClubEventSearchCondition(filter.status(), rangeStart, rangeEnd,
                        offset, limit)));
    }

    @Override
    public ResponseEntity<List<MemberAttendanceResponse>> searchMyAttendances(
            @LoginMember Long actorMemberId) {
        return ResponseEntity.ok(clubEventService.searchMyAttendances(actorMemberId));
    }
}
