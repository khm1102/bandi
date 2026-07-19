package kr.ac.tukorea.bandi.domain.file.storage;

import io.minio.CopyObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import kr.ac.tukorea.bandi.domain.file.exception.FileStorageUnavailableException;
import kr.ac.tukorea.bandi.domain.file.model.StorageScope;
import kr.ac.tukorea.bandi.global.config.FileStorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MinioFileObjectStorageTest {

    @Mock
    private MinioClient minioClient;
    @Mock
    private ObjectWriteResponse writeResponse;

    private MinioFileObjectStorage storage;

    @BeforeEach
    void setUp() {
        storage = new MinioFileObjectStorage(minioClient, new FileStorageProperties(
                "http://localhost:9000", "access", "secret", "bandi-private",
                "bandi-public", 1024, Duration.ofMinutes(5)));
    }

    @Test
    void PRIVATE_업로드는_비공개_버킷을_사용하고_ETag를_반환한다() throws Exception {
        given(writeResponse.etag()).willReturn("etag-1");
        given(minioClient.putObject(org.mockito.ArgumentMatchers.any(PutObjectArgs.class)))
                .willReturn(writeResponse);

        String etag = storage.upload(StorageScope.PRIVATE, "activity/2026/07/id",
                "image/png", 4, () -> new ByteArrayInputStream(new byte[4]));

        assertThat(etag).isEqualTo("etag-1");
        ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient).putObject(captor.capture());
        assertThat(captor.getValue().bucket()).isEqualTo("bandi-private");
        assertThat(captor.getValue().object()).isEqualTo("activity/2026/07/id");
        assertThat(captor.getValue().contentType()).isEqualTo("image/png");
        assertThat(captor.getValue().objectSize()).isEqualTo(4);
    }

    @Test
    void 공개_승격은_PRIVATE에서_PUBLIC_버킷으로_복사한다() throws Exception {
        given(writeResponse.etag()).willReturn("public-etag");
        given(minioClient.copyObject(org.mockito.ArgumentMatchers.any(CopyObjectArgs.class)))
                .willReturn(writeResponse);

        String etag = storage.copy(StorageScope.PRIVATE, "activity/private",
                StorageScope.PUBLIC, "activity/public");

        assertThat(etag).isEqualTo("public-etag");
        ArgumentCaptor<CopyObjectArgs> captor = ArgumentCaptor.forClass(CopyObjectArgs.class);
        verify(minioClient).copyObject(captor.capture());
        assertThat(captor.getValue().bucket()).isEqualTo("bandi-public");
        assertThat(captor.getValue().object()).isEqualTo("activity/public");
        assertThat(captor.getValue().source().bucket()).isEqualTo("bandi-private");
        assertThat(captor.getValue().source().object()).isEqualTo("activity/private");
    }

    @Test
    void presigned_GET은_scope에_맞는_버킷과_만료시간을_사용한다() throws Exception {
        given(minioClient.getPresignedObjectUrl(
                org.mockito.ArgumentMatchers.any(GetPresignedObjectUrlArgs.class)))
                .willReturn("https://storage/signed");

        String url = storage.createPresignedGetUrl(StorageScope.PRIVATE,
                "activity/private", Duration.ofMinutes(5));

        assertThat(url).isEqualTo("https://storage/signed");
        ArgumentCaptor<GetPresignedObjectUrlArgs> captor =
                ArgumentCaptor.forClass(GetPresignedObjectUrlArgs.class);
        verify(minioClient).getPresignedObjectUrl(captor.capture());
        assertThat(captor.getValue().bucket()).isEqualTo("bandi-private");
        assertThat(captor.getValue().expiry()).isEqualTo(300);
    }

    @Test
    void SDK_장애는_저장소_예외로_변환하고_내부_정보를_노출하지_않는다() throws Exception {
        given(minioClient.putObject(org.mockito.ArgumentMatchers.any(PutObjectArgs.class)))
                .willThrow(new IOException("secret endpoint detail"));

        assertThatThrownBy(() -> storage.upload(StorageScope.PRIVATE, "activity/key",
                "image/png", 4, () -> new ByteArrayInputStream(new byte[4])))
                .isInstanceOf(FileStorageUnavailableException.class)
                .hasMessageNotContaining("secret endpoint detail");
    }
}
