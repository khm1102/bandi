package kr.ac.tukorea.bandi.domain.file.service;

import kr.ac.tukorea.bandi.domain.file.exception.FileStorageUnavailableException;
import kr.ac.tukorea.bandi.domain.file.mapper.ProfilePhotoRetirementMapper;
import kr.ac.tukorea.bandi.domain.file.model.ProfilePhotoRetirementManifest;
import kr.ac.tukorea.bandi.domain.file.model.ProfilePhotoRetirementStatus;
import kr.ac.tukorea.bandi.domain.file.model.StorageScope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProfilePhotoRetirementServiceTest {

    @Mock
    private ProfilePhotoRetirementMapper retirementMapper;
    @Mock
    private FileMetadataService metadataService;
    @Mock
    private FileObjectStorage objectStorage;

    private ProfilePhotoRetirementService retirementService;

    @BeforeEach
    void setUp() {
        retirementService = new ProfilePhotoRetirementService(retirementMapper,
                metadataService, objectStorage,
                Clock.fixed(Instant.parse("2026-07-23T00:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void 퇴역_대기_사진은_파일과_메타데이터를_지운_뒤_완료로_기록한다() {
        ProfilePhotoRetirementManifest manifest = manifest();
        given(retirementMapper.searchUncompleted()).willReturn(List.of(manifest));

        retirementService.processUncompleted();

        verify(objectStorage).remove(StorageScope.PRIVATE, "member-profile/2026/07/photo");
        verify(metadataService).remove(40L);
        verify(retirementMapper).updateDeleted(eq(1L), any());
    }

    @Test
    void 물리_파일_삭제가_실패하면_재시도_상태로_남긴다() {
        ProfilePhotoRetirementManifest manifest = manifest();
        given(retirementMapper.searchUncompleted()).willReturn(List.of(manifest));
        org.mockito.Mockito.doThrow(new FileStorageUnavailableException("disk-failure"))
                .when(objectStorage).remove(StorageScope.PRIVATE,
                        "member-profile/2026/07/photo");

        retirementService.processUncompleted();

        verify(retirementMapper).updateFailed(eq(1L), any());
    }

    private ProfilePhotoRetirementManifest manifest() {
        return new ProfilePhotoRetirementManifest(1L, 40L, StorageScope.PRIVATE,
                "member-profile/2026/07/photo", ProfilePhotoRetirementStatus.PENDING,
                0, null, null, null, null);
    }
}
