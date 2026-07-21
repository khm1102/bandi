package kr.ac.tukorea.bandi.domain.performance.retirement.mapper;

import kr.ac.tukorea.bandi.domain.performance.retirement.PerformanceRetirementFile;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

public interface PerformanceFileRetirementMapper {

    List<PerformanceRetirementFile> searchCandidates();

    List<PerformanceRetirementFile> searchPendingCandidates();

    Optional<String> lookupRetainedReferenceType(Long storedFileId);

    int upsertPending(PerformanceRetirementFile retirementFile);

    int upsertSkipped(@Param("retirementFile") PerformanceRetirementFile retirementFile,
                      @Param("retainedReferenceType") String retainedReferenceType);

    int updateDeleted(Long storedFileId);

    int updateFailed(@Param("storedFileId") Long storedFileId,
                     @Param("failureReason") String failureReason);
}
