package kr.ac.tukorea.bandi.domain.checklist.mapper;

import kr.ac.tukorea.bandi.domain.checklist.dto.request.ChecklistItemSearchCondition;
import kr.ac.tukorea.bandi.domain.checklist.dto.response.ChecklistItemHistoryResponse;
import kr.ac.tukorea.bandi.domain.checklist.dto.response.ChecklistItemResponse;
import kr.ac.tukorea.bandi.domain.checklist.model.ChecklistItem;
import kr.ac.tukorea.bandi.domain.checklist.model.ChecklistItemHistory;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChecklistMapper {

    Optional<ChecklistItem> lookupByIdForUpdate(Long checklistItemId);

    List<ChecklistItemResponse> search(
            ChecklistItemSearchCondition condition);

    List<ChecklistItemHistoryResponse> searchHistories(
            Long checklistItemId);

    int insert(ChecklistItem item);

    int update(ChecklistItem item);

    int delete(@Param("checklistItemId") Long checklistItemId,
               @Param("actorMemberId") Long actorMemberId,
               @Param("deletedDttm") LocalDateTime deletedDttm);

    int insertHistory(ChecklistItemHistory history);
}
