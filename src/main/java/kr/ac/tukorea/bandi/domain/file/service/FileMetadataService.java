package kr.ac.tukorea.bandi.domain.file.service;

import kr.ac.tukorea.bandi.domain.file.exception.StoredFileNotFoundException;
import kr.ac.tukorea.bandi.domain.file.mapper.StoredFileMapper;
import kr.ac.tukorea.bandi.domain.file.model.StoredFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileMetadataService {

    private final StoredFileMapper storedFileMapper;

    public StoredFile lookup(Long storedFileId) {
        return storedFileMapper.lookupById(storedFileId)
                .orElseThrow(() -> new StoredFileNotFoundException(storedFileId));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public StoredFile createPending(StoredFile pendingFile) {
        storedFileMapper.insert(pendingFile);
        return pendingFile;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public StoredFile markReady(Long storedFileId, String objectEtag) {
        StoredFile file = lock(storedFileId);
        file.markReady(objectEtag);
        storedFileMapper.updateReady(storedFileId, objectEtag);
        return file;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long storedFileId) {
        StoredFile file = lock(storedFileId);
        file.markFailed();
        storedFileMapper.updateFailed(storedFileId);
    }

    private StoredFile lock(Long storedFileId) {
        return storedFileMapper.lookupByIdForUpdate(storedFileId)
                .orElseThrow(() -> new StoredFileNotFoundException(storedFileId));
    }
}
