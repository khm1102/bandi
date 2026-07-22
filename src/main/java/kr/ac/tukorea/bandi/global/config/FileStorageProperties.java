package kr.ac.tukorea.bandi.global.config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Files;
import java.nio.file.Path;

@Validated
@ConfigurationProperties(prefix = "bandi.storage")
public record FileStorageProperties(
        @NotNull Path root,
        @Positive long maxUploadBytes
) {

    public FileStorageProperties {
        if (root != null) {
            root = root.toAbsolutePath().normalize();
            if (!Files.isDirectory(root) || !Files.isWritable(root)) {
                throw new IllegalArgumentException("file storage root must be a writable directory");
            }
        }
    }
}
