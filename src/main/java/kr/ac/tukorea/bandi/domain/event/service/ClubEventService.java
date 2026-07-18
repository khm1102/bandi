package kr.ac.tukorea.bandi.domain.event.service;

import kr.ac.tukorea.bandi.domain.calendar.dto.request.CalendarEventCreateParam;
import kr.ac.tukorea.bandi.domain.calendar.dto.request.CalendarEventUpdateParam;
import kr.ac.tukorea.bandi.domain.calendar.service.CalendarService;
import kr.ac.tukorea.bandi.domain.event.dto.request.AttendanceProcessParam;
import kr.ac.tukorea.bandi.domain.event.dto.request.ClubEventCreateParam;
import kr.ac.tukorea.bandi.domain.event.dto.request.ClubEventSearchCondition;
import kr.ac.tukorea.bandi.domain.event.dto.request.ClubEventUpdateParam;
import kr.ac.tukorea.bandi.domain.event.dto.request.EventTargetConfirmParam;
import kr.ac.tukorea.bandi.domain.event.dto.response.AttendanceStatusCountResponse;
import kr.ac.tukorea.bandi.domain.event.dto.response.AttendanceHistoryResponse;
import kr.ac.tukorea.bandi.domain.event.dto.response.ClubEventResponse;
import kr.ac.tukorea.bandi.domain.event.dto.response.EventAttendanceResponse;
import kr.ac.tukorea.bandi.domain.event.dto.response.MemberAttendanceResponse;
import kr.ac.tukorea.bandi.domain.event.exception.ClubEventAccessDeniedException;
import kr.ac.tukorea.bandi.domain.event.exception.ClubEventNotFoundException;
import kr.ac.tukorea.bandi.domain.event.exception.EventAttendanceNotFoundException;
import kr.ac.tukorea.bandi.domain.event.exception.InvalidClubEventException;
import kr.ac.tukorea.bandi.domain.event.mapper.ClubEventMapper;
import kr.ac.tukorea.bandi.domain.event.model.AttendanceStatus;
import kr.ac.tukorea.bandi.domain.event.model.ClubEvent;
import kr.ac.tukorea.bandi.domain.event.model.EventAttendance;
import kr.ac.tukorea.bandi.domain.event.model.EventAttendanceHistory;
import kr.ac.tukorea.bandi.domain.event.model.EventTargetScope;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubEventService {

    private final ClubEventMapper clubEventMapper;
    private final MemberService memberService;
    private final CalendarService calendarService;
    private final Clock clock;

    @Transactional
    public Long create(Long actorMemberId, ClubEventCreateParam param) {
        validateAdmin(actorMemberId);
        validateTeam(param.targetScope(), param.teamId());
        Long calendarEventId = calendarService.create(actorMemberId,
                new CalendarEventCreateParam(param.teamId(), param.title(),
                        calendarDescription(param.title(), param.description()),
                        param.startDttm(), param.endDttm(),
                        false, param.place()));
        ClubEvent event = ClubEvent.draft(calendarEventId, param.targetScope(),
                param.teamId(), param.title(), param.description(), param.place(),
                param.startDttm(), param.endDttm(), param.checkInStartDttm(),
                param.checkInEndDttm(), actorMemberId);
        clubEventMapper.insert(event);
        return event.getClubEventId();
    }

    @Transactional
    public void update(Long actorMemberId, ClubEventUpdateParam param) {
        validateAdmin(actorMemberId);
        ClubEvent event = lock(param.clubEventId());
        validateTeam(param.targetScope(), param.teamId());
        ClubEvent changed = event.edit(param.targetScope(), param.teamId(),
                param.title(), param.description(), param.place(),
                param.startDttm(), param.endDttm(), param.checkInStartDttm(),
                param.checkInEndDttm(), actorMemberId);
        calendarService.update(actorMemberId, new CalendarEventUpdateParam(
                event.getCalendarEventId(), param.teamId(), param.title(),
                calendarDescription(param.title(), param.description()),
                param.startDttm(), param.endDttm(),
                false, param.place()));
        clubEventMapper.update(changed);
    }

    @Transactional
    public int confirmTargets(Long actorMemberId, EventTargetConfirmParam param) {
        validateAdmin(actorMemberId);
        ClubEvent event = lock(param.clubEventId());
        ClubEvent scheduled = event.schedule(actorMemberId);
        List<Long> memberIds = resolveTargetMemberIds(
                event, param.selectedMemberIds());
        if (memberIds.isEmpty()) {
            throw new InvalidClubEventException("empty-targets");
        }
        List<EventAttendance> attendances = memberIds.stream()
                .map(memberId -> EventAttendance.pending(
                        event.getClubEventId(), memberId))
                .toList();
        clubEventMapper.insertAttendances(attendances);
        clubEventMapper.update(scheduled);
        return attendances.size();
    }

    @Transactional
    public void openCheckIn(Long actorMemberId, Long clubEventId) {
        validateAdmin(actorMemberId);
        ClubEvent event = lock(clubEventId);
        clubEventMapper.update(event.openCheckIn(
                actorMemberId, LocalDateTime.now(clock)));
    }

    @Transactional
    public void closeCheckIn(Long actorMemberId, Long clubEventId) {
        validateAdmin(actorMemberId);
        clubEventMapper.update(lock(clubEventId).closeCheckIn(actorMemberId));
    }

    @Transactional
    public void archive(Long actorMemberId, Long clubEventId) {
        validateAdmin(actorMemberId);
        clubEventMapper.update(lock(clubEventId).archive(actorMemberId));
    }

    @Transactional
    public int processAttendance(Long actorMemberId, AttendanceProcessParam param) {
        validateAdmin(actorMemberId);
        ClubEvent event = lock(param.clubEventId());
        LocalDateTime currentDttm = LocalDateTime.now(clock);
        event.validateAttendanceProcessing(currentDttm);
        List<Long> attendanceIds = validateAttendanceIds(
                param.eventAttendanceIds());
        List<EventAttendance> attendances = clubEventMapper
                .searchAttendancesByIdsForUpdate(
                        param.clubEventId(), attendanceIds);
        if (attendances.size() != attendanceIds.size()) {
            throw new EventAttendanceNotFoundException(
                    findMissingAttendanceId(attendanceIds, attendances));
        }
        for (EventAttendance attendance : attendances) {
            EventAttendance changed = attendance.changeStatus(param.status(),
                    actorMemberId, currentDttm, param.reason());
            clubEventMapper.updateAttendance(changed);
            clubEventMapper.insertAttendanceHistory(EventAttendanceHistory.change(
                    attendance.getEventAttendanceId(), attendance.getStatus(),
                    changed.getStatus(), changed.getReason(), actorMemberId,
                    currentDttm));
        }
        return attendances.size();
    }

    public List<ClubEventResponse> search(Long actorMemberId,
                                          ClubEventSearchCondition condition) {
        validateInternal(actorMemberId);
        return clubEventMapper.search(condition);
    }

    public List<EventAttendanceResponse> searchAttendanceRoster(
            Long actorMemberId, Long clubEventId,
            AttendanceStatus status) {
        validateAdmin(actorMemberId);
        return clubEventMapper.searchAttendanceRoster(clubEventId, status);
    }

    public List<AttendanceStatusCountResponse> countAttendanceStatuses(
            Long actorMemberId, Long clubEventId) {
        validateAdmin(actorMemberId);
        return clubEventMapper.countAttendanceStatuses(clubEventId);
    }

    public List<AttendanceHistoryResponse> searchAttendanceHistories(
            Long actorMemberId, Long eventAttendanceId) {
        validateAdmin(actorMemberId);
        return clubEventMapper.searchAttendanceHistories(eventAttendanceId);
    }

    public List<MemberAttendanceResponse> searchMyAttendances(Long actorMemberId) {
        validateInternal(actorMemberId);
        return clubEventMapper.searchMemberAttendances(actorMemberId);
    }

    private ClubEvent lock(Long clubEventId) {
        return clubEventMapper.lookupByIdForUpdate(clubEventId)
                .orElseThrow(() -> new ClubEventNotFoundException(clubEventId));
    }

    private void validateAdmin(Long actorMemberId) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        if (!access.canManageGlobal()) {
            throw new ClubEventAccessDeniedException();
        }
    }

    private void validateInternal(Long actorMemberId) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        if (!access.canReadInternal()) {
            throw new ClubEventAccessDeniedException();
        }
    }

    private void validateTeam(EventTargetScope scope, Long teamId) {
        if (scope == EventTargetScope.TEAM && teamId != null) {
            memberService.validateActiveTeam(teamId);
        }
    }

    private List<Long> resolveTargetMemberIds(ClubEvent event,
                                              List<Long> selectedMemberIds) {
        List<Long> selected = selectedMemberIds == null
                ? List.of() : selectedMemberIds;
        return switch (event.getTargetScope()) {
            case ALL -> resolveAllOrTeamTargets(null, selected);
            case TEAM -> resolveAllOrTeamTargets(event.getTeamId(), selected);
            case SELECTED -> resolveSelectedTargets(selected);
        };
    }

    private List<Long> resolveAllOrTeamTargets(Long teamId,
                                               List<Long> selectedMemberIds) {
        if (!selectedMemberIds.isEmpty()) {
            throw new InvalidClubEventException("unexpected-selected-targets");
        }
        return memberService.searchActiveMemberIds(teamId);
    }

    private List<Long> resolveSelectedTargets(List<Long> selectedMemberIds) {
        validateSelectedMemberIds(selectedMemberIds);
        Set<Long> activeMemberIds = new HashSet<>(
                memberService.searchActiveMemberIds(null));
        if (!activeMemberIds.containsAll(selectedMemberIds)) {
            throw new InvalidClubEventException("inactive-selected-target");
        }
        return List.copyOf(selectedMemberIds);
    }

    private void validateSelectedMemberIds(List<Long> selectedMemberIds) {
        if (selectedMemberIds.isEmpty()
                || selectedMemberIds.stream().anyMatch(Objects::isNull)
                || new HashSet<>(selectedMemberIds).size()
                != selectedMemberIds.size()) {
            throw new InvalidClubEventException("selected-targets");
        }
    }

    private List<Long> validateAttendanceIds(List<Long> attendanceIds) {
        if (attendanceIds == null || attendanceIds.isEmpty()
                || attendanceIds.stream().anyMatch(Objects::isNull)
                || new HashSet<>(attendanceIds).size() != attendanceIds.size()) {
            throw new InvalidClubEventException("attendance-ids");
        }
        return List.copyOf(attendanceIds);
    }

    private Long findMissingAttendanceId(List<Long> attendanceIds,
                                         List<EventAttendance> attendances) {
        Set<Long> foundIds = attendances.stream()
                .map(EventAttendance::getEventAttendanceId)
                .collect(Collectors.toSet());
        return attendanceIds.stream()
                .filter(attendanceId -> !foundIds.contains(attendanceId))
                .findFirst()
                .orElse(attendanceIds.get(0));
    }

    private String calendarDescription(String title, String description) {
        return description == null || description.isBlank()
                ? title : description;
    }
}
