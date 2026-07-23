package kr.ac.tukorea.bandi.domain.resource.service;

import kr.ac.tukorea.bandi.domain.file.service.FileMetadataService;
import kr.ac.tukorea.bandi.domain.file.service.FileObjectStorage;
import kr.ac.tukorea.bandi.domain.resource.mapper.ResourceLinkPreviewRetirementMapper;
import kr.ac.tukorea.bandi.domain.resource.model.ResourceLinkPreviewRetirementManifest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceLinkPreviewRetirementService {

    private final ResourceLinkPreviewRetirementMapper retirementMapper;
    private final FileMetadataService metadataService;
    private final FileObjectStorage objectStorage;
    private final Clock clock;

    @Transactional
    public void queue(Long storedFileId) {
        try {
            retirementMapper.insert(ResourceLinkPreviewRetirementManifest.pending(
                    metadataService.lookup(storedFileId)));
        } catch (DuplicateKeyException exception) {
            log.debug("링크 카드 이미지 퇴역 manifest가 이미 존재합니다 - storedFileId={}", storedFileId);
        }
        scheduleAfterCommit();
    }

    @Transactional
    public void processUncompleted() {
        retirementMapper.searchUncompleted().forEach(this::process);
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

    private void process(ResourceLinkPreviewRetirementManifest manifest) {
        try {
            objectStorage.remove(manifest.getStorageScope(), manifest.getStorageKey());
            metadataService.remove(manifest.getStoredFileId());
            retirementMapper.updateDeleted(manifest.getManifestId(), now());
        } catch (RuntimeException exception) {
            retirementMapper.updateFailed(manifest.getManifestId(), summarize(exception));
            log.warn("링크 카드 이미지 퇴역 재시도 필요 - manifestId={}, storedFileId={}",
                    manifest.getManifestId(), manifest.getStoredFileId(), exception);
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private String summarize(RuntimeException exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank()
                ? exception.getClass().getSimpleName()
                : message.substring(0, Math.min(message.length(), 500));
    }
}
