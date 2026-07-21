package kr.ac.tukorea.bandi.domain.file.storage;

import kr.ac.tukorea.bandi.domain.file.exception.FileStorageUnavailableException;
import kr.ac.tukorea.bandi.domain.file.model.StorageScope;
import kr.ac.tukorea.bandi.domain.file.service.FileContentSource;
import kr.ac.tukorea.bandi.domain.file.service.FileObjectStorage;
import kr.ac.tukorea.bandi.global.config.FileStorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class LocalFileObjectStorage implements FileObjectStorage {

    private static final int BUFFER_SIZE = 8192;

    private final FileStorageProperties properties;

    @Override
    public String upload(StorageScope scope, String storageKey, String contentType,
                         long sizeBytes, FileContentSource contentSource) {
        Path target = resolve(scope, storageKey);
        Path temporary = null;
        try {
            Files.createDirectories(target.getParent());
            rejectExistingTarget(target);
            temporary = Files.createTempFile(target.getParent(), ".upload-", ".tmp");
            MessageDigest digest = sha256Digest();
            try (InputStream input = contentSource.openStream();
                 DigestInputStream digestInput = new DigestInputStream(input, digest)) {
                long copied = Files.copy(digestInput, temporary, StandardCopyOption.REPLACE_EXISTING);
                if (copied != sizeBytes) {
                    throw new IOException("content-size-mismatch");
                }
            }
            moveAtomically(temporary, target);
            return HexFormat.of().formatHex(digest.digest());
        } catch (IOException exception) {
            deleteQuietly(temporary);
            throw new FileStorageUnavailableException("local-upload-failed");
        }
    }

    @Override
    public String copy(StorageScope sourceScope, String sourceKey,
                       StorageScope targetScope, String targetKey) {
        Path source = resolve(sourceScope, sourceKey);
        Path target = resolve(targetScope, targetKey);
        Path temporary = null;
        try {
            requireRegularFile(source);
            Files.createDirectories(target.getParent());
            rejectExistingTarget(target);
            temporary = Files.createTempFile(target.getParent(), ".copy-", ".tmp");
            Files.copy(source, temporary, StandardCopyOption.REPLACE_EXISTING);
            moveAtomically(temporary, target);
            return sha256(target);
        } catch (IOException exception) {
            deleteQuietly(temporary);
            throw new FileStorageUnavailableException("local-copy-failed");
        }
    }

    @Override
    public FileContentSource open(StorageScope scope, String storageKey) {
        Path target = resolve(scope, storageKey);
        try {
            requireRegularFile(target);
            return () -> Files.newInputStream(target);
        } catch (IOException exception) {
            throw new FileStorageUnavailableException("local-file-unavailable");
        }
    }

    @Override
    public void remove(StorageScope scope, String storageKey) {
        try {
            Files.deleteIfExists(resolve(scope, storageKey));
        } catch (IOException exception) {
            throw new FileStorageUnavailableException("local-delete-failed");
        }
    }

    private Path resolve(StorageScope scope, String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            throw new FileStorageUnavailableException("invalid-storage-key");
        }
        Path relative = Path.of(storageKey).normalize();
        if (relative.isAbsolute() || relative.getNameCount() == 0 || relative.startsWith("..")) {
            throw new FileStorageUnavailableException("invalid-storage-key");
        }
        Path scopeRoot = properties.root().resolve(scope.name().toLowerCase(Locale.ROOT)).normalize();
        Path resolved = scopeRoot.resolve(relative).normalize();
        if (!resolved.startsWith(scopeRoot)) {
            throw new FileStorageUnavailableException("invalid-storage-key");
        }
        return resolved;
    }

    private void moveAtomically(Path temporary, Path target) throws IOException {
        try {
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            throw new IOException("atomic-move-not-supported", exception);
        }
    }

    private String sha256(Path file) throws IOException {
        MessageDigest digest = sha256Digest();
        try (InputStream input = Files.newInputStream(file);
             DigestInputStream digestInput = new DigestInputStream(input, digest)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            while (digestInput.read(buffer) != -1) {
                // DigestInputStream이 해시 상태를 갱신한다.
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private MessageDigest sha256Digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 must be available", exception);
        }
    }

    private void requireRegularFile(Path path) throws IOException {
        if (!Files.isRegularFile(path)) {
            throw new IOException("file-not-found");
        }
    }

    private void rejectExistingTarget(Path path) throws IOException {
        if (Files.exists(path)) {
            throw new IOException("target-already-exists");
        }
    }

    private void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // 원래 저장 실패를 보존한다. FileService가 DB 상태를 FAILED로 전환한다.
        }
    }
}
