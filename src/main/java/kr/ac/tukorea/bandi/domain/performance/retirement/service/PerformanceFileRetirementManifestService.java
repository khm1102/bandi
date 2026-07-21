package kr.ac.tukorea.bandi.domain.performance.retirement.service;

import kr.ac.tukorea.bandi.domain.performance.retirement.PerformanceRetirementFile;
import kr.ac.tukorea.bandi.domain.performance.retirement.mapper.PerformanceFileRetirementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceFileRetirementManifestService {

    private final PerformanceFileRetirementMapper retirementMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordPending(PerformanceRetirementFile retirementFile) {
        retirementMapper.upsertPending(retirementFile);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSkipped(PerformanceRetirementFile retirementFile,
                              String retainedReferenceType) {
        retirementMapper.upsertSkipped(retirementFile, retainedReferenceType);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordDeleted(Long storedFileId) {
        retirementMapper.updateDeleted(storedFileId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailed(PerformanceRetirementFile retirementFile,
                             String failureReason) {
        retirementMapper.updateFailed(retirementFile.storedFileId(), failureReason);
    }
}
