package kr.ac.tukorea.bandi.domain.file.service;

import kr.ac.tukorea.bandi.domain.file.exception.FileAccessDeniedException;
import kr.ac.tukorea.bandi.domain.file.model.StorageScope;
import kr.ac.tukorea.bandi.domain.file.model.StoredFile;
import kr.ac.tukorea.bandi.global.config.FileStorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
public class FileService {

    private final FileContentInspector inspector;
    private final StorageKeyGenerator keyGenerator;
    private final FileMetadataService metadataService;
    private final FileObjectStorage objectStorage;
    private final Duration privateUrlLifetime;

    public FileService(FileContentInspector inspector,
                       StorageKeyGenerator keyGenerator,
                       FileMetadataService metadataService,
                       FileObjectStorage objectStorage,
                       FileStorageProperties properties) {
        this.inspector = inspector;
        this.keyGenerator = keyGenerator;
        this.metadataService = metadataService;
        this.objectStorage = objectStorage;
        this.privateUrlLifetime = properties.privateUrlLifetime();
    }

    public Long uploadPrivate(FileUploadParam param) {
        FileInspection inspection = inspector.inspect(
                param.originalName(), param.sizeBytes(), param.contentSource());
        String storageKey = keyGenerator.generate(param.domain());
        StoredFile pending = StoredFile.pending(param.originalName(), StorageScope.PRIVATE,
                storageKey, inspection.contentType(), inspection.sizeBytes(),
                inspection.sha256Hash(), param.uploadedByMemberId());
        StoredFile created = metadataService.createPending(pending);

        try {
            String etag = objectStorage.upload(StorageScope.PRIVATE, storageKey,
                    inspection.contentType(), inspection.sizeBytes(), param.contentSource());
            StoredFile ready = metadataService.markReady(created.getStoredFileId(), etag);
            return ready.getStoredFileId();
        } catch (RuntimeException exception) {
            compensateFailure(created.getStoredFileId(), StorageScope.PRIVATE, storageKey);
            throw exception;
        }
    }

    public String createPrivateDownloadUrl(Long storedFileId, FileAccessDecision accessDecision) {
        if (accessDecision != FileAccessDecision.GRANTED) {
            throw new FileAccessDeniedException();
        }
        StoredFile file = metadataService.lookup(storedFileId);
        file.validatePrivateDownload();
        return objectStorage.createPresignedGetUrl(StorageScope.PRIVATE,
                file.getStorageKey(), privateUrlLifetime);
    }

    public Long promoteToPublic(Long privateStoredFileId, String domain,
                                Long uploadedByMemberId) {
        StoredFile source = metadataService.lookup(privateStoredFileId);
        String publicKey = keyGenerator.generate(domain);
        StoredFile publicPending = source.createPublicPromotion(publicKey, uploadedByMemberId);
        StoredFile created = metadataService.createPending(publicPending);

        try {
            String etag = objectStorage.copy(StorageScope.PRIVATE, source.getStorageKey(),
                    StorageScope.PUBLIC, publicKey);
            StoredFile ready = metadataService.markReady(created.getStoredFileId(), etag);
            return ready.getStoredFileId();
        } catch (RuntimeException exception) {
            compensateFailure(created.getStoredFileId(), StorageScope.PUBLIC, publicKey);
            throw exception;
        }
    }

    private void compensateFailure(Long storedFileId, StorageScope scope, String storageKey) {
        try {
            objectStorage.remove(scope, storageKey);
        } catch (RuntimeException exception) {
            log.warn("파일 객체 삭제 보상 실패 - storedFileId={}, scope={}",
                    storedFileId, scope, exception);
        }
        try {
            metadataService.markFailed(storedFileId);
        } catch (RuntimeException exception) {
            log.warn("파일 메타데이터 실패 전환 보상 실패 - storedFileId={}",
                    storedFileId, exception);
        }
    }
}
