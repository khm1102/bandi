package kr.ac.tukorea.bandi.domain.file.storage;

import kr.ac.tukorea.bandi.domain.file.exception.FileStorageUnavailableException;
import kr.ac.tukorea.bandi.domain.file.model.StorageScope;
import kr.ac.tukorea.bandi.global.config.FileStorageProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalFileObjectStorageTest {

    @TempDir
    Path storageRoot;

    @Test
    void PRIVATE_업로드는_임시_파일을_원자적으로_이동하고_SHA256을_반환한다() throws Exception {
        LocalFileObjectStorage storage = storage();
        byte[] content = "bandi".getBytes(StandardCharsets.UTF_8);

        String hash = storage.upload(StorageScope.PRIVATE, "activity/2026/07/file",
                "text/plain", content.length, () -> new ByteArrayInputStream(content));

        Path saved = storageRoot.resolve("private/activity/2026/07/file");
        assertThat(Files.readAllBytes(saved)).isEqualTo(content);
        assertThat(hash).isEqualTo("893b95a698ff9bca2806a9bbb04d1d713d33ca45ced817ab1cfa8a38b04bce92");
        try (var files = Files.list(saved.getParent())) {
            assertThat(files.map(path -> path.getFileName().toString()))
                    .noneMatch(name -> name.startsWith(".upload-"));
        }
    }

    @Test
    void scope_루트를_벗어나는_저장_키는_거부한다() {
        LocalFileObjectStorage storage = storage();

        assertThatThrownBy(() -> storage.upload(StorageScope.PRIVATE, "../outside",
                "text/plain", 1, () -> new ByteArrayInputStream(new byte[]{1})))
                .isInstanceOf(FileStorageUnavailableException.class);

        assertThat(storageRoot.resolve("outside")).doesNotExist();
    }

    @Test
    void 대상_파일이_이미_있어_원자_이동에_실패하면_기존_파일을_보존한다() throws Exception {
        LocalFileObjectStorage storage = storage();
        Path target = storageRoot.resolve("private/activity/existing");
        Files.createDirectories(target.getParent());
        Files.writeString(target, "existing", StandardCharsets.UTF_8);

        assertThatThrownBy(() -> storage.upload(StorageScope.PRIVATE, "activity/existing",
                "text/plain", 3, () -> new ByteArrayInputStream("new".getBytes(StandardCharsets.UTF_8))))
                .isInstanceOf(FileStorageUnavailableException.class);

        assertThat(Files.readString(target, StandardCharsets.UTF_8)).isEqualTo("existing");
        try (var files = Files.list(target.getParent())) {
            assertThat(files.map(path -> path.getFileName().toString()))
                    .noneMatch(name -> name.startsWith(".upload-"));
        }
    }

    @Test
    void 공개_승격은_PRIVATE_원본을_유지한_채_PUBLIC에_복사한다() throws Exception {
        LocalFileObjectStorage storage = storage();
        byte[] content = "bandi".getBytes(StandardCharsets.UTF_8);
        storage.upload(StorageScope.PRIVATE, "notice/original", "text/plain",
                content.length, () -> new ByteArrayInputStream(content));

        String hash = storage.copy(StorageScope.PRIVATE, "notice/original",
                StorageScope.PUBLIC, "notice/public");

        assertThat(Files.readAllBytes(storageRoot.resolve("private/notice/original")))
                .isEqualTo(content);
        assertThat(Files.readAllBytes(storageRoot.resolve("public/notice/public")))
                .isEqualTo(content);
        assertThat(hash).isEqualTo("893b95a698ff9bca2806a9bbb04d1d713d33ca45ced817ab1cfa8a38b04bce92");
    }

    @Test
    void 저장된_파일은_스트리밍으로_열고_삭제할_수_있다() throws Exception {
        LocalFileObjectStorage storage = storage();
        byte[] content = "bandi".getBytes(StandardCharsets.UTF_8);
        storage.upload(StorageScope.PRIVATE, "resource/file", "text/plain",
                content.length, () -> new ByteArrayInputStream(content));

        try (InputStream input = storage.open(StorageScope.PRIVATE, "resource/file").openStream()) {
            assertThat(input.readAllBytes()).isEqualTo(content);
        }
        storage.remove(StorageScope.PRIVATE, "resource/file");

        assertThat(storageRoot.resolve("private/resource/file")).doesNotExist();
    }

    private LocalFileObjectStorage storage() {
        return new LocalFileObjectStorage(new FileStorageProperties(storageRoot, 1024));
    }
}
