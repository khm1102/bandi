package kr.ac.tukorea.bandi.domain.file.service;

import kr.ac.tukorea.bandi.domain.file.dto.response.FileReferenceResponse;
import kr.ac.tukorea.bandi.domain.file.exception.FileAccessDeniedException;
import kr.ac.tukorea.bandi.domain.file.exception.FileStorageUnavailableException;
import kr.ac.tukorea.bandi.domain.file.exception.InvalidFileScopeException;
import kr.ac.tukorea.bandi.domain.file.exception.InvalidFileStateException;
import kr.ac.tukorea.bandi.domain.file.model.StorageScope;
import kr.ac.tukorea.bandi.domain.file.model.StoredFile;
import kr.ac.tukorea.bandi.global.config.FileStorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    private static final Long MEMBER_ID = 3L;
    private static final Long PRIVATE_FILE_ID = 11L;
    private static final Long PUBLIC_FILE_ID = 12L;
    private static final String PRIVATE_KEY = "activity/2026/07/private-uuid";
    private static final String PUBLIC_KEY = "activity/2026/07/public-uuid";
    private static final byte[] CONTENT = new byte[]{(byte) 0x89, 0x50, 0x4e, 0x47};

    @Mock
    private FileContentInspector inspector;
    @Mock
    private StorageKeyGenerator keyGenerator;
    @Mock
    private FileMetadataService metadataService;
    @Mock
    private FileObjectStorage objectStorage;

    private FileService fileService;

    @BeforeEach
    void setUp() {
        fileService = new FileService(inspector, keyGenerator, metadataService,
                objectStorage, new FileStorageProperties("http://localhost:9000", "access", "secret",
                "private", "public", 1024, Duration.ofMinutes(5)));
    }

    @Test
    void 비공개_파일을_업로드하면_검증된_메타데이터와_객체를_저장하고_READY로_전환한다() {
        FileUploadParam param = uploadParam();
        FileInspection inspection = new FileInspection("image/png", CONTENT.length, "abc123");
        StoredFile pending = pendingPrivate(PRIVATE_KEY);
        assignId(pending, PRIVATE_FILE_ID);
        StoredFile ready = readyPrivate(PRIVATE_KEY);
        assignId(ready, PRIVATE_FILE_ID);
        given(inspector.inspect(param.originalName(), param.sizeBytes(), param.contentSource()))
                .willReturn(inspection);
        given(keyGenerator.generate("activity")).willReturn(PRIVATE_KEY);
        given(metadataService.createPending(any())).willReturn(pending);
        given(objectStorage.upload(StorageScope.PRIVATE, PRIVATE_KEY, "image/png",
                CONTENT.length, param.contentSource())).willReturn("etag-1");
        given(metadataService.markReady(PRIVATE_FILE_ID, "etag-1")).willReturn(ready);

        Long result = fileService.uploadPrivate(param);

        assertThat(result).isEqualTo(PRIVATE_FILE_ID);
        ArgumentCaptor<StoredFile> captor = ArgumentCaptor.forClass(StoredFile.class);
        verify(metadataService).createPending(captor.capture());
        assertThat(captor.getValue().getContentType()).isEqualTo("image/png");
        assertThat(captor.getValue().getStorageScope()).isEqualTo(StorageScope.PRIVATE);
        assertThat(captor.getValue().getSha256Hash()).isEqualTo("abc123");
    }

    @Test
    void 객체_업로드가_실패하면_메타데이터를_FAILED로_전환한다() {
        FileUploadParam param = uploadParam();
        StoredFile pending = pendingPrivate(PRIVATE_KEY);
        assignId(pending, PRIVATE_FILE_ID);
        given(inspector.inspect(any(), eq((long) CONTENT.length), any()))
                .willReturn(new FileInspection("image/png", CONTENT.length, "abc123"));
        given(keyGenerator.generate("activity")).willReturn(PRIVATE_KEY);
        given(metadataService.createPending(any())).willReturn(pending);
        given(objectStorage.upload(any(), any(), any(), eq((long) CONTENT.length), any()))
                .willThrow(new FileStorageUnavailableException());

        assertThatThrownBy(() -> fileService.uploadPrivate(param))
                .isInstanceOf(FileStorageUnavailableException.class);
        verify(metadataService).markFailed(PRIVATE_FILE_ID);
        verify(metadataService, never()).markReady(any(), any());
    }

    @Test
    void READY_전환이_실패하면_업로드된_객체를_삭제하고_FAILED로_전환한다() {
        FileUploadParam param = uploadParam();
        StoredFile pending = pendingPrivate(PRIVATE_KEY);
        assignId(pending, PRIVATE_FILE_ID);
        given(inspector.inspect(any(), eq((long) CONTENT.length), any()))
                .willReturn(new FileInspection("image/png", CONTENT.length, "abc123"));
        given(keyGenerator.generate("activity")).willReturn(PRIVATE_KEY);
        given(metadataService.createPending(any())).willReturn(pending);
        given(objectStorage.upload(any(), any(), any(), eq((long) CONTENT.length), any())).willReturn("etag-1");
        given(metadataService.markReady(PRIVATE_FILE_ID, "etag-1"))
                .willThrow(new IllegalStateException("db failure"));

        assertThatThrownBy(() -> fileService.uploadPrivate(param))
                .isInstanceOf(IllegalStateException.class);
        verify(objectStorage).remove(StorageScope.PRIVATE, PRIVATE_KEY);
        verify(metadataService).markFailed(PRIVATE_FILE_ID);
    }

    @Test
    void 권한이_거부된_비공개_파일은_URL을_발급하지_않는다() {
        assertThatThrownBy(() -> fileService.createPrivateDownloadUrl(
                PRIVATE_FILE_ID, FileAccessDecision.DENIED))
                .isInstanceOf(FileAccessDeniedException.class);
        verify(metadataService, never()).lookup(any());
        verify(objectStorage, never()).createPresignedGetUrl(any(), any(), any());
    }

    @Test
    void 권한이_승인된_READY_비공개_파일은_짧은_수명의_URL을_발급한다() {
        StoredFile source = readyPrivate(PRIVATE_KEY);
        assignId(source, PRIVATE_FILE_ID);
        given(metadataService.lookup(PRIVATE_FILE_ID)).willReturn(source);
        given(objectStorage.createPresignedGetUrl(StorageScope.PRIVATE, PRIVATE_KEY,
                Duration.ofMinutes(5))).willReturn("https://storage/private-signed");

        String url = fileService.createPrivateDownloadUrl(PRIVATE_FILE_ID, FileAccessDecision.GRANTED);

        assertThat(url).isEqualTo("https://storage/private-signed");
    }

    @Test
    void READY_비공개_파일은_업무_레코드에_연결할_수_있다() {
        StoredFile source = readyPrivate(PRIVATE_KEY);
        assignId(source, PRIVATE_FILE_ID);
        given(metadataService.lookup(PRIVATE_FILE_ID)).willReturn(source);

        FileReferenceResponse result = fileService.lookupPrivateReady(PRIVATE_FILE_ID);

        assertThat(result.storedFileId()).isEqualTo(PRIVATE_FILE_ID);
        assertThat(result.originalName()).isEqualTo("proof.png");
        assertThat(result.contentType()).isEqualTo("image/png");
        assertThat(result.sizeBytes()).isEqualTo(CONTENT.length);
    }

    @Test
    void PENDING_파일은_업무_레코드에_연결할_수_없다() {
        StoredFile source = pendingPrivate(PRIVATE_KEY);
        assignId(source, PRIVATE_FILE_ID);
        given(metadataService.lookup(PRIVATE_FILE_ID)).willReturn(source);

        assertThatThrownBy(() -> fileService.validatePrivateReady(PRIVATE_FILE_ID))
                .isInstanceOf(InvalidFileStateException.class);
    }

    @Test
    void READY_공개_파일은_내부_업무_레코드에_연결할_수_없다() {
        StoredFile source = readyPublic(PUBLIC_KEY);
        assignId(source, PUBLIC_FILE_ID);
        given(metadataService.lookup(PUBLIC_FILE_ID)).willReturn(source);

        assertThatThrownBy(() -> fileService.validatePrivateReady(PUBLIC_FILE_ID))
                .isInstanceOf(InvalidFileScopeException.class);
    }

    @Test
    void 비공개_READY_파일을_공개로_승격하면_원본과_분리된_PUBLIC_파일을_만든다() {
        StoredFile source = readyPrivate(PRIVATE_KEY);
        assignId(source, PRIVATE_FILE_ID);
        StoredFile publicPending = source.createPublicPromotion(PUBLIC_KEY, MEMBER_ID);
        assignId(publicPending, PUBLIC_FILE_ID);
        StoredFile publicReady = readyPublic(PUBLIC_KEY);
        assignId(publicReady, PUBLIC_FILE_ID);
        given(metadataService.lookup(PRIVATE_FILE_ID)).willReturn(source);
        given(keyGenerator.generate("activity")).willReturn(PUBLIC_KEY);
        given(metadataService.createPending(any())).willReturn(publicPending);
        given(objectStorage.copy(StorageScope.PRIVATE, PRIVATE_KEY,
                StorageScope.PUBLIC, PUBLIC_KEY)).willReturn("public-etag");
        given(metadataService.markReady(PUBLIC_FILE_ID, "public-etag")).willReturn(publicReady);

        Long result = fileService.promoteToPublic(PRIVATE_FILE_ID, "activity", MEMBER_ID);

        assertThat(result).isEqualTo(PUBLIC_FILE_ID);
        assertThat(source.getStorageScope()).isEqualTo(StorageScope.PRIVATE);
        assertThat(source.getStorageKey()).isEqualTo(PRIVATE_KEY);
    }

    private FileUploadParam uploadParam() {
        return new FileUploadParam("activity", "proof.png", CONTENT.length,
                () -> new ByteArrayInputStream(CONTENT), MEMBER_ID);
    }

    private StoredFile pendingPrivate(String key) {
        return StoredFile.pending("proof.png", StorageScope.PRIVATE, key,
                "image/png", CONTENT.length, "abc123", MEMBER_ID);
    }

    private StoredFile readyPrivate(String key) {
        StoredFile file = pendingPrivate(key);
        file.markReady("etag-1");
        return file;
    }

    private StoredFile readyPublic(String key) {
        StoredFile file = StoredFile.pending("proof.png", StorageScope.PUBLIC, key,
                "image/png", CONTENT.length, "abc123", MEMBER_ID);
        file.markReady("public-etag");
        return file;
    }

    private void assignId(StoredFile file, Long id) {
        try {
            var field = StoredFile.class.getDeclaredField("storedFileId");
            field.setAccessible(true);
            field.set(file, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
