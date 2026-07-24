package kr.ac.tukorea.bandi.domain.activity.mapper;

import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityManageSearchCondition;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityRecordSearchCondition;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityRecordListSearchCondition;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityFileLinkResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityRecordContentResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityRecordManageContentResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityRecordSummaryResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityReviewCsvRow;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityRevisionResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityReviewHistoryResponse;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityFileRole;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecord;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecordFile;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecordRevision;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityReviewHistory;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

public interface ActivityRecordMapper {

    Optional<ActivityRecord> lookupById(Long activityRecordId);

    Optional<ActivityRecord> lookupByIdForUpdate(Long activityRecordId);

    Optional<ActivityRecordFile> lookupFileByIdForUpdate(Long activityRecordFileId);

    List<ActivityRecordSummaryResponse> searchApproved(ActivityRecordSearchCondition condition);

    List<ActivityRecordSummaryResponse> searchManageable(ActivityManageSearchCondition condition);

    List<ActivityRecordSummaryResponse> searchMine(ActivityRecordListSearchCondition condition);

    long countMine(ActivityRecordListSearchCondition condition);

    List<ActivityRecordSummaryResponse> searchArchive(ActivityRecordListSearchCondition condition);

    long countArchive(ActivityRecordListSearchCondition condition);

    List<ActivityRecordSummaryResponse> searchReview(ActivityRecordListSearchCondition condition);

    long countReview(ActivityRecordListSearchCondition condition);

    List<ActivityReviewCsvRow> searchReviewCsv(ActivityRecordListSearchCondition condition);

    Optional<ActivityRecordContentResponse> lookupApprovedContent(Long activityRecordId);

    Optional<ActivityRecordManageContentResponse> lookupManageContent(Long activityRecordId);

    List<ActivityFileLinkResponse> searchCurrentFileLinks(Long activityRecordId);

    List<ActivityRevisionResponse> searchRevisions(Long activityRecordId);

    List<ActivityReviewHistoryResponse> searchReviewHistories(Long activityRecordId);

    boolean existsCurrentStoredFile(@Param("activityRecordId") Long activityRecordId,
                                    @Param("storedFileId") Long storedFileId);

    boolean existsApprovedCurrentFile(@Param("activityRecordId") Long activityRecordId,
                                      @Param("storedFileId") Long storedFileId);

    int lookupNextDisplayOrder(@Param("activityRecordId") Long activityRecordId,
                               @Param("fileRole") ActivityFileRole fileRole);

    int countCurrentFiles(@Param("activityRecordId") Long activityRecordId,
                          @Param("fileRole") ActivityFileRole fileRole);

    boolean existsReportDocument(Long activityRecordId);

    Optional<Integer> lookupMaxRevisionNo(Long activityRecordId);

    int insert(ActivityRecord activityRecord);

    int update(ActivityRecord activityRecord);

    int insertFile(ActivityRecordFile activityRecordFile);

    int updateFile(ActivityRecordFile activityRecordFile);

    int insertRevision(ActivityRecordRevision revision);

    int insertReviewHistory(ActivityReviewHistory history);
}
