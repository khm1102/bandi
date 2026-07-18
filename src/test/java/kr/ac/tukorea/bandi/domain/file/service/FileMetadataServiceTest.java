package kr.ac.tukorea.bandi.domain.file.service;

import kr.ac.tukorea.bandi.domain.file.exception.StoredFileNotFoundException;
import kr.ac.tukorea.bandi.domain.file.mapper.StoredFileMapper;
import kr.ac.tukorea.bandi.domain.file.model.StorageScope;
import kr.ac.tukorea.bandi.domain.file.model.StoredFile;
import kr.ac.tukorea.bandi.domain.file.model.UploadStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FileMetadataServiceTest {

    private static final Long FILE_ID = 7L;

    @Mock
    private StoredFileMapper storedFileMapper;

    private FileMetadataService metadataService;

    @BeforeEach
    void setUp() {
        metadataService = new FileMetadataService(storedFileMapper);
    }

    @Test
    void PENDING_메타데이터를_저장한다() {
        StoredFile pending = pending();

        StoredFile result = metadataService.createPending(pending);

        assertThat(result).isSameAs(pending);
        verify(storedFileMapper).insert(pending);
    }

    @Test
    void 존재하지_않는_파일은_조회할_수_없다() {
        given(storedFileMapper.lookupById(FILE_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> metadataService.lookup(FILE_ID))
                .isInstanceOf(StoredFileNotFoundException.class);
    }

    @Test
    void 잠근_PENDING_파일을_READY로_전환한다() {
        StoredFile pending = pending();
        given(storedFileMapper.lookupByIdForUpdate(FILE_ID)).willReturn(Optional.of(pending));

        StoredFile result = metadataService.markReady(FILE_ID, "etag-1");

        assertThat(result.getUploadStatus()).isEqualTo(UploadStatus.READY);
        assertThat(result.getObjectEtag()).isEqualTo("etag-1");
        verify(storedFileMapper).updateReady(FILE_ID, "etag-1");
    }

    @Test
    void 잠근_PENDING_파일을_FAILED로_전환한다() {
        StoredFile pending = pending();
        given(storedFileMapper.lookupByIdForUpdate(FILE_ID)).willReturn(Optional.of(pending));

        metadataService.markFailed(FILE_ID);

        assertThat(pending.getUploadStatus()).isEqualTo(UploadStatus.FAILED);
        verify(storedFileMapper).updateFailed(FILE_ID);
    }

    private StoredFile pending() {
        return StoredFile.pending("proof.png", StorageScope.PRIVATE,
                "activity/2026/07/id", "image/png", 9, "a".repeat(64), null);
    }
}
