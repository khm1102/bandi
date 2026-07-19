package kr.ac.tukorea.bandi.domain.file.mapper;

import kr.ac.tukorea.bandi.domain.file.model.StorageScope;
import kr.ac.tukorea.bandi.domain.file.model.StoredFile;
import kr.ac.tukorea.bandi.domain.file.model.UploadStatus;
import kr.ac.tukorea.bandi.global.annotation.MapperTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@MapperTest
class StoredFileMapperTest {

    private final StoredFileMapper storedFileMapper;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    StoredFileMapperTest(StoredFileMapper storedFileMapper, JdbcTemplate jdbcTemplate) {
        this.storedFileMapper = storedFileMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Test
    void PENDING_파일을_저장하고_조회한다() {
        StoredFile file = pending("activity/2026/07/first");

        storedFileMapper.insert(file);
        Optional<StoredFile> found = storedFileMapper.lookupById(file.getStoredFileId());

        assertThat(file.getStoredFileId()).isNotNull();
        assertThat(found).isPresent();
        assertThat(found.get().getOriginalName()).isEqualTo("proof.png");
        assertThat(found.get().getStorageScope()).isEqualTo(StorageScope.PRIVATE);
        assertThat(found.get().getUploadStatus()).isEqualTo(UploadStatus.PENDING);
        assertThat(found.get().getCreatedDttm()).isNotNull();
    }

    @Test
    void PENDING_파일을_READY로_전환한다() {
        StoredFile file = pending("activity/2026/07/ready");
        storedFileMapper.insert(file);

        int changed = storedFileMapper.updateReady(file.getStoredFileId(), "etag-1");

        assertThat(changed).isEqualTo(1);
        assertThat(storedFileMapper.lookupById(file.getStoredFileId()))
                .isPresent()
                .get()
                .satisfies(found -> {
                    assertThat(found.getUploadStatus()).isEqualTo(UploadStatus.READY);
                    assertThat(found.getObjectEtag()).isEqualTo("etag-1");
                });
    }

    @Test
    void READY_파일은_FAILED로_되돌릴_수_없다() {
        StoredFile file = pending("activity/2026/07/no-rollback");
        storedFileMapper.insert(file);
        storedFileMapper.updateReady(file.getStoredFileId(), "etag-1");

        int changed = storedFileMapper.updateFailed(file.getStoredFileId());

        assertThat(changed).isZero();
    }

    @Test
    void 같은_scope와_storage_key는_중복_저장할_수_없다() {
        storedFileMapper.insert(pending("activity/2026/07/duplicate"));

        assertThatThrownBy(() -> storedFileMapper.insert(pending("activity/2026/07/duplicate")))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void 논리_삭제된_파일은_조회하지_않는다() {
        StoredFile file = pending("activity/2026/07/deleted");
        storedFileMapper.insert(file);
        jdbcTemplate.update("UPDATE stored_file SET deleted_dttm = NOW(6) WHERE stored_file_id = ?",
                file.getStoredFileId());

        assertThat(storedFileMapper.lookupById(file.getStoredFileId())).isEmpty();
    }

    @Test
    void SHA256이_소문자_16진수_64자가_아니면_저장할_수_없다() {
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO stored_file (
                    original_name, storage_scope_code, storage_key, content_type,
                    size_bytes, sha256_hash, upload_status_code
                ) VALUES (?, 'PRIVATE', ?, 'image/png', 4, ?, 'PENDING')
                """, "proof.png", "activity/2026/07/invalid-hash", "not-a-hash"))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void ETag_없는_파일은_READY로_전환할_수_없다() {
        StoredFile file = pending("activity/2026/07/ready-without-etag");
        storedFileMapper.insert(file);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                UPDATE stored_file
                SET upload_status_code = 'READY'
                WHERE stored_file_id = ?
                """, file.getStoredFileId()))
                .isInstanceOf(DataAccessException.class);
    }

    private StoredFile pending(String storageKey) {
        return StoredFile.pending("proof.png", StorageScope.PRIVATE, storageKey,
                "image/png", 4, "a".repeat(64), null);
    }
}
