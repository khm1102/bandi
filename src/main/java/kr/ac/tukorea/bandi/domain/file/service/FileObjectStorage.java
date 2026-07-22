package kr.ac.tukorea.bandi.domain.file.service;

import kr.ac.tukorea.bandi.domain.file.model.StorageScope;

public interface FileObjectStorage {

    String upload(StorageScope scope, String storageKey, String contentType,
                  long sizeBytes, FileContentSource contentSource);

    String copy(StorageScope sourceScope, String sourceKey,
                StorageScope targetScope, String targetKey);

    FileContentSource open(StorageScope scope, String storageKey);

    void remove(StorageScope scope, String storageKey);
}
