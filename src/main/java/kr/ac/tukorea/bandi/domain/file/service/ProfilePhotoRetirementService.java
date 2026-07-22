package kr.ac.tukorea.bandi.domain.file.service;

import kr.ac.tukorea.bandi.domain.file.mapper.ProfilePhotoRetirementMapper;
import kr.ac.tukorea.bandi.domain.file.model.ProfilePhotoRetirementManifest;
import kr.ac.tukorea.bandi.domain.file.model.StoredFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * 프로필 사진은 교체 후 보관하지 않는다. DB 연결 해제와 실제 파일 삭제 사이의 실패는
 * manifest로 남겨 재시도한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfilePhotoRetirementService {

    private final ProfilePhotoRetirementMapper retirementMapper;
    private final FileMetadataService metadataService;
    private final FileObjectStorage objectStorage;
    private final Clock clock;

    @Transactional
    public void queue(StoredFile file) {
        try {
            retirementMapper.insert(ProfilePhotoRetirementManifest.pending(file));
        } catch (DuplicateKeyException exception) {
            log.debug("프로필 사진 퇴역 manifest가 이미 존재합니다 - storedFileId={}",
                    file.getStoredFileId());
        }
        scheduleAfterCommit();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void queueOrphan(Long storedFileId) {
        queueWithoutSchedule(metadataService.lookup(storedFileId));
    }

    @Transactional
    public void processUncompleted() {
        retirementMapper.searchUncompleted().forEach(this::process);
    }

    private void queueWithoutSchedule(StoredFile file) {
        try {
            retirementMapper.insert(ProfilePhotoRetirementManifest.pending(file));
        } catch (DuplicateKeyException exception) {
            log.debug("프로필 사진 퇴역 manifest가 이미 존재합니다 - storedFileId={}",
                    file.getStoredFileId());
        }
    }

    private void scheduleAfterCommit() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            processUncompleted();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                processUncompleted();
            }
        });
    }

    private void process(ProfilePhotoRetirementManifest manifest) {
        try {
            objectStorage.remove(manifest.getStorageScope(), manifest.getStorageKey());
            metadataService.remove(manifest.getStoredFileId());
            retirementMapper.updateDeleted(manifest.getManifestId(), now());
        } catch (RuntimeException exception) {
            retirementMapper.updateFailed(manifest.getManifestId(), summarize(exception));
            log.warn("프로필 사진 퇴역 재시도 필요 - manifestId={}, storedFileId={}",
                    manifest.getManifestId(), manifest.getStoredFileId(), exception);
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private String summarize(RuntimeException exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return message.substring(0, Math.min(message.length(), 500));
    }
}
