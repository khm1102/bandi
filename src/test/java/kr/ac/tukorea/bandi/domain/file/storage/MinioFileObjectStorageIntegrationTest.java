package kr.ac.tukorea.bandi.domain.file.storage;

import io.minio.MinioClient;
import kr.ac.tukorea.bandi.domain.file.model.StorageScope;
import kr.ac.tukorea.bandi.global.config.FileStorageProperties;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MinioFileObjectStorageIntegrationTest {

    private static final String ENDPOINT = "http://localhost:9000";
    private static final String PRIVATE_BUCKET = "bandi-private";
    private static final String PUBLIC_BUCKET = "bandi-public";

    @Test
    void Docker_MinIO에서_비공개_업로드_presigned_GET_공개_복사를_수행한다() throws Exception {
        FileStorageProperties properties = new FileStorageProperties(ENDPOINT, "bandi",
                "bandi-minio-dev", PRIVATE_BUCKET, PUBLIC_BUCKET, 1024, Duration.ofMinutes(5));
        MinioFileObjectStorage storage = new MinioFileObjectStorage(MinioClient.builder()
                .endpoint(ENDPOINT)
                .credentials(properties.accessKey(), properties.secretKey())
                .build(), properties);
        String privateKey = "integration/2026/07/" + UUID.randomUUID();
        String publicKey = "integration/2026/07/" + UUID.randomUUID();
        byte[] content = "bandi-minio-integration".getBytes(StandardCharsets.UTF_8);

        try {
            String privateEtag = storage.upload(StorageScope.PRIVATE, privateKey, "text/plain",
                    content.length, () -> new ByteArrayInputStream(content));
            String signedUrl = storage.createPresignedGetUrl(
                    StorageScope.PRIVATE, privateKey, Duration.ofMinutes(1));

            assertThat(privateEtag).isNotBlank();
            assertThat(get(signedUrl).body()).isEqualTo(content);
            assertThat(get("%s/%s/%s".formatted(ENDPOINT, PRIVATE_BUCKET, privateKey)).statusCode())
                    .isEqualTo(403);

            String publicEtag = storage.copy(StorageScope.PRIVATE, privateKey,
                    StorageScope.PUBLIC, publicKey);
            HttpResponse<byte[]> publicResponse = get(
                    "%s/%s/%s".formatted(ENDPOINT, PUBLIC_BUCKET, publicKey));

            assertThat(publicEtag).isNotBlank();
            assertThat(publicResponse.statusCode()).isEqualTo(200);
            assertThat(publicResponse.body()).isEqualTo(content);
        } finally {
            storage.remove(StorageScope.PRIVATE, privateKey);
            storage.remove(StorageScope.PUBLIC, publicKey);
        }
    }

    private HttpResponse<byte[]> get(String url) throws Exception {
        return HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create(url)).GET().build(),
                HttpResponse.BodyHandlers.ofByteArray());
    }
}
