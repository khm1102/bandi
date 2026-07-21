package kr.ac.tukorea.bandi.domain.performance.retirement.service;

import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.domain.performance.retirement.PerformanceFileRetirementResult;
import kr.ac.tukorea.bandi.domain.performance.retirement.PerformanceRetirementFile;
import kr.ac.tukorea.bandi.domain.performance.retirement.mapper.PerformanceFileRetirementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PerformanceFileRetirementService {

    private final PerformanceFileRetirementMapper retirementMapper;
    private final PerformanceFileRetirementManifestService manifestService;
    private final FileService fileService;

    public PerformanceFileRetirementResult report() {
        List<PerformanceRetirementFile> candidates = retirementMapper.searchCandidates();
        int skippedCount = 0;
        for (PerformanceRetirementFile candidate : candidates) {
            Optional<String> retainedReferenceType = lookupRetainedReferenceType(candidate);
            if (retainedReferenceType.isPresent()) {
                manifestService.recordSkipped(candidate, retainedReferenceType.get());
                skippedCount++;
                continue;
            }
            manifestService.recordPending(candidate);
        }
        int pendingCount = retirementMapper.searchPendingCandidates().size();
        return PerformanceFileRetirementResult.report(candidates.size(), pendingCount, skippedCount);
    }

    public PerformanceFileRetirementResult apply() {
        PerformanceFileRetirementResult reportResult = report();
        int skippedCount = reportResult.skippedCount();
        int deletedCount = 0;
        for (PerformanceRetirementFile candidate : retirementMapper.searchPendingCandidates()) {
            Optional<String> retainedReferenceType = lookupRetainedReferenceType(candidate);
            if (retainedReferenceType.isPresent()) {
                manifestService.recordSkipped(candidate, retainedReferenceType.get());
                skippedCount++;
                continue;
            }
            try {
                fileService.removeObjectForRetirement(candidate.storageScope(),
                        candidate.storageKey());
                manifestService.recordDeleted(candidate.storedFileId());
                deletedCount++;
            } catch (RuntimeException exception) {
                manifestService.recordFailed(candidate, exception.getClass().getSimpleName());
                throw exception;
            }
        }
        return reportResult.withDeleted(deletedCount, skippedCount);
    }

    private Optional<String> lookupRetainedReferenceType(PerformanceRetirementFile candidate) {
        return retirementMapper.lookupRetainedReferenceType(candidate.storedFileId());
    }
}
