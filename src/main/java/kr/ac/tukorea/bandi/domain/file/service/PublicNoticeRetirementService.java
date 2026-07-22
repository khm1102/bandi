package kr.ac.tukorea.bandi.domain.file.service;

import kr.ac.tukorea.bandi.domain.file.mapper.PublicNoticeRetirementMapper;
import kr.ac.tukorea.bandi.domain.file.model.PublicNoticeRetirementManifest;
import kr.ac.tukorea.bandi.domain.file.model.PublicNoticeRetirementReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicNoticeRetirementService {

    private final PublicNoticeRetirementMapper retirementMapper;
    private final FileObjectStorage objectStorage;

    public PublicNoticeRetirementReport report() {
        synchronizeManifest();
        return summarize();
    }

    public PublicNoticeRetirementReport apply() {
        synchronizeManifest();
        List<PublicNoticeRetirementManifest> pendingManifests = retirementMapper.searchPending();

        for (PublicNoticeRetirementManifest manifest : pendingManifests) {
            removeObject(manifest);
        }
        return summarize();
    }

    private void synchronizeManifest() {
        retirementMapper.insertMissingManifestEntries();
        retirementMapper.markPendingSharedReferences();
    }

    private void removeObject(PublicNoticeRetirementManifest manifest) {
        try {
            objectStorage.remove(manifest.storageScope(), manifest.storageKey());
            retirementMapper.markDeleted(manifest.publicNoticeRetirementManifestId());
            log.info("외부 공시 파일 퇴역 완료 - manifestId={}",
                    manifest.publicNoticeRetirementManifestId());
        } catch (RuntimeException exception) {
            retirementMapper.markFailed(manifest.publicNoticeRetirementManifestId(),
                    exception.getClass().getSimpleName());
            log.warn("외부 공시 파일 퇴역 실패 - manifestId={}, reason={}",
                    manifest.publicNoticeRetirementManifestId(),
                    exception.getClass().getSimpleName());
        }
    }

    private PublicNoticeRetirementReport summarize() {
        return PublicNoticeRetirementReport.from(retirementMapper.searchAll());
    }
}
