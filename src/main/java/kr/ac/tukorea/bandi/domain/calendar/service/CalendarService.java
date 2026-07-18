package kr.ac.tukorea.bandi.domain.calendar.service;

import kr.ac.tukorea.bandi.domain.calendar.dto.request.CalendarEventCreateParam;
import kr.ac.tukorea.bandi.domain.calendar.dto.request.CalendarEventSearchCondition;
import kr.ac.tukorea.bandi.domain.calendar.dto.request.CalendarEventUpdateParam;
import kr.ac.tukorea.bandi.domain.calendar.dto.response.CalendarEventResponse;
import kr.ac.tukorea.bandi.domain.calendar.exception.CalendarAccessDeniedException;
import kr.ac.tukorea.bandi.domain.calendar.exception.CalendarEventNotFoundException;
import kr.ac.tukorea.bandi.domain.calendar.mapper.CalendarEventMapper;
import kr.ac.tukorea.bandi.domain.calendar.model.CalendarEvent;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarService {

    private final CalendarEventMapper calendarEventMapper;
    private final MemberService memberService;
    private final Clock clock;

    public List<CalendarEventResponse> search(Long actorMemberId,
                                               CalendarEventSearchCondition condition) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        if (!access.canReadInternal()) {
            throw new CalendarAccessDeniedException();
        }
        return calendarEventMapper.searchOverlapping(condition).stream()
                .map(CalendarEventResponse::from)
                .toList();
    }

    @Transactional
    public Long create(Long actorMemberId, CalendarEventCreateParam param) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        validateManagement(access, param.teamId());
        if (param.teamId() != null) {
            memberService.validateActiveTeam(param.teamId());
        }

        CalendarEvent event = CalendarEvent.create(param.teamId(), param.title(),
                param.description(), param.startDttm(), param.endDttm(), param.allDay(),
                param.place(), actorMemberId);
        calendarEventMapper.insert(event);
        return event.getCalendarEventId();
    }

    @Transactional
    public void update(Long actorMemberId, CalendarEventUpdateParam param) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        validateInternalAccess(access);
        CalendarEvent original = lock(param.calendarEventId());
        validateManagement(access, original.getTeamId());
        validateManagement(access, param.teamId());
        if (param.teamId() != null) {
            memberService.validateActiveTeam(param.teamId());
        }

        CalendarEvent changed = original.change(param.teamId(), param.title(),
                param.description(), param.startDttm(), param.endDttm(), param.allDay(),
                param.place(), actorMemberId);
        calendarEventMapper.update(changed);
    }

    @Transactional
    public void delete(Long actorMemberId, Long calendarEventId) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        validateInternalAccess(access);
        CalendarEvent event = lock(calendarEventId);
        validateManagement(access, event.getTeamId());
        calendarEventMapper.delete(calendarEventId, actorMemberId, LocalDateTime.now(clock));
    }

    private CalendarEvent lock(Long calendarEventId) {
        return calendarEventMapper.lookupByIdForUpdate(calendarEventId)
                .orElseThrow(() -> new CalendarEventNotFoundException(calendarEventId));
    }

    private void validateInternalAccess(MemberAccessContext access) {
        if (!access.canReadInternal()) {
            throw new CalendarAccessDeniedException();
        }
    }

    private void validateManagement(MemberAccessContext access, Long targetTeamId) {
        boolean allowed = targetTeamId == null
                ? access.canManageGlobal()
                : access.canManageTeam(targetTeamId);
        if (!allowed) {
            throw new CalendarAccessDeniedException();
        }
    }
}
