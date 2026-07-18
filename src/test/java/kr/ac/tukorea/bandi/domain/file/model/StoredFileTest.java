package kr.ac.tukorea.bandi.domain.file.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.ac.tukorea.bandi.domain.file.exception.InvalidFileScopeException;
import kr.ac.tukorea.bandi.domain.file.exception.InvalidFileStateException;
import org.junit.jupiter.api.Test;

class StoredFileTest {

    private static final String SHA256 = "a".repeat(64);

    @Test
    void 업로드_대기_파일을_생성한다() {
        StoredFile file = pendingFile();

        assertThat(file.getStoredFileId()).isNull();
        assertThat(file.getStorageScope()).isEqualTo(StorageScope.PRIVATE);
        assertThat(file.getUploadStatus()).isEqualTo(UploadStatus.PENDING);
        assertThat(file.getObjectEtag()).isNull();
    }

    @Test
    void 대기_파일을_준비_완료로_전환한다() {
        StoredFile file = pendingFile();

        file.markReady("etag-1");

        assertThat(file.getUploadStatus()).isEqualTo(UploadStatus.READY);
        assertThat(file.getObjectEtag()).isEqualTo("etag-1");
    }

    @Test
    void 대기_상태가_아니면_준비_완료로_전환할_수_없다() {
        StoredFile file = pendingFile();
        file.markFailed();

        assertThatThrownBy(() -> file.markReady("etag-1"))
                .isInstanceOf(InvalidFileStateException.class);
    }

    @Test
    void 빈_ETag로_준비_완료_전환을_할_수_없다() {
        StoredFile file = pendingFile();

        assertThatThrownBy(() -> file.markReady(" "))
                .isInstanceOf(InvalidFileStateException.class);
    }

    @Test
    void 저장_실패_시_대기_파일을_실패로_전환한다() {
        StoredFile file = pendingFile();

        file.markFailed();

        assertThat(file.getUploadStatus()).isEqualTo(UploadStatus.FAILED);
    }

    @Test
    void 준비되지_않은_파일은_다운로드할_수_없다() {
        StoredFile file = pendingFile();

        assertThatThrownBy(file::validateReady)
                .isInstanceOf(InvalidFileStateException.class);
    }

    @Test
    void 공개_파일은_비공개_다운로드_URL을_발급할_수_없다() {
        StoredFile file = readyPublicFile();

        assertThatThrownBy(file::validatePrivateDownload)
                .isInstanceOf(InvalidFileScopeException.class);
    }

    @Test
    void 공개_승격은_별도_PUBLIC_대기_파일을_만든다() {
        StoredFile source = pendingFile();
        source.markReady("private-etag");

        StoredFile promoted = source.createPublicPromotion(
                "public-notice/2026/07/22222222-2222-2222-2222-222222222222",
                2L);

        assertThat(source.getStorageScope()).isEqualTo(StorageScope.PRIVATE);
        assertThat(source.getUploadStatus()).isEqualTo(UploadStatus.READY);
        assertThat(promoted.getStorageScope()).isEqualTo(StorageScope.PUBLIC);
        assertThat(promoted.getUploadStatus()).isEqualTo(UploadStatus.PENDING);
        assertThat(promoted.getStorageKey()).isNotEqualTo(source.getStorageKey());
        assertThat(promoted.getSha256Hash()).isEqualTo(source.getSha256Hash());
        assertThat(promoted.getUploadedByMemberId()).isEqualTo(2L);
    }

    @Test
    void 준비되지_않은_파일은_공개_승격할_수_없다() {
        StoredFile source = pendingFile();

        assertThatThrownBy(() -> source.createPublicPromotion("public/2026/07/new-key", 2L))
                .isInstanceOf(InvalidFileStateException.class);
    }

    private StoredFile pendingFile() {
        return StoredFile.pending(
                "final-script.pdf",
                StorageScope.PRIVATE,
                "resource/2026/07/11111111-1111-1111-1111-111111111111",
                "application/pdf",
                12L,
                SHA256,
                1L);
    }

    private StoredFile readyPublicFile() {
        StoredFile file = StoredFile.pending(
                "poster.png",
                StorageScope.PUBLIC,
                "public-notice/2026/07/33333333-3333-3333-3333-333333333333",
                "image/png",
                12L,
                SHA256,
                1L);
        file.markReady("public-etag");
        return file;
    }
}
