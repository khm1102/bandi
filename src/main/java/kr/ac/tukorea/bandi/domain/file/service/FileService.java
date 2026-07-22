package kr.ac.tukorea.bandi.domain.file.service;

import kr.ac.tukorea.bandi.domain.file.dto.response.FileReferenceResponse;
import kr.ac.tukorea.bandi.domain.file.exception.FileAccessDeniedException;
import kr.ac.tukorea.bandi.domain.file.exception.InvalidFileException;
import kr.ac.tukorea.bandi.domain.file.model.StorageScope;
import kr.ac.tukorea.bandi.domain.file.model.StoredFile;
import kr.ac.tukorea.bandi.domain.file.exception.FileStorageUnavailableException;
import kr.ac.tukorea.bandi.global.response.FileDownloadResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class FileService {

    private final FileContentInspector inspector;
    private final StorageKeyGenerator keyGenerator;
    private final FileMetadataService metadataService;
    private final FileObjectStorage objectStorage;
    public FileService(FileContentInspector inspector,
                       StorageKeyGenerator keyGenerator,
                       FileMetadataService metadataService,
                       FileObjectStorage objectStorage) {
        this.inspector = inspector;
        this.keyGenerator = keyGenerator;
        this.metadataService = metadataService;
        this.objectStorage = objectStorage;
    }

    public Long uploadPrivate(FileUploadParam param) {
        FileInspection inspection = inspector.inspect(
                param.originalName(), param.sizeBytes(), param.contentSource());
        return uploadPrivate(param, inspection, StoredFile.pending(
                param.originalName(), StorageScope.PRIVATE,
                keyGenerator.generate(param.domain()), inspection.contentType(),
                inspection.sizeBytes(), inspection.sha256Hash(), param.uploadedByMemberId()));
    }

    public Long uploadProfileImage(FileUploadParam param) {
        FileInspection inspection = inspector.inspectProfileImage(
                param.originalName(), param.sizeBytes(), param.contentSource());
        String storageKey = keyGenerator.generate("member-profile");
        StoredFile pending = StoredFile.pendingProfileImage(param.originalName(), storageKey,
                inspection.contentType(), inspection.sizeBytes(), inspection.sha256Hash(),
                param.uploadedByMemberId());
        return uploadPrivate(param, inspection, pending);
    }

    private Long uploadPrivate(FileUploadParam param, FileInspection inspection,
                               StoredFile pending) {
        String storageKey = pending.getStorageKey();
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

    public FileDownloadResponse openPrivateDownload(Long storedFileId, FileAccessDecision accessDecision) {
        if (accessDecision != FileAccessDecision.GRANTED) {
            throw new FileAccessDeniedException();
        }
        StoredFile file = metadataService.lookup(storedFileId);
        file.validatePrivateDownload();
        return download(file, StorageScope.PRIVATE);
    }

    public FileDownloadResponse openProfileImageDownload(Long storedFileId) {
        StoredFile file = metadataService.lookup(storedFileId);
        file.validateProfileImage();
        return download(file, StorageScope.PRIVATE);
    }

    public FileDownloadResponse openPublicDownload(Long storedFileId) {
        StoredFile file = metadataService.lookup(storedFileId);
        file.validatePublicUse();
        return download(file, StorageScope.PUBLIC);
    }

    public void validatePrivateReady(Long storedFileId) {
        lookupPrivateReady(storedFileId);
    }

    public FileReferenceResponse lookupPrivateReady(Long storedFileId) {
        return FileReferenceResponse.from(lookupPrivateStoredFile(storedFileId));
    }

    public void validatePrivateReadyOwnedBy(Long storedFileId, Long memberId) {
        StoredFile file = lookupPrivateStoredFile(storedFileId);
        if (!file.isUploadedBy(memberId)) {
            throw new FileAccessDeniedException();
        }
    }

    public void validateProfileImageReadyOwnedBy(Long storedFileId, Long memberId) {
        StoredFile file = metadataService.lookup(storedFileId);
        file.validateProfileImage();
        if (!file.isUploadedBy(memberId)) {
            throw new FileAccessDeniedException();
        }
    }

    public StoredFile lookupProfileImageReadyOwnedBy(Long storedFileId, Long memberId) {
        StoredFile file = metadataService.lookup(storedFileId);
        file.validateProfileImage();
        if (!file.isUploadedBy(memberId)) {
            throw new FileAccessDeniedException();
        }
        return file;
    }

    public void validatePublicReady(Long storedFileId) {
        lookupPublicReady(storedFileId);
    }

    public FileReferenceResponse lookupPublicReady(Long storedFileId) {
        StoredFile file = metadataService.lookup(storedFileId);
        file.validatePublicUse();
        return FileReferenceResponse.from(file);
    }

    public void validatePublicImageReady(Long storedFileId) {
        FileReferenceResponse file = lookupPublicReady(storedFileId);
        if (file.contentType() == null
                || !file.contentType().startsWith("image/")) {
            throw new InvalidFileException("contentType");
        }
    }

    public Long promoteToPublic(Long privateStoredFileId, String domain,
                                Long uploadedByMemberId) {
        StoredFile source = lookupPrivateStoredFile(privateStoredFileId);
        if (!source.isUploadedBy(uploadedByMemberId)) {
            throw new FileAccessDeniedException();
        }
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

    private StoredFile lookupPrivateStoredFile(Long storedFileId) {
        StoredFile file = metadataService.lookup(storedFileId);
        file.validatePrivateDownload();
        file.validateGeneralPurpose();
        return file;
    }

    private FileDownloadResponse download(StoredFile file, StorageScope scope) {
        try {
            return new FileDownloadResponse(file.getOriginalName(), file.getContentType(),
                    file.getSizeBytes(), new InputStreamResource(
                    objectStorage.open(scope, file.getStorageKey()).openStream()));
        } catch (IOException exception) {
            throw new FileStorageUnavailableException("stream-open-failed");
        }
    }
}
