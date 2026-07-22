package kr.ac.tukorea.bandi.domain.calendar.mapper;

import kr.ac.tukorea.bandi.domain.calendar.dto.request.CalendarEventSearchCondition;
import kr.ac.tukorea.bandi.domain.calendar.model.CalendarEvent;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CalendarEventMapper {

    Optional<CalendarEvent> lookupById(Long calendarEventId);

    Optional<CalendarEvent> lookupByIdForUpdate(Long calendarEventId);

    List<CalendarEvent> searchOverlapping(CalendarEventSearchCondition condition);

    int insert(CalendarEvent event);

    int update(CalendarEvent event);

    int delete(@Param("calendarEventId") Long calendarEventId,
               @Param("updatedByMemberId") Long updatedByMemberId,
               @Param("deletedDttm") LocalDateTime deletedDttm);
}
