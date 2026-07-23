package kr.ac.tukorea.bandi.domain.activity.document;

import kr.ac.tukorea.bandi.domain.activity.exception.InvalidActivityReportDocumentException;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActivityReportPhotoProcessorTest {

    private final ActivityReportPhotoProcessor processor =
            new ActivityReportPhotoProcessor();

    @Test
    void 가로와_세로_사진을_크롭하지_않고_흰색_사대삼_캔버스에_배치한다() throws Exception {
        BufferedImage landscape = new BufferedImage(800, 400,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = landscape.createGraphics();
        graphics.setColor(Color.RED);
        graphics.fillRect(0, 0, 800, 400);
        graphics.dispose();

        byte[] normalized = processor.normalize(new ActivityReportPhotoParam(
                imageBytes(landscape, "png"), "image/png"));
        BufferedImage result = ImageIO.read(new ByteArrayInputStream(normalized));

        assertThat(result.getWidth()).isEqualTo(1600);
        assertThat(result.getHeight()).isEqualTo(1200);
        assertThat(new Color(result.getRGB(800, 100))).isEqualTo(Color.WHITE);
        assertThat(new Color(result.getRGB(800, 600))).isEqualTo(Color.RED);
    }

    @Test
    void 제이펙과_피엔지만_허용한다() throws Exception {
        BufferedImage image = new BufferedImage(20, 20,
                BufferedImage.TYPE_INT_RGB);

        assertThat(processor.normalize(new ActivityReportPhotoParam(
                imageBytes(image, "jpg"), "image/jpeg"))).isNotEmpty();
        assertThat(processor.normalize(new ActivityReportPhotoParam(
                imageBytes(image, "png"), "image/png"))).isNotEmpty();
        assertThatThrownBy(() -> processor.normalize(new ActivityReportPhotoParam(
                "GIF89a".getBytes(java.nio.charset.StandardCharsets.US_ASCII),
                "image/gif")))
                .isInstanceOf(InvalidActivityReportDocumentException.class);
        assertThatThrownBy(() -> processor.normalize(new ActivityReportPhotoParam(
                "RIFF0000WEBP".getBytes(java.nio.charset.StandardCharsets.US_ASCII),
                "image/webp")))
                .isInstanceOf(InvalidActivityReportDocumentException.class);
    }

    @Test
    void 손상된_이미지와_용량_초과_이미지를_거부한다() {
        assertThatThrownBy(() -> processor.normalize(new ActivityReportPhotoParam(
                new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47}, "image/png")))
                .isInstanceOf(InvalidActivityReportDocumentException.class);
        assertThatThrownBy(() -> processor.normalize(new ActivityReportPhotoParam(
                new byte[10 * 1024 * 1024 + 1], "image/png")))
                .isInstanceOf(InvalidActivityReportDocumentException.class);
    }

    @Test
    void 최대_변과_픽셀_수를_넘는_사진은_디코딩_전에_거부한다() throws Exception {
        byte[] maxSideExceeded = pngWithDimensions(12_001, 1);
        byte[] maxPixelsExceeded = pngWithDimensions(7_000, 6_000);

        assertThatThrownBy(() -> processor.normalize(new ActivityReportPhotoParam(
                maxSideExceeded, "image/png")))
                .isInstanceOf(InvalidActivityReportDocumentException.class);
        assertThatThrownBy(() -> processor.normalize(new ActivityReportPhotoParam(
                maxPixelsExceeded, "image/png")))
                .isInstanceOf(InvalidActivityReportDocumentException.class);
    }

    @Test
    void 제이펙_EXIF_회전_방향을_적용한_뒤_배치한다() throws Exception {
        BufferedImage source = new BufferedImage(40, 20,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = source.createGraphics();
        graphics.setColor(Color.RED);
        graphics.fillRect(0, 0, 20, 20);
        graphics.setColor(Color.BLUE);
        graphics.fillRect(20, 0, 20, 20);
        graphics.dispose();
        byte[] jpeg = withExifOrientation(imageBytes(source, "jpg"), 6);

        BufferedImage result = ImageIO.read(new ByteArrayInputStream(
                processor.normalize(new ActivityReportPhotoParam(jpeg, "image/jpeg"))));

        Color upper = new Color(result.getRGB(800, 300));
        Color lower = new Color(result.getRGB(800, 900));
        assertThat(upper.getRed()).isGreaterThan(220);
        assertThat(upper.getBlue()).isLessThan(35);
        assertThat(lower.getBlue()).isGreaterThan(220);
        assertThat(lower.getRed()).isLessThan(35);
        assertThat(new Color(result.getRGB(200, 600))).isEqualTo(Color.WHITE);
    }

    @Test
    void 제이펙_EXIF_반전_방향도_적용한다() throws Exception {
        BufferedImage source = new BufferedImage(40, 20,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = source.createGraphics();
        graphics.setColor(Color.RED);
        graphics.fillRect(0, 0, 20, 20);
        graphics.setColor(Color.BLUE);
        graphics.fillRect(20, 0, 20, 20);
        graphics.dispose();
        byte[] jpeg = withExifOrientation(imageBytes(source, "jpg"), 2);

        BufferedImage result = ImageIO.read(new ByteArrayInputStream(
                processor.normalize(new ActivityReportPhotoParam(jpeg, "image/jpeg"))));

        Color left = new Color(result.getRGB(400, 600));
        Color right = new Color(result.getRGB(1_200, 600));
        assertThat(left.getBlue()).isGreaterThan(220);
        assertThat(left.getRed()).isLessThan(35);
        assertThat(right.getRed()).isGreaterThan(220);
        assertThat(right.getBlue()).isLessThan(35);
    }

    private byte[] imageBytes(BufferedImage image, String format) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, format, output);
        return output.toByteArray();
    }

    private byte[] withExifOrientation(byte[] jpeg, int orientation) {
        byte[] payload = new byte[]{
                'E', 'x', 'i', 'f', 0, 0,
                'I', 'I', 42, 0, 8, 0, 0, 0,
                1, 0,
                0x12, 0x01, 3, 0, 1, 0, 0, 0,
                (byte) orientation, 0, 0, 0,
                0, 0, 0, 0,
        };
        int length = payload.length + 2;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(jpeg, 0, 2);
        output.write(0xFF);
        output.write(0xE1);
        output.write((length >>> 8) & 0xFF);
        output.write(length & 0xFF);
        output.writeBytes(payload);
        output.write(jpeg, 2, jpeg.length - 2);
        return output.toByteArray();
    }

    private byte[] pngWithDimensions(int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        byte[] png = imageBytes(image, "png");
        java.nio.ByteBuffer.wrap(png, 16, 8)
                .order(java.nio.ByteOrder.BIG_ENDIAN)
                .putInt(width)
                .putInt(height);
        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        crc.update(png, 12, 17);
        java.nio.ByteBuffer.wrap(png, 29, 4)
                .order(java.nio.ByteOrder.BIG_ENDIAN)
                .putInt((int) crc.getValue());
        return png;
    }
}
