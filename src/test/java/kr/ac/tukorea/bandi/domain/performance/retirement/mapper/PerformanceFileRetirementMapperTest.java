package kr.ac.tukorea.bandi.domain.performance.retirement.mapper;

import kr.ac.tukorea.bandi.domain.file.model.StorageScope;
import kr.ac.tukorea.bandi.domain.performance.retirement.PerformanceRetirementFile;
import kr.ac.tukorea.bandi.global.annotation.MapperTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@MapperTest
class PerformanceFileRetirementMapperTest {

    private final PerformanceFileRetirementMapper retirementMapper;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    PerformanceFileRetirementMapperTest(PerformanceFileRetirementMapper retirementMapper,
                                        JdbcTemplate jdbcTemplate) {
        this.retirementMapper = retirementMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Test
    void performance_접두사의_파일은_폐기_후보로_조회한다() {
        Long storedFileId = insertStoredFile("performance/2026/07/retirement-prefix");

        assertThat(retirementMapper.searchCandidates())
                .extracting(PerformanceRetirementFile::storedFileId)
                .contains(storedFileId);
    }

    @Test
    void 유지되는_소품_사진은_공유_참조로_식별한다() {
        Long storedFileId = insertStoredFile("performance/2026/07/retirement-shared");
        jdbcTemplate.update("""
                INSERT INTO asset_item (
                    name, category_code, tracking_type_code, owner_type_code,
                    total_quantity, storage_location, status_code, photo_file_id
                ) VALUES (?, 'PROP', 'QUANTITY', 'CLUB', 1, '창고', 'AVAILABLE', ?)
                """, "공유 소품 사진", storedFileId);

        Optional<String> referenceType =
                retirementMapper.lookupRetainedReferenceType(storedFileId);

        assertThat(referenceType).contains("asset_item");
    }

    @Test
    void 보고_후_적용_대기_후보를_manifest에서_조회한다() {
        Long storedFileId = insertStoredFile("performance/2026/07/retirement-pending");
        PerformanceRetirementFile candidate = new PerformanceRetirementFile(
                storedFileId, StorageScope.PRIVATE,
                "performance/2026/07/retirement-pending");

        retirementMapper.upsertPending(candidate);

        assertThat(retirementMapper.searchPendingCandidates())
                .extracting(PerformanceRetirementFile::storedFileId)
                .contains(storedFileId);
    }

    private Long insertStoredFile(String storageKey) {
        jdbcTemplate.update("""
                INSERT INTO stored_file (
                    original_name, storage_scope_code, storage_key, content_type,
                    size_bytes, sha256_hash, upload_status_code
                ) VALUES ('retirement.png', 'PRIVATE', ?, 'image/png', 4, ?, 'PENDING')
                """, storageKey, "a".repeat(64));
        return jdbcTemplate.queryForObject("""
                SELECT stored_file_id
                FROM stored_file
                WHERE storage_scope_code = 'PRIVATE'
                  AND storage_key = ?
                """, Long.class, storageKey);
    }
}
