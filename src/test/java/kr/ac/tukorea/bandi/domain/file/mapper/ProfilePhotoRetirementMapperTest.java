package kr.ac.tukorea.bandi.domain.file.mapper;

import kr.ac.tukorea.bandi.domain.file.model.FilePurpose;
import kr.ac.tukorea.bandi.domain.file.model.ProfilePhotoRetirementManifest;
import kr.ac.tukorea.bandi.domain.file.model.ProfilePhotoRetirementStatus;
import kr.ac.tukorea.bandi.domain.file.model.StorageScope;
import kr.ac.tukorea.bandi.domain.file.model.StoredFile;
import kr.ac.tukorea.bandi.global.annotation.MapperTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@MapperTest
class ProfilePhotoRetirementMapperTest {

    private final StoredFileMapper storedFileMapper;
    private final ProfilePhotoRetirementMapper retirementMapper;

    @Autowired
    ProfilePhotoRetirementMapperTest(StoredFileMapper storedFileMapper,
                                     ProfilePhotoRetirementMapper retirementMapper) {
        this.storedFileMapper = storedFileMapper;
        this.retirementMapper = retirementMapper;
    }

    @Test
    void 프로필_사진_파일은_전용_목적과_재시도_manifest로_저장된다() {
        StoredFile photo = StoredFile.pendingProfileImage("me.png",
                "member-profile/2026/07/retirement-test", "image/png", 4,
                "a".repeat(64), null);
        photo.markReady("etag");
        storedFileMapper.insert(photo);

        retirementMapper.insert(ProfilePhotoRetirementManifest.pending(photo));

        assertThat(storedFileMapper.lookupById(photo.getStoredFileId()))
                .isPresent()
                .get()
                .extracting(StoredFile::getPurpose)
                .isEqualTo(FilePurpose.PROFILE_IMAGE);
        assertThat(retirementMapper.searchUncompleted())
                .anySatisfy(manifest -> {
                    assertThat(manifest.getStoredFileId()).isEqualTo(photo.getStoredFileId());
                    assertThat(manifest.getStatus()).isEqualTo(ProfilePhotoRetirementStatus.PENDING);
                });

        ProfilePhotoRetirementManifest manifest = retirementMapper.searchUncompleted().stream()
                .filter(candidate -> candidate.getStoredFileId().equals(photo.getStoredFileId()))
                .findFirst().orElseThrow();
        retirementMapper.updateDeleted(manifest.getManifestId(), LocalDateTime.now());
        assertThat(retirementMapper.searchUncompleted())
                .noneMatch(candidate -> candidate.getManifestId().equals(manifest.getManifestId()));
    }
}
