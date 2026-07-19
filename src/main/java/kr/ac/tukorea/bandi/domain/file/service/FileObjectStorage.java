package kr.ac.tukorea.bandi.domain.file.service;

import kr.ac.tukorea.bandi.domain.file.model.StorageScope;

import java.time.Duration;

public interface FileObjectStorage {

    String upload(StorageScope scope, String storageKey, String contentType,
                  long sizeBytes, FileContentSource contentSource);

    String copy(StorageScope sourceScope, String sourceKey,
                StorageScope targetScope, String targetKey);

    String createPresignedGetUrl(StorageScope scope, String storageKey, Duration lifetime);

    void remove(StorageScope scope, String storageKey);
}
