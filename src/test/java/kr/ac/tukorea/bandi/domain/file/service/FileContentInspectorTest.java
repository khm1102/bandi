package kr.ac.tukorea.bandi.domain.file.service;

import kr.ac.tukorea.bandi.domain.file.exception.FileTooLargeException;
import kr.ac.tukorea.bandi.domain.file.exception.InvalidFileException;
import kr.ac.tukorea.bandi.global.config.FileStorageProperties;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.file.Path;
import java.util.HexFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileContentInspectorTest {

    private static final long MAX_BYTES = 1024;

    private final FileContentInspector inspector = new FileContentInspector(new FileStorageProperties(
            Path.of(System.getProperty("java.io.tmpdir")), MAX_BYTES));

    @Test
    void PNG_시그니처를_확인하고_서버가_판별한_MIME과_해시를_반환한다() {
        byte[] content = png();

        FileInspection inspection = inspector.inspect("poster.png", content.length, source(content));

        assertThat(inspection.contentType()).isEqualTo("image/png");
        assertThat(inspection.sizeBytes()).isEqualTo(content.length);
        assertThat(inspection.sha256Hash()).isEqualTo(sha256(content));
    }

    @Test
    void DOCX는_ZIP_내부_구조까지_확인한다() throws IOException {
        byte[] content = officeFile("word/document.xml");

        FileInspection inspection = inspector.inspect("script.docx", content.length, source(content));

        assertThat(inspection.contentType())
                .isEqualTo("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }

    @Test
    void HWPX는_미디어_타입과_필수_문서_구조를_확인한다() throws IOException {
        byte[] content = hwpxFile(true);

        FileInspection inspection = inspector.inspect("activity.hwpx", content.length,
                source(content));

        assertThat(inspection.contentType()).isEqualTo("application/hwp+zip");
    }

    @Test
    void 필수_구조가_없는_ZIP을_HWPX로_위장하면_거부한다() throws IOException {
        byte[] content = hwpxFile(false);

        assertThatThrownBy(() -> inspector.inspect("fake.hwpx", content.length,
                source(content))).isInstanceOf(InvalidFileException.class);
    }

    @Test
    void 확장자와_실제_시그니처가_다르면_거부한다() {
        byte[] content = png();

        assertThatThrownBy(() -> inspector.inspect("poster.jpg", content.length, source(content)))
                .isInstanceOf(InvalidFileException.class);
    }

    @Test
    void 허용되지_않은_확장자는_거부한다() {
        byte[] content = "plain text".getBytes(StandardCharsets.UTF_8);

        assertThatThrownBy(() -> inspector.inspect("memo.txt", content.length, source(content)))
                .isInstanceOf(InvalidFileException.class);
    }

    @Test
    void 경로가_포함된_파일명은_거부한다() {
        byte[] content = png();

        assertThatThrownBy(() -> inspector.inspect("../poster.png", content.length, source(content)))
                .isInstanceOf(InvalidFileException.class);
    }

    @Test
    void 빈_파일은_거부한다() {
        byte[] content = new byte[0];

        assertThatThrownBy(() -> inspector.inspect("empty.png", 0, source(content)))
                .isInstanceOf(InvalidFileException.class);
    }

    @Test
    void 파일_내용_스트림이_없으면_거부한다() {
        assertThatThrownBy(() -> inspector.inspect("empty.png", 1, null))
                .isInstanceOf(InvalidFileException.class);
    }

    @Test
    void 허용_용량을_초과하면_스트림을_다_읽기_전에_거부한다() {
        byte[] content = new byte[(int) MAX_BYTES + 1];

        assertThatThrownBy(() -> inspector.inspect("large.png", content.length, source(content)))
                .isInstanceOf(FileTooLargeException.class);
    }

    @Test
    void 선언한_크기와_실제_크기가_다르면_거부한다() {
        byte[] content = png();

        assertThatThrownBy(() -> inspector.inspect("poster.png", content.length + 1, source(content)))
                .isInstanceOf(InvalidFileException.class);
    }

    @Test
    void 일반_ZIP을_DOCX로_위장하면_거부한다() throws IOException {
        byte[] content = officeFile("other/file.xml");

        assertThatThrownBy(() -> inspector.inspect("fake.docx", content.length, source(content)))
                .isInstanceOf(InvalidFileException.class);
    }

    @Test
    void 프로필_사진은_이미지_확장자만_허용한다() {
        byte[] content = "%PDF-1.7".getBytes(StandardCharsets.UTF_8);

        assertThatThrownBy(() -> inspector.inspectProfileImage("me.pdf", content.length,
                source(content))).isInstanceOf(InvalidFileException.class);
    }

    @Test
    void 프로필_사진은_5MiB를_초과하면_읽기_전에_거부한다() {
        FileContentInspector profileInspector = new FileContentInspector(new FileStorageProperties(
                Path.of(System.getProperty("java.io.tmpdir")), 6L * 1024 * 1024));

        assertThatThrownBy(() -> profileInspector.inspectProfileImage("me.png",
                5L * 1024 * 1024 + 1, source(new byte[]{1})))
                .isInstanceOf(FileTooLargeException.class);
    }

    @Test
    void 공지_본문_이미지는_JPG_PNG_WebP만_10MiB_이하로_허용한다() {
        FileContentInspector noticeInspector = new FileContentInspector(new FileStorageProperties(
                Path.of(System.getProperty("java.io.tmpdir")), 11L * 1024 * 1024));
        byte[] content = png();

        FileInspection inspection = noticeInspector.inspectNoticeInlineImage("poster.png",
                content.length, source(content));

        assertThat(inspection.contentType()).isEqualTo("image/png");
        assertThatThrownBy(() -> noticeInspector.inspectNoticeInlineImage("animation.gif",
                content.length, source(content))).isInstanceOf(InvalidFileException.class);
        assertThatThrownBy(() -> noticeInspector.inspectNoticeInlineImage("large.png",
                10L * 1024 * 1024 + 1, source(new byte[]{1})))
                .isInstanceOf(FileTooLargeException.class);
    }

    private FileContentSource source(byte[] content) {
        return () -> new ByteArrayInputStream(content);
    }

    private byte[] png() {
        return new byte[]{(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 0x01};
    }

    private byte[] officeFile(String documentEntry) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(output)) {
            zip.putNextEntry(new ZipEntry("[Content_Types].xml"));
            zip.write("types".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry(documentEntry));
            zip.write("document".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        return output.toByteArray();
    }

    private byte[] hwpxFile(boolean includeSection) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(output)) {
            zip.putNextEntry(new ZipEntry("mimetype"));
            zip.write("application/hwp+zip".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("Contents/content.hpf"));
            zip.write("content".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            if (includeSection) {
                zip.putNextEntry(new ZipEntry("Contents/section0.xml"));
                zip.write("<section/>".getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
        return output.toByteArray();
    }

    private String sha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
