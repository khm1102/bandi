package kr.ac.tukorea.bandi.domain.event.mapper;

import kr.ac.tukorea.bandi.domain.event.dto.request.ClubEventSearchCondition;
import kr.ac.tukorea.bandi.domain.event.dto.response.AttendanceStatusCountResponse;
import kr.ac.tukorea.bandi.domain.event.dto.response.AttendanceHistoryResponse;
import kr.ac.tukorea.bandi.domain.event.dto.response.ClubEventResponse;
import kr.ac.tukorea.bandi.domain.event.dto.response.EventAttendanceResponse;
import kr.ac.tukorea.bandi.domain.event.dto.response.MemberAttendanceResponse;
import kr.ac.tukorea.bandi.domain.event.model.AttendanceStatus;
import kr.ac.tukorea.bandi.domain.event.model.ClubEvent;
import kr.ac.tukorea.bandi.domain.event.model.EventAttendance;
import kr.ac.tukorea.bandi.domain.event.model.EventAttendanceHistory;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

public interface ClubEventMapper {

    Optional<ClubEvent> lookupById(Long clubEventId);

    Optional<ClubEvent> lookupByIdForUpdate(Long clubEventId);

    List<ClubEventResponse> search(ClubEventSearchCondition condition);

    List<EventAttendance> searchAttendancesByIdsForUpdate(
            @Param("clubEventId") Long clubEventId,
            @Param("eventAttendanceIds") List<Long> eventAttendanceIds);

    List<EventAttendanceResponse> searchAttendanceRoster(
            @Param("clubEventId") Long clubEventId,
            @Param("status") AttendanceStatus status);

    List<AttendanceStatusCountResponse> countAttendanceStatuses(Long clubEventId);

    List<AttendanceHistoryResponse> searchAttendanceHistories(Long eventAttendanceId);

    List<MemberAttendanceResponse> searchMemberAttendances(Long memberId);

    int insert(ClubEvent clubEvent);

    int update(ClubEvent clubEvent);

    int insertAttendances(@Param("attendances") List<EventAttendance> attendances);

    int updateAttendance(EventAttendance attendance);

    int insertAttendanceHistory(EventAttendanceHistory history);
}
