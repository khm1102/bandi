package kr.ac.tukorea.bandi.domain.performance.retirement;

import kr.ac.tukorea.bandi.domain.file.model.StorageScope;
import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.domain.performance.retirement.mapper.PerformanceFileRetirementMapper;
import kr.ac.tukorea.bandi.domain.performance.retirement.service.PerformanceFileRetirementManifestService;
import kr.ac.tukorea.bandi.domain.performance.retirement.service.PerformanceFileRetirementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PerformanceFileRetirementServiceTest {

    private static final Long PERFORMANCE_FILE_ID = 101L;
    private static final Long SHARED_FILE_ID = 102L;
    private static final String PERFORMANCE_KEY = "performance/2026/07/poster";
    private static final String SHARED_KEY = "performance/2026/07/shared";

    @Mock
    private PerformanceFileRetirementMapper retirementMapper;
    @Mock
    private PerformanceFileRetirementManifestService manifestService;
    @Mock
    private FileService fileService;

    private PerformanceFileRetirementService service;

    @BeforeEach
    void setUp() {
        service = new PerformanceFileRetirementService(
                retirementMapper, manifestService, fileService);
    }

    @Test
    void 보고_모드는_후보를_기록하지만_MinIO_객체를_삭제하지_않는다() {
        PerformanceRetirementFile candidate = performanceFile();
        given(retirementMapper.searchCandidates()).willReturn(List.of(candidate));
        given(retirementMapper.lookupRetainedReferenceType(PERFORMANCE_FILE_ID))
                .willReturn(Optional.<String>empty());
        given(retirementMapper.searchPendingCandidates()).willReturn(List.of(candidate));

        PerformanceFileRetirementResult result = service.report();

        assertThat(result.candidateCount()).isEqualTo(1);
        assertThat(result.pendingCount()).isEqualTo(1);
        assertThat(result.skippedCount()).isZero();
        verify(manifestService).recordPending(candidate);
        verify(fileService, never()).removeObjectForRetirement(
                StorageScope.PRIVATE, PERFORMANCE_KEY);
    }

    @Test
    void 유지_서비스가_참조하는_파일은_삭제_대상에서_제외한다() {
        PerformanceRetirementFile candidate = sharedFile();
        given(retirementMapper.searchCandidates()).willReturn(List.of(candidate));
        given(retirementMapper.lookupRetainedReferenceType(SHARED_FILE_ID))
                .willReturn(Optional.of("asset_item"));
        given(retirementMapper.searchPendingCandidates()).willReturn(List.of());

        PerformanceFileRetirementResult result = service.report();

        assertThat(result.pendingCount()).isZero();
        assertThat(result.skippedCount()).isEqualTo(1);
        verify(manifestService).recordSkipped(candidate, "asset_item");
        verify(fileService, never()).removeObjectForRetirement(
                StorageScope.PRIVATE, SHARED_KEY);
    }

    @Test
    void 적용_모드는_삭제_직전에_공유_참조를_다시_검사한다() {
        PerformanceRetirementFile candidate = performanceFile();
        given(retirementMapper.searchCandidates()).willReturn(List.of(candidate));
        given(retirementMapper.searchPendingCandidates()).willReturn(List.of(candidate));
        given(retirementMapper.lookupRetainedReferenceType(PERFORMANCE_FILE_ID))
                .willReturn(Optional.<String>empty())
                .willReturn(Optional.of("resource_file"));

        PerformanceFileRetirementResult result = service.apply();

        assertThat(result.deletedCount()).isZero();
        assertThat(result.skippedCount()).isEqualTo(1);
        verify(manifestService).recordSkipped(candidate, "resource_file");
        verify(fileService, never()).removeObjectForRetirement(
                StorageScope.PRIVATE, PERFORMANCE_KEY);
    }

    @Test
    void 객체_삭제에_실패하면_manifest에_실패를_기록하고_실행을_중단한다() {
        PerformanceRetirementFile candidate = performanceFile();
        given(retirementMapper.searchCandidates()).willReturn(List.of(candidate));
        given(retirementMapper.searchPendingCandidates()).willReturn(List.of(candidate));
        given(retirementMapper.lookupRetainedReferenceType(PERFORMANCE_FILE_ID))
                .willReturn(Optional.<String>empty());
        RuntimeException failure = new IllegalStateException("minio unavailable");
        org.mockito.Mockito.doThrow(failure).when(fileService)
                .removeObjectForRetirement(StorageScope.PRIVATE, PERFORMANCE_KEY);

        assertThatThrownBy(service::apply)
                .isSameAs(failure);

        verify(manifestService).recordFailed(candidate, "IllegalStateException");
    }

    @Test
    void 이미_삭제된_후보는_다시_삭제하지_않는다() {
        PerformanceRetirementFile candidate = performanceFile();
        given(retirementMapper.searchCandidates()).willReturn(List.of(candidate));
        given(retirementMapper.lookupRetainedReferenceType(PERFORMANCE_FILE_ID))
                .willReturn(Optional.<String>empty());
        given(retirementMapper.searchPendingCandidates()).willReturn(List.of());

        PerformanceFileRetirementResult result = service.apply();

        assertThat(result.candidateCount()).isEqualTo(1);
        assertThat(result.pendingCount()).isZero();
        verify(fileService, never()).removeObjectForRetirement(
                StorageScope.PRIVATE, PERFORMANCE_KEY);
    }

    private PerformanceRetirementFile performanceFile() {
        return new PerformanceRetirementFile(PERFORMANCE_FILE_ID,
                StorageScope.PRIVATE, PERFORMANCE_KEY);
    }

    private PerformanceRetirementFile sharedFile() {
        return new PerformanceRetirementFile(SHARED_FILE_ID,
                StorageScope.PRIVATE, SHARED_KEY);
    }
}
