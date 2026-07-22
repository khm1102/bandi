package kr.ac.tukorea.bandi.domain.file.mapper;

import kr.ac.tukorea.bandi.domain.file.model.PublicNoticeRetirementManifest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PublicNoticeRetirementMapper {

    int insertMissingManifestEntries();

    int markPendingSharedReferences();

    List<PublicNoticeRetirementManifest> searchPending();

    List<PublicNoticeRetirementManifest> searchAll();

    int markDeleted(Long publicNoticeRetirementManifestId);

    int markFailed(@Param("publicNoticeRetirementManifestId") Long publicNoticeRetirementManifestId,
                   @Param("failureReason") String failureReason);
}
