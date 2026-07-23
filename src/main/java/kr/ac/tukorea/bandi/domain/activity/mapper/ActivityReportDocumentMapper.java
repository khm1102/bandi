package kr.ac.tukorea.bandi.domain.activity.mapper;

import kr.ac.tukorea.bandi.domain.activity.model.ActivityReportDocumentRecord;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityReportParticipantRecord;

import java.util.List;
import java.util.Optional;

public interface ActivityReportDocumentMapper {

    Optional<ActivityReportDocumentRecord> lookupByActivityRecordId(Long activityRecordId);

    List<ActivityReportParticipantRecord> searchParticipants(Long activityReportDocumentId);

    int insert(ActivityReportDocumentRecord document);

    int update(ActivityReportDocumentRecord document);

    int insertParticipant(ActivityReportParticipantRecord participant);

    int removeParticipants(Long activityReportDocumentId);
}
