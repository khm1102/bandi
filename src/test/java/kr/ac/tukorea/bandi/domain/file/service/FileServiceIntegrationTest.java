package kr.ac.tukorea.bandi.domain.file.service;

import kr.ac.tukorea.bandi.domain.file.dto.response.FileReferenceResponse;
import kr.ac.tukorea.bandi.domain.file.mapper.StoredFileMapper;
import kr.ac.tukorea.bandi.domain.file.model.StorageScope;
import kr.ac.tukorea.bandi.domain.file.model.StoredFile;
import kr.ac.tukorea.bandi.domain.file.model.UploadStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayInputStream;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class FileServiceIntegrationTest {

    private static final byte[] PNG = new byte[]{
            (byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 0x01};

    private final FileService fileService;
    private final FileObjectStorage objectStorage;
    private final StoredFileMapper storedFileMapper;
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;

    @Autowired
    FileServiceIntegrationTest(FileService fileService, FileObjectStorage objectStorage,
                               StoredFileMapper storedFileMapper, JdbcTemplate jdbcTemplate,
                               PlatformTransactionManager transactionManager) {
        this.fileService = fileService;
        this.objectStorage = objectStorage;
        this.storedFileMapper = storedFileMapper;
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Test
    void 검증_DB_상태전이_로컬_저장_직접_스트리밍이_하나의_흐름으로_동작한다() throws Exception {
        StoredFile stored = null;
        try {
            Long storedFileId = fileService.uploadPrivate(new FileUploadParam("activity", "proof.png",
                    PNG.length, () -> new ByteArrayInputStream(PNG), null));
            stored = storedFileMapper.lookupById(storedFileId).orElseThrow();

            assertThat(stored.getUploadStatus()).isEqualTo(UploadStatus.READY);
            assertThat(stored.getObjectEtag()).isNotBlank();
            assertThat(storedFileMapper.lookupById(stored.getStoredFileId()))
                    .isPresent()
                    .get()
                    .extracting(StoredFile::getUploadStatus)
                    .isEqualTo(UploadStatus.READY);

            try (var input = fileService.openPrivateDownload(
                    stored.getStoredFileId(), FileAccessDecision.GRANTED).resource().getInputStream()) {
                assertThat(input.readAllBytes()).isEqualTo(PNG);
            }
        } finally {
            if (stored != null) {
                objectStorage.remove(StorageScope.PRIVATE, stored.getStorageKey());
                jdbcTemplate.update("DELETE FROM stored_file WHERE stored_file_id = ?",
                        stored.getStoredFileId());
            }
        }
    }

    @Test
    void 외부_트랜잭션이_시작된_뒤_업로드한_파일도_즉시_조회할_수_있다() {
        AtomicReference<Long> storedFileId = new AtomicReference<>();
        try {
            FileReferenceResponse result = transactionTemplate.execute(status -> {
                storedFileMapper.lookupById(Long.MAX_VALUE);
                Long createdId = fileService.uploadPrivate(new FileUploadParam(
                        "activity-report", "proof.png", PNG.length,
                        () -> new ByteArrayInputStream(PNG), null));
                storedFileId.set(createdId);
                return fileService.lookupPrivateReadyForUpdate(createdId);
            });

            assertThat(result).isNotNull();
            assertThat(result.storedFileId()).isEqualTo(storedFileId.get());
        } finally {
            removeStoredFile(storedFileId.get());
        }
    }

    private void removeStoredFile(Long storedFileId) {
        if (storedFileId == null) {
            return;
        }
        storedFileMapper.lookupById(storedFileId).ifPresent(stored ->
                objectStorage.remove(StorageScope.PRIVATE, stored.getStorageKey()));
        jdbcTemplate.update("DELETE FROM stored_file WHERE stored_file_id = ?",
                storedFileId);
    }
}
