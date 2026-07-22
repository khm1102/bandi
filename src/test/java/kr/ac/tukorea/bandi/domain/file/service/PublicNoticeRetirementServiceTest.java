package kr.ac.tukorea.bandi.domain.file.service;

import kr.ac.tukorea.bandi.domain.file.mapper.PublicNoticeRetirementMapper;
import kr.ac.tukorea.bandi.domain.file.model.PublicNoticeRetirementManifest;
import kr.ac.tukorea.bandi.domain.file.model.PublicNoticeRetirementReport;
import kr.ac.tukorea.bandi.domain.file.model.PublicNoticeRetirementStatus;
import kr.ac.tukorea.bandi.domain.file.model.StorageScope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PublicNoticeRetirementServiceTest {

    @Mock
    private PublicNoticeRetirementMapper retirementMapper;
    @Mock
    private FileObjectStorage objectStorage;

    private PublicNoticeRetirementService retirementService;

    @BeforeEach
    void setUp() {
        retirementService = new PublicNoticeRetirementService(retirementMapper, objectStorage);
    }

    @Test
    void 보고는_후보를_동기화하지만_파일을_삭제하지_않는다() {
        PublicNoticeRetirementManifest pending = manifest(1L,
                PublicNoticeRetirementStatus.PENDING);
        PublicNoticeRetirementManifest retained = manifest(2L,
                PublicNoticeRetirementStatus.RETAINED_SHARED);
        given(retirementMapper.searchAll()).willReturn(List.of(pending, retained));

        PublicNoticeRetirementReport report = retirementService.report();

        verify(retirementMapper).insertMissingManifestEntries();
        verify(retirementMapper).markPendingSharedReferences();
        verify(objectStorage, never()).remove(StorageScope.PUBLIC, "notice/retire-1");
        assertThat(report.totalCount()).isEqualTo(2);
        assertThat(report.pendingCount()).isEqualTo(1);
        assertThat(report.retainedSharedCount()).isEqualTo(1);
    }

    @Test
    void 적용은_공유되지_않은_파일만_삭제하고_완료로_기록한다() {
        PublicNoticeRetirementManifest pending = manifest(1L,
                PublicNoticeRetirementStatus.PENDING);
        given(retirementMapper.searchPending()).willReturn(List.of(pending));
        given(retirementMapper.searchAll()).willReturn(List.of(manifest(1L,
                PublicNoticeRetirementStatus.DELETED)));

        PublicNoticeRetirementReport report = retirementService.apply();

        verify(objectStorage).remove(StorageScope.PUBLIC, "notice/retire-1");
        verify(retirementMapper).markDeleted(1L);
        assertThat(report.deletedCount()).isEqualTo(1);
    }

    @Test
    void 이미_없는_파일도_저장소가_성공으로_처리하면_완료로_기록한다() {
        PublicNoticeRetirementManifest pending = manifest(1L,
                PublicNoticeRetirementStatus.PENDING);
        given(retirementMapper.searchPending()).willReturn(List.of(pending));
        given(retirementMapper.searchAll()).willReturn(List.of(manifest(1L,
                PublicNoticeRetirementStatus.DELETED)));

        retirementService.apply();

        verify(retirementMapper).markDeleted(1L);
    }

    @Test
    void 파일_삭제가_실패하면_실패_상태를_기록하고_다음_후보를_계속_처리한다() {
        PublicNoticeRetirementManifest failed = manifest(1L,
                PublicNoticeRetirementStatus.PENDING);
        PublicNoticeRetirementManifest succeeded = manifest(2L,
                PublicNoticeRetirementStatus.PENDING);
        given(retirementMapper.searchPending()).willReturn(List.of(failed, succeeded));
        given(retirementMapper.searchAll()).willReturn(List.of(
                manifest(1L, PublicNoticeRetirementStatus.FAILED),
                manifest(2L, PublicNoticeRetirementStatus.DELETED)));
        org.mockito.Mockito.doThrow(new IllegalStateException("storage unavailable"))
                .when(objectStorage).remove(StorageScope.PUBLIC, "notice/retire-1");

        PublicNoticeRetirementReport report = retirementService.apply();

        verify(retirementMapper).markFailed(1L, "IllegalStateException");
        verify(retirementMapper).markDeleted(2L);
        assertThat(report.failedCount()).isEqualTo(1);
        assertThat(report.deletedCount()).isEqualTo(1);
    }

    @Test
    void 적용_전에_새로운_첨부와_공유_참조를_다시_동기화한다() {
        given(retirementMapper.searchPending()).willReturn(List.of());
        given(retirementMapper.searchAll()).willReturn(List.of());

        retirementService.apply();

        verify(retirementMapper).insertMissingManifestEntries();
        verify(retirementMapper).markPendingSharedReferences();
    }

    private PublicNoticeRetirementManifest manifest(Long manifestId,
                                                    PublicNoticeRetirementStatus status) {
        return new PublicNoticeRetirementManifest(manifestId, manifestId + 100L,
                StorageScope.PUBLIC, "notice/retire-" + manifestId, status,
                null, LocalDateTime.of(2026, 7, 23, 12, 0), null);
    }
}
