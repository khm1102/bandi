package kr.ac.tukorea.bandi.domain.file.storage;

import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import kr.ac.tukorea.bandi.domain.file.exception.FileStorageUnavailableException;
import kr.ac.tukorea.bandi.domain.file.model.StorageScope;
import kr.ac.tukorea.bandi.domain.file.service.FileContentSource;
import kr.ac.tukorea.bandi.domain.file.service.FileObjectStorage;
import kr.ac.tukorea.bandi.global.config.FileStorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class MinioFileObjectStorage implements FileObjectStorage {

    private final MinioClient minioClient;
    private final FileStorageProperties properties;

    @Override
    public String upload(StorageScope scope, String storageKey, String contentType,
                         long sizeBytes, FileContentSource contentSource) {
        try (InputStream input = contentSource.openStream()) {
            ObjectWriteResponse response = minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket(scope))
                    .object(storageKey)
                    .stream(input, sizeBytes, -1)
                    .contentType(contentType)
                    .build());
            return requireEtag(response);
        } catch (Exception exception) {
            throw storageException(exception);
        }
    }

    @Override
    public String copy(StorageScope sourceScope, String sourceKey,
                       StorageScope targetScope, String targetKey) {
        try {
            ObjectWriteResponse response = minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(bucket(targetScope))
                    .object(targetKey)
                    .source(CopySource.builder()
                            .bucket(bucket(sourceScope))
                            .object(sourceKey)
                            .build())
                    .build());
            return requireEtag(response);
        } catch (Exception exception) {
            throw storageException(exception);
        }
    }

    @Override
    public String createPresignedGetUrl(StorageScope scope, String storageKey, Duration lifetime) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucket(scope))
                    .object(storageKey)
                    .expiry(Math.toIntExact(lifetime.toSeconds()))
                    .build());
        } catch (Exception exception) {
            throw storageException(exception);
        }
    }

    @Override
    public void remove(StorageScope scope, String storageKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket(scope))
                    .object(storageKey)
                    .build());
        } catch (Exception exception) {
            throw storageException(exception);
        }
    }

    private String bucket(StorageScope scope) {
        return switch (scope) {
            case PRIVATE -> properties.privateBucket();
            case PUBLIC -> properties.publicBucket();
        };
    }

    private String requireEtag(ObjectWriteResponse response) {
        String etag = response.etag();
        if (etag == null || etag.isBlank()) {
            throw new FileStorageUnavailableException("missing-etag");
        }
        return etag;
    }

    private FileStorageUnavailableException storageException(Exception exception) {
        if (exception instanceof FileStorageUnavailableException storageException) {
            return storageException;
        }
        return new FileStorageUnavailableException(exception.getClass().getSimpleName());
    }
}
